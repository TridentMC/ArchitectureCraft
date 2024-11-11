package com.tridevmc.architecture.legacy.client.render.model.baked;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import java.util.Arrays;

/**
 * A lightly modified version of {@link BakedQuad} that allows for the texture to be changed.
 * <p>
 * Sorta based off of retextured quads from older versions of Forge.
 */
@Deprecated
public class LegacyTexturableBakedQuad extends BakedQuad {

    public LegacyTexturableBakedQuad(int[] vertices, int tintIndex, Direction direction, TextureAtlasSprite sprite, boolean shade, int p_361140_) {
        super(vertices, tintIndex, direction, sprite, shade, p_361140_);
    }


    private static int[] recalculateVertices(int[] vertices, TextureAtlasSprite from, TextureAtlasSprite to) {
        VertexFormat format = DefaultVertexFormat.BLOCK;
        vertices = Arrays.copyOf(vertices, vertices.length);
        for (int i = 0; i < 4; ++i) {
            int j = format.getVertexSize() * i;
            int uvIndex = format.getOffset(VertexFormatElement.UV) / 4;
            vertices[j + uvIndex] = Float.floatToRawIntBits(to.getU(getUnInterpolatedU(from, Float.intBitsToFloat(vertices[j + uvIndex]))));
            vertices[j + uvIndex + 1] = Float.floatToRawIntBits(to.getV(getUnInterpolatedV(from, Float.intBitsToFloat(vertices[j + uvIndex + 1]))));
        }
        return vertices;
    }

    private static float getUnInterpolatedU(TextureAtlasSprite sprite, float u) {
        float f = sprite.getU1() - sprite.getU0();
        return ((u - sprite.getU0()) / f) * 16.0F;
    }

    private static float getUnInterpolatedV(TextureAtlasSprite sprite, float v) {
        float f = sprite.getV1() - sprite.getV0();
        return ((v - sprite.getV0()) / f) * 16.0F;
    }

}
