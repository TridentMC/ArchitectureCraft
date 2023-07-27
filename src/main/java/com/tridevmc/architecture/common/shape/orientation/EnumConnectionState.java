package com.tridevmc.architecture.common.shape.orientation;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.StringRepresentable;

import java.util.Collection;

public enum EnumConnectionState implements StringRepresentable {
    DISCONNECTED(0, "disconnected"),
    CONNECTED(1, "connected");

    private static final EnumConnectionState[] VALUES = new EnumConnectionState[]{
            DISCONNECTED, CONNECTED
    };

    private static final ImmutableCollection<EnumConnectionState> VALUES_COLLECTION = ImmutableSet.copyOf(
            VALUES
    );
    private final int id;
    private final String name;

    EnumConnectionState(int id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * Gets the EnumConnectionState with the given index.
     *
     * @param index the index of the EnumConnectionState to get.
     * @return the EnumConnectionState with the given index.
     */
    public static EnumConnectionState byIndex(int index) {
        return VALUES[index % VALUES.length];
    }

    /**
     * Gets an immutable collection of all EnumConnectionStates.
     *
     * @return an immutable collection of all EnumConnectionStates.
     */
    public static Collection<EnumConnectionState> getValues() {
        return VALUES_COLLECTION;
    }

    /**
     * Gets the name of this EnumConnectionState.
     *
     * @return the name of this EnumConnectionState.
     */
    @Override
    public String getSerializedName() {
        return this.name;
    }

    /**
     * Gets the ID of this EnumConnectionState.
     *
     * @return the ID of this EnumConnectionState.
     */
    public int getId() {
        return this.id;
    }
}
