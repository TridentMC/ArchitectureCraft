package com.tridevmc.architecture.client.render.model.data;

import java.util.Objects;

/**
 * A wrapper for {@link IQuadMetadataTintIndexResolver} that uses the hash of the hash source to determine equality.
 *
 * @param <D> The type of metadata to resolve.
 */
public class HashedQuadMetadataTintIndexResolver<D> implements IQuadMetadataTintIndexResolver<D> {

    private final IQuadMetadataTintIndexResolver<D> resolver;
    private final Object hashSource;

    private HashedQuadMetadataTintIndexResolver(Object hashSource, IQuadMetadataTintIndexResolver<D> resolver) {
        this.hashSource = hashSource;
        this.resolver = resolver;
    }

    /**
     * Creates a new {@link HashedQuadMetadataTintIndexResolver} that uses the hash of the given hash source to determine equality.
     *
     * @param hashSource The hash source.
     * @param resolver   The resolver.
     * @param <D>        The type of metadata to resolve.
     * @return The new instance.
     */
    public static <D> HashedQuadMetadataTintIndexResolver<D> of(Object hashSource, IQuadMetadataTintIndexResolver<D> resolver) {
        return new HashedQuadMetadataTintIndexResolver<D>(hashSource, resolver);
    }

    @Override
    public int getTintIndex(D metadata) {
        return this.resolver.getTintIndex(metadata);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        HashedQuadMetadataTintIndexResolver<?> that = (HashedQuadMetadataTintIndexResolver<?>) o;
        return Objects.equals(this.hashSource, that.hashSource);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.hashSource);
    }

}
