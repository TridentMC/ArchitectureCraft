package com.tridevmc.architecture.common.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.ITrans3Immutable;
import com.tridevmc.architecture.core.physics.AABB;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class BlockStateArchitecture extends BlockState {

    private final ITrans3Immutable cachedTransform;
    private final VoxelShape cachedShape;

    public BlockStateArchitecture(BlockArchitecture block,
                                  ImmutableMap<Property<?>, Comparable<?>> properties,
                                  MapCodec<BlockState> codec) {
        super(block, properties, codec);
        this.cachedTransform = block.getTransformForState(this);
        var boxes = block.getBoxesForState(this)
                .stream()
                .map(AABB::toMC)
                .map(Shapes::create)
                .toArray(VoxelShape[]::new);
        this.cachedShape = Shapes.or(boxes[0], boxes);
    }

    @NotNull
    public ITrans3 getTransform() {
        return this.cachedTransform;
    }

    @NotNull
    public VoxelShape getShape() {
        return this.cachedShape;
    }


}
