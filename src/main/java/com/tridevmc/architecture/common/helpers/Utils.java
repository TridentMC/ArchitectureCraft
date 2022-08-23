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
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.CommonLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Random;

import static java.lang.Math.*;

public class Utils {

    public static Random random = new Random();

    public static int playerTurn(LivingEntity player) {
        return floor((player.rotationYaw * 4.0 / 360.0) + 0.5) & 3;
    }

    public static int lookTurn(Vector3 look) {
        double a = atan2(look.x, look.z);
        return (int) round(a * 2 / PI) & 3;
    }

    public static boolean playerIsInCreativeMode(Player player) {
        return (player instanceof ServerPlayer)
                && player.isCreative();
    }

    public static TextureAtlasSprite getSpriteForBlockState(BlockState state) {
        if (state != null)
            return Minecraft.getInstance().getBlockRenderer()
                    .getBlockModelShaper().getTexture(state);
        else
            return null;
    }

    public static TextureAtlasSprite getSpriteForPos(CommonLevelAccessor world, BlockPos pos, boolean renderPrimary) {
        BlockState blockState = world.getBlockState(pos);

        if (blockState.isAir())
            return null;

        if (blockState.getBlock() instanceof BlockShape) {
            TileShape shape = TileShape.get(world, pos);

            if (renderPrimary) {
                return getSpriteForBlockState(shape.getBaseBlockState());
            } else {
                return getSpriteForBlockState(shape.getSecondaryBlockState());
            }
        } else if (!renderPrimary) {
            return null;
        }

        return getSpriteForBlockState(blockState);
    }

    @OnlyIn(Dist.CLIENT)
    public static int getColourFromState(BlockState state) {
        if (state == null)
            return -1;

        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        return blockColors.getColor(state, null, null, 0);
    }

    public static String displayNameOnlyOfBlock(Block block) {
        String name = null;
        Item item = Item.byBlock(block);
        if (item != Items.AIR) {
            ItemStack stack = new ItemStack(item, 1);
            name = stack.getDisplayName().getString();
        }
        if (name == null)
            name = block.getName().getString();
        return name;
    }

    public static AABB unionOfBoxes(List<AABB> list) {
        AABB box = list.get(0);
        int n = list.size();
        for (int i = 1; i < n; i++)
            box = box.minmax(list.get(i));
        return box;
    }
}
