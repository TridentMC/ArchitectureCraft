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

package com.tridevmc.architecture.client.render.target;

import com.tridevmc.architecture.client.render.texture.ITexture;
import com.tridevmc.architecture.client.render.texture.TextureBase;
import com.tridevmc.architecture.common.helpers.Vector3;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;

public abstract class RenderTargetBase {

    // Position of block in rendering coordinates (may be different from world coordinates)
    protected double blockX, blockY, blockZ;

    protected int verticesPerFace;
    protected int vertexCount;
    protected ITexture texture;
    protected Vector3 normal;
    protected Direction face;
    protected float red = 1, green = 1, blue = 1, alpha = 1;
    protected float shade;
    protected boolean expandTrianglesToQuads;
    protected boolean textureOverride;

    public RenderTargetBase(double x, double y, double z, TextureAtlasSprite overrideIcon) {
        this.blockX = x;
        this.blockY = y;
        this.blockZ = z;
        if (overrideIcon != null) {
            this.texture = TextureBase.fromSprite(overrideIcon);
            this.textureOverride = true;
        }
    }

    // ---------------------------- IRenderTarget ----------------------------

    public boolean isRenderingBreakEffects() {
        return this.textureOverride;
    }

    public void beginTriangle() {
        this.setMode(3);
    }

    public void beginQuad() {
        this.setMode(4);
    }

    protected void setMode(int mode) {
        if (this.vertexCount != 0)
            throw new IllegalStateException("Changing mode in mid-face");
        this.verticesPerFace = mode;
    }

    public void setTexture(ITexture texture) {
        if (!this.textureOverride) {
            if (texture == null)
                throw new IllegalArgumentException("Setting null texture");
            this.texture = texture;
        }
    }

    public void setColor(int color) {
        this.setColor((color >> 16 & 255) / 255F, (color >> 8 & 255) / 255F, (color & 255) / 255F);
    }

    public void setColor(float r, float g, float b) {
        this.setColor(r, g, b, 1F);
    }

    public void setColor(float r, float g, float b, float a) {
        this.red = r;
        this.green = g;
        this.blue = b;
        this.alpha = a;
    }

    public void setNormal(Vector3 n) {
        this.normal = n;
        this.face = n.facing();
        this.shade = (float) (0.6 * n.x * n.x + 0.8 * n.z * n.z + (n.y > 0 ? 1 : 0.5) * n.y * n.y);
    }

    public void addVertex(Vector3 p, double u, double v) {
        if (this.texture.isProjected())
            this.addProjectedVertex(p, this.face);
        else
            this.addUVVertex(p, u, v);
    }

    public void addUVVertex(Vector3 p, double u, double v) {
        double iu, iv;
        if (this.verticesPerFace == 0)
            throw new IllegalStateException("No face active");
        if (this.vertexCount >= this.verticesPerFace)
            throw new IllegalStateException("Too many vertices in face");
        if (this.normal == null)
            throw new IllegalStateException("No normal");
        if (this.texture == null)
            throw new IllegalStateException("No texture");
        iu = this.texture.interpolateU(u);
        iv = this.texture.interpolateV(v);
        this.rawAddVertex(p, iu, iv);
        if (++this.vertexCount == 3 && this.expandTrianglesToQuads && this.verticesPerFace == 3) {
            this.rawAddVertex(p, iu, iv);
        }
    }

    public void endFace() {
        if (this.vertexCount < this.verticesPerFace) {
            throw new IllegalStateException("Too few vertices in face");
        }
        this.vertexCount = 0;
        this.verticesPerFace = 0;
    }

    public void finish() {
        if (this.vertexCount > 0)
            throw new IllegalStateException("Rendering ended with incomplete face");
    }

    //-----------------------------------------------------------------------------------------

    protected abstract void rawAddVertex(Vector3 p, double u, double v);

    public float r() {
        return (float) (this.red * this.texture.red());
    }

    public float g() {
        return (float) (this.green * this.texture.green());
    }

    public float b() {
        return (float) (this.blue * this.texture.blue());
    }

    public float a() {
        return this.alpha;
    }

    // Add vertex with texture coords projected from the given direction
    public void addProjectedVertex(Vector3 p, Direction face) {
        double x = p.x - this.blockX;
        double y = p.y - this.blockY;
        double z = p.z - this.blockZ;
        //System.out.printf("BaseRenderTarget.addProjectedVertex: world (%.3f, %.3f, %.3f) block (%.3f, %.3f, %.3f) %s\n",
        //  p.x, p.y, p.z, x, y, z, face);
        double u, v;
        switch (face) {
            case DOWN:
                u = x;
                v = 1 - z;
                break;
            case UP:
                u = x;
                v = z;
                break;
            case NORTH:
                u = 1 - x;
                v = 1 - y;
                break;
            case SOUTH:
                u = x;
                v = 1 - y;
                break;
            case EAST:
                u = 1 - z;
                v = 1 - y;
                break;
            case WEST:
                u = z;
                v = 1 - y;
                break;
            default:
                u = 0;
                v = 0;
        }
        this.addUVVertex(p, u, v);
    }

}
