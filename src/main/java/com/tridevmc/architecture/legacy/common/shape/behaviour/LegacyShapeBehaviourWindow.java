package com.tridevmc.architecture.legacy.common.shape.behaviour;

import com.tridevmc.architecture.legacy.common.block.LegacyBlockHelper;
import com.tridevmc.architecture.legacy.math.LegacyTrans3;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

@Deprecated
public class LegacyShapeBehaviourWindow extends LegacyShapeBehaviour {

    public Direction[] frameSides;

    public boolean[] frameAlways;
    public FrameType[] frameTypes;
    public Direction[] frameOrientations;
    public LegacyTrans3[] frameTrans;


    public FrameType frameTypeForLocalSide(Direction side) {
        return this.frameTypes[side.ordinal()];
    }

    public Direction frameOrientationForLocalSide(Direction side) {
        return this.frameOrientations[side.ordinal()];
    }


    @Override
    public double sideZoneSize() {
        return 1 / 8d; // 3/32d;
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

    protected VoxelShape addCentreBoxesToList(double r, double s, LegacyTrans3 t, VoxelShape shape) {
        return shape;
    }

    protected VoxelShape addFrameBoxesToList(int i, double r, double s, LegacyTrans3 ts, VoxelShape shape) {
        return ts.addBox(-0.5, -0.5, -s, 0.5, -0.5 + r, s, shape);

    }

    protected VoxelShape addGlassBoxesToList(double r, double s, double w, double[] e, LegacyTrans3 t, VoxelShape shape) {
        return t.addBox(-e[3], -e[0], -w, e[1], e[2], w, shape);
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
