package com.tridevmc.architecture.client.render.model;


import com.mojang.math.Transformation;
import com.tridevmc.architecture.client.render.model.data.IBakedQuadContainer;
import com.tridevmc.architecture.client.render.model.data.IQuadMetadataResolver;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Provides a common interface for resolving models from sources of data (such as ItemStacks or BlockStates).
 *
 * @param <D> The type of quad metadata resolver to use.
 */
public interface IModelResolver<D> {

    /**
     * Gets the quad metadata resolver for the given ItemStack.
     *
     * @param stack The ItemStack to get the resolver for.
     * @return The quad metadata resolver.
     */
    IQuadMetadataResolver<D> getMetadataResolver(ItemStack stack);

    /**
     * Gets the quad metadata resolver for the given LevelAccessor, BlockPos and BlockState.
     *
     * @param level The LevelAccessor to get the resolver for.
     * @param pos   The BlockPos to get the resolver for.
     * @param state The BlockState to get the resolver for.
     * @return The quad metadata resolver.
     */
    IQuadMetadataResolver<D> getMetadataResolver(LevelAccessor level, BlockPos pos, BlockState state);

    /**
     * Gets the baked quads for the given quad metadata resolver and transformation.
     *
     * @param resolver  The quad metadata resolver to use.
     * @param transform The transformation to apply to the quads.
     * @return The baked quads.
     */
    IBakedQuadContainer getQuads(IQuadMetadataResolver<D> resolver, Transformation transform);

    /**
     * Gets the default sprite for this model.
     *
     * @return The default sprite.
     */
    TextureAtlasSprite getDefaultSprite();

    /**
     * Gets the baked quads for the given quad metadata resolver.
     *
     * @param resolver The quad metadata resolver to use.
     * @return The baked quads.
     */
    default IBakedQuadContainer getQuads(IQuadMetadataResolver<D> resolver) {
        return this.getQuads(resolver, Transformation.identity());
    }

    /**
     * Gets the baked quads for the given LevelAccessor, BlockPos and BlockState and transformation.
     *
     * @param level     The LevelAccessor to get the resolver for.
     * @param pos       The BlockPos to get the resolver for.
     * @param state     The BlockState to get the resolver for.
     * @param transform The transformation to apply to the quads.
     * @return The baked quads.
     */
    default IBakedQuadContainer getQuads(LevelAccessor level, BlockPos pos, BlockState state, Transformation transform) {
        return this.getQuads(this.getMetadataResolver(level, pos, state), transform);
    }

    /**
     * Gets the baked quads for the given LevelAccessor, BlockPos and BlockState.
     *
     * @param level The LevelAccessor to get the resolver for.
     * @param pos   The BlockPos to get the resolver for.
     * @param state The BlockState to get the resolver for.
     * @return The baked quads.
     */
    default IBakedQuadContainer getQuads(LevelAccessor level, BlockPos pos, BlockState state) {
        return this.getQuads(level, pos, state, Transformation.identity());
    }

    /**
     * Gets the baked quads for the given ItemStack and transformation.
     *
     * @param stack     The ItemStack to get the resolver for.
     * @param transform The transformation to apply to the quads.
     * @return The baked quads.
     */
    default IBakedQuadContainer getQuads(ItemStack stack, Transformation transform) {
        return this.getQuads(this.getMetadataResolver(stack), transform);
    }

    /**
     * Gets the baked quads for the given ItemStack.
     *
     * @param stack The ItemStack to get the resolver for.
     * @return The baked quads.
     */
    default IBakedQuadContainer getQuads(ItemStack stack) {
        return this.getQuads(stack, Transformation.identity());
    }


}
