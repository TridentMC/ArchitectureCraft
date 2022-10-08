package com.tridevmc.architecture.common.shape.behaviour;

import com.tridevmc.architecture.common.block.BlockShape;
import com.tridevmc.architecture.common.block.entity.ShapeBlockEntity;
import com.tridevmc.architecture.common.helpers.Profile;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Vector3;
import com.tridevmc.architecture.common.utils.MiscUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

import static net.minecraft.core.Direction.*;


public class ShapeBehaviourBanister extends ShapeBehaviourModel {

    public ShapeBehaviourBanister(String modelName) {
        super(modelName, Profile.Generic.tbOffset);
    }

    private static Direction stairsFacing(BlockState state) {
        return state.getValue(StairBlock.FACING);
    }

    private static int stairsSide(BlockState state) {
        if (state.getValue(StairBlock.HALF) == Half.TOP)
            return 1;
        else
            return 0;
    }

    @Override
    public boolean orientOnPlacement(Player player, ShapeBlockEntity te,
                                     BlockPos npos, BlockState nstate, BlockEntity nte, Direction otherFace, Vector3 hit) {
        if (!player.isCrouching()) {
            var nblock = nstate.getBlock();
            boolean placedOnStair = false;
            int nside = -1; // Side that the neighbouring block is placed on
            int nturn = -1; // Turn of the neighbouring block
            if (StairBlock.isStairs(nstate) && (otherFace == UP || otherFace == DOWN)) {
                placedOnStair = true;
                nside = stairsSide(nstate);
                nturn = MiscUtils.turnToFace(SOUTH, stairsFacing(nstate));
                if (nside == 1 && (nturn & 1) == 0)
                    nturn ^= 2;
            } else if (nblock instanceof BlockShape) {
                if (nte instanceof ShapeBlockEntity) {
                    placedOnStair = true;
                    nside = ((ShapeBlockEntity) nte).getSide();
                    nturn = ((ShapeBlockEntity) nte).getTurn();
                }
            }
            if (placedOnStair) {
                int side = otherFace.getOpposite().ordinal();
                if (side == nside) {
                    Vector3 h = Trans3.sideTurn(side, 0).ip(hit);
                    double offx = te.getArchitectureShape().offsetXForPlacementHit(side, nturn, hit);
                    te.setSide(side);
                    te.setTurn(nturn & 3);
                    te.setOffsetX(offx);
                    return true;
                }
            }
        }
        return super.orientOnPlacement(player, te, npos, nstate, nte, otherFace, hit);
    }

    @Override
    public double placementOffsetX() {
        return 6 / 16d;
    }
}
