package com.tridevmc.architecture.client.render.model.baked;

import com.tridevmc.architecture.client.render.model.resolver.IModelResolver;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.common.block.state.BlockStateArchitecture;
import com.tridevmc.architecture.core.math.ITrans3;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Basic baked model that delegates to a {@link IModelResolver} and {@link IQuadMetadataResolver} to resolve quads from data in the world.
 *
 * @param <D> The type of quad metadata to resolve.
 */
public interface IModelResolverBaked<D> extends IArchitectureBakedModel {
    /**
     * Gets the model resolver to use.
     *
     * @return The model resolver.
     */
    IModelResolver<D> getModelResolver();

    /**
     * Gets the quad metadata resolver to use.
     *
     * @return The quad metadata resolver for the model.
     */
    default IQuadMetadataResolver<D> getMetadataResolver() {
        return this.getModelResolver().getMetadataResolver();
    }

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
