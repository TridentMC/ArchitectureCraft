package com.tridevmc.architecture.client.render.model.data;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@FunctionalInterface
public interface IQuadMetadataTextureResolver<T> {
    TextureAtlasSprite getTexture(T metadata);
}
