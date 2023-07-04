package com.tridevmc.architecture.common.shape.placement;

import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyAxis;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFacing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of {@link IShapePlacementLogic} that sets the axis of the shape based on the side of the block clicked.
 */
public class ShapePlacementLogicOnAxis implements IShapePlacementLogic<BlockArchitecture> {
    public static final ShapePlacementLogicOnAxis INSTANCE = new ShapePlacementLogicOnAxis();

    @Override
    @NotNull
    public ShapeOrientation getShapeOrientationForPlacement(
            @NotNull BlockArchitecture beingPlaced,
            @NotNull Level level,
            @NotNull BlockPos placementPos,
            @NotNull Player placer,
            @Nullable BlockHitResult hitResult) {
        // The axis is just the same as the axis of the side clicked.
        var axis = hitResult != null ? hitResult.getDirection().getAxis() : Direction.Axis.Y;
        return ShapeOrientation.forProperties(
                ShapeOrientationPropertyAxis.of(axis)
        );
    }
}
