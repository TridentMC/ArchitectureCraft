package com.tridevmc.architecture.client.render.model.data;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/**
 * Defines two methods that are used to resolve the texture and tint index for a quad using the quad's metadata.
 *
 * @param <D> The type of the quad metadata.
 */
public interface IQuadMetadataResolver<D> {

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
     * Gets the texture to use for the quad.
     *
     * @param metadata The quad metadata.
     * @return The texture to use for the quad.
     */
    TextureAtlasSprite getTexture(D metadata);

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
     * Gets the tint index to use for the quad.
     *
     * @param metadata The quad metadata.
     * @return The tint index to use for the quad.
     */
    int getTintIndex(D metadata);

}
