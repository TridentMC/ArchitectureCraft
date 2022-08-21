package com.tridevmc.architecture.client.render.model;

import com.mojang.math.Vector3d;
import com.tridevmc.architecture.common.utils.AABBTree;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reads an OBJSON object and converts it into voxels.
 * <p>
 * Implementation of SAT based off of https://gdbooks.gitbooks.io/3dcollisions/content/Chapter4/aabb-triangle.html
 */
public class OBJSONVoxelizer {

    private static final Vector3d xNormal = new Vector3d(1, 0, 0);
    private static final Vector3d yNormal = new Vector3d(0, 1, 0);
    private static final Vector3d zNormal = new Vector3d(0, 0, 1);

    public static VoxelShape voxelize(OBJSON model, int blockResolution) {
        double resolution = 1D / blockResolution;
        List<UnpackedTri> unpackedTris = Arrays.stream(model.getFaces()).flatMap((Function<OBJSON.Face, Stream<UnpackedTri>>) face -> Arrays.stream(face.triangles).map(t -> new UnpackedTri(face, t, resolution / 2))).collect(Collectors.toList());
        AABBTree<UnpackedTri> aabbTree = new AABBTree<>(unpackedTris, UnpackedTri::getBox);

        double[] xS = unpackedTris.stream().flatMapToDouble(t -> Arrays.stream(t.getXs())).sorted().toArray();
        double[] yS = unpackedTris.stream().flatMapToDouble(t -> Arrays.stream(t.getYs())).sorted().toArray();
        double[] zS = unpackedTris.stream().flatMapToDouble(t -> Arrays.stream(t.getZs())).sorted().toArray();

        int minX = (int) (((resolution * Math.round(xS[0] / resolution))) / resolution);
        int minY = (int) (((resolution * Math.round(yS[0] / resolution))) / resolution);
        int minZ = (int) (((resolution * Math.round(zS[0] / resolution))) / resolution);
        int maxX = (int) (((resolution * Math.round(xS[xS.length - 1] / resolution))) / resolution);
        int maxY = (int) (((resolution * Math.round(yS[yS.length - 1] / resolution))) / resolution);
        int maxZ = (int) (((resolution * Math.round(zS[zS.length - 1] / resolution))) / resolution);


        VoxelShape out = VoxelShapes.empty();
        for (int y = minY; y < maxY; y++) {
            AABB layer = null;
            for (int x = minX; x < maxX; x++) {
                for (int z = minZ; z < maxZ; z++) {
                    double bX = x * resolution;
                    double bY = y * resolution;
                    double bZ = z * resolution;
                    AABB box = new AABB(bX, bY, bZ, bX + resolution, bY + resolution, bZ + resolution);
                    List<UnpackedTri> tris = aabbTree.search(box);
                    if (tris.stream().anyMatch(t -> checkCollision(box.grow(1D / 16D), t))) {
                        if (layer == null) {
                            layer = box;
                        }
                        layer = layer.union(box);
                    }
                }
            }
            if (layer != null) {
                out = VoxelShapes.combine(out, VoxelShapes.create(layer), IBooleanFunction.OR);
            }
        }

        return out;
    }

    private static boolean checkCollision(AABB box, UnpackedTri tri) {
        Vector3d aabbCenter = box.getCenter();
        Vector3d aabbSize = new Vector3d(box.getXSize() / 2, box.getYSize() / 2, box.getZSize() / 2);

        Vector3d v0 = tri.getV0().subtract(aabbCenter);
        Vector3d v1 = tri.getV1().subtract(aabbCenter);
        Vector3d v2 = tri.getV2().subtract(aabbCenter);

        Vector3d l0 = v1.subtract(v0);
        Vector3d l1 = v2.subtract(v1);
        Vector3d l2 = v0.subtract(v2);

        Vector3d xAxis0 = xNormal.crossProduct(l0);
        Vector3d xAxis1 = xNormal.crossProduct(l1);
        Vector3d xAxis2 = xNormal.crossProduct(l2);

        Vector3d yAxis0 = yNormal.crossProduct(l0);
        Vector3d yAxis1 = yNormal.crossProduct(l1);
        Vector3d yAxis2 = yNormal.crossProduct(l2);

        Vector3d zAxis0 = zNormal.crossProduct(l0);
        Vector3d zAxis1 = zNormal.crossProduct(l1);
        Vector3d zAxis2 = zNormal.crossProduct(l2);

        Vector3d[] axes = new Vector3d[]{xAxis0, xAxis1, xAxis2, yAxis0, yAxis1, yAxis2, zAxis0, zAxis1, zAxis2, xNormal, yNormal, zNormal, l0.crossProduct(l1)};

        return Arrays.stream(axes).noneMatch(a -> testSeparatingAxis(v0, v1, v2, a, aabbSize));
    }

    private static boolean testSeparatingAxis(Vector3d v0, Vector3d v1, Vector3d v2, Vector3d axis, Vector3d aabbSize) {
        double v0Projection = v0.dotProduct(axis);
        double v1Projection = v1.dotProduct(axis);
        double v2Projection = v2.dotProduct(axis);

        double r = aabbSize.x * Math.abs(xNormal.dotProduct(axis)) +
                aabbSize.y * Math.abs(yNormal.dotProduct(axis)) +
                aabbSize.z * Math.abs(zNormal.dotProduct(axis));

        double[] projections = new double[]{v0Projection, v1Projection, v2Projection};
        return Math.max(-Arrays.stream(projections).max().getAsDouble(), Arrays.stream(projections).min().getAsDouble()) > r;
    }

    private static class UnpackedTri {
        private final AABB box;
        private final double[][] vertices;

        public UnpackedTri(OBJSON.Face face, OBJSON.Triangle triangle, double resolution) {
            this.vertices = new double[3][];
            for (int i = 0; i < this.vertices.length; i++) {
                // Models might be right on the edges of cube bounds. This makes sure they don't actually escape those bounds.
                DoubleUnaryOperator operator = (d) -> {
                    return d/* + ((d % resolution) >= (resolution / 2) ? (resolution % 2 - d % resolution) : -(d % resolution))*/;
                };
                this.vertices[i] = face.vertices[triangle.vertices[i]].getPos().apply(operator).toArray();
            }
            double[] xs = this.getXs();
            double[] ys = this.getYs();
            double[] zs = this.getZs();
            this.box = new AABB(Arrays.stream(xs).min().orElse(0),
                    Arrays.stream(ys).min().orElse(0),
                    Arrays.stream(zs).min().orElse(0),
                    Arrays.stream(xs).max().orElse(0),
                    Arrays.stream(ys).max().orElse(0),
                    Arrays.stream(zs).max().orElse(0));
        }

        private AABB getBox() {
            return this.box;
        }

        private double[] getXs() {
            return new double[]{this.vertices[0][0], this.vertices[1][0], this.vertices[2][0]};
        }

        private double[] getYs() {
            return new double[]{this.vertices[0][1], this.vertices[1][1], this.vertices[2][1]};
        }

        private double[] getZs() {
            return new double[]{this.vertices[0][2], this.vertices[1][2], this.vertices[2][2]};
        }

        private Vector3d getV0() {
            return new Vector3d(this.vertices[0][0], this.vertices[0][1], this.vertices[0][2]);
        }

        private Vector3d getV1() {
            return new Vector3d(this.vertices[1][0], this.vertices[1][1], this.vertices[1][2]);
        }

        private Vector3d getV2() {
            return new Vector3d(this.vertices[2][0], this.vertices[2][1], this.vertices[2][2]);
        }
    }
}
