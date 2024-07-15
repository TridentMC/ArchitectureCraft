package com.tridevmc.architecture.common.shape.orientation;

public class ShapeOrientationPropertyFlip extends ShapeOrientationProperty<EnumFlip> {
    public final static ShapeOrientationPropertyFlip INSTANCE = new ShapeOrientationPropertyFlip();

    protected ShapeOrientationPropertyFlip() {
        super("flip", EnumFlip.class, EnumFlip.getValues());
    }

    public static Value<EnumFlip> of(EnumFlip value) {
        return INSTANCE.findValue(value);
    }
}
