//------------------------------------------------------
//
//   ArchitectureCraft - Client Proxy
//
//------------------------------------------------------

package com.elytradev.architecture.client;

import com.elytradev.architecture.base.BaseModClient;
import com.elytradev.architecture.client.gui.GuiSawbench;
import com.elytradev.architecture.client.render.RenderCladding;
import com.elytradev.architecture.client.render.RenderWindow;
import com.elytradev.architecture.client.render.ShapeRenderDispatch;
import com.elytradev.architecture.common.ArchitectureCraft;

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
        addBlockRenderer(base.blockShape, shapeRenderDispatch);
    }

    @Override
    protected void registerItemRenderers() {
        addItemRenderer(base.itemCladding, new RenderCladding());
    }

}
