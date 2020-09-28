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
import com.tridevmc.architecture.common.helpers.Utils;
import com.tridevmc.architecture.common.helpers.Vector3;
import com.tridevmc.architecture.common.tile.TileShape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class ItemShape extends BlockItem {

    private static Map<EnumShape, ItemShape> SHAPE_ITEMS = Maps.newHashMap();
    private EnumShape shape;

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
        CompoundNBT tag = new CompoundNBT();
        ItemStack stack = new ItemStack(SHAPE_ITEMS.get(shape), count);
        tag.putInt("BaseBlockState", Block.getStateId(baseBlockState));
        stack.setTag(tag);
        return stack;
    }

    @Nullable
    public static EnumShape getShapeFromStack(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemShape) {
            return ((ItemShape) item).shape;
        } else {
            return null;
        }
    }

    @Nonnull
    public static BlockState getStateFromStack(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        return Block.getStateById(tag.getInt("BaseBlockState"));
    }

    @Override
    protected boolean placeBlock(BlockItemUseContext context, BlockState newState) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        BlockPos pos = context.getPos();
        Direction face = context.getFace();
        ItemStack stack = context.getItem();
        double hitX = context.getHitVec().getX();
        double hitY = context.getHitVec().getY();
        double hitZ = context.getHitVec().getZ();
        if (!world.setBlockState(pos, newState, 3))
            return false;
        Vector3i dirVec = Vector3.getDirectionVec(face);
        Vector3 hit = new Vector3(hitX - dirVec.getX() - 0.5, hitY - dirVec.getY() - 0.5, hitZ - dirVec.getZ() - 0.5);
        TileShape tile = TileShape.get(world, pos);
        if (tile != null) {
            BlockState state = getStateFromStack(stack);
            tile.setBaseBlockState(state);
            BlockPos neighbourPos = tile.getPos().offset(face.getOpposite());
            BlockState neighbourState = world.getBlockState(neighbourPos);
            TileEntity neighbourTile = world.getTileEntity(neighbourPos);
            this.shape.orientOnPlacement(player, tile, neighbourPos, neighbourState, neighbourTile, face, hit);
        }
        return true;
    }

    @Nullable
    @Override
    public CompoundNBT getShareTag(ItemStack stack) {
        return stack.getTag();
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> lines, ITooltipFlag flagIn) {
        CompoundNBT tag = stack.getTag();
        if (tag != null) {
            if (this.shape != null)
                lines.set(0, new StringTextComponent(this.shape.getLocalizedShapeName()));
            else
                lines.set(0, new StringTextComponent(lines.get(0).getString() + " (" + -1 + ")"));
            Block baseBlock = getStateFromStack(stack).getBlock();
            lines.add(new StringTextComponent(Utils.displayNameOnlyOfBlock(baseBlock)));
        }
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (group == ArchitectureMod.CONTENT.SHAPE_TAB) {
            if (this.shape.isCladding())
                return;

            items.add(createStack(this.shape, Blocks.OAK_PLANKS.getDefaultState()));
        }

        super.fillItemGroup(group, items);
    }

    @Override
    public ItemStack getDefaultInstance() {
        return createStack(EnumShape.ROOF_TILE, Blocks.OAK_PLANKS.getDefaultState());
    }

    @Override
    public ITextComponent getDisplayName(ItemStack stack) {
        CompoundNBT tag = stack.getTag();
        if (tag == null)
            return super.getDisplayName(stack);

        BlockState state = getStateFromStack(stack);
        return new StringTextComponent(this.shape.getLocalizedShapeName() + ": " + Utils.displayNameOnlyOfBlock(state.getBlock()));
    }
}