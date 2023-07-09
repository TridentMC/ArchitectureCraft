package com.tridevmc.architecture.common.shape.placement;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationProperty;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFacing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link IShapePlacementLogic} that points the shape based on the player's look vector.
 */
public class ShapePlacementLogicPointed implements IShapePlacementLogic<BlockArchitecture> {

    public static final ShapePlacementLogicPointed INSTANCE = new ShapePlacementLogicPointed();
    private final ImmutableCollection<ShapeOrientationProperty<?>> properties = ImmutableList.of(
            ShapeOrientationPropertyFacing.INSTANCE
    );

    @Override
    @NotNull
    public ShapeOrientation getShapeOrientationForPlacement(
            @NotNull BlockArchitecture beingPlaced,
            @NotNull Level level,
            @NotNull BlockPos placementPos,
            @NotNull Player placer,
            @NotNull BlockHitResult hitResult) {
        // Pointed can point any direction, and is determined by the player's look vector.
        // If the player is crouching, then the orientation will point towards their look vector instead of at the player.
        Direction nearest = Direction.orderedByNearest(placer)[0].getOpposite();
        return ShapeOrientation.forProperties(
                ShapeOrientationPropertyFacing.of(placer.isCrouching() ? nearest.getOpposite() : nearest)
        );
    }

    @Override
    @NotNull
    public ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return this.properties;
    }


}
