package com.tridevmc.architecture.core.model.mesh;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.IVector3;
import com.tridevmc.architecture.core.math.IVector3Immutable;
import com.tridevmc.architecture.core.physics.AABB;
import com.tridevmc.architecture.core.physics.PhysicsHelper;
import com.tridevmc.architecture.core.physics.Ray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Default implementation of {@link IPolygon} for quads.
 *
 * @param data     The data associated with this quad.
 * @param vertices The vertices that make up this quad.
 */
public record Quad<D extends IPolygonData<D>>(@NotNull D data, @NotNull ImmutableList<IVertex> vertices,
                                           @NotNull IVector3Immutable normal,
                                           @NotNull AABB aabb) implements IPolygon<D> {

    private static final double EPSILON = 1e-8;

    public Quad {
        if (vertices.size() != 4) {
            throw new IllegalArgumentException("Quads must have 4 vertices");
        }
    }

    @Override
    @NotNull
    public D getPolygonData() {
        return this.data;
    }

    @Override
    public @NotNull ImmutableList<IVertex> getVertices() {
        return this.vertices;
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
        // Vertices of the quadrilateral
        var v0 = this.vertices.get(0).getPos().asImmutable();
        var v1 = this.vertices.get(1).getPos().asImmutable();
        var v2 = this.vertices.get(2).getPos().asImmutable();
        var v3 = this.vertices.get(3).getPos().asImmutable();

        // Compute vectors for two of the quadrilateral's edges
        var e1 = v1.sub(v0);
        var e2 = v2.sub(v0);
        var e3 = v3.sub(v0);

        // Compute the cross product of the ray direction and edge 2
        var p = ray.direction().asMutable().cross(e2);

        // Compute the determinant
        var det = e1.dot(p);
        if (det > -EPSILON && det < EPSILON) {
            // Ray is parallel to the quadrilateral
            return null;
        }

        var invDet = 1 / det;
        var t = ray.origin().asMutable().sub(v0).mul(invDet);
        var q = t.asMutable().cross(e1);

        // Compute the barycentric coordinates
        var u = t.dot(p);
        if (u < 0 || u > 1) {
            return null;
        }
        var v = ray.direction().dot(q);
        if (v < 0 || u + v > 1) {
            // Check for intersection with the second triangle of the quad
            p.set(ray.direction()).cross(e3);
            det = e1.dot(p);
            if (det > -EPSILON && det < EPSILON) {
                return null;
            }
            invDet = 1 / det;
            t.set(ray.origin()).sub(v0).mul(invDet);
            q.set(t).cross(e1);
            u = t.dot(p);
            if (u < 0 || u > 1) {
                return null;
            }
            v = ray.direction().dot(q);
            if (v < 0 || u + v > 1) {
                return null;
            }
        }

        // Compute the distance from the ray origin to the quadrilateral
        var t2 = e2.dot(q);
        if (t2 > EPSILON) {
            // Ray intersects the quadrilateral
            return ray.getPoint(t2);
        }

        // Ray does not intersect the quadrilateral
        return null;
    }

    @Override
    public boolean intersect(AABB box) {
        // Check if any of the vertices are inside the box, if so, we intersect.
        for (var vertex : this.vertices) {
            if (box.contains(vertex.getPos())) {
                return true;
            }
        }

        // Check if any of the edges intersect the box, if so, we intersect.
        for (var i = 0; i < 4; i++) {
            var v1 = this.vertices.get(i).getPos().asImmutable();
            var v2 = this.vertices.get((i + 1) % 4).getPos().asImmutable();
            var ray = new Ray(v1, v2.sub(v1));
            if (box.intersects(ray).findAny().isPresent()) {
                return true;
            }
        }

        // We've exhausted all of our quick checks, so we move on to the SAT test.
        // This is similar to what we do with Tris, but we add a few more axes to check against.
        var v0 = this.vertices.get(0).getPos().asMutable();
        var v1 = this.vertices.get(1).getPos().asMutable();
        var v2 = this.vertices.get(2).getPos().asMutable();
        var v3 = this.vertices.get(3).getPos().asMutable();

        var aabbCenter = box.center();
        var aabbSize = box.size().asImmutable().mul(0.5);
        v0 = v0.sub(aabbCenter);
        v1 = v1.sub(aabbCenter);
        v2 = v2.sub(aabbCenter);
        v3 = v3.sub(aabbCenter);

        // Check against the X, Y, and Z axes first, since they're the most likely to fail.
        // Also saves some allocations if the tests fail.
        if (PhysicsHelper.testSeparatingAxis(v0, v1, v2, v3, IVector3.UNIT_X, aabbSize) ||
                PhysicsHelper.testSeparatingAxis(v0, v1, v2, v3, IVector3.UNIT_Y, aabbSize) ||
                PhysicsHelper.testSeparatingAxis(v0, v1, v2, v3, IVector3.UNIT_Z, aabbSize)) {
            return false;
        }

        var edges = new IVector3[]{
                v1.asMutable().sub(v0),
                v2.asMutable().sub(v1),
                v3.asMutable().sub(v2),
                v0.asMutable().sub(v3)
        };
        var diagonals = new IVector3[]{
                v2.asMutable().sub(v0),
                v3.asMutable().sub(v1)
        };

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
    public @NotNull IPolygon<D> transform(@NotNull ITrans3 trans, boolean transformUVs) {
        var builder = new Builder<D>();
        for (var v : this.vertices) {
            builder.addVertex(v.transform(trans, transformUVs));
        }
        return builder.setData(this.getPolygonData().transform(trans)).build();
    }

    /**
     * Builder for {@link Quad} instances.
     *
     * @param <D> The type of data that is stored on the polygons.
     */
    public static class Builder<D extends IPolygonData<D>> {
        private D data;
        private final IVertex[] vertices = new IVertex[4];

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
         * @param vertices The vertices to set.
         * @return This builder.
         */
        public Builder<D> withVertices(IVertex... vertices) {
            if (vertices.length != 4) {
                throw new IllegalArgumentException("Quads must have 4 vertices");
            }
            this.vertices[0] = vertices[0];
            this.vertices[1] = vertices[1];
            this.vertices[2] = vertices[2];
            this.vertices[3] = vertices[3];
            return this;
        }

        /**
         * Builds a new {@link Quad} instance.
         *
         * @return The new quad.
         */
        public Quad<D> build() {
            if (this.data == null) {
                throw new IllegalStateException("Polygon data must be set");
            }
            if (this.vertices[0] == null || this.vertices[1] == null || this.vertices[2] == null || this.vertices[3] == null) {
                throw new IllegalStateException("All vertices must be set");
            }
            var v1SubV0 = this.vertices[1].getPos().asMutable().sub(this.vertices[0].getPos());
            var v2SubV0 = this.vertices[2].getPos().asMutable().sub(this.vertices[0].getPos());
            var normal = v1SubV0.cross(v2SubV0).normalize().asImmutable();
            return new Quad<>(this.data, ImmutableList.copyOf(this.vertices), normal, AABB.fromVertices(this.vertices));
        }
    }
}
