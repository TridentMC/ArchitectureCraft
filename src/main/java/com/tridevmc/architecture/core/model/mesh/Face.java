package com.tridevmc.architecture.core.model.mesh;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.core.math.Transform;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Default implementation of {@link IFace}.
 *
 * @param polygons The polygons that make up this face.
 * @param <D>      The type of data that is stored on the polygons.
 */
public record Face<D extends IPolygonData>(ImmutableList<IPolygon<D>> polygons) implements IFace<D> {

    @Override
    public @NotNull ImmutableList<IPolygon<D>> getPolygons() {
        return this.polygons;
    }

    @Override
    public @NotNull IFace<D> transform(@NotNull Transform trans, boolean transformUVs) {
        var builder = new Builder<D>();
        for (var p : this.polygons) {
            builder.addPolygon(p.transform(trans, transformUVs));
        }
        return builder.build();
    }

    /**
     * Builder for {@link Face} instances.
     *
     * @param <D> The type of data that is stored on the polygons.
     */
    public static class Builder<D extends IPolygonData> {
        private final List<IPolygon<D>> polygons = new ArrayList<>();

        /**
         * Adds a polygon to the face.
         *
         * @param polygon The polygon to add.
         * @return This builder.
         */
        public Builder<D> addPolygon(IPolygon<D> polygon) {
            this.polygons.add(polygon);
            return this;
        }

        /**
         * Builds a new {@link Face} instance.
         *
         * @return The new face.
         */
        @SuppressWarnings("unchecked")
        public Face<D> build() {
            return new Face<>(ImmutableList.copyOf(this.polygons));
        }
    }
}
