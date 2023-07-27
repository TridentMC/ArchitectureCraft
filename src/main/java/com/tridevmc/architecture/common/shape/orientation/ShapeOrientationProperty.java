package com.tridevmc.architecture.common.shape.orientation;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

/**
 * Defines a property used for orienting a shape in the world, combined with other properties to form a {@link ShapeOrientation}.
 */
public class ShapeOrientationProperty<T extends Enum<T> & StringRepresentable> extends EnumProperty<T> {

    private final Collection<Value<T>> values;

    protected ShapeOrientationProperty(String name, Class<T> type, Collection<T> values) {
        super(name, type, values);
        this.values = values.stream().map(v -> new Value<>(this, v)).toList();
    }

    /**
     * Gets a value object for the given value.
     *
     * @param value the value to get the object for.
     * @return the value object, or null if not found.
     */
    @Nullable
    protected Value<T> findValue(T value) {
        return this.values.stream().filter(v -> v.value == value).findFirst().orElse(null);
    }

    protected int order() {
        return this.getName().hashCode();
    }

    /**
     * Represents a possible value for a {@link ShapeOrientationProperty}.
     *
     * @param property the property this value is for.
     * @param value    the value this object represents.
     * @param <T>      the type of the value.
     */
    public record Value<T extends Enum<T> & StringRepresentable>(
            ShapeOrientationProperty<T> property,
            T value
    ) {

        /**
         * Applies this value to a block state.
         *
         * @param state the state to apply to.
         * @return the new state.
         */
        public BlockState applyToState(BlockState state) {
            return state.setValue(this.property, this.value);
        }

    }

}
