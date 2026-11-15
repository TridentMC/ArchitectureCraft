package com.tridevmc.architecture.common.shape.placement;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.EnumRelativeDirection;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationProperty;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyAxis;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyRelativeConnection;
import com.tridevmc.architecture.common.shape.rule.INeighbourConnectionRule;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

/**
 * An implementation of {@link IShapePlacementLogic} for placing window frames that are always flat.
 * <p>
 * Includes 5 properties, one for each relative direction's connection state, and one for the axis of the window.
 */
public class ShapePlacementLogicWindowFrame<T extends BlockArchitecture & INeighbourConnectionRule> implements IShapePlacementLogic<T> {

    private static final ImmutableList<ShapeOrientation> CACHE;

    static {
        // We have 4 connection states, so 2^4 = 16 permutations
        // Adding two more bits for the axis enum
        var orientations = new ShapeOrientation[16 * Direction.Axis.values().length];
        for (Direction.Axis axis : Direction.Axis.values()) {
            for (int connections = 0; connections < 16; connections++) {
                orientations[(axis.ordinal() << 4) | connections] = ShapeOrientation.forProperties(
                        ShapeOrientationPropertyRelativeConnection.of(EnumRelativeDirection.BOTTOM, (connections & 1) != 0),
                        ShapeOrientationPropertyRelativeConnection.of(EnumRelativeDirection.TOP, (connections & 2) != 0),
                        ShapeOrientationPropertyRelativeConnection.of(EnumRelativeDirection.RIGHT, (connections & 4) != 0),
                        ShapeOrientationPropertyRelativeConnection.of(EnumRelativeDirection.LEFT, (connections & 8) != 0),
                        ShapeOrientationPropertyAxis.of(axis)
                );
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
            @NotNull BlockHitResult hitResult) {
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
        return CACHE.get((axis.ordinal() << 4));
    }

    @Override
    @NotNull
    public ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return this.properties;
    }
}
