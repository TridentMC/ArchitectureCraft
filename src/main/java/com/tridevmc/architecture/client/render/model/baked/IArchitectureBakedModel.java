package com.tridevmc.architecture.client.render.model.baked;

import com.tridevmc.architecture.common.block.state.BlockStateArchitecture;
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

public interface IArchitectureBakedModel extends IDynamicBakedModel {

    @Override
    default @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        if (state instanceof BlockStateArchitecture stateArchitecture) {
            return this.getQuads(stateArchitecture, side, rand, extraData, renderType);
        } else {
            return Collections.emptyList();
        }
    }

    @NotNull List<BakedQuad> getQuads(@Nullable BlockStateArchitecture state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType);
}
