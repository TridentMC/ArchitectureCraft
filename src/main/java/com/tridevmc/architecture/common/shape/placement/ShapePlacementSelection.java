package com.tridevmc.architecture.common.shape.placement;

import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyAxis;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFacing;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFlip;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyOffset;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertySpin;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyTurn;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.List;

public final class ShapePlacementSelection {
    private static final List<Property<?>> PROPERTIES = List.of(
            ShapeOrientationPropertyFacing.INSTANCE, ShapeOrientationPropertyAxis.INSTANCE,
            ShapeOrientationPropertySpin.INSTANCE, ShapeOrientationPropertyTurn.INSTANCE,
            ShapeOrientationPropertyFlip.INSTANCE, ShapeOrientationPropertyOffset.INSTANCE
    );

    private ShapePlacementSelection() {
    }

    public static List<Property<?>> properties(BlockState state) {
        return PROPERTIES.stream().filter(state::hasProperty).toList();
    }

    public static BlockState apply(BlockState state, BlockState selection) {
        for (var property : PROPERTIES) {
            if (state.hasProperty(property)) {
                state = copyValue(state, selection, property);
            }
        }
        return state;
    }

    private static <T extends Comparable<T>> BlockState copyValue(BlockState state, BlockState selection,
                                                                 Property<T> property) {
        return state.setValue(property, selection.getValue(property));
    }
}
