package com.tridevmc.architecture.core.model.mesh;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.core.math.Transform;
import com.tridevmc.architecture.legacy.math.LegacyVector3;
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
public record Quad<D extends IPolygonData>(@NotNull D data, @NotNull ImmutableList<IVertex> vertices,
                                           @NotNull LegacyVector3 normal, @NotNull AABB aabb) implements IPolygon<D> {

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
    public LegacyVector3 getNormal() {
        return this.normal;
    }

    @Override
    public @NotNull AABB getAABB() {
        return this.aabb;
    }

    @Override
    @Nullable
    public LegacyVector3 intersect(Ray ray) {
        // Vertices of the quadrilateral
        var v0 = this.vertices.get(0).getPos();
        var v1 = this.vertices.get(1).getPos();
        var v2 = this.vertices.get(2).getPos();
        var v3 = this.vertices.get(3).getPos();

        // Compute vectors for two of the quadrilateral's edges
        var e1 = v1.sub(v0);
        var e2 = v2.sub(v0);
        var e3 = v3.sub(v0);

        // Compute the cross product of the ray direction and edge 2
        var p = ray.direction().cross(e2);

        // Compute the determinant
        var det = e1.dot(p);
        if (det > -EPSILON && det < EPSILON) {
            // Ray is parallel to the quadrilateral
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
            // Check for intersection with the second triangle of the quad
            p = ray.direction().cross(e3);
            det = e1.dot(p);
            if (det > -EPSILON && det < EPSILON) {
                return null;
            }
            invDet = 1 / det;
            t = ray.origin().sub(v0).mul(invDet);
            q = t.cross(e1);
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
            var v1 = this.vertices.get(i);
            var v2 = this.vertices.get((i + 1) % 4);
            if (box.intersects(new Ray(v1.getPos(), v2.getPos().sub(v1.getPos()))).findAny().isPresent()) {
                return true;
            }
        }

        // We've exhausted all of our quick checks, so we move on to the SAT test.
        // This is similar to what we do with Tris, but we add a few more axes to check against.
        var v0 = this.vertices.get(0).getPos();
        var v1 = this.vertices.get(1).getPos();
        var v2 = this.vertices.get(2).getPos();
        var v3 = this.vertices.get(3).getPos();

        var aabbCenter = box.center();
        var aabbSize = box.size().mul(0.5);
        v0 = v0.sub(aabbCenter);
        v1 = v1.sub(aabbCenter);
        v2 = v2.sub(aabbCenter);
        v3 = v3.sub(aabbCenter);

        var l0 = v1.sub(v0);
        var l1 = v2.sub(v1);
        var l2 = v3.sub(v2);
        var l3 = v0.sub(v3);

        var axes = new LegacyVector3[]{
                LegacyVector3.UNIT_X.cross(l0),
                LegacyVector3.UNIT_X.cross(l1),
                LegacyVector3.UNIT_X.cross(l2),
                LegacyVector3.UNIT_Y.cross(l0),
                LegacyVector3.UNIT_Y.cross(l1),
                LegacyVector3.UNIT_Y.cross(l2),
                LegacyVector3.UNIT_Z.cross(l0),
                LegacyVector3.UNIT_Z.cross(l1),
                LegacyVector3.UNIT_Z.cross(l2),
                LegacyVector3.UNIT_X,
                LegacyVector3.UNIT_Y,
                LegacyVector3.UNIT_Z,
                l0.cross(l1)
        };

        // Check for SAT on the first triangle, if we don't exit early, we'll check the second triangle.
        for (var axis : axes) {
            if (PhysicsHelper.testSeparatingAxis(axis, v0, v1, v2, aabbSize)) {
                return false;
            }
        }

        // Check for SAT on the second triangle, excluding any axes that are parallel to the first triangle.
        axes = new LegacyVector3[]{
                LegacyVector3.UNIT_X.cross(l2),
                LegacyVector3.UNIT_Y.cross(l2),
                LegacyVector3.UNIT_Z.cross(l2),
                l2.cross(l3)
        };

        for (var axis : axes) {
            if (PhysicsHelper.testSeparatingAxis(axis, v0, v2, v3, aabbSize)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public @NotNull IPolygon<D> transform(@NotNull Transform trans, boolean transformUVs) {
        var builder = new Builder<D>();
        for (var v : this.vertices) {
            builder.addVertex(v.transform(trans, transformUVs));
        }
        return builder.build();
    }

    /**
     * Builder for {@link Quad} instances.
     *
     * @param <D> The type of data that is stored on the polygons.
     */
    public static class Builder<D extends IPolygonData> {
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
            var normal = this.vertices[0].getPos().sub(this.vertices[1].getPos())
                    .cross(this.vertices[2].getPos().sub(this.vertices[1].getPos()))
                    .normalize();
            return new Quad<>(this.data, ImmutableList.copyOf(this.vertices), normal, AABB.fromVertices(this.vertices));
        }
    }
}
