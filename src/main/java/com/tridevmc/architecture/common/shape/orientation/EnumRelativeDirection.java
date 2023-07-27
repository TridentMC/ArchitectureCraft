package com.tridevmc.architecture.common.shape.orientation;

import net.minecraft.core.Direction;

/**
 * An enum used to represent a relative direction, and convert it to an absolute direction based on a given facing.
 * <p>
 * All directions are made under the assumption that the shape is being viewed from the north side, eyes facing south.
 */
public enum EnumRelativeDirection {
    BOTTOM(0, "bottom", Direction.DOWN),
    TOP(1, "top", Direction.UP),
    FRONT(2, "front", Direction.NORTH),
    BACK(3, "back", Direction.SOUTH),
    RIGHT(4, "right", Direction.EAST),
    LEFT(5, "left", Direction.WEST);

    private final int index;
    private final String name;
    private final Direction identityDirection;

    EnumRelativeDirection(int index, String name, Direction value) {
        this.index = index;
        this.name = name;
        this.identityDirection = value;
    }

    public static EnumRelativeDirection fromIndex(int index) {
        return values()[index];
    }

    public static EnumRelativeDirection fromName(String name) {
        for (EnumRelativeDirection direction : values()) {
            if (direction.getName().equals(name)) {
                return direction;
            }
        }
        return null;
    }

    public int getIndex() {
        return this.index;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Converts a relative direction to an absolute direction based on the given facing.
     *
     * @param facing the facing to convert the relative direction to.
     * @return the absolute direction.
     */
    public Direction toAbsoluteDirection(Direction facing) {
        var value = this.identityDirection;
        return switch (facing) {
            case NORTH -> value;
            case SOUTH -> value.getOpposite();
            case EAST -> value.getClockWise(Direction.Axis.Y);
            case WEST -> value.getCounterClockWise(Direction.Axis.Y);
            case UP -> value.getClockWise(Direction.Axis.X);
            case DOWN -> value.getCounterClockWise(Direction.Axis.X);
        };
    }


}
