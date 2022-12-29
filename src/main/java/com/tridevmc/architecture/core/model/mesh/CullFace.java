package com.tridevmc.architecture.core.model.mesh;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.Direction;
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

    private final int index;

    CullFace(int index) {
        this.index = index;
    }

    public int getIndex() {
        return this.index;
    }

    @Nullable
    public Direction toDirection() {
        return this.index == -1 ? null : Direction.from3DDataValue(this.index);
    }
}
