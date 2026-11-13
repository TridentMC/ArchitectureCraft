package com.tridevmc.architecture.common.shape;

import com.tridevmc.architecture.core.math.IVector3;
import com.tridevmc.architecture.core.model.mesh.CullFace;
import com.tridevmc.architecture.core.model.mesh.Face;
import com.tridevmc.architecture.core.model.mesh.FaceDirection;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.IVertex;
import com.tridevmc.architecture.core.model.mesh.Mesh;
import com.tridevmc.architecture.core.model.mesh.Part;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import com.tridevmc.architecture.core.model.mesh.Tri;
import com.tridevmc.architecture.core.model.mesh.Vertex;

import com.google.common.collect.ImmutableList;

final class ShapeGeometry {

    private ShapeGeometry() {
    }

    static IMesh<String, PolygonData> roof(EnumShape shape, int connections) {
        var part = new Part.Builder<String, PolygonData>().setId("root");
        double[][] ring = {{0, 0}, {0.5, 0}, {1, 0}, {1, 0.5}, {1, 1}, {0.5, 1}, {0, 1}, {0, 0.5}};
        var center = IVector3.ofImmutable(0.5, height(shape, 0.5, 0.5, connections), 0.5);
        for (int i = 0; i < ring.length; i++) {
            var a = ring[i];
            var b = ring[(i + 1) % ring.length];
            var topA = IVector3.ofImmutable(a[0], height(shape, a[0], a[1], connections), a[1]);
            var topB = IVector3.ofImmutable(b[0], height(shape, b[0], b[1], connections), b[1]);
            triangle(part, 1, center, topB, topA);
            var bottomA = IVector3.ofImmutable(a[0], 0, a[1]);
            var bottomB = IVector3.ofImmutable(b[0], 0, b[1]);
            triangle(part, 0, bottomA, topB, bottomB);
            triangle(part, 0, bottomA, topA, topB);
        }
        triangle(part, 0, IVector3.ofImmutable(0, 0, 0), IVector3.ofImmutable(1, 0, 0), IVector3.ofImmutable(1, 0, 1));
        triangle(part, 0, IVector3.ofImmutable(0, 0, 0), IVector3.ofImmutable(1, 0, 1), IVector3.ofImmutable(0, 0, 1));
        return new Mesh.Builder<String, PolygonData>(shape.getName() + "_" + connections).addPart(part.build()).build();
    }

    private static double height(EnumShape shape, double x, double z, int connections) {
        return switch (shape) {
            case ROOF_TILE -> z;
            case ROOF_OUTER_CORNER -> Math.min(1 - x, z);
            case ROOF_INNER_CORNER -> Math.max(1 - x, z);
            case ROOF_RIDGE -> 0.5 - Math.abs(z - 0.5);
            case ROOF_VALLEY -> 0.5 + Math.abs(z - 0.5);
            case ROOF_SMART_RIDGE, ROOF_SMART_VALLEY -> {
                var ridge = shape == EnumShape.ROOF_SMART_RIDGE;
                var result = ridge ? 0.5 - Math.max(Math.abs(x - 0.5), Math.abs(z - 0.5))
                        : 0.5 + Math.max(Math.abs(x - 0.5), Math.abs(z - 0.5));
                var side = z == 0 && x == 0.5 ? 0 : z == 1 && x == 0.5 ? 1
                        : x == 0 && z == 0.5 ? 2 : x == 1 && z == 0.5 ? 3 : -1;
                yield side >= 0 && (connections & (1 << side)) != 0 ? 0.5 : result;
            }
            default -> throw new IllegalArgumentException("Not a procedural roof: " + shape);
        };
    }

    static void box(Part.Builder<String, PolygonData> part, double x0, double y0, double z0,
            double x1, double y1, double z1, int texture) {
        var p = new IVector3[]{IVector3.ofImmutable(x0, y0, z0), IVector3.ofImmutable(x1, y0, z0),
                IVector3.ofImmutable(x1, y1, z0), IVector3.ofImmutable(x0, y1, z0),
                IVector3.ofImmutable(x0, y0, z1), IVector3.ofImmutable(x1, y0, z1),
                IVector3.ofImmutable(x1, y1, z1), IVector3.ofImmutable(x0, y1, z1)};
        int[][] faces = {{0, 3, 2, 1}, {4, 5, 6, 7}, {0, 4, 7, 3}, {1, 2, 6, 5}, {0, 1, 5, 4}, {3, 7, 6, 2}};
        for (var face : faces) {
            triangle(part, texture, p[face[0]], p[face[1]], p[face[2]]);
            triangle(part, texture, p[face[0]], p[face[2]], p[face[3]]);
        }
    }

    private static void triangle(Part.Builder<String, PolygonData> part, int texture, IVector3 a, IVector3 b, IVector3 c) {
        var normal = b.asMutable().sub(a).cross(c.asMutable().sub(a));
        if (normal.lengthSquared() < 1e-12) {
            return;
        }
        normal.normalize();
        var vertices = ImmutableList.<IVertex>of(vertex(a, normal), vertex(b, normal), vertex(c, normal));
        var direction = Math.abs(normal.y()) >= Math.abs(normal.x()) && Math.abs(normal.y()) >= Math.abs(normal.z())
                ? (normal.y() > 0 ? FaceDirection.UP : FaceDirection.DOWN)
                : Math.abs(normal.x()) >= Math.abs(normal.z()) ? (normal.x() > 0 ? FaceDirection.EAST : FaceDirection.WEST)
                : (normal.z() > 0 ? FaceDirection.SOUTH : FaceDirection.NORTH);
        part.addFace(new Face.Builder<PolygonData>().addPolygon(Tri.getProvider(),
                new PolygonData(CullFace.NONE, direction, texture, -1), vertices).build());
    }

    private static Vertex vertex(IVector3 point, IVector3 normal) {
        var u = Math.abs(normal.x()) > 0.5 ? point.z() : point.x();
        var v = Math.abs(normal.y()) > 0.5 ? point.z() : 1 - point.y();
        return new Vertex(point.x(), point.y(), point.z(), normal.x(), normal.y(), normal.z(), u, v);
    }
}
