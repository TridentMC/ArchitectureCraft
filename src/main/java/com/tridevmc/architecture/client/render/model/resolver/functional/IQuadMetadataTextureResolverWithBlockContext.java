package com.tridevmc.architecture.client.render.model.resolver.functional;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * A functional interface that resolves a quad's metadata to a texture, with additional context from the block.
 *
 * @param <D> The type of metadata to resolve.
 */
@FunctionalInterface
public interface IQuadMetadataTextureResolverWithBlockContext<D> {

    /**
     * Resolves the quad's metadata to a texture.
     *
     * @param level    The level the block is in.
     * @param pos      The position of the block.
     * @param state    The state of the block.
     * @param metadata The metadata to resolve.
     * @return The texture to use.
     */
    TextureAtlasSprite getTexture(@NotNull LevelAccessor level, @NotNull BlockPos pos, @NotNull BlockState state, D metadata);

}
