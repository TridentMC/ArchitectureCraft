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

public class ShapePlacementLogicWindowCorner<T extends BlockArchitecture & INeighbourConnectionRule> implements IShapePlacementLogic<T> {

    public static final ImmutableList<ShapeOrientation> CACHE;

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

        // Multiply by 4 for the direction, then 3 for the axis
        var orientations = new ShapeOrientation[connectionPermutations.length * 4 * 3];
        for (Direction dir : Direction.Plane.HORIZONTAL.stream().toList()) {
            for (Direction.Axis axis : Direction.Axis.values()) {
                for (boolean[] connectionPermutation : connectionPermutations) {
                    var orientation = new ShapeOrientation(
                            ShapeOrientationPropertyRelativeConnection.of(EnumRelativeDirection.BOTTOM, connectionPermutation[0]),
                            ShapeOrientationPropertyRelativeConnection.of(EnumRelativeDirection.TOP, connectionPermutation[1]),
                            ShapeOrientationPropertyRelativeConnection.of(EnumRelativeDirection.RIGHT, connectionPermutation[2]),
                            ShapeOrientationPropertyRelativeConnection.of(EnumRelativeDirection.LEFT, connectionPermutation[3]),
                            ShapeOrientationPropertyFacing.of(dir),
                            ShapeOrientationPropertyAxis.of(axis)
                    );
                    var lookupIndex = 0;
                    for (int i = 0; i < connectionPermutation.length; i++) {
                        if (connectionPermutation[i]) {
                            lookupIndex |= 1 << i;
                        }
                    }
                    lookupIndex |= (((dir.ordinal() - 2) * 3) + axis.ordinal()) << 4;
                    orientations[lookupIndex] = orientation;
                }
            }
        }

        CACHE = ImmutableList.copyOf(orientations);
    }

    private final ImmutableList<ShapeOrientationProperty<?>> properties = ImmutableList.of(
            ShapeOrientationPropertyRelativeConnection.forRelativeDirection(EnumRelativeDirection.BOTTOM),
            ShapeOrientationPropertyRelativeConnection.forRelativeDirection(EnumRelativeDirection.TOP),
            ShapeOrientationPropertyRelativeConnection.forRelativeDirection(EnumRelativeDirection.RIGHT),
            ShapeOrientationPropertyRelativeConnection.forRelativeDirection(EnumRelativeDirection.LEFT),
            ShapeOrientationPropertyFacing.INSTANCE,
            ShapeOrientationPropertyAxis.INSTANCE
    );

    @Override
    public @NotNull ShapeOrientation getShapeOrientationForPlacement(
            @NotNull T beingPlaced,
            @NotNull Level level,
            @NotNull BlockPos placementPos,
            @NotNull Player placer,
            @NotNull BlockHitResult hitResult) {
        // Determine the axis from the player's look direction, only using the Y axis if the player is crouching
        var nearestDirections = Direction.orderedByNearest(placer);
        var nearest = nearestDirections[0].getOpposite();
        var facing = nearest;

        // Determine the axis but only when the player is crouching, otherwise assume X.
        var axis = Direction.Axis.X;
        if (placer.isCrouching()) {
            // Determine based on the side of the block the player is looking at.
            var hitFace = hitResult.getDirection();
            // Try and be intuitive for the placement, even if the code here might look a bit odd.
            axis = switch (hitFace) {
                case UP, DOWN ->
                        Direction.Axis.X; // If we click the top or bottom of a block then use the default axis of X
                case NORTH, SOUTH ->
                        Direction.Axis.Z; // If we click the north or south face of a block then use the Z axis
                case EAST, WEST -> Direction.Axis.Y; // If we click the east or west face of a block then use the Y axis
            };
        }

        // Determine the rotation for the connections
        var rotationForConnections = switch (axis) {
            case X -> facing;
            case Y -> facing.getCounterClockWise(Direction.Axis.X);
            case Z -> facing.getCounterClockWise();
        };

        // We connect on the TOP, BOTTOM, LEFT, and RIGHT faces.
        var lookupIndex = 0;
        lookupIndex |= isConnectedOnRelative(beingPlaced, level, placementPos, EnumRelativeDirection.BOTTOM, rotationForConnections) ? 1 : 0;
        lookupIndex |= isConnectedOnRelative(beingPlaced, level, placementPos, EnumRelativeDirection.TOP, rotationForConnections) ? 2 : 0;
        lookupIndex |= isConnectedOnRelative(beingPlaced, level, placementPos, EnumRelativeDirection.RIGHT, rotationForConnections) ? 4 : 0;
        lookupIndex |= isConnectedOnRelative(beingPlaced, level, placementPos, EnumRelativeDirection.LEFT, rotationForConnections) ? 8 : 0;
        lookupIndex |= (((facing.ordinal() - 2) * 3) + axis.ordinal()) << 4;

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