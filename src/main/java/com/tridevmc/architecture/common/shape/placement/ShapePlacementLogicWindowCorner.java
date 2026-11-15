package com.tridevmc.architecture.common.shape.placement;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.EnumRelativeDirection;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationProperty;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyAxis;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyHorizontalFacing;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyRelativeConnection;
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
        // Multiply by 4 for the direction, then 3 for the axis
        var orientations = new ShapeOrientation[16 * 4 * Direction.Axis.values().length];
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            for (Direction.Axis axis : Direction.Axis.values()) {
                for (int connections = 0; connections < 16; connections++) {
                    var index = (((facing.ordinal() - 2) * 3 + axis.ordinal()) << 4) | connections;
                    orientations[index] = ShapeOrientation.forProperties(
                            ShapeOrientationPropertyRelativeConnection.of(EnumRelativeDirection.BOTTOM, (connections & 1) != 0),
                            ShapeOrientationPropertyRelativeConnection.of(EnumRelativeDirection.TOP, (connections & 2) != 0),
                            ShapeOrientationPropertyRelativeConnection.of(EnumRelativeDirection.RIGHT, (connections & 4) != 0),
                            ShapeOrientationPropertyRelativeConnection.of(EnumRelativeDirection.LEFT, (connections & 8) != 0),
                            ShapeOrientationPropertyHorizontalFacing.of(facing),
                            ShapeOrientationPropertyAxis.of(axis)
                    );
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
            ShapeOrientationPropertyHorizontalFacing.INSTANCE,
            ShapeOrientationPropertyAxis.INSTANCE
    );

    @Override
    public @NotNull ShapeOrientation getShapeOrientationForPlacement(
            @NotNull T beingPlaced,
            @NotNull Level level,
            @NotNull BlockPos placementPos,
            @NotNull Player placer,
            @NotNull BlockHitResult hitResult) {
        // The axis supplies the tilt; the facing must stay horizontal for this state representation.
        var facing = placer.getDirection().getOpposite();

        var axis = placer.isCrouching() ? hitResult.getDirection().getAxis() : Direction.Axis.Y;

        return CACHE.get((((facing.ordinal() - 2) * 3 + axis.ordinal()) << 4));
    }

    @Override
    @NotNull
    public ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return this.properties;
    }
}