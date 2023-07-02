package com.tridevmc.architecture.common.shape.orientation;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.StringRepresentable;

import java.util.Collection;

/**
 * Used to determine how many quarters around the Z axis a shape is rotated before the facing rotation is applied.
 */
public enum EnumSpin implements StringRepresentable {
    NONE("none", 0),
    ONE_QUARTER("one_quarter", 1),
    HALF("half", 2),
    THREE_QUARTER("three_quarter", 3);

    private static final EnumSpin[] VALUES = new EnumSpin[]{
            NONE, ONE_QUARTER, HALF, THREE_QUARTER
    };

    private static final ImmutableCollection<EnumSpin> VALUES_COLLECTION = ImmutableSet.copyOf(
            VALUES
    );

    /**
     * Gets the EnumSpin with the given index.
     *
     * @param index the index of the EnumSpin to get.
     * @return the EnumSpin with the given index.
     */
    public static EnumSpin byIndex(int index) {
        return VALUES[index % VALUES.length];
    }

    /**
     * Gets an immutable collection of all EnumSpins.
     *
     * @return an immutable collection of all EnumSpins.
     */
    public static Collection<EnumSpin> getValues() {
        return VALUES_COLLECTION;
    }

    private final String name;
    private final int quarterTurns;

    EnumSpin(String name, int quarterTurns) {
        this.name = name;
        this.quarterTurns = quarterTurns;
    }

    /**
     * Gets the name of this EnumSpin.
     *
     * @return the name of this EnumSpin.
     */
    @Override
    public String getSerializedName() {
        return this.name;
    }

    /**
     * Gets the number of quarter turns this EnumSpin represents.
     *
     * @return the number of quarter turns this EnumSpin represents.
     */
    public int getQuarterTurns() {
        return this.quarterTurns;
    }

    /**
     * Gets the number of degrees this EnumSpin represents.
     *
     * @return the number of degrees this EnumSpin represents.
     */
    public double getDegrees() {
        return this.quarterTurns * 90;
    }
}
