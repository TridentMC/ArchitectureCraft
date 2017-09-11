//------------------------------------------------------
//
//   ArchitectureCraft - Client Proxy
//
//------------------------------------------------------

package com.elytradev.architecture.legacy.client;

import com.elytradev.architecture.legacy.base.BaseModClient;
import com.elytradev.architecture.legacy.client.gui.GuiSawbench;
import com.elytradev.architecture.legacy.client.render.RenderCladding;
import com.elytradev.architecture.legacy.client.render.RenderWindow;
import com.elytradev.architecture.legacy.client.render.ShapeRenderDispatch;
import com.elytradev.architecture.legacy.common.ArchitectureCraft;

public class ArchitectureCraftClient extends BaseModClient<ArchitectureCraft> {

    public static ShapeRenderDispatch shapeRenderDispatch = new ShapeRenderDispatch();

    public ArchitectureCraftClient(ArchitectureCraft mod) {
        super(mod);
        //debugModelRegistration = true;
        RenderWindow.init(this);
    }

    @Override
    public void registerScreens() {
        addScreen(ArchitectureCraft.guiSawbench, GuiSawbench.class);
    }

    @Override
    protected void registerBlockRenderers() {
        addBlockRenderer(ArchitectureCraft.blockShape, shapeRenderDispatch);
    }

    @Override
    protected void registerItemRenderers() {
        addItemRenderer(ArchitectureCraft.itemCladding, new RenderCladding());
    }

}
