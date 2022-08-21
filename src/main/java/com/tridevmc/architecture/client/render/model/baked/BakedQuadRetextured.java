package com.tridevmc.architecture.client.render.model.baked;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.Arrays;

/**
 * Re-implemented from older versions because I need it.
 */
public class BakedQuadRetextured extends BakedQuad {
    private final TextureAtlasSprite texture;

    public BakedQuadRetextured(BakedQuad quad, TextureAtlasSprite textureIn) {
        super(Arrays.copyOf(quad.getVertices(), quad.getVertices().length), quad.getTintIndex(), FaceBakery.calculateFacing(quad.getVertices()), quad.getSprite(), quad.isShade());
        this.texture = textureIn;
        this.remapQuad();
    }

    private void remapQuad() {
        VertexFormat format = DefaultVertexFormat.BLOCK;
        for (int i = 0; i < 4; ++i) {
            int j = format.getIntegerSize() * i;
            int uvIndex = format.getOffset(0) / 4;
            this.vertices[j + uvIndex] = Float.floatToRawIntBits(this.texture.getU(this.getUnInterpolatedU(this.sprite, Float.intBitsToFloat(this.vertices[j + uvIndex]))));
            this.vertices[j + uvIndex + 1] = Float.floatToRawIntBits(this.texture.getV(this.getUnInterpolatedV(this.sprite, Float.intBitsToFloat(this.vertices[j + uvIndex + 1]))));
        }
    }

    public float getUnInterpolatedU(TextureAtlasSprite sprite, float u) {
        float f = sprite.getU1() - sprite.getU0();
        return ((u - sprite.getU0()) / f) * 16.0F;
    }

    public float getUnInterpolatedV(TextureAtlasSprite sprite, float v) {
        float f = sprite.getV1() - sprite.getV0();
        return ((v - sprite.getV0()) / f) * 16.0F;
    }

    @Override
    public TextureAtlasSprite getSprite() {
        return this.texture;
    }

}