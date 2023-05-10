package com.tridevmc.architecture.core.model.mesh;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.IVector3;
import com.tridevmc.architecture.core.math.IVector3Immutable;
import com.tridevmc.architecture.core.physics.AABB;
import com.tridevmc.architecture.core.physics.PhysicsHelper;
import com.tridevmc.architecture.core.physics.Ray;
import it.unimi.dsi.fastutil.ints.IntImmutableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Default implementation of {@link IPolygon} for tris.
 *
 * @param data The data associated with this tri.
 */
public record Tri<D extends IPolygonData<D>>(@NotNull IFace<D> face,
                                             @NotNull D data,
                                             @NotNull IntImmutableList vertexIndices,
                                             @NotNull IVector3Immutable normal,
                                             @NotNull AABB aabb) implements IPolygon<D> {

    private static final double EPSILON = 1e-8;

    @SuppressWarnings("rawtypes")
    // Type erasure means this doesn't matter. We return the correctly typed version in the below method.
    private static final IPolygonProvider PROVIDER = (face, data, vertexIndices) -> {
        var v0 = (IVertex) face.getVertices().get(vertexIndices[0]);
        var v1 = (IVertex) face.getVertices().get(vertexIndices[1]);
        var v2 = (IVertex) face.getVertices().get(vertexIndices[2]);
        return new Tri(
                face,
                data,
                IntImmutableList.of(vertexIndices),
                MeshHelper.calculateNormal(v0, v1, v2),
                AABB.fromVertices(v0, v1, v2)
        );
    };

    /**
     * Gets the static tri provider instance, used to create tris from a face, data and vertex indices.
     *
     * @param <D> the type of data to be associated with the tri.
     * @return the static tri provider instance.
     */
    public static <D extends IPolygonData<D>> IPolygonProvider<Tri<D>, D> getProvider() {
        //noinspection unchecked - This method can be ugly because it makes others less ugly :)
        return PROVIDER;
    }

    public Tri {
        if (vertexIndices.size() != 3) {
            throw new IllegalArgumentException("Tris must have 3 vertices");
        }
    }

    @Override
    @NotNull
    public D getPolygonData() {
        return this.data;
    }

    @Override
    @NotNull
    public IFace<D> getFace() {
        return this.face;
    }

    @Override
    public int getVertexCount() {
        return 3;
    }

    @Override
    @NotNull
    public IntImmutableList getVertexIndices() {
        return this.vertexIndices;
    }

    @Override
    public @NotNull ImmutableList<IVertex> getVertices() {
        return ImmutableList.of(
                this.getVertex(0),
                this.getVertex(1),
                this.getVertex(2)
        );
    }

    @Override
    @NotNull
    public IVector3Immutable getNormal() {
        return this.normal;
    }

    @Override
    @NotNull
    public AABB getAABB() {
        return this.aabb;
    }

    @Override
    @Nullable
    public IVector3 intersect(Ray ray) {
        // Vertices of the triangle
        var v0 = this.getVertex(0).getPos().asImmutable();
        var v1 = this.getVertex(1).getPos().asImmutable();
        var v2 = this.getVertex(2).getPos().asImmutable();

        // Compute vectors for two of the triangle's edges
        var e1 = v1.sub(v0);
        var e2 = v2.sub(v0);

        // Compute the cross product of the ray direction and edge 2
        var p = ray.direction().cross(e2);

        // Compute the determinant
        var det = e1.dot(p);
        if (det > -EPSILON && det < EPSILON) {
            // Ray is parallel to the triangle
            return null;
        }

        var invDet = 1 / det;
        var t = ray.origin().sub(v0).mul(invDet);
        var q = t.cross(e1);

        // Compute the barycentric coordinates
        var u = t.dot(p);
        if (u < 0 || u > 1) {
            return null;
        }
        var v = ray.direction().dot(q);
        if (v < 0 || u + v > 1) {
            return null;
        }

        // Compute the distance from the ray origin to the triangle
        var t2 = e2.dot(q);
        if (t2 > EPSILON) {
            // Ray intersects the triangle
            return ray.getPoint(t2);
        }

        // Ray does not intersect the triangle
        return null;
    }


    @Override
    public boolean intersect(AABB box) {
        // We need to use the separating axis theorem to determine if the box intersects with the triangle.
        var v0 = this.getVertex(0).getPos().asMutable();
        var v1 = this.getVertex(1).getPos().asMutable();
        var v2 = this.getVertex(2).getPos().asMutable();

        // Check if any of the triangle's vertices are inside the box, if so we can exit early.
        if (box.contains(v0) || box.contains(v1) || box.contains(v2)) {
            return true;
        }

        var aabbCenter = box.center();
        var aabbSize = box.size().asImmutable().mul(0.5D);
        v0 = v0.sub(aabbCenter);
        v1 = v1.sub(aabbCenter);
        v2 = v2.sub(aabbCenter);

        // Check against the X, Y, and Z axes first, since they are the most likely to fail.
        if (PhysicsHelper.testSeparatingAxis(v0, v1, v2, IVector3.UNIT_X, aabbSize) ||
                PhysicsHelper.testSeparatingAxis(v0, v1, v2, IVector3.UNIT_Y, aabbSize) ||
                PhysicsHelper.testSeparatingAxis(v0, v1, v2, IVector3.UNIT_Z, aabbSize)) {
            return false;
        }

        // Should be slightly more efficient than creating a new array with every value calculated.
        // Plus it saves us a lot of allocations for the vectors.
        // The previous implementation allocated one array containing 13 vectors, this only allocates the one array and a total of 4 vectors.
        var edges = new IVector3[]{v1.asMutable().sub(v0), v2.asMutable().sub(v1), v0.asMutable().sub(v2)};

        var axis = IVector3.ofMutable(0, 0, 0);
        for (var i = 0; i < 3; i++) {
            axis.set(IVector3.UNIT_X);
            axis.cross(edges[i]);
            if (PhysicsHelper.testSeparatingAxis(v0, v1, v2, axis, aabbSize)) {
                return false;
            }
        }

        for (var i = 0; i < 3; i++) {
            axis.set(IVector3.UNIT_Y);
            axis.cross(edges[i]);
            if (PhysicsHelper.testSeparatingAxis(v0, v1, v2, axis, aabbSize)) {
                return false;
            }
        }

        for (var i = 0; i < 3; i++) {
            axis.set(IVector3.UNIT_Z);
            axis.cross(edges[i]);
            if (PhysicsHelper.testSeparatingAxis(v0, v1, v2, axis, aabbSize)) {
                return false;
            }
        }

        return !PhysicsHelper.testSeparatingAxis(v0, v1, v2, edges[0].asMutable().cross(edges[1]), aabbSize);
    }

    @Override
    public @NotNull IPolygon<D> transform(@NotNull IFace<D> face, @NotNull ITrans3 trans, boolean transformUVs) {
        return new Tri<D>(
                face,
                this.getPolygonData().transform(trans),
                this.getVertexIndices(),
                trans.transformNormalImmutable(this.getNormal()),
                trans.transformAABB(this.getAABB())
        );
    }

    /**
     * Builder for {@link Tri} instances.
     *
     * @param <D> The type of data that is stored on the polygons.
     */
    public static class Builder<D extends IPolygonData<D>> {

        private D data;
        private final IVertex[] vertices = new IVertex[3];
        private int nextVertex = 0;

        /**
         * Sets the polygon data for the tri.
         *
         * @param data The polygon data.
         * @return This builder.
         */
        public Builder<D> setData(D data) {
            this.data = data;
            return this;
        }

        /**
         * Adds a vertex to the tri.
         *
         * @param vertex The vertex to add.
         * @return This builder.
         */
        public Builder<D> setVertexAt(int index, IVertex vertex) {
            if (index < 0 || index > 3) {
                throw new IllegalArgumentException("Vertex index must be between 0 and 3");
            }
            this.vertices[index] = vertex;
            return this;
        }

        /**
         * Adds a vertex to the tri.
         *
         * @param vertex The vertex to add.
         * @return This builder.
         */
        public Builder<D> addVertex(IVertex vertex) {
            if (this.nextVertex < 0) {
                throw new IllegalStateException("Vertices were added to this builder by index, cannot add by order.");
            }
            if (this.nextVertex >= 3) {
                throw new IllegalStateException("Cannot add more than 3 vertices to a tri.");
            }
            this.vertices[this.nextVertex] = vertex;
            this.nextVertex++;
            return this;
        }

        /**
         * Sets the vertices for the tri.
         *
         * @param vertices The vertices to set.
         * @return This builder.
         */
        public Builder<D> withVertices(IVertex... vertices) {
            if (vertices.length != 3) {
                throw new IllegalArgumentException("Tris must have 3 vertices");
            }
            this.vertices[0] = vertices[0];
            this.vertices[1] = vertices[1];
            this.vertices[2] = vertices[2];
            return this;
        }

        /**
         * Builds a new {@link Tri} instance.
         *
         * @return The new tri.
         */
        public IRawPolygonPayload<Tri<D>, D> build() {
            if (this.data == null) {
                throw new IllegalStateException("Polygon data must be set");
            }
            if (this.vertices[0] == null || this.vertices[1] == null || this.vertices[2] == null) {
                throw new IllegalStateException("All vertices must be set");
            }
            return IRawPolygonPayload.of(Tri.getProvider(), this.data, ImmutableList.copyOf(this.vertices));
        }

    }

}
