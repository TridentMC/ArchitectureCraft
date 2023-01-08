package com.tridevmc.architecture.client.render.model.data;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * A functional interface that resolves a quad's metadata to a texture.
 *
 * @param <T> The type of metadata to resolve.
 */
@FunctionalInterface
public interface IQuadMetadataTextureResolver<T> {
    /**
     * Resolves the quad's metadata to a texture.
     *
     * @param metadata The metadata to resolve.
     * @return The texture to use.
     */
    TextureAtlasSprite getTexture(T metadata);
}
