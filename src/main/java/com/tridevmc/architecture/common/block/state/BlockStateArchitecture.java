package com.tridevmc.architecture.common.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.ITrans3Immutable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;

public class BlockStateArchitecture extends BlockState {

    private final ITrans3Immutable cachedTransform;

    public BlockStateArchitecture(BlockArchitecture block,
                                  ImmutableMap<Property<?>, Comparable<?>> properties,
                                  MapCodec<BlockState> codec) {
        super(block, properties, codec);
        this.cachedTransform = block.getTransformForState(this);
    }

    @NotNull
    public ITrans3 getTransform() {
        return this.cachedTransform;
    }


}
