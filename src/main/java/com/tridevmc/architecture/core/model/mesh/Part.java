package com.tridevmc.architecture.core.model.mesh;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.physics.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record Part<I, D extends IPolygonData<D>>(I id, ImmutableList<IFace<D>> faces, AABB bounds) implements IPart<I, D> {
    @Override
    public @NotNull I getId() {
        return this.id;
    }

    @Override
    public @NotNull ImmutableList<IFace<D>> getFaces() {
        return this.faces;
    }

    @Override
    public @NotNull AABB getBounds() {
        return this.bounds;
    }

    @Override
    public @NotNull IPart<I, D> transform(@NotNull ITrans3 trans, boolean transformUVs) {
        var builder = new Builder<I, D>();
        for (var f : this.faces) {
            builder.addFace(f.transform(trans, transformUVs));
        }
        return builder.setId(this.id).build();
    }

    /**
     * Builder for {@link Part} instances.
     *
     * @param <I> The type of data used to identify the part.
     * @param <D> The type of data that is stored on the polygons.
     */
    public static class Builder<I, D extends IPolygonData<D>> {
        private I id;
        private final List<IFace<D>> faces = new ArrayList<>();

        /**
         * Sets the identifier of the part.
         *
         * @param id The identifier of the part.
         * @return This builder.
         */
        public Builder<I, D> setId(I id) {
            this.id = id;
            return this;
        }

        /**
         * Adds a face to the mesh.
         *
         * @param face The face to add.
         * @return This builder.
         */
        public Builder<I, D> addFace(IFace<D> face) {
            this.faces.add(face);
            return this;
        }

        /**
         * Builds a new {@link Part} instance.
         *
         * @return The new part.
         */
        @SuppressWarnings("unchecked")
        public Part<I, D> build() {
            if (this.id == null) {
                throw new IllegalStateException("Part ID must be set");
            }
            var bounds = AABB.fromVertices(
                    this.faces.stream().flatMap(IFace::getPolygonStream).flatMap(IPolygon::getVertexStream).toList()
            );
            return new Part<>(this.id, ImmutableList.copyOf(this.faces), bounds);
        }
    }
}
