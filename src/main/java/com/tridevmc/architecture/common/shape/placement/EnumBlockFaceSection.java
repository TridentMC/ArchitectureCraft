package com.tridevmc.architecture.common.shape.placement;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.BlockHitResult;


/**
 * Enum representing the section of a block face that was hit by a raytrace, relative to the player's horizontal direction.
 * <p>
 * When looking at the UP or DOWN face of a block, the top and bottom sections are defined as further and closer to the player respectively.
 */
public enum EnumBlockFaceSection {
    TOP("Top"),
    BOTTOM("Bottom"),
    LEFT("Left"),
    RIGHT("Right"),
    MIDDLE("Middle");

    private final String name;

    EnumBlockFaceSection(String name) {
        this.name = name;
    }

    static EnumBlockFaceSection fromHitResult(Direction playerHorizontalDirection, BlockHitResult hitResult) {
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

        // Middle is defined as 0 +- 1/8
        if (Math.abs(hitCoord0) < 1 / 8D && Math.abs(hitCoord1) < 1 / 8D) {
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

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "BlockHitSection{" +
                "name='" + name + '\'' +
                '}';
    }
}
