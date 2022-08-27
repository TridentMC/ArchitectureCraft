package com.tridevmc.architecture.common.shape.behaviour;

import com.tridevmc.architecture.common.helpers.Trans3;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShapeBehaviourMullionWindow extends ShapeBehaviourPlainWindow {
    @Override
    protected VoxelShape addCentreBoxesToList(double r, double s, Trans3 t, VoxelShape shape) {
        return t.addBox(-r, -0.5, -s, r, 0.5, s, shape);
    }

    @Override
    protected VoxelShape addGlassBoxesToList(double r, double s, double w, double[] e, Trans3 t, VoxelShape shape) {
        shape = t.addBox(-e[3], -e[0], -w, -r, e[2], w, shape);
        shape = t.addBox(r, -e[0], -w, e[1], e[2], w, shape);
        return shape;
    }
}
