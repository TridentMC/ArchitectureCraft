package com.tridevmc.architecture.common.shape.behaviour;

import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Vector3;
import com.tridevmc.architecture.common.tile.TileShape;
import com.tridevmc.architecture.common.utils.MiscUtils;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;

import static net.minecraft.util.Direction.*;

public class ShapeBehaviourPlainWindow extends ShapeBehaviourWindow {
    public ShapeBehaviourPlainWindow() {
        this.frameSides = new Direction[]{DOWN, EAST, UP, WEST};
        this.frameAlways = new boolean[]{false, false, false, false};
        this.frameTypes = new ShapeBehaviourWindow.FrameType[]{ShapeBehaviourWindow.FrameType.PLAIN, ShapeBehaviourWindow.FrameType.PLAIN, ShapeBehaviourWindow.FrameType.NONE, FrameType.NONE, ShapeBehaviourWindow.FrameType.PLAIN, ShapeBehaviourWindow.FrameType.PLAIN};
        this.frameOrientations = new Direction[]{EAST, EAST, null, null, UP, UP};
        this.frameTrans = new Trans3[]{
                Trans3.ident,
                Trans3.ident.rotZ(90),
                Trans3.ident.rotZ(180),
                Trans3.ident.rotZ(270),
        };
    }

    @Override
    public boolean orientOnPlacement(PlayerEntity player, TileShape te, TileShape nte, Direction face,
                                     Vector3 hit) {
        if (nte != null && !player.isCrouching()) {
            if (nte.shape.behaviour instanceof ShapeBehaviourPlainWindow) {
                te.setSide(nte.getSide());
                te.setTurn(nte.getTurn());
                return true;
            }
            if (nte.shape.behaviour instanceof ShapeBehaviourCornerWindow) {
                Direction nlf = nte.localFace(face);
                ShapeBehaviourWindow.FrameType nfk = ((ShapeBehaviourWindow) nte.shape.behaviour).frameTypeForLocalSide(nlf);
                if (nfk == FrameType.PLAIN) {
                    Direction lf = face.getOpposite();
                    te.setSide(nte.getSide());
                    switch (nlf) {
                        case SOUTH:
                            te.setTurn(MiscUtils.turnToFace(WEST, lf));
                            return true;
                        case WEST:
                            te.setTurn(MiscUtils.turnToFace(EAST, lf));
                            return true;
                    }
                }
            }
        }
        return super.orientOnPlacement(player, te, nte, face, hit);
    }
}
