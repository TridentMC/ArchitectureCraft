package com.tridevmc.architecture.client.render.model.data;

/**
 * A functional interface that resolves a quad's metadata to a colour.
 *
 * @param <T> The type of metadata to resolve.
 */
@FunctionalInterface
public interface IQuadMetadataColourResolver<T> {
    /**
     * Resolves the quad's metadata to a colour.
     *
     * @param metadata The metadata to resolve.
     * @return The colour to use.
     */
    int getColour(T metadata);
}
