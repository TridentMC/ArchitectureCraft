package com.tridevmc.architecture.client.render.model.data;

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
