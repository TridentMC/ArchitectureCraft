package com.tridevmc.architecture.legacy.client.render.model.builder;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.NotNull;

@Deprecated
public class LegacyBakedQuadBuilderVertexConsumer implements VertexConsumer {

    private final QuadBakingVertexConsumer realConsumer;
    private BakedQuad out = null;

    public LegacyBakedQuadBuilderVertexConsumer() {
        this.realConsumer = new QuadBakingVertexConsumer(q -> this.out = q);
    }

    public LegacyBakedQuadBuilderVertexConsumer setTintIndex(int tintIndex) {
        this.realConsumer.setTintIndex(tintIndex);
        return this;
    }

    public LegacyBakedQuadBuilderVertexConsumer setDirection(Direction direction) {
        this.realConsumer.setDirection(direction);
        return this;
    }

    public LegacyBakedQuadBuilderVertexConsumer setSprite(TextureAtlasSprite sprite) {
        this.realConsumer.setSprite(sprite);
        return this;
    }

    public LegacyBakedQuadBuilderVertexConsumer setShade(boolean shade) {
        this.realConsumer.setShade(shade);
        return this;
    }

    public LegacyBakedQuadBuilderVertexConsumer setHasAmbientOcclusion(boolean hasAmbientOcclusion) {
        this.realConsumer.setHasAmbientOcclusion(hasAmbientOcclusion);
        return this;
    }

    @Override
    @NotNull
    public VertexConsumer vertex(double x, double y, double z) {
        return this.realConsumer.vertex(x, y, z);
    }

    @Override
    @NotNull
    public VertexConsumer color(int r, int g, int b, int a) {
        return this.realConsumer.color(r, g, b, a);
    }

    @Override
    @NotNull
    public VertexConsumer uv(float u, float v) {
        return this.realConsumer.uv(u, v);
    }

    @Override
    @NotNull
    public VertexConsumer overlayCoords(int u, int v) {
        return this.realConsumer.overlayCoords(u, v);
    }

    @Override
    @NotNull
    public VertexConsumer uv2(int u2, int v2) {
        return this.realConsumer.uv2(u2, v2);
    }

    @Override
    @NotNull
    public VertexConsumer normal(float x, float y, float z) {
        return this.realConsumer.normal(x, y, z);
    }

    @Override
    public void endVertex() {
        this.realConsumer.endVertex();
    }

    @Override
    public void defaultColor(int r, int g, int b, int a) {
        this.realConsumer.defaultColor(r, g, b, a);
    }

    @Override
    public void unsetDefaultColor() {
        this.realConsumer.unsetDefaultColor();
    }

    public BakedQuad getBakedQuad() {
        return this.out;
    }

}
