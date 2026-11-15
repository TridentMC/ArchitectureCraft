package com.tridevmc.architecture.client.render.model.baked;

import com.tridevmc.architecture.common.block.state.BlockStateArchitecture;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.SimpleModelWrapper;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Extension to {@link DynamicBlockStateModel} that delegates to a new method that accepts our own {@link BlockStateArchitecture} object instead of the vanilla {@link BlockState}.
 */
public interface IArchitectureBakedModel extends DynamicBlockStateModel {
    /**
     * Gets the quads for the given level, position, and state.
     *
     * @param level The level to get quads for.
     * @param pos   The position of the block.
     * @param state The state of the block.
     * @return A container of quads, grouped into culled faces and general quads.
     */
    IBakedQuadContainer getQuads(BlockGetter level, BlockPos pos, @Nullable BlockStateArchitecture state);

    /**
     * Gets a list of quads for the given stack.
     *
     * @param stack The stack to get quads for.
     * @return A list of quads.
     */
    List<BakedQuad> getQuads(ItemStack stack);

    @Override
    default void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state,
                              RandomSource random, List<BlockStateModelPart> parts) {
        var architectureState = state instanceof BlockStateArchitecture stateArchitecture ? stateArchitecture : null;
        var quads = this.getQuads(level, pos, architectureState);
        var material = this.particleMaterial(level, pos, state);
        var geometry = new QuadCollection.Builder();
        for (var direction : Direction.values()) {
            for (var quad : quads.quadsFor(direction)) {
                geometry.addCulledFace(direction, quad);
            }
        }
        for (var quad : quads.generalQuads()) {
            geometry.addUnculledFace(quad);
        }
        parts.add(new SimpleModelWrapper(geometry.build(), true, material));
    }

    @Override
    default int materialFlags() {
        return BakedQuad.FLAG_TRANSLUCENT | BakedQuad.FLAG_ANIMATED;
    }
}
