package com.tridevmc.architecture.common.shape.placement;

import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyRelativeConnection;


import java.util.List;

public final class ShapeConnectionProperties {

    public static final ShapeOrientationPropertyRelativeConnection DOWN_LEFT = new Diagonal("down_left");
    public static final ShapeOrientationPropertyRelativeConnection DOWN_RIGHT = new Diagonal("down_right");
    public static final ShapeOrientationPropertyRelativeConnection UP_LEFT = new Diagonal("up_left");
    public static final ShapeOrientationPropertyRelativeConnection UP_RIGHT = new Diagonal("up_right");
    public static final List<ShapeOrientationPropertyRelativeConnection> DIAGONALS = List.of(
            DOWN_LEFT, DOWN_RIGHT, UP_LEFT, UP_RIGHT);

    private ShapeConnectionProperties() {
    }

    private static final class Diagonal extends ShapeOrientationPropertyRelativeConnection {
        private Diagonal(String name) {
            super(name);
        }
    }

}
