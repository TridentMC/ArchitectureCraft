package com.tridevmc.architecture.common.shape.placement;

import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Defines a set of rules for placing a shape in the world, returning a {@link com.tridevmc.architecture.common.shape.orientation.ShapeOrientation} for the shape to be placed.
 */
@FunctionalInterface
public interface IShapePlacementLogic<T extends BlockArchitecture> {

    /**
     * Gets the {@link com.tridevmc.architecture.common.shape.orientation.ShapeOrientation} for the shape to be placed at the given position.
     *
     * @param beingPlaced  the shape being placed, usually a reference to the block.
     * @param level        the level the shape is being placed in.
     * @param placementPos the position the shape is being placed at.
     * @param placer       the player placing the shape.
     * @param hitResult    the hit result of the placement, may be null.
     * @return the orientation for the shape to be placed.
     */
    @NotNull
    ShapeOrientation getShapeOrientationForPlacement(
            @NotNull T beingPlaced,
            @NotNull Level level,
            @NotNull BlockPos placementPos,
            @NotNull Player placer,
            @Nullable BlockHitResult hitResult
    );

}