package com.tridevmc.architecture.common.shape.placement;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationProperty;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyAxis;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyConnection;
import com.tridevmc.architecture.common.shape.rule.INeighbourConnectionRule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An implementation of {@link IShapePlacementLogic} that places blocks in a window-like fashion.
 * <p>
 * A window can be facing any direction, and connects to other neighbouring windows.
 */
public class ShapePlacementLogicWindow<T extends BlockArchitecture & INeighbourConnectionRule> implements IShapePlacementLogic<T> {

    private static final ImmutableList<ShapeOrientation> CACHE;

    static {
        // We cache all the orientations here instead of in the ShapeOrientation class as that would incur
        // allocations for every lookup. Instead, we store them here and allocate only once.
        // We have 64 possible permutations of the 6 connection states, then we have two enums each with 3 possible values.
        // This can be stored in an int with the first 6 bits for the connection states, and the last 2 bits for the enum value.

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

        var orientations = new ShapeOrientation[64 * 3];
        for (Direction.Axis axis : Direction.Axis.values()) {
            for (boolean[] connectionPermutation : connectionPermutations) {
                var orientation = new ShapeOrientation(
                        ShapeOrientationPropertyAxis.of(axis),
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
                // Store the enum in the last two bits
                lookup |= axis.ordinal() << 6;
                orientations[lookup] = orientation;
            }
        }

        CACHE = ImmutableList.copyOf(orientations);
    }

    private final ImmutableCollection<ShapeOrientationProperty<?>> properties = ImmutableList.of(
            ShapeOrientationPropertyAxis.INSTANCE,
            ShapeOrientationPropertyConnection.DOWN,
            ShapeOrientationPropertyConnection.UP,
            ShapeOrientationPropertyConnection.NORTH,
            ShapeOrientationPropertyConnection.SOUTH,
            ShapeOrientationPropertyConnection.WEST,
            ShapeOrientationPropertyConnection.EAST
    );

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
        if (!placer.isCrouching() && axis == Direction.Axis.Y) {
            // We're trying to avoid allocations, so just loop with an index and not a for-each
            //noinspection ForLoopReplaceableByForEach
            for (int i = 0; i < nearestDirections.length; i++) {
                if (nearestDirections[i].getAxis() != Direction.Axis.Y) {
                    axis = nearestDirections[i].getAxis();
                    break;
                }
            }
        }

        // Finally, determine the connections based on the neighbouring blocks
        var lookup = 0;
        for (int i = 0; i < Direction.values().length; i++) {
            var dir = Direction.from3DDataValue(i);
            var neighbour = level.getBlockState(placementPos.relative(dir));
            if (beingPlaced.connectsToOnSide(neighbour, dir)) {
                lookup |= 1 << i;
            }
        }
        lookup |= axis.ordinal() << 6;

        return CACHE.get(lookup);
    }

    @Override
    @NotNull
    public ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return this.properties;
    }
}
