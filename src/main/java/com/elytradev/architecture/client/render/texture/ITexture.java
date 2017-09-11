package com.elytradev.architecture.client.render.texture;

import com.elytradev.architecture.legacy.base.BaseModClient;
import net.minecraft.util.ResourceLocation;

public interface ITexture {
    ResourceLocation location();

    int tintIndex();

    double red();

    double green();

    double blue();

    double interpolateU(double u);

    double interpolateV(double v);

    boolean isEmissive();

    boolean isProjected();

    boolean isSolid();

    ITexture tinted(int index);

    ITexture colored(double red, double green, double blue);

    ITexture projected();

    ITexture emissive();

    BaseModClient.ITiledTexture tiled(int numRows, int numCols);
}
