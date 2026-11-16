package com.tridevmc.architecture.common.shape.placement;

import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationProperty;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverPointedWithSpin;

import com.google.common.collect.ImmutableCollection;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public final class ShapePlacementLogicRounded implements IShapePlacementLogic<BlockArchitecture> {

    public static final ShapePlacementLogicRounded CORNER = new ShapePlacementLogicRounded(ShapePlacementLogicOnFace.CORNER);
    public static final ShapePlacementLogicRounded EDGE = new ShapePlacementLogicRounded(ShapePlacementLogicOnFace.EDGE);

    private final IShapePlacementLogic<BlockArchitecture> placement;

    private ShapePlacementLogicRounded(IShapePlacementLogic<BlockArchitecture> placement) {
        this.placement = placement;
    }

    @Override
    public @NotNull ShapeOrientation getShapeOrientationForPlacement(
            @NotNull BlockArchitecture block, @NotNull Level level, @NotNull BlockPos pos,
            @NotNull Player player, @NotNull BlockHitResult hit) {
        var orientation = this.placement.getShapeOrientationForPlacement(block, level, pos, player, hit);
        var transform = ShapeTransformationResolverPointedWithSpin.INSTANCE.resolve(orientation);
        // Rounded quarters have their cap on +X; the face picker expects its edge on +Z.
        return ShapePlacementLogicOnFace.along(
                transform.transformDirection(Direction.UP), transform.transformDirection(Direction.WEST));
    }

    @Override
    public @NotNull ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return this.placement.getProperties();
    }
}
