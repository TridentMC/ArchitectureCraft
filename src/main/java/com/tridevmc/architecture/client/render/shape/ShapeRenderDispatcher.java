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

import com.tridevmc.architecture.client.render.ICustomRenderer;
import com.tridevmc.architecture.client.render.target.RenderTargetBase;
import com.tridevmc.architecture.client.render.texture.ITexture;
import com.tridevmc.architecture.client.render.texture.TextureBase;
import com.tridevmc.architecture.common.block.BlockHelper;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Utils;
import com.tridevmc.architecture.common.tile.TileShape;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class ShapeRenderDispatcher implements ICustomRenderer {

    // Cannot have any per-render state, because it may be
    // called from more than one thread.

    @Override
    public void renderBlock(IBlockReader world, BlockPos pos, BlockState state, RenderTargetBase target,
                            RenderType layer, Trans3 t) {
        TileShape te = TileShape.get(world, pos);
        if (te != null) {
            Trans3 t2 = t.t(te.localToGlobalRotation());
            boolean renderBase = this.canRenderInLayer(te.baseBlockState, layer);
            boolean renderSecondary = this.canRenderInLayer(te.secondaryBlockState, layer);

            int baseColour = renderBase ? this.getColourFromState(te.baseBlockState) : -1;
            int secondaryColour = renderSecondary ? this.getColourFromState(te.secondaryBlockState) : baseColour;

            this.renderShapeTE(te, target, t2, renderBase, renderSecondary, baseColour, secondaryColour);
        }
    }

    protected boolean canRenderInLayer(BlockState state, RenderType layer) {
        if (layer == null)
            return true;
        return state != null && BlockHelper.blockCanRenderInLayer(state, layer);
    }

    @Override
    public void renderItemStack(ItemStack stack, RenderTargetBase target, Trans3 t) {
        TileShape te = new TileShape();
        te.readFromItemStack(stack);
        ItemColors itemColors = Minecraft.getInstance().getItemColors();
        ItemStack baseStack = this.getStackFromState(te.baseBlockState);
        ItemStack secondaryStack = this.getStackFromState(te.secondaryBlockState);
        int baseColour = baseStack != null ?
                itemColors.getColor(baseStack, 0) : -1;
        int secondaryColour = secondaryStack != null ?
                itemColors.getColor(secondaryStack, 0) : baseColour;

        this.renderShapeTE(te, target, t,
                te.baseBlockState != null,
                te.secondaryBlockState != null,
                baseColour, secondaryColour);
    }

    @Nullable
    private ItemStack getStackFromState(BlockState state) {
        if (state != null && Item.getItemFromBlock(state.getBlock()) != null) {
            Item itemFromBlock = Item.getItemFromBlock(state.getBlock());
            ItemStack defaultInstance = itemFromBlock.getDefaultInstance();
            return defaultInstance;
        }

        return ItemStack.EMPTY;
    }

    public void renderShapeTE(TileShape te, RenderTargetBase target, Trans3 t,
                              boolean renderBase, boolean renderSecondary,
                              int baseColourMult, int secondaryColourMult) {
        if (te.shape != null && (renderBase || renderSecondary)) {
            BlockState base = te.baseBlockState;
            if (base != null) {
                TextureAtlasSprite baseSprite = Utils.getSpriteForBlockState(base);
                TextureAtlasSprite secondarySprite = Utils.getSpriteForBlockState(te.secondaryBlockState);
                if (baseSprite != null) {
                    ITexture[] textures = new ITexture[4];
                    if (renderBase) {
                        textures[0] = TextureBase.fromSprite(baseSprite);
                        textures[1] = textures[0].projected();
                    }
                    if (renderSecondary) {
                        if (secondarySprite != null) {
                            textures[2] = TextureBase.fromSprite(secondarySprite);
                            textures[3] = textures[2].projected();
                        } else
                            renderSecondary = false;
                    }
                    if (renderBase && te.shape.kind.secondaryDefaultsToBase()) {
                        if (secondarySprite == null || (te.secondaryBlockState != null &&
                                RenderTypeLookup.getRenderType(te.secondaryBlockState) != RenderType.solid())) {
                            textures[2] = textures[0];
                            textures[3] = textures[1];
                            renderSecondary = renderBase;
                        }
                    }
                    te.shape.kind.renderShape(te, textures, target, t,
                            renderBase, renderSecondary,
                            baseColourMult, secondaryColourMult);
                }
            }
        }
    }

    private int getColourFromState(BlockState state) {
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        int color = blockColors.getColor(state, null, null, 0);

        return color;
    }

    @Override
    public void renderBlock(IBlockReader world, BlockPos pos, BlockState state, RenderTargetBase target,
                            RenderType layer, Trans3 t, boolean renderBase, boolean renderSecondary) {
        if (TileShape.get(world, pos) != null) {
            TileShape te = TileShape.get(world, pos);
            Trans3 t2 = t.t(te.localToGlobalRotation());
            int baseColour = renderBase ? this.getColourFromState(te.baseBlockState) : -1;
            int secondaryColour = renderSecondary ? this.getColourFromState(te.secondaryBlockState) : baseColour;

            this.renderShapeTE(TileShape.get(world, pos), target, t2,
                    renderBase, renderSecondary,
                    baseColour, secondaryColour);
        }
    }

}
