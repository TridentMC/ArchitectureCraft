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

/**
 * Implementation of {@link IShapePlacementLogic} that points the shape based on the player's look vector, and spins
 * the shape based on the side of the block the player clicked on during placement.
 */
public class ShapePlacementLogicPointedWithSpin implements IShapePlacementLogic<BlockArchitecture> {

    private enum RelativeBlockFaceSection {
        TOP("Top"),
        BOTTOM("Bottom"),
        LEFT("Left"),
        RIGHT("Right"),
        MIDDLE("Middle");

        String name;

        RelativeBlockFaceSection(String name) {
            this.name = name;
        }

        static RelativeBlockFaceSection fromHitResult(Direction playerHorizontalDirection, BlockHitResult hitResult) {
            var hitVec = hitResult.getLocation();
            var hitSide = hitResult.getDirection();
            var hitX = hitVec.x - hitResult.getBlockPos().getX();
            var hitY = hitVec.y - hitResult.getBlockPos().getY();
            var hitZ = hitVec.z - hitResult.getBlockPos().getZ();

            var hitCoord0 = 0D;
            var hitCoord1 = 0D;
            if (hitSide.getAxis().isHorizontal()) {
                hitCoord0 = hitY;
                switch (playerHorizontalDirection) {
                    case NORTH -> {
                        hitCoord1 = 1 - hitX;
                    }
                    case SOUTH -> {
                        hitCoord1 = hitX;
                    }
                    case WEST -> {
                        hitCoord1 = hitZ;
                    }
                    case EAST -> {
                        hitCoord1 = 1 - hitZ;
                    }
                }
            } else {
                switch (playerHorizontalDirection) {
                    case NORTH -> {
                        hitCoord0 = 1 - hitZ;
                        hitCoord1 = 1 - hitX;
                    }
                    case SOUTH -> {
                        hitCoord0 = hitZ;
                        hitCoord1 = hitX;
                    }
                    case WEST -> {
                        hitCoord0 = 1 - hitX;
                        hitCoord1 = hitZ;
                    }
                    case EAST -> {
                        hitCoord0 = hitX;
                        hitCoord1 = 1 - hitZ;
                    }
                }
            }

            hitCoord0 -= 0.5D;
            hitCoord1 -= 0.5D;

            // Middle is defined as 0 +- 0.1
            if (Math.abs(hitCoord0) <= 0.1 && Math.abs(hitCoord1) <= 0.1) {
                return MIDDLE;
            }

            // Top is defined as 0.1+, bottom is defined as -0.1-
            // Left is defined as 0.1+, right is defined as -0.1-
            // Select the dominant coordinate to calculate from.
            var is0Dominant = Math.abs(hitCoord0) > Math.abs(hitCoord1);
            var dominantCoord = is0Dominant ? hitCoord0 : hitCoord1;
            var nonDominantCoord = is0Dominant ? hitCoord1 : hitCoord0;

            if (is0Dominant) {
                if (dominantCoord > 0.1) {
                    return TOP;
                } else if (dominantCoord < -0.1) {
                    return BOTTOM;
                } else {
                    if (nonDominantCoord > 0) {
                        return LEFT;
                    } else {
                        return RIGHT;
                    }
                }
            } else {
                if (dominantCoord > 0.1) {
                    return LEFT;
                } else if (dominantCoord < -0.1) {
                    return RIGHT;
                } else {
                    if (nonDominantCoord > 0) {
                        return TOP;
                    } else {
                        return BOTTOM;
                    }
                }
            }
        }

        @Override
        public String toString() {
            return "BlockHitSection{" +
                    "name='" + name + '\'' +
                    '}';
        }
    }

    public static final ShapePlacementLogicPointedWithSpin INSTANCE = new ShapePlacementLogicPointedWithSpin();
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
        // Pointed can point any direction, and is determined by the player's look vector.
        // If the player is crouching, then the orientation will point towards their look vector instead of at the player.
        // The spin is determined by the side of the block the player clicked on, again this is reversed if the player is crouching.
        var facing = Direction.orderedByNearest(placer)[0].getOpposite();
        facing = placer.isCrouching() ? facing.getOpposite() : facing;

        var hitSide = hitResult.getDirection();
        ArchitectureLog.debug("Hit side: {}", hitSide);


        return ShapeOrientation.forProperties(
                ShapeOrientationPropertyFacing.of(facing),
                ShapeOrientationPropertySpin.of(EnumSpin.NONE)
        );
    }

    @Override
    @NotNull
    public ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return this.properties;
    }

}
