package com.tridevmc.architecture.common.shape.placement;

import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationProperty;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;


public final class ShapePlacementLogicExtended<T extends BlockArchitecture> implements IShapePlacementLogic<T> {

    private final IShapePlacementLogic<T> delegate;
    private final ImmutableList<ShapeOrientationProperty<?>> properties;

    public ShapePlacementLogicExtended(@NotNull IShapePlacementLogic<T> delegate,
                                       @NotNull ShapeOrientationProperty<?>... additional) {
        this.delegate = delegate;
        this.properties = ImmutableList.<ShapeOrientationProperty<?>>builder()
                .addAll(delegate.getProperties()).add(additional).build();
    }

    @Override
    public @NotNull ShapeOrientation getShapeOrientationForPlacement(@NotNull T block, @NotNull Level level,
            @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        var orientation = this.delegate.getShapeOrientationForPlacement(block, level, pos, player, hit);
        return orientation;
    }

    @Override
    public @NotNull ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return this.properties;
    }
}
