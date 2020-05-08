package com.tridevmc.architecture.common.shape.behaviour;

import com.tridevmc.architecture.common.block.BlockHelper;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Vector3;
import com.tridevmc.architecture.common.tile.TileShape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.PaneBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import java.util.List;

public class ShapeBehaviourWindow extends ShapeBehaviour {
    public Direction[] frameSides;

    public boolean[] frameAlways;
    public FrameType[] frameTypes;
    public Direction[] frameOrientations;
    public Trans3[] frameTrans;

    @Override
    public boolean orientOnPlacement(PlayerEntity player, TileShape te, TileShape nte, Direction otherFace, Vector3 hit) {
        int turn = -1;
        // If click is on side of a non-window block, orient perpendicular to it
        if (!player.isCrouching() && (nte == null || !(nte.getArchitectureShape().behaviour instanceof ShapeBehaviourWindow))) {
            switch (otherFace) {
                case EAST:
                case WEST:
                    turn = 0;
                    break;
                case NORTH:
                case SOUTH:
                    turn = 1;
                    break;
            }
        }
        if (turn >= 0) {
            te.setSide(0);
            te.setTurn(turn);
            return true;
        } else
            return false;
    }

    public FrameType frameTypeForLocalSide(Direction side) {
        return this.frameTypes[side.ordinal()];
    }

    public Direction frameOrientationForLocalSide(Direction side) {
        return this.frameOrientations[side.ordinal()];
    }

    @Override
    public boolean canPlaceUpsideDown() {
        return false;
    }

    @Override
    public double sideZoneSize() {
        return 1 / 8d; // 3/32d;
    }

    @Override
    public boolean highlightZones() {
        return true;
    }

    @Override
    public ItemStack newSecondaryMaterialStack(BlockState state) {
        return BlockHelper.blockStackWithState(state, 1);
    }

    @Override
    public boolean isValidSecondaryMaterial(BlockState state) {
        Block block = state.getBlock();
        return block instanceof PaneBlock;
    }

    @Override
    public void addCollisionBoxesToList(TileShape te, IBlockReader world, BlockPos pos, BlockState state,
                                        Entity entity, Trans3 t, List list) {
        final double r = 1 / 8d, s = 3 / 32d;
        double[] e = new double[4];
        this.addCentreBoxesToList(r, s, t, list);
        for (int i = 0; i <= 3; i++) {
            boolean frame = this.frameAlways[i] || !this.isConnectedGlobal(te, t.t(this.frameSides[i]));
            if (entity == null || frame) {
                Trans3 ts = t.t(this.frameTrans[i]);
                this.addFrameBoxesToList(i, r, s, ts, list);
            }
            e[i] = frame ? 0.5 - r : 0.5;
        }
        if (te.getSecondaryBlockState() != null)
            this.addGlassBoxesToList(r, s, 1 / 32d, e, t, list);

        if (list.isEmpty()) {
            // Fallback box in the unlikely case that no box was added.
            this.addBox(new Vector3(-0.5, -0.5, -0.5), new Vector3(0.5, 0.5, 0.5), t, list);
        }
    }

    protected void addCentreBoxesToList(double r, double s, Trans3 t, List list) {
    }

    protected void addFrameBoxesToList(int i, double r, double s, Trans3 ts, List list) {
        ts.addBox(-0.5, -0.5, -s, 0.5, -0.5 + r, s, list);
    }

    protected void addGlassBoxesToList(double r, double s, double w, double[] e, Trans3 t, List list) {
        t.addBox(-e[3], -e[0], -w, e[1], e[2], w, list);
    }

    protected boolean isConnectedGlobal(TileShape te, Direction globalDir) {
        return this.getConnectedWindowGlobal(te, globalDir) != null;
    }

    public TileShape getConnectedWindowGlobal(TileShape te, Direction globalDir) {
        Direction thisLocalDir = te.localFace(globalDir);
        FrameType thisFrameType = this.frameTypeForLocalSide(thisLocalDir);
        if (thisFrameType != FrameType.NONE) {
            Direction thisOrient = this.frameOrientationForLocalSide(thisLocalDir);
            TileShape nte = te.getConnectedNeighbourGlobal(globalDir);
            if (nte != null && nte.getArchitectureShape().behaviour instanceof ShapeBehaviourWindow) {
                ShapeBehaviourWindow otherType = (ShapeBehaviourWindow) nte.getArchitectureShape().behaviour;
                Direction otherLocalDir = nte.localFace(globalDir.getOpposite());
                FrameType otherFrameType = otherType.frameTypeForLocalSide(otherLocalDir);
                if (otherFrameType != FrameType.NONE) {
                    Direction otherOrient = otherType.frameOrientationForLocalSide(otherLocalDir);
                    if (this.framesMatch(thisFrameType, otherFrameType,
                            te.globalFace(thisOrient), nte.globalFace(otherOrient)))
                        return nte;
                }
            }
        }
        return null;
    }

    protected boolean framesMatch(FrameType type0, FrameType type1,
                                  Direction orient1, Direction orient2) {
        if (type0 == type1) {
            switch (type0) {
                case PLAIN:
                    return orient1.getAxis() == orient2.getAxis();
                default:
                    return orient1 == orient2;
            }
        }
        return false;
    }

    public enum FrameType {NONE, PLAIN, CORNER}
}
