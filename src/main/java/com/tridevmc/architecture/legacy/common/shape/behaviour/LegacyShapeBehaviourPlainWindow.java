package com.tridevmc.architecture.legacy.common.shape.behaviour;

import com.tridevmc.architecture.common.utils.MiscUtils;
import com.tridevmc.architecture.legacy.common.block.entity.LegacyShapeBlockEntity;
import com.tridevmc.architecture.legacy.math.LegacyTrans3;
import com.tridevmc.architecture.legacy.math.LegacyVector3;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;

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

    @Override
    public boolean orientOnPlacement(Player player, LegacyShapeBlockEntity te, LegacyShapeBlockEntity nte, Direction face,
                                     LegacyVector3 hit) {
        if (nte != null && !player.isCrouching()) {
            if (nte.getArchitectureShape().behaviour instanceof LegacyShapeBehaviourPlainWindow) {
                te.setSide(nte.getSide());
                te.setTurn(nte.getTurn());
                return true;
            }
            if (nte.getArchitectureShape().behaviour instanceof LegacyShapeBehaviourCornerWindow) {
                Direction nlf = nte.localFace(face);
                LegacyShapeBehaviourWindow.FrameType nfk = ((LegacyShapeBehaviourWindow) nte.getArchitectureShape().behaviour).frameTypeForLocalSide(nlf);
                if (nfk == FrameType.PLAIN) {
                    Direction lf = face.getOpposite();
                    te.setSide(nte.getSide());
                    switch (nlf) {
                        case SOUTH -> {
                            te.setTurn(MiscUtils.turnToFace(WEST, lf));
                            return true;
                        }
                        case WEST -> {
                            te.setTurn(MiscUtils.turnToFace(EAST, lf));
                            return true;
                        }
                    }
                }
            }
        }
        return super.orientOnPlacement(player, te, nte, face, hit);
    }

}
