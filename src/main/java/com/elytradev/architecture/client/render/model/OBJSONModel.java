/*
 * MIT License
 *
 * Copyright (c) 2017 Benjamin K
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.elytradev.architecture.client.render.model;

import com.elytradev.architecture.client.render.target.RenderTargetBase;
import com.elytradev.architecture.client.render.texture.ITexture;
import com.elytradev.architecture.common.helpers.Trans3;
import com.elytradev.architecture.common.helpers.Vector3;
import com.google.gson.Gson;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

/**
 * Reads and renders objson (previously smeg) files.
 */
public class OBJSONModel implements IArchitectureModel {

    private static final Gson GSON = new Gson();
    public double[] bounds;
    public Face[] faces;
    public double[][] boxes;

    public static OBJSONModel fromResource(ResourceLocation location) {
        // Can't use resource manager because this needs to work on the server
        String path = String.format("/assets/%s/%s", location.getNamespace(), location.getPath());
        InputStream in = OBJSONModel.class.getResourceAsStream(path);
        OBJSONModel model = GSON.fromJson(new InputStreamReader(in), OBJSONModel.class);
        if (in == null)
            throw new RuntimeException("Model file not found: " + path);
        model.setNormals();
        return model;
    }

    @Override
    public AxisAlignedBB getBounds() {
        return new AxisAlignedBB(this.bounds[0], this.bounds[1], this.bounds[2], this.bounds[3], this.bounds[4], this.bounds[5]);
    }

    /**
     * Add normals to all the faces.
     */
    private void setNormals() {
        for (Face face : this.faces) {
            double[][] p = face.vertices;
            int[] t = face.triangles[0];
            face.normal = Vector3.unit(Vector3.sub(p[t[1]], p[t[0]]).cross(Vector3.sub(p[t[2]], p[t[0]])));
        }
    }

    @Override
    public void addBoxesToList(Trans3 t, List list) {
        if (this.boxes != null && this.boxes.length > 0) {
            Arrays.stream(this.boxes).forEach(b -> this.addBoxToList(b, t, list));
        } else {
            this.addBoxToList(this.bounds, t, list);
        }
    }

    protected void addBoxToList(double[] b, Trans3 t, List list) {
        t.addBox(b[0], b[1], b[2], b[3], b[4], b[5], list);
    }

    @Override
    public void render(Trans3 t, RenderTargetBase target, int baseColourMult, int secondaryColourMult, ITexture... textures) {
        Vector3 p = null, n = null;
        for (Face face : this.faces) {
            ITexture tex = textures[face.texture];
            if (tex != null) {
                target.setTexture(tex);
                target.setColor(face.texture > 1 ? secondaryColourMult : baseColourMult);
                for (int[] tri : face.triangles) {
                    target.beginTriangle();
                    for (int i = 0; i < 3; i++) {
                        int j = tri[i];
                        double[] c = face.vertices[j];
                        p = t.p(c[0], c[1], c[2]);
                        n = t.v(c[3], c[4], c[5]);
                        target.setNormal(n);
                        target.addVertex(p, c[6], c[7]);
                    }
                    target.endFace();
                }
            }
        }
    }

    /**
     * Stores info about a given face.
     */
    public static class Face {
        public int texture;
        double[][] vertices;
        int[][] triangles;
        Vector3 normal;
    }

}
