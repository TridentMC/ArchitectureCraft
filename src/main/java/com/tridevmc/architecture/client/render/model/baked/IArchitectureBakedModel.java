package com.tridevmc.architecture.client.render.model.baked;

import com.tridevmc.architecture.legacy.common.block.state.LegacyBlockStateArchitecture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.IDynamicBakedModel;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Extension to {@link IDynamicBakedModel} that delegates to a new method that accepts our own {@link LegacyBlockStateArchitecture} object instead of the vanilla {@link BlockState}.
 */
public interface IArchitectureBakedModel extends IDynamicBakedModel {

    @Override
    default @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        if (state instanceof LegacyBlockStateArchitecture stateArchitecture) {
            return this.getQuads(stateArchitecture, side, rand, extraData, renderType);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Gets a list of quads for the given state, side, rand, extraData, and renderType.
     *
     * @param state      The state of the block.
     * @param side       The side of the block to get quads for, refers to culled faces. Can be null for general quads.
     * @param rand       The random source.
     * @param extraData  The extra data.
     * @param renderType The render type.
     * @return A list of quads.
     */
    @NotNull List<BakedQuad> getQuads(@Nullable LegacyBlockStateArchitecture state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType);

}
