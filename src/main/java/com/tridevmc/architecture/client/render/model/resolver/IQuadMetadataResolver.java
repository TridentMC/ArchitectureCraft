package com.tridevmc.architecture.client.render.model.resolver;

import com.tridevmc.architecture.client.render.model.piped.IPipedBakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Defines two methods that are used to resolve the texture and tint index for a quad using the quad's metadata as well as any additional context required.
 *
 * @param <D> The type of the quad metadata.
 */
public interface IQuadMetadataResolver<D> {

    /**
     * Gets the texture to use for the quad, chooses the correct method to use based on the context provided.
     *
     * @param level    The level to get the texture for, can be null.
     * @param pos      The position to get the texture for, can be null.
     * @param state    The state to get the texture for, can be null.
     * @param stack    The stack to get the texture for, can be null.
     * @param provider The quad provider containing the metadata.
     * @return The texture to use for the quad.
     */
    default TextureAtlasSprite getTexture(@Nullable LevelAccessor level, @Nullable BlockPos pos, @Nullable BlockState state, @Nullable ItemStack stack, IPipedBakedQuad<?, ?, D> provider) {
        return this.getTexture(level, pos, state, stack, provider.metadata());
    }


    /**
     * Gets the texture to use for the quad, chooses the correct method to use based on the context provided.
     *
     * @param level    The level to get the texture for, can be null.
     * @param pos      The position to get the texture for, can be null.
     * @param state    The state to get the texture for, can be null.
     * @param stack    The stack to get the texture for, can be null.
     * @param metadata The metadata to get the texture for.
     * @return The texture to use for the quad.
     */
    default TextureAtlasSprite getTexture(@Nullable LevelAccessor level, @Nullable BlockPos pos, @Nullable BlockState state, @Nullable ItemStack stack, D metadata) {
        if (level != null && pos != null && state != null) {
            return this.getTexture(level, pos, state, metadata);
        } else if (stack != null) {
            return this.getTexture(stack, metadata);
        } else {
            return this.getTexture(metadata);
        }
    }

    /**
     * Gets the texture to use for the quad.
     *
     * @param level    The level to get the texture for.
     * @param pos      The position to get the texture for.
     * @param state    The state to get the texture for.
     * @param provider The quad provider containing the metadata.
     * @return The texture to use for the quad.
     */
    default TextureAtlasSprite getTexture(LevelAccessor level, BlockPos pos, BlockState state, IPipedBakedQuad<?, ?, D> provider) {
        return this.getTexture(provider.metadata());
    }

    /**
     * Gets the texture to use for the quad.
     *
     * @param level    The level to get the texture for.
     * @param pos      The position to get the texture for.
     * @param state    The state to get the texture for.
     * @param metadata The quad metadata.
     * @return The texture to use for the quad.
     */
    default TextureAtlasSprite getTexture(LevelAccessor level, BlockPos pos, BlockState state, D metadata) {
        return this.getTexture(metadata);
    }

    /**
     * Gets the texture to use for the quad.
     *
     * @param stack    The stack to get the texture for.
     * @param provider The quad provider containing the metadata.
     * @return The texture to use for the quad.
     */
    default TextureAtlasSprite getTexture(ItemStack stack, IPipedBakedQuad<?, ?, D> provider) {
        return this.getTexture(provider.metadata());
    }

    /**
     * Gets the texture to use for the quad.
     *
     * @param stack    The stack to get the texture for.
     * @param metadata The quad metadata.
     * @return The texture to use for the quad.
     */
    default TextureAtlasSprite getTexture(ItemStack stack, D metadata) {
        return this.getTexture(metadata);
    }

    /**
     * Gets the texture to use for the quad.
     *
     * @param provider The quad provider containing the metadata.
     * @return The texture to use for the quad.
     */
    default TextureAtlasSprite getTexture(IPipedBakedQuad<?, ?, D> provider) {
        return this.getTexture(provider.metadata());
    }

    /**
     * Gets the texture to use for the quad, should be used primarily as a fallback or for quads that aren't context-sensitive.
     *
     * @param metadata The quad metadata.
     * @return The texture to use for the quad.
     */
    TextureAtlasSprite getTexture(D metadata);


    /**
     * Gets the tint index to use for the quad, chooses the correct method to use based on the context provided.
     *
     * @param level    The level to get the tint index for, can be null.
     * @param pos      The position to get the tint index for, can be null.
     * @param state    The state to get the tint index for, can be null.
     * @param stack    The stack to get the tint index for, can be null.
     * @param provider The quad provider containing the metadata.
     * @return The tint index to use for the quad.
     */
    default int getTintIndex(@Nullable LevelAccessor level, @Nullable BlockPos pos, @Nullable BlockState state, @Nullable ItemStack stack, IPipedBakedQuad<?, ?, D> provider) {
        return this.getTintIndex(level, pos, state, stack, provider.metadata());
    }


    /**
     * Gets the tint index to use for the quad, chooses the correct method to use based on the context provided.
     *
     * @param level    The level to get the tint index for, can be null.
     * @param pos      The position to get the tint index for, can be null.
     * @param state    The state to get the tint index for, can be null.
     * @param stack    The stack to get the tint index for, can be null.
     * @param metadata The metadata to get the tint index for.
     * @return The tint index to use for the quad.
     */
    default int getTintIndex(@Nullable LevelAccessor level, @Nullable BlockPos pos, @Nullable BlockState state, @Nullable ItemStack stack, D metadata) {
        if (level != null && pos != null && state != null) {
            return this.getTintIndex(level, pos, state, metadata);
        } else if (stack != null) {
            return this.getTintIndex(stack, metadata);
        } else {
            return this.getTintIndex(metadata);
        }
    }


    /**
     * Gets the tint index to use for the quad.
     *
     * @param level    The level to get the tint index for.
     * @param pos      The position to get the tint index for.
     * @param state    The state to get the tint index for.
     * @param provider The quad provider containing the metadata.
     * @return The tint index to use for the quad.
     */
    default int getTintIndex(LevelAccessor level, BlockPos pos, BlockState state, IPipedBakedQuad<?, ?, D> provider) {
        return this.getTintIndex(provider.metadata());
    }

    /**
     * Gets the tint index to use for the quad.
     *
     * @param level    The level to get the tint index for.
     * @param pos      The position to get the tint index for.
     * @param state    The state to get the tint index for.
     * @param metadata The quad metadata.
     * @return The tint index to use for the quad.
     */
    default int getTintIndex(LevelAccessor level, BlockPos pos, BlockState state, D metadata) {
        return this.getTintIndex(metadata);
    }


    /**
     * Gets the tint index to use for the quad.
     *
     * @param stack    The stack to get the tint index for.
     * @param provider The quad provider containing the metadata.
     * @return The tint index to use for the quad.
     */
    default int getTintIndex(ItemStack stack, IPipedBakedQuad<?, ?, D> provider) {
        return this.getTintIndex(provider.metadata());
    }


    /**
     * Gets the tint index to use for the quad.
     *
     * @param stack    The stack to get the tint index for.
     * @param metadata The quad metadata.
     * @return The tint index to use for the quad.
     */
    default int getTintIndex(ItemStack stack, D metadata) {
        return this.getTintIndex(metadata);
    }


    /**
     * Gets the tint index to use for the quad.
     *
     * @param provider The quad provider containing the metadata.
     * @return The tint index to use for the quad.
     */
    default int getTintIndex(IPipedBakedQuad<?, ?, D> provider) {
        return this.getTintIndex(provider.metadata());
    }

    /**
     * Gets the tint index to use for the quad, should be used primarily as a fallback or for quads that aren't context-sensitive.
     *
     * @param metadata The quad metadata.
     * @return The tint index to use for the quad.
     */
    int getTintIndex(D metadata);

}
