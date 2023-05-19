package com.tridevmc.architecture.legacy.client.render.model.objson;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.tridevmc.architecture.core.ArchitectureLog;
import com.tridevmc.architecture.legacy.math.LegacyTrans3;
import com.tridevmc.architecture.legacy.math.LegacyVector3;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fml.loading.progress.StartupMessageManager;

import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Deprecated
public class LegacyOBJSON {

    private static final Gson GSON = new Gson();
    private String name;
    private double[] bounds;
    private double[][] boxes;
    private Face[] faces;
    private LegacyOBJSONVoxelizer voxelizer;
    private VoxelShape voxelized;

    public static LegacyOBJSON fromResource(ResourceLocation location) {
        return fromResource(location, LegacyTrans3.blockCenter);
    }

    public static LegacyOBJSON fromResource(ResourceLocation location, LegacyTrans3 trans) {
        // Can't use resource manager because this needs to work on the server
        var path = String.format("/data/%s/objson/%s", location.getNamespace(), location.getPath());
        var in = LegacyOBJSON.class.getResourceAsStream(path);
        var model = GSON.fromJson(new InputStreamReader(in), LegacyOBJSON.class);
        model.name = location.toString();
        model.setNormals();

        for (int i = 0; i < model.faces.length; i++) {
            var face = model.faces[i];
            face.model = model;
            for (int v = 0; v < model.faces[i].vertices.length; v++) {
                var vPos = face.vertices[v].getPos();
                face.vertices[v].pos = trans.p(vPos).toArray();
            }
        }
        model.bounds = trans.t(model.bounds);
        model.boxes = model.boxes == null ? new double[0][] : model.boxes;
        for (int i = 0; i < model.boxes.length; i++) {
            model.boxes[i] = trans.t(model.boxes[i]);
        }
        model.voxelizer = new LegacyOBJSONVoxelizer(model, 16);
        return model;
    }

    /**
     * Offsets the position of every vertex by the given vector and places it into a new OBJSON object.
     *
     * @param by the vector to offset all of the vertices by.
     * @return the new OBJSON with offset vertices.
     */
    public LegacyOBJSON offset(LegacyVector3 by) {
        LegacyOBJSON out = new LegacyOBJSON();
        out.bounds = this.bounds;
        out.boxes = this.boxes;
        out.faces = new Face[this.faces.length];
        for (int i = 0; i < this.faces.length; i++) {
            out.faces[i] = this.faces[i].clone();
            Face face = out.faces[i];
            for (int v = 0; v < out.faces[i].vertices.length; v++) {
                LegacyVector3 vPos = face.vertices[v].getPos();
                face.vertices[v].pos = vPos.add(by).toArray();
            }
        }
        return out;
    }

    public Face[] getFaces() {
        return this.faces;
    }

    public String getName() {
        return this.name;
    }

    public VoxelShape getVoxelized() {
        if (this.voxelized == null) {
            String msg = String.format("Voxelizing '%s'", this.name);
            StartupMessageManager.addModMessage(msg);
            ArchitectureLog.info(msg);
            long t0 = System.nanoTime();
            this.voxelized = this.voxelizer.voxelizeShape();
            long t1 = System.nanoTime();
            ArchitectureLog.info("Voxelized {} in {} nanos", this.name, t1 - t0);
        }
        return this.voxelized;
    }

    public VoxelShape getShape(LegacyTrans3 t, VoxelShape shape) {
        var voxelized = this.getVoxelized();
        if (!voxelized.isEmpty()) {
            for (AABB bb : voxelized.toAabbs()) {
                shape = Shapes.or(shape, t.t(Shapes.create(bb)));
            }
        } else {
            return Shapes.or(shape, t.t(Shapes.create(this.bounds[0], this.bounds[1], this.bounds[2], this.bounds[3], this.bounds[4], this.bounds[5])));
        }
        return shape;
    }

    private void setNormals() {
        for (Face face : this.faces) {
            Vertex[] vertices = face.vertices;
            Triangle tri = face.triangles[0];
            face.normal = LegacyVector3.unit(vertices[tri.vertices[1]].getPos().sub(vertices[tri.vertices[0]].getPos())
                    .cross(vertices[tri.vertices[2]].getPos().sub(vertices[tri.vertices[0]].getPos())));
        }
    }

    public LegacyOBJSONVoxelizer getVoxelizer() {
        return this.voxelizer;
    }

    public List<Edge> calculateOuterEdges() {
        // Calculate the outer edges of the mesh
        List<Edge> edges = Lists.newArrayList();
        for (Face face : this.faces) {
            Map<Edge, Integer> edgesWithCount = Maps.newHashMap();
            for (Triangle tri : face.triangles) {
                var e0 = new Edge(face.vertices[tri.vertices[0]].getPos(), face.vertices[tri.vertices[1]].getPos());
                var e1 = new Edge(face.vertices[tri.vertices[1]].getPos(), face.vertices[tri.vertices[2]].getPos());
                var e2 = new Edge(face.vertices[tri.vertices[2]].getPos(), face.vertices[tri.vertices[0]].getPos());
                edgesWithCount.put(e0, edgesWithCount.getOrDefault(e0, 0) + 1);
                edgesWithCount.put(e1, edgesWithCount.getOrDefault(e1, 0) + 1);
                edgesWithCount.put(e2, edgesWithCount.getOrDefault(e2, 0) + 1);
            }
            // Remove any repeated edges
            edges.addAll(edgesWithCount.entrySet().stream().filter(e -> e.getValue() == 1).map(Map.Entry::getKey).toList());
        }

        return edges.stream().distinct().toList();

    }

    @Deprecated
public class Face {
        LegacyOBJSON model;
        int texture;
        Vertex[] vertices;
        Triangle[] triangles;
        LegacyVector3 normal;

        public Face clone() {
            Face out = new Face();

            out.texture = this.texture;
            out.vertices = new Vertex[this.vertices.length];
            out.triangles = new Triangle[this.triangles.length];
            out.normal = new LegacyVector3(this.normal);

            for (int i = 0; i < out.vertices.length; i++) {
                out.vertices[i] = this.vertices[i].clone();
            }

            for (int i = 0; i < out.triangles.length; i++) {
                out.triangles[i] = this.triangles[i].clone();
            }

            return out;
        }

        public int getTexture() {
            return this.texture;
        }
    }

    @Deprecated
public class Triangle {
        int[] vertices;

        public Triangle clone() {
            Triangle out = new Triangle();
            out.vertices = Arrays.copyOf(this.vertices, this.vertices.length);
            return out;
        }
    }

    @Deprecated
public class Vertex {
        double[] pos;
        double[] normal;
        double[] uv;

        public LegacyVector3 getPos() {
            return new LegacyVector3(this.pos[0], this.pos[1], this.pos[2]);
        }

        public LegacyVector3 getNormal() {
            return new LegacyVector3(this.normal[0], this.normal[1], this.normal[2]);
        }

        public double getU() {
            return this.uv[0];
        }

        public double getV() {
            return this.uv[1];
        }

        public Vertex clone() {
            Vertex out = new Vertex();
            out.pos = Arrays.copyOf(this.pos, this.pos.length);
            out.normal = Arrays.copyOf(this.normal, this.normal.length);
            out.uv = Arrays.copyOf(this.uv, this.uv.length);
            return out;
        }
    }

    public record Edge(LegacyVector3 a, LegacyVector3 b) {

        public double aX() {
            return this.a.x();
        }

        public double aY() {
            return this.a.y();
        }

        public double aZ() {
            return this.a.z();
        }

        public double bX() {
            return this.b.x();
        }

        public double bY() {
            return this.b.y();
        }

        public double bZ() {
            return this.b.z();
        }

    }
}
