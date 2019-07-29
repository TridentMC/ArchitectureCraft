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

package com.elytradev.architecture.common.item;

import com.elytradev.architecture.common.block.BlockHelper;
import com.elytradev.architecture.common.helpers.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCladding extends ItemArchitecture {

    public ItemStack newStack(IBlockState state, int stackSize) {
        Block block = state.getBlock();
        int meta = block.getMetaFromState(state);
        return this.newStack(block, meta, stackSize);
    }

    public ItemStack newStack(Block block, int meta, int stackSize) {
        ItemStack result = new ItemStack(this, stackSize, meta);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("block", BlockHelper.getNameForBlock(block));
        result.setTagCompound(nbt);
        return result;
    }

    public IBlockState blockStateFromStack(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null) {
            Block block = Block.getBlockFromName(nbt.getString("block"));
            if (block != null)
                return block.getStateFromMeta(stack.getItemDamage());
        }
        return null;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> lines, ITooltipFlag flagIn) {
        lines.set(0, super.getItemStackDisplayName(stack));

        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null) {
            Block block = Block.getBlockFromName(tag.getString("block"));
            int meta = stack.getItemDamage();
            if (block != null)
                lines.add(Utils.displayNameOfBlock(block, meta));
        }
    }

    @Override
    protected boolean isInCreativeTab(CreativeTabs targetTab) {
        return false;
    }

    @Override
    public int getNumSubtypes() {
        return 16;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
            return super.getItemStackDisplayName(stack);

        Block baseBlock = Block.getBlockFromName(tag.getString("block"));
        return I18n.translateToLocal("item.architecturecraft.cladding.name") + ": " + Utils.displayNameOnlyOfBlock(baseBlock, stack.getMetadata());
    }
}
