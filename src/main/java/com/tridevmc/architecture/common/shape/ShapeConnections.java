package com.tridevmc.architecture.common.shape;

import com.tridevmc.architecture.common.block.BlockShape;
import com.tridevmc.architecture.common.block.state.BlockStateShape;
import com.tridevmc.architecture.common.shape.orientation.EnumConnectionState;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyRelativeConnection;
import com.tridevmc.architecture.common.shape.placement.ShapeConnectionProperties;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;

import java.util.EnumSet;

public final class ShapeConnections {

    private static final ShapeOrientationPropertyRelativeConnection[] WINDOW_PROPERTIES = {
            ShapeOrientationPropertyRelativeConnection.BOTTOM, ShapeOrientationPropertyRelativeConnection.RIGHT,
            ShapeOrientationPropertyRelativeConnection.TOP, ShapeOrientationPropertyRelativeConnection.LEFT};
    private static final ShapeOrientationPropertyRelativeConnection[] ROOF_PROPERTIES = {
            ShapeOrientationPropertyRelativeConnection.FRONT, ShapeOrientationPropertyRelativeConnection.BACK,
            ShapeOrientationPropertyRelativeConnection.LEFT, ShapeOrientationPropertyRelativeConnection.RIGHT};
    private static final Direction[] ROOF_DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    private ShapeConnections() {
    }

    public static BlockState update(BlockState state, BlockGetter level, BlockPos pos) {
        var shape = ((BlockShape<?>) state.getBlock()).getShape();
        if (ShapeMeshes.isWindow(shape)) {
            return updateWindow(state, level, pos);
        }
        if (shape == EnumShape.STAIRS_SMART) {
            return updateStairs(state, level, pos);
        }
        if (shape == EnumShape.ROOF_SMART_RIDGE || shape == EnumShape.ROOF_SMART_VALLEY) {
            for (int i = 0; i < ROOF_DIRECTIONS.length; i++) {
                var direction = direction(state, ROOF_DIRECTIONS[i]);
                var other = level.getBlockState(pos.relative(direction));
                var connected = roofPortsMatch(state, other, direction);
                state = state.setValue(ROOF_PROPERTIES[i], connection(connected));
            }
        }
        return state;
    }

    public static boolean roofPortsMatch(BlockState state, BlockState other, Direction side) {
        if (!(other.getBlock() instanceof BlockShape<?> block)
                || direction(state, Direction.UP) != direction(other, Direction.UP)) {
            return false;
        }
        var ridge = ((BlockShape<?>) state.getBlock()).getShape() == EnumShape.ROOF_SMART_RIDGE;
        var shape = block.getShape();
        if (shape == (ridge ? EnumShape.ROOF_SMART_RIDGE : EnumShape.ROOF_SMART_VALLEY)) {
            return true;
        }
        return shape == (ridge ? EnumShape.ROOF_RIDGE : EnumShape.ROOF_VALLEY)
                && (direction(other, Direction.WEST) == side.getOpposite()
                || direction(other, Direction.EAST) == side.getOpposite());
    }

    public static Direction direction(BlockState state, Direction local) {
        var block = (BlockShape<?>) state.getBlock();
        return block.getShape().getTransformationResolver().resolve((BlockStateShape) state).transformDirection(local);
    }

    public static Direction[] windowDirections(BlockState state) {
        var corner = ((BlockShape<?>) state.getBlock()).getShape() == EnumShape.WINDOW_CORNER;
        var local = new Direction[]{Direction.DOWN, corner ? Direction.SOUTH : Direction.EAST, Direction.UP, Direction.WEST};
        for (int i = 0; i < local.length; i++) {
            local[i] = direction(state, local[i]);
        }
        return local;
    }

    private static BlockState updateWindow(BlockState state, BlockGetter level, BlockPos pos) {
        var directions = windowDirections(state);
        for (int i = 0; i < directions.length; i++) {
            var connected = windowsConnect(state, level.getBlockState(pos.relative(directions[i])), directions[i]);
            state = state.setValue(WINDOW_PROPERTIES[i], connection(connected));
        }
        for (int i = 0; i < 4; i++) {
            var vertical = i < 2 ? 0 : 2;
            var horizontal = (i & 1) == 0 ? 3 : 1;
            var diagonal = level.getBlockState(pos.relative(directions[vertical]).relative(directions[horizontal]));
            var connected = state.getValue(WINDOW_PROPERTIES[vertical]) == EnumConnectionState.CONNECTED
                    && state.getValue(WINDOW_PROPERTIES[horizontal]) == EnumConnectionState.CONNECTED
                    && windowsConnect(level.getBlockState(pos.relative(directions[vertical])), diagonal, directions[horizontal])
                    && windowsConnect(level.getBlockState(pos.relative(directions[horizontal])), diagonal, directions[vertical]);
            state = state.setValue(ShapeConnectionProperties.DIAGONALS.get(i), connection(connected));
        }
        return state;
    }

    public static boolean windowsConnect(BlockState state, BlockState other, Direction side) {
        if (!(other.getBlock() instanceof BlockShape<?> block) || !ShapeMeshes.isWindow(block.getShape())) {
            return false;
        }
        var ours = windowDirections(state);
        var theirs = windowDirections(other);
        for (int i = 0; i < ours.length; i++) {
            if (ours[i] != side) {
                continue;
            }
            for (int j = 0; j < theirs.length; j++) {
                if (theirs[j] == side.getOpposite()) {
                    return portProfile(state, i).equals(portProfile(other, j));
                }
            }
        }
        return false;
    }

    private record PortProfile(EnumSet<Direction.Axis> normals, EnumSet<Direction> arms) {
    }

    private static PortProfile portProfile(BlockState state, int port) {
        var corner = ((BlockShape<?>) state.getBlock()).getShape() == EnumShape.WINDOW_CORNER;
        var normals = EnumSet.noneOf(Direction.Axis.class);
        if (!corner || port != 1) {
            normals.add(direction(state, Direction.NORTH).getAxis());
        }
        if (corner && port != 3) {
            normals.add(direction(state, Direction.EAST).getAxis());
        }
        var arms = EnumSet.noneOf(Direction.class);
        if (corner && (port == 0 || port == 2)) {
            arms.add(direction(state, Direction.SOUTH));
            arms.add(direction(state, Direction.WEST));
        }
        return new PortProfile(normals, arms);
    }

    private static BlockState updateStairs(BlockState state, BlockGetter level, BlockPos pos) {
        var high = direction(state, Direction.EAST);
        var up = direction(state, Direction.UP);
        var right = direction(state, Direction.SOUTH);
        var front = level.getBlockState(pos.relative(high));
        if (isStair(front, up)) {
            var otherHigh = direction(front, Direction.EAST);
            if (otherHigh.getAxis() != high.getAxis() && !sameStair(level.getBlockState(pos.relative(otherHigh.getOpposite())), high, up)) {
                return state.setValue(ShapeConnectionProperties.STAIRS,
                        otherHigh == right ? StairsShape.OUTER_RIGHT : StairsShape.OUTER_LEFT);
            }
        }
        var back = level.getBlockState(pos.relative(high.getOpposite()));
        if (isStair(back, up)) {
            var otherHigh = direction(back, Direction.EAST);
            if (otherHigh.getAxis() != high.getAxis() && !sameStair(level.getBlockState(pos.relative(otherHigh)), high, up)) {
                return state.setValue(ShapeConnectionProperties.STAIRS,
                        otherHigh == right ? StairsShape.INNER_RIGHT : StairsShape.INNER_LEFT);
            }
        }
        return state.setValue(ShapeConnectionProperties.STAIRS, StairsShape.STRAIGHT);
    }

    private static boolean sameStair(BlockState state, Direction high, Direction up) {
        return isStair(state, up) && direction(state, Direction.EAST) == high;
    }

    private static boolean isStair(BlockState state, Direction up) {
        return state.getBlock() instanceof BlockShape<?> block && switch (block.getShape()) {
            case STAIRS, STAIRS_INNER_CORNER, STAIRS_OUTER_CORNER, STAIRS_SMART -> direction(state, Direction.UP) == up;
            default -> false;
        };
    }

    private static EnumConnectionState connection(boolean connected) {
        return connected ? EnumConnectionState.CONNECTED : EnumConnectionState.DISCONNECTED;
    }
}
