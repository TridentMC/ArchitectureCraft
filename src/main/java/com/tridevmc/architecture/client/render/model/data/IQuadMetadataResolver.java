package com.tridevmc.architecture.client.render.model.data;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public interface IQuadMetadataResolver<T> {

    default TextureAtlasSprite getTexture(IBakedQuadProvider<T> provider) {
        return this.getTexture(provider.getMetadata());
    }

    TextureAtlasSprite getTexture(T metadata);

    default int getTint(IBakedQuadProvider<T> provider) {
        return this.getTint(provider.getMetadata());
    }

    int getTint(T metadata);

}
