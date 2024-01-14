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

/**
 * Implementation of {@link IShapePlacementLogic} that points the shape based on the player's look vector, and spins
 * the shape based on the side of the block the player clicked on during placement.
 */
public class ShapePlacementLogicStairLike implements IShapePlacementLogic<BlockArchitecture> {

    public static final ShapePlacementLogicStairLike INSTANCE = new ShapePlacementLogicStairLike();
    private final ImmutableCollection<ShapeOrientationProperty<?>> properties = ImmutableList.of(
            ShapeOrientationPropertyFacing.INSTANCE,
            ShapeOrientationPropertySpin.INSTANCE
    );

    @Override
    public @NotNull ShapeOrientation getShapeOrientationForPlacement(
            @NotNull BlockArchitecture beingPlaced,
            @NotNull Level level,
            @NotNull BlockPos placementPos,
            @NotNull Player placer,
            @NotNull BlockHitResult hitResult) {
        var playerFacing = Direction.orderedByNearest(placer)[0];
        playerFacing = placer.isCrouching() ? playerFacing.getOpposite() : playerFacing;

        var horizontalDirection = placer.getDirection();

        var hitSide = hitResult.getDirection();
        var hitSection = EnumBlockFaceSection.fromHitResult(horizontalDirection, hitResult);
        var facing = horizontalDirection.getOpposite();
        var spin = this.getSpin(hitSide, playerFacing, hitSection);

        return ShapeOrientation.forProperties(
                ShapeOrientationPropertyFacing.of(facing),
                ShapeOrientationPropertySpin.of(spin)
        );
    }

    @NotNull
    private EnumSpin getSpin(Direction hitSide, Direction playerFacing, EnumBlockFaceSection hitSection) {
        var spin = EnumSpin.NONE;
        if (hitSide == Direction.DOWN) {
            spin = EnumSpin.HALF;
        }

        if (playerFacing.getAxis().isVertical()) {
            if (playerFacing == Direction.UP) {
                spin = EnumSpin.HALF;
            }
        } else {
            if (hitSide.getAxis().isHorizontal()) {
                switch (hitSection) {
                    case TOP -> {
                        spin = EnumSpin.HALF;
                    }
                    case LEFT -> {
                        spin = EnumSpin.ONE_QUARTER;
                    }
                    case RIGHT -> {
                        spin = EnumSpin.THREE_QUARTER;
                    }
                }
            }
        }
        return spin;
    }

    @Override
    @NotNull
    public ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return this.properties;
    }

}
