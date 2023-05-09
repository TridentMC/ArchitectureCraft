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

import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.block.BlockHelper;
import com.tridevmc.architecture.common.block.entity.ShapeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class ItemChisel extends Item {

    public ItemChisel() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var world = context.getLevel();
        var pos = context.getClickedPos();
        var player = context.getPlayer();
        var side = context.getClickedFace();
        var hitX = (float) context.getClickLocation().x();
        var hitY = (float) context.getClickLocation().y();
        var hitZ = (float) context.getClickLocation().z();
        var te = world.getBlockEntity(pos);
        if (te instanceof ShapeBlockEntity) {
            if (!world.isClientSide()) {
                ShapeBlockEntity ste = (ShapeBlockEntity) te;
                ste.onChiselUse(player, side, hitX, hitY, hitZ);
            }
            return InteractionResult.SUCCESS;
        }
        var state = world.getBlockState(pos);
        var block = state.getBlock();
        if ((block == Blocks.GLASS) || (block == Blocks.GLASS_PANE)
                || (block == Blocks.GLOWSTONE) || (block == Blocks.ICE)) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), 0x3);
            if (!world.isClientSide()) {
                this.dropBlockAsItem(world, pos, state);
                world.levelEvent(2001, pos, Block.getId(Blocks.STONE.defaultBlockState())); // block breaking sound and particles
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    private void dropBlockAsItem(Level world, BlockPos pos, BlockState state) {
        ItemStack stack = BlockHelper.blockStackWithState(state, 1);
        Block.popResource(world, pos, stack);
    }

}
