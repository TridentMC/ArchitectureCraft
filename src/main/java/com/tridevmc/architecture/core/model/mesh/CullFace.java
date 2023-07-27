package com.tridevmc.architecture.core.model.mesh;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Used to represent the cull face of a polygon, used instead of Minecraft's Direction enum to allow for null-safe values.
 */
public enum CullFace {
    @SerializedName("-1")
    NONE(-1),
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

    private static final CullFace[] FACES_BY_INDEX = new CullFace[7];

    static {
        for (CullFace face : values()) {
            FACES_BY_INDEX[face.getIndex() + 1] = face;
        }
    }

    private final int index;

    CullFace(int index) {
        this.index = index;
    }

    /**
     * Gets the cull face from the index of the given direction.
     *
     * @param direction The direction to get the cull face for.
     * @return The cull face for the given direction.
     */
    @NotNull
    public static CullFace fromDirection(@Nullable Direction direction) {
        if (direction == null) return NONE;
        return FACES_BY_INDEX[direction.ordinal() + 1];
    }

    public int getIndex() {
        return this.index;
    }

    @Nullable
    public Direction toDirection() {
        return this.index == -1 ? null : Direction.from3DDataValue(this.index);
    }
}
