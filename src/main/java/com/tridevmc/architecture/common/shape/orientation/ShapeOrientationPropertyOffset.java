package com.tridevmc.architecture.common.shape.orientation;

import java.util.List;

public class ShapeOrientationPropertyOffset extends ShapeOrientationProperty<EnumPlacementOffset> {

    public final static ShapeOrientationPropertyOffset INSTANCE = new ShapeOrientationPropertyOffset();

    protected ShapeOrientationPropertyOffset() {
        super("offset", EnumPlacementOffset.class, List.of(EnumPlacementOffset.FRONT, EnumPlacementOffset.BACK));
    }

    public static Value<EnumPlacementOffset> of(EnumPlacementOffset value) {
        return INSTANCE.findValue(value);
    }

}
