package com.tridevmc.architecture.common.block;

import com.tridevmc.architecture.common.block.state.BlockStateShape;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.placement.IShapePlacementLogic;
import com.tridevmc.architecture.core.ArchitectureLog;
import com.tridevmc.compound.core.reflect.WrappedField;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for all shape blocks, handles placement logic and state definition based on the shape.
 *
 * @param <T> a self-referential generic type.
 */
public class BlockShape<T extends BlockShape<T>> extends BlockArchitecture {

    private final EnumShape shape;
    private static final WrappedField<StateDefinition<Block, BlockState>> STATE_DEFINITION = WrappedField.create(Block.class, "stateDefinition", "f_49792_");

    public BlockShape(EnumShape shape, Properties properties) {
        super(properties);
        this.shape = shape;
        // We use a custom BlockState implementation, so we have to use reflection to force the state definition to use it.
        var builder = new StateDefinition.Builder<Block, BlockState>(this);
        this.shape.getPlacementLogic().getProperties().forEach(builder::add);
        this.createBlockStateDefinition(builder);
        STATE_DEFINITION.set(this,
                builder.create(Block::defaultBlockState,
                        (block, propertyValues, codec) ->
                                new BlockStateShape((BlockShape) block, propertyValues, codec)
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
}
