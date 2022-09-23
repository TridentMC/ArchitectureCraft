package com.tridevmc.architecture.client.render.model.data;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface IQuadMetadataResolver<T> {

    default TextureAtlasSprite getTexture(IBakedQuadProvider<T> provider) {
        return this.getTexture(provider.getMetadata());
    }

    TextureAtlasSprite getTexture(T metadata);

    default int getColour(IBakedQuadProvider<T> provider) {
        return this.getColour(provider.getMetadata());
    }

    int getColour(T metadata);

}
