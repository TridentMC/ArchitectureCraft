package com.tridevmc.architecture.common.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.tridevmc.architecture.common.block.BlockShape;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * An extension of {@link BlockStateArchitecture} that provides a cached ShapeOrientation to avoid any lookups.
 */
public class BlockStateShape extends BlockStateArchitecture {

    private final ShapeOrientation cachedOrientation;

    public BlockStateShape(BlockShape<?> block, ImmutableMap<Property<?>, Comparable<?>> properties,
                           MapCodec<BlockState> codec) {
        this(block, ShapeOrientation::forState, properties, codec);
    }

    public BlockStateShape(BlockShape<?> block, ShapeOrientation orientation,
                           ImmutableMap<Property<?>, Comparable<?>> properties, MapCodec<BlockState> codec) {
        this(block, s -> orientation, properties, codec);
    }

    public BlockStateShape(BlockShape<?> block, Function<BlockStateShape, ShapeOrientation> orientationFunc,
                           ImmutableMap<Property<?>, Comparable<?>> properties, MapCodec<BlockState> codec) {
        super(block, properties, codec);
        this.cachedOrientation = orientationFunc.apply(this);
    }

    @NotNull
    public ShapeOrientation getOrientation() {
        return this.cachedOrientation;
    }

}
