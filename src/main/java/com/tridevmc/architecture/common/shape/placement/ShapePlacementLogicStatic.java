package com.tridevmc.architecture.common.shape.placement;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

/**
 * Simple implementation of {@link IShapePlacementLogic} that always returns {@link ShapeOrientation#IDENTITY}.
 * <p>
 * Useful for shapes that wouldn't be impacted by rotation like a sphere.
 */
public class ShapePlacementLogicStatic implements IShapePlacementLogic<BlockArchitecture> {

    public static final ShapePlacementLogicStatic INSTANCE = new ShapePlacementLogicStatic();

    @Override
    @NotNull
    public ShapeOrientation getShapeOrientationForPlacement(
            @NotNull BlockArchitecture beingPlaced,
            @NotNull Level level,
            @NotNull BlockPos placementPos,
            @NotNull Player placer,
            @NotNull BlockHitResult hitResult) {
        return ShapeOrientation.IDENTITY;
    }

    @Override
    @NotNull
    public ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return ImmutableList.of();
    }

}
