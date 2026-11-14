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

package com.tridevmc.architecture.legacy.common.block;

import com.tridevmc.architecture.common.utils.MiscUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;

@Deprecated
public class LegacyBlockHelper {

    private static final RandomSource RANDOM = RandomSource.create();

    public static String getNameForBlock(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).toString();
    }

    /**
     * Test whether a block is receiving a redstone signal from a source
     * other than itself. For blocks that can both send and receive in
     * any direction.
     **/
    public static boolean blockIsGettingExternallyPowered(Level level, BlockPos pos) {
        for (Direction side : MiscUtils.facings) {
            if (isPoweringSide(level, pos.relative(side), side))
                return true;
        }
        return false;
    }

    static boolean isPoweringSide(Level level, BlockPos pos, Direction side) {
        var state = level.getBlockState(pos);
        if (state.getSignal(level, pos, side) > 0)
            return true;
        if (state.shouldCheckWeakPower(level, pos, side)) {
            for (Direction side2 : MiscUtils.facings) {
                if (side2 != side.getOpposite())
                    if (level.getDirectSignal(pos.relative(side2), side2) > 0)
                        return true;
            }
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

    public static boolean blockCanRenderInLayer(BlockAndTintGetter level, BlockPos pos, BlockState state, ChunkSectionLayer layer) {
        // What a beautiful method, my favourite part is the newlines.
        var parts = new ArrayList<BlockStateModelPart>();
        Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state)
                .collectParts(level, pos, state, RANDOM, parts);
        for (var part : parts) {
            if (part.getQuads(null).stream().anyMatch(quad -> quad.materialInfo().layer() == layer))
                return true;
            for (Direction side : Direction.values()) {
                if (part.getQuads(side).stream().anyMatch(quad -> quad.materialInfo().layer() == layer))
                    return true;
            }
        }
        return false;
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
