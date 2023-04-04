package com.tridevmc.architecture.client.render.model.baked;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

import java.util.Arrays;

/**
 * A lightly modified version of {@link BakedQuad} that allows for the texture to be changed.
 * <p>
 * Sorta based off of retextured quads from older versions of Forge.
 */
public class TexturableBakedQuad extends BakedQuad {

    public TexturableBakedQuad(int[] pVertices, int pTintIndex, Direction pDirection, TextureAtlasSprite pSprite, boolean pShade) {
        super(pVertices, pTintIndex, pDirection, pSprite, pShade);
    }

    public TexturableBakedQuad(int[] pVertices, int pTintIndex, Direction pDirection, TextureAtlasSprite pSprite, boolean pShade, boolean hasAmbientOcclusion) {
        super(pVertices, pTintIndex, pDirection, pSprite, pShade, hasAmbientOcclusion);
    }

    /**
     * Creates a copy of the given quad with the uv coordinates remapped to the given texture.
     *
     * @param quad   The quad to copy.
     * @param sprite The texture to remap the quad to.
     * @return A copy of the given quad with the uv coordinates remapped to the given texture.
     */
    public static TexturableBakedQuad retextured(BakedQuad quad, TextureAtlasSprite sprite) {
        return new TexturableBakedQuad(recalculateVertices(quad.getVertices(), quad.getSprite(), sprite),
                quad.getTintIndex(), quad.getDirection(),
                sprite, quad.isShade(), quad.hasAmbientOcclusion());
    }

    /**
     * Creates a copy of this quad with the uv coordinates remapped to the given texture.
     *
     * @param texture The texture to remap the quad to.
     * @return A copy of this quad with the uv coordinates remapped to the given texture.
     */
    public TexturableBakedQuad retextured(TextureAtlasSprite texture) {
        return new TexturableBakedQuad(
                recalculateVertices(this.getVertices(), this.sprite, texture),
                this.getTintIndex(),
                this.getDirection(),
                texture,
                this.isShade(),
                this.hasAmbientOcclusion()
        );
    }

    private static int[] recalculateVertices(int[] vertices, TextureAtlasSprite from, TextureAtlasSprite to) {
        VertexFormat format = DefaultVertexFormat.BLOCK;
        vertices = Arrays.copyOf(vertices, vertices.length);
        for (int i = 0; i < 4; ++i) {
            int j = format.getIntegerSize() * i;
            int uvIndex = format.getOffset(0) / 4;
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
