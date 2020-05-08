package com.tridevmc.architecture.common.shape.behaviour;

import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Vector3;
import com.tridevmc.architecture.common.tile.TileShape;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;

import java.util.List;

import static net.minecraft.util.Direction.*;

public class ShapeBehaviourCornerWindow extends ShapeBehaviourWindow {

    public ShapeBehaviourCornerWindow() {
        this.frameSides = new Direction[]{DOWN, SOUTH, UP, WEST};
        this.frameAlways = new boolean[]{false, false, false, false};
        this.frameTypes = new FrameType[]{FrameType.CORNER, FrameType.CORNER, FrameType.NONE, FrameType.PLAIN, FrameType.PLAIN, FrameType.NONE};
        this.frameOrientations = new Direction[]{EAST, EAST, null, UP, UP, null};
        this.frameTrans = new Trans3[]{
                Trans3.ident,
                Trans3.ident.rotY(-90).rotZ(90),
                Trans3.ident.rotY(-90).rotZ(180),
                Trans3.ident.rotZ(270),
        };
    }

    @Override
    protected void addCentreBoxesToList(double r, double s, Trans3 t, List list) {
        t.addBox(-r, -0.5, -r, r, 0.5, r, list);
    }

    @Override
    protected void addFrameBoxesToList(int i, double r, double s, Trans3 ts, List list) {
        if ((i & 1) == 0) {
            ts.addBox(-0.5, -0.5, -s, s, -0.5 + r, s, list);
            ts.addBox(-s, -0.5, -s, s, -0.5 + r, 0.5, list);
        } else
            super.addFrameBoxesToList(i, r, s, ts, list);
    }

    @Override
    protected void addGlassBoxesToList(double r, double s, double w, double[] e, Trans3 t, List list) {
        t.addBox(-e[3], -e[0], -w, -s, e[2], w, list);
        t.addBox(-w, -e[0], s, w, e[2], e[1], list);
    }

    @Override
    public boolean orientOnPlacement(PlayerEntity player, TileShape te, TileShape nte, Direction face,
                                     Vector3 hit) {
        if (nte != null && !player.isCrouching()) {
            if (nte.getArchitectureShape().behaviour instanceof ShapeBehaviourWindow) {
                ShapeBehaviourWindow nsk = (ShapeBehaviourWindow) nte.getArchitectureShape().behaviour;
                Direction nlf = nte.localFace(face);
                FrameType nfk = nsk.frameTypeForLocalSide(nlf);
                switch (nfk) {
                    case CORNER:
                        te.setSide(nte.getSide());
                        te.setTurn(nte.getTurn());
                        return true;
                    case PLAIN:
                        Direction nfo = nte.globalFace(nsk.frameOrientationForLocalSide(nlf));
                        return this.orientFromAdjacentCorner(te, nfo, hit)
                                || this.orientFromAdjacentCorner(te, nfo.getOpposite(), hit);
                }
            }
        }
        return super.orientOnPlacement(player, te, nte, face, hit);
    }

    protected boolean orientFromAdjacentCorner(TileShape te, Direction face, Vector3 hit) {
        TileShape nte = TileShape.get(te.getWorld(), te.getPos().offset(face.getOpposite()));
        if (nte != null && nte.getArchitectureShape().behaviour instanceof ShapeBehaviourWindow) {
            ShapeBehaviourWindow nsk = (ShapeBehaviourWindow) nte.getArchitectureShape().behaviour;
            Direction nlf = nte.localFace(face);
            FrameType nfk = nsk.frameTypeForLocalSide(nlf);
            if (nfk == FrameType.CORNER) {
                te.setSide(nte.getSide());
                te.setTurn(nte.getTurn());
                return true;
            }
        }
        return false;
    }
}
