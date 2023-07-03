package com.tridevmc.architecture.common.shape.placement;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.*;
import com.tridevmc.architecture.common.shape.rule.INeighbourConnectionRule;
import com.tridevmc.architecture.core.ArchitectureLog;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

/**
 * An implementation of {@link IShapePlacementLogic} that places blocks in a window-like fashion.
 * <p>
 * A window can be facing any direction, can be offset between the front, middle, and back of a block, and connects
 * to other neighbouring windows.
 */
public class ShapePlacementLogicWindow<T extends BlockArchitecture & INeighbourConnectionRule> implements IShapePlacementLogic<T> {

    public static final ImmutableList<ShapeOrientation> CACHE;

    static {
        // We cache all the orientations here instead of in the ShapeOrientation class as that would incur
        // allocations for every lookup. Instead, we store them here and allocate only once.
        // We have 64 possible permutations of the 6 connection states, then we have two enums each with 3 possible values.
        // This can be stored in an int with the first 6 bits for the connection states, and the last 4 bits for the enums.

        // We'll start by generating all the possible connection permutations.

        var connectionPermutations = new boolean[64][];
        for (int i = 0; i < 64; i++) {
            connectionPermutations[i] = new boolean[]{
                    (i & 1) != 0,
                    (i & 2) != 0,
                    (i & 4) != 0,
                    (i & 8) != 0,
                    (i & 16) != 0,
                    (i & 32) != 0
            };
        }

        // Our two enums give a total of 9 possible values, which can be stored in 4 bits.
        // We'll use the first 2 bits for the first enum, and the last 2 bits for the second enum.

        var orientations = new ShapeOrientation[64*9];
        for (Direction.Axis axis : Direction.Axis.values()) {
            for (EnumPlacementOffset offset : EnumPlacementOffset.values()) {
                for (boolean[] connectionPermutation : connectionPermutations) {
                    var orientation = new ShapeOrientation(
                            ShapeOrientationPropertyAxis.of(axis),
                            ShapeOrientationPropertyOffset.of(offset),
                            ShapeOrientationPropertyConnection.of(Direction.DOWN, connectionPermutation[0]),
                            ShapeOrientationPropertyConnection.of(Direction.UP, connectionPermutation[1]),
                            ShapeOrientationPropertyConnection.of(Direction.NORTH, connectionPermutation[2]),
                            ShapeOrientationPropertyConnection.of(Direction.SOUTH, connectionPermutation[3]),
                            ShapeOrientationPropertyConnection.of(Direction.WEST, connectionPermutation[4]),
                            ShapeOrientationPropertyConnection.of(Direction.EAST, connectionPermutation[5])
                    );
                    var lookup = 0;
                    for (int i = 0; i < 6; i++) {
                        lookup |= (connectionPermutation[i] ? 1 : 0) << i;
                    }
                    // Store the last two enums in such a way that we don't exceed the limit of our orientation array
                    var enumLookupOffset = (axis.ordinal() * 3) + offset.ordinal();
                    lookup |= enumLookupOffset << 6;
                    orientations[lookup] = orientation;
                }
            }
        }

        CACHE = ImmutableList.copyOf(orientations);
    }

    @Override
    @NotNull
    public ShapeOrientation getShapeOrientationForPlacement(
            @NotNull T beingPlaced,
            @NotNull Level level,
            @NotNull BlockPos placementPos,
            @NotNull Player placer,
            @Nullable BlockHitResult hitResult) {
        // First we'll determine the axis of the window, which is the direction the player is looking excluding up and down unless crouching.
        var nearestDirections = Direction.orderedByNearest(placer);
        var axis = nearestDirections[0].getAxis();
        if (placer.isCrouching() && axis == Direction.Axis.Y) {
            for (int i = 0; i < nearestDirections.length; i++) {
                if (nearestDirections[i].getAxis() != Direction.Axis.Y) {
                    axis = nearestDirections[i].getAxis();
                    break;
                }
            }
        }

        // Now determine the offset, which is based on which third of the block the player clicked on.
        var hitX = hitResult != null ? hitResult.getLocation().x - placementPos.getX() : 0.5;
        var hitY = hitResult != null ? hitResult.getLocation().y - placementPos.getY() : 0.5;
        var hitZ = hitResult != null ? hitResult.getLocation().z - placementPos.getZ() : 0.5;
        var offset = switch (axis) {
            case X -> hitX < 1D / 3D
                    ? EnumPlacementOffset.FRONT
                    : hitX > 2D / 3D
                    ? EnumPlacementOffset.BACK
                    : EnumPlacementOffset.MIDDLE;
            case Y -> hitY < 1D / 3D
                    ? EnumPlacementOffset.FRONT
                    : hitY > 2D / 3D
                    ? EnumPlacementOffset.BACK
                    : EnumPlacementOffset.MIDDLE;
            case Z -> hitZ < 1D / 3D
                    ? EnumPlacementOffset.FRONT
                    : hitZ > 2D / 3D
                    ? EnumPlacementOffset.BACK
                    : EnumPlacementOffset.MIDDLE;
        };

        // Finally, determine the connections based on the neighbouring blocks
        var lookup = 0;
        for (int i = 0; i < Direction.values().length; i++) {
            var dir = Direction.from3DDataValue(i);
            var neighbour = level.getBlockState(placementPos.relative(dir));
            if (beingPlaced.connectsToOnSide(neighbour, dir)) {
                lookup |= 1 << i;
            }
        }
        lookup |= ((axis.ordinal() * 3) + offset.ordinal()) << 6;

        return CACHE.get(lookup);
    }
}
