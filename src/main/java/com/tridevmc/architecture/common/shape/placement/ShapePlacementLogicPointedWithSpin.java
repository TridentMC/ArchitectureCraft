package com.tridevmc.architecture.common.shape.placement;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.*;
import com.tridevmc.architecture.core.ArchitectureLog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link IShapePlacementLogic} that points the shape based on the player's look vector, and spins
 * the shape based on the side of the block the player clicked on during placement.
 */
public class ShapePlacementLogicPointedWithSpin implements IShapePlacementLogic<BlockArchitecture> {

    public static final ShapePlacementLogicPointedWithSpin INSTANCE = new ShapePlacementLogicPointedWithSpin();
    private final ImmutableCollection<ShapeOrientationProperty<?>> properties = ImmutableList.of(
            ShapeOrientationPropertyFacing.INSTANCE,
            ShapeOrientationPropertySpin.INSTANCE
    );

    @Override
    public @NotNull ShapeOrientation getShapeOrientationForPlacement(
            @NotNull BlockArchitecture beingPlaced,
            @NotNull Level level,
            @NotNull BlockPos placementPos,
            @NotNull Player placer,
            @NotNull BlockHitResult hitResult) {
        // Pointed can point any direction, and is determined by the player's look vector.
        // If the player is crouching, then the orientation will point towards their look vector instead of at the player.
        // The spin is determined by the side of the block the player clicked on, again this is reversed if the player is crouching.
        var facing = Direction.orderedByNearest(placer)[0].getOpposite();
        facing = placer.isCrouching() ? facing.getOpposite() : facing;

        var hitSide = hitResult.getDirection();
        ArchitectureLog.debug("Hit side: {}", hitSide);


        return ShapeOrientation.forProperties(
                ShapeOrientationPropertyFacing.of(facing),
                ShapeOrientationPropertySpin.of(EnumSpin.NONE)
        );
    }

    @Override
    @NotNull
    public ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return this.properties;
    }

}
