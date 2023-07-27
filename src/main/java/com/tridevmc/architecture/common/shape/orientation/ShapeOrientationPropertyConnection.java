package com.tridevmc.architecture.common.shape.orientation;


import net.minecraft.core.Direction;

public class ShapeOrientationPropertyConnection extends ShapeOrientationProperty<EnumConnectionState> {

    public static final ShapeOrientationPropertyConnection NORTH = new ShapeOrientationPropertyConnection("north");
    public static final ShapeOrientationPropertyConnection SOUTH = new ShapeOrientationPropertyConnection("south");
    public static final ShapeOrientationPropertyConnection WEST = new ShapeOrientationPropertyConnection("west");
    public static final ShapeOrientationPropertyConnection EAST = new ShapeOrientationPropertyConnection("east");
    public static final ShapeOrientationPropertyConnection UP = new ShapeOrientationPropertyConnection("up");
    public static final ShapeOrientationPropertyConnection DOWN = new ShapeOrientationPropertyConnection("down");

    protected ShapeOrientationPropertyConnection(String name) {
        super(name, EnumConnectionState.class, EnumConnectionState.getValues());
    }

    public static ShapeOrientationPropertyConnection forDirection(Direction value) {
        return switch (value) {
            case NORTH -> NORTH;
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
            case UP -> UP;
            case DOWN -> DOWN;
        };
    }

    public static Value<EnumConnectionState> of(Direction value, EnumConnectionState state) {
        return forDirection(value).findValue(state);
    }

    public static Value<EnumConnectionState> of(Direction value, boolean connected) {
        return forDirection(value).findValue(connected ? EnumConnectionState.CONNECTED : EnumConnectionState.DISCONNECTED);
    }
}
