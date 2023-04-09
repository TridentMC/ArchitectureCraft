package com.tridevmc.architecture.client.render.model.data;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.Objects;

/**
 * A wrapper for {@link IQuadMetadataTextureResolver} that uses the hash of the hash source to determine equality.
 *
 * @param <D> The type of metadata to resolve.
 */
public class HashedQuadMetadataTextureResolver<D> implements IQuadMetadataTextureResolver<D> {

    private final IQuadMetadataTextureResolver<D> resolver;
    private final Object hashSource;

    private HashedQuadMetadataTextureResolver(Object hashSource, IQuadMetadataTextureResolver<D> resolver) {
        this.hashSource = hashSource;
        this.resolver = resolver;
    }

    /**
     * Creates a new {@link HashedQuadMetadataTextureResolver} that uses the hash of the given hash source to determine equality.
     *
     * @param hashSource The hash source.
     * @param resolver   The resolver.
     * @param <D>        The type of metadata to resolve.
     * @return The new instance.
     */
    public static <D> HashedQuadMetadataTextureResolver<D> of(Object hashSource, IQuadMetadataTextureResolver<D> resolver) {
        return new HashedQuadMetadataTextureResolver<D>(hashSource, resolver);
    }

    @Override
    public TextureAtlasSprite getTexture(D metadata) {
        return this.resolver.getTexture(metadata);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        HashedQuadMetadataTextureResolver<?> that = (HashedQuadMetadataTextureResolver<?>) o;
        return Objects.equals(this.hashSource, that.hashSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.hashSource);
    }

}
