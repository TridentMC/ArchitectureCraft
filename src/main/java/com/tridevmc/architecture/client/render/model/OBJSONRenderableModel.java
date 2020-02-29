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

package com.tridevmc.architecture.client.render.model;

import com.google.common.collect.Lists;
import com.tridevmc.architecture.client.render.texture.ITexture;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Vector3;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import java.util.Arrays;
import java.util.List;

/**
 * Reads and renders OBJSON (previously smeg) files.
 */
public class OBJSONRenderableModel implements IRenderableModel {

    private OBJSON objson;

    public static OBJSONRenderableModel fromResource(ResourceLocation location) {
        return new OBJSONRenderableModel(OBJSON.fromResource(location));
    }

    public OBJSONRenderableModel(OBJSON objson) {
        this.objson = objson;
    }

    @Override
    public AxisAlignedBB getBounds() {
        return new AxisAlignedBB(this.objson.bounds[0], this.objson.bounds[1], this.objson.bounds[2], this.objson.bounds[3], this.objson.bounds[4], this.objson.bounds[5]);
    }

    /**
     * Add normals to all the faces.
     */
    private void setNormals() {
        for (OBJSON.Face face : this.objson.faces) {
            double[][] p = face.vertices;
            int[] t = face.triangles[0];
            face.normal = Vector3.unit(Vector3.sub(p[t[1]], p[t[0]]).cross(Vector3.sub(p[t[2]], p[t[0]])));
        }
    }

    @Override
    public void addBoxesToList(Trans3 t, List<AxisAlignedBB> list) {
        if (this.objson.boxes != null && this.objson.boxes.length > 0) {
            Arrays.stream(this.objson.boxes).forEach(b -> this.addBoxToList(b, t, list));
        } else {
            this.addBoxToList(this.objson.bounds, t, list);
        }
    }

    @Override
    public ArchitectureModelData generateModelData(Trans3 t, int baseColourMult, int secondaryColourMult, ITexture... textures) {
        ArchitectureModelData modelData = new ArchitectureModelData();
        Vector3 p = null, n = null;
        for (OBJSON.Face face : this.objson.faces) {
            ITexture tex = textures[face.texture];
            if (tex != null) {
                BakedQuadBuilder builder = new BakedQuadBuilder();
                builder.setTexture(tex.getSprite());

                target.setTexture(tex.getSprite());
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
        return null;
    }

    protected void addBoxToList(double[] b, Trans3 t, List<AxisAlignedBB> list) {
        t.addBox(b[0], b[1], b[2], b[3], b[4], b[5], list);
    }

    public List<BakedQuad> generateQuads(Trans3 t, int baseColourMult, int secondaryColourMult, ITexture... textures) {
        List<BakedQuad> quadsOut = Lists.newArrayList();
        Vector3 p = null, n = null;
        for (OBJSON.Face face : this.objson.faces) {
            ITexture tex = textures[face.texture];
            if (tex != null) {
                BakedQuadBuilder builder = new BakedQuadBuilder();
                builder.setTexture(tex.getSprite());

                target.setTexture(tex.getSprite());
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

}
