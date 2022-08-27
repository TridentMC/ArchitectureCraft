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

import com.tridevmc.architecture.client.render.texture.ITexture;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Vector3;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.block.entity.ShapeBlockEntity;
import net.minecraft.core.Direction;

public class RenderRoof {

    protected final static EnumShape[] ridgeShapes = {
            EnumShape.ROOF_RIDGE, EnumShape.ROOF_SMART_RIDGE};

    protected final static EnumShape[] ridgeOrSlopeShapes = {
            EnumShape.ROOF_RIDGE, EnumShape.ROOF_SMART_RIDGE,
            EnumShape.ROOF_TILE, EnumShape.ROOF_OUTER_CORNER, EnumShape.ROOF_INNER_CORNER};

    protected final static EnumShape[] valleyShapes = {
            EnumShape.ROOF_VALLEY, EnumShape.ROOF_SMART_VALLEY};

    protected final static EnumShape[] valleyOrSlopeShapes = {
            EnumShape.ROOF_VALLEY, EnumShape.ROOF_SMART_VALLEY,
            EnumShape.ROOF_TILE, EnumShape.ROOF_INNER_CORNER};

    protected Direction face;
    protected boolean outerFace;
    protected boolean renderBase, renderSecondary;

    public RenderRoof(ShapeBlockEntity te, ITexture[] textures, Trans3 t,
                      boolean renderBase, boolean renderSecondary, int baseColourMult, int secondaryColourMult) {
        this.renderBase = renderBase;
        this.renderSecondary = renderSecondary;
    }

    //-------------------------------------------------------------------------------------

    protected void renderSlope() {
        boolean valley = this.valleyAt(0, 0, 1);
        if (this.renderSecondary) {
            // Sloping face
            this.beginNegZSlope();
            if (valley) {
                this.beginTriangle();
                this.vertex(1, 1, 1, 0, 0);
                this.vertex(1, 0, 0, 0, 1);
                this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
                this.newTriangle();
                this.vertex(1, 0, 0, 0, 1);
                this.vertex(0, 0, 0, 1, 1);
                this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
                this.newTriangle();
                this.vertex(0, 0, 0, 1, 1);
                this.vertex(0, 1, 1, 1, 0);
                this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
                this.endFace();
                this.connectValleyBack();
            } else {
                this.beginQuad();
                this.vertex(1, 1, 1, 0, 0);
                this.vertex(1, 0, 0, 0, 1);
                this.vertex(0, 0, 0, 1, 1);
                this.vertex(0, 1, 1, 1, 0);
                this.endFace();
            }
        }
        // Other faces
        if (this.renderBase) {
            this.leftTriangle();
            this.rightTriangle();
            this.bottomQuad();
            if (!valley)
                this.backQuad();
        }
        if (this.renderSecondary)
            if (this.ridgeAt(0, 0, -1))
                this.connectRidgeFront();
    }

    protected void renderOuterCorner() {
        if (this.renderSecondary) {
            // Front slope
            this.beginNegZSlope();
            this.beginTriangle();
            this.vertex(0, 1, 1, 1, 0);
            this.vertex(1, 0, 0, 0, 1);
            this.vertex(0, 0, 0, 1, 1);
            this.endFace();
            // Left slope
            this.beginPosXSlope();
            this.beginTriangle();
            this.vertex(0, 1, 1, 0, 0);
            this.vertex(1, 0, 1, 0, 1);
            this.vertex(1, 0, 0, 1, 1);
            this.endFace();
        }
        if (this.renderBase) {
            // Back
            this.beginPosZFace();
            this.beginTriangle();
            this.vertex(0, 1, 1, 0, 0);
            this.vertex(0, 0, 1, 0, 1);
            this.vertex(1, 0, 1, 1, 1);
            this.endFace();
            // Other faces
            this.rightTriangle();
            this.bottomQuad();
        }
        if (this.renderSecondary) {
            if (this.ridgeAt(0, 0, -1))
                this.connectRidgeFront();
            if (this.ridgeAt(1, 0, 0))
                this.connectRidgeLeft();
        }
    }

    protected void renderInnerCorner() {
        if (this.renderSecondary) {
            // Left slope
            this.beginPosXSlope();
            this.beginTriangle();
            this.vertex(0, 1, 0, 1, 0);
            this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            this.vertex(1, 0, 0, 1, 1);
            this.endFace();
            // Front slope
            this.beginNegZSlope();
            this.beginTriangle();
            this.vertex(1, 1, 1, 0, 0);
            this.vertex(1, 0, 0, 0, 1);
            this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            this.endFace();
        }
        if (this.renderBase) {
            // Front triangle
            this.beginNegZFace();
            this.beginTriangle();
            this.vertex(0, 1, 0, 1, 0);
            this.vertex(1, 0, 0, 0, 1);
            this.vertex(0, 0, 0, 1, 1);
            this.endFace();
            // Other faces
            this.leftTriangle();
            this.bottomQuad();
        }
        if (this.valleyAt(0, 0, 1))
            this.connectValleyBack();
        else
            this.terminateValleyBack();
        if (this.valleyAt(-1, 0, 0))
            this.connectValleyRight();
        else
            this.terminateValleyRight();
    }

    protected void renderRidge() {
        if (this.renderSecondary) {
            // Front slope
            this.beginNegZSlope();
            this.beginQuad();
            this.vertex(1, 0.5, 0.5, 0, 0.5);
            this.vertex(1, 0, 0, 0, 1);
            this.vertex(0, 0, 0, 1, 1);
            this.vertex(0, 0.5, 0.5, 1, 0.5);
            this.endFace();
            // Other slops
            this.ridgeBackSlope();
            this.ridgeFront(false);
            this.ridgeBack(false);
        }
        if (this.renderBase) {
            this.ridgeLeftFace();
            this.ridgeRightFace();
            this.bottomQuad();
        }
    }

    protected void renderSmartRidge() {
        if (this.renderSecondary) {
            this.ridgeLeft();
            this.ridgeRight();
            this.ridgeBack(true);
            this.ridgeFront(true);
        }
        if (this.renderBase)
            this.bottomQuad();
    }

    protected void renderValley() {
        this.connectValleyLeft();
        this.connectValleyRight();
        this.smartValleyFront();
        this.smartValleyBack();
        if (this.renderBase)
            this.bottomQuad();
    }

    protected void renderSmartValley() {
        this.smartValleyLeft();
        this.smartValleyRight();
        this.smartValleyFront();
        this.smartValleyBack();
        if (this.renderBase)
            this.bottomQuad();
    }

    //-------------------------------------------------------------------------------------

    protected void smartValleyLeft() {
        if (this.valleyOrSlopeAt(1, 0, 0))
            this.connectValleyLeft();
        else
            this.terminateValleyLeft();
    }

    protected void terminateValleyLeft() {
        if (this.renderSecondary) {
            this.beginNegXSlope();
            this.beginTriangle();
            this.vertex(1, 1, 0, 0, 0);
            this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            this.vertex(1, 1, 1, 1, 0);
            this.endFace();
        }
        if (this.renderBase)
            this.leftQuad();
    }

    protected void smartValleyRight() {
        if (this.valleyOrSlopeAt(-1, 0, 0))
            this.connectValleyRight();
        else
            this.terminateValleyRight();
    }

    protected void terminateValleyRight() {
        if (this.renderSecondary) {
            this.beginPosXSlope();
            this.beginTriangle();
            this.vertex(0, 1, 1, 0, 0);
            this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            this.vertex(0, 1, 0, 1, 0);
            this.endFace();
        }
        if (this.renderBase)
            this.rightQuad();
    }

    protected void smartValleyFront() {
        if (this.valleyOrSlopeAt(0, 0, -1))
            this.connectValleyFront();
        else
            this.terminateValleyFront();
    }

    protected void terminateValleyFront() {
        if (this.renderSecondary) {
            this.beginPosZSlope();
            this.beginTriangle();
            this.vertex(0, 1, 0, 0, 0);
            this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            this.vertex(1, 1, 0, 1, 0);
            this.endFace();
        }
        if (this.renderBase)
            this.frontQuad();
    }

    protected void smartValleyBack() {
        if (this.valleyOrSlopeAt(0, 0, 1))
            this.connectValleyBack();
        else
            this.terminateValleyBack();
    }

    protected void terminateValleyBack() {
        if (this.renderSecondary) {
            this.beginNegZSlope();
            this.beginTriangle();
            this.vertex(1, 1, 1, 0, 0);
            this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            this.vertex(0, 1, 1, 1, 0);
            this.endFace();
        }
        if (this.renderBase)
            this.backQuad();
    }

    //-------------------------------------------------------------------------------------

    protected void leftQuad() {
        this.beginPosXFace();
        this.beginQuad();
        this.vertex(1, 1, 1, 0, 0);
        this.vertex(1, 0, 1, 0, 1);
        this.vertex(1, 0, 0, 1, 1);
        this.vertex(1, 1, 0, 1, 0);
        this.endFace();
    }

    protected void rightQuad() {
        this.beginNegXFace();
        this.beginQuad();
        this.vertex(0, 1, 0, 0, 0);
        this.vertex(0, 0, 0, 0, 1);
        this.vertex(0, 0, 1, 1, 1);
        this.vertex(0, 1, 1, 1, 0);
        this.endFace();
    }

    protected void frontQuad() {
        this.beginNegZFace();
        this.beginQuad();
        this.vertex(1, 1, 0, 0, 0);
        this.vertex(1, 0, 0, 0, 1);
        this.vertex(0, 0, 0, 1, 1);
        this.vertex(0, 1, 0, 1, 0);
        this.endFace();
    }

    protected void backQuad() {
//		System.out.printf("ShapeRenderer.backQuad\n");
        this.beginPosZFace();
        this.beginQuad();
        this.vertex(0, 1, 1, 0, 0);
        this.vertex(0, 0, 1, 0, 1);
        this.vertex(1, 0, 1, 1, 1);
        this.vertex(1, 1, 1, 1, 0);
        this.endFace();
    }

    protected void bottomQuad() {
        this.beginBottomFace();
        this.beginQuad();
        this.vertex(0, 0, 1, 0, 0);
        this.vertex(0, 0, 0, 0, 1);
        this.vertex(1, 0, 0, 1, 1);
        this.vertex(1, 0, 1, 1, 0);
        this.endFace();
    }

    protected void leftTriangle() {
        this.beginPosXFace();
        this.beginTriangle();
        this.vertex(1, 1, 1, 0, 0);
        this.vertex(1, 0, 1, 0, 1);
        this.vertex(1, 0, 0, 1, 1);
        //vertex(1, 1, 1,   0, 0);
        this.endFace();
    }

    protected void rightTriangle() {
        this.beginNegXFace();
        this.beginTriangle();
        this.vertex(0, 1, 1, 1, 0);
        this.vertex(0, 0, 0, 0, 1);
        this.vertex(0, 0, 1, 1, 1);
        this.endFace();
    }

    protected void ridgeLeftFace() {
        this.beginPosXFace();
        this.beginTriangle();
        this.vertex(1, 0.5, 0.5, 0.5, 0.5);
        this.vertex(1, 0, 1, 0, 1);
        this.vertex(1, 0, 0, 1, 1);
        this.endFace();
    }

    protected void ridgeRightFace() {
        this.beginNegXFace();
        this.beginTriangle();
        this.vertex(0, 0.5, 0.5, 0.5, 0.5);
        this.vertex(0, 0, 0, 0, 1);
        this.vertex(0, 0, 1, 1, 1);
        this.endFace();
    }

    protected void ridgeBackFace() {
        this.beginPosZFace();
        this.beginTriangle();
        this.vertex(0.5, 0.5, 1, 0.5, 0.5);
        this.vertex(0, 0, 1, 0, 1);
        this.vertex(1, 0, 1, 1, 1);
        this.endFace();
    }

    protected void ridgeFrontFace() {
        this.beginNegZFace();
        this.beginTriangle();
        this.vertex(0.5, 0.5, 0, 0.5, 0.5);
        this.vertex(1, 0, 0, 0, 1);
        this.vertex(0, 0, 0, 1, 1);
        this.endFace();
    }

    protected void ridgeFrontSlope() {
        this.beginNegZSlope();
        this.beginTriangle();
        this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        this.vertex(1, 0, 0, 0, 1);
        this.vertex(0, 0, 0, 1, 1);
        this.endFace();
    }

    protected void ridgeBackSlope() {
        this.beginPosZSlope();
        this.beginQuad();
        this.vertex(0, 0.5, 0.5, 0, 0.5);
        this.vertex(0, 0, 1, 0, 1);
        this.vertex(1, 0, 1, 1, 1);
        this.vertex(1, 0.5, 0.5, 1, 0.5);
        this.endFace();
    }

    protected void ridgeLeft() {
        if (this.ridgeOrSlopeAt(1, 0, 0))
            this.connectRidgeLeft();
        else {
            this.beginPosXSlope();
            this.beginTriangle();
            this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            this.vertex(1, 0, 1, 0, 1);
            this.vertex(1, 0, 0, 1, 1);
            this.endFace();
        }
    }

    protected void connectRidgeLeft() {
        this.beginNegZSlope();
        this.beginTriangle();
        this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        this.vertex(1, 0.5, 0.5, 0, 0.5);
        this.vertex(1, 0, 0, 0, 1);
        this.endFace();
        this.beginPosZSlope();
        this.beginTriangle();
        this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        this.vertex(1, 0, 1, 1, 1);
        this.vertex(1, 0.5, 0.5, 1, 0.5);
        this.endFace();
    }

    protected void ridgeRight() {
        if (this.ridgeOrSlopeAt(-1, 0, 0))
            this.connectRidgeRight();
        else {
            this.beginNegXSlope();
            this.beginTriangle();
            this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            this.vertex(0, 0, 0, 0, 1);
            this.vertex(0, 0, 1, 1, 1);
            this.endFace();
        }
    }

    protected void connectRidgeRight() {
        this.beginNegZSlope();
        this.beginTriangle();
        this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        this.vertex(0, 0, 0, 1, 1);
        this.vertex(0, 0.5, 0.5, 1, 0.5);
        this.endFace();
        this.beginPosZSlope();
        this.beginTriangle();
        this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        this.vertex(0, 0.5, 0.5, 0, 0.5);
        this.vertex(0, 0, 1, 0, 1);
        this.endFace();
    }

    protected void ridgeFront(boolean fill) {
        if (this.ridgeOrSlopeAt(0, 0, -1))
            this.connectRidgeFront();
        else if (fill) {
            this.beginNegZSlope();
            this.beginTriangle();
            this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            this.vertex(1, 0, 0, 0, 1);
            this.vertex(0, 0, 0, 1, 1);
            this.endFace();
        }
    }

    protected void connectRidgeFront() {
        this.beginPosXSlope();
        this.beginTriangle();
        this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        this.vertex(1, 0, 0, 1, 1);
        this.vertex(0.5, 0.5, 0, 1, 0.5);
        this.endFace();
        this.beginNegXSlope();
        this.beginTriangle();
        this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        this.vertex(0.5, 0.5, 0, 0, 0.5);
        this.vertex(0, 0, 0, 0, 1);
        this.endFace();
    }

    protected void ridgeBack(boolean fill) {
        if (this.ridgeOrSlopeAt(0, 0, 1))
            this.connectRidgeBack();
        else if (fill) {
            this.beginPosZSlope();
            this.beginTriangle();
            this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
            this.vertex(0, 0, 1, 0, 1);
            this.vertex(1, 0, 1, 1, 1);
            this.endFace();
        }
    }

    protected void connectRidgeBack() {
        this.beginPosXSlope();
        this.beginTriangle();
        this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        this.vertex(0.5, 0.5, 1, 0, 0.5);
        this.vertex(1, 0, 1, 0, 1);
        this.endFace();
        this.beginNegXSlope();
        this.beginTriangle();
        this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        this.vertex(0, 0, 1, 1, 1);
        this.vertex(0.5, 0.5, 1, 1, 0.5);
        this.endFace();
    }

    protected void connectValleyLeft() {
        this.beginPosZSlope();
        this.beginTriangle();
        this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        this.vertex(1, 0.5, 0.5, 1, 0.5);
        this.vertex(1, 1, 0, 1, 0);
        this.endFace();
        this.beginNegZSlope();
        this.beginTriangle();
        this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        this.vertex(1, 1, 1, 0, 0);
        this.vertex(1, 0.5, 0.5, 0, 0.5);
        this.endFace();
        this.valleyEndLeft();
    }

    protected void connectValleyRight() {
        this.beginPosZSlope();
        this.beginTriangle();
        this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        this.vertex(0, 1, 0, 0, 0);
        this.vertex(0, 0.5, 0.5, 0, 0.5);
        this.endFace();
        this.beginNegZSlope();
        this.beginTriangle();
        this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        this.vertex(0, 0.5, 0.5, 1, 0.5);
        this.vertex(0, 1, 1, 1, 0);
        this.endFace();
        this.valleyEndRight();
    }

    protected void connectValleyFront() {
        this.beginPosXSlope();
        this.beginTriangle();
        this.vertex(0, 1, 0, 1, 0);
        this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        this.vertex(0.5, 0.5, 0, 1, 0.5);
        this.endFace();
        this.beginNegXSlope();
        this.beginTriangle();
        this.vertex(1, 1, 0, 0, 0);
        this.vertex(0.5, 0.5, 0, 0, 0.5);
        this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        this.endFace();
        this.valleyEndFront();
    }

    protected void connectValleyBack() {
        this.beginPosXSlope();
        this.beginTriangle();
        this.vertex(0, 1, 1, 0, 0);
        this.vertex(0.5, 0.5, 1, 0, 0.5);
        this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        this.endFace();
        this.beginNegXSlope();
        this.beginTriangle();
        this.vertex(1, 1, 1, 1, 0);
        this.vertex(0.5, 0.5, 0.5, 0.5, 0.5);
        this.vertex(0.5, 0.5, 1, 1, 0.5);
        this.endFace();
        this.valleyEndBack();
    }

    protected void valleyEndLeft() {
        this.beginPosXFace();
        this.beginTriangle();
        this.vertex(1, 1, 1, 0, 0);
        this.vertex(1, 0, 1, 0, 1);
        this.vertex(1, 0.5, 0.5, 0.5, 0.5);
        this.newTriangle();
        this.vertex(1, 0, 1, 0, 1);
        this.vertex(1, 0, 0, 1, 1);
        this.vertex(1, 0.5, 0.5, 0.5, 0.5);
        this.newTriangle();
        this.vertex(1, 0, 0, 1, 1);
        this.vertex(1, 1, 0, 1, 0);
        this.vertex(1, 0.5, 0.5, 0.5, 0.5);
        this.endFace();
    }

    protected void valleyEndRight() {
        this.beginNegXFace();
        this.beginTriangle();
        this.vertex(0, 0, 1, 1, 1);
        this.vertex(0, 1, 1, 1, 0);
        this.vertex(0, 0.5, 0.5, 0.5, 0.5);
        this.newTriangle();
        this.vertex(0, 0, 0, 0, 1);
        this.vertex(0, 0, 1, 1, 1);
        this.vertex(0, 0.5, 0.5, 0.5, 0.5);
        this.newTriangle();
        this.vertex(0, 1, 0, 0, 0);
        this.vertex(0, 0, 0, 0, 1);
        this.vertex(0, 0.5, 0.5, 0.5, 0.5);
        this.endFace();
    }

    protected void valleyEndFront() {
        this.beginNegZFace();
        this.beginTriangle();
        this.vertex(1, 1, 0, 0, 0);
        this.vertex(1, 0, 0, 0, 1);
        this.vertex(0.5, 0.5, 0, 0.5, 0.5);
        this.newTriangle();
        this.vertex(1, 0, 0, 0, 1);
        this.vertex(1, 0, 0, 1, 1);
        this.vertex(0.5, 0.5, 0, 0.5, 0.5);
        this.newTriangle();
        this.vertex(0, 0, 0, 1, 1);
        this.vertex(0, 1, 0, 1, 0);
        this.vertex(0.5, 0.5, 0, 0.5, 0.5);
        this.endFace();
    }

    protected void valleyEndBack() {
        this.beginPosZFace();
        this.beginTriangle();
        this.vertex(0, 1, 1, 0, 0);
        this.vertex(0, 0, 1, 0, 1);
        this.vertex(0.5, 0.5, 1, 0.5, 0.5);
        this.newTriangle();
        this.vertex(0, 0, 1, 0, 1);
        this.vertex(1, 0, 1, 1, 1);
        this.vertex(0.5, 0.5, 1, 0.5, 0.5);
        this.newTriangle();
        this.vertex(1, 0, 1, 1, 1);
        this.vertex(1, 1, 1, 1, 0);
        this.vertex(0.5, 0.5, 1, 0.5, 0.5);
        this.endFace();
    }

    //-------------------------------------------------------------------------------------

    protected boolean ridgeAt(int dx, int dy, int dz) {
        return this.hasNeighbour(dx, dy, dz, ridgeShapes);
    }

    protected boolean ridgeOrSlopeAt(int dx, int dy, int dz) {
        return this.hasNeighbour(dx, dy, dz, ridgeOrSlopeShapes);
    }

    protected boolean valleyAt(int dx, int dy, int dz) {
        return this.hasNeighbour(dx, dy, dz, valleyShapes);
    }

    protected boolean valleyOrSlopeAt(int dx, int dy, int dz) {
        return this.hasNeighbour(dx, dy, dz, valleyOrSlopeShapes);
    }

    protected boolean hasNeighbour(int dx, int dy, int dz, EnumShape[] shapes) {
        //Direction dir = this.t.v(dx, dy, dz).facing();
        //TileShape nte = this.te.getConnectedNeighbourGlobal(dir);
        //if (nte != null) {
        //    for (int i = 0; i < shapes.length; i++)
        //        if (nte.shape == shapes[i])
        //            return true;
        //}
        return false;
    }

    //-------------------------------------------------------------------------------------

    protected void beginTopFace() {
        this.beginOuterFaces(Vector3.unitY);
    }

    protected void beginBottomFace() {
        this.beginOuterFaces(Vector3.unitNY);
    }

    protected void beginPosXFace() {
        this.beginOuterFaces(Vector3.unitX);
    }

    protected void beginNegXFace() {
        this.beginOuterFaces(Vector3.unitNX);
    }

    protected void beginPosZFace() {
        this.beginOuterFaces(Vector3.unitZ);
    }

    protected void beginNegZFace() {
        this.beginOuterFaces(Vector3.unitNZ);
    }

    protected void beginPosXSlope() {
        this.beginInnerFaces(Vector3.unitPXPY);
    }

    protected void beginNegXSlope() {
        this.beginInnerFaces(Vector3.unitNXPY);
    }

    protected void beginPosZSlope() {
        this.beginInnerFaces(Vector3.unitPYPZ);
    }

    protected void beginNegZSlope() {
        this.beginInnerFaces(Vector3.unitPYNZ);
    }

    //-------------------------------------------------------------------------------------

    protected void beginInnerFaces(Vector3 n) {
        this.outerFace = false;
        this.normal(n);
        //this.target.setTexture(this.textures[2]);
        //this.target.setColor(this.getSecondaryColourMult());
    }

    protected void beginOuterFaces(Vector3 n) {
        this.outerFace = true;
        this.normal(n);
        //this.target.setTexture(this.textures[1]);
        //this.target.setColor(this.getBaseColourMult());
    }

    protected void beginTriangle() {
        //this.target.beginTriangle();
    }

    protected void beginQuad() {
        //this.target.beginQuad();
    }

    protected void newTriangle() {
        this.endFace();
        this.beginTriangle();
    }

    protected void newQuad() {
        this.endFace();
        this.beginQuad();
    }

    protected void endFace() {
        //this.target.endFace();
    }

    protected void normal(Vector3 n) {
        //Vector3 tn = this.t.v(n);
        //this.face = tn.facing();
        //this.target.setNormal(tn);
    }

    protected void vertex(double x, double y, double z, double u, double v) {
        //Vector3 q = this.t.p(x - 0.5, y - 0.5, z - 0.5);
        //this.target.addVertex(q, u, v);
    }

}