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
import org.jetbrains.annotations.Nullable;

/**
 * An implementation of {@link IShapePlacementLogic} intended to emulate vanilla slab placement logic but providing
 * support for slabs to be placed on walls when the player is crouching.
 */
public class ShapePlacementLogicSlab implements IShapePlacementLogic<BlockArchitecture> {

    public static final ShapePlacementLogicSlab INSTANCE = new ShapePlacementLogicSlab();
    private final ImmutableCollection<ShapeOrientationProperty<?>> properties = ImmutableList.of(
            ShapeOrientationPropertyFacing.INSTANCE
    );

    @Override
    public @NotNull ShapeOrientation getShapeOrientationForPlacement(
            @NotNull BlockArchitecture beingPlaced,
            @NotNull Level level,
            @NotNull BlockPos placementPos,
            @NotNull Player placer,
            @Nullable BlockHitResult hitResult) {
        // If the player isn't crouching then we're just doing normal slab placement
        if (!placer.isCrouching()) {
            var faceClicked = hitResult.getDirection();
            return switch (faceClicked) {
                case DOWN, UP ->
                        ShapeOrientation.forProperties(ShapeOrientationPropertyFacing.of(faceClicked.getOpposite()));
                default -> ShapeOrientation.forProperties(ShapeOrientationPropertyFacing.of(
                        hitResult.getLocation().y - placementPos.getY() > 0.5 ? Direction.UP : Direction.DOWN
                ));
            };
        } else {
            // If the player is crouching then we're allowing the player to place slabs on walls
            var faceClicked = hitResult.getDirection();
            return ShapeOrientation.forProperties(ShapeOrientationPropertyFacing.of(faceClicked.getOpposite()));
        }
    }

    @Override
    @NotNull
    public ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return this.properties;
    }
}
