package com.tridevmc.architecture.client.render.model.data;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class FunctionalQuadMetadataResolver<T> implements IQuadMetadataResolver<T> {

    private final IQuadMetadataTextureResolver<T> textureResolver;
    private final IQuadMetadataColourResolver<T> colourResolver;

    private FunctionalQuadMetadataResolver(IQuadMetadataTextureResolver<T> textureResolver, IQuadMetadataColourResolver<T> colourResolver) {
        this.textureResolver = textureResolver;
        this.colourResolver = colourResolver;
    }

    public static <T> FunctionalQuadMetadataResolver<T> of(IQuadMetadataTextureResolver<T> textureResolver, IQuadMetadataColourResolver<T> tintResolver) {
        return new FunctionalQuadMetadataResolver<>(textureResolver, tintResolver);
    }

    @Override
    public TextureAtlasSprite getTexture(T metadata) {
        return this.textureResolver.getTexture(metadata);
    }

    @Override
    public int getColour(T metadata) {
        return this.colourResolver.getColour(metadata);
    }
}
