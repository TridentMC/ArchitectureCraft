package com.tridevmc.architecture.common.shape.orientation;

public class ShapeOrientationPropertyOffset extends ShapeOrientationProperty<EnumPlacementOffset> {

    public final static ShapeOrientationPropertyOffset INSTANCE = new ShapeOrientationPropertyOffset();

    protected ShapeOrientationPropertyOffset() {
        super("offset", EnumPlacementOffset.class, EnumPlacementOffset.getValues());
    }

    public static Value<EnumPlacementOffset> of(EnumPlacementOffset value) {
        return INSTANCE.findValue(value);
    }

}
