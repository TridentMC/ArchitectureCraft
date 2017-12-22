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

package com.elytradev.architecture.client.render.shape;

import com.elytradev.architecture.client.render.target.RenderTargetBase;
import com.elytradev.architecture.client.render.texture.ITexture;
import com.elytradev.architecture.common.helpers.Trans3;
import com.elytradev.architecture.common.helpers.Vector3;
import com.elytradev.architecture.common.shape.Shape;
import com.elytradev.architecture.common.tile.TileShape;
import net.minecraft.util.EnumFacing;

import java.util.Arrays;

public class RenderRoof extends RenderShape {

    protected final static Shape ridgeShapes[] = {
            Shape.ROOF_RIDGE, Shape.ROOF_SMART_RIDGE};

    protected final static Shape ridgeOrSlopeShapes[] = {
            Shape.ROOF_RIDGE, Shape.ROOF_SMART_RIDGE,
            Shape.ROOF_TILE, Shape.ROOF_OUTER_CORNER, Shape.ROOF_INNER_CORNER};

    protected final static Shape valleyShapes[] = {
            Shape.ROOF_VALLEY, Shape.ROOF_SMART_VALLEY};

    protected final static Shape valleyOrSlopeShapes[] = {
            Shape.ROOF_VALLEY, Shape.ROOF_SMART_VALLEY,
            Shape.ROOF_TILE, Shape.ROOF_INNER_CORNER};

    protected EnumFacing face;
    protected boolean outerFace;
    protected boolean renderBase, renderSecondary;

    public RenderRoof(TileShape te, ITexture[] textures, Trans3 t, RenderTargetBase target,
                      boolean renderBase, boolean renderSecondary, int baseColourMult, int secondaryColourMult) {
        super(te, textures, t, target);
        this.renderBase = renderBase;
        this.renderSecondary = renderSecondary;
        this.setBaseColourMult(baseColourMult);
        this.setSecondaryColourMult(secondaryColourMult);
    }

    @Override
    public void render() {
        switch (te.shape) {
            case ROOF_TILE:
                renderSlope();
                break;
            case ROOF_OUTER_CORNER:
                renderOuterCorner();
                break;
            case ROOF_INNER_CORNER:
                renderInnerCorner();
                break;
            case ROOF_RIDGE:
                renderRidge();
                break;
            case ROOF_SMART_RIDGE:
                renderSmartRidge();
                break;
            case ROOF_VALLEY:
                renderValley();
                break;
            case ROOF_SMART_VALLEY:
                StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
                boolean handleItemState = Arrays.stream(stackTrace).anyMatch(stackTraceElement -> stackTraceElement.getMethodName().startsWith("handleItemState"));
                if (!handleItemState)
                    renderSmartValley();
                else
                    renderSmartValley();
                break;
        }
    }

    //-------------------------------------------------------------------------------------

    protected void renderSlope() {
        boolean valley = valleyAt(0, 0, 1);
        if (renderSecondary) {
            // Sloping face
            beginNegZSlope();
            if (valley) {
                beginTriangle();
                vertex(1, 1, 1, 0, 0);
                vertex(1, 0, 0, 0, 1);
                vertex(0.5, 0.5, 0.5, 0.5, 0.5);
                newTriangle();
                vertex(1, 0, 0, 0, 1);
                vertex(0, 0, 0, 1, 1);
                vertex(0.5, 0.5, 0.5, 0.5, 0.5);
                newTriangle();
                vertex(0, 0, 0, 1, 1);
                vertex(0, 1, 1, 1, 0);
                vertex(0.5, 0.5, 0.5, 0.5, 0.5);
                endFace();
                connectValleyBack();
            } else {
                beginQuad();
                vertex(1, 1, 1, 0, 0);
                vertex(1, 0, 0, 0, 1);
                vertex(0, 0, 0, 1, 1);
                vertex(0, 1, 1, 1, 0);
                endFace();
            }
        }
        // Other faces
        if (renderBase) {
            leftTriangle();
            rightTriangle();
            bottomQuad();
            if (!valley)
                backQuad();
        }
        if (renderSecondary)
            if (ridgeAt(0, 0, -1))
                connectRidgeFront();
    }

    protected void renderOuterCorner() {
        if (renderSecondary) {
            // Front slope
            beginNegZSlope();
            beginTriangle();
            vertex(0, 1, 1, 1, 0);
            vertex(1, 0, 0, 0, 1);
            vertex(0, 0, 0, 1, 1);
            endFace();
            // Left slope
            beginPosXSlope();
            beginTriangle();
            vertex(0, 1, 1, 0, 0);
            vertex(1, 0, 1, 0, 1);
            vertex(1, 0, 0, 1, 1);
            endFace();
        }
        if (renderBase) {
            // Back
            beginPosZFace();
            beginTriangle();
            vertex(0, 1, 1, 0, 0);
            vertex(0, 0, 1, 0, 1);
            vertex(1, 0, 1, 1, 1);
            endFace();
            // Other faces
            rightTriangle();
            bottomQuad();
        }
        if (renderSecondary) {
            if (ridgeAt(0, 0, -1))
                connectRidgeFront();
            if (ridgeAt(1, 0, 0))
                connectRidgeLeft();
        }
    }

    protected void renderInnerCorner() {
        if (renderSecondary) {
            // Left slope
            beginPosXSlope();
            beginTriangle();
            vertex(0, 1, 0, 1, 0);
            vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            vertex(1, 0, 0, 1, 1);
            endFace();
            // Front slope
            beginNegZSlope();
            beginTriangle();
            vertex(1, 1, 1, 0, 0);
            vertex(1, 0, 0, 0, 1);
            vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            endFace();
        }
        if (renderBase) {
            // Front triangle
            beginNegZFace();
            beginTriangle();
            vertex(0, 1, 0, 1, 0);
            vertex(1, 0, 0, 0, 1);
            vertex(0, 0, 0, 1, 1);
            endFace();
            // Other faces
            leftTriangle();
            bottomQuad();
        }
        if (valleyAt(0, 0, 1))
            connectValleyBack();
        else
            terminateValleyBack();
        if (valleyAt(-1, 0, 0))
            connectValleyRight();
        else
            terminateValleyRight();
    }

    protected void renderRidge() {
        if (renderSecondary) {
            // Front slope
            beginNegZSlope();
            beginQuad();
            vertex(1, 0.5, 0.5, 0, 0.5);
            vertex(1, 0, 0, 0, 1);
            vertex(0, 0, 0, 1, 1);
            vertex(0, 0.5, 0.5, 1, 0.5);
            endFace();
            // Other slops
            ridgeBackSlope();
            ridgeFront(false);
            ridgeBack(false);
        }
        if (renderBase) {
            ridgeLeftFace();
            ridgeRightFace();
            bottomQuad();
        }
    }

    protected void renderSmartRidge() {
        if (renderSecondary) {
            ridgeLeft();
            ridgeRight();
            ridgeBack(true);
            ridgeFront(true);
        }
        if (renderBase)
            bottomQuad();
    }

    protected void renderValley() {
        connectValleyLeft();
        connectValleyRight();
        smartValleyFront();
        smartValleyBack();
        if (renderBase)
            bottomQuad();
    }

    protected void renderSmartValley() {
        smartValleyLeft();
        smartValleyRight();
        smartValleyFront();
        smartValleyBack();
        if (renderBase)
            bottomQuad();
    }

    //-------------------------------------------------------------------------------------

    protected void smartValleyLeft() {
        if (valleyOrSlopeAt(1, 0, 0))
            connectValleyLeft();
        else
            terminateValleyLeft();
    }

    protected void terminateValleyLeft() {
        if (renderSecondary) {
            beginNegXSlope();
            beginTriangle();
            vertex(1, 1, 0, 0, 0);
            vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            vertex(1, 1, 1, 1, 0);
            endFace();
        }
        if (renderBase)
            leftQuad();
    }

    protected void smartValleyRight() {
        if (valleyOrSlopeAt(-1, 0, 0))
            connectValleyRight();
        else
            terminateValleyRight();
    }

    protected void terminateValleyRight() {
        if (renderSecondary) {
            beginPosXSlope();
            beginTriangle();
            vertex(0, 1, 1, 0, 0);
            vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            vertex(0, 1, 0, 1, 0);
            endFace();
        }
        if (renderBase)
            rightQuad();
    }

    protected void smartValleyFront() {
        if (valleyOrSlopeAt(0, 0, -1))
            connectValleyFront();
        else
            terminateValleyFront();
    }

    protected void terminateValleyFront() {
        if (renderSecondary) {
            beginPosZSlope();
            beginTriangle();
            vertex(0, 1, 0, 0, 0);
            vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            vertex(1, 1, 0, 1, 0);
            endFace();
        }
        if (renderBase)
            frontQuad();
    }

    protected void smartValleyBack() {
        if (valleyOrSlopeAt(0, 0, 1))
            connectValleyBack();
        else
            terminateValleyBack();
    }

    protected void terminateValleyBack() {
        if (renderSecondary) {
            beginNegZSlope();
            beginTriangle();
            vertex(1, 1, 1, 0, 0);
            vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            vertex(0, 1, 1, 1, 0);
            endFace();
        }
        if (renderBase)
            backQuad();
    }

    //-------------------------------------------------------------------------------------

    protected void leftQuad() {
        beginPosXFace();
        beginQuad();
        vertex(1, 1, 1, 0, 0);
        vertex(1, 0, 1, 0, 1);
        vertex(1, 0, 0, 1, 1);
        vertex(1, 1, 0, 1, 0);
        endFace();
    }

    protected void rightQuad() {
        beginNegXFace();
        beginQuad();
        vertex(0, 1, 0, 0, 0);
        vertex(0, 0, 0, 0, 1);
        vertex(0, 0, 1, 1, 1);
        vertex(0, 1, 1, 1, 0);
        endFace();
    }

    protected void frontQuad() {
        beginNegZFace();
        beginQuad();
        vertex(1, 1, 0, 0, 0);
        vertex(1, 0, 0, 0, 1);
        vertex(0, 0, 0, 1, 1);
        vertex(0, 1, 0, 1, 0);
        endFace();
    }

    protected void backQuad() {
//		System.out.printf("ShapeRenderer.backQuad\n");
        beginPosZFace();
        beginQuad();
        vertex(0, 1, 1, 0, 0);
        vertex(0, 0, 1, 0, 1);
        vertex(1, 0, 1, 1, 1);
        vertex(1, 1, 1, 1, 0);
        endFace();
    }

    protected void bottomQuad() {
        beginBottomFace();
        beginQuad();
        vertex(0, 0, 1, 0, 0);
        vertex(0, 0, 0, 0, 1);
        vertex(1, 0, 0, 1, 1);
        vertex(1, 0, 1, 1, 0);
        endFace();
    }

    protected void leftTriangle() {
        beginPosXFace();
        beginTriangle();
        vertex(1, 1, 1, 0, 0);
        vertex(1, 0, 1, 0, 1);
        vertex(1, 0, 0, 1, 1);
        //vertex(1, 1, 1,   0, 0);
        endFace();
    }

    protected void rightTriangle() {
        beginNegXFace();
        beginTriangle();
        vertex(0, 1, 1, 1, 0);
        vertex(0, 0, 0, 0, 1);
        vertex(0, 0, 1, 1, 1);
        endFace();
    }

    protected void ridgeLeftFace() {
        beginPosXFace();
        beginTriangle();
        vertex(1, 0.5, 0.5, 0.5, 0.5);
        vertex(1, 0, 1, 0, 1);
        vertex(1, 0, 0, 1, 1);
        endFace();
    }

    protected void ridgeRightFace() {
        beginNegXFace();
        beginTriangle();
        vertex(0, 0.5, 0.5, 0.5, 0.5);
        vertex(0, 0, 0, 0, 1);
        vertex(0, 0, 1, 1, 1);
        endFace();
    }

    protected void ridgeBackFace() {
        beginPosZFace();
        beginTriangle();
        vertex(0.5, 0.5, 1, 0.5, 0.5);
        vertex(0, 0, 1, 0, 1);
        vertex(1, 0, 1, 1, 1);
        endFace();
    }

    protected void ridgeFrontFace() {
        beginNegZFace();
        beginTriangle();
        vertex(0.5, 0.5, 0, 0.5, 0.5);
        vertex(1, 0, 0, 0, 1);
        vertex(0, 0, 0, 1, 1);
        endFace();
    }

    protected void ridgeFrontSlope() {
        beginNegZSlope();
        beginTriangle();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(1, 0, 0, 0, 1);
        vertex(0, 0, 0, 1, 1);
        endFace();
    }

    protected void ridgeBackSlope() {
        beginPosZSlope();
        beginQuad();
        vertex(0, 0.5, 0.5, 0, 0.5);
        vertex(0, 0, 1, 0, 1);
        vertex(1, 0, 1, 1, 1);
        vertex(1, 0.5, 0.5, 1, 0.5);
        endFace();
    }

    protected void ridgeLeft() {
        if (ridgeOrSlopeAt(1, 0, 0))
            connectRidgeLeft();
        else {
            beginPosXSlope();
            beginTriangle();
            vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            vertex(1, 0, 1, 0, 1);
            vertex(1, 0, 0, 1, 1);
            endFace();
        }
    }

    protected void connectRidgeLeft() {
        beginNegZSlope();
        beginTriangle();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(1, 0.5, 0.5, 0, 0.5);
        vertex(1, 0, 0, 0, 1);
        endFace();
        beginPosZSlope();
        beginTriangle();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(1, 0, 1, 1, 1);
        vertex(1, 0.5, 0.5, 1, 0.5);
        endFace();
    }

    protected void ridgeRight() {
        if (ridgeOrSlopeAt(-1, 0, 0))
            connectRidgeRight();
        else {
            beginNegXSlope();
            beginTriangle();
            vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            vertex(0, 0, 0, 0, 1);
            vertex(0, 0, 1, 1, 1);
            endFace();
        }
    }

    protected void connectRidgeRight() {
        beginNegZSlope();
        beginTriangle();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(0, 0, 0, 1, 1);
        vertex(0, 0.5, 0.5, 1, 0.5);
        endFace();
        beginPosZSlope();
        beginTriangle();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(0, 0.5, 0.5, 0, 0.5);
        vertex(0, 0, 1, 0, 1);
        endFace();
    }

    protected void ridgeFront(boolean fill) {
        if (ridgeOrSlopeAt(0, 0, -1))
            connectRidgeFront();
        else if (fill) {
            beginNegZSlope();
            beginTriangle();
            vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            vertex(1, 0, 0, 0, 1);
            vertex(0, 0, 0, 1, 1);
            endFace();
        }
    }

    protected void connectRidgeFront() {
        beginPosXSlope();
        beginTriangle();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(1, 0, 0, 1, 1);
        vertex(0.5, 0.5, 0, 1, 0.5);
        endFace();
        beginNegXSlope();
        beginTriangle();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(0.5, 0.5, 0, 0, 0.5);
        vertex(0, 0, 0, 0, 1);
        endFace();
    }

    protected void ridgeBack(boolean fill) {
        if (ridgeOrSlopeAt(0, 0, 1))
            connectRidgeBack();
        else if (fill) {
            beginPosZSlope();
            beginTriangle();
            vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            vertex(0, 0, 1, 0, 1);
            vertex(1, 0, 1, 1, 1);
            endFace();
        }
    }

    protected void connectRidgeBack() {
        beginPosXSlope();
        beginTriangle();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(0.5, 0.5, 1, 0, 0.5);
        vertex(1, 0, 1, 0, 1);
        endFace();
        beginNegXSlope();
        beginTriangle();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(0, 0, 1, 1, 1);
        vertex(0.5, 0.5, 1, 1, 0.5);
        endFace();
    }

    protected void connectValleyLeft() {
        beginPosZSlope();
        beginTriangle();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(1, 0.5, 0.5, 1, 0.5);
        vertex(1, 1, 0, 1, 0);
        endFace();
        beginNegZSlope();
        beginTriangle();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(1, 1, 1, 0, 0);
        vertex(1, 0.5, 0.5, 0, 0.5);
        endFace();
        valleyEndLeft();
    }

    protected void connectValleyRight() {
        beginPosZSlope();
        beginTriangle();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(0, 1, 0, 0, 0);
        vertex(0, 0.5, 0.5, 0, 0.5);
        endFace();
        beginNegZSlope();
        beginTriangle();
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(0, 0.5, 0.5, 1, 0.5);
        vertex(0, 1, 1, 1, 0);
        endFace();
        valleyEndRight();
    }

    protected void connectValleyFront() {
        beginPosXSlope();
        beginTriangle();
        vertex(0, 1, 0, 1, 0);
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(0.5, 0.5, 0, 1, 0.5);
        endFace();
        beginNegXSlope();
        beginTriangle();
        vertex(1, 1, 0, 0, 0);
        vertex(0.5, 0.5, 0, 0, 0.5);
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        endFace();
        valleyEndFront();
    }

    protected void connectValleyBack() {
        beginPosXSlope();
        beginTriangle();
        vertex(0, 1, 1, 0, 0);
        vertex(0.5, 0.5, 1, 0, 0.5);
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        endFace();
        beginNegXSlope();
        beginTriangle();
        vertex(1, 1, 1, 1, 0);
        vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        vertex(0.5, 0.5, 1, 1, 0.5);
        endFace();
        valleyEndBack();
    }

    protected void valleyEndLeft() {
        beginPosXFace();
        beginTriangle();
        vertex(1, 1, 1, 0, 0);
        vertex(1, 0, 1, 0, 1);
        vertex(1, 0.5, 0.5, 0.5, 0.5);
        newTriangle();
        vertex(1, 0, 1, 0, 1);
        vertex(1, 0, 0, 1, 1);
        vertex(1, 0.5, 0.5, 0.5, 0.5);
        newTriangle();
        vertex(1, 0, 0, 1, 1);
        vertex(1, 1, 0, 1, 0);
        vertex(1, 0.5, 0.5, 0.5, 0.5);
        endFace();
    }

    protected void valleyEndRight() {
        beginNegXFace();
        beginTriangle();
        vertex(0, 0, 1, 1, 1);
        vertex(0, 1, 1, 1, 0);
        vertex(0, 0.5, 0.5, 0.5, 0.5);
        newTriangle();
        vertex(0, 0, 0, 0, 1);
        vertex(0, 0, 1, 1, 1);
        vertex(0, 0.5, 0.5, 0.5, 0.5);
        newTriangle();
        vertex(0, 1, 0, 0, 0);
        vertex(0, 0, 0, 0, 1);
        vertex(0, 0.5, 0.5, 0.5, 0.5);
        endFace();
    }

    protected void valleyEndFront() {
        beginNegZFace();
        beginTriangle();
        vertex(1, 1, 0, 0, 0);
        vertex(1, 0, 0, 0, 1);
        vertex(0.5, 0.5, 0, 0.5, 0.5);
        newTriangle();
        vertex(1, 0, 0, 0, 1);
        vertex(1, 0, 0, 1, 1);
        vertex(0.5, 0.5, 0, 0.5, 0.5);
        newTriangle();
        vertex(0, 0, 0, 1, 1);
        vertex(0, 1, 0, 1, 0);
        vertex(0.5, 0.5, 0, 0.5, 0.5);
        endFace();
    }

    protected void valleyEndBack() {
        beginPosZFace();
        beginTriangle();
        vertex(0, 1, 1, 0, 0);
        vertex(0, 0, 1, 0, 1);
        vertex(0.5, 0.5, 1, 0.5, 0.5);
        newTriangle();
        vertex(0, 0, 1, 0, 1);
        vertex(1, 0, 1, 1, 1);
        vertex(0.5, 0.5, 1, 0.5, 0.5);
        newTriangle();
        vertex(1, 0, 1, 1, 1);
        vertex(1, 1, 1, 1, 0);
        vertex(0.5, 0.5, 1, 0.5, 0.5);
        endFace();
    }

    //-------------------------------------------------------------------------------------

    protected boolean ridgeAt(int dx, int dy, int dz) {
        return hasNeighbour(dx, dy, dz, ridgeShapes);
    }

    protected boolean ridgeOrSlopeAt(int dx, int dy, int dz) {
        return hasNeighbour(dx, dy, dz, ridgeOrSlopeShapes);
    }

    protected boolean valleyAt(int dx, int dy, int dz) {
        return hasNeighbour(dx, dy, dz, valleyShapes);
    }

    protected boolean valleyOrSlopeAt(int dx, int dy, int dz) {
        return hasNeighbour(dx, dy, dz, valleyOrSlopeShapes);
    }

    protected boolean hasNeighbour(int dx, int dy, int dz, Shape[] shapes) {
        EnumFacing dir = t.v(dx, dy, dz).facing();
        TileShape nte = te.getConnectedNeighbourGlobal(dir);
        if (nte != null) {
            for (int i = 0; i < shapes.length; i++)
                if (nte.shape == shapes[i])
                    return true;
        }
        return false;
    }

    //-------------------------------------------------------------------------------------

    protected void beginTopFace() {
        beginOuterFaces(Vector3.unitY);
    }

    protected void beginBottomFace() {
        beginOuterFaces(Vector3.unitNY);
    }

    protected void beginPosXFace() {
        beginOuterFaces(Vector3.unitX);
    }

    protected void beginNegXFace() {
        beginOuterFaces(Vector3.unitNX);
    }

    protected void beginPosZFace() {
        beginOuterFaces(Vector3.unitZ);
    }

    protected void beginNegZFace() {
        beginOuterFaces(Vector3.unitNZ);
    }

    protected void beginPosXSlope() {
        beginInnerFaces(Vector3.unitPXPY);
    }

    protected void beginNegXSlope() {
        beginInnerFaces(Vector3.unitNXPY);
    }

    protected void beginPosZSlope() {
        beginInnerFaces(Vector3.unitPYPZ);
    }

    protected void beginNegZSlope() {
        beginInnerFaces(Vector3.unitPYNZ);
    }

    //-------------------------------------------------------------------------------------

    protected void beginInnerFaces(Vector3 n) {
        outerFace = false;
        normal(n);
        target.setTexture(textures[2]);
        float r = (float) (getSecondaryColourMult() >> 16 & 255) / 255.0F;
        float g = (float) (getSecondaryColourMult() >> 8 & 255) / 255.0F;
        float b = (float) (getSecondaryColourMult() & 255) / 255.0F;
        target.setColor(r, g, b, 1F);
    }

    protected void beginOuterFaces(Vector3 n) {
        outerFace = true;
        normal(n);
        target.setTexture(textures[1]);
        float r = (float) (getBaseColourMult() >> 16 & 255) / 255.0F;
        float g = (float) (getBaseColourMult() >> 8 & 255) / 255.0F;
        float b = (float) (getBaseColourMult() & 255) / 255.0F;
        target.setColor(r, g, b, 1F);
    }

    protected void beginTriangle() {
        target.beginTriangle();
    }

    protected void beginQuad() {
        target.beginQuad();
    }

    protected void newTriangle() {
        endFace();
        beginTriangle();
    }

    protected void newQuad() {
        endFace();
        beginQuad();
    }

    protected void endFace() {
        target.endFace();
    }

    protected void normal(Vector3 n) {
        Vector3 tn = t.v(n);
        face = tn.facing();
        target.setNormal(tn);
    }

    protected void vertex(double x, double y, double z, double u, double v) {
        Vector3 q = t.p(x - 0.5, y - 0.5, z - 0.5);
        target.addVertex(q, u, v);
    }

}