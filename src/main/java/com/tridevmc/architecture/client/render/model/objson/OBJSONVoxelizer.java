package com.tridevmc.architecture.client.render.model.objson;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.common.utils.AABBTree;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
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

    public static VoxelShape voxelizeShape(OBJSON model, int blockResolution) {
        var aabbs = voxelize(model, blockResolution);
        return aabbs.stream()
                .map(Shapes::create)
                .reduce((a, b) -> Shapes.joinUnoptimized(a, b, BooleanOp.OR))
                .orElse(Shapes.empty())
                .optimize();
    }

    public static List<AABB> voxelize(OBJSON model, int blockResolution) {
        double resolution = 1D / blockResolution;
        List<UnpackedTri> unpackedTris = Arrays.stream(model.getFaces()).flatMap((Function<OBJSON.Face, Stream<UnpackedTri>>) face -> Arrays.stream(face.triangles).map(t -> new UnpackedTri(face, t, 1D / (blockResolution * 64)))).collect(Collectors.toList());
        Mesh mesh = new Mesh(unpackedTris);

        double[] xS = unpackedTris.stream().flatMapToDouble(t -> Arrays.stream(t.getXs())).sorted().toArray();
        double[] yS = unpackedTris.stream().flatMapToDouble(t -> Arrays.stream(t.getYs())).sorted().toArray();
        double[] zS = unpackedTris.stream().flatMapToDouble(t -> Arrays.stream(t.getZs())).sorted().toArray();

        int minX = (int) (((resolution * Math.round(xS[0] / resolution))) / resolution);
        int minY = (int) (((resolution * Math.round(yS[0] / resolution))) / resolution);
        int minZ = (int) (((resolution * Math.round(zS[0] / resolution))) / resolution);
        int maxX = (int) (((resolution * Math.round(xS[xS.length - 1] / resolution))) / resolution);
        int maxY = (int) (((resolution * Math.round(yS[yS.length - 1] / resolution))) / resolution);
        int maxZ = (int) (((resolution * Math.round(zS[zS.length - 1] / resolution))) / resolution);

        var out = new ArrayList<AABB>();
        for (int y = minY; y < maxY; y++) {
            for (int x = minX; x < maxX; x++) {
                for (int z = minZ; z < maxZ; z++) {
                    double bX = x * resolution;
                    double bY = y * resolution;
                    double bZ = z * resolution;
                    var box = new AABB(bX, bY, bZ, bX + resolution, bY + resolution, bZ + resolution);
                    if (doesBoxIntersectWithPolyhedron(mesh, box.deflate(1D /( blockResolution * 32))) || isPointWithinPolyhedron(mesh, box.getCenter())) {
                        out.add(box);
                    }
                }
            }
        }
        return out;
    }

    private static boolean doesBoxIntersectWithPolyhedron(Mesh polyhedron, AABB box) {
        return polyhedron.search(box).stream().anyMatch(t -> t.intersects(box));
    }

    private static boolean isPointWithinPolyhedron(Mesh polyhedron, Vec3 point) {
        var meshBounds = polyhedron.getBounds();
        var closestX = point.x - meshBounds.minX > meshBounds.maxX - point.x ? meshBounds.maxX + 64 : meshBounds.minX - 64;
        var closestY = point.y - meshBounds.minY > meshBounds.maxY - point.y ? meshBounds.maxY + 64 : meshBounds.minY - 64;
        var closestZ = point.z - meshBounds.minZ > meshBounds.maxZ - point.z ? meshBounds.maxZ + 64 : meshBounds.minZ - 64;

        var closestPoints = Stream.of(
                        new Vec3(closestX, point.y, point.z),
                        new Vec3(point.x, closestY, point.z),
                        new Vec3(point.x, point.y, closestZ))
                .sorted(Comparator.comparingDouble(point::distanceTo)).toList();
        for (var p : closestPoints) {
            var ray = new Ray(point, p.subtract(point));
            var intersections = polyhedron.search(new AABB(point, p)).stream()
                    .map(t -> ImmutablePair.of(t, ray.intersect(t)))
                    .filter(ip -> ip.getRight() != null)
                    .sorted(Comparator.comparingDouble(ip -> ip.getRight().distanceTo(point)))
                    .toList();
            if (intersections.size() > 0) {
                var minPoint = intersections.get(0).getRight();
                if (intersections.stream()
                        .filter(ip -> ip.getRight().equals(minPoint))
                        .anyMatch(ip -> ip.getLeft().isFacing(point))) {
                    return true;
                }
            }
        }
        return false;
    }

    private static double getFastWindingNumber(Mesh polyhedron, Vec3 point) {
        return getFastWindingNumber(polyhedron.getRoot(), point);
    }

    private static double getFastWindingNumber(AABBTree<UnpackedTri>.Node node, Vec3 point) {
        // Fast Approximation of Winding Number: https://www.dgp.toronto.edu/projects/fast-winding-numbers/fast-winding-numbers-for-soups-and-clouds-siggraph-2018-barill-et-al.pdf
        // I have no clue how any of this works, I just tried my best to implement it based on the pseudocode in the paper.
        //var tolerance = 2;
        //var treeBounds = node.getValue();
        //var treeP = treeBounds.getCenter();
        //var treeR = (Math.pow(treeBounds.getXsize(), 2) + Math.pow(treeBounds.getYsize(), 2) + Math.pow(treeBounds.getZsize(), 2)) / 2;
        //if (point.subtract(treeP).length() > (treeR * tolerance) && treeR != 0) {
        //    // q is sufficiently far from all elements in tree.
        //    var dist = treeP.subtract(point).length();
        //    return treeP.subtract(point).dot(polyhedron.meanNormal) / (4 * Math.PI * dist * dist * dist);
        //} else {
        //    var value = 0;
        //    if()
        //}
        return 0;
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

    private static class Ray {
        private final Vec3 origin;
        private final Vec3 direction;

        public Ray(Vec3 origin, Vec3 direction) {
            this.origin = origin;
            this.direction = direction;
        }

        public Vec3 getOrigin() {
            return origin;
        }

        public Vec3 getDirection() {
            return direction;
        }

        public Vec3 intersect(UnpackedTri tri) {
            return tri.intersect(this);
        }
    }

    private static class UnpackedTri {
        private final AABB box;
        private final double[][] vertices;

        public UnpackedTri(OBJSON.Face face, OBJSON.Triangle triangle, double resolution) {
            this.vertices = new double[3][];
            for (int i = 0; i < this.vertices.length; i++) {
                this.vertices[i] = face.vertices[triangle.vertices[i]].getPos().apply(d->{
                    // Clamp to the nearest multiple of resolution
                    return d;
                }).toArray();
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

        public Vec3 intersect(Ray ray) {
            // https://en.wikipedia.org/wiki/M%C3%B6ller%E2%80%93Trumbore_intersection_algorithm
            var e1 = this.getV1().subtract(this.getV0());
            var e2 = this.getV2().subtract(this.getV0());
            var p = ray.getDirection().cross(e2);
            var det = e1.dot(p);
            if (det > -1e-8 && det < 1e-8) {
                return null;
            }
            var invDet = 1 / det;
            var t = ray.getOrigin().subtract(this.getV0()).scale(invDet);
            var q = t.cross(e1);
            var u = t.dot(p);
            if (u < 0 || u > 1) {
                return null;
            }
            var v = ray.getDirection().dot(q);
            if (v < 0 || u + v > 1) {
                return null;
            }
            var t2 = e2.dot(q);
            if (t2 > 1e-8) {
                return ray.getOrigin().add(ray.getDirection().scale(t2));
            }
            return null;
        }

        public boolean intersects(AABB box) {
            var aabbCenter = box.getCenter();
            var aabbSize = new Vec3(box.getXsize(), box.getYsize(), box.getZsize()).scale(0.5D);

            var v0 = this.getV0().subtract(aabbCenter);
            var v1 = this.getV1().subtract(aabbCenter);
            var v2 = this.getV2().subtract(aabbCenter);

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

        public boolean isFacing(Vec3 point) {
            // Determine if the triangle is facing towards the given point, the triangle's vertices are stored in a clockwise order
            var v0 = this.getV0();
            var v1 = this.getV1();
            var v2 = this.getV2();
            var normal = v1.subtract(v0).cross(v2.subtract(v0)).normalize();
            return normal.dot(point.subtract(v0)) < 0;
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

    private static class Mesh {
        private final List<UnpackedTri> tris;
        private final AABBTree<UnpackedTri> trisTree;
        private final Vec3 meanNormal;

        public Mesh(List<UnpackedTri> unpackedTris) {
            this.tris = ImmutableList.copyOf(unpackedTris);
            this.trisTree = new AABBTree<UnpackedTri>(unpackedTris, UnpackedTri::getBox);

            var meanNormal = new Vec3(0, 0, 0);
            for (var unpackedTri : this.tris) {
                // Calculate the area of the triangle
                var area = unpackedTri.getV0().subtract(unpackedTri.getV1()).cross(unpackedTri.getV0().subtract(unpackedTri.getV2())).length() / 2;
                meanNormal = meanNormal.add(unpackedTri.getV0().subtract(unpackedTri.getV1()).cross(unpackedTri.getV0().subtract(unpackedTri.getV2())).scale(area));
            }
            this.meanNormal = meanNormal;
        }

        public AABBTree<UnpackedTri>.Node getRoot() {
            return trisTree.getRoot();
        }

        public AABB getBounds() {
            return trisTree.getBounds();
        }

        public List<UnpackedTri> search(AABB box) {
            return trisTree.search(box);
        }
    }

}
