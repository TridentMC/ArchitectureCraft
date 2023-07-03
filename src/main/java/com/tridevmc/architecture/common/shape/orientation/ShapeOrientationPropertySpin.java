package com.tridevmc.architecture.common.shape.orientation;

public class ShapeOrientationPropertySpin extends ShapeOrientationProperty<EnumSpin> {

    public final static ShapeOrientationPropertySpin INSTANCE = new ShapeOrientationPropertySpin();

    protected ShapeOrientationPropertySpin() {
        super("spin", EnumSpin.class, EnumSpin.getValues());
    }

    public static Value<EnumSpin> of(EnumSpin value) {
        return INSTANCE.findValue(value);
    }

}
