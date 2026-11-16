package com.tridevmc.architecture.common.shape.placement;

import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.block.BlockShape;
import com.tridevmc.architecture.common.block.state.BlockStateShape;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationProperty;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverPointedWithSpin;
import com.tridevmc.architecture.core.math.ITrans3;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.NotNull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public final class ShapePlacementLogicConnected implements IShapePlacementLogic<BlockArchitecture> {

    public static final ShapePlacementLogicConnected STAIRS = new ShapePlacementLogicConnected(
            ImmutableMap.of(Direction.NORTH, Profile.LEFT, Direction.SOUTH, Profile.RIGHT));
    public static final ShapePlacementLogicConnected STAIRS_OUTER_CORNER = new ShapePlacementLogicConnected(
            ImmutableMap.of(Direction.EAST, Profile.LEFT, Direction.SOUTH, Profile.RIGHT));
    public static final ShapePlacementLogicConnected STAIRS_INNER_CORNER = new ShapePlacementLogicConnected(
            ImmutableMap.of(Direction.NORTH, Profile.LEFT, Direction.WEST, Profile.RIGHT));
    public static final ShapePlacementLogicConnected STRAIGHT = new ShapePlacementLogicConnected(
            ImmutableMap.of(Direction.EAST, Profile.LEFT, Direction.WEST, Profile.RIGHT));
    public static final ShapePlacementLogicConnected CORNER = new ShapePlacementLogicConnected(
            ImmutableMap.of(Direction.SOUTH, Profile.LEFT, Direction.WEST, Profile.RIGHT));
    public static final ShapePlacementLogicConnected ROOF = new ShapePlacementLogicConnected(
            ImmutableMap.of(Direction.NORTH, Profile.ROOF_LEFT, Direction.SOUTH, Profile.ROOF_RIGHT));
    public static final ShapePlacementLogicConnected ROOF_OUTER_CORNER = new ShapePlacementLogicConnected(
            ImmutableMap.of(Direction.EAST, Profile.ROOF_LEFT, Direction.SOUTH, Profile.ROOF_RIGHT));
    public static final ShapePlacementLogicConnected ROOF_INNER_CORNER = new ShapePlacementLogicConnected(
            ImmutableMap.of(Direction.NORTH, Profile.ROOF_LEFT, Direction.WEST, Profile.ROOF_RIGHT));
    public static final ShapePlacementLogicConnected ROOF_RIDGE = allFaces(Profile.RIDGE);
    public static final ShapePlacementLogicConnected ROOF_VALLEY = allFaces(Profile.VALLEY);

    private final ImmutableMap<Direction, Profile> profiles;

    private ShapePlacementLogicConnected(ImmutableMap<Direction, Profile> profiles) {
        this.profiles = profiles;
    }

    private static ShapePlacementLogicConnected allFaces(Profile profile) {
        var profiles = ImmutableMap.<Direction, Profile>builder();
        for (var face : Direction.values()) {
            profiles.put(face, profile);
        }
        return new ShapePlacementLogicConnected(profiles.build());
    }

    @Override
    public @NotNull ShapeOrientation getShapeOrientationForPlacement(
            @NotNull BlockArchitecture block, @NotNull Level level, @NotNull BlockPos pos,
            @NotNull Player player, @NotNull BlockHitResult hit) {
        if (!player.isSecondaryUseActive()) {
            var support = level.getBlockState(pos.relative(hit.getDirection().getOpposite()));
            if (support instanceof BlockStateShape shape
                    && shape.getBlock() instanceof BlockShape<?> neighbor
                    && neighbor.getPlacementLogic() instanceof ShapePlacementLogicConnected connection) {
                var transform = shape.getTransform();
                var profile = connection.profile(transform, hit.getDirection());
                if (profile != Profile.NONE) {
                    var up = transform.transformDirection(Direction.UP);
                    var forward = transform.transformDirection(Direction.SOUTH);
                    // Keep the neighbor's mounting plane, including ceiling and wall-mounted runs.
                    for (int turn = 0; turn < 4; turn++) {
                        var orientation = ShapePlacementLogicOnFace.along(up, forward);
                        var candidate = ShapeTransformationResolverPointedWithSpin.INSTANCE.resolve(orientation);
                        if (profile.matches(this.profile(candidate, hit.getDirection().getOpposite()))) {
                            return orientation;
                        }
                        forward = Direction.getApproximateNearest(
                                forward.getUnitVec3().cross(up.getUnitVec3()));
                    }
                }
            }
        }
        return ShapePlacementLogicStairLike.INSTANCE.getShapeOrientationForPlacement(block, level, pos, player, hit);
    }

    private Profile profile(ITrans3 transform, Direction face) {
        for (var entry : this.profiles.entrySet()) {
            if (transform.transformDirection(entry.getKey()) == face) {
                return entry.getValue();
            }
        }
        return Profile.NONE;
    }

    @Override
    public @NotNull ImmutableCollection<ShapeOrientationProperty<?>> getProperties() {
        return ShapePlacementLogicStairLike.INSTANCE.getProperties();
    }

    private enum Profile {
        NONE, LEFT, RIGHT, ROOF_LEFT, ROOF_RIGHT, RIDGE, VALLEY;

        private boolean matches(Profile other) {
            return switch (this) {
                case LEFT -> other == RIGHT;
                case RIGHT -> other == LEFT;
                case ROOF_LEFT -> other == ROOF_RIGHT;
                case ROOF_RIGHT -> other == ROOF_LEFT;
                case RIDGE, VALLEY -> other == this;
                case NONE -> false;
            };
        }
    }
}
