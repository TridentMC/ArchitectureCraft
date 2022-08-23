package com.tridevmc.architecture.client.render.model.baked;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

public class UnpackedBakedQuad extends BakedQuad {

    private float[][] unpackedData;
    private int[] packedData;

    public UnpackedBakedQuad(int tintIndex, Direction face, TextureAtlasSprite sprite, boolean applyDiffuseLighting) {
        super(new int[0], tintIndex, face, sprite, applyDiffuseLighting);
    }

    @Override
    public int[] getVertices() {
        if(this.packedData == null){
            this.packedData = this.packData();
        }
        return this.packedData;
    }

    private int[] packData(){
        int[] out = new int[32];

        return null;
    }
}
