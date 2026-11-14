package com.tridevmc.architecture.common.shape.placement;

import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.EnumTurn;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationProperty;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyTurn;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import java.util.Arrays;

public final class ShapePlacementLogicExtended<T extends BlockArchitecture> implements IShapePlacementLogic<T> {

    private final IShapePlacementLogic<T> delegate;
    private final ImmutableList<ShapeOrientationProperty<?>> properties;

    public ShapePlacementLogicExtended(IShapePlacementLogic<T> delegate, ShapeOrientationProperty<?>... additional) {
        this.delegate = delegate;
        this.properties = ImmutableList.<ShapeOrientationProperty<?>>builder()
                .addAll(delegate.getProperties()).add(additional).build();
    }

    @Override
    public @NotNull ShapeOrientation getShapeOrientationForPlacement(@NotNull T block, @NotNull Level level,
            @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        var orientation = this.delegate.getShapeOrientationForPlacement(block, level, pos, player, hit);
        if (this.properties.contains(ShapeOrientationPropertyTurn.INSTANCE)) {
            var section = EnumBlockFaceSection.fromHitResult(player.getDirection(), hit);
            var values = Arrays.copyOf(orientation.properties(), orientation.properties().length + 1);
            values[values.length - 1] = ShapeOrientationPropertyTurn.of(
                    section == EnumBlockFaceSection.LEFT || section == EnumBlockFaceSection.RIGHT
                            ? EnumTurn.TURNED : EnumTurn.UNTURNED);
            return ShapeOrientation.forProperties(values);
        }
        return orientation;
    }

    @Override
    public ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return this.properties;
    }
}
