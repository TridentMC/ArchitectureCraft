package com.tridevmc.architecture.client.render.model.resolver;


import com.tridevmc.architecture.client.render.model.baked.IBakedQuadContainer;
import com.tridevmc.architecture.core.math.ITrans3;
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
     * Gets the quad metadata resolver for the model, additional context is provided to the resolver itself.
     *
     * @return The quad metadata resolver.
     */
    IQuadMetadataResolver<D> getMetadataResolver();

    /**
     * Gets the baked quads for the given quad metadata resolver and transformation.
     *
     * @param level     The LevelAccessor to get quads for.
     * @param pos       The BlockPos to get quads for.
     * @param state     The BlockState to get quads for.
     * @param resolver  The quad metadata resolver to use.
     * @param transform The transformation to apply to the quads.
     * @return The baked quads.
     */
    IBakedQuadContainer getQuads(LevelAccessor level, BlockPos pos, BlockState state,
                                 IQuadMetadataResolver<D> resolver, ITrans3 transform);

    /**
     * Gets the baked quads for the given quad metadata resolver and transformation.
     *
     * @param stack     The ItemStack to get quads for.
     * @param resolver  The quad metadata resolver to use.
     * @param transform The transformation to apply to the quads.
     * @return The baked quads.
     */
    IBakedQuadContainer getQuads(ItemStack stack, IQuadMetadataResolver<D> resolver, ITrans3 transform);

    /**
     * Gets the default sprite for this model.
     *
     * @return The default sprite.
     */
    TextureAtlasSprite getDefaultSprite();

    /**
     * Gets the baked quads for the given LevelAccessor, BlockPos and BlockState and transformation.
     *
     * @param level     The LevelAccessor to get the resolver for.
     * @param pos       The BlockPos to get the resolver for.
     * @param state     The BlockState to get the resolver for.
     * @param transform The transformation to apply to the quads.
     * @return The baked quads.
     */
    default IBakedQuadContainer getQuads(LevelAccessor level, BlockPos pos, BlockState state, ITrans3 transform) {
        return this.getQuads(level, pos, state, this.getMetadataResolver(), transform);
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
        return this.getQuads(level, pos, state, ITrans3.ofIdentity());
    }

    /**
     * Gets the baked quads for the given ItemStack and transformation.
     *
     * @param stack     The ItemStack to get the resolver for.
     * @param transform The transformation to apply to the quads.
     * @return The baked quads.
     */
    default IBakedQuadContainer getQuads(ItemStack stack, ITrans3 transform) {
        return this.getQuads(stack, this.getMetadataResolver(), transform);
    }

    /**
     * Gets the baked quads for the given ItemStack.
     *
     * @param stack The ItemStack to get the resolver for.
     * @return The baked quads.
     */
    default IBakedQuadContainer getQuads(ItemStack stack) {
        return this.getQuads(stack, ITrans3.ofIdentity());
    }


}
