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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.types.Type;
import com.tridevmc.architecture.common.block.BlockSawbench;
import com.tridevmc.architecture.common.block.BlockShape;
import com.tridevmc.architecture.common.item.ItemArchitecture;
import com.tridevmc.architecture.common.item.ItemChisel;
import com.tridevmc.architecture.common.item.ItemCladding;
import com.tridevmc.architecture.common.item.ItemHammer;
import com.tridevmc.architecture.common.itemgroup.ArchitectureItemGroup;
import com.tridevmc.architecture.common.shape.ItemShape;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.tile.TileSawbench;
import com.tridevmc.architecture.common.tile.TileShape;
import com.tridevmc.architecture.common.ui.ArchitectureUIHooks;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SharedConstants;
import net.minecraft.util.datafix.DataFixesManager;
import net.minecraft.util.datafix.TypeReferences;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.HashMap;
import java.util.List;
import java.util.function.Supplier;

import static com.tridevmc.architecture.common.ArchitectureMod.MOD_ID;

public class ArchitectureContent {

    public final ItemGroup TOOL_TAB = new ArchitectureItemGroup("architecture.tool", () -> (ArchitectureContent.this.itemHammer != null) ? ArchitectureContent.this.itemHammer.getDefaultInstance() : ItemStack.EMPTY);
    public final ItemGroup SHAPE_TAB = new ArchitectureItemGroup("architecture.shape", () -> EnumShape.ROOF_TILE.kind.newStack(EnumShape.ROOF_TILE, Blocks.OAK_PLANKS.getDefaultState(), 1));

    private static final String REGISTRY_PREFIX = MOD_ID.toLowerCase();
    public static HashMap<String, Block> registeredBlocks = Maps.newHashMap();
    public static HashMap<String, Item> registeredItems = Maps.newHashMap();
    private static List<Item> itemBlocksToRegister = Lists.newArrayList();

    public BlockSawbench blockSawbench;
    public BlockShape blockShape;
    public TileEntityType<TileShape> tileTypeShape;
    public TileEntityType<TileSawbench> tileTypeSawbench;
    public Item itemSawblade;
    public Item itemLargePulley;
    public Item itemChisel;
    public Item itemHammer;
    public ItemCladding itemCladding;
    public ContainerType<? extends Container> universalContainerType;

    @SubscribeEvent
    public void onTileRegister(RegistryEvent.Register<TileEntityType<?>> e) {
        IForgeRegistry<TileEntityType<?>> registry = e.getRegistry();
        this.tileTypeShape = this.registerTileEntity(registry, TileShape::new, "shape");
        this.tileTypeSawbench = this.registerTileEntity(registry, TileSawbench::new, "sawbench");
    }

    @SubscribeEvent
    public void onBlockRegister(RegistryEvent.Register<Block> e) {
        IForgeRegistry<Block> registry = e.getRegistry();
        this.blockSawbench = this.registerBlock(registry, "sawbench", new BlockSawbench());
        this.blockShape = this.registerBlock(registry, "shape", new BlockShape(), ItemShape.class);
    }

    @SubscribeEvent
    public void onItemRegister(RegistryEvent.Register<Item> e) {
        IForgeRegistry<Item> registry = e.getRegistry();
        this.itemSawblade = this.registerItem(registry, "sawblade");
        this.itemLargePulley = this.registerItem(registry, "large_pulley");
        this.itemChisel = this.registerItem(registry, "chisel", new ItemChisel());
        this.itemHammer = this.registerItem(registry, "hammer", new ItemHammer());
        this.itemCladding = this.registerItem(registry, "cladding", new ItemCladding());

        itemBlocksToRegister.forEach(registry::register);

        ArchitectureMod.PROXY.registerCustomRenderers();
    }

    @SubscribeEvent
    public void onContainerRegister(final RegistryEvent.Register<ContainerType<?>> e) {
        this.universalContainerType = ArchitectureUIHooks.register(e.getRegistry());
    }

    private <T extends TileEntity> TileEntityType<T> registerTileEntity(IForgeRegistry<TileEntityType<?>> registry, Supplier<T> tileSupplier, String id) {
        ResourceLocation key = new ResourceLocation(REGISTRY_PREFIX, id);
        Type<?> dataFixerType = null;
        try {
            dataFixerType = DataFixesManager.getDataFixer().getSchema(DataFixUtils.makeKey(SharedConstants.getVersion().getWorldVersion())).getChoiceType(TypeReferences.BLOCK_ENTITY, key.toString());
        } catch (IllegalArgumentException e) {
            ArchitectureLog.error("No data fixer was registered for resource id {}", key);
        }
        TileEntityType<T> tileType = TileEntityType.Builder.create(tileSupplier).build(dataFixerType);
        registry.register(tileType.setRegistryName(key));
        return tileType;
    }

    private <T extends Block> T registerBlock(IForgeRegistry<Block> registry, String id, T block) {
        return this.registerBlock(registry, id, block, true);
    }

    private <T extends Block> T registerBlock(IForgeRegistry<Block> registry, String id, T block, boolean withItemBlock) {
        block.setRegistryName(REGISTRY_PREFIX, id);
        registry.register(block);
        if (withItemBlock)
            itemBlocksToRegister.add(new BlockItem(block, new Item.Properties()).setRegistryName(block.getRegistryName()));
        registeredBlocks.put(id, block);
        return (T) registeredBlocks.get(id);
    }

    private <T extends Block> T registerBlock(IForgeRegistry<Block> registry, String id, T block, Class<? extends BlockItem> itemBlockClass) {
        try {
            block.setRegistryName(REGISTRY_PREFIX, id);
            registry.register(block);

            BlockItem itemBlock = itemBlockClass.getDeclaredConstructor(Block.class, Item.Properties.class).newInstance(block, new Item.Properties());
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
