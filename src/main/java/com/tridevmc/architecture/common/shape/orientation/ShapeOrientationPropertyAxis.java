package com.tridevmc.architecture.common.shape.orientation;

import net.minecraft.core.Direction;

import java.util.Arrays;

public class ShapeOrientationPropertyAxis extends ShapeOrientationProperty<Direction.Axis> {

    public static final ShapeOrientationPropertyAxis INSTANCE = new ShapeOrientationPropertyAxis();


    protected ShapeOrientationPropertyAxis() {
        super("axis", Direction.Axis.class, Arrays.asList(Direction.Axis.values()));
    }

    public static Value<Direction.Axis> of(Direction.Axis value) {
        return INSTANCE.findValue(value);
    }

}
