package com.tridevmc.architecture.common.shape.placement;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class ShapePlacementLogicStairLikeWithFlip implements IShapePlacementLogic<BlockArchitecture> {

    public static final ShapePlacementLogicStairLikeWithFlip INSTANCE = new ShapePlacementLogicStairLikeWithFlip();
    private final ImmutableCollection<ShapeOrientationProperty<?>> properties = ImmutableList.of(
            ShapeOrientationPropertyFlip.INSTANCE,
            ShapeOrientationPropertyFacing.INSTANCE
    );

    @Override
    public @NotNull ShapeOrientation getShapeOrientationForPlacement(@NotNull BlockArchitecture beingPlaced, @NotNull Level level, @NotNull BlockPos placementPos, @NotNull Player placer, @NotNull BlockHitResult hitResult) {
        var faceClicked = hitResult.getDirection();
        return ShapeOrientation.forProperties(
                ShapeOrientationPropertyFacing.of(faceClicked.getOpposite()),
                ShapeOrientationPropertyFlip.of(placer.isCrouching() ? EnumFlip.FLIPPED : EnumFlip.STANDARD)
        );
    }

    @Override
    public @NotNull ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return this.properties;
    }
}
