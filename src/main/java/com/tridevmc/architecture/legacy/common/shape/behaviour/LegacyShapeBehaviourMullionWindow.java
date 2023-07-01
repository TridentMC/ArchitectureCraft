package com.tridevmc.architecture.legacy.common.shape.behaviour;

import com.tridevmc.architecture.legacy.math.LegacyTrans3;
import net.minecraft.world.phys.shapes.VoxelShape;

@Deprecated
public class LegacyShapeBehaviourMullionWindow extends LegacyShapeBehaviourPlainWindow {

    @Override
    protected VoxelShape addCentreBoxesToList(double r, double s, LegacyTrans3 t, VoxelShape shape) {
        return t.addBox(-r, -0.5, -s, r, 0.5, s, shape);
    }

    @Override
    protected VoxelShape addGlassBoxesToList(double r, double s, double w, double[] e, LegacyTrans3 t, VoxelShape shape) {
        shape = t.addBox(-e[3], -e[0], -w, -r, e[2], w, shape);
        shape = t.addBox(r, -e[0], -w, e[1], e[2], w, shape);
        return shape;
    }

}
