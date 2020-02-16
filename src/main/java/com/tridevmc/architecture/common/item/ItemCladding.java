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

package com.tridevmc.architecture.common.item;

import com.tridevmc.architecture.common.helpers.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCladding extends ItemArchitecture {

    public ItemCladding() {
        super(new Item.Properties());
    }

    public ItemStack newStack(BlockState state, int stackSize) {
        ItemStack result = new ItemStack(this, stackSize);
        CompoundNBT nbt = new CompoundNBT();
        nbt.putInt("block", Block.getStateId(state));
        result.setTag(nbt);
        return result;
    }

    public ItemStack newStack(Block block, int stackSize) {
        return this.newStack(block.getDefaultState(), stackSize);
    }

    public BlockState blockStateFromStack(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag != null) {
            BlockState state = Block.getStateById(tag.getInt("block"));
            return state;
        }
        return null;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> lines, ITooltipFlag flagIn) {
        lines.set(0, super.getDisplayName(stack));

        CompoundNBT tag = stack.getTag();
        if (tag != null) {
            BlockState state = Block.getStateById(tag.getInt("block"));
            if (!state.isAir())
                lines.add(new StringTextComponent(Utils.displayNameOnlyOfBlock(state.getBlock())));
        }
    }

    @Override
    protected boolean isInGroup(ItemGroup group) {
        return false;
    }

    @Override
    public int getNumSubtypes() {
        return 16;
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag == null)
            return super.getDisplayName(stack);

        BlockState state = Block.getStateById(tag.getInt("block"));
        return new TranslationTextComponent("item.architecturecraft.cladding.name", Utils.displayNameOnlyOfBlock(state.getBlock()));
    }
}
