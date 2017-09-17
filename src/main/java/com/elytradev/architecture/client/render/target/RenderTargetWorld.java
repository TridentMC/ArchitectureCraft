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

import com.elytradev.architecture.common.ArchitectureMod;
import com.elytradev.architecture.common.helpers.Vector3;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import static com.elytradev.architecture.common.utils.MiscUtils.ifloor;
import static com.elytradev.architecture.common.utils.MiscUtils.iround;
import static java.lang.Math.floor;

public class RenderTargetWorld extends RenderTargetBase {

    protected IBlockAccess world;
    protected BlockPos blockPos;
    protected IBlockState blockState;
    protected Block block;
    protected float cmr = 1, cmg = 1, cmb = 1;
    protected boolean ao;
    protected boolean axisAlignedNormal;
    protected boolean renderingOccurred;
    protected float vr, vg, vb, va; // Colour to be applied to next vertex
    protected int vlm1, vlm2; // Light map values to be applied to next vertex
    private BufferBuilder tess;

    public RenderTargetWorld(IBlockAccess world, BlockPos pos, BufferBuilder tess, TextureAtlasSprite overrideIcon) {
        super(pos.getX(), pos.getY(), pos.getZ(), overrideIcon);
        //ArchitectureMod.LOG.info("BaseWorldRenderTarget(%s)\n", pos);
        this.world = world;
        this.blockPos = pos;
        this.blockState = world.getBlockState(pos);
        this.block = blockState.getBlock();
        this.setTess(tess);
        ao = Minecraft.isAmbientOcclusionEnabled() && block.getLightValue(blockState) == 0;
        expandTrianglesToQuads = true;
    }


    // ---------------------------- IRenderTarget ----------------------------

    @Override
    public void setNormal(Vector3 n) {
//      ArchitectureMod.LOG.info("BaseWorldRenderer.setNormal: %s (%.3f, %.3f, %.3f)\n",
//          vertexCount, n.x, n.y, n.z);
        super.setNormal(n);
        axisAlignedNormal = n.dot(face) >= 0.99;
    }

    @Override
    protected void rawAddVertex(Vector3 p, double u, double v) {
        lightVertex(p);
        //ArchitectureMod.LOG.info("BaseWorldRenderer.rawAddVertex: %s (%.3f, %.3f, %.3f) rgba (%.3f, %.3f, %.3f, %.3f) uv (%.5f, %.5f) lm (%s, %s)\n",
        //    vertexCount, p.x, p.y, p.z, vr, vg, vb, va, u, v, vlm1, vlm2); // tess.getCurrentOffset());
        getWorldRenderer().pos(p.x, p.y, p.z);
        getWorldRenderer().color(vr, vg, vb, va);
        getWorldRenderer().tex(u, v);
        getWorldRenderer().lightmap(vlm1, vlm2);
        getWorldRenderer().endVertex();
        renderingOccurred = true;
//      if (textureOverride)
//          tess.dumpLastVertex();
    }

    //-----------------------------------------------------------------------------------------

    protected void lightVertex(Vector3 p) {
        // TODO: Colour multiplier
        if (ao)
            aoLightVertex(p);
        else
            brLightVertex(p);
    }

    protected void aoLightVertex(Vector3 v) {
        //ArchitectureMod.LOG.info("BaseWorldRenderer.aoLightVertex: %s normal %s\n", v, normal);
        Vector3 n = normal;
        double brSum1 = 0, brSum2 = 0, lvSum = 0, wt = 0;
        // Sample a unit cube offset half a block in the direction of the normal
        double vx = v.x + 0.5 * n.x;
        double vy = v.y + 0.5 * n.y;
        double vz = v.z + 0.5 * n.z;
        // Examine 8 neighbouring blocks
        for (int dx = -1; dx <= 1; dx += 2)
            for (int dy = -1; dy <= 1; dy += 2)
                for (int dz = -1; dz <= 1; dz += 2) {
                    int X = ifloor(vx + 0.5 * dx);
                    int Y = ifloor(vy + 0.5 * dy);
                    int Z = ifloor(vz + 0.5 * dz);
                    //ArchitectureMod.LOG.info("Examining (%s, %s, %s) delta (%s, %s, %s)\n", X, Y, Z, dx, dy, dz);
                    BlockPos pos = new BlockPos(X, Y, Z);
                    //ArchitectureMod.LOG.info("wnx = %.3f wny = %.3f wnz = %.3f\n", wnx, wny, wnz);
                    // Calculate overlap of sampled block with sampling cube
                    double wox = (dx < 0) ? (X + 1) - (vx - 0.5) : (vx + 0.5) - X;
                    double woy = (dy < 0) ? (Y + 1) - (vy - 0.5) : (vy + 0.5) - Y;
                    double woz = (dz < 0) ? (Z + 1) - (vz - 0.5) : (vz + 0.5) - Z;
                    //ArchitectureMod.LOG.info("wox = %.3f woy = %.3f woz = %.3f\n", wox, woy, woz);
                    // Take weighted sample of brightness and light value
                    double w = wox * woy * woz;
                    if (w > 0) {
                        int br;
                        try {
                            br = block.getPackedLightmapCoords(blockState, world, pos);
                        } catch (RuntimeException e) {
                            ArchitectureMod.LOG.info("BaseWorldRenderTarget.aoLightVertex: getMixedBrightnessForBlock(%s) with weight %s for block at %s: %s\n",
                                    pos, w, blockPos, e);
                            throw e;
                        }
                        float lv;
                        if (!pos.equals(blockPos)) {
                            IBlockState state = world.getBlockState(pos);
                            lv = state.getBlock().getAmbientOcclusionLightValue(state);
                        } else
                            lv = 1.0f;
                        //ArchitectureMod.LOG.info("BaseWorldRenderTarget.aoLightVertex: (%s,%s,%s) br = 0x%08x lv = %.3f w = %.3f\n", X, Y, Z, br, lv, w);
                        if (br != 0) {
                            double br1 = ((br >> 16) & 0xff) / 240.0;
                            double br2 = (br & 0xff) / 240.0;
                            //ArchitectureMod.LOG.info("br1 = %.3f br2 = %.3f\n", br1, br2);
                            brSum1 += w * br1;
                            brSum2 += w * br2;
                            wt += w;
                        }
                        lvSum += w * lv;
                    }
                }
        //ArchitectureMod.LOG.info("brSum1 = %.3f brSum2 = %.3f lvSum = %.3f\n", brSum1, brSum2, lvSum);
        //ArchitectureMod.LOG.info("wt = %.3f\n", wt);
        int brv;
        if (wt > 0)
            brv = (iround(brSum1 / wt * 0xf0) << 16) | iround(brSum2 / wt * 0xf0);
        else
            brv = block.getPackedLightmapCoords(blockState, world, blockPos);
        float lvv = (float) lvSum;
        //ArchitectureMod.LOG.info("BaseWorldRenderTarget.aoLightVertex: brv = 0x%08x lvv = %.3f shade = %.3f\n", brv, lvv, shade);
        setLight(shade * lvv, brv);
    }

    protected void brLightVertex(Vector3 p) {
        //ArchitectureMod.LOG.info("BaseWorldRenderTarget.brLightVertex: %s\n", p);
        Vector3 n = normal;
        BlockPos pos;
        if (axisAlignedNormal)
            pos = new BlockPos(
                    (int) floor(p.x + 0.01 * n.x),
                    (int) floor(p.y + 0.01 * n.y),
                    (int) floor(p.z + 0.01 * n.z));
        else
            pos = blockPos;
        int br = block.getPackedLightmapCoords(blockState, world, pos);
        setLight(shade, br);
    }

    protected void setLight(float shadow, int br) {
        //ArchitectureMod.LOG.info("BaseWorldRenderTarget.setLight: shadow %.3f br %08x cmr (%.3f, %.3f, %.3f) rgba (%.3f, %.3f, %.3f, %.3f)\n",
        //    shadow, br, cmr, cmg, cmb, r(), g(), b(), a());
        vr = shadow * cmr * r();
        vg = shadow * cmg * g();
        vb = shadow * cmb * b();
        va = a();
        vlm1 = br >> 16;
        vlm2 = br & 0xffff;
    }

    public boolean end() {
        super.finish();
        return renderingOccurred;
    }


    public BufferBuilder getWorldRenderer() {
        return tess;
    }

    public void setTess(BufferBuilder tess) {
        this.tess = tess;
    }
}
