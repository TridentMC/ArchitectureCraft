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

package com.tridevmc.architecture.common.shape;

import com.google.common.collect.Maps;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.block.BlockShape;
import com.tridevmc.architecture.common.block.entity.ShapeBlockEntity;
import com.tridevmc.architecture.common.helpers.Utils;
import com.tridevmc.architecture.legacy.math.LegacyVector3;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
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

    public ItemShape(BlockShape block, Item.Properties builder) {
        super(block, builder);
        this.shape = block.getArchitectureShape();
        SHAPE_ITEMS.put(block.getArchitectureShape(), this);
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
        var player = context.getPlayer();
        var world = context.getLevel();
        var pos = context.getClickedPos();
        var face = context.getNearestLookingDirection();
        var stack = context.getItemInHand();
        var hitX = context.getClickLocation().x();
        var hitY = context.getClickLocation().y();
        var hitZ = context.getClickLocation().z();
        if (!world.setBlock(pos, newState, 3))
            return false;
        var dirVec = LegacyVector3.getDirectionVec(face);
        var hit = new LegacyVector3(hitX - dirVec.getX() - 0.5, hitY - dirVec.getY() - 0.5, hitZ - dirVec.getZ() - 0.5);
        var tile = ShapeBlockEntity.get(world, pos);
        if (tile != null) {
            var state = getStateFromStack(stack);
            tile.setBaseBlockState(state);
            var neighbourPos = tile.getBlockPos().relative(face.getOpposite());
            var neighbourState = world.getBlockState(neighbourPos);
            var neighbourTile = world.getBlockEntity(neighbourPos);
            this.shape.orientOnPlacement(player, tile, neighbourPos, neighbourState, neighbourTile, face, hit);
        }
        return true;
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
                lines.set(0, Component.literal(this.shape.getLocalizedShapeName()));
            else
                lines.set(0, Component.literal(lines.get(0).getString() + " (" + -1 + ")"));
            Block baseBlock = getStateFromStack(stack).getBlock();
            lines.add(Component.literal(Utils.displayNameOnlyOfBlock(baseBlock)));
        }
    }

    @Override
    public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (group == ArchitectureMod.CONTENT.SHAPE_TAB) {
            if (this.shape.isCladding())
                return;

            items.add(createStack(this.shape, Blocks.OAK_PLANKS.defaultBlockState()));
        }

        super.fillItemCategory(group, items);
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
        return Component.literal(this.shape.getLocalizedShapeName() + ": " + Utils.displayNameOnlyOfBlock(state.getBlock()));
    }
}