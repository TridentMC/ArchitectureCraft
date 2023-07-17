package com.tridevmc.architecture.client.render.model.resolver.functional;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * A functional interface that resolves a quad's metadata to a texture.
 *
 * @param <D> The type of metadata to resolve.
 */
@FunctionalInterface
public interface IQuadMetadataTextureResolver<D> {

    /**
     * Resolves the quad's metadata to a texture.
     *
     * @param metadata The metadata to resolve.
     * @return The texture to use.
     */
    TextureAtlasSprite getTexture(D metadata);

}
