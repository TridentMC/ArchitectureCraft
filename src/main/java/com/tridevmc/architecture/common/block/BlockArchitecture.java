package com.tridevmc.architecture.common.block;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.common.block.state.BlockStateArchitecture;
import com.tridevmc.architecture.core.ArchitectureLog;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.ITrans3Immutable;
import com.tridevmc.architecture.core.physics.AABB;
import com.tridevmc.compound.core.reflect.WrappedField;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class BlockArchitecture extends Block {

    static final ImmutableList<AABB> DEFAULT_BOX = ImmutableList.of(AABB.BLOCK_FULL);

    private static final WrappedField<StateDefinition<Block, BlockState>> STATE_DEFINITION = WrappedField.create(Block.class, "stateDefinition", "f_49792_");

    static {
        if (STATE_DEFINITION == null) {
            ArchitectureLog.error("Failed to find field 'stateDefinition' in Block, this is a critical error and will cause crashes.");
        }
    }

    public BlockArchitecture(Properties properties) {
        super(properties);
        // We use a custom BlockState implementation, so we have to use reflection to force the state definition to use it.
        var builder = new StateDefinition.Builder<Block, BlockState>(this);
        this.createBlockStateDefinition(builder);
        //noinspection DataFlowIssue -- We know this is not null, we check for it in the static block.
        STATE_DEFINITION.set(this,
                builder.create(Block::defaultBlockState,
                        (block, propertyValues, codec) ->
                                new BlockStateArchitecture((BlockArchitecture) block, propertyValues, codec)
                )
        );
        this.registerDefaultState(this.getStateDefinition().any());
    }

    /**
     * Gets the transform for the given state, used for rendering.
     *
     * @param state The state to get the transform for.
     * @return The transform for the given state.
     */
    public ITrans3Immutable getTransformForState(BlockStateArchitecture state) {
        return ITrans3.ofIdentity();
    }

    /**
     * Gets the boxes for the given state, with transformations applied.
     * <p>
     * The result of this method is cached within the state, it should not be called directly.
     *
     * @param state the state to get the boxes for.
     * @return the boxes for the given state.
     */
    public ImmutableList<AABB> getBoxesForStateNow(BlockStateArchitecture state) {
        try {
            return this.getBoxesForState(state).get();
        } catch (InterruptedException | ExecutionException e) {
            ArchitectureLog.fatal("Failed to get boxes for state, this is a critical error and will cause crashes.", e);
        }
        return DEFAULT_BOX;
    }

    /**
     * Gets the boxes for the given state, with transformations applied.
     * <p>
     * The result of this method is cached within the state, it should not be called directly.
     *
     * @param state the state to get the boxes for.
     * @return the boxes for the given state.
     */
    public CompletableFuture<ImmutableList<AABB>> getBoxesForState(BlockStateArchitecture state) {
        return CompletableFuture.completedFuture(DEFAULT_BOX);
    }

    private BlockStateArchitecture asArchitectureState(BlockState state) {
        if (state instanceof BlockStateArchitecture) {
            return (BlockStateArchitecture) state;
        } else {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    @NotNull
    public VoxelShape getShape(@NotNull BlockState pState, @NotNull BlockGetter pLevel, @NotNull BlockPos pPos, @NotNull CollisionContext pContext) {
        // Defer to our state implementation as it caches this value for us.
        var state = this.asArchitectureState(pState);
        if (state != null) {
            return state.getShape();
        } else {
            return super.getShape(pState, pLevel, pPos, pContext);
        }
    }

}
