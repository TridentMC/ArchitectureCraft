package com.tridevmc.architecture.client.render.model.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Encoder;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public record SpecialSawbenchModelRenderer(BakedModelSawbench model) implements SpecialModelRenderer<Void> {
    @Override
    public void render(@Nullable Void patterns, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, boolean hasFoilType) {

    }

    @Override
    public @Nullable Void extractArgument(ItemStack stack) {
        return null;
    }

    public static record Unbaked(Void unit) implements SpecialModelRenderer.Unbaked {

        public static final MapCodec<SpecialSawbenchModelRenderer.Unbaked> CODEC = MapCodec.of(Encoder.empty(), Decoder.unit(new SpecialSawbenchModelRenderer.Unbaked(null)));

        // Create the special model renderer, or null if it fails
        @Nullable
        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet modelSet) {
            return new SpecialSawbenchModelRenderer(
                    new BakedModelSawbench()
            );
        }

        @Override
        public MapCodec<SpecialSawbenchModelRenderer.Unbaked> type() {
            return CODEC;
        }
    }
}
