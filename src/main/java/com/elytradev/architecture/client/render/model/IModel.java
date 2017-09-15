package com.elytradev.architecture.client.render.model;

import com.elytradev.architecture.client.render.target.RenderTargetBase;
import com.elytradev.architecture.client.render.texture.ITexture;
import com.elytradev.architecture.legacy.common.helpers.Trans3;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;

public interface IModel {
    AxisAlignedBB getBounds();

    void addBoxesToList(Trans3 t, List list);

    void render(Trans3 t, RenderTargetBase renderer, ITexture... textures);
}
