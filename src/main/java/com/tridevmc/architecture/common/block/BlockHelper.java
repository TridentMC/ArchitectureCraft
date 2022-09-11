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

package com.tridevmc.architecture.common.block;

import com.tridevmc.architecture.common.utils.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class BlockHelper {

    private static final RandomSource RANDOM = RandomSource.create();

    public static String getNameForBlock(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block).toString();
    }

    /*
     *   Test whether a block is receiving a redstone signal from a source
     *   other than itself. For blocks that can both send and receive in
     *   any direction.
     */
    public static boolean blockIsGettingExternallyPowered(Level level, BlockPos pos) {
        for (Direction side : MiscUtils.facings) {
            if (isPoweringSide(level, pos.relative(side), side))
                return true;
        }
        return false;
    }

    static boolean isPoweringSide(Level level, BlockPos pos, Direction side) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        if (block.getSignal(state, level, pos, side) > 0)
            return true;
        if (block.shouldCheckWeakPower(state, level, pos, side)) {
            for (Direction side2 : MiscUtils.facings)
                if (side2 != side.getOpposite())
                    if (level.getDirectSignal(pos.relative(side2), side2) > 0)
                        return true;
        }
        return false;
    }

    public static BlockState getBlockStateFromItemStack(ItemStack stack) {
        Block block = Block.byItem(stack.getItem());
        return block.defaultBlockState();
    }

    // -------------------- 1.7/1.8 Compatibility --------------------

    public static Block getWorldBlock(BlockAndTintGetter level, BlockPos pos) {
        return level.getBlockState(pos).getBlock();
    }

    public static BlockState getWorldBlockState(BlockAndTintGetter level, BlockPos pos) {
        return level.getBlockState(pos);
    }

    public static void setWorldBlockState(Level level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 3);
    }

    public static void notifyWorldNeighborsOfStateChange(Level level, BlockPos pos, Block block) {
        level.updateNeighborsAt(pos, block);
    }

    public static BlockEntity getWorldBlockEntity(BlockAndTintGetter level, BlockPos pos) {
        return level.getBlockEntity(pos);
    }

    public static Level getBlockEntityWorld(BlockEntity te) {
        return te.getLevel();
    }

    public static BlockPos getBlockEntityPos(BlockEntity te) {
        return te.getBlockPos();
    }

    public static boolean blockCanRenderInLayer(BlockState state, RenderType layer) {
        // What a beautiful method, my favourite part is the newlines.
        return Minecraft.getInstance()
                .getBlockRenderer()
                .getBlockModelShaper()
                .getBlockModel(state)
                .getRenderTypes(state, RANDOM, ModelData.EMPTY)
                .contains(layer);
    }

    public static ItemStack blockStackWithState(BlockState state, int size) {
        Block block = state.getBlock();
        return new ItemStack(block, size);
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

    public static void markBlockForUpdate(Level level, BlockPos pos) {
        level.setBlocksDirty(pos, Blocks.AIR.defaultBlockState(), level.getBlockState(pos));
        if (!level.isClientSide()) {
            BlockState state = level.getBlockState(pos);
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

}
