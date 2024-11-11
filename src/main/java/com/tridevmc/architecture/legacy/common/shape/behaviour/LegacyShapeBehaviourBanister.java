package com.tridevmc.architecture.legacy.common.shape.behaviour;

import com.tridevmc.architecture.common.helpers.Profile;
import com.tridevmc.architecture.common.utils.MiscUtils;
import com.tridevmc.architecture.legacy.math.LegacyTrans3;
import com.tridevmc.architecture.legacy.math.LegacyVector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;

import static net.minecraft.core.Direction.*;

@Deprecated
public class LegacyShapeBehaviourBanister extends LegacyShapeBehaviourModel {

    public LegacyShapeBehaviourBanister(String modelName) {
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
    public double placementOffsetX() {
        return 6 / 16d;
    }

}
