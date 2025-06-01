package com.tridevmc.architecture.common.shape.placement;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.*;
import com.tridevmc.architecture.core.ArchitectureLog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class ShapePlacementLogicFacingWithTurn implements IShapePlacementLogic<BlockArchitecture> {

    public static final ShapePlacementLogicFacingWithTurn INSTANCE = new ShapePlacementLogicFacingWithTurn();
    private final ImmutableCollection<ShapeOrientationProperty<?>> properties = ImmutableList.of(
            ShapeOrientationPropertyTurn.INSTANCE,
            ShapeOrientationPropertyFacing.INSTANCE
    );

    @Override
    public @NotNull ShapeOrientation getShapeOrientationForPlacement(@NotNull BlockArchitecture beingPlaced, @NotNull Level level, @NotNull BlockPos placementPos, @NotNull Player placer, @NotNull BlockHitResult hitResult) {
        // Facing is determined by the face of the block that was hit
        var faceClicked = hitResult.getDirection();
        var facing = placer.isCrouching() ? faceClicked : faceClicked.getOpposite();
        
        var playerFacing = placer.getDirection();
        var turn = EnumTurn.UNTURNED;
        
        if (faceClicked.getAxis().isVertical()) {
            if (playerFacing.getAxis() == Direction.Axis.X) { // Looking east/west
                turn = EnumTurn.TURNED;
            }
        } else {
            // Check if the player is looking at the block directly (roughly) by checking if the player's facing direction is aligned with the clicked face
            if(playerFacing.getAxis() == facing.getAxis()) {
                turn = EnumTurn.TURNED; // Player is looking directly at the block
            }
        }

        ArchitectureLog.info(
                "Player is facing {} while placing a shape on the {} face, resulting in facing {} and turn {}",
                playerFacing,
                faceClicked,
                facing,
                turn
        );

        return ShapeOrientation.forProperties(
                ShapeOrientationPropertyFacing.of(facing),
                ShapeOrientationPropertyTurn.of(turn)
        );
    }

    @Override
    public @NotNull ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return this.properties;
    }
}