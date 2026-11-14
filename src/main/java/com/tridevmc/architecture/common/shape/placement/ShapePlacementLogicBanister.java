package com.tridevmc.architecture.common.shape.placement;

import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.EnumPlacementOffset;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationProperty;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyOffset;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverPointedWithSpin;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Arrays;

public final class ShapePlacementLogicBanister implements IShapePlacementLogic<BlockArchitecture> {

    public static final ShapePlacementLogicBanister INSTANCE = new ShapePlacementLogicBanister();

    @Override
    public @NotNull ShapeOrientation getShapeOrientationForPlacement(@NotNull BlockArchitecture block,
            @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        var orientation = ShapePlacementLogicStairLike.INSTANCE.getShapeOrientationForPlacement(block, level, pos, player, hit);
        var matrix = ShapeTransformationResolverPointedWithSpin.INSTANCE.resolve(orientation).matrix();
        var point = hit.getLocation();
        var localX = matrix.m00() * (point.x - pos.getX() - 0.5)
                + matrix.m10() * (point.y - pos.getY() - 0.5)
                + matrix.m20() * (point.z - pos.getZ() - 0.5);
        var values = Arrays.copyOf(orientation.properties(), orientation.properties().length + 1);
        values[values.length - 1] = ShapeOrientationPropertyOffset.of(
                localX < 0 ? EnumPlacementOffset.FRONT : EnumPlacementOffset.BACK);
        return ShapeOrientation.forProperties(values);
    }

    @Override
    public ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return ImmutableList.<ShapeOrientationProperty<?>>builder()
                .addAll(ShapePlacementLogicStairLike.INSTANCE.getProperties())
                .add(ShapeOrientationPropertyOffset.INSTANCE).build();
    }
}
