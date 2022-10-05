package com.tridevmc.architecture.client.render.model.objson;

import com.tridevmc.architecture.client.render.model.data.IQuadMetadataResolver;
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
        return textures[Math.min(metadata.texture(), textures.length - 1)];
    }

    @Override
    public int getColour(OBJSONQuadMetadata metadata) {
        return colours[metadata.tintIndex()];
    }
}
