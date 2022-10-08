package com.tridevmc.architecture.common.shape.behaviour;

import com.tridevmc.architecture.common.block.entity.ShapeBlockEntity;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Vector3;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.shapes.VoxelShape;

import static net.minecraft.core.Direction.*;


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
    protected VoxelShape addCentreBoxesToList(double r, double s, Trans3 t, VoxelShape shape) {
        return t.addBox(-r, -0.5, -r, r, 0.5, r, shape);
    }

    @Override
    protected VoxelShape addFrameBoxesToList(int i, double r, double s, Trans3 ts, VoxelShape shape) {
        if ((i & 1) == 0) {
            shape = ts.addBox(-0.5, -0.5, -s, s, -0.5 + r, s, shape);
            shape = ts.addBox(-s, -0.5, -s, s, -0.5 + r, 0.5, shape);
        } else {
            shape = super.addFrameBoxesToList(i, r, s, ts, shape);
        }
        return shape;
    }

    @Override
    protected VoxelShape addGlassBoxesToList(double r, double s, double w, double[] e, Trans3 t, VoxelShape shape) {
        shape = t.addBox(-e[3], -e[0], -w, -s, e[2], w, shape);
        shape = t.addBox(-w, -e[0], s, w, e[2], e[1], shape);
        return shape;
    }

    @Override
    public boolean orientOnPlacement(Player player, ShapeBlockEntity te, ShapeBlockEntity nte, Direction face,
                                     Vector3 hit) {
        if (nte != null && !player.isCrouching()) {
            if (nte.getArchitectureShape().behaviour instanceof ShapeBehaviourWindow nsk) {
                Direction nlf = nte.localFace(face);
                FrameType nfk = nsk.frameTypeForLocalSide(nlf);
                switch (nfk) {
                    case CORNER -> {
                        te.setSide(nte.getSide());
                        te.setTurn(nte.getTurn());
                        return true;
                    }
                    case PLAIN -> {
                        Direction nfo = nte.globalFace(nsk.frameOrientationForLocalSide(nlf));
                        return this.orientFromAdjacentCorner(te, nfo, hit)
                                || this.orientFromAdjacentCorner(te, nfo.getOpposite(), hit);
                    }
                }
            }
        }
        return super.orientOnPlacement(player, te, nte, face, hit);
    }

    protected boolean orientFromAdjacentCorner(ShapeBlockEntity te, Direction face, Vector3 hit) {
        ShapeBlockEntity nte = ShapeBlockEntity.get(te.getLevel(), te.getBlockPos().relative(face.getOpposite()));
        if (nte != null && nte.getArchitectureShape().behaviour instanceof ShapeBehaviourWindow nsk) {
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
