package com.tridevmc.architecture.legacy.client.render.model.data;

import com.tridevmc.architecture.client.render.model.piped.IPipedBakedQuad;

/**
 * Default implementation of {@link IPipedBakedQuad}.
 *
 * @param <T> The type of metadata this provider uses.
 */
@Deprecated
public abstract class LegacyBakedQuadProvider<T> implements IPipedBakedQuad {

    private final T metadata;

    protected LegacyBakedQuadProvider(T metadata) {
        this.metadata = metadata;
    }

    @Override
    public T metadata() {
        return this.metadata;
    }
}
