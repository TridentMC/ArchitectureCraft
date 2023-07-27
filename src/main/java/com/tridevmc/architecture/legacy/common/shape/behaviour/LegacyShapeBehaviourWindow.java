package com.tridevmc.architecture.legacy.common.shape.behaviour;

import com.tridevmc.architecture.legacy.common.block.LegacyBlockHelper;
import com.tridevmc.architecture.legacy.common.block.entity.LegacyShapeBlockEntity;
import com.tridevmc.architecture.legacy.math.LegacyTrans3;
import com.tridevmc.architecture.legacy.math.LegacyVector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Deprecated
public class LegacyShapeBehaviourWindow extends LegacyShapeBehaviour {

    public Direction[] frameSides;

    public boolean[] frameAlways;
    public FrameType[] frameTypes;
    public Direction[] frameOrientations;
    public LegacyTrans3[] frameTrans;

    @Override
    public boolean orientOnPlacement(Player player, LegacyShapeBlockEntity te, LegacyShapeBlockEntity nte, Direction otherFace, LegacyVector3 hit) {
        int turn = -1;
        // If click is on side of a non-window block, orient perpendicular to it
        if (!player.isCrouching() && (nte == null || !(nte.getArchitectureShape().behaviour instanceof LegacyShapeBehaviourWindow))) {
            switch (otherFace) {
                case EAST, WEST -> turn = 0;
                case NORTH, SOUTH -> turn = 1;
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
        return LegacyBlockHelper.blockStackWithState(state, 1);
    }

    @Override
    public boolean isValidSecondaryMaterial(BlockState state) {
        Block block = state.getBlock();
        return block instanceof CrossCollisionBlock;
    }

    @Override
    protected VoxelShape getCollisionBox(LegacyShapeBlockEntity te, BlockGetter world, BlockPos pos, BlockState state, Entity entity, LegacyTrans3 t) {
        VoxelShape shape = Shapes.empty();
        final double r = 1 / 8d, s = 3 / 32d;
        double[] e = new double[4];
        shape = this.addCentreBoxesToList(r, s, t, shape);
        for (int i = 0; i <= 3; i++) {
            boolean frame = this.frameAlways[i] || !this.isConnectedGlobal(te, t.t(this.frameSides[i]));
            if (entity == null || frame) {
                LegacyTrans3 ts = t.t(this.frameTrans[i]);
                shape = this.addFrameBoxesToList(i, r, s, ts, shape);
            }
            e[i] = frame ? 0.5 - r : 0.5;
        }
        if (te.getSecondaryBlockState() != null)
            shape = this.addGlassBoxesToList(r, s, 1 / 32d, e, t, shape);
        if (shape.isEmpty()) {
            // Fallback box in the unlikely case that no box was added.
            shape = this.addBox(new LegacyVector3(-0.5, -0.5, -0.5), new LegacyVector3(0.5, 0.5, 0.5), t, shape);
        }
        return shape.optimize();
    }

    protected VoxelShape addCentreBoxesToList(double r, double s, LegacyTrans3 t, VoxelShape shape) {
        return shape;
    }

    protected VoxelShape addFrameBoxesToList(int i, double r, double s, LegacyTrans3 ts, VoxelShape shape) {
        return ts.addBox(-0.5, -0.5, -s, 0.5, -0.5 + r, s, shape);

    }

    protected VoxelShape addGlassBoxesToList(double r, double s, double w, double[] e, LegacyTrans3 t, VoxelShape shape) {
        return t.addBox(-e[3], -e[0], -w, e[1], e[2], w, shape);
    }

    protected boolean isConnectedGlobal(LegacyShapeBlockEntity te, Direction globalDir) {
        return this.getConnectedWindowGlobal(te, globalDir) != null;
    }

    public LegacyShapeBlockEntity getConnectedWindowGlobal(LegacyShapeBlockEntity te, Direction globalDir) {
        Direction thisLocalDir = te.localFace(globalDir);
        FrameType thisFrameType = this.frameTypeForLocalSide(thisLocalDir);
        if (thisFrameType != FrameType.NONE) {
            Direction thisOrient = this.frameOrientationForLocalSide(thisLocalDir);
            LegacyShapeBlockEntity nte = te.getConnectedNeighbourGlobal(globalDir);
            if (nte != null && nte.getArchitectureShape().behaviour instanceof LegacyShapeBehaviourWindow otherType) {
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
            return switch (type0) {
                case PLAIN -> orient1.getAxis() == orient2.getAxis();
                default -> orient1 == orient2;
            };
        }
        return false;
    }

    public enum FrameType {NONE, PLAIN, CORNER}

}
