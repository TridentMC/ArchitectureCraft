package com.tridevmc.architecture.common.block.state;

import com.tridevmc.architecture.common.block.BlockShape;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * An extension of {@link BlockStateArchitecture} that provides a cached ShapeOrientation to avoid any lookups.
 */
public class BlockStateShape extends BlockStateArchitecture {

    private final ShapeOrientation cachedOrientation;

    public BlockStateShape(BlockShape<?> block, Property<?>[] propertyKeys, Comparable<?>[] propertyValues) {
        this(block, ShapeOrientation::forState, propertyKeys, propertyValues);
    }

    public BlockStateShape(BlockShape<?> block, ShapeOrientation orientation,
                           Property<?>[] propertyKeys, Comparable<?>[] propertyValues) {
        this(block, s -> orientation, propertyKeys, propertyValues);
    }

    public BlockStateShape(BlockShape<?> block, Function<BlockStateShape, ShapeOrientation> orientationFunc,
                           Property<?>[] propertyKeys, Comparable<?>[] propertyValues) {
        super(block, propertyKeys, propertyValues);
        this.cachedOrientation = orientationFunc.apply(this);
        this.postConstruct();
    }

    @NotNull
    public ShapeOrientation getOrientation() {
        return this.cachedOrientation;
    }

}
