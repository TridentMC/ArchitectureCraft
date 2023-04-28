package com.tridevmc.architecture.core.model.mesh;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

public record RawPolygonPayload<P extends IPolygon<D>, D extends IPolygonData<D>>(
        @NotNull IPolygonProvider<P, D> provider,
        @NotNull D data,
        @NotNull ImmutableList<IVertex> vertices
) implements IRawPolygonPayload<P, D> {

    @Override
    @NotNull
    public IPolygonProvider<P, D> getProvider() {
        return this.provider();
    }

    @Override
    @NotNull
    public D getData() {
        return this.data();
    }

    @Override
    @NotNull
    public ImmutableList<IVertex> getVertices() {
        return this.vertices();
    }

}
