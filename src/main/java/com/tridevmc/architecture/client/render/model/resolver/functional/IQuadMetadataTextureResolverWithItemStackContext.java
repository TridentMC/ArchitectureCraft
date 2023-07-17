package com.tridevmc.architecture.client.render.model.resolver.functional;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A functional interface that resolves a quad's metadata to a texture, with the context of an item stack to resolve.
 *
 * @param <D> The type of metadata to resolve.
 */
@FunctionalInterface
public interface IQuadMetadataTextureResolverWithItemStackContext<D> {

    /**
     * Resolves the quad's metadata to a texture.
     *
     * @param stack    The stack to resolve the texture for.
     * @param metadata The metadata to resolve.
     * @return The texture to use.
     */
    TextureAtlasSprite getTexture(@NotNull ItemStack stack, D metadata);

}
