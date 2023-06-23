package com.tridevmc.architecture.common.shape.orientation;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import java.util.Collection;

/**
 * Defines a property used for orienting a shape in the world, combined with other properties to form a {@link ShapeOrientation}.
 */
public class ShapeOrientationProperty<T extends Enum<T> & StringRepresentable> extends EnumProperty<T> {

    protected ShapeOrientationProperty(String name, Class<T> type, Collection<T> values) {
        super(name, type, values);
    }

}
