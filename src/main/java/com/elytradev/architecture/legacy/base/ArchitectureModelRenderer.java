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

package com.elytradev.architecture.legacy.base;

import com.elytradev.architecture.client.render.ICustomRenderer;
import com.elytradev.architecture.client.render.model.IArchitectureModel;
import com.elytradev.architecture.client.render.target.RenderTargetBase;
import com.elytradev.architecture.client.render.texture.ITexture;
import com.elytradev.architecture.common.block.BlockArchitecture;
import com.elytradev.architecture.common.helpers.Trans3;
import com.elytradev.architecture.common.helpers.Vector3;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class ArchitectureModelRenderer implements ICustomRenderer {

    protected IArchitectureModel model;
    protected ITexture[] textures;
    protected Vector3 origin;

    public ArchitectureModelRenderer(IArchitectureModel model, ITexture... textures) {
        this(model, Vector3.zero, textures);
    }

    public ArchitectureModelRenderer(IArchitectureModel model, Vector3 origin, ITexture... textures) {
        this.model = model;
        this.textures = textures;
        this.origin = origin;
    }

    @Override
    public void renderBlock(IBlockReader world, BlockPos pos, IBlockState state, RenderTargetBase target,
                            BlockRenderLayer layer, Trans3 t) {
        BlockArchitecture block = (BlockArchitecture) state.getBlock();
        Trans3 t2 = t.t(block.localToGlobalTransformation(world, pos, state, Vector3.zero)).translate(origin);
        int colour = -1;
        model.render(t2, target, colour, colour, textures);
    }

    @Override
    public void renderBlock(IBlockReader world, BlockPos pos, IBlockState state, RenderTargetBase target, BlockRenderLayer layer, Trans3 t, boolean renderPrimary, boolean renderSecondary) {
        if (renderPrimary) renderBlock(world, pos, state, target, layer, t);
    }

    @Override
    public void renderItemStack(ItemStack stack, RenderTargetBase target, Trans3 t) {
        Item item = stack.getItem();
        if (item instanceof ItemBlock) {
            Block block = Block.getBlockFromItem(item);
            if (block instanceof BlockArchitecture)
                t = t.t(((BlockArchitecture) block).itemTransformation());
        }
        model.render(t.translate(origin), target, -1, -1, textures);
    }

    private int getColourFromState(IBlockState state) {
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        int color = blockColors.getColor(state, null, null);
        return color;
    }

}

