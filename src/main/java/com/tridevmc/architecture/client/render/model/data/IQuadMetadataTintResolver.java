package com.tridevmc.architecture.client.render.model.data;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

@FunctionalInterface
public interface IQuadMetadataTintResolver<T> {
    int getTint(T metadata);
}
