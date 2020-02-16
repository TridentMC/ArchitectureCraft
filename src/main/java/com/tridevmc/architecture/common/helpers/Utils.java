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

package com.tridevmc.architecture.common.helpers;

import com.tridevmc.architecture.common.block.BlockShape;
import com.tridevmc.architecture.common.tile.TileShape;
import javafx.geometry.Side;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Random;

import static java.lang.Math.*;

public class Utils {

    public static Random random = new Random();

    public static int playerTurn(LivingEntity player) {
        return MathHelper.floor((player.rotationYaw * 4.0 / 360.0) + 0.5) & 3;
    }

    public static int lookTurn(Vector3 look) {
        double a = atan2(look.x, look.z);
        return (int) round(a * 2 / PI) & 3;
    }

    public static boolean playerIsInCreativeMode(PlayerEntity player) {
        return (player instanceof ServerPlayerEntity)
                && ((ServerPlayerEntity) player).interactionManager.isCreative();
    }

    public static TextureAtlasSprite getSpriteForBlockState(BlockState state) {
        if (state != null)
            return Minecraft.getInstance().getBlockRendererDispatcher()
                    .getBlockModelShapes().getTexture(state);
        else
            return null;
    }

    public static TextureAtlasSprite getSpriteForPos(IWorldReader world, BlockPos pos, boolean renderPrimary) {
        BlockState blockState = world.getBlockState(pos);

        if (blockState == null)
            return null;

        if (blockState.getBlock() instanceof BlockShape) {
            TileShape shape = TileShape.get(world, pos);

            if (renderPrimary) {
                return getSpriteForBlockState(shape.baseBlockState);
            } else {
                return getSpriteForBlockState(shape.secondaryBlockState);
            }
        } else if (!renderPrimary) {
            return null;
        }

        return getSpriteForBlockState(blockState);
    }

    public static String displayNameOfBlock(Block block, int meta) {
        String name = null;
        Item item = Item.getItemFromBlock(block);
        if (item != null) {
            ItemStack stack = new ItemStack(item, 1, meta);
            name = stack.getDisplayName();
        }
        if (name == null)
            name = block.getLocalizedName();
        return I18n.format("tooltip.architecturecraft.cutfrom", name);
    }

    public static String displayNameOnlyOfBlock(Block block) {
        String name = null;
        Item item = Item.getItemFromBlock(block);
        if (item != null) {
            ItemStack stack = new ItemStack(item, 1);
            name = stack.getDisplayName().getFormattedText();
        }
        if (name == null)
            name = block.getNameTextComponent().getFormattedText();
        return name;
    }

    public static AxisAlignedBB unionOfBoxes(List<AxisAlignedBB> list) {
        AxisAlignedBB box = list.get(0);
        int n = list.size();
        for (int i = 1; i < n; i++)
            box = box.union(list.get(i));
        return box;
    }
}
