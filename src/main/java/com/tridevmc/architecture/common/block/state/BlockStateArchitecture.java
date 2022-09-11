package com.tridevmc.architecture.common.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.function.Predicate;


@MethodsReturnNonnullByDefault
public class BlockStateArchitecture extends BlockState {
    public BlockStateArchitecture(BlockArchitecture block, ImmutableMap<Property<?>, Comparable<?>> propertyComparableImmutableMap, MapCodec<BlockState> blockStateMapCodec) {
        super(block, propertyComparableImmutableMap, blockStateMapCodec);
    }

    @Override
    public BlockArchitecture getBlock() {
        return (BlockArchitecture) super.getBlock();
    }

    @Override
    public boolean hasBlockEntity() {
        return this.getBlock().hasBlockEntity(this);
    }

    @Override
    public boolean is(TagKey<Block> tag) {
        return this.getBlock().is(this, tag);
    }

    @Override
    public boolean is(TagKey<Block> tag, Predicate<BlockBehaviour.BlockStateBase> predicate) {
        return this.getBlock().is(this, tag, predicate);
    }

    @Override
    public boolean is(HolderSet<Block> holderSet) {
        return this.getBlock().is(this, holderSet);
    }
}
