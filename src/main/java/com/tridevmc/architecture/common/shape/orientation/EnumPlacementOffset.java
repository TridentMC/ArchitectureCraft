package com.tridevmc.architecture.common.shape.orientation;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.StringRepresentable;

import java.util.Collection;

public enum EnumPlacementOffset implements StringRepresentable {
    FRONT(0, "front", -0.5),
    MIDDLE(1, "middle", 0.0),
    BACK(2, "back", 0.5);

    private static final EnumPlacementOffset[] VALUES = new EnumPlacementOffset[]{
            FRONT, MIDDLE, BACK
    };

    private static final ImmutableCollection<EnumPlacementOffset> VALUES_COLLECTION = ImmutableSet.copyOf(
            VALUES
    );

    /**
     * Gets the EnumPlacementOffset with the given index.
     *
     * @param index the index of the EnumPlacementOffset to get.
     * @return the EnumPlacementOffset with the given index.
     */
    public static EnumPlacementOffset byIndex(int index) {
        return VALUES[index % VALUES.length];
    }

    /**
     * Gets an immutable collection of all EnumPlacementOffsets.
     *
     * @return an immutable collection of all EnumPlacementOffsets.
     */
    public static Collection<EnumPlacementOffset> getValues() {
        return VALUES_COLLECTION;
    }

    private final int id;
    private final String name;
    private final double offset;

    EnumPlacementOffset(int id, String name, double offset) {
        this.id = id;
        this.name = name;
        this.offset = offset;
    }

    /**
     * Gets the name of this EnumPlacementOffset.
     *
     * @return the name of this EnumPlacementOffset.
     */
    @Override
    public String getSerializedName() {
        return this.name;
    }

    /**
     * Gets the ID of this EnumPlacementOffset.
     *
     * @return the ID of this EnumPlacementOffset.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Gets the offset of this EnumPlacementOffset.
     *
     * @return the offset of this EnumPlacementOffset.
     */
    public double getOffset() {
        return this.offset;
    }
}