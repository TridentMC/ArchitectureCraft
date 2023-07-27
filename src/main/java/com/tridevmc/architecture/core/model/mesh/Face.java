package com.tridevmc.architecture.core.model.mesh;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.IVector3;
import com.tridevmc.architecture.core.math.IVector3Immutable;
import com.tridevmc.architecture.core.math.IVector3Mutable;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Default implementation of {@link IFace}.
 */
public final class Face<D extends IPolygonData<D>> implements IFace<D> {

    private final ImmutableList<IVertex> vertices;
    private final ImmutableList<IPolygon<D>> polygons;
    private final IVector3Immutable normal;

    /**
     * Creates a new face with the given vertices and polygons.
     *
     * @param vertices The vertices of the face.
     * @param polygons The polygons of the face.
     * @param normal   The normal of the face.
     */
    public Face(ImmutableList<IVertex> vertices,
                ImmutableList<IPolygon<D>> polygons,
                IVector3Immutable normal) {
        if (normal == null) {
            throw new NullPointerException("Normal cannot be null");
        }
        this.vertices = vertices;
        this.polygons = polygons;
        this.normal = normal;
    }

    /**
     * Creates a new face with the given vertices and polygons.
     *
     * @param vertexPool       The vertices of the face.
     * @param polygonProviders The polygons of the face.
     * @param normal           The normal of the face.
     */
    private Face(Object2IntMap<IVertex> vertexPool, List<Function<IFace<D>, IPolygon<D>>> polygonProviders, IVector3Immutable normal) {
        if (normal == null) {
            throw new NullPointerException("Normal cannot be null");
        }
        // Object2IntMap is not sorted by our indices, so we need to create a new array we can pass further down.
        var vertexArray = new IVertex[vertexPool.size()];
        vertexPool.forEach((v, i) -> vertexArray[i] = v);
        this.vertices = ImmutableList.copyOf(vertexArray);
        this.polygons = ImmutableList.copyOf(polygonProviders.stream().map(p -> p.apply(this)).iterator());
        this.normal = normal;
    }

    /**
     * Creates a new face with the given vertices and polygons.
     *
     * @param vertices         The vertices of the face.
     * @param polygonProviders The polygons of the face.
     * @param normal           The normal of the face.
     */
    private Face(ImmutableList<IVertex> vertices, List<Function<IFace<D>, IPolygon<D>>> polygonProviders, IVector3Immutable normal) {
        if (normal == null) {
            throw new NullPointerException("Normal cannot be null");
        }
        this.vertices = vertices;
        this.polygons = ImmutableList.copyOf(polygonProviders.stream().map(p -> p.apply(this)).iterator());
        this.normal = normal;
    }

    @Override
    public @NotNull ImmutableList<IPolygon<D>> getPolygons() {
        return this.polygons;
    }

    @Override
    public @NotNull ImmutableList<IVertex> getVertices() {
        return this.vertices;
    }

    @Override
    public @NotNull IVector3Immutable getNormal() {
        return this.normal;
    }

    @Override
    public @NotNull IVertex getVertex(int index) {
        return this.vertices.get(index);
    }

    @Override
    public @NotNull IFace<D> transform(@NotNull ITrans3 trans, boolean transformUVs) {
        var transformedVertices = this.vertices()
                .stream()
                .map(v -> v.transform(this, trans, transformUVs))
                .collect(ImmutableList.toImmutableList());
        var transformedPolygons = this.polygons()
                .stream()
                .map(p -> (Function<IFace<D>, IPolygon<D>>) diFace -> p.transform(diFace, trans, transformUVs))
                .collect(ImmutableList.toImmutableList());
        return new Face<>(transformedVertices, transformedPolygons, trans.transformNormalImmutable(this.normal));
    }

    public ImmutableList<IVertex> vertices() {
        return this.vertices;
    }

    public ImmutableList<IPolygon<D>> polygons() {
        return this.polygons;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Face) obj;
        return Objects.equals(this.vertices, that.vertices) &&
                Objects.equals(this.polygons, that.polygons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.vertices, this.polygons);
    }

    @Override
    public String toString() {
        return "Face[" +
                "vertices=" + this.vertices + ", " +
                "polygons=" + this.polygons + ']';
    }

    /**
     * Builder for {@link Face} instances.
     *
     * @param <D> The type of data that is stored on the polygons.
     */
    public static class Builder<D extends IPolygonData<D>> {

        private final Object2IntMap<IVertex> vertexPool = new Object2IntOpenHashMap<IVertex>();
        private final List<Function<IFace<D>, IPolygon<D>>> polygons = Lists.newArrayList();
        private final IVector3Mutable normal = IVector3.ofMutable(0, 0, 0);

        public Builder<D> addVertex(IVertex vertex) {
            if (!this.vertexPool.containsKey(vertex)) {
                this.vertexPool.put(vertex, this.vertexPool.size());
            }
            return this;
        }

        public Builder<D> addPolygon(IRawPolygonPayload<?, D> payload) {
            return this.addPolygon(payload.getProvider(), payload.getData(), payload.getVertices());
        }

        public Builder<D> addPolygon(@NotNull IPolygonProvider<?, D> provider, @NotNull D data, @NotNull IVertex... vertices) {
            var indices = new int[vertices.length];
            for (var i = 0; i < vertices.length; i++) {
                var vertex = vertices[i];
                var vertexIndex = this.vertexPool.getOrDefault(vertex, -1);
                if (vertexIndex == -1) {
                    vertexIndex = this.vertexPool.size();
                    this.vertexPool.put(vertex, this.vertexPool.size());
                }
                indices[i] = vertexIndex;
            }
            this.polygons.add(f -> provider.createPolygon(f, data, indices));
            return this;
        }

        public Builder<D> addPolygon(@NotNull IPolygonProvider<?, D> provider, @NotNull D data, @NotNull ImmutableList<IVertex> vertices) {
            var indices = new int[vertices.size()];
            for (var i = 0; i < vertices.size(); i++) {
                var vertex = vertices.get(i);
                var vertexIndex = this.vertexPool.getOrDefault(vertex, -1);
                if (vertexIndex == -1) {
                    vertexIndex = this.vertexPool.size();
                    this.vertexPool.put(vertex, this.vertexPool.size());
                }
                indices[i] = vertexIndex;
            }
            this.normal.add(MeshHelper.calculateNormal(vertices.get(0), vertices.get(1), vertices.get(2)));
            this.polygons.add(f -> provider.createPolygon(f, data, indices));
            return this;
        }

        /**
         * Builds a new {@link Face} instance.
         *
         * @return The new face.
         */
        public Face<D> build() {
            return new Face<>(this.vertexPool, this.polygons, this.normal.normalize().asImmutable());
        }

    }

}
