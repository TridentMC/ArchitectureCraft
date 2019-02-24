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

package com.tridevmc.architecture.client.render.shape;

import com.tridevmc.architecture.client.proxy.ClientProxy;
import com.tridevmc.architecture.client.render.model.IArchitectureModel;
import com.tridevmc.architecture.client.render.target.RenderTargetBase;
import com.tridevmc.architecture.client.render.texture.ITexture;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.tile.TileShape;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public abstract class RenderShape {

    protected IBlockReader blockWorld;
    protected BlockPos blockPos;
    protected TileShape te;
    protected ITexture[] textures;
    protected Trans3 t;
    protected RenderTargetBase target;
    private int baseColourMult;
    private int secondaryColourMult;

    public RenderShape(TileShape te, ITexture[] textures, Trans3 t, RenderTargetBase target) {
        this.te = te;
        this.blockWorld = te.getWorld();
        this.blockPos = te.getPos();
        this.textures = textures;
        this.t = t;
        this.target = target;
    }

    protected abstract void render();

    protected IArchitectureModel getModel(String name) {
        return ClientProxy.RENDERING_MANAGER.getModel(name);
    }

    public int getBaseColourMult() {
        return baseColourMult;
    }

    public void setBaseColourMult(int baseColourMult) {
        this.baseColourMult = baseColourMult;
    }

    public int getSecondaryColourMult() {
        return secondaryColourMult;
    }

    public void setSecondaryColourMult(int secondaryColourMult) {
        this.secondaryColourMult = secondaryColourMult;
    }
}
