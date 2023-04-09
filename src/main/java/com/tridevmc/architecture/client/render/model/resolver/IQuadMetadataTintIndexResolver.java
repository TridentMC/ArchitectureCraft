package com.tridevmc.architecture.client.render.model.resolver;

/**
 * A functional interface that resolves a quad's metadata to a colour.
 *
 * @param <D> The type of metadata to resolve.
 */
@FunctionalInterface
public interface IQuadMetadataTintIndexResolver<D> {

    /**
     * Resolves the quad's metadata to a colour.
     *
     * @param metadata The metadata to resolve.
     * @return The colour to use.
     */
    int getTintIndex(D metadata);

}
