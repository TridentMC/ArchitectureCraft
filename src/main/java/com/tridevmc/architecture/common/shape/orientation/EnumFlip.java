package com.tridevmc.architecture.common.shape.orientation;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public enum EnumFlip implements StringRepresentable {
    STANDARD("standard"),
    FLIPPED("flipped");

    private static final EnumFlip[] VALUES = new EnumFlip[]{
            STANDARD, FLIPPED
    };

    private static final ImmutableCollection<EnumFlip> VALUES_COLLECTION = ImmutableSet.copyOf(
            VALUES
    );

    /**
     * Gets the EnumFlip with the given index.
     *
     * @param index the index of the EnumFlip to get.
     * @return the EnumFlip with the given index.
     */
    public static EnumFlip byIndex(int index) {
        return VALUES[index % VALUES.length];
    }

    /**
     * Gets an immutable collection of all EnumFlips.
     *
     * @return an immutable collection of all EnumFlips.
     */
    public static Collection<EnumFlip> getValues() {
        return VALUES_COLLECTION;
    }

    private final String name;

    EnumFlip(String name) {
        this.name = name;
    }

    @Override
    @NotNull
    public String getSerializedName() {
        return this.name;
    }
}
