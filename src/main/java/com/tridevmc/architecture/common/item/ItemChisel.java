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

import com.tridevmc.architecture.common.block.BlockHelper;
import com.tridevmc.architecture.common.tile.TileShape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ItemChisel extends Item {

    public ItemChisel() {
        super(new Item.Properties().maxStackSize(1));
    }

    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        PlayerEntity player = context.getPlayer();
        Direction side = context.getFace();
        float hitX = (float) context.getHitVec().getX();
        float hitY = (float) context.getHitVec().getY();
        float hitZ = (float) context.getHitVec().getZ();
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileShape) {
            if (!world.isRemote) {
                TileShape ste = (TileShape) te;
                ste.onChiselUse(player, side, hitX, hitY, hitZ);
            }
            return ActionResultType.SUCCESS;
        }
        BlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block == Blocks.GLASS || block == Blocks.GLASS_PANE
                || block == Blocks.GLOWSTONE || block == Blocks.ICE) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 0x3);
            if (!world.isRemote) {
                dropBlockAsItem(world, pos, state);
                world.playEvent(2001, pos, Block.getStateId(Blocks.STONE.getDefaultState())); // block breaking sound and particles
            }
            return ActionResultType.SUCCESS;
        }
        return ActionResultType.FAIL;
    }

    private void dropBlockAsItem(World world, BlockPos pos, BlockState state) {
        ItemStack stack = BlockHelper.blockStackWithState(state, 1);
        Block.spawnAsEntity(world, pos, stack);
    }

}
