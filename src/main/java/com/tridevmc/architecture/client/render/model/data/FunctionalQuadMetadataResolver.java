package com.tridevmc.architecture.client.render.model.data;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * An implementation of {@link IQuadMetadataResolver} that resolves the texture and tint index for a quad using two functional interfaces.
 *
 * @param <T> The type of the metadata object.
 */
public class FunctionalQuadMetadataResolver<T> implements IQuadMetadataResolver<T> {

    private final IQuadMetadataTextureResolver<T> textureResolver;
    private final IQuadMetadataColourResolver<T> colourResolver;

    private FunctionalQuadMetadataResolver(IQuadMetadataTextureResolver<T> textureResolver, IQuadMetadataColourResolver<T> colourResolver) {
        this.textureResolver = textureResolver;
        this.colourResolver = colourResolver;
    }

    /**
     * Creates a new instance of {@link FunctionalQuadMetadataResolver} using the given texture and colour resolvers.
     *
     * @param textureResolver The texture resolver.
     * @param colourResolver  The colour resolver.
     * @param <T>             The type of the metadata object.
     * @return The new instance.
     */
    public static <T> FunctionalQuadMetadataResolver<T> of(IQuadMetadataTextureResolver<T> textureResolver, IQuadMetadataColourResolver<T> colourResolver) {
        return new FunctionalQuadMetadataResolver<>(textureResolver, colourResolver);
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
