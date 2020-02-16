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

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.tridevmc.architecture.common.ArchitectureLog;
import com.tridevmc.architecture.common.helpers.Vector3;
import com.tridevmc.architecture.common.utils.MiscUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;

import static java.lang.Math.floor;

public class RenderTargetWorld extends RenderTargetBase {

    protected ILightReader world;
    protected BlockPos blockPos;
    protected BlockState blockState;
    protected Block block;
    protected float cmr = 1, cmg = 1, cmb = 1;
    protected boolean ao;
    protected boolean axisAlignedNormal;
    protected boolean renderingOccurred;
    protected float vr, vg, vb, va; // Colour to be applied to next vertex
    protected int vlm1, vlm2; // Light map values to be applied to next vertex
    private IVertexBuilder tess;

    public RenderTargetWorld(ILightReader world, BlockPos pos, IVertexBuilder tess, TextureAtlasSprite overrideIcon) {
        super(pos.getX(), pos.getY(), pos.getZ(), overrideIcon);
        //ArchitectureLog.info("BaseWorldRenderTarget(%s)\n", pos);
        this.world = world;
        this.blockPos = pos;
        this.blockState = world.getBlockState(pos);
        this.block = this.blockState.getBlock();
        this.setTess(tess);
        this.ao = Minecraft.isAmbientOcclusionEnabled() && this.block.getLightValue(this.blockState) == 0;
        this.expandTrianglesToQuads = true;
    }


    // ---------------------------- IRenderTarget ----------------------------

    @Override
    public void setNormal(Vector3 n) {
//      ArchitectureLog.info("BaseWorldRenderer.setNormal: %s (%.3f, %.3f, %.3f)\n",
//          vertexCount, n.x, n.y, n.z);
        super.setNormal(n);
        this.axisAlignedNormal = n.dot(this.face) >= 0.99;
    }

    @Override
    protected void rawAddVertex(Vector3 p, double u, double v) {
        this.lightVertex(p);
        //ArchitectureLog.info("BaseWorldRenderer.rawAddVertex: %s (%.3f, %.3f, %.3f) rgba (%.3f, %.3f, %.3f, %.3f) uv (%.5f, %.5f) lm (%s, %s)\n",
        //    vertexCount, p.x, p.y, p.z, vr, vg, vb, va, u, v, vlm1, vlm2); // tess.getCurrentOffset());
        this.getWorldRenderer().pos(p.x, p.y, p.z);
        this.getWorldRenderer().color(this.vr, this.vg, this.vb, this.va);
        this.getWorldRenderer().tex((float) u, (float) v);
        this.getWorldRenderer().overlay(OverlayTexture.DEFAULT_LIGHT);
        this.getWorldRenderer().lightmap(this.vlm1, this.vlm2);
        this.getWorldRenderer().normal((float) this.normal.x, (float) this.normal.y, (float) this.normal.z);
        this.getWorldRenderer().endVertex();
        this.renderingOccurred = true;
//      if (textureOverride)
//          tess.dumpLastVertex();
    }

    //-----------------------------------------------------------------------------------------

    protected void lightVertex(Vector3 p) {
        // TODO: Colour multiplier
        if (this.ao)
            this.aoLightVertex(p);
        else
            this.brLightVertex(p);
    }

    protected void aoLightVertex(Vector3 v) {
        Vector3 n = this.normal;
        double brSum1 = 0, brSum2 = 0, lvSum = 0, wt = 0;
        // Sample a unit cube offset half a block in the direction of the normal
        double vx = v.x + 0.5 * n.x;
        double vy = v.y + 0.5 * n.y;
        double vz = v.z + 0.5 * n.z;
        // Examine 8 neighbouring blocks
        for (int dx = -1; dx <= 1; dx += 2)
            for (int dy = -1; dy <= 1; dy += 2)
                for (int dz = -1; dz <= 1; dz += 2) {
                    int X = MiscUtils.ifloor(vx + 0.5 * dx);
                    int Y = MiscUtils.ifloor(vy + 0.5 * dy);
                    int Z = MiscUtils.ifloor(vz + 0.5 * dz);
                    BlockPos pos = new BlockPos(X, Y, Z);
                    // Calculate overlap of sampled block with sampling cube
                    double wox = (dx < 0) ? (X + 1) - (vx - 0.5) : (vx + 0.5) - X;
                    double woy = (dy < 0) ? (Y + 1) - (vy - 0.5) : (vy + 0.5) - Y;
                    double woz = (dz < 0) ? (Z + 1) - (vz - 0.5) : (vz + 0.5) - Z;
                    // Take weighted sample of brightness and light value
                    double w = wox * woy * woz;
                    if (w > 0) {
                        int br;
                        try {
                            br = this.block.getLightValue(this.blockState, this.world, pos);
                        } catch (RuntimeException e) {
                            ArchitectureLog.info("BaseWorldRenderTarget.aoLightVertex: getMixedBrightnessForBlock(%s) with weight %s for block at %s: %s\n",
                                    pos, w, this.blockPos, e);
                            throw e;
                        }
                        float lv;
                        if (!pos.equals(this.blockPos)) {
                            BlockState state = this.world.getBlockState(pos);
                            lv = state.getBlock().getAmbientOcclusionLightValue(state, this.world, pos);
                        } else
                            lv = 1.0f;
                        if (br != 0) {
                            double br1 = ((br >> 16) & 0xff) / 240.0;
                            double br2 = (br & 0xff) / 240.0;
                            brSum1 += w * br1;
                            brSum2 += w * br2;
                            wt += w;
                        }
                        lvSum += w * lv;
                    }
                }
        int brv;
        if (wt > 0)
            brv = (MiscUtils.iround(brSum1 / wt * 0xf0) << 16) | MiscUtils.iround(brSum2 / wt * 0xf0);
        else
            brv = this.block.getLightValue(this.blockState, this.world, this.blockPos);
        float lvv = (float) lvSum;
        this.setLight(this.shade * lvv, brv);
    }

    protected void brLightVertex(Vector3 p) {
        Vector3 n = this.normal;
        BlockPos pos;
        if (this.axisAlignedNormal)
            pos = new BlockPos(
                    (int) floor(p.x + 0.01 * n.x),
                    (int) floor(p.y + 0.01 * n.y),
                    (int) floor(p.z + 0.01 * n.z));
        else
            pos = this.blockPos;
        int br = this.block.getLightValue(this.blockState, this.world, pos);
        this.setLight(this.shade, br);
    }

    protected void setLight(float shadow, int br) {
        this.vr = shadow * this.cmr * this.r();
        this.vg = shadow * this.cmg * this.g();
        this.vb = shadow * this.cmb * this.b();
        this.va = this.a();
        this.vlm1 = br >> 16;
        this.vlm2 = br & 0xffff;
    }

    public boolean end() {
        super.finish();
        return this.renderingOccurred;
    }

    public IVertexBuilder getWorldRenderer() {
        return this.tess;
    }

    public void setTess(IVertexBuilder tess) {
        this.tess = tess;
    }
}
