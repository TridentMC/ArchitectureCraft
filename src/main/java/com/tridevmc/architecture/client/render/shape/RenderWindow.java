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


import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class RenderWindow {

    //protected static WindowModels frameModels, cornerModels, mullionModels;
    protected boolean renderBase, renderSecondary;
   // protected ShapeKind.Window kind;

    //public RenderWindow(TileShape te, ITexture[] textures, Trans3 t, boolean renderBase, boolean renderSecondary, int baseColourMult, int secondaryColourMult) {
    //    this.renderBase = renderBase;
    //    this.renderSecondary = renderSecondary;
    //    this.kind = (ShapeKind.Window) te.shape.kind;
//
    //}

    //protected static IRenderableModel[] models(String... names) {
    //    IRenderableModel[] result = new IRenderableModel[names.length];
    //    for (int i = 0; i < names.length; i++)
    //        result[i] = model(names[i]);
    //    return result;
    //}

    //protected static IRenderableModel[] models(int n, String name) {
    //    IRenderableModel[] result = new IRenderableModel[n];
    //    IRenderableModel m = model(name);
    //    for (int i = 0; i < n; i++)
    //        result[i] = m;
    //    return result;
    //}

    //public static void init() {
    //    frameModels = new WindowModels(
    //            null,
    //            null,
    //            models(4, "frame_side"),
    //            models(4, "frame_end0"),
    //            models(4, "frame_end1"),
    //            model("glass"),
    //            models(4, "glass_edge"));

    //    cornerModels = new WindowModels(
    //            model("corner_centre"),
    //            models("corner_centre_end0", null, "corner_centre_end2", null),
    //            models("corner_topbot", "frame_side", "corner_topbot", "frame_side"),
    //            models(4, "frame_end0"),
    //            models("corner_topbot_end1", "frame_end1", "corner_topbot_end1", "frame_end1"),
    //            model("corner_glass"),
    //            models("corner_glass_edge", "glass_edge", "corner_glass_edge", "glass_edge"));

    //    mullionModels = new WindowModels(
    //            model("mullion_centre"),
    //            models("mullion_centre_end0", null, "mullion_centre_end2", null),
    //            models("mullion_topbot", "frame_side", "mullion_topbot", "frame_side"),
    //            models(4, "frame_end0"),
    //            models(4, "frame_end1"),
    //            model("glass"),
    //            models("mullion_glass_edge", "glass_edge", "mullion_glass_edge", "glass_edge"));

    //}

    public void render() {
        //switch (this.te.shape) {
        //    case WINDOW_FRAME:
        //        this.renderWindow(frameModels);
        //        break;
        //    case WINDOW_CORNER:
        //        this.renderWindow(cornerModels);
        //        break;
        //    case WINDOW_MULLION:
        //        this.renderWindow(mullionModels);
        //        break;
        //}
    }

    //protected void renderWindow(WindowModels models) {
    //    boolean[][] frame = this.getFrameFlags();
    //    if (this.renderBase)
    //        this.renderModel(this.t, models.centre);
    //    for (int i = 0; i <= 3; i++) {
    //        int j = (i - 1) & 3;
    //        int k = (i + 1) & 3;
    //        Trans3 ts = this.t.t(this.kind.frameTrans[i]);
    //        if (this.renderBase) {
    //            if (frame[i][1])
    //                this.renderModel(ts, models.side[i]);
    //            else if (models.centreEnd != null)
    //                this.renderModel(this.t, models.centreEnd[i]);
    //            if (frame[i][1] && !frame[j][1] || frame[i][0] && frame[j][2])
    //                this.renderModel(ts, models.end0[i]);
    //            if (frame[i][1] && !frame[k][1] || frame[i][2] && frame[k][0])
    //                this.renderModel(ts, models.end1[i]);
    //        }
    //        if (this.renderSecondary && !frame[i][1] && !frame[i][3])
    //            this.renderModel(ts, models.glassEdge[i]);
    //    }
    //    if (this.renderSecondary) {
    //        this.renderModel(this.t, models.glass);
    //    }
    //}

    //protected boolean[][] getFrameFlags() {
    //    boolean[][] frame = new boolean[4][4];
    //    if (this.blockWorld == null) {
    //        for (int i = 0; i <= 3; i++)
    //            frame[i][1] = true;
    //    } else {
    //        Direction[] gdir = new Direction[4];
    //        TileShape[] neighbour = new TileShape[4];
    //        for (int i = 0; i <= 3; i++)
    //            gdir[i] = this.t.t(this.kind.frameSides[i]);
    //        for (int i = 0; i <= 3; i++) {
    //            if (this.kind.frameAlways[i])
    //                frame[i][1] = true;
    //            else {
    //                TileShape nte = this.getConnectedNeighbourGlobal(this.te, gdir[i]);
    //                if (nte == null)
    //                    frame[i][1] = true;
    //                else {
    //                    int j = (i - 1) & 3;
    //                    int k = (i + 1) & 3;
    //                    if (this.getConnectedNeighbourGlobal(nte, gdir[j]) == null)
    //                        frame[j][2] = true;
    //                    if (this.getConnectedNeighbourGlobal(nte, gdir[k]) == null)
    //                        frame[k][0] = true;
    //                    if (nte.secondaryBlockState != null)
    //                        frame[i][3] = true;
    //                }
    //            }
    //        }
    //    }
    //    //dumpFrameFlags(frame);
    //    return frame;
    //}

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


    //protected TileShape getConnectedNeighbourGlobal(TileShape te, Direction globalDir) {
    //    return this.kind.getConnectedWindowGlobal(te, globalDir);
    //}

    //protected static class WindowModels {

    //    public IRenderableModel centre;
    //    public IRenderableModel[] centreEnd;
    //    public IRenderableModel[] side;
    //    public IRenderableModel[] end0;
    //    public IRenderableModel[] end1;
    //    public IRenderableModel glass;
    //    public IRenderableModel[] glassEdge;

    //    public WindowModels(IRenderableModel centre, IRenderableModel[] centreEnd, IRenderableModel[] side,
    //                        IRenderableModel[] end0, IRenderableModel[] end1,
    //                        IRenderableModel glass, IRenderableModel[] glassEdge) {
    //        this.centre = centre;
    //        this.centreEnd = centreEnd;
    //        this.side = side;
    //        this.end0 = end0;
    //        this.end1 = end1;
    //        this.glass = glass;
    //        this.glassEdge = glassEdge;
    //    }
    //}

}
