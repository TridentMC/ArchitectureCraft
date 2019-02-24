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

package com.tridevmc.architecture.common;

import com.tridevmc.architecture.common.block.BlockSawbench;
import com.tridevmc.architecture.common.block.BlockShape;
import com.tridevmc.architecture.common.item.ItemArchitecture;
import com.tridevmc.architecture.common.item.ItemChisel;
import com.tridevmc.architecture.common.item.ItemCladding;
import com.tridevmc.architecture.common.item.ItemHammer;
import com.tridevmc.architecture.common.shape.ItemShape;
import com.tridevmc.architecture.common.shape.Shape;
import com.tridevmc.architecture.common.tile.TileSawbench;
import com.tridevmc.architecture.common.tile.TileShape;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashMap;
import java.util.List;

import static com.tridevmc.architecture.common.ArchitectureMod.MOD_ID;

public class ArchitectureContent {

    public static final ItemGroup TOOL_TAB = new ItemGroup("architecture.tool") {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(MOD_ID, "hammer")));
        }
    };
    public static final ItemGroup SHAPE_TAB = new ItemGroup("architecture.shape") {

        @Override
        public ItemStack createIcon() {
            return Shape.ROOF_TILE.kind.newStack(Shape.ROOF_TILE, Blocks.OAK_PLANKS.getDefaultState(), 1);
        }
    };

    private static final String REGISTRY_PREFIX = MOD_ID.toLowerCase();
    public static HashMap<String, Block> registeredBlocks = Maps.newHashMap();
    public static HashMap<String, Item> registeredItems = Maps.newHashMap();
    private static List<Item> itemBlocksToRegister = Lists.newArrayList();

    public BlockSawbench blockSawbench;
    public Block blockShape;
    public TileEntityType<TileShape> tileTypeShape;
    public TileEntityType<TileSawbench> tileTypeSawbench;
    public Item itemSawblade;
    public Item itemLargePulley;
    public Item itemChisel;
    public Item itemHammer;
    public ItemCladding itemCladding;
    private int recipeID = 0;


    public void setup(FMLCommonSetupEvent e) {
        TileEntityType.register(ArchitectureMod.MOD_ID + ":shape",
                TileEntityType.Builder.create(TileShape::new));
        TileEntityType.register(ArchitectureMod.MOD_ID + ":sawbench",
                TileEntityType.Builder.create(TileSawbench::new));
    }

    @SubscribeEvent
    public void onTileRegister(RegistryEvent.Register<TileEntityType<?>> e){
        IForgeRegistry<TileEntityType<?>> registry = e.getRegistry();

    }
    
    @SubscribeEvent
    public void onBlockRegister(RegistryEvent.Register<Block> e) {
        IForgeRegistry<Block> registry = e.getRegistry();
        this.blockSawbench = registerBlock(registry, "sawbench", new BlockSawbench());
        this.blockShape = registerBlock(registry, "shape", new BlockShape(), ItemShape.class);
    }

    @SubscribeEvent
    public void onItemRegister(RegistryEvent.Register<Item> e) {
        IForgeRegistry<Item> registry = e.getRegistry();
        this.itemSawblade = registerItem(registry, "sawblade");
        this.itemLargePulley = registerItem(registry, "largePulley");
        this.itemChisel = registerItem(registry, "chisel", new ItemChisel());
        this.itemHammer = registerItem(registry, "hammer", new ItemHammer());
        this.itemCladding = registerItem(registry, "cladding", new ItemCladding());

        this.itemBlocksToRegister.forEach(registry::register);

        ArchitectureMod.PROXY.registerCustomRenderers();
    }

    @SubscribeEvent
    public void onRecipeRegister(RegistryEvent.Register<IRecipe> e) {
        IForgeRegistry<IRecipe> registry = e.getRegistry();
        registerShapedRecipe(registry, blockSawbench,
                "I*I",
                "/0/",
                "/_/",
                'I', "ingotIron",
                '*', itemSawblade,
                '/', "stickWood",
                '_', Blocks.OAK_PRESSURE_PLATE,
                '0', itemLargePulley);
        registerShapedRecipe(registry, itemSawblade,
                " I ",
                "I/I",
                " I ",
                'I', "ingotIron",
                '/', "stickWood");
        registerShapedRecipe(registry, itemLargePulley,
                " W ",
                "W/W",
                " W ",
                'W', "plankWood",
                '/', "stickWood");
        registerShapedRecipe(registry, itemChisel,
                "I ",
                "ds",
                'I', "ingotIron",
                's', "stickWood",
                'd', "dyeOrange");
        registerShapedRecipe(registry, itemHammer,
                "II ",
                "dsI",
                "ds ",
                'I', "ingotIron",
                's', "stickWood",
                'd', "dyeOrange");
    }

    private void registerShapedRecipe(IForgeRegistry<IRecipe> registry, Block out, Object... input) {
        registerShapedRecipe(registry, new ItemStack(out, 1), input);
    }

    private void registerShapedRecipe(IForgeRegistry<IRecipe> registry, Item out, Object... input) {
        registerShapedRecipe(registry, new ItemStack(out, 1), input);
    }

    private void registerShapedRecipe(IForgeRegistry<IRecipe> registry, ItemStack out, Object... input) {
        ResourceLocation resourceLocation = new ResourceLocation(MOD_ID, out.getTranslationKey() + recipeID++);
        registry.register(new ShapedOreRecipe(resourceLocation, out, input).setRegistryName(resourceLocation));
    }

    private void registerShapelessRecipe(IForgeRegistry<IRecipe> registry, ItemStack out, Object... input) {
        ResourceLocation resourceLocation = new ResourceLocation(MOD_ID, out.getTranslationKey() + recipeID++);
        registry.register(new ShapelessOreRecipe(resourceLocation, out, input).setRegistryName(resourceLocation));
    }

    private <T extends Block> T registerBlock(IForgeRegistry<Block> registry, String id, T block) {
        return registerBlock(registry, id, block, true);
    }

    private <T extends Block> T registerBlock(IForgeRegistry<Block> registry, String id, T block, boolean withItemBlock) {
        block.setRegistryName(REGISTRY_PREFIX, id);
        registry.register(block);
        if (withItemBlock)
            itemBlocksToRegister.add(new ItemBlock(block, new Item.Properties()).setRegistryName(block.getRegistryName()));
        registeredBlocks.put(id, block);
        return (T) registeredBlocks.get(id);
    }

    private <T extends Block> T registerBlock(IForgeRegistry<Block> registry, String id, T block, Class<? extends ItemBlock> itemBlockClass) {
        try {
            block.setRegistryName(REGISTRY_PREFIX, id);
            registry.register(block);

            ItemBlock itemBlock = itemBlockClass.getDeclaredConstructor(Block.class).newInstance(block);
            itemBlock.setRegistryName(REGISTRY_PREFIX, id);
            itemBlocksToRegister.add(itemBlock);
            registeredBlocks.put(id, block);
        } catch (Exception e) {
            ArchitectureLog.error("Caught exception while registering " + block, e);
        }

        return (T) registeredBlocks.get(id);
    }

    private <T extends Item> T registerItem(IForgeRegistry<Item> registry, String id) {
        ItemArchitecture item = new ItemArchitecture(new Item.Properties());
        item.setRegistryName(REGISTRY_PREFIX, id);
        registry.register(item);
        registeredItems.put(id, item);

        return (T) registeredItems.get(id);
    }

    private <T extends Item> T registerItem(IForgeRegistry<Item> registry, String id, T item) {
        item.setRegistryName(REGISTRY_PREFIX, id);
        registry.register(item);
        registeredItems.put(id, item);

        return (T) registeredItems.get(id);
    }

}
