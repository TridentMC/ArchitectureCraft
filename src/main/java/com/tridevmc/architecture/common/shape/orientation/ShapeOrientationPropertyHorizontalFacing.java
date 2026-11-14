package com.tridevmc.architecture.common.shape.orientation;

import net.minecraft.core.Direction;

import java.util.Objects;

public final class ShapeOrientationPropertyHorizontalFacing extends ShapeOrientationProperty<Direction> {

    public static final ShapeOrientationPropertyHorizontalFacing INSTANCE = new ShapeOrientationPropertyHorizontalFacing();

    private ShapeOrientationPropertyHorizontalFacing() {
        super("horizontal_facing", Direction.class, Direction.Plane.HORIZONTAL.stream().toList());
    }

    public static Value<Direction> of(Direction facing) {
        return Objects.requireNonNull(INSTANCE.findValue(facing), "Horizontal facing required");
    }
}
