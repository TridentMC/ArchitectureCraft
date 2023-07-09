package com.tridevmc.architecture.common.shape.placement;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.*;
import com.tridevmc.architecture.common.shape.rule.INeighbourConnectionRule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An implementation of {@link IShapePlacementLogic} for placing window frames that are always flat.
 * <p>
 * Includes 5 properties, one for each relative direction's connection state, and one for the axis of the window.
 */
public class ShapePlacementLogicWindowFrame<T extends BlockArchitecture & INeighbourConnectionRule> implements IShapePlacementLogic<T> {

    private static final ImmutableList<ShapeOrientation> CACHE;

    static {
        // We have 4 connection states, so 2^4 = 16 permutations
        var connectionPermutations = new boolean[2 * 2 * 2 * 2][];
        for (int i = 0; i < connectionPermutations.length; i++) {
            connectionPermutations[i] = new boolean[]{
                    (i & 1) != 0,
                    (i & 2) != 0,
                    (i & 4) != 0,
                    (i & 8) != 0
            };
        }

        // Adding two more bits for the axis enum
        var orientations = new ShapeOrientation[connectionPermutations.length * 2 * 2];
        for (Direction.Axis axis : Direction.Axis.values()) {
            for (boolean[] connectionPermutation : connectionPermutations) {
                var orientation = new ShapeOrientation(
                        ShapeOrientationPropertyRelativeConnection.of(EnumRelativeDirection.BOTTOM, connectionPermutation[0]),
                        ShapeOrientationPropertyRelativeConnection.of(EnumRelativeDirection.TOP, connectionPermutation[1]),
                        ShapeOrientationPropertyRelativeConnection.of(EnumRelativeDirection.RIGHT, connectionPermutation[4]),
                        ShapeOrientationPropertyRelativeConnection.of(EnumRelativeDirection.LEFT, connectionPermutation[5]),
                        ShapeOrientationPropertyAxis.of(axis)
                );
                var lookupIndex = 0;
                for (int i = 0; i < connectionPermutation.length; i++) {
                    if (connectionPermutation[i]) {
                        lookupIndex |= 1 << i;
                    }
                }
                lookupIndex |= axis.ordinal() << 4;
                orientations[lookupIndex] = orientation;
            }
        }

        CACHE = ImmutableList.copyOf(orientations);
    }

    private final ImmutableList<ShapeOrientationProperty<?>> properties = ImmutableList.of(
            ShapeOrientationPropertyRelativeConnection.forRelativeDirection(EnumRelativeDirection.BOTTOM),
            ShapeOrientationPropertyRelativeConnection.forRelativeDirection(EnumRelativeDirection.TOP),
            ShapeOrientationPropertyRelativeConnection.forRelativeDirection(EnumRelativeDirection.RIGHT),
            ShapeOrientationPropertyRelativeConnection.forRelativeDirection(EnumRelativeDirection.LEFT),
            ShapeOrientationPropertyAxis.INSTANCE
    );

    @Override
    public @NotNull ShapeOrientation getShapeOrientationForPlacement(
            @NotNull T beingPlaced,
            @NotNull Level level,
            @NotNull BlockPos placementPos,
            @NotNull Player placer,
            @Nullable BlockHitResult hitResult) {
        // Determine the axis from the player's look direction, only using the Y axis if the player is crouching
        var nearestDirections = Direction.orderedByNearest(placer);
        var nearest = nearestDirections[0].getOpposite();
        var axis = nearest.getAxis();
        if (!placer.isCrouching() && axis == Direction.Axis.Y) {
            for (int i = 0; i < nearestDirections.length; i++) {
                if (nearestDirections[i].getAxis() != Direction.Axis.Y) {
                    axis = nearestDirections[i].getAxis();
                    break;
                }
            }
        }
        var rotationForConnections = switch (axis) {
            case X -> Direction.NORTH;
            case Y -> Direction.UP;
            case Z -> Direction.EAST;
        };

        // We connect on the TOP, BOTTOM, LEFT, and RIGHT faces.
        var lookupIndex = 0;
        lookupIndex |= isConnectedOnRelative(beingPlaced, level, placementPos, EnumRelativeDirection.BOTTOM, rotationForConnections) ? 1 : 0;
        lookupIndex |= isConnectedOnRelative(beingPlaced, level, placementPos, EnumRelativeDirection.TOP, rotationForConnections) ? 2 : 0;
        lookupIndex |= isConnectedOnRelative(beingPlaced, level, placementPos, EnumRelativeDirection.RIGHT, rotationForConnections) ? 4 : 0;
        lookupIndex |= isConnectedOnRelative(beingPlaced, level, placementPos, EnumRelativeDirection.LEFT, rotationForConnections) ? 8 : 0;
        lookupIndex |= axis.ordinal() << 4;

        return CACHE.get(lookupIndex);
    }

    private boolean isConnectedOnRelative(T beingPlaced, Level level, BlockPos placementPos, EnumRelativeDirection direction, Direction facing) {
        var neigbourDirection = direction.toAbsoluteDirection(facing);
        var neighbourPos = placementPos.relative(neigbourDirection);
        var neighbourState = level.getBlockState(neighbourPos);
        return beingPlaced.connectsToOnSide(neighbourState, neigbourDirection);
    }

    @Override
    @NotNull
    public ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return this.properties;
    }
}
