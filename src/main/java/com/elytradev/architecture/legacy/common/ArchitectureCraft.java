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

package com.elytradev.architecture.legacy.common;

import com.elytradev.architecture.common.block.BlockSawbench;
import com.elytradev.architecture.common.block.BlockShape;
import com.elytradev.architecture.common.item.ItemChisel;
import com.elytradev.architecture.common.item.ItemCladding;
import com.elytradev.architecture.common.item.ItemHammer;
import com.elytradev.architecture.legacy.base.BaseDataChannel;
import com.elytradev.architecture.legacy.base.BaseMod;
import com.elytradev.architecture.legacy.client.ArchitectureCraftClient;
import com.elytradev.architecture.common.shape.ShapeItem;
import com.elytradev.architecture.common.tile.ContainerSawbench;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import static com.elytradev.architecture.legacy.common.ArchitectureCraft.*;

@Mod(modid = MOD_ID, name = MOD_NAME, version = VERSION)
public class ArchitectureCraft extends BaseMod<ArchitectureCraftClient> {

    public static final String MOD_NAME = "ArchitectureCraft";
    public static final String MOD_ID = "architecturecraft";
    public static final String VERSION = "@VERSION@";
    public final static int guiSawbench = 1;
    public static final String RESOURCE_DOMAIN = MOD_ID + ":";
    public static ArchitectureCraft mod;

    //
    //   Blocks and Items
    //
    public static BaseDataChannel channel;


    public ArchitectureCraft() {
        super();
        mod = this;
        channel = new BaseDataChannel(MOD_ID);
    }

    @Override
    protected void registerContainers() {
        addContainer(guiSawbench, ContainerSawbench.class);
    }

    public void openGuiSawbench(World world, BlockPos pos, EntityPlayer player) {
        openGui(player, guiSawbench, world, pos);
    }

}
