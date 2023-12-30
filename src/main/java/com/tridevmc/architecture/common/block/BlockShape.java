package com.tridevmc.architecture.common.block;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.client.debug.ArchitectureDebugEventListeners;
import com.tridevmc.architecture.common.block.entity.BlockEntityShape;
import com.tridevmc.architecture.common.block.state.BlockStateArchitecture;
import com.tridevmc.architecture.common.block.state.BlockStateShape;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.placement.IShapePlacementLogic;
import com.tridevmc.architecture.core.ArchitectureLog;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.ITrans3Immutable;
import com.tridevmc.architecture.core.model.Voxelizer;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.IPart;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import com.tridevmc.architecture.core.physics.AABB;
import com.tridevmc.compound.core.reflect.WrappedField;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ToolAction;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Base class for all shape blocks, handles placement logic and state definition based on the shape.
 *
 * @param <T> a self-referential generic type.
 */
public class BlockShape<T extends BlockShape<T>> extends BlockArchitecture implements EntityBlock {

    private static final WrappedField<StateDefinition<Block, BlockState>> STATE_DEFINITION = WrappedField.create(Block.class, "stateDefinition", "f_49792_");

    static {
        if (STATE_DEFINITION == null) {
            ArchitectureLog.error("Failed to find field 'stateDefinition' in Block, this is a critical error and will cause crashes.");
        }
    }

    private final EnumShape shape;

    public BlockShape(EnumShape shape) {
        this(shape, BlockBehaviour.Properties.of());
    }

    public BlockShape(EnumShape shape, Properties properties) {
        this(shape, properties, ShapeOrientation::forState);
    }

    public BlockShape(EnumShape shape, Properties properties, ShapeOrientation orientation) {
        this(shape, properties, state -> orientation);
    }

    public BlockShape(EnumShape shape, Properties properties, Function<BlockStateShape, ShapeOrientation> orientationFunc) {
        super(properties);
        this.shape = shape;
        // We use a custom BlockState implementation, so we have to use reflection to force the state definition to use it.
        var builder = new StateDefinition.Builder<Block, BlockState>(this);
        var placementLogic = this.shape.getPlacementLogic();
        // TODO: These shouldn't be null in the future, we just want to compile for now.
        if (placementLogic != null)
            placementLogic.getProperties().forEach(builder::add);
        this.createBlockStateDefinition(builder);
        //noinspection DataFlowIssue -- We know this is not null, we check for it in the static block.
        STATE_DEFINITION.set(this,
                builder.create(Block::defaultBlockState,
                        (block, propertyValues, codec) ->
                                new BlockStateShape((BlockShape<?>) block, orientationFunc, propertyValues, codec)
                )
        );
        this.registerDefaultState(this.getStateDefinition().any());
    }

    public EnumShape getShape() {
        return this.shape;
    }

    public IShapePlacementLogic<T> getPlacementLogic() {
        return this.shape.getPlacementLogic();
    }

    /**
     * Gets the block as a generic type, used when we need to return the block as a generic type.
     *
     * @return this cast to the generic type.
     */
    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

    /**
     * Gets the block state as a shape state, used when we need to reference our custom state implementation.
     *
     * @param state the state to cast.
     * @return the state cast to our custom state implementation, or null if it is not.
     */
    @Nullable
    private BlockStateShape asShapeState(BlockState state) {
        if (state instanceof BlockStateShape) {
            return (BlockStateShape) state;
        } else {
            return null;
        }
    }

    /**
     * Gets the parts of the mesh used for the given state, prior to any transformations being applied.
     *
     * @param state the state to get the mesh parts for.
     * @return the mesh parts for the given state.
     */
    public Collection<IPart<String, PolygonData>> getMeshPartsForState(BlockStateShape state) {
        return this.getMeshForState(state).getParts().values();
    }

    /**
     * Gets the mesh for the given state, prior to any transformations being applied.
     *
     * @param state the state to get the mesh for.
     * @return the mesh for the given state.
     */
    public IMesh<String, PolygonData> getMeshForState(BlockStateShape state) {
        return this.shape.getMesh();
    }

    @Override
    public ITrans3Immutable getTransformForState(BlockStateArchitecture state) {
        var shapeState = this.asShapeState(state);
        if (shapeState == null) {
            ArchitectureLog.error("BlockShape#getTransformForState called with a non-shape state, this should not happen.");
            return ITrans3.ofIdentity();
        }
        var transformationResolver = Optional.ofNullable(this.getShape().getTransformationResolver()).orElse(s -> ITrans3.ofIdentity());
        return transformationResolver.resolve(shapeState).asImmutable();
    }

    @Override
    public CompletableFuture<ImmutableList<AABB>> getBoxesForState(BlockStateArchitecture state) {
        var shapeState = this.asShapeState(state);
        if (shapeState == null) {
            ArchitectureLog.error("BlockShape#getBoxesForState called with a non-shape state, this should not happen.");
            return CompletableFuture.completedFuture(DEFAULT_BOX);
        }
        var transformationResolver = Optional.ofNullable(this.getShape().getTransformationResolver()).orElse(s -> ITrans3.ofIdentity());
        var voxelizer = shape.getVoxelizer();
        var transform = transformationResolver.resolve(shapeState);
        var voxelsCompletableFuture = Optional.ofNullable(voxelizer).map(Voxelizer::voxelize).orElse(CompletableFuture.completedFuture(DEFAULT_BOX));
        return voxelsCompletableFuture.thenApplyAsync(aabbs -> aabbs.stream().map(transform::transformAABB).collect(ImmutableList.toImmutableList()));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getPlayer() == null) {
            ArchitectureLog.error("BlockShape#getStateForPlacement called with a null player, this should not happen.");
            return this.defaultBlockState();
        }
        ShapeOrientation safeShapeOrientation = null;
        try {
            safeShapeOrientation = this.getPlacementLogic().getShapeOrientationForPlacement(
                    this.self(),
                    context.getLevel(),
                    context.getClickedPos(),
                    context.getPlayer(),
                    new BlockHitResult(
                            context.getClickLocation(),
                            context.getClickedFace(),
                            context.getClickedPos(),
                            context.isInside()
                    )
            );
        } catch (Exception e) {
            ArchitectureLog.error("BlockShape#getStateForPlacement threw an exception, this should not happen.", e);
            safeShapeOrientation = ShapeOrientation.forState(asShapeState(this.defaultBlockState()));
        }
        // Defer to the placement logic to get the correct state for the shape.
        return safeShapeOrientation.applyToState(this.defaultBlockState());
    }

    @Override
    public boolean hasDynamicShape() {
        // By returning true here we tell the game to generate a Cache object on our state.
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityShape(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return ArchitectureDebugEventListeners.onVoxelizedBlockClicked(level, pos, player, hit, getShape().getVoxelizer());
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return super.getInteractionShape(state, level, pos);
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        return super.getToolModifiedState(state, context, toolAction, simulate);
    }

    @Override
    public @Nullable PushReaction getPistonPushReaction(BlockState state) {
        return super.getPistonPushReaction(state);
    }
}
