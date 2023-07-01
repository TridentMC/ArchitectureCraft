package com.tridevmc.architecture.common.shape.orientation;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;

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

    protected Value<T> findValue(T value) {
        return this.values.stream().filter(v -> v.value == value).findFirst().orElse(null);
    }

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
