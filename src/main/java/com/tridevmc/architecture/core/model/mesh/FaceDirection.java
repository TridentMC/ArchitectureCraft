package com.tridevmc.architecture.core.model.mesh;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used to represent the cull face of a polygon, used instead of Minecraft's Direction enum to allow for null-safe values.
 */
public enum FaceDirection {
    @SerializedName("0")
    DOWN(0),
    @SerializedName("1")
    UP(1),
    @SerializedName("2")
    NORTH(2),
    @SerializedName("3")
    SOUTH(3),
    @SerializedName("4")
    WEST(4),
    @SerializedName("5")
    EAST(5);

    private static final FaceDirection[] FACES_BY_INDEX = new FaceDirection[6];

    static {
        for (FaceDirection face : values()) {
            FACES_BY_INDEX[face.getIndex()] = face;
        }
    }

    /**
     * Gets the cull face from the index of the given direction.
     *
     * @param direction The direction to get the cull face for.
     * @return The cull face for the given direction.
     */
    @Nullable
    public static FaceDirection fromDirection(@Nullable Direction direction) {
        if (direction == null) return null;
        return FACES_BY_INDEX[direction.ordinal()];
    }

    private final int index;

    FaceDirection(int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    @NotNull
    public Direction toDirection() {
        return Direction.from3DDataValue(this.index);
    }
}