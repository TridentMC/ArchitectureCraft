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

import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

public class RenderCladding {

    // TODO: Cladding needs to be reworked for baked conversion.
    //public void renderItemStack(ItemStack stack, RenderTargetBase target, Trans3 t) {
    //    CompoundNBT tag = stack.getTag();
    //    if (tag != null) {
    //        BlockState state = Block.getStateById(tag.getInt("block"));
    //        TextureAtlasSprite sprite = Utils.getSpriteForBlockState(state);
    //        if (sprite != null) {
    //            int colourMult = Minecraft.getInstance().getItemColors().getColor(stack, 0);
    //            ITexture texture = TextureBase.fromSprite(sprite);
    //            IRenderableModel model = ClientProxy.RENDERING_MANAGER.getModel("shape/cladding.objson");
    //            model.render(t, target, colourMult, colourMult, texture);
    //        }
    //    }
    //}

    @Nullable
    private ItemStack getStackFromState(BlockState state) {
        if (state != null) {
            Item itemFromBlock = Item.getItemFromBlock(state.getBlock());
            ItemStack defaultInstance = itemFromBlock.getDefaultInstance();
            return defaultInstance;
        }

        return ItemStack.EMPTY;
    }

}

