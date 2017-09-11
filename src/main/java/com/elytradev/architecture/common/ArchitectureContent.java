package com.elytradev.architecture.common;

import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
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

    private static final String REGISTRY_PREFIX = MOD_ID.toLowerCase();
    public static HashMap<String, Block> registeredBlocks;
    public static HashMap<String, Item> registeredItems;

    private static List<Item> itemBlocksToRegister;
    private int recipeID = 0;

    public void preInit(FMLPreInitializationEvent e) {

    }

    public void init(FMLInitializationEvent e) {

    }

    public void postInit(FMLPostInitializationEvent e) {

    }



    private void registerShapedRecipe(IForgeRegistry<IRecipe> registry, ItemStack out, Object... input) {
        ResourceLocation resourceLocation = new ResourceLocation(MOD_ID, out.getUnlocalizedName() + recipeID++);
        registry.register(new ShapedOreRecipe(resourceLocation, out, input).setRegistryName(resourceLocation));
    }

    private void registerShapelessRecipe(IForgeRegistry<IRecipe> registry, ItemStack out, Object... input) {
        ResourceLocation resourceLocation = new ResourceLocation(MOD_ID, out.getUnlocalizedName() + recipeID++);
        registry.register(new ShapelessOreRecipe(resourceLocation, out, input).setRegistryName(resourceLocation));
    }

    private void registerBlock(IForgeRegistry<Block> registry, String id, Block block) {
        registerBlock(registry, id, block, true);
    }

    private void registerBlock(IForgeRegistry<Block> registry, String id, Block block, boolean withItemBlock) {
        block.setUnlocalizedName("teckle." + id);
        block.setRegistryName(REGISTRY_PREFIX, id);
        block.setCreativeTab(creativeTab);
        registry.register(block);
        if (withItemBlock)
            itemBlocksToRegister.add(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        registeredBlocks.put(id, block);
    }

    private void registerBlock(IForgeRegistry<Block> registry, String id, Block block, Class<? extends ItemBlock> itemBlockClass) {
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
    }

    private void registerItem(IForgeRegistry<Item> registry, String id, Item item) {
        item.setUnlocalizedName("architecturecraft." + id);
        item.setRegistryName(REGISTRY_PREFIX, id);
        item.setCreativeTab(creativeTab);
        registry.register(item);
        registeredItems.put(id, item);
    }

}
