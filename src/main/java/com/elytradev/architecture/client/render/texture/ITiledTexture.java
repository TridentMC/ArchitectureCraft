package com.elytradev.architecture.client.render.texture;

public interface ITiledTexture extends ITexture {
    ITexture tile(int row, int col);
}
