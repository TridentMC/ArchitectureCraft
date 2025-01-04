package com.tridevmc.architecture.common.shape.orientation;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableSet;
import net.minecraft.util.StringRepresentable;

import java.util.Collection;

/**
 * Used to determine if a shape should be rotated 90 degrees on the X axis before the facing rotation is applied.
 */
public enum EnumTurn implements StringRepresentable {
    UNTURNED("none", 0),
    TURNED("turned", 1);

    private static final EnumTurn[] VALUES = new EnumTurn[]{
            UNTURNED, TURNED
    };

    private static final ImmutableCollection<EnumTurn> VALUES_COLLECTION = ImmutableSet.copyOf(
            VALUES
    );
    private final String name;
    private final int quarterTurns;

    EnumTurn(String name, int quarterTurns) {
        this.name = name;
        this.quarterTurns = quarterTurns;
    }

    /**
     * Gets the EnumTurn with the given index.
     *
     * @param index the index of the EnumTurn to get.
     * @return the EnumTurn with the given index.
     */
    public static EnumTurn byIndex(int index) {
        return VALUES[index % VALUES.length];
    }

    /**
     * Gets an immutable collection of all EnumTurns.
     *
     * @return an immutable collection of all EnumTurns.
     */
    public static Collection<EnumTurn> getValues() {
        return VALUES_COLLECTION;
    }

    /**
     * Gets the name of this EnumTurn.
     *
     * @return the name of this EnumTurn.
     */
    @Override
    public String getSerializedName() {
        return this.name;
    }

    /**
     * Gets the number of quarter turns this EnumTurn represents.
     *
     * @return the number of quarter turns this EnumTurn represents.
     */
    public int getQuarterTurns() {
        return this.quarterTurns;
    }

    /**
     * Gets the number of degrees this EnumTurn represents.
     *
     * @return the number of degrees this EnumTurn represents.
     */
    public double getDegrees() {
        return this.quarterTurns * 90;
    }
}
