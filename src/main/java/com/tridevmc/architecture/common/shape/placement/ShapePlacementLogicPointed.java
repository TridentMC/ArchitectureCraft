package com.tridevmc.architecture.common.shape.placement;

import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFacing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of {@link ShapePlacementLogic} that points the shape based on the player's look vector.
 */
public class ShapePlacementLogicPointed extends ShapePlacementLogic {

    public static final ShapePlacementLogicPointed INSTANCE = new ShapePlacementLogicPointed();

    @Override
    @NotNull
    public ShapeOrientation getShapeOrientationForPlacement(@NotNull Level level, @NotNull BlockPos placementPos, @NotNull Player placer, @Nullable BlockHitResult hitResult) {
        // Pointed can point any direction, and is determined by the player's look vector.
        // If the player is crouching, then the orientation will point towards their look vector instead of at the player.
        Direction nearest = Direction.orderedByNearest(placer)[0].getOpposite();
        return ShapeOrientation.forProperties(
                ShapeOrientationPropertyFacing.of(placer.isCrouching() ? nearest.getOpposite() : nearest)
        );
    }

}
