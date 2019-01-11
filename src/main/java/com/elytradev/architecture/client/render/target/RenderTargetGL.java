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

package com.elytradev.architecture.client.render.target;

import com.elytradev.architecture.client.render.texture.ITexture;
import com.elytradev.architecture.common.ArchitectureLog;
import com.elytradev.architecture.common.helpers.Vector3;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OpenGlHelper;
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
        glMode = 0;
        emissiveMode = -1;
        texturedMode = -1;
    }

    @Override
    public void setTexture(ITexture tex) {
        if (texture != tex) {
            super.setTexture(tex);
            ResourceLocation loc = tex.location();
            if (loc != null) {
                setGLMode(0);
                if (debugGL) ArchitectureLog.info("BaseGLRenderTarget: bindTexture(%s)\n", loc);
                Minecraft.getInstance().getTextureManager().bindTexture(loc);
            }
            setTexturedMode(!tex.isSolid());
            setEmissiveMode(tex.isEmissive());
        }
    }

    protected void setEmissiveMode(boolean state) {
        int mode = state ? 1 : 0;
        if (emissiveMode != mode) {
            if (debugGL) ArchitectureLog.info("BaseGLRenderTarget: glSetEnabled(GL_LIGHTING, %s)\n", !state);
            glSetEnabled(GL_LIGHTING, !state);
            if (usingLightmap)
                setLightmapEnabled(!state);
            emissiveMode = mode;
        }
    }

    protected void setTexturedMode(boolean state) {
        int mode = state ? 1 : 0;
        if (texturedMode != mode) {
            //ArchitectureLog.info("BaseGLRenderTarget.setTexturedMode: %s\n", state);
            setGLMode(0);
            if (debugGL) ArchitectureLog.info("BaseGLRenderTarget: glSetEnabled(GL_TEXTURE_2D, %s)\n", state);
            glSetEnabled(GL_TEXTURE_2D, state);
            texturedMode = mode;
        }
    }

    protected void setLightmapEnabled(boolean state) {
        OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
        glSetEnabled(GL_TEXTURE_2D, state);
        OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
    }

    protected void glSetEnabled(int mode, boolean state) {
        if (state)
            glEnable(mode);
        else
            glDisable(mode);
    }

    @Override
    protected void rawAddVertex(Vector3 p, double u, double v) {
        setGLMode(verticesPerFace);
        //ArchitectureLog.info("BaseGLRenderTarget: glColor4f(%.2f, %.2f, %.2f, %.2f)\n",
        //  r(), g(), b(), a());
        glColor4f(r(), g(), b(), a());
        glNormal3d(normal.x, normal.y, normal.z);
        glTexCoord2d(u, v);
        if (debugGL) ArchitectureLog.info("BaseGLRenderTarget: glVertex3d%s\n", p);
        glVertex3d(p.x, p.y, p.z);
    }

    protected void setGLMode(int mode) {
        if (glMode != mode) {
            if (glMode != 0) {
                if (debugGL) ArchitectureLog.info("BaseGLRenderTarget: glEnd()\n");
                glEnd();
            }
            glMode = mode;
            switch (glMode) {
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
                    throw new IllegalStateException(String.format("Invalid glMode %s", glMode));
            }
        }
    }

    @Override
    public void finish() {
        setGLMode(0);
        setEmissiveMode(false);
        setTexturedMode(true);
        if (debugGL) ArchitectureLog.info("BaseGLRenderTarget: glPopAttrib()\n");
        glPopAttrib();
        super.finish();
    }

}
