package com.tridevmc.architecture.core.model.objson;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.IVector3;
import com.tridevmc.architecture.core.model.mesh.Face;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.IVertex;
import com.tridevmc.architecture.core.model.mesh.Mesh;
import com.tridevmc.architecture.core.model.mesh.MeshHelper;
import com.tridevmc.architecture.core.model.mesh.Part;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import com.tridevmc.architecture.core.model.mesh.Quad;
import com.tridevmc.architecture.core.model.mesh.Tri;
import com.tridevmc.architecture.core.model.mesh.Vertex;
import com.tridevmc.architecture.core.model.voxelize.IVoxelizer;
import com.tridevmc.architecture.core.model.voxelize.Voxelizer;
import com.tridevmc.architecture.core.physics.AABB;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public record OBJSON(OBJSONData data, IMesh<String, PolygonData> mesh, IVoxelizer voxelizer) {

    public OBJSON(OBJSONData data, IMesh<String, PolygonData> mesh, int blockResolution) {
        this(data, mesh, new Voxelizer(mesh, blockResolution));
    }

    public OBJSON(OBJSONData data, IMesh<String, PolygonData> mesh) {
        this(data, mesh, 16);
    }

    public OBJSON(OBJSONData data, @NotNull ITrans3 trans, int blockResolution) {
        this(data, createMesh(data).transform(trans, false), blockResolution);
    }

    public OBJSON(OBJSONData data, @NotNull ITrans3 trans) {
        this(data, trans, 16);
    }

    public static OBJSON fromResource(Identifier location) {
        return OBJSON.fromResource(location, ITrans3.BLOCK_CENTER);
    }

    public static OBJSON fromResource(Identifier location, ITrans3 trans) {
        return OBJSON.fromResource(location, trans, 16);
    }

    public static OBJSON fromResource(Identifier location, ITrans3 trans, int blockResolution) {
        return new OBJSON(OBJSONData.fromResource(location), trans, blockResolution);
    }

    private static IMesh<String, PolygonData> createMesh(OBJSONData data) {
        // TODO: Might be able to simplify this now that our faces pool vertices for faster transforms just like OBJSON does?
        // OBJSON stores parts and faces in a slightly different way to how our mesh implementation, so we'll need to convert as we build.
        var builder = new Mesh.Builder<String, PolygonData>(data.name());

        for (var partData : data.parts()) {
            var part = new Part.Builder<String, PolygonData>().setId(partData.name());
            var faceMap = new Int2ObjectOpenHashMap<Face.Builder<PolygonData>>();

            // OBJSON doesn't currently support tinting, so we'll just use the default value of -1.
            for (var triangle : partData.trianglesAsOptional().orElse(new OBJSONData.TriangleData[0])) {
                addPolygon(faceMap, triangle.face(), data.faces()[triangle.face()], triangle.vertices(),
                        new PolygonData(triangle.cullFace(), data.faces()[triangle.face()].face(), triangle.texture(), -1));
            }
            for (var quad : partData.quadsAsOptional().orElse(new OBJSONData.QuadData[0])) {
                addPolygon(faceMap, quad.face(), data.faces()[quad.face()], quad.vertices(),
                        new PolygonData(quad.cullFace(), data.faces()[quad.face()].face(), quad.texture(), -1));
            }

            for (var face : faceMap.values()) {
                part.addFace(face.build());
            }

            builder.addPart(part.build());
        }

        return builder.build();
    }

    private static void addPolygon(Int2ObjectOpenHashMap<Face.Builder<PolygonData>> faces, int faceIndex,
            OBJSONData.FaceData faceData, int[] indices, PolygonData metadata) {
        var builder = ImmutableList.<IVertex>builder();
        for (var index : indices) {
            var vertex = faceData.vertices()[index];
            builder.add(new Vertex(vertex.pos(), faceData.normal(), vertex.uv()));
        }
        var vertices = builder.build();
        var authored = faceData.normal();
        var authoredNormal = IVector3.ofImmutable(authored[0], authored[1], authored[2]);
        if (vertices.size() == 4 && needsTriangulation(vertices, authoredNormal)) {
            var internalDiagonal = normal(vertices, 0, 1, 2).dot(normal(vertices, 0, 2, 3)) >= 0;
            if (internalDiagonal) {
                addPolygon(faces, faceIndex, faceData, new int[]{indices[0], indices[1], indices[2]}, metadata);
                addPolygon(faces, faceIndex, faceData, new int[]{indices[0], indices[2], indices[3]}, metadata);
            } else {
                addPolygon(faces, faceIndex, faceData, new int[]{indices[0], indices[1], indices[3]}, metadata);
                addPolygon(faces, faceIndex, faceData, new int[]{indices[1], indices[2], indices[3]}, metadata);
            }
            return;
        }
        var normal = MeshHelper.calculateNormal(vertices.get(0), vertices.get(1), vertices.get(2));
        if (!Double.isFinite(normal.lengthSquared()) || normal.lengthSquared() < 1e-12) {
            return;
        }
        if (normal.dot(authoredNormal) < 0) {
            vertices = vertices.reverse();
        }
        var face = faces.computeIfAbsent(faceIndex, index -> new Face.Builder<>());
        if (vertices.size() == 3) {
            face.addPolygon(Tri.getProvider(), metadata, vertices);
        } else {
            face.addPolygon(Quad.getProvider(), metadata, vertices);
        }
    }

    private static boolean needsTriangulation(ImmutableList<IVertex> vertices, IVector3 authoredNormal) {
        var positive = false;
        var negative = false;
        for (int i = 0; i < 4; i++) {
            var normal = normal(vertices, i, (i + 1) % 4, (i + 2) % 4);
            if (normal.lengthSquared() < 1e-18) {
                return true;
            }
            var alignment = normal.dot(authoredNormal);
            positive |= alignment > 1e-12;
            negative |= alignment < -1e-12;
        }
        return positive && negative;
    }

    private static IVector3 normal(ImmutableList<IVertex> vertices, int first, int second, int third) {
        var origin = vertices.get(first).getPos();
        return vertices.get(second).getPos().asMutable().sub(origin)
                .cross(vertices.get(third).getPos().asMutable().sub(origin));
    }

    public String name() {
        return this.data.name();
    }

    public CompletableFuture<ImmutableList<AABB>> voxelize() {
        return this.voxelizer.voxelize();
    }

    public List<AABB> voxelizeNow() {
        return this.voxelizer.voxelizeNow();
    }

}
