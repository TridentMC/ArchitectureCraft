package com.tridevmc.architecture.client.render.model.resolver;

import com.tridevmc.architecture.client.render.model.piped.IPipedBakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Defines two methods that are used to resolve the material and tint index for a quad using the quad's metadata as well as any additional context required.
 *
 * @param <D> The type of the quad metadata.
 */
public interface IQuadMetadataResolver<D> {

    /**
     * Gets the material to use for the quad, chooses the correct method to use based on the context provided.
     *
     * @param level    The level to get the material for, can be null.
     * @param pos      The position to get the material for, can be null.
     * @param state    The state to get the material for, can be null.
     * @param stack    The stack to get the material for, can be null.
     * @param provider The quad provider containing the metadata.
     * @return The material to use for the quad.
     */
    default Material.Baked getMaterial(@Nullable BlockGetter level, @Nullable BlockPos pos, @Nullable BlockState state, @Nullable ItemStack stack, IPipedBakedQuad<?, ?, D> provider) {
        return this.getMaterial(level, pos, state, stack, provider.metadata());
    }


    /**
     * Gets the material to use for the quad, chooses the correct method to use based on the context provided.
     *
     * @param level    The level to get the material for, can be null.
     * @param pos      The position to get the material for, can be null.
     * @param state    The state to get the material for, can be null.
     * @param stack    The stack to get the material for, can be null.
     * @param metadata The metadata to get the material for.
     * @return The material to use for the quad.
     */
    default Material.Baked getMaterial(@Nullable BlockGetter level, @Nullable BlockPos pos, @Nullable BlockState state, @Nullable ItemStack stack, D metadata) {
        if (level != null && pos != null && state != null) {
            return this.getMaterial(level, pos, state, metadata);
        } else if (stack != null) {
            return this.getMaterial(stack, metadata);
        } else {
            return this.getMaterial(metadata);
        }
    }

    /**
     * Gets the material to use for the quad.
     *
     * @param level    The level to get the material for.
     * @param pos      The position to get the material for.
     * @param state    The state to get the material for.
     * @param provider The quad provider containing the metadata.
     * @return The material to use for the quad.
     */
    default Material.Baked getMaterial(BlockGetter level, BlockPos pos, BlockState state, IPipedBakedQuad<?, ?, D> provider) {
        return this.getMaterial(provider.metadata());
    }

    /**
     * Gets the material to use for the quad.
     *
     * @param level    The level to get the material for.
     * @param pos      The position to get the material for.
     * @param state    The state to get the material for.
     * @param metadata The quad metadata.
     * @return The material to use for the quad.
     */
    default Material.Baked getMaterial(BlockGetter level, BlockPos pos, BlockState state, D metadata) {
        return this.getMaterial(metadata);
    }

    /**
     * Gets the material to use for the quad.
     *
     * @param stack    The stack to get the material for.
     * @param provider The quad provider containing the metadata.
     * @return The material to use for the quad.
     */
    default Material.Baked getMaterial(ItemStack stack, IPipedBakedQuad<?, ?, D> provider) {
        return this.getMaterial(provider.metadata());
    }

    /**
     * Gets the material to use for the quad.
     *
     * @param stack    The stack to get the material for.
     * @param metadata The quad metadata.
     * @return The material to use for the quad.
     */
    default Material.Baked getMaterial(ItemStack stack, D metadata) {
        return this.getMaterial(metadata);
    }

    /**
     * Gets the material to use for the quad.
     *
     * @param provider The quad provider containing the metadata.
     * @return The material to use for the quad.
     */
    default Material.Baked getMaterial(IPipedBakedQuad<?, ?, D> provider) {
        return this.getMaterial(provider.metadata());
    }

    /**
     * Gets the material to use for the quad, should be used primarily as a fallback or for quads that aren't context-sensitive.
     *
     * @param metadata The quad metadata.
     * @return The material to use for the quad.
     */
    Material.Baked getMaterial(D metadata);


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
    default int getTintIndex(@Nullable BlockGetter level, @Nullable BlockPos pos, @Nullable BlockState state, @Nullable ItemStack stack, IPipedBakedQuad<?, ?, D> provider) {
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
    default int getTintIndex(@Nullable BlockGetter level, @Nullable BlockPos pos, @Nullable BlockState state, @Nullable ItemStack stack, D metadata) {
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
    default int getTintIndex(BlockGetter level, BlockPos pos, BlockState state, IPipedBakedQuad<?, ?, D> provider) {
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
    default int getTintIndex(BlockGetter level, BlockPos pos, BlockState state, D metadata) {
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
