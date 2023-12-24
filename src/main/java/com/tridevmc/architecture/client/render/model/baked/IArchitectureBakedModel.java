package com.tridevmc.architecture.client.render.model.baked;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tridevmc.architecture.common.block.state.BlockStateArchitecture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Extension to {@link IDynamicBakedModel} that delegates to a new method that accepts our own {@link BlockStateArchitecture} object instead of the vanilla {@link BlockState}.
 */
public interface IArchitectureBakedModel extends IDynamicBakedModel {


    @Override
    default @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        if (state instanceof BlockStateArchitecture stateArchitecture) {
            return this.getQuads(stateArchitecture, side, rand, extraData, renderType);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    default ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }

    @Override
    default boolean isCustomRenderer() {
        return true;
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
    @NotNull List<BakedQuad> getQuads(@Nullable BlockStateArchitecture state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType);

    /**
     * Gets a list of quads for the given stack.
     *
     * @param stack The stack to get quads for.
     * @return A list of quads.
     */
    @NotNull List<BakedQuad> getQuads(@NotNull ItemStack stack);

    @Override
    default BakedModel applyTransform(ItemDisplayContext transformType, PoseStack poseStack, boolean applyLeftHandTransform) {
        return IDynamicBakedModel.super.applyTransform(transformType, poseStack, applyLeftHandTransform);
    }
}
