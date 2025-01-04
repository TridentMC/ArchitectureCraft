package com.tridevmc.architecture.common.shape.orientation;

public class ShapeOrientationPropertyTurn extends ShapeOrientationProperty<EnumTurn> {

    public final static ShapeOrientationPropertyTurn INSTANCE = new ShapeOrientationPropertyTurn();

    protected ShapeOrientationPropertyTurn() {
        super("turn", EnumTurn.class, EnumTurn.getValues());
    }

    public static Value<EnumTurn> of(EnumTurn value) {
        return INSTANCE.findValue(value);
    }

}
