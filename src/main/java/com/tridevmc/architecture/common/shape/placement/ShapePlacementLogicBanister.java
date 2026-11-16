package com.tridevmc.architecture.common.shape.placement;

import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.block.BlockShape;
import com.tridevmc.architecture.common.block.state.BlockStateShape;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.orientation.EnumPlacementOffset;
import com.tridevmc.architecture.common.shape.orientation.EnumSpin;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationProperty;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFacing;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertySpin;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyOffset;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverPointedWithSpin;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

public final class ShapePlacementLogicBanister implements IShapePlacementLogic<BlockArchitecture> {

    public static final ShapePlacementLogicBanister INSTANCE = new ShapePlacementLogicBanister();

    private final ImmutableCollection<ShapeOrientationProperty<?>> properties =
            ImmutableList.<ShapeOrientationProperty<?>>builder()
                    .addAll(ShapePlacementLogicStairLike.INSTANCE.getProperties())
                    .add(ShapeOrientationPropertyOffset.INSTANCE).build();

    private ShapePlacementLogicBanister() {
    }

    @Override
    public @NotNull ShapeOrientation getShapeOrientationForPlacement(@NotNull BlockArchitecture block,
            @NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull BlockHitResult hit) {
        var orientation = ShapePlacementLogicOnFace.ALONG_LOOK.getShapeOrientationForPlacement(block, level, pos, player, hit);
        if (!player.isSecondaryUseActive()) {
            orientation = this.alignToSupport(level, pos, hit, orientation);
        }
        if (orientation.getValue(ShapeOrientationPropertyOffset.INSTANCE) != null) {
            return orientation;
        }
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

    private ShapeOrientation alignToSupport(Level level, BlockPos pos, BlockHitResult hit, ShapeOrientation fallback) {
        var face = hit.getDirection();
        var supportPos = pos.relative(face.getOpposite());
        var support = level.getBlockState(supportPos);
        if (support.getBlock() instanceof StairBlock) {
            var upsideDown = support.getValue(StairBlock.HALF) == Half.TOP;
            if (face == (upsideDown ? Direction.DOWN : Direction.UP)) {
                var direction = support.getValue(StairBlock.FACING);
                var stairShape = support.getValue(StairBlock.SHAPE);
                if (stairShape != StairsShape.STRAIGHT) {
                    var side = stairShape == StairsShape.INNER_LEFT
                            || stairShape == StairsShape.OUTER_LEFT
                            ? direction.getCounterClockWise() : direction.getClockWise();
                    var inner = stairShape == StairsShape.INNER_LEFT
                            || stairShape == StairsShape.INNER_RIGHT;
                    return this.alignToCorner(face, direction, side, inner,
                            hit.getLocation().subtract(Vec3.atCenterOf(supportPos)));
                }
                // Local +Y stays upright and local +Z follows the stair's ascending run.
                return ShapeOrientation.forProperties(
                        ShapeOrientationPropertyFacing.of(upsideDown
                                ? direction.getCounterClockWise() : direction.getClockWise()),
                        ShapeOrientationPropertySpin.of(upsideDown ? EnumSpin.HALF : EnumSpin.NONE));
            }
        } else if (support instanceof BlockStateShape shape) {
            var transform = shape.getTransform();
            var up = transform.transformDirection(Direction.UP);
            if (shape.getOrientation().getValue(ShapeOrientationPropertyOffset.INSTANCE) != null
                    && up.getAxis() == face.getAxis()) {
                return shape.getOrientation();
            }
            if (up == face && shape.getBlock() instanceof BlockShape<?> block) {
                // Stairs rise along local X; the banister's run is local Z.
                var forward = transform.transformDirection(Direction.EAST);
                switch (block.getShape()) {
                    case STAIRS -> {
                        return ShapePlacementLogicOnFace.along(up, forward);
                    }
                    case STAIRS_INNER_CORNER, STAIRS_OUTER_CORNER -> {
                        return this.alignToCorner(up, forward,
                                transform.transformDirection(Direction.SOUTH),
                                block.getShape() == EnumShape.STAIRS_INNER_CORNER,
                                hit.getLocation().subtract(Vec3.atCenterOf(supportPos)));
                    }
                }
            }
        }
        return fallback;
    }

    private ShapeOrientation alignToCorner(Direction up, Direction forward, Direction side,
            boolean inner, Vec3 hit) {
        var forwardHit = hit.dot(forward.getUnitVec3());
        var sideHit = hit.dot(side.getUnitVec3());
        var alongForward = inner ? sideHit <= forwardHit + 1E-6 : sideHit >= forwardHit - 1E-6;
        var run = alongForward ? forward : side;
        var edge = alongForward ? side : forward;
        if (inner) {
            edge = edge.getOpposite();
        }
        // The other two edges are flat treads; a sloped rail must stay on a rising edge.
        var orientation = ShapePlacementLogicOnFace.along(up, run);
        var offsetDirection = ShapeTransformationResolverPointedWithSpin.INSTANCE.resolve(orientation)
                .transformDirection(Direction.EAST);
        var values = Arrays.copyOf(orientation.properties(), orientation.properties().length + 1);
        values[values.length - 1] = ShapeOrientationPropertyOffset.of(
                offsetDirection == edge ? EnumPlacementOffset.BACK : EnumPlacementOffset.FRONT);
        return ShapeOrientation.forProperties(values);
    }

    @Override
    public @NotNull ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return this.properties;
    }
}
