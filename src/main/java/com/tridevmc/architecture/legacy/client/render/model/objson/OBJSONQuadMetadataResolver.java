package com.tridevmc.architecture.legacy.client.render.model.objson;

import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class OBJSONQuadMetadataResolver implements IQuadMetadataResolver<OBJSONQuadMetadata> {

    private final TextureAtlasSprite[] textures;
    private final int[] colours;

    public OBJSONQuadMetadataResolver(TextureAtlasSprite[] textures, int[] colours) {
        this.textures = textures;
        this.colours = colours;
    }

    @Override
    public TextureAtlasSprite getTexture(OBJSONQuadMetadata metadata) {
        return this.textures[Math.min(metadata.texture(), this.textures.length - 1)];
    }

    @Override
    public int getTintIndex(OBJSONQuadMetadata metadata) {
        return this.colours[metadata.tintIndex()];
    }
}
