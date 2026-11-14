package com.tridevmc.architecture.common.shape.placement;

import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationProperty;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFacing;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public final class ShapePlacementLogicCladding implements IShapePlacementLogic<BlockArchitecture> {

    public static final ShapePlacementLogicCladding INSTANCE = new ShapePlacementLogicCladding();

    @Override
    public @NotNull ShapeOrientation getShapeOrientationForPlacement(@NotNull BlockArchitecture block,
            @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        return ShapeOrientation.forProperties(ShapeOrientationPropertyFacing.of(hit.getDirection().getOpposite()));
    }

    @Override
    public ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return ImmutableList.of(ShapeOrientationPropertyFacing.INSTANCE);
    }
}
