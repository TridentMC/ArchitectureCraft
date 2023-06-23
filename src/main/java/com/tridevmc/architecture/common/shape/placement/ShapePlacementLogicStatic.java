package com.tridevmc.architecture.common.shape.placement;

import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Simple implementation of {@link ShapePlacementLogic} that always returns {@link ShapeOrientation#IDENTITY}.
 * <p>
 * Useful for shapes that wouldn't be impacted by rotation like a sphere.
 */
public class ShapePlacementLogicStatic extends ShapePlacementLogic {

    @Override
    @NotNull
    public ShapeOrientation getShapeOrientationForPlacement(@NotNull Level level,
                                                            @NotNull BlockPos placementPos,
                                                            @NotNull Player placer,
                                                            @Nullable BlockHitResult hitResult) {
        return ShapeOrientation.IDENTITY;
    }

}
