package com.tridevmc.architecture.common.shape;

import com.tridevmc.architecture.common.block.BlockShape;
import com.tridevmc.architecture.common.block.state.BlockStateShape;
import com.tridevmc.architecture.common.shape.orientation.EnumConnectionState;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyRelativeConnection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;


public final class ShapeConnections {

    private static final ShapeOrientationPropertyRelativeConnection[] ROOF_PROPERTIES = {
            ShapeOrientationPropertyRelativeConnection.FRONT, ShapeOrientationPropertyRelativeConnection.BACK,
            ShapeOrientationPropertyRelativeConnection.LEFT, ShapeOrientationPropertyRelativeConnection.RIGHT};
    private static final Direction[] ROOF_DIRECTIONS = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    private ShapeConnections() {
    }

    public static BlockState update(BlockState state, BlockGetter level, BlockPos pos) {
        var shape = ((BlockShape<?>) state.getBlock()).getShape();
        if (shape == EnumShape.ROOF_SMART_RIDGE) {
            for (int i = 0; i < ROOF_DIRECTIONS.length; i++) {
                var direction = direction(state, ROOF_DIRECTIONS[i]);
                var other = level.getBlockState(pos.relative(direction));
                var connected = roofPortsMatch(state, other, direction);
                state = state.setValue(ROOF_PROPERTIES[i], connection(connected));
            }
        }
        return state;
    }

    private static boolean roofPortsMatch(BlockState state, BlockState other, Direction side) {
        if (!(other.getBlock() instanceof BlockShape<?> block)
                || block.getShape().getTransformationResolver() == null
                || direction(state, Direction.UP) != direction(other, Direction.UP)) {
            return false;
        }
        var shape = block.getShape();
        if (shape == EnumShape.ROOF_SMART_RIDGE) {
            return true;
        }
        return shape == EnumShape.ROOF_RIDGE
                && (direction(other, Direction.WEST) == side.getOpposite()
                || direction(other, Direction.EAST) == side.getOpposite());
    }

    private static Direction direction(BlockState state, Direction local) {
        var block = (BlockShape<?>) state.getBlock();
        return block.getShape().getTransformationResolver().resolve((BlockStateShape) state).transformDirection(local);
    }


    private static EnumConnectionState connection(boolean connected) {
        return connected ? EnumConnectionState.CONNECTED : EnumConnectionState.DISCONNECTED;
    }
}
