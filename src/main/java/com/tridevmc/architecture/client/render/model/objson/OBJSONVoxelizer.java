package com.tridevmc.architecture.client.render.model.objson;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.core.ArchitectureLog;
import com.tridevmc.architecture.common.utils.AABBTree;
import com.tridevmc.architecture.common.utils.MiscUtils;
import it.unimi.dsi.fastutil.Pair;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Reads an OBJSON object and converts it into voxels.
 * <p>
 * Implementation of SAT based off of <a href="https://gdbooks.gitbooks.io/3dcollisions/content/Chapter4/aabb-triangle.html">...</a>
 */
public class OBJSONVoxelizer {

    private static final ExecutorService POOL = Executors.newWorkStealingPool();

    private static final Vec3 xNormal = new Vec3(1, 0, 0);
    private static final Vec3 yNormal = new Vec3(0, 1, 0);
    private static final Vec3 zNormal = new Vec3(0, 0, 1);

    private final LegacyOBJSON objson;
    private final Mesh mesh;
    private final int blockResolution;
    private final Vec3i min, max;
    private List<Edge> edges;

    public OBJSONVoxelizer(LegacyOBJSON objson, int blockResolution) {
        this.objson = objson;
        this.blockResolution = blockResolution;
        this.mesh = new Mesh(Arrays.stream(objson.getFaces())
                .flatMap((Function<LegacyOBJSON.Face, Stream<UnpackedTri>>) face -> Arrays.stream(face.triangles)
                        .map(t -> new UnpackedTri(face, t, 1D / (blockResolution * 64))))
                .collect(Collectors.toList()));

        var xEdges = MiscUtils.getEdges(this.mesh.tris.stream().flatMapToDouble(t -> Arrays.stream(t.getXs())));
        var yEdges = MiscUtils.getEdges(this.mesh.tris.stream().flatMapToDouble(t -> Arrays.stream(t.getYs())));
        var zEdges = MiscUtils.getEdges(this.mesh.tris.stream().flatMapToDouble(t -> Arrays.stream(t.getZs())));

        var resolution = 1D / blockResolution;
        int minX = (int) (((resolution * Math.round(xEdges.leftDouble() / resolution))) / resolution);
        int minY = (int) (((resolution * Math.round(yEdges.leftDouble() / resolution))) / resolution);
        int minZ = (int) (((resolution * Math.round(zEdges.leftDouble() / resolution))) / resolution);
        int maxX = (int) (((resolution * Math.round(xEdges.rightDouble() / resolution))) / resolution);
        int maxY = (int) (((resolution * Math.round(yEdges.rightDouble() / resolution))) / resolution);
        int maxZ = (int) (((resolution * Math.round(zEdges.rightDouble() / resolution))) / resolution);

        this.min = new Vec3i(minX, minY, minZ);
        this.max = new Vec3i(maxX, maxY, maxZ);
    }

    public VoxelShape voxelizeShape() {
        var aabbs = this.voxelize();
        return aabbs.stream()
                .map(Shapes::create)
                .reduce((a, b) -> Shapes.joinUnoptimized(a, b, BooleanOp.OR))
                .orElse(Shapes.empty())
                .optimize();
    }

    public List<AABB> voxelize() {
        var dimensions = this.max.subtract(this.min);
        var futures = new ArrayList<Future<AABB>>(dimensions.getX() * dimensions.getY() * dimensions.getZ());
        for (int y = this.min.getY(); y < this.max.getY(); y++) {
            for (int x = this.min.getX(); x < this.max.getX(); x++) {
                for (int z = this.min.getZ(); z < this.max.getZ(); z++) {
                    var box = this.getBoxForOffset(x, y, z);
                    futures.add(POOL.submit(() -> {
                        if (this.isBoxValidVoxel(box)) {
                            return box;
                        }
                        return null;
                    }));
                }
            }
        }
        return futures.stream()
                .map(f -> {
                    try {
                        return f.get();
                    } catch (Exception e) {
                        ArchitectureLog.error("Failed to voxelize model {}, throwing exception", this.objson.getName());
                        throw new RuntimeException("Failed to voxelize " + this.objson.getName(), e);
                    }
                })
                .filter(Objects::nonNull)
                .toList();
    }

    public boolean isBoxValidVoxel(AABB box) {
        return this.doesBoxIntersect(box) || this.isPointWithinPolyhedron(box.getCenter());
    }

    public AABB getBoxForOffset(Vec3i offset) {
        return this.getBoxForOffset(offset.getX(), offset.getY(), offset.getZ());
    }

    public AABB getBoxForOffset(int x, int y, int z) {
        double resolution = 1D / this.blockResolution;
        double bX = x * resolution;
        double bY = y * resolution;
        double bZ = z * resolution;
        return new AABB(bX, bY, bZ, bX + resolution, bY + resolution, bZ + resolution);
    }

    public boolean doesBoxIntersect(AABB box) {
        return this.getIntersectingTris(box.deflate(1D / (this.blockResolution * 32))).findAny().isPresent();
    }

    public Stream<UnpackedTri> getIntersectingTris(AABB box) {
        return this.mesh.search(box).stream().filter(t -> t.intersects(box));
    }

    private boolean isPointWithinPolyhedron(Vec3 point) {
        var meshBounds = this.mesh.getBounds();
        var fromPoint = new Vec3(meshBounds.minX - 1, point.y, point.z);
        var toPoint = new Vec3(meshBounds.maxX + 1, point.y, point.z);
        var rayDirection = toPoint.subtract(fromPoint);
        var ray = new Ray(fromPoint, rayDirection);

        var hits = ray.intersect(this.mesh)
                .map(h -> {
                    var hit = h.rounded();
                    return Pair.of(hit, hit.distanceTo(point));
                })
                .sorted(Comparator.comparingDouble(Pair::right))
                .toList();
        double minDistance = hits.size() > 0 ? hits.get(0).right() : Double.MAX_VALUE;
        for (var hitData : hits) {
            if (hitData.right() > minDistance) {
                return false;
            } else if (hitData.left().tri.isFacing(point)) {
                return true;
            }
        }
        return false;
    }

    public LegacyOBJSON getObjson() {
        return this.objson;
    }

    public Mesh getMesh() {
        return this.mesh;
    }

    public int getBlockResolution() {
        return this.blockResolution;
    }

    public Vec3i getMin() {
        return this.min;
    }

    public Vec3i getMax() {
        return this.max;
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

    /**
     * A record representing a ray in 3D space, with an origin and direction.
     *
     * @param origin    the origin of the ray.
     * @param direction the direction of the ray.
     */
    public record Ray(Vec3 origin, Vec3 direction) {
        /**
         * Attempts to intersect this ray with the given triangle.
         *
         * @param tri the triangle to intersect with.
         * @return a hit containing the point of intersection and the triangle, or an invalid hit if there is no intersection.
         */
        public Hit intersect(UnpackedTri tri) {
            return new Hit(this, tri.intersect(this), tri);
        }

        /**
         * Attempts to intersect this ray with the given mesh, returns a stream of valid hits.
         *
         * @param mesh the mesh to intersect with.
         * @return a stream of valid hits.
         */
        public Stream<Hit> intersect(Mesh mesh) {
            return this.intersectUnfiltered(mesh).filter(Hit::isValidHit);
        }

        /**
         * Attempts to intersect this ray with the given mesh, returns a stream of hits including invalid hits.
         *
         * @param mesh the mesh to intersect with.
         * @return a stream of hits or failed hits.
         */
        public Stream<Hit> intersectUnfiltered(Mesh mesh) {
            return mesh.search(new AABB(this.origin, this.origin.add(this.direction)))
                    .stream()
                    .map(this::intersect);
        }

        /**
         * A record representing a hit between a ray and a triangle.
         *
         * @param ray   the ray that was used to calculate any hit.
         * @param point the point of intersection, if any.
         * @param tri   the triangle that was hit, if any.
         */
        public record Hit(Ray ray, Vec3 point, UnpackedTri tri) {
            /**
             * Determines if the hit is valid, i.e. if the hit point is not null.
             *
             * @return true if the hit is valid, false otherwise.
             */
            public boolean isValidHit() {
                return this.point != null;
            }

            /**
             * Gets the distance between the hit point and the given point.
             *
             * @param point the point to calculate the distance to.
             * @return the distance between the hit point and the given point.
             */
            public double distanceTo(Vec3 point) {
                return point.distanceTo(this.point);
            }

            public Hit rounded() {
                // Round the hit point to the nearest 256th of a block.
                return new Hit(this.ray, new Vec3(Math.round(this.point.x * 256) / 256D, Math.round(this.point.y * 256) / 256D, Math.round(this.point.z * 256) / 256D), this.tri);
            }
        }
    }


    private static class UnpackedTri {
        private final AABB box;
        private final double[][] vertices;
        private final Vec3 normal;

        public UnpackedTri(LegacyOBJSON.Face face, LegacyOBJSON.Triangle triangle, double resolution) {
            this.vertices = new double[3][];
            for (int i = 0; i < this.vertices.length; i++) {
                this.vertices[i] = face.vertices[triangle.vertices[i]].getPos().toArray();
            }
            this.normal = this.getV1().subtract(this.getV0()).cross(this.getV2().subtract(this.getV0())).normalize();

            var xEdges = MiscUtils.getEdges(Arrays.stream(this.getXs()));
            var yEdges = MiscUtils.getEdges(Arrays.stream(this.getYs()));
            var zEdges = MiscUtils.getEdges(Arrays.stream(this.getZs()));

            this.box = new AABB(
                    xEdges.leftDouble(),
                    yEdges.leftDouble(),
                    zEdges.leftDouble(),
                    xEdges.rightDouble(),
                    yEdges.rightDouble(),
                    zEdges.rightDouble()
            );
        }

        public Vec3 intersect(Ray ray) {
            // https://en.wikipedia.org/wiki/M%C3%B6ller%E2%80%93Trumbore_intersection_algorithm
            var e1 = this.getV1().subtract(this.getV0());
            var e2 = this.getV2().subtract(this.getV0());
            var p = ray.direction().cross(e2);
            var det = e1.dot(p);
            if (det > -1e-8 && det < 1e-8) {
                return null;
            }
            var invDet = 1 / det;
            var t = ray.origin().subtract(this.getV0()).scale(invDet);
            var q = t.cross(e1);
            var u = t.dot(p);
            if (u < 0 || u > 1) {
                return null;
            }
            var v = ray.direction().dot(q);
            if (v < 0 || u + v > 1) {
                return null;
            }
            var t2 = e2.dot(q);
            if (t2 > 1e-8) {
                return ray.origin().add(ray.direction().scale(t2));
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
            return this.normal.dot(point.subtract(this.getV0())) < 0;
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

    public static class Mesh {
        private final List<UnpackedTri> tris;
        private final AABBTree<UnpackedTri> trisTree;

        public Mesh(List<UnpackedTri> unpackedTris) {
            this.tris = ImmutableList.copyOf(unpackedTris);
            this.trisTree = new AABBTree<UnpackedTri>(unpackedTris, UnpackedTri::getBox);
        }

        public AABBTree<UnpackedTri>.Node getRoot() {
            return this.trisTree.getRoot();
        }

        public AABB getBounds() {
            return this.trisTree.getBounds();
        }

        public List<UnpackedTri> search(AABB box) {
            return this.trisTree.search(box);
        }
    }

    public record Edge(Vec3 v0, Vec3 v1) {
    }

}
