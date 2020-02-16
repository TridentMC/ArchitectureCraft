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

package com.tridevmc.architecture.client.render.shape;

import com.tridevmc.architecture.client.proxy.ClientProxy;
import com.tridevmc.architecture.client.render.model.IArchitectureModel;
import com.tridevmc.architecture.client.render.target.RenderTargetBase;
import com.tridevmc.architecture.client.render.texture.ITexture;
import com.tridevmc.architecture.common.ArchitectureLog;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.shape.ShapeKind;
import com.tridevmc.architecture.common.tile.TileShape;
import net.minecraft.util.Direction;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderWindow extends RenderShape {

    protected static WindowModels frameModels, cornerModels, mullionModels;
    protected boolean renderBase, renderSecondary;
    protected ShapeKind.Window kind;

    public RenderWindow(TileShape te, ITexture[] textures, Trans3 t, RenderTargetBase target,
                        boolean renderBase, boolean renderSecondary, int baseColourMult, int secondaryColourMult) {
        super(te, textures, t, target);
        this.renderBase = renderBase;
        this.renderSecondary = renderSecondary;
        this.kind = (ShapeKind.Window) te.shape.kind;
        this.setBaseColourMult(baseColourMult);
        this.setSecondaryColourMult(secondaryColourMult);
    }

    protected static IArchitectureModel model(String name) {
        if (name != null)
            return ClientProxy.RENDERING_MANAGER.getModel("shape/window_" + name + ".objson");
        else
            return null;
    }

    protected static IArchitectureModel[] models(String... names) {
        IArchitectureModel[] result = new IArchitectureModel[names.length];
        for (int i = 0; i < names.length; i++)
            result[i] = model(names[i]);
        return result;
    }

    protected static IArchitectureModel[] models(int n, String name) {
        IArchitectureModel[] result = new IArchitectureModel[n];
        IArchitectureModel m = model(name);
        for (int i = 0; i < n; i++)
            result[i] = m;
        return result;
    }

    public static void init() {
        frameModels = new WindowModels(
                null,
                null,
                models(4, "frame_side"),
                models(4, "frame_end0"),
                models(4, "frame_end1"),
                model("glass"),
                models(4, "glass_edge"));

        cornerModels = new WindowModels(
                model("corner_centre"),
                models("corner_centre_end0", null, "corner_centre_end2", null),
                models("corner_topbot", "frame_side", "corner_topbot", "frame_side"),
                models(4, "frame_end0"),
                models("corner_topbot_end1", "frame_end1", "corner_topbot_end1", "frame_end1"),
                model("corner_glass"),
                models("corner_glass_edge", "glass_edge", "corner_glass_edge", "glass_edge"));

        mullionModels = new WindowModels(
                model("mullion_centre"),
                models("mullion_centre_end0", null, "mullion_centre_end2", null),
                models("mullion_topbot", "frame_side", "mullion_topbot", "frame_side"),
                models(4, "frame_end0"),
                models(4, "frame_end1"),
                model("glass"),
                models("mullion_glass_edge", "glass_edge", "mullion_glass_edge", "glass_edge"));

    }

    @Override
    public void render() {
        switch (te.shape) {
            case WINDOW_FRAME:
                renderWindow(frameModels);
                break;
            case WINDOW_CORNER:
                renderWindow(cornerModels);
                break;
            case WINDOW_MULLION:
                renderWindow(mullionModels);
                break;
        }
    }

    protected void renderWindow(WindowModels models) {
        boolean frame[][] = getFrameFlags();
        if (renderBase)
            renderModel(t, models.centre);
        for (int i = 0; i <= 3; i++) {
            int j = (i - 1) & 3;
            int k = (i + 1) & 3;
            Trans3 ts = t.t(kind.frameTrans[i]);
            if (renderBase) {
                if (frame[i][1])
                    renderModel(ts, models.side[i]);
                else if (models.centreEnd != null)
                    renderModel(t, models.centreEnd[i]);
                if (frame[i][1] && !frame[j][1] || frame[i][0] && frame[j][2])
                    renderModel(ts, models.end0[i]);
                if (frame[i][1] && !frame[k][1] || frame[i][2] && frame[k][0])
                    renderModel(ts, models.end1[i]);
            }
            if (renderSecondary && !frame[i][1] && !frame[i][3])
                renderModel(ts, models.glassEdge[i]);
        }
        if (renderSecondary) {
            renderModel(t, models.glass);
        }
    }

    protected void renderModel(Trans3 t, IArchitectureModel model) {
        if (model != null)
            model.render(t, target, getBaseColourMult(), getSecondaryColourMult(), textures);
    }

    protected boolean[][] getFrameFlags() {
        boolean[][] frame = new boolean[4][4];
        if (blockWorld == null) {
            for (int i = 0; i <= 3; i++)
                frame[i][1] = true;
        } else {
            Direction[] gdir = new Direction[4];
            TileShape neighbour[] = new TileShape[4];
            for (int i = 0; i <= 3; i++)
                gdir[i] = t.t(kind.frameSides[i]);
            for (int i = 0; i <= 3; i++) {
                if (kind.frameAlways[i])
                    frame[i][1] = true;
                else {
                    TileShape nte = getConnectedNeighbourGlobal(te, gdir[i]);
                    if (nte == null)
                        frame[i][1] = true;
                    else {
                        int j = (i - 1) & 3;
                        int k = (i + 1) & 3;
                        if (getConnectedNeighbourGlobal(nte, gdir[j]) == null)
                            frame[j][2] = true;
                        if (getConnectedNeighbourGlobal(nte, gdir[k]) == null)
                            frame[k][0] = true;
                        if (nte.secondaryBlockState != null)
                            frame[i][3] = true;
                    }
                }
            }
        }
        //dumpFrameFlags(frame);
        return frame;
    }

    //
    //  Layout of frame presence flags from perspective of side i.
    //
    //           |           |
    //           |           |
    //       [i-1][0]    [i+1][2]
    //           |           |
    //           |           |
    // ----------+-----------+----------
    //           |           |
    //       [i-1][1]    [i+1][1]
    //           |           |
    //           |           |
    //   [i][0]  |   [i][1]  |  [i][2]
    // ----------+===========+----------
    //           |     i     |
    //           |           |
    //       [i-1][2]    [i+1][0]
    //           |           |
    //           |           |
    //
    //  frame[i][3] == glass in neighbour i
    //

    protected void dumpFrameFlags(boolean[][] frame) {
        if (te != null && te.secondaryBlockState != null) {
            ArchitectureLog.info("RenderWindow.getFrameFlags:\n");
            for (int i = 0; i <= 3; i++)
                ArchitectureLog.info("Side %s: %s %s %s\n", i, frame[i][0], frame[i][1], frame[i][2]);
        }
    }

    protected TileShape getConnectedNeighbourGlobal(TileShape te, Direction globalDir) {
        return kind.getConnectedWindowGlobal(te, globalDir);
    }

    protected void debug(String fmt, Object... args) {
        if (blockWorld != null && te.secondaryBlockState != null)
            ArchitectureLog.info(fmt, args);
    }

    protected static class WindowModels {

        public IArchitectureModel centre, centreEnd[], side[], end0[], end1[], glass, glassEdge[];

        public WindowModels(IArchitectureModel centre, IArchitectureModel[] centreEnd, IArchitectureModel side[],
                            IArchitectureModel end0[], IArchitectureModel end1[],
                            IArchitectureModel glass, IArchitectureModel glassEdge[]) {
            this.centre = centre;
            this.centreEnd = centreEnd;
            this.side = side;
            this.end0 = end0;
            this.end1 = end1;
            this.glass = glass;
            this.glassEdge = glassEdge;
        }
    }

}
