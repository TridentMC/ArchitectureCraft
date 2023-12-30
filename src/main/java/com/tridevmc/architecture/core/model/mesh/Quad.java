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
 * Default implementation of {@link IPolygon} for quads.
 *
 * @param data The data associated with this quad.
 */
public record Quad<D extends IPolygonData<D>>(@NotNull IFace<D> face,
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
        var v3 = (IVertex) face.getVertices().get(vertexIndices[3]);
        return new Quad(
                face,
                data,
                IntImmutableList.of(vertexIndices),
                MeshHelper.calculateNormal(v0, v1, v2),
                AABB.fromVertices(v0, v1, v2, v3)
        );
    };

    public Quad {
        if (vertexIndices.size() != 4) {
            throw new IllegalArgumentException("Quads must have 4 vertices");
        }
    }

    /**
     * Gets the static quad provider instance, used to create quads from a face, data and vertex indices.
     *
     * @param <D> the type of data to be associated with the quad.
     * @return the static quad provider instance.
     */
    public static <D extends IPolygonData<D>> IPolygonProvider<Quad<D>, D> getProvider() {
        //noinspection unchecked - This method can be ugly because it makes others less ugly :)
        return PROVIDER;
    }

    @Override
    @NotNull
    public D getPolygonData() {
        return this.data;
    }

    @Override
    public @NotNull IFace<D> getFace() {
        return this.face;
    }

    @Override
    public int getVertexCount() {
        return 4;
    }

    @Override
    public @NotNull IntImmutableList getVertexIndices() {
        return this.vertexIndices;
    }

    @Override
    public @NotNull ImmutableList<IVertex> getVertices() {
        return ImmutableList.of(
                this.getVertex(0),
                this.getVertex(1),
                this.getVertex(2),
                this.getVertex(3)
        );
    }

    @Override
    @NotNull
    public IVector3Immutable getNormal() {
        return this.normal;
    }

    @Override
    public @NotNull AABB getAABB() {
        return this.aabb;
    }

    @Override
    @Nullable
    public IVector3 intersect(Ray ray) {
        // We treat the ray as if it starts at the set origin and continues infinitely in the set direction.
        // We then find the point of intersection between the ray and the plane of the quad, and return it.
        // This method ignores the direction of the quad, so a hit can be found from either side.

        var eps = EPSILON;
        var rayOrigin = ray.origin();
        var rayDirection = ray.direction();
        var planeNormal = this.normal;
        var planePoint = this.getVertex(0).getPos();

        var denom = planeNormal.dot(rayDirection);
        // Check if the ray intersects the plane at all.
        if (Math.abs(denom) > eps) {
            // Find the point of intersection, then confirm it's within the quad.
            var t = planePoint.asMutable().sub(rayOrigin).dot(planeNormal) / denom;
            if (t >= 0) {
                var hitPoint = rayOrigin.asMutable().add(rayDirection.mul(t));
                if (this.containsPoint(hitPoint)) {
                    return hitPoint.asImmutable();
                }
            }
        }

        return null;
    }

    private boolean containsPoint(IVector3 point) {
        // Checks if the given point is within the quad, assuming it's on the same plane as the quad.
        var eps = EPSILON;

        var p0 = this.getVertex(0).getPos();
        var p1 = this.getVertex(1).getPos();
        var p2 = this.getVertex(2).getPos();
        var p3 = this.getVertex(3).getPos();

        var e0 = p1.asMutable().sub(p0);
        var e1 = p2.asMutable().sub(p1);
        var e2 = p3.asMutable().sub(p2);
        var e3 = p0.asMutable().sub(p3);
        var v0 = point.asMutable().sub(p0);
        var v1 = point.asMutable().sub(p1);
        var v2 = point.asMutable().sub(p2);
        var v3 = point.asMutable().sub(p3);

        var c0 = e0.cross(v0);
        var c1 = e1.cross(v1);
        var c2 = e2.cross(v2);
        var c3 = e3.cross(v3);

        return c0.dot(c1) > -eps && c1.dot(c2) > -eps && c2.dot(c3) > -eps && c3.dot(c0) > -eps;

    }

    @Override
    public boolean intersect(AABB box) {
        // Check if any of the vertices are inside the box, if so, we intersect.
        if (box.contains(this.getVertex(0).getPos()) || box.contains(this.getVertex(1).getPos()) || box.contains(this.getVertex(2).getPos()) || box.contains(this.getVertex(3).getPos())) {
            return true;
        }

        // We've exhausted all of our quick checks, so we move on to the SAT test.
        // This is similar to what we do with Tris, but we add a few more axes to check against.
        var v0 = this.getVertex(0).getPos().asMutable();
        var v1 = this.getVertex(1).getPos().asMutable();
        var v2 = this.getVertex(2).getPos().asMutable();
        var v3 = this.getVertex(3).getPos().asMutable();

        var aabbCenter = box.center();
        var aabbSize = box.size().asImmutable().mul(0.5);
        v0 = v0.sub(aabbCenter);
        v1 = v1.sub(aabbCenter);
        v2 = v2.sub(aabbCenter);
        v3 = v3.sub(aabbCenter);

        // Check against the X, Y, and Z axes first, since they're the most likely to fail.
        // Also saves some allocations if the tests fail.
        if (PhysicsHelper.testSeparatingAxis(v0, v1, v2, v3, IVector3.UNIT_X, aabbSize) || PhysicsHelper.testSeparatingAxis(v0, v1, v2, v3, IVector3.UNIT_Y, aabbSize) || PhysicsHelper.testSeparatingAxis(v0, v1, v2, v3, IVector3.UNIT_Z, aabbSize)) {
            return false;
        }

        var edges = new IVector3[]{v1.asMutable().sub(v0), v2.asMutable().sub(v1), v3.asMutable().sub(v2), v0.asMutable().sub(v3)};
        var diagonals = new IVector3[]{v2.asMutable().sub(v0), v3.asMutable().sub(v1)};

        var axis = IVector3.ofMutable(0, 0, 0);
        for (var i = 0; i < 4; i++) {
            axis.set(IVector3.UNIT_X);
            axis.cross(edges[i]);
            if (PhysicsHelper.testSeparatingAxis(v0, v1, v2, v3, axis, aabbSize)) {
                return false;
            }
        }

        for (var i = 0; i < 4; i++) {
            axis.set(IVector3.UNIT_Y);
            axis.cross(edges[i]);
            if (PhysicsHelper.testSeparatingAxis(v0, v1, v2, v3, axis, aabbSize)) {
                return false;
            }
        }

        for (var i = 0; i < 4; i++) {
            axis.set(IVector3.UNIT_Z);
            axis.cross(edges[i]);
            if (PhysicsHelper.testSeparatingAxis(v0, v1, v2, v3, axis, aabbSize)) {
                return false;
            }
        }

        for (var i = 0; i < 2; i++) {
            axis.set(diagonals[i]);
            if (PhysicsHelper.testSeparatingAxis(v0, v1, v2, v3, axis, aabbSize)) {
                return false;
            }
        }

        for (var i = 0; i < 4; i++) {
            axis.set(edges[i]);
            // Cross it by its neighbouring element, ie 0 -> 1, 1 -> 2, 2 -> 3, 3 -> 0
            axis.cross(edges[(i + 1) % 4]);
            if (PhysicsHelper.testSeparatingAxis(v0, v1, v2, v3, axis, aabbSize)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public @NotNull IPolygon<D> transform(@NotNull IFace<D> face, @NotNull ITrans3 trans, boolean transformUVs) {
        return new Quad<D>(
                face,
                this.getPolygonData().transform(trans),
                this.getVertexIndices(),
                trans.transformNormalImmutable(this.getNormal()),
                trans.transformAABB(this.getAABB())
        );
    }

    @Override
    public String toString() {
        return "Quad{" +
                "data=" + this.data +
                ", vertexIndices=" + this.vertexIndices +
                ", normal=" + this.normal +
                ", aabb=" + this.aabb +
                '}';
    }

    /**
     * Builder for {@link Quad} instances.
     *
     * @param <D> The type of data that is stored on the polygons.
     */
    public static class Builder<D extends IPolygonData<D>> {

        private final IVertex[] vertices = new IVertex[4];
        private D data;
        private int nextVertex = 0;

        /**
         * Sets the polygon data for the quad.
         *
         * @param data The polygon data.
         * @return This builder.
         */
        public Builder<D> setData(D data) {
            this.data = data;
            return this;
        }

        /**
         * Adds a vertex to the quad.
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
         * Adds a vertex to the quad.
         *
         * @param vertex The vertex to add.
         * @return This builder.
         */
        public Builder<D> addVertex(IVertex vertex) {
            if (this.nextVertex < 0) {
                throw new IllegalStateException("Vertices were added to this builder by index, cannot add by order.");
            }
            if (this.nextVertex >= 4) {
                throw new IllegalStateException("Cannot add more than 4 vertices to a quad.");
            }
            this.vertices[this.nextVertex] = vertex;
            this.nextVertex++;
            return this;
        }

        /**
         * Sets the vertices for the quad.
         *
         * @param v0 The first vertex.
         * @param v1 The second vertex.
         * @param v2 The third vertex.
         * @param v3 The fourth vertex.
         * @return This builder.
         */
        public Builder<D> withVertices(IVertex v0, IVertex v1, IVertex v2, IVertex v3) {
            if (v0 == null || v1 == null || v2 == null || v3 == null) {
                throw new IllegalArgumentException("Vertices cannot be null");
            }
            this.vertices[0] = v0;
            this.vertices[1] = v1;
            this.vertices[2] = v2;
            this.vertices[3] = v3;
            return this;
        }

        /**
         * Builds a new {@link Quad} instance.
         *
         * @return The new quad.
         */
        public IRawPolygonPayload<Quad<D>, D> build() {
            if (this.data == null) {
                throw new IllegalStateException("Polygon data must be set");
            }
            if (this.vertices[0] == null || this.vertices[1] == null
                    || this.vertices[2] == null || this.vertices[3] == null) {
                throw new IllegalStateException("All vertices must be set");
            }
            return IRawPolygonPayload.of(Quad.getProvider(), this.data, ImmutableList.copyOf(this.vertices));
        }

    }

}
