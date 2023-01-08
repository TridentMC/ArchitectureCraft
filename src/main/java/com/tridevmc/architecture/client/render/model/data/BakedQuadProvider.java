package com.tridevmc.architecture.client.render.model.data;

/**
 * Default implementation of {@link IBakedQuadProvider}.
 *
 * @param <T> The type of metadata this provider uses.
 */
public abstract class BakedQuadProvider<T> implements IBakedQuadProvider<T> {

    private final T metadata;

    protected BakedQuadProvider(T metadata) {
        this.metadata = metadata;
    }

    @Override
    public T getMetadata() {
        return this.metadata;
    }
}
