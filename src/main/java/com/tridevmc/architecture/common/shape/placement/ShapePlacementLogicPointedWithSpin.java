package com.tridevmc.architecture.common.shape.placement;

import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.EnumSpin;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFacing;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertySpin;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Implementation of {@link IShapePlacementLogic} that points the shape based on the player's look vector, and spins
 * the shape based on the side of the block the player clicked on during placement.
 */
public class ShapePlacementLogicPointedWithSpin implements IShapePlacementLogic<BlockArchitecture> {

    public static final ShapePlacementLogicPointedWithSpin INSTANCE = new ShapePlacementLogicPointedWithSpin();

    @Override
    public @NotNull ShapeOrientation getShapeOrientationForPlacement(
            @NotNull BlockArchitecture beingPlaced,
            @NotNull Level level,
            @NotNull BlockPos placementPos,
            @NotNull Player placer,
            @Nullable BlockHitResult hitResult) {
        // Pointed can point any direction, and is determined by the player's look vector.
        // If the player is crouching, then the orientation will point towards their look vector instead of at the player.
        // The spin is determined by the side of the block the player clicked on, again this is reversed if the player is crouching.
        var facing = Direction.orderedByNearest(placer)[0].getOpposite();
        facing = placer.isCrouching() ? facing.getOpposite() : facing;

        // Assuming facing north then we use the following rules;
        // if player clicked on top of block then EnumSpin.NONE
        // if player clicked on bottom of block then EnumSpin.HALF
        // if player clicked on the west side of block then EnumSpin.ONE_QUARTER
        // if player clicked on the east side of block then EnumSpin.THREE_QUARTER
        // if player clicked on the north side of block then EnumSpin.HALF
        // if player clicked on the south side of block then EnumSpin.NONE
        // All of these rules are rotated based on the facing of the block.
        var hitSide = hitResult != null ? hitResult.getDirection() : Direction.UP;
        hitSide = placer.isCrouching() ? hitSide.getOpposite() : hitSide;
        var spin = EnumSpin.NONE;
        // Rotate the hit side to match the facing of the block.
        hitSide = switch (facing) {
            case NORTH -> hitSide;
            case SOUTH -> hitSide.getOpposite();
            case EAST -> hitSide.getClockWise();
            case WEST -> hitSide.getCounterClockWise();
            case UP -> hitSide.getClockWise(Direction.Axis.X);
            case DOWN -> hitSide.getCounterClockWise(Direction.Axis.X);
        };

        // Now we can determine the spin.
        spin = switch (hitSide) {
            case UP, SOUTH -> EnumSpin.NONE;
            case DOWN, NORTH -> EnumSpin.HALF;
            case WEST -> EnumSpin.ONE_QUARTER;
            case EAST -> EnumSpin.THREE_QUARTER;
        };

        return ShapeOrientation.forProperties(
                ShapeOrientationPropertyFacing.of(facing),
                ShapeOrientationPropertySpin.of(spin)
        );
    }

}
