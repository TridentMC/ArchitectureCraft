package com.tridevmc.architecture.common.shape.orientation;

import net.minecraft.core.Direction;

import java.util.Arrays;
import java.util.Collection;

public class ShapeOrientationPropertyFacing extends ShapeOrientationProperty<Direction> {

    public static final ShapeOrientationPropertyFacing INSTANCE = new ShapeOrientationPropertyFacing();


    protected ShapeOrientationPropertyFacing() {
        super("facing", Direction.class, Arrays.asList(Direction.values()));
    }

    public static Value<Direction> of(Direction value) {
        return INSTANCE.findValue(value);
    }

}
