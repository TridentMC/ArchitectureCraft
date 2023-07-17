package com.tridevmc.architecture.common.block;

import com.tridevmc.architecture.common.block.state.BlockStateArchitecture;
import com.tridevmc.architecture.common.block.state.BlockStateShape;
import com.tridevmc.architecture.core.ArchitectureLog;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.ITrans3Immutable;
import com.tridevmc.compound.core.reflect.WrappedField;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public class BlockArchitecture extends Block {

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
}
