package com.tridevmc.architecture.client.render.model;

import com.google.gson.Gson;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Vector3;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

public class OBJSON {

    private static final Gson GSON = new Gson();
    public double[] bounds;
    public Face[] faces;
    public double[][] boxes;

    public static OBJSON fromResource(ResourceLocation location) {
        // Can't use resource manager because this needs to work on the server
        String path = String.format("/data/%s/%s", location.getNamespace(), location.getPath());
        InputStream in = OBJSON.class.getResourceAsStream(path);
        OBJSON model = GSON.fromJson(new InputStreamReader(in), OBJSON.class);
        model.setNormals();
        return model;
    }

    public AxisAlignedBB getBounds() {
        return new AxisAlignedBB(this.bounds[0], this.bounds[1], this.bounds[2], this.bounds[3], this.bounds[4], this.bounds[5]);
    }


    public void addBoxesToList(Trans3 t, List<AxisAlignedBB> list) {
        if (this.boxes != null && this.boxes.length > 0) {
            Arrays.stream(this.boxes).forEach(b -> this.addBoxToList(b, t, list));
        } else {
            this.addBoxToList(this.bounds, t, list);
        }
    }

    protected void addBoxToList(double[] b, Trans3 t, List<AxisAlignedBB> list) {
        t.addBox(b[0], b[1], b[2], b[3], b[4], b[5], list);
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
