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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCladding extends ItemArchitecture {

    public ItemCladding() {
        super(new Item.Properties());
    }

    public ItemStack newStack(BlockState state, int stackSize) {
        ItemStack result = new ItemStack(this, stackSize);
        var nbt = new CompoundTag();
        nbt.putInt("block", Block.getId(state));
        result.setTag(nbt);
        return result;
    }

    public ItemStack newStack(Block block, int stackSize) {
        return this.newStack(block.defaultBlockState(), stackSize);
    }

    public BlockState blockStateFromStack(ItemStack stack) {
        var tag = stack.getTag();
        if (tag != null) {
            BlockState state = Block.stateById(tag.getInt("block"));
            return state;
        }
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> lines, TooltipFlag flagIn) {
        lines.set(0, super.getName(stack));

        var tag = stack.getTag();
        if (tag != null) {
            BlockState state = Block.stateById(tag.getInt("block"));
            if (!state.isAir())
                lines.add(Component.literal(Utils.displayNameOnlyOfBlock(state.getBlock())));
        }
    }

    @Override
    protected boolean allowedIn(CreativeModeTab group) {
        return false;
    }

    @Override
    public int getNumSubtypes() {
        return 16;
    }


    @Override
    public Component getName(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null)
            return super.getName(stack);

        BlockState state = Block.stateById(tag.getInt("block"));
        return Component.translatable("item.architecturecraft.cladding.name", Utils.displayNameOnlyOfBlock(state.getBlock()));
    }
}
