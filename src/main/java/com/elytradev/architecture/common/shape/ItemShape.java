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

package com.elytradev.architecture.common.shape;

import com.elytradev.architecture.common.ArchitectureContent;
import com.elytradev.architecture.common.helpers.Utils;
import com.elytradev.architecture.common.helpers.Vector3;
import com.elytradev.architecture.common.tile.TileShape;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ItemShape extends ItemBlock {

    static Random rand = new Random();

    public ItemShape(Block block) {
        super(block);
        this.setCreativeTab(ArchitectureContent.SHAPE_TAB);
    }

    @Override
    public boolean getHasSubtypes() {
        return true;
    }

    /**
     * Converts the given ItemStack damage value into a metadata value to be placed in the world when this Item is
     * placed as a Block (mostly used with ItemBlocks).
     */
    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World world, BlockPos pos,
                                EnumFacing face, float hitX, float hitY, float hitZ, IBlockState newState) {
        if (!world.setBlockState(pos, newState, 3))
            return false;
        Vec3i direction = Vector3.getDirectionVec(face);
        Vector3 hit = new Vector3(hitX - direction.getX() - 0.5, hitY - direction.getY() - 0.5, hitZ - direction.getZ() - 0.5);
        TileShape shape = TileShape.get(world, pos);
        if (shape != null) {
            shape.readFromItemStack(stack);
            if (shape.getShape() != null) {
                BlockPos neighbourPos = shape.getPos().offset(face.getOpposite());
                IBlockState neighbourState = world.getBlockState(neighbourPos);
                TileEntity neighbourTile = world.getTileEntity(neighbourPos);
                shape.getShape().orientOnPlacement(player, shape, neighbourPos, neighbourState, neighbourTile, face, hit);
            }
        }
        return true;
    }

    @Override
    public boolean getShareTag() {
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> lines, ITooltipFlag flagIn) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null) {
            int id = tag.getInteger("Shape");
            EnumShape shape = EnumShape.forId(id);
            if (shape != null)
                lines.set(0, shape.getLocalizedShapeName());
            else
                lines.set(0, lines.get(0) + " (" + id + ")");
            Block baseBlock = Block.getBlockFromName(tag.getString("BaseName"));
            int baseMetadata = tag.getInteger("BaseData");
            if (baseBlock != null)
                lines.add(Utils.displayNameOfBlock(baseBlock, baseMetadata));
        }
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab.equals(ArchitectureContent.SHAPE_TAB)) {
            for (EnumShape shape : EnumShape.values()) {
                if (shape.isCladding())
                    continue;

                ItemStack defaultStack = new ItemStack(this, 1);
                NBTTagCompound tag = new NBTTagCompound();
                tag.setInteger("Shape", shape.id);
                tag.setString("BaseName", Blocks.PLANKS.getRegistryName().toString());
                tag.setInteger("BaseData", 0);
                defaultStack.setTagCompound(tag);
                items.add(defaultStack);
            }
        }
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack defaultStack = new ItemStack(this, 1);
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("Shape", EnumShape.ROOF_TILE.id);
        tag.setString("BaseName", Blocks.PLANKS.getRegistryName().toString());
        tag.setInteger("BaseData", 0);
        defaultStack.setTagCompound(tag);

        return defaultStack;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null)
            return super.getItemStackDisplayName(stack);

        int id = tag.getInteger("Shape");
        EnumShape shape = EnumShape.forId(id);
        Block baseBlock = Block.getBlockFromName(tag.getString("BaseName"));
        int baseMetadata = tag.getInteger("BaseData");
        return shape.getLocalizedShapeName() + ": " + Utils.displayNameOnlyOfBlock(baseBlock, baseMetadata);
    }
}
