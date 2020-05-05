package com.tridevmc.architecture.common.shape.behaviour;

import com.tridevmc.architecture.common.block.BlockShape;
import com.tridevmc.architecture.common.helpers.Profile;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Vector3;
import com.tridevmc.architecture.common.tile.TileShape;
import com.tridevmc.architecture.common.utils.MiscUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StairsBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.properties.Half;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

import static net.minecraft.util.Direction.*;

public class ShapeBehaviourBanister extends ShapeBehaviourModel {

    public ShapeBehaviourBanister(String modelName) {
        super(modelName, Profile.Generic.tbOffset);
    }

    private static Direction stairsFacing(BlockState state) {
        return state.get(StairsBlock.FACING);
    }

    private static int stairsSide(BlockState state) {
        if (state.get(StairsBlock.HALF) == Half.TOP)
            return 1;
        else
            return 0;
    }

    @Override
    public boolean orientOnPlacement(PlayerEntity player, TileShape te,
                                     BlockPos npos, BlockState nstate, TileEntity nte, Direction otherFace, Vector3 hit) {
        if (!player.isCrouching()) {
            Block nblock = nstate.getBlock();
            boolean placedOnStair = false;
            int nside = -1; // Side that the neighbouring block is placed on
            int nturn = -1; // Turn of the neighbouring block
            if (StairsBlock.isBlockStairs(nstate) && (otherFace == UP || otherFace == DOWN)) {
                placedOnStair = true;
                nside = stairsSide(nstate);
                nturn = MiscUtils.turnToFace(SOUTH, stairsFacing(nstate));
                if (nside == 1 && (nturn & 1) == 0)
                    nturn ^= 2;
            } else if (nblock instanceof BlockShape) {
                if (nte instanceof TileShape) {
                    placedOnStair = true;
                    nside = ((TileShape) nte).getSide();
                    nturn = ((TileShape) nte).getTurn();
                }
            }
            if (placedOnStair) {
                int side = otherFace.getOpposite().ordinal();
                if (side == nside) {
                    Vector3 h = Trans3.sideTurn(side, 0).ip(hit);
                    double offx = te.shape.offsetXForPlacementHit(side, nturn, hit);
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
