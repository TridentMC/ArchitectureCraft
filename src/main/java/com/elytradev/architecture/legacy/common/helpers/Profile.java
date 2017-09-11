//------------------------------------------------------------------------------
//
//	 ArchitectureCraft - Shape profile utilities
//
//------------------------------------------------------------------------------

package com.elytradev.architecture.legacy.common.helpers;

import com.elytradev.architecture.legacy.common.shape.Shape;
import net.minecraft.util.EnumFacing;

import java.util.HashMap;
import java.util.Map;

public class Profile {

    protected static Map opposites = new HashMap();

    public static Object getProfileGlobal(Shape shape, int side, int turn, EnumFacing globalFace) {
        EnumFacing localFace = Trans3.sideTurnRotations[side][turn].it(globalFace);
        return shape.kind.profileForLocalFace(shape, localFace);
    }

    public static boolean matches(Object profile1, Object profile2) {
        Object opposite1 = opposites.get(profile1);
        if (opposite1 != null)
            return opposite1 == profile2;
        else
            return profile1 == profile2;
    }

    public static void declareOpposite(Object profile1, Object profile2) {
        opposites.put(profile1, profile2);
        opposites.put(profile2, profile1);
    }

    public enum Generic {
        End, LeftEnd, RightEnd, OffsetBottom, OffsetTop;

        public static Generic[] eeStraight = {null, null, null, null, End, End};
        public static Generic[] lrStraight = {null, null, null, null, RightEnd, LeftEnd};
        public static Generic[] eeCorner = {null, null, null, End, End, null};
        public static Generic[] lrCorner = {null, null, null, LeftEnd, RightEnd, null};
        public static Generic[] rlCorner = {null, null, RightEnd, null, null, LeftEnd};
        public static Generic[] tOffset = {null, OffsetTop, null, null, null, null};
        public static Generic[] bOffset = {OffsetBottom, null, null, null, null, null};
        public static Generic[] tbOffset = {OffsetBottom, OffsetTop, null, null, null, null};

        static {
            declareOpposite(LeftEnd, RightEnd);
            declareOpposite(OffsetBottom, OffsetTop);
        }

    }

}
