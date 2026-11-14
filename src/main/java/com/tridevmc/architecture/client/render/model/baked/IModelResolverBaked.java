package com.tridevmc.architecture.client.render.model.baked;

import com.tridevmc.architecture.client.render.model.resolver.IModelResolver;
import com.tridevmc.architecture.common.block.state.BlockStateArchitecture;
import com.tridevmc.architecture.core.math.ITrans3;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public interface IModelResolverBaked<D> extends IArchitectureBakedModel {
    IModelResolver<D> getModelResolver();

    @Override
    default IBakedQuadContainer getQuads(BlockGetter level, BlockPos pos, BlockState state) {
        var transform = state instanceof BlockStateArchitecture architecture
                ? architecture.getTransform() : ITrans3.ofIdentity();
        return this.getModelResolver().getQuads(level, pos, state, transform);
    }

    @Override
    default List<BakedQuad> getQuads(ItemStack stack) {
        return this.getModelResolver().getQuads(stack).allQuads();
    }
}
