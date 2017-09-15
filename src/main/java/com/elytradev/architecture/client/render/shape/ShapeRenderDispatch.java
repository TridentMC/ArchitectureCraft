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

package com.elytradev.architecture.client.render.shape;

import com.elytradev.architecture.client.render.ICustomRenderer;
import com.elytradev.architecture.client.render.target.RenderTargetBase;
import com.elytradev.architecture.client.render.texture.ITexture;
import com.elytradev.architecture.client.render.texture.TextureBase;
import com.elytradev.architecture.common.tile.TileShape;
import com.elytradev.architecture.common.helpers.Trans3;
import com.elytradev.architecture.common.helpers.Utils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ShapeRenderDispatch implements ICustomRenderer {

    // Cannot have any per-render state, because it may be
    // called from more than one thread.

    @Override
    public void renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, RenderTargetBase target,
                            BlockRenderLayer layer, Trans3 t) {
        TileShape te = TileShape.get(world, pos);
        if (te != null) {
            Trans3 t2 = t.t(te.localToGlobalRotation());
            boolean renderBase = canRenderInLayer(te.baseBlockState, layer);
            boolean renderSecondary = canRenderInLayer(te.secondaryBlockState, layer);
            renderShapeTE(te, target, t2, renderBase, renderSecondary);
        }
    }

    public void renderShape(IBlockAccess world, BlockPos pos, RenderTargetBase target,
                            boolean renderBase, boolean renderSecondary, Trans3 t) {
        TileShape te = TileShape.get(world, pos);
        if (te != null) {
            Trans3 t2 = t.t(te.localToGlobalRotation());
            renderShapeTE(te, target, t2, renderBase, renderSecondary);
        }
    }

    protected boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return state != null && state.getBlock().canRenderInLayer(state, layer);
    }

    @Override
    public void renderItemStack(ItemStack stack, RenderTargetBase target, Trans3 t) {
        TileShape te = new TileShape();
        te.readFromItemStack(stack);
        renderShapeTE(te, target, t,
                te.baseBlockState != null, te.secondaryBlockState != null);
    }

    protected void renderShapeTE(TileShape te, RenderTargetBase target, Trans3 t,
                                 boolean renderBase, boolean renderSecondary) {
        if (te.shape != null && (renderBase || renderSecondary)) {
            IBlockState base = te.baseBlockState;
            if (base != null) {
                TextureAtlasSprite icon = Utils.getSpriteForBlockState(base);
                TextureAtlasSprite icon2 = Utils.getSpriteForBlockState(te.secondaryBlockState);
                if (icon != null) {
                    ITexture[] textures = new ITexture[4];
                    if (renderBase) {
                        textures[0] = TextureBase.fromSprite(icon);
                        textures[1] = textures[0].projected();
                    }
                    if (renderSecondary) {
                        if (icon2 != null) {
                            textures[2] = TextureBase.fromSprite(icon2);
                            textures[3] = textures[2].projected();
                        } else
                            renderSecondary = false;
                    }
                    if (renderBase && te.shape.kind.secondaryDefaultsToBase()) {
                        if (icon2 == null || (te.secondaryBlockState != null &&
                                te.secondaryBlockState.getBlock().getBlockLayer() != BlockRenderLayer.SOLID)) {
                            textures[2] = textures[0];
                            textures[3] = textures[1];
                            renderSecondary = renderBase;
                        }
                    }
                    te.shape.kind.renderShape(te, textures, target, t, renderBase, renderSecondary);
                }
            }
        }
    }

    @Override
    public void renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, RenderTargetBase target,
                            BlockRenderLayer layer, Trans3 t, boolean renderPrimary, boolean renderSecondary) {
        if (TileShape.get(world, pos) != null) {
            renderShapeTE(TileShape.get(world, pos), target, t, renderPrimary, renderSecondary);
        }
    }

}
