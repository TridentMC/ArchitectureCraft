//------------------------------------------------------------------------------
//
//   ArchitectureCraft - Cladding Item
//
//------------------------------------------------------------------------------

package com.elytradev.architecture.legacy.common.item;

import com.elytradev.architecture.common.block.BaseBlockUtils;
import com.elytradev.architecture.common.item.ItemArchitecture;
import com.elytradev.architecture.legacy.common.helpers.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ItemCladding extends ItemArchitecture {

    public ItemStack newStack(IBlockState state, int stackSize) {
        Block block = state.getBlock();
        int meta = block.getMetaFromState(state);
        return newStack(block, meta, stackSize);
    }

    public ItemStack newStack(Block block, int meta, int stackSize) {
        ItemStack result = new ItemStack(this, stackSize, meta);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("block", BaseBlockUtils.getNameForBlock(block));
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

}
