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

import com.google.common.collect.Maps;
import com.tridevmc.architecture.common.block.BlockShape;
import com.tridevmc.architecture.common.block.entity.BlockEntityShape;
import com.tridevmc.architecture.common.helpers.Utils;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.core.ArchitectureLog;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class ItemShape extends BlockItem {

    private static final Map<EnumShape, ItemShape> SHAPE_ITEMS = Maps.newHashMap();
    private final EnumShape shape;

    public ItemShape(BlockShape block) {
        super(block, new Item.Properties());
        this.shape = block.getShape();
        SHAPE_ITEMS.put(block.getShape(), this);
    }

    @Nullable
    public static ItemShape getItemFromShape(EnumShape shape) {
        return SHAPE_ITEMS.getOrDefault(shape, null);
    }

    @Nonnull
    public static ItemStack createStack(EnumShape shape, BlockState baseBlockState) {
        return createStack(shape, baseBlockState, 1);
    }

    @Nonnull
    public static ItemStack createStack(EnumShape shape, BlockState baseBlockState, int count) {
        var tag = new CompoundTag();
        var stack = new ItemStack(SHAPE_ITEMS.get(shape), count);
        tag.putInt("BaseBlockState", Block.getId(baseBlockState));
        stack.setTag(tag);
        return stack;
    }

    @Nullable
    public static EnumShape getShapeFromStack(ItemStack stack) {
        var item = stack.getItem();
        if (item instanceof ItemShape) {
            return ((ItemShape) item).shape;
        } else {
            return null;
        }
    }

    @Nonnull
    public static BlockState getStateFromStack(ItemStack stack) {
        var tag = stack.getTag();
        return Block.stateById(tag.getInt("BaseBlockState"));
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState newState) {
        var result = super.placeBlock(context, newState);
        if (result && !context.getLevel().isClientSide()) {
            BlockEntityShape.getAtOptionally(context.getLevel(), context.getClickedPos()).ifPresentOrElse(shape -> {
                shape.setBaseMaterialState(getStateFromStack(context.getItemInHand()));
            }, () -> ArchitectureLog.error("Failed to place shape block entity at position: " + context.getClickedPos()));
        }
        return result;
    }

    @Nullable
    @Override
    public CompoundTag getShareTag(ItemStack stack) {
        return stack.getTag();
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> lines, TooltipFlag flagIn) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            if (this.shape != null)
                lines.set(0, Component.translatable(this.shape.getLocalizationKey()));
            else
                lines.set(0, Component.literal(lines.get(0).getString() + " (" + -1 + ")"));
            Block baseBlock = getStateFromStack(stack).getBlock();
            lines.add(Component.literal(Utils.displayNameOnlyOfBlock(baseBlock)));
        }
    }

    @Override
    public ItemStack getDefaultInstance() {
        return createStack(EnumShape.ROOF_TILE, Blocks.OAK_PLANKS.defaultBlockState());
    }

    @Override
    public Component getName(ItemStack stack) {
        var tag = stack.getTag();
        if (tag == null)
            return super.getName(stack);

        BlockState state = getStateFromStack(stack);
        return Component.literal(this.shape.getName() + ": " + Utils.displayNameOnlyOfBlock(state.getBlock()));
    }
}