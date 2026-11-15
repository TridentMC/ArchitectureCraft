package com.tridevmc.architecture.common.block;

import com.tridevmc.architecture.client.debug.ArchitectureDebugEventListeners;
import com.tridevmc.architecture.common.ArchitectureContent;
import com.tridevmc.architecture.common.block.entity.BlockEntityShape;
import com.tridevmc.architecture.common.block.state.BlockStateArchitecture;
import com.tridevmc.architecture.common.block.state.BlockStateShape;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.ShapeConnections;
import com.tridevmc.architecture.common.shape.ShapeMeshes;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationProperty;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyRelativeConnection;
import com.tridevmc.architecture.common.shape.placement.IShapePlacementLogic;
import com.tridevmc.architecture.common.shape.placement.ShapeConnectionProperties;
import com.tridevmc.architecture.common.shape.rule.INeighbourConnectionRule;
import com.tridevmc.architecture.core.ArchitectureLog;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.ITrans3Immutable;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.IPart;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import com.tridevmc.architecture.core.physics.AABB;

import com.google.common.collect.ImmutableList;
import com.tridevmc.compound.core.reflect.WrappedField;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.ItemAbility;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

/**
 * Base class for all shape blocks, handles placement logic and state definition based on the shape.
 *
 * @param <T> a self-referential generic type.
 */
public class BlockShape<T extends BlockShape<T>> extends BlockArchitecture implements EntityBlock, INeighbourConnectionRule {

    private static final WrappedField<StateDefinition<Block, BlockState>> STATE_DEFINITION = WrappedField.create(Block.class, "stateDefinition", "f_49792_");

    static {
        if (STATE_DEFINITION == null) {
            ArchitectureLog.error("Failed to find field 'stateDefinition' in Block, this is a critical error and will cause crashes.");
        }
    }

    private final EnumShape shape;

    public BlockShape(EnumShape shape) {
        this(shape, BlockBehaviour.Properties.of().setId(
                ResourceKey.create(Registries.BLOCK,
                        Identifier.fromNamespaceAndPath(ArchitectureContent.REGISTRY_PREFIX,
                                "shape_" + shape.getName())
                ))
        );
    }

    public BlockShape(EnumShape shape, Properties properties) {
        this(shape, properties, ShapeOrientation::forState);
    }

    public BlockShape(EnumShape shape, Properties properties, ShapeOrientation orientation) {
        this(shape, properties, state -> orientation);
    }

    public BlockShape(EnumShape shape, Properties properties, Function<BlockStateShape, ShapeOrientation> orientationFunc) {
        super(properties.dynamicShape().noOcclusion());
        this.shape = shape;
        // We use a custom BlockState implementation, so we have to use reflection to force the state definition to use it.
        var builder = new StateDefinition.Builder<Block, BlockState>(this);
        var placementLogic = this.shape.getPlacementLogic();
        placementLogic.getProperties().forEach(builder::add);
        this.createBlockStateDefinition(builder);
        //noinspection DataFlowIssue -- We know this is not null, we check for it in the static block.
        STATE_DEFINITION.set(this,
                builder.create(Block::defaultBlockState,
                        (block, propertyKeys, propertyValues) ->
                                new BlockStateShape((BlockShape<?>) block, orientationFunc, propertyKeys, propertyValues)
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
        return ShapeMeshes.getMesh(this.shape, state.getOrientation());
    }

    @Override
    public ITrans3Immutable getTransformForState(BlockStateArchitecture state) {
        var shapeState = this.asShapeState(state);
        if (shapeState == null) {
            ArchitectureLog.error("BlockShape#getTransformForState called with a non-shape state, this should not happen.");
            return ITrans3.ofIdentity();
        }
        var transformationResolver = this.getShape().getTransformationResolver();
        return transformationResolver.resolve(shapeState).asImmutable();
    }

    @Override
    public CompletableFuture<ImmutableList<AABB>> getBoxesForState(BlockStateArchitecture state) {
        var shapeState = this.asShapeState(state);
        if (shapeState == null) {
            ArchitectureLog.error("BlockShape#getBoxesForState called with a non-shape state, this should not happen.");
            return CompletableFuture.completedFuture(DEFAULT_BOX);
        }
        var transformationResolver = this.getShape().getTransformationResolver();
        var voxelizer = ShapeMeshes.getVoxelizer(this.getMeshForState(shapeState));
        var transform = transformationResolver.resolve(shapeState);
        return voxelizer.voxelize().thenApply(aabbs -> aabbs.stream().map(transform::transformAABB).collect(ImmutableList.toImmutableList()));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getPlayer() == null) {
            ArchitectureLog.error("BlockShape#getStateForPlacement called with a null player, this should not happen.");
            return this.defaultBlockState();
        }
        // Defer to the placement logic to get the correct state for the shape.
        var orientation = this.getPlacementLogic().getShapeOrientationForPlacement(
                this.self(), context.getLevel(), context.getClickedPos(), context.getPlayer(),
                new BlockHitResult(context.getClickLocation(), context.getClickedFace(),
                        context.getClickedPos(), context.isInside()));
        return ShapeConnections.update(orientation.applyToState(this.defaultBlockState()),
                context.getLevel(), context.getClickedPos());
    }

    @Override
    public boolean connectsToOnSide(BlockState other, Direction side) {
        return other.getBlock() instanceof BlockShape<?> block && ShapeMeshes.isWindow(block.getShape());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks,
            BlockPos pos, Direction side, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        return ShapeConnections.update(state, level, pos);
    }

    @Override
    protected void updateIndirectNeighbourShapes(BlockState state, LevelAccessor level, BlockPos pos, int flags, int recursionLeft) {
        super.updateIndirectNeighbourShapes(state, level, pos, flags, recursionLeft);
        if (!ShapeMeshes.isWindow(this.shape) || recursionLeft <= 0) {
            return;
        }
        var directions = ShapeConnections.windowDirections(state);
        for (int vertical : new int[]{0, 2}) {
            for (int horizontal : new int[]{1, 3}) {
                var diagonalPos = pos.relative(directions[vertical]).relative(directions[horizontal]);
                var diagonal = level.getBlockState(diagonalPos);
                if (diagonal.getBlock() instanceof BlockShape<?> block && ShapeMeshes.isWindow(block.getShape())) {
                    var updated = ShapeConnections.update(diagonal, level, diagonalPos);
                    if (updated != diagonal) {
                        level.setBlock(diagonalPos, updated, flags, recursionLeft - 1);
                    }
                }
            }
        }
    }

    public BlockState rotatePlacement(BlockState state, boolean reverse) {
        var properties = this.getPlacementLogic().getProperties().stream()
                .filter(property -> !(property instanceof ShapeOrientationPropertyRelativeConnection))
                .filter(property -> property != ShapeConnectionProperties.STAIRS).toList();
        for (var property : properties) {
            var result = cycle(state, property, reverse);
            state = result.state();
            if (!result.wrapped()) {
                break;
            }
        }
        return state;
    }

    private record CycleResult(BlockState state, boolean wrapped) {
    }

    private static <V extends Enum<V> & StringRepresentable> CycleResult cycle(
            BlockState state, ShapeOrientationProperty<V> property, boolean reverse) {
        var values = new ArrayList<>(property.getPossibleValues());
        var index = values.indexOf(state.getValue(property));
        var next = Math.floorMod(index + (reverse ? -1 : 1), values.size());
        return new CycleResult(state.setValue(property, values.get(next)), reverse ? next == values.size() - 1 : next == 0);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityShape(pos, state);
    }


    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() || FMLEnvironment.isProduction()) {
            return InteractionResult.PASS;
        }
        return ArchitectureDebugEventListeners.onVoxelizedBlockClicked(level, pos, player, hit,
                ShapeMeshes.getVoxelizer(this.getMeshForState((BlockStateShape) state)));
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return super.getInteractionShape(state, level, pos);
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility itemAbility, boolean simulate) {
        return super.getToolModifiedState(state, context, itemAbility, simulate);
    }

    @Override
    public @Nullable PushReaction getPistonPushReaction(BlockState state) {
        return super.getPistonPushReaction(state);
    }
}
