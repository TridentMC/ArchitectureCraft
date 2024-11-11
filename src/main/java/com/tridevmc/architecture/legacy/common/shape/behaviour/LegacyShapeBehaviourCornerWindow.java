package com.tridevmc.architecture.legacy.common.shape.behaviour;

import com.tridevmc.architecture.legacy.math.LegacyTrans3;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.core.Direction.*;

@Deprecated
public class LegacyShapeBehaviourCornerWindow extends LegacyShapeBehaviourWindow {

    public LegacyShapeBehaviourCornerWindow() {
        this.frameSides = new Direction[]{DOWN, SOUTH, UP, WEST};
        this.frameAlways = new boolean[]{false, false, false, false};
        this.frameTypes = new FrameType[]{FrameType.CORNER, FrameType.CORNER, FrameType.NONE, FrameType.PLAIN, FrameType.PLAIN, FrameType.NONE};
        this.frameOrientations = new Direction[]{EAST, EAST, null, UP, UP, null};
        this.frameTrans = new LegacyTrans3[]{
                LegacyTrans3.ident,
                LegacyTrans3.ident.rotY(-90).rotZ(90),
                LegacyTrans3.ident.rotY(-90).rotZ(180),
                LegacyTrans3.ident.rotZ(270),
        };
    }

    @Override
    protected VoxelShape addCentreBoxesToList(double r, double s, LegacyTrans3 t, VoxelShape shape) {
        return t.addBox(-r, -0.5, -r, r, 0.5, r, shape);
    }

    @Override
    protected VoxelShape addFrameBoxesToList(int i, double r, double s, LegacyTrans3 ts, VoxelShape shape) {
        if ((i & 1) == 0) {
            shape = ts.addBox(-0.5, -0.5, -s, s, -0.5 + r, s, shape);
            shape = ts.addBox(-s, -0.5, -s, s, -0.5 + r, 0.5, shape);
        } else {
            shape = super.addFrameBoxesToList(i, r, s, ts, shape);
        }
        return shape;
    }

    @Override
    protected VoxelShape addGlassBoxesToList(double r, double s, double w, double[] e, LegacyTrans3 t, VoxelShape shape) {
        shape = t.addBox(-e[3], -e[0], -w, -s, e[2], w, shape);
        shape = t.addBox(-w, -e[0], s, w, e[2], e[1], shape);
        return shape;
    }


}
