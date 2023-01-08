package com.tridevmc.architecture.client.render.model.data;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * Defines two methods that are used to resolve the texture and tint index for a quad using the quad's metadata.
 *
 * @param <T> The type of the quad metadata.
 */
public interface IQuadMetadataResolver<T> {

    /**
     * Gets the texture to use for the quad.
     *
     * @param provider The quad provider containing the metadata.
     * @return The texture to use for the quad.
     */
    default TextureAtlasSprite getTexture(IBakedQuadProvider<T> provider) {
        return this.getTexture(provider.getMetadata());
    }

    /**
     * Gets the texture to use for the quad.
     *
     * @param metadata The quad metadata.
     * @return The texture to use for the quad.
     */
    TextureAtlasSprite getTexture(T metadata);

    /**
     * Gets the tint index to use for the quad.
     *
     * @param provider The quad provider containing the metadata.
     * @return The tint index to use for the quad.
     */
    default int getColour(IBakedQuadProvider<T> provider) {
        return this.getColour(provider.getMetadata());
    }

    /**
     * Gets the tint index to use for the quad.
     *
     * @param metadata The quad metadata.
     * @return The tint index to use for the quad.
     */
    int getColour(T metadata);

}
