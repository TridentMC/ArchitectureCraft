package com.tridevmc.architecture.legacy.common.shape.behaviour;

import com.tridevmc.architecture.legacy.math.LegacyTrans3;
import net.minecraft.core.Direction;

import static net.minecraft.core.Direction.*;

@Deprecated
public class LegacyShapeBehaviourPlainWindow extends LegacyShapeBehaviourWindow {

    public LegacyShapeBehaviourPlainWindow() {
        this.frameSides = new Direction[]{DOWN, EAST, UP, WEST};
        this.frameAlways = new boolean[]{false, false, false, false};
        this.frameTypes = new LegacyShapeBehaviourWindow.FrameType[]{LegacyShapeBehaviourWindow.FrameType.PLAIN, LegacyShapeBehaviourWindow.FrameType.PLAIN, LegacyShapeBehaviourWindow.FrameType.NONE, FrameType.NONE, LegacyShapeBehaviourWindow.FrameType.PLAIN, LegacyShapeBehaviourWindow.FrameType.PLAIN};
        this.frameOrientations = new Direction[]{EAST, EAST, null, null, UP, UP};
        this.frameTrans = new LegacyTrans3[]{
                LegacyTrans3.ident,
                LegacyTrans3.ident.rotZ(90),
                LegacyTrans3.ident.rotZ(180),
                LegacyTrans3.ident.rotZ(270),
        };
    }


}
