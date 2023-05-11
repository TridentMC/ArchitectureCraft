package com.tridevmc.architecture.core.model.objson;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.tridevmc.architecture.core.model.mesh.CullFace;
import com.tridevmc.architecture.core.model.mesh.FaceDirection;
import net.minecraft.resources.ResourceLocation;

import java.io.InputStreamReader;
import java.util.Objects;

/**
 * Represents the raw data deserialized from an OBJSON file, not to be used directly, use {@link OBJSON} instead.
 */
public record OBJSONData(String name, double[] bounds, FaceData[] faces, PartData[] parts) {

    private static final Gson GSON = new Gson();

    /**
     * Represents a part of the OBJSON model, used for grouping faces together.
     *
     * @param name      The name of the part.
     * @param bounds    The bounds of the part.
     * @param boxes     The boxes of the part.
     * @param triangles The triangles of the part.
     */
    record PartData(String name, double[] bounds, double[][] boxes, TriangleData[] triangles, QuadData[] quads) {

    }

    /**
     * Represents a face of the OBJSON model.
     *
     * @param vertices The vertices of the face.
     * @param face     The direction of the face.
     * @param normal   The normal of the face.
     */
    record FaceData(VertexData[] vertices, FaceDirection face, double[] normal) {

    }

    /**
     * Represents a vertex of the OBJSON model.
     *
     * @param pos    The position of the vertex.
     * @param normal The normal of the vertex.
     * @param uv     The UV coordinates of the vertex.
     */
    record VertexData(double[] pos, double[] normal, double[] uv) {

    }

    /**
     * Represents a triangle of the OBJSON model.
     *
     * @param face     The face of the triangle, used for obtaining the vertices from the root face array.
     * @param cullFace The cull face of the triangle.
     * @param texture  The texture of the triangle.
     * @param vertices The vertices of the triangle.
     */
    record TriangleData(int face, @SerializedName("cull_face") CullFace cullFace, int texture, int[] vertices) {

    }

    /**
     * Represents a quad of the OBJSON model.
     *
     * @param face     The face of the quad, used for obtaining the vertices from the root face array.
     * @param cullFace The cull face of the quad.
     * @param texture  The texture of the quad.
     * @param vertices The vertices of the quad.
     */
    record QuadData(int face, @SerializedName("cull_face") CullFace cullFace, int texture, int[] vertices) {

    }

    /**
     * Loads an OBJSON model from the given resource location.
     *
     * @param location The resource location of the model.
     * @return The loaded model.
     * @throws NullPointerException If no input stream could be found for the given resource location.
     */
    public static OBJSONData fromResource(ResourceLocation location) {
        var path = String.format("/data/%s/objson/%s", location.getNamespace(), location.getPath());
        var in = OBJSONData.class.getResourceAsStream(path);
        return GSON.fromJson(new InputStreamReader(Objects.requireNonNull(in, "Failed to obtain input stream for resource \"%s\"".formatted(path))), OBJSONData.class);
    }

}
