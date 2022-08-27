package com.tridevmc.architecture.common.shape.behaviour;

import com.tridevmc.architecture.common.helpers.Profile;
import com.tridevmc.architecture.common.shape.EnumShape;
import net.minecraft.core.Direction;

public class ShapeBehaviourRoof extends ShapeBehaviour {

    public static final ShapeBehaviourRoof INSTANCE = new ShapeBehaviourRoof();

    static {
        Profile.declareOpposite(RoofProfile.LEFT, RoofProfile.RIGHT);
    }

    @Override
    public boolean acceptsCladding() {
        return true;
    }

    @Override
    public boolean secondaryDefaultsToBase() {
        return true;
    }

    @Override
    public Object profileForLocalFace(EnumShape shape, Direction face) {
        switch (shape) {
            case ROOF_TILE:
            case ROOF_OVERHANG:
                switch (face) {
                    case EAST:
                        return RoofProfile.LEFT;
                    case WEST:
                        return RoofProfile.RIGHT;
                }
                break;
            case ROOF_OUTER_CORNER:
            case ROOF_OVERHANG_OUTER_CORNER:
                switch (face) {
                    case SOUTH:
                        return RoofProfile.LEFT;
                    case WEST:
                        return RoofProfile.RIGHT;
                }
                break;
            case ROOF_INNER_CORNER:
            case ROOF_OVERHANG_INNER_CORNER:
                switch (face) {
                    case EAST:
                        return RoofProfile.LEFT;
                    case NORTH:
                        return RoofProfile.RIGHT;
                }
                break;
            case ROOF_RIDGE:
            case ROOF_SMART_RIDGE:
            case ROOF_OVERHANG_RIDGE:
                return RoofProfile.RIDGE;
            case ROOF_VALLEY:
            case ROOF_SMART_VALLEY:
            case ROOF_OVERHANG_VALLEY:
                return RoofProfile.VALLEY;
        }
        return RoofProfile.NONE;
    }

    protected enum RoofProfile {NONE, LEFT, RIGHT, RIDGE, VALLEY}
}
