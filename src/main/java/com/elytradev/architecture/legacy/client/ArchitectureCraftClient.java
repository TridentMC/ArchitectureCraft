/*
 * MIT License
 *
 * Copyright (c) 2017 Benjamin K
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.elytradev.architecture.legacy.client;

import com.elytradev.architecture.client.gui.GuiSawbench;
import com.elytradev.architecture.legacy.base.BaseModClient;
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
