package com.tridevmc.architecture.common.shape.orientation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Defines a property used for orienting a shape in the world, combined with other properties to form a {@link ShapeOrientation}.
 */
public class ShapeOrientationProperty<T extends Enum<T> & StringRepresentable> extends Property<T> {

    private final ImmutableList<Value<T>> values;
    private final ImmutableMap<String, T> nameToValue;
    private final ImmutableList<T> possibleValues;
    private final int[] ordinalToValueIndex;

    protected ShapeOrientationProperty(String name, Class<T> type, Collection<T> values) {
        super(name, type);
        this.values = ImmutableList.copyOf(values.stream().map(v -> new Value<>(this, v)).toList());
        this.nameToValue = this.values.stream().collect(ImmutableMap.toImmutableMap(v -> v.value.getSerializedName(), Value::value));
        this.possibleValues = ImmutableList.copyOf(this.values.stream().map(Value::value).toList());
        this.ordinalToValueIndex = new int[values.size()];
        for (int i = 0; i < this.values.size(); i++) {
            this.ordinalToValueIndex[this.values.get(i).value.ordinal()] = i;
        }
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

    @Override
    public List<T> getPossibleValues() {
        return this.possibleValues;
    }

    @Override
    public String getName(T t) {
        return t.getSerializedName();
    }

    @Override
    public Optional<T> getValue(String s) {
        return Optional.ofNullable(this.nameToValue.get(s));
    }

    @Override
    public int getInternalIndex(T t) {
        return this.ordinalToValueIndex[t.ordinal()];
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
