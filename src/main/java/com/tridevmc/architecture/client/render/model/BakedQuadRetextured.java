package com.tridevmc.architecture.client.render.model;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;

import java.util.Arrays;

/**
 * Re-implemented from older versions because I need it.
 */
public class BakedQuadRetextured extends BakedQuad {
    private final TextureAtlasSprite texture;

    public BakedQuadRetextured(BakedQuad quad, TextureAtlasSprite textureIn) {
        super(Arrays.copyOf(quad.getVertexData(), quad.getVertexData().length), quad.getTintIndex(), FaceBakery.getFacingFromVertexData(quad.getVertexData()), quad.func_187508_a(), quad.shouldApplyDiffuseLighting());
        this.texture = textureIn;
        this.remapQuad();
    }

    private void remapQuad() {
        VertexFormat format = DefaultVertexFormats.BLOCK;
        for (int i = 0; i < 4; ++i) {
            int j = format.getIntegerSize() * i;
            int uvIndex = format.getOffset(0) / 4;
            this.vertexData[j + uvIndex] = Float.floatToRawIntBits(this.texture.getInterpolatedU(this.getUnInterpolatedU(this.sprite, Float.intBitsToFloat(this.vertexData[j + uvIndex]))));
            this.vertexData[j + uvIndex + 1] = Float.floatToRawIntBits(this.texture.getInterpolatedV(this.getUnInterpolatedV(this.sprite, Float.intBitsToFloat(this.vertexData[j + uvIndex + 1]))));
        }
    }

    public float getUnInterpolatedU(TextureAtlasSprite sprite, float u) {
        float f = sprite.getMaxU() - sprite.getMinU();
        return ((u - sprite.getMinU()) / f) * 16.0F;
    }

    public float getUnInterpolatedV(TextureAtlasSprite sprite, float v) {
        float f = sprite.getMaxV() - sprite.getMinV();
        return ((v - sprite.getMinV()) / f) * 16.0F;
    }

    @Override
    public TextureAtlasSprite func_187508_a() {
        return this.texture;
    }

}