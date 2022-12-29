package com.tridevmc.architecture.core.model.mesh;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.core.math.Transform;
import com.tridevmc.architecture.core.math.LegacyVector3;
import com.tridevmc.architecture.core.physics.AABB;
import com.tridevmc.architecture.core.physics.PhysicsHelper;
import com.tridevmc.architecture.core.physics.Ray;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Default implementation of {@link IPolygon} for tris.
 *
 * @param data     The data associated with this tri.
 * @param vertices The vertices that make up this tri.
 */
public record Tri<D extends IPolygonData>(@NotNull D data, ImmutableList<IVertex> vertices,
                                          @NotNull LegacyVector3 normal, @NotNull AABB aabb) implements IPolygon<D> {

    private static final double EPSILON = 1e-8;

    public Tri {
        if (vertices.size() != 3) {
            throw new IllegalArgumentException("Tris must have 3 vertices");
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
    @NotNull
    public AABB getAABB() {
        return this.aabb;
    }

    @Override
    @Nullable
    public LegacyVector3 intersect(Ray ray) {
        // Vertices of the triangle
        var v0 = this.vertices.get(0).getPos();
        var v1 = this.vertices.get(1).getPos();
        var v2 = this.vertices.get(2).getPos();

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
        LegacyVector3 v0 = this.vertices.get(0).getPos();
        LegacyVector3 v1 = this.vertices.get(1).getPos();
        LegacyVector3 v2 = this.vertices.get(2).getPos();

        // Check if any of the triangle's vertices are inside the box, if so we can exit early.
        if (box.contains(v0) || box.contains(v1) || box.contains(v2)) {
            return true;
        }

        LegacyVector3 aabbCenter = box.center();
        LegacyVector3 aabbSize = box.size().mul(0.5);
        v0 = v0.sub(aabbCenter);
        v1 = v1.sub(aabbCenter);
        v2 = v2.sub(aabbCenter);
        LegacyVector3 l0 = v1.sub(v0);
        LegacyVector3 l1 = v2.sub(v1);
        LegacyVector3 l2 = v0.sub(v2);

        // Compute the separating axes
        LegacyVector3[] axes = {
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

        for (LegacyVector3 axis : axes) {
            if (PhysicsHelper.testSeparatingAxis(v0, v1, v2, axis, aabbSize)) {
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
     * Builder for {@link Tri} instances.
     *
     * @param <D> The type of data that is stored on the polygons.
     */
    public static class Builder<D extends IPolygonData> {
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
        public Tri<D> build() {
            if (this.data == null) {
                throw new IllegalStateException("Polygon data must be set");
            }
            if (this.vertices[0] == null || this.vertices[1] == null || this.vertices[2] == null) {
                throw new IllegalStateException("All vertices must be set");
            }
            var normal = this.vertices[1].getPos().sub(this.vertices[0].getPos())
                    .cross(this.vertices[2].getPos().sub(this.vertices[0].getPos()))
                    .normalize();
            return new Tri<>(this.data, ImmutableList.copyOf(this.vertices), normal, AABB.fromVertices(this.vertices));
        }
    }
}
