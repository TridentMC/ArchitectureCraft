package com.tridevmc.architecture.common.shape.orientation;

import net.minecraft.core.Direction;

import java.util.Arrays;
import java.util.Collection;

public class ShapeOrientationPropertyFacing extends ShapeOrientationProperty<Direction> {

    private static final ShapeOrientationPropertyFacing INSTANCE = new ShapeOrientationPropertyFacing();

    private static final Collection<Direction> ALL_DIRECTIONS = Arrays.asList(Direction.values());

    protected ShapeOrientationPropertyFacing() {
        super("spin", Direction.class, ALL_DIRECTIONS);
    }

    public static ShapeOrientationPropertyFacing getInstance() {
        return INSTANCE;
    }

    public static Value<Direction> of(Direction value) {
        return INSTANCE.findValue(value);
    }

}
