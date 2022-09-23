package com.tridevmc.architecture.client.render.model.data;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class FunctionalQuadMetadataResolver<T> implements IQuadMetadataResolver<T> {

    private final IQuadMetadataTextureResolver<T> textureResolver;
    private final IQuadMetadataTintResolver<T> tintResolver;

    private FunctionalQuadMetadataResolver(IQuadMetadataTextureResolver<T> textureResolver, IQuadMetadataTintResolver<T> tintResolver) {
        this.textureResolver = textureResolver;
        this.tintResolver = tintResolver;
    }

    public static <T> FunctionalQuadMetadataResolver<T> of(IQuadMetadataTextureResolver<T> textureResolver, IQuadMetadataTintResolver<T> tintResolver) {
        return new FunctionalQuadMetadataResolver<>(textureResolver, tintResolver);
    }

    @Override
    public TextureAtlasSprite getTexture(T metadata) {
        return textureResolver.getTexture(metadata);
    }

    @Override
    public int getTint(T metadata) {
        return tintResolver.getTint(metadata);
    }
}
