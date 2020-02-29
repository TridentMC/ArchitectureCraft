package com.tridevmc.architecture.client.render.model;

import com.google.gson.Gson;
import com.tridevmc.architecture.common.helpers.Vector3;
import net.minecraft.util.ResourceLocation;

import java.io.InputStream;
import java.io.InputStreamReader;

public class OBJSON {

    private static final Gson GSON = new Gson();
    public double[] bounds;
    public Face[] faces;
    public double[][] boxes;

    public static OBJSON fromResource(ResourceLocation location) {
        // Can't use resource manager because this needs to work on the server
        String path = String.format("/assets/%s/%s", location.getNamespace(), location.getPath());
        InputStream in = OBJSONRenderableModel.class.getResourceAsStream(path);
        OBJSON model = GSON.fromJson(new InputStreamReader(in), OBJSON.class);
        model.setNormals();
        return model;
    }

    private void setNormals() {
        for (Face face : this.faces) {
            double[][] p = face.vertices;
            int[] t = face.triangles[0];
            face.normal = Vector3.unit(Vector3.sub(p[t[1]], p[t[0]]).cross(Vector3.sub(p[t[2]], p[t[0]])));
        }
    }

    public static class Face {
        public int texture;
        double[][] vertices;
        int[][] triangles;
        Vector3 normal;
    }

}
