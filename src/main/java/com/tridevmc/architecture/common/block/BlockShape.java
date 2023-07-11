package com.tridevmc.architecture.common.block;

import com.tridevmc.architecture.common.block.state.BlockStateShape;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.placement.IShapePlacementLogic;
import com.tridevmc.architecture.core.ArchitectureLog;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.IPart;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import com.tridevmc.architecture.core.physics.AABB;
import com.tridevmc.compound.core.reflect.WrappedField;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Base class for all shape blocks, handles placement logic and state definition based on the shape.
 *
 * @param <T> a self-referential generic type.
 */
public class BlockShape<T extends BlockShape<T>> extends BlockArchitecture {

    private final EnumShape shape;
    private static final WrappedField<StateDefinition<Block, BlockState>> STATE_DEFINITION = WrappedField.create(Block.class, "stateDefinition", "f_49792_");

    static {
        if (STATE_DEFINITION == null) {
            ArchitectureLog.error("Failed to find field 'stateDefinition' in Block, this is a critical error and will cause crashes.");
        }
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
        this.shape.getPlacementLogic().getProperties().forEach(builder::add);
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

    /**
     * Gets the boxes for the given state, with transformations applied.
     * <p>
     * The result of this method is cached within the state, it should not be called directly.
     *
     * @param state the state to get the boxes for.
     * @return the boxes for the given state.
     */
    public List<AABB> getBoxesForState(BlockStateShape state) {
        var transform = this.getShape().getTransformationResolver().resolve(state);
        return this.shape.getVoxelizer().voxelize().stream().map(transform::transformAABB).toList();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getPlayer() == null) {
            ArchitectureLog.error("BlockShape#getStateForPlacement called with a null player, this should not happen.");
            return this.defaultBlockState();
        }
        // Defer to the placement logic to get the correct state for the shape.
        return this.getPlacementLogic().getShapeOrientationForPlacement(
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
        ).applyToState(this.defaultBlockState());
    }

    @SuppressWarnings("deprecation")
    @Override
    @NotNull
    public VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        // Defer to our state implementation as it caches this value for us.
        var state = this.asShapeState(pState);
        if (state != null) {
            return state.getShape();
        } else {
            return super.getShape(pState, pLevel, pPos, pContext);
        }
    }

    @Override
    public boolean hasDynamicShape() {
        // By returning true here we tell the game to generate a Cache object on our state.
        return true;
    }
}
