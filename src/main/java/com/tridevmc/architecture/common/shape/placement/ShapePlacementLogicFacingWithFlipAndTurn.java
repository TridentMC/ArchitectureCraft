package com.tridevmc.architecture.common.shape.placement;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class ShapePlacementLogicFacingWithFlipAndTurn implements IShapePlacementLogic<BlockArchitecture> {

    public static final ShapePlacementLogicFacingWithFlipAndTurn INSTANCE = new ShapePlacementLogicFacingWithFlipAndTurn();
    private final ImmutableCollection<ShapeOrientationProperty<?>> properties = ImmutableList.of(
            ShapeOrientationPropertyFlip.INSTANCE,
            ShapeOrientationPropertyTurn.INSTANCE,
            ShapeOrientationPropertyFacing.INSTANCE
    );

    @Override
    public @NotNull ShapeOrientation getShapeOrientationForPlacement(@NotNull BlockArchitecture beingPlaced, @NotNull Level level, @NotNull BlockPos placementPos, @NotNull Player placer, @NotNull BlockHitResult hitResult) {
        var faceClicked = hitResult.getDirection();
        var turn = EnumTurn.UNTURNED;

        // If the player isn't looking directly at the block and is instead looking at it from the side then we turn the shape.
        var playerFacing = placer.getDirection();
        if (faceClicked.getAxis().isHorizontal()) {
            if (faceClicked != playerFacing && faceClicked != playerFacing.getOpposite()) {
                turn = EnumTurn.TURNED;
            }
        } else {
            if (playerFacing.getAxis() == Direction.Axis.Z) {
                turn = EnumTurn.TURNED;
            }
        }

        return ShapeOrientation.forProperties(
                ShapeOrientationPropertyFacing.of(faceClicked.getOpposite()),
                ShapeOrientationPropertyFlip.of(placer.isCrouching() ? EnumFlip.FLIPPED : EnumFlip.STANDARD),
                ShapeOrientationPropertyTurn.of(turn)
        );
    }

    @Override
    public @NotNull ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return this.properties;
    }
}
