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

package com.elytradev.architecture.common.block;

import com.elytradev.architecture.common.utils.MiscUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class BlockHelper {

    public static String getNameForBlock(Block block) {
        return Block.REGISTRY.getKey(block).toString();
    }

    /*
     *   Test whether a block is receiving a redstone signal from a source
     *   other than itself. For blocks that can both send and receive in
     *   any direction.
     */
    public static boolean blockIsGettingExternallyPowered(World world, BlockPos pos) {
        for (EnumFacing side : MiscUtils.facings) {
            if (isPoweringSide(world, pos.offset(side), side))
                return true;
        }
        return false;
    }

    static boolean isPoweringSide(World world, BlockPos pos, EnumFacing side) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block.getWeakPower(state, world, pos, side) > 0)
            return true;
        if (block.shouldCheckWeakPower(state, world, pos, side)) {
            for (EnumFacing side2 : MiscUtils.facings)
                if (side2 != side.getOpposite())
                    if (world.getStrongPower(pos.offset(side2), side2) > 0)
                        return true;
        }
        return false;
    }

    public static IBlockState getBlockStateFromItemStack(ItemStack stack) {
        Block block = Block.getBlockFromItem(stack.getItem());
        int meta = 0;
        if (stack.getItem().getHasSubtypes())
            meta = stack.getItemDamage() & 0xf;
        return block.getStateFromMeta(meta);
    }

    // -------------------- 1.7/1.8 Compatibility --------------------

    public static IBlockState getBlockStateFromMeta(Block block, int meta) {
        return block.getStateFromMeta(meta);
    }

    public static int getMetaFromBlockState(IBlockState state) {
        return state.getBlock().getMetaFromState(state);
    }

    public static Block getWorldBlock(IBlockReader world, BlockPos pos) {
        return world.getBlockState(pos).getBlock();
    }

    public static IBlockState getWorldBlockState(IBlockReader world, BlockPos pos) {
        return world.getBlockState(pos);
    }

    public static void setWorldBlockState(World world, BlockPos pos, IBlockState state) {
        world.setBlockState(pos, state, 3);
    }

    public static void notifyWorldNeighborsOfStateChange(World world, BlockPos pos, Block block) {
        world.notifyNeighborsOfStateChange(pos, block, true);
    }

    public static TileEntity getWorldTileEntity(IBlockReader world, BlockPos pos) {
        return world.getTileEntity(pos);
    }

    public static World getTileEntityWorld(TileEntity te) {
        return te.getWorld();
    }

    public static BlockPos getTileEntityPos(TileEntity te) {
        return te.getPos();
    }

    public static boolean blockCanRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return state.getBlock().canRenderInLayer(state, layer);
    }

    public static ItemStack blockStackWithState(IBlockState state, int size) {
        Block block = state.getBlock();
        int meta = block.getMetaFromState(state);
        return new ItemStack(block, size, meta);
    }

    public static BlockPos readBlockPos(DataInput data) {
        try {
            int x = data.readInt();
            int y = data.readInt();
            int z = data.readInt();
            return new BlockPos(x, y, z);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeBlockPos(DataOutput data, BlockPos pos) {
        try {
            data.writeInt(pos.getX());
            data.writeInt(pos.getY());
            data.writeInt(pos.getZ());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void markBlockForUpdate(World world, BlockPos pos) {
        world.markBlockRangeForRenderUpdate(pos, pos);
        if (!world.isRemote) {
            IBlockState state = world.getBlockState(pos);
            world.notifyBlockUpdate(pos, state, state, 3);
        }
    }

}
