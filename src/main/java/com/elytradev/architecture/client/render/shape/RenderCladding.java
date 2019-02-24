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

import com.elytradev.architecture.client.proxy.ClientProxy;
import com.elytradev.architecture.client.render.ICustomRenderer;
import com.elytradev.architecture.client.render.model.IArchitectureModel;
import com.elytradev.architecture.client.render.target.RenderTargetBase;
import com.elytradev.architecture.client.render.texture.ITexture;
import com.elytradev.architecture.client.render.texture.TextureBase;
import com.elytradev.architecture.common.helpers.Trans3;
import com.elytradev.architecture.common.helpers.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;

public class RenderCladding implements ICustomRenderer {

    @Override
    public void renderBlock(IBlockReader world, BlockPos pos, IBlockState state,
                            RenderTargetBase target, BlockRenderLayer layer, Trans3 t) {
        //NOOP
    }

    @Override
    public void renderBlock(IBlockReader world, BlockPos pos, IBlockState state, RenderTargetBase target, BlockRenderLayer layer, Trans3 t, boolean renderPrimary, boolean renderSecondary) {
        //NOOP
    }

    @Override
    public void renderItemStack(ItemStack stack, RenderTargetBase target, Trans3 t) {
        NBTTagCompound nbt = stack.getTag();
        if (nbt != null) {
            String blockName = nbt.getString("block");
            int meta = stack.getDamage();
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(blockName));
            if (block != null) {
                IBlockState state = block.getStateFromMeta(meta);
                if (state != null) {
                    TextureAtlasSprite sprite = Utils.getSpriteForBlockState(state);
                    if (sprite != null) {
                        int colourMult = Minecraft.getInstance().getItemColors().getColor(stack, 0);
                        ITexture texture = TextureBase.fromSprite(sprite);
                        IArchitectureModel model = ClientProxy.RENDERING_MANAGER.getModel("shape/cladding.objson");
                        model.render(t, target, colourMult, colourMult, texture);
                    }
                }
            }
        }
    }

    @Nullable
    private ItemStack getStackFromState(IBlockState state) {
        if (state != null && Item.getItemFromBlock(state.getBlock()) != null) {
            Item itemFromBlock = Item.getItemFromBlock(state.getBlock());
            ItemStack defaultInstance = itemFromBlock.getDefaultInstance();
            return defaultInstance;
        }

        return null;
    }

}

