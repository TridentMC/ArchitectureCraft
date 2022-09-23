package com.tridevmc.architecture.client.render.model.objson;

import com.tridevmc.architecture.common.utils.AABBTree;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
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

    private static final Vec3 xNormal = new Vec3(1, 0, 0);
    private static final Vec3 yNormal = new Vec3(0, 1, 0);
    private static final Vec3 zNormal = new Vec3(0, 0, 1);

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


        VoxelShape out = Shapes.empty();
        for (int y = minY; y < maxY; y++) {
            AABB layer = null;
            for (int x = minX; x < maxX; x++) {
                for (int z = minZ; z < maxZ; z++) {
                    double bX = x * resolution;
                    double bY = y * resolution;
                    double bZ = z * resolution;
                    AABB box = new AABB(bX, bY, bZ, bX + resolution, bY + resolution, bZ + resolution);
                    List<UnpackedTri> tris = aabbTree.search(box);
                    if (tris.stream().anyMatch(t -> checkCollision(box.inflate(1D / 16D), t))) {
                        if (layer == null) {
                            layer = box;
                        }
                        layer = layer.minmax(box);
                    }
                }
            }
            if (layer != null) {
                out = Shapes.join(out, Shapes.create(layer), BooleanOp.OR);
            }
        }

        return out;
    }

    private static boolean checkCollision(AABB box, UnpackedTri tri) {
        var aabbCenter = box.getCenter();
        var aabbSize = new Vec3(box.getXsize() / 2, box.getYsize() / 2, box.getZsize() / 2);

        var v0 = tri.getV0().subtract(aabbCenter);
        var v1 = tri.getV1().subtract(aabbCenter);
        var v2 = tri.getV2().subtract(aabbCenter);

        var l0 = v1.subtract(v0);
        var l1 = v2.subtract(v1);
        var l2 = v0.subtract(v2);

        var xAxis0 = xNormal.cross(l0);
        var xAxis1 = xNormal.cross(l1);
        var xAxis2 = xNormal.cross(l2);

        var yAxis0 = yNormal.cross(l0);
        var yAxis1 = yNormal.cross(l1);
        var yAxis2 = yNormal.cross(l2);

        var zAxis0 = zNormal.cross(l0);
        var zAxis1 = zNormal.cross(l1);
        var zAxis2 = zNormal.cross(l2);

        var axes = new Vec3[]{xAxis0, xAxis1, xAxis2, yAxis0, yAxis1, yAxis2, zAxis0, zAxis1, zAxis2, xNormal, yNormal, zNormal, l0.cross(l1)};

        return Arrays.stream(axes).noneMatch(a -> testSeparatingAxis(v0, v1, v2, a, aabbSize));
    }

    private static boolean testSeparatingAxis(Vec3 v0, Vec3 v1, Vec3 v2, Vec3 axis, Vec3 aabbSize) {
        double v0Projection = v0.dot(axis);
        double v1Projection = v1.dot(axis);
        double v2Projection = v2.dot(axis);

        double r = aabbSize.x * Math.abs(xNormal.dot(axis)) +
                aabbSize.y * Math.abs(yNormal.dot(axis)) +
                aabbSize.z * Math.abs(zNormal.dot(axis));

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

        private Vec3 getV0() {
            return new Vec3(this.vertices[0][0], this.vertices[0][1], this.vertices[0][2]);
        }

        private Vec3 getV1() {
            return new Vec3(this.vertices[1][0], this.vertices[1][1], this.vertices[1][2]);
        }

        private Vec3 getV2() {
            return new Vec3(this.vertices[2][0], this.vertices[2][1], this.vertices[2][2]);
        }
    }
}
