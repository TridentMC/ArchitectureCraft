package com.tridevmc.architecture.client.render.model.baked;

import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.core.math.ITrans3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Provides a way to get a cached {@link IBakedQuadContainer} for a given part id, metadata resolver and transform.
 *
 * @param <I> The type of the part id.
 * @param <D> The type of the metadata.
 */
public interface IMultipartBakedQuadContainerProvider<I, D> {

    /**
     * Creates or fetches a cached {@link IBakedQuadContainer} using the given metadata resolver and transform and returns it.
     *
     * @param partId           The part id to get the quads for, or null if all parts should be used.
     * @param level            The LevelAccessor to get the quads for.
     * @param pos              The BlockPos to get the quads for.
     * @param state            The BlockState to get the quads for.
     * @param metadataResolver The metadata resolver to use for getting the texture and tint index for each quad.
     * @param transform        The transform to use.
     * @param forceRebuild     Whether to force a rebuild of the quad container, primarily used for debugging.
     * @return The baked quad container.
     */
    IBakedQuadContainer getQuads(@Nullable I partId, LevelAccessor level, BlockPos pos, BlockState state, IQuadMetadataResolver<D> metadataResolver, ITrans3 transform, boolean forceRebuild);

    /**
     * Creates or fetches a cached {@link IBakedQuadContainer} using the given metadata resolver and transform and returns it.
     *
     * @param partId           The part id to get the quads for, or null if all parts should be used.
     * @param stack            The ItemStack to get the quads for.
     * @param metadataResolver The metadata resolver to use for getting the texture and tint index for each quad.
     * @param transform        The transform to use.
     * @param forceRebuild     Whether to force a rebuild of the quad container, primarily used for debugging.
     * @return The baked quad container.
     */
    IBakedQuadContainer getQuads(@Nullable I partId, ItemStack stack, IQuadMetadataResolver<D> metadataResolver, ITrans3 transform, boolean forceRebuild);

    /**
     * Creates or fetches a cached {@link IBakedQuadContainer} using the given metadata resolver and transform and returns it.
     *
     * @param partId           The part id to get the quads for, or null if all parts should be used.
     * @param metadataResolver The metadata resolver to use for getting the texture and tint index for each quad.
     * @param transform        The transform to use.
     * @param forceRebuild     Whether to force a rebuild of the quad container, primarily used for debugging.
     * @return The baked quad container.
     */
    IBakedQuadContainer getQuads(@Nullable I partId, IQuadMetadataResolver<D> metadataResolver, ITrans3 transform, boolean forceRebuild);

    /**
     * Creates or fetches a cached {@link IBakedQuadContainer} using the given metadata resolver and transform and returns it.
     *
     * @param partId           The part id to get the quads for, or null if all parts should be used.
     * @param level            The LevelAccessor to get the quads for.
     * @param pos              The BlockPos to get the quads for.
     * @param state            The BlockState to get the quads for.
     * @param metadataResolver The metadata resolver to use for getting the texture and tint index for each quad.
     * @param transform        The transform to use.
     * @return The baked quad container.
     */
    default IBakedQuadContainer getQuads(@Nullable I partId, LevelAccessor level, BlockPos pos, BlockState state, IQuadMetadataResolver<D> metadataResolver, ITrans3 transform) {
        return this.getQuads(partId, metadataResolver, transform, false);
    }


    /**
     * Creates or fetches a cached {@link IBakedQuadContainer} using the given metadata resolver and transform and returns it.
     *
     * @param partId           The part id to get the quads for, or null if all parts should be used.
     * @param stack            The ItemStack to get the quads for.
     * @param metadataResolver The metadata resolver to use for getting the texture and tint index for each quad.
     * @param transform        The transform to use.
     * @return The baked quad container.
     */
    default IBakedQuadContainer getQuads(@Nullable I partId, ItemStack stack, IQuadMetadataResolver<D> metadataResolver, ITrans3 transform) {
        return this.getQuads(partId, metadataResolver, transform, false);
    }

    /**
     * Creates or fetches a cached {@link IBakedQuadContainer} using the given metadata resolver and transform and returns it.
     *
     * @param partId           The part id to get the quads for, or null if all parts should be used.
     * @param metadataResolver The metadata resolver to use for getting the texture and tint index for each quad.
     * @param transform        The transform to use.
     * @return The baked quad container.
     */
    default IBakedQuadContainer getQuads(@Nullable I partId, IQuadMetadataResolver<D> metadataResolver, ITrans3 transform) {
        return this.getQuads(partId, metadataResolver, transform, false);
    }


}
