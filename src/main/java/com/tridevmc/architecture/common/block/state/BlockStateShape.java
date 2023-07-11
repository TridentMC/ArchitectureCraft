package com.tridevmc.architecture.common.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.tridevmc.architecture.common.block.BlockShape;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.core.physics.AABB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * An extension of {@link BlockStateArchitecture} that provides a cached ShapeOrientation to avoid any lookups.
 */
public class BlockStateShape extends BlockStateArchitecture {

    private final ShapeOrientation cachedOrientation;
    private final VoxelShape cachedShape;

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
        var boxes = block.getBoxesForState(this)
                .stream()
                .map(AABB::toMC)
                .map(Shapes::create)
                .toArray(VoxelShape[]::new);
        this.cachedShape = Shapes.or(boxes[0], boxes);
    }

    @NotNull
    public ShapeOrientation getOrientation() {
        return this.cachedOrientation;
    }

    @NotNull
    public VoxelShape getShape() {
        return this.cachedShape;
    }

}
