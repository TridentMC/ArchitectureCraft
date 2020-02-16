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
import com.tridevmc.architecture.common.ArchitectureLog;
import com.tridevmc.architecture.common.helpers.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_RESCALE_NORMAL;

public class RenderTargetGL extends RenderTargetBase {

    public static boolean debugGL = false;

    protected boolean usingLightmap;
    protected int glMode;
    protected int emissiveMode;
    protected int texturedMode;

    public RenderTargetGL() {
        super(0, 0, 0, null);
    }

    public void start(boolean usingLightmap) {
        this.usingLightmap = usingLightmap;
        if (debugGL) ArchitectureLog.info("BaseGLRenderTarget: glPushAttrib()\n");
        glPushAttrib(GL_LIGHTING_BIT | GL_TEXTURE_BIT | GL_TRANSFORM_BIT);
        if (debugGL) ArchitectureLog.info("BaseGLRenderTarget: glEnable(GL_RESCALE_NORMAL)\n");
        glEnable(GL_RESCALE_NORMAL);
        this.glMode = 0;
        this.emissiveMode = -1;
        this.texturedMode = -1;
    }

    @Override
    public void setTexture(ITexture tex) {
        if (this.texture != tex) {
            super.setTexture(tex);
            ResourceLocation loc = tex.location();
            if (loc != null) {
                this.setGLMode(0);
                if (debugGL) ArchitectureLog.info("BaseGLRenderTarget: bindTexture(%s)\n", loc);
                Minecraft.getInstance().getTextureManager().bindTexture(loc);
            }
            this.setTexturedMode(!tex.isSolid());
            this.setEmissiveMode(tex.isEmissive());
        }
    }

    protected void setEmissiveMode(boolean state) {
        int mode = state ? 1 : 0;
        if (this.emissiveMode != mode) {
            if (debugGL) ArchitectureLog.info("BaseGLRenderTarget: glSetEnabled(GL_LIGHTING, %s)\n", !state);
            this.glSetEnabled(GL_LIGHTING, !state);
            if (this.usingLightmap)
                this.setLightmapEnabled(!state);
            this.emissiveMode = mode;
        }
    }

    protected void setTexturedMode(boolean state) {
        int mode = state ? 1 : 0;
        if (this.texturedMode != mode) {
            //ArchitectureLog.info("BaseGLRenderTarget.setTexturedMode: %s\n", state);
            this.setGLMode(0);
            if (debugGL) ArchitectureLog.info("BaseGLRenderTarget: glSetEnabled(GL_TEXTURE_2D, %s)\n", state);
            this.glSetEnabled(GL_TEXTURE_2D, state);
            this.texturedMode = mode;
        }
    }

    protected void setLightmapEnabled(boolean state) {
        //TODO: idk what these are replaced with.
        //OpenGlHelper.glActiveTexture(OpenGlHelper.lightmapTexUnit);
        this.glSetEnabled(GL_TEXTURE_2D, state);
        //OpenGlHelper.glActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    protected void glSetEnabled(int mode, boolean state) {
        if (state)
            glEnable(mode);
        else
            glDisable(mode);
    }

    @Override
    protected void rawAddVertex(Vector3 p, double u, double v) {
        this.setGLMode(this.verticesPerFace);
        //ArchitectureLog.info("BaseGLRenderTarget: glColor4f(%.2f, %.2f, %.2f, %.2f)\n",
        //  r(), g(), b(), a());
        glColor4f(this.r(), this.g(), this.b(), this.a());
        glNormal3d(this.normal.x, this.normal.y, this.normal.z);
        glTexCoord2d(u, v);
        if (debugGL) ArchitectureLog.info("BaseGLRenderTarget: glVertex3d%s\n", p);
        glVertex3d(p.x, p.y, p.z);
    }

    protected void setGLMode(int mode) {
        if (this.glMode != mode) {
            if (this.glMode != 0) {
                if (debugGL) ArchitectureLog.info("BaseGLRenderTarget: glEnd()\n");
                glEnd();
            }
            this.glMode = mode;
            switch (this.glMode) {
                case 0:
                    break;
                case 3:
                    if (debugGL) ArchitectureLog.info("BaseGLRenderTarget: glBegin(GL_TRIANGLES)\n");
                    glBegin(GL_TRIANGLES);
                    break;
                case 4:
                    if (debugGL) ArchitectureLog.info("BaseGLRenderTarget: glBegin(GL_QUADS)\n");
                    glBegin(GL_QUADS);
                    break;
                default:
                    throw new IllegalStateException(String.format("Invalid glMode %s", this.glMode));
            }
        }
    }

    @Override
    public void finish() {
        this.setGLMode(0);
        this.setEmissiveMode(false);
        this.setTexturedMode(true);
        if (debugGL) ArchitectureLog.info("BaseGLRenderTarget: glPopAttrib()\n");
        glPopAttrib();
        super.finish();
    }

}
