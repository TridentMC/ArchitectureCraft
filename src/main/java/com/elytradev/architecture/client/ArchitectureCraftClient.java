//------------------------------------------------------
//
//   ArchitectureCraft - Client Proxy
//
//------------------------------------------------------

package com.elytradev.architecture.client;

import com.elytradev.architecture.base.BaseModClient;
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
        addScreen(ArchitectureCraft.guiSawbench, SawbenchGui.class);
    }

    @Override
    protected void registerBlockRenderers() {
        addBlockRenderer(base.blockShape, shapeRenderDispatch);
    }

    @Override
    protected void registerItemRenderers() {
        addItemRenderer(base.itemCladding, new CladdingRenderer());
    }

}
