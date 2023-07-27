package com.tridevmc.architecture.common.shape.orientation;


public class ShapeOrientationPropertyRelativeConnection extends ShapeOrientationProperty<EnumConnectionState> {

    // We assume all these values are from the perspective of looking at the shape from the north side, eyes facing south.
    public static final ShapeOrientationPropertyRelativeConnection TOP = new ShapeOrientationPropertyRelativeConnection("top");
    public static final ShapeOrientationPropertyRelativeConnection BOTTOM = new ShapeOrientationPropertyRelativeConnection("bottom");
    public static final ShapeOrientationPropertyRelativeConnection LEFT = new ShapeOrientationPropertyRelativeConnection("left");
    public static final ShapeOrientationPropertyRelativeConnection RIGHT = new ShapeOrientationPropertyRelativeConnection("right");
    public static final ShapeOrientationPropertyRelativeConnection FRONT = new ShapeOrientationPropertyRelativeConnection("front");
    public static final ShapeOrientationPropertyRelativeConnection BACK = new ShapeOrientationPropertyRelativeConnection("back");

    protected ShapeOrientationPropertyRelativeConnection(String name) {
        super(name, EnumConnectionState.class, EnumConnectionState.getValues());
    }

    public static ShapeOrientationPropertyRelativeConnection forRelativeDirection(EnumRelativeDirection direction) {
        return switch (direction) {
            case TOP -> TOP;
            case BOTTOM -> BOTTOM;
            case LEFT -> LEFT;
            case RIGHT -> RIGHT;
            case FRONT -> FRONT;
            case BACK -> BACK;
        };
    }

    public static Value<EnumConnectionState> of(EnumRelativeDirection value, EnumConnectionState state) {
        return forRelativeDirection(value).findValue(state);
    }

    public static Value<EnumConnectionState> of(EnumRelativeDirection value, boolean connected) {
        return forRelativeDirection(value).findValue(connected ? EnumConnectionState.CONNECTED : EnumConnectionState.DISCONNECTED);
    }
}
