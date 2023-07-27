package com.tridevmc.architecture.legacy.common.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.tridevmc.architecture.legacy.common.block.LegacyBlockArchitecture;
import com.tridevmc.architecture.legacy.math.LegacyTrans3;
import com.tridevmc.architecture.legacy.math.LegacyVector3;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.function.Predicate;


@MethodsReturnNonnullByDefault
@Deprecated
public class LegacyBlockStateArchitecture extends BlockState {
    public LegacyBlockStateArchitecture(LegacyBlockArchitecture block, ImmutableMap<Property<?>, Comparable<?>> propertyComparableImmutableMap, MapCodec<BlockState> blockStateMapCodec) {
        super(block, propertyComparableImmutableMap, blockStateMapCodec);
    }

    @Override
    public LegacyBlockArchitecture getBlock() {
        return (LegacyBlockArchitecture) super.getBlock();
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

    public LegacyBlockArchitecture.IOrientationHandler getOrientationHandler() {
        return this.getBlock().getOrientationHandler();
    }

    public RenderShape getRenderShape() {
        return this.getBlock().getRenderShape(this);
    }

    public LegacyTrans3 localToGlobalRotation(BlockAndTintGetter level, BlockPos pos) {
        return this.getBlock().localToGlobalRotation(level, pos, this);
    }

    public LegacyTrans3 localToGlobalTransformation(BlockGetter level, BlockPos pos) {
        return this.getBlock().localToGlobalTransformation(level, pos, this);
    }

    public LegacyTrans3 localToGlobalTransformation(BlockGetter level, BlockPos pos, LegacyVector3 origin) {
        return this.getBlock().localToGlobalTransformation(level, pos, this, origin);
    }

    public BlockState getParticleState(BlockAndTintGetter level, BlockPos pos) {
        return this.getBlock().getParticleState(level, pos);
    }

    @Override
    public boolean isCollisionShapeFullBlock(BlockGetter p_60839_, BlockPos p_60840_) {
        // Vanilla is excruciatingly slow at this, so we'll just assume it's a full block for now.
        return false;
    }

    @Override
    public boolean isSolidRender(BlockGetter p_60805_, BlockPos p_60806_) {
        return false;
    }

}
