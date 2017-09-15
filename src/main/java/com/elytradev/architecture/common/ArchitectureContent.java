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

package com.elytradev.architecture.common;

import com.elytradev.architecture.common.block.BlockSawbench;
import com.elytradev.architecture.common.block.BlockShape;
import com.elytradev.architecture.common.item.ItemArchitecture;
import com.elytradev.architecture.common.item.ItemChisel;
import com.elytradev.architecture.common.item.ItemCladding;
import com.elytradev.architecture.common.item.ItemHammer;
import com.elytradev.architecture.common.shape.ShapeItem;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashMap;
import java.util.List;

import static com.elytradev.architecture.common.ArchitectureMod.MOD_ID;

public class ArchitectureContent {

    public static final CreativeTabs creativeTab = new CreativeTabs("architecture") {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(Item.REGISTRY.getObject(new ResourceLocation(MOD_ID, "hammer")));
        }
    };

    public BlockSawbench blockSawbench;
    public Block blockShape;
    public Item itemSawblade;
    public Item itemLargePulley;
    public Item itemChisel;
    public Item itemHammer;
    public ItemCladding itemCladding;

    private static final String REGISTRY_PREFIX = MOD_ID.toLowerCase();
    public static HashMap<String, Block> registeredBlocks;
    public static HashMap<String, Item> registeredItems;

    private static List<Item> itemBlocksToRegister;
    private int recipeID = 0;

    @SubscribeEvent
    public void onBlockRegister(RegistryEvent.Register<Block> event) {
        IForgeRegistry<Block> registry = event.getRegistry();
        this.blockSawbench = registerBlock(registry, "sawbench", new BlockSawbench());
        this.blockShape = registerBlock(registry, "shape", new BlockShape(), ShapeItem.class);
    }

    @SubscribeEvent
    public void onItemRegister(RegistryEvent.Register<Item> event) {
        IForgeRegistry<Item> registry = event.getRegistry();
        this.itemSawblade = registerItem(registry, "sawblade");
        this.itemLargePulley = registerItem(registry, "largePulley");
        this.itemChisel = registerItem(registry, "chisel", new ItemChisel());
        this.itemHammer = registerItem(registry, "hammer", new ItemHammer());
        this.itemCladding = registerItem(registry, "cladding", new ItemCladding());
    }

    @SubscribeEvent
    public void onRecipeRegister(RegistryEvent.Register<IRecipe> event) {
        IForgeRegistry<IRecipe> registry = event.getRegistry();
        ItemStack orangeDye = new ItemStack(Items.DYE, 1, EnumDyeColor.ORANGE.getDyeDamage());
        registerShapedRecipe(registry, blockSawbench, 1,
                "I*I",
                "/0/",
                "/_/",
                'I', Items.IRON_INGOT, '*', itemSawblade, '/', Items.STICK,
                '_', Blocks.WOODEN_PRESSURE_PLATE, '0', itemLargePulley);
        registerShapedRecipe(registry, itemSawblade, 1,
                " I ",
                "I/I",
                " I ",
                'I', Items.IRON_INGOT, '/', Items.STICK);
        registerShapedRecipe(registry, itemLargePulley, 1,
                " W ",
                "W/W",
                " W ",
                'W', Blocks.PLANKS, '/', Items.STICK);
        registerShapedRecipe(registry, itemChisel, 1,
                "I ",
                "ds",
                'I', Items.IRON_INGOT, 's', Items.STICK, 'd', orangeDye);
        registerShapedRecipe(registry, itemHammer, 1,
                "II ",
                "dsI",
                "ds ",
                'I', Items.IRON_INGOT, 's', Items.STICK, 'd', orangeDye);
    }

    public void preInit(FMLPreInitializationEvent e) {

    }

    public void init(FMLInitializationEvent e) {

    }

    public void postInit(FMLPostInitializationEvent e) {

    }

    private void registerShapedRecipe(IForgeRegistry<IRecipe> registry, Block out, Object... input) {
        registerShapedRecipe(registry, new ItemStack(out, 1), input);
    }

    private void registerShapedRecipe(IForgeRegistry<IRecipe> registry, Item out, Object... input) {
        registerShapedRecipe(registry, new ItemStack(out, 1), input);
    }

    private void registerShapedRecipe(IForgeRegistry<IRecipe> registry, ItemStack out, Object... input) {
        ResourceLocation resourceLocation = new ResourceLocation(MOD_ID, out.getUnlocalizedName() + recipeID++);
        registry.register(new ShapedOreRecipe(resourceLocation, out, input).setRegistryName(resourceLocation));
    }

    private void registerShapelessRecipe(IForgeRegistry<IRecipe> registry, ItemStack out, Object... input) {
        ResourceLocation resourceLocation = new ResourceLocation(MOD_ID, out.getUnlocalizedName() + recipeID++);
        registry.register(new ShapelessOreRecipe(resourceLocation, out, input).setRegistryName(resourceLocation));
    }

    private <T extends Block> T registerBlock(IForgeRegistry<Block> registry, String id, T block) {
        return registerBlock(registry, id, block, true);
    }

    private <T extends Block> T registerBlock(IForgeRegistry<Block> registry, String id, T block, boolean withItemBlock) {
        block.setUnlocalizedName("architecturecraft." + id);
        block.setRegistryName(REGISTRY_PREFIX, id);
        block.setCreativeTab(creativeTab);
        registry.register(block);
        if (withItemBlock)
            itemBlocksToRegister.add(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        registeredBlocks.put(id, block);
        return (T) registeredBlocks.get(id);
    }

    private <T extends Block> T registerBlock(IForgeRegistry<Block> registry, String id, T block, Class<? extends ItemBlock> itemBlockClass) {
        try {
            block.setUnlocalizedName("architecturecraft." + id);
            block.setRegistryName(REGISTRY_PREFIX, id);
            registry.register(block);

            ItemBlock itemBlock = itemBlockClass.getDeclaredConstructor(Block.class).newInstance(block);
            itemBlock.setRegistryName(REGISTRY_PREFIX, id);
            itemBlock.setCreativeTab(creativeTab);
            itemBlocksToRegister.add(itemBlock);
            registeredBlocks.put(id, block);
        } catch (Exception e) {
            ArchitectureMod.LOG.error("Caught exception while registering " + block, e);
        }

        return (T) registeredBlocks.get(id);
    }

    private <T extends Item> T registerItem(IForgeRegistry<Item> registry, String id) {
        ItemArchitecture item = new ItemArchitecture();
        item.setUnlocalizedName("architecturecraft." + id);
        item.setRegistryName(REGISTRY_PREFIX, id);
        item.setCreativeTab(creativeTab);
        registry.register(item);
        registeredItems.put(id, item);

        return (T) registeredItems.get(id);
    }

    private <T extends Item> T registerItem(IForgeRegistry<Item> registry, String id, T item) {
        item.setUnlocalizedName("architecturecraft." + id);
        item.setRegistryName(REGISTRY_PREFIX, id);
        item.setCreativeTab(creativeTab);
        registry.register(item);
        registeredItems.put(id, item);

        return (T) registeredItems.get(id);
    }

}
