package com.tridevmc.architecture.core.model.objson;

import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.model.Voxelizer;
import com.tridevmc.architecture.core.model.mesh.*;
import com.tridevmc.architecture.core.physics.AABB;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record OBJSON(OBJSONData data, IMesh<String, PolygonData> mesh, Voxelizer voxelizer) {

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

    public static OBJSON fromResource(ResourceLocation location) {
        return OBJSON.fromResource(location, ITrans3.BLOCK_CENTER);
    }

    public static OBJSON fromResource(ResourceLocation location, ITrans3 trans) {
        return OBJSON.fromResource(location, trans, 16);
    }

    public static OBJSON fromResource(ResourceLocation location, ITrans3 trans, int blockResolution) {
        return new OBJSON(OBJSONData.fromResource(location), trans, blockResolution);
    }

    public String name() {
        return this.data.name();
    }

    public List<AABB> voxelize() {
        return this.voxelizer.voxelize();
    }

    private static IMesh<String, PolygonData> createMesh(OBJSONData data) {
        // TODO: Might be able to simplify this now that our faces pool vertices for faster transforms just like OBJSON does?
        // OBJSON stores parts and faces in a slightly different way to how our mesh implementation, so we'll need to convert as we build.
        var builder = new Mesh.Builder<String, PolygonData>();

        for (var partData : data.parts()) {
            var part = new Part.Builder<String, PolygonData>().setId(partData.name());
            var faceMap = new Int2ObjectOpenHashMap<Face.Builder<PolygonData>>();

            for (OBJSONData.TriangleData triData : partData.triangles()) {
                // OBJSON doesn't currently support tinting, so we'll just use the default value of -1.
                var tri = new Tri.Builder<PolygonData>();
                var faceData = data.faces()[triData.face()];
                var face = faceMap.computeIfAbsent(triData.face(), i -> new Face.Builder<>());

                for (int vertIndex : triData.vertices()) {
                    var vertData = faceData.vertices()[vertIndex];
                    tri.addVertex(new Vertex(vertData.pos(), faceData.normal(), vertData.uv()));
                }

                face.addPolygon(tri.setData(
                        new PolygonData(triData.cullFace(), faceData.face(), triData.texture(), -1)
                ).build());
            }

            for (OBJSONData.QuadData quadData : partData.quads()) {
                var quad = new Quad.Builder<PolygonData>();
                var faceData = data.faces()[quadData.face()];
                var face = faceMap.computeIfAbsent(quadData.face(), i -> new Face.Builder<>());

                for (int vertIndex : quadData.vertices()) {
                    var vertData = faceData.vertices()[vertIndex];
                    quad.addVertex(new Vertex(vertData.pos(), faceData.normal(), vertData.uv()));
                }

                face.addPolygon(quad.setData(
                        new PolygonData(quadData.cullFace(), faceData.face(), quadData.texture(), -1)
                ).build());
            }

            for (var face : faceMap.values()) {
                part.addFace(face.build());
            }

            builder.addPart(part.build());
        }

        return builder.build();
    }

}
