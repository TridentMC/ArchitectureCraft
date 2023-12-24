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
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.block.BlockSawbench;
import com.tridevmc.architecture.common.block.BlockShape;
import com.tridevmc.architecture.common.block.entity.BlockEntityShape;
import com.tridevmc.architecture.common.item.*;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.ui.ArchitectureUIHooks;
import com.tridevmc.architecture.core.ArchitectureLog;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static com.tridevmc.architecture.common.ArchitectureMod.MOD_ID;

public class ArchitectureContent {

    private static final String REGISTRY_PREFIX = MOD_ID.toLowerCase();
    private static final List<Pair<ResourceLocation, Item>> itemBlocksToRegister = Lists.newArrayList();
    public static HashMap<String, Block> registeredBlocks = Maps.newHashMap();
    public static HashMap<String, Item> registeredItems = Maps.newHashMap();
    public BlockSawbench blockSawbench;
    public Map<EnumShape, BlockShape> blockShapes;
    public BlockEntityType<BlockEntityShape> blockEntityTypeShape;
    public Item itemSawblade;
    public Item itemLargePulley;
    public Item itemChisel;
    public Item itemHammer;
    public ItemCladding itemCladding;
    public Map<EnumShape, ItemShape> itemShapes;
    public MenuType<? extends Container> universalMenuType;

    @SubscribeEvent
    public void onRegisterEvent(final RegisterEvent e) {
        e.register(BuiltInRegistries.BLOCK.key(), this::onBlockRegister);
        e.register(BuiltInRegistries.ITEM.key(), this::onItemRegister);
        e.register(BuiltInRegistries.BLOCK_ENTITY_TYPE.key(), this::onBlockEntityRegister);
        e.register(BuiltInRegistries.MENU.key(), this::onMenuTypeRegister);
        e.register(Registries.CREATIVE_MODE_TAB, this::onCreativeTabRegisterEvent);
    }

    public void onCreativeTabRegisterEvent(RegisterEvent.RegisterHelper<CreativeModeTab> registry) {
        registry.register(new ResourceLocation(MOD_ID, "tools"), CreativeModeTab.builder().title(Component.translatable("item_group.architecture.tool"))
                .icon(() -> ArchitectureContent.this.itemHammer != null ?
                        ArchitectureContent.this.itemHammer.getDefaultInstance() :
                        ItemStack.EMPTY)
                .displayItems((p, o) -> {
                    o.accept(new ItemStack(this.itemHammer));
                    o.accept(new ItemStack(this.itemChisel));
                    o.accept(new ItemStack(this.itemSawblade));
                    o.accept(new ItemStack(this.itemLargePulley));
                    o.accept(new ItemStack(this.blockSawbench));
                }).build()
        );
        registry.register(new ResourceLocation(MOD_ID, "shapes"), CreativeModeTab.builder().title(Component.translatable("item_group.architecture.shape"))
                .icon(() -> ArchitectureContent.this.itemShapes != null ?
                        ArchitectureContent.this.itemShapes.get(EnumShape.ROOF_TILE).getDefaultInstance() :
                        ItemStack.EMPTY)
                .displayItems((p, o) -> {
                    for (var shape : EnumShape.values()) {
                        o.accept(this.itemShapes.get(shape).getDefaultInstance());
                    }
                }).build()
        );
    }

    public void onBlockEntityRegister(RegisterEvent.RegisterHelper<BlockEntityType<?>> registry) {
        this.blockEntityTypeShape = this.registerBlockEntity(registry, BlockEntityShape::new, "shape");
    }

    public void onBlockRegister(RegisterEvent.RegisterHelper<Block> registry) {
        this.blockSawbench = this.registerBlock(registry, "sawbench", new BlockSawbench());
        this.blockShapes = Maps.newHashMap();
        for (var shape : EnumShape.values()) {
            this.blockShapes.put(shape, this.registerBlock(registry, "shape_" + shape.getName(), new BlockShape(shape), (b) -> new ItemShape(b)));
        }
    }

    public void onItemRegister(RegisterEvent.RegisterHelper<Item> registry) {
        this.itemSawblade = this.registerItem(registry, "sawblade");
        this.itemLargePulley = this.registerItem(registry, "large_pulley");
        this.itemChisel = this.registerItem(registry, "chisel", new ItemChisel());
        this.itemHammer = this.registerItem(registry, "hammer", new ItemHammer());
        this.itemCladding = this.registerItem(registry, "cladding", new ItemCladding());

        itemBlocksToRegister.forEach(e -> registry.register(e.getLeft(), e.getRight()));
        this.itemShapes = Maps.newHashMap();
        Arrays.stream(EnumShape.values()).forEach(s -> this.itemShapes.put(s, ItemShape.getItemFromShape(s)));
        ArchitectureMod.PROXY.registerCustomRenderers();
    }

    public void onMenuTypeRegister(final RegisterEvent.RegisterHelper<MenuType<?>> e) {
        this.universalMenuType = ArchitectureUIHooks.register(e);
    }

    private <T extends BlockEntity> BlockEntityType<T> registerBlockEntity(RegisterEvent.RegisterHelper<BlockEntityType<?>> registry, BlockEntityType.BlockEntitySupplier<T> tileSupplier, String id) {
        ResourceLocation key = new ResourceLocation(REGISTRY_PREFIX, id);
        Type<?> dataFixerType = null;
        try {
            dataFixerType = DataFixers.getDataFixer().getSchema(
                            DataFixUtils.makeKey(SharedConstants
                                    .getCurrentVersion()
                                    .getDataVersion()
                                    .getVersion()))
                    .getChoiceType(References.BLOCK_ENTITY, key.toString());
        } catch (IllegalArgumentException e) {
            ArchitectureLog.error("No data fixer was registered for resource id {}", key);
        }
        BlockEntityType<T> tileType = BlockEntityType.Builder.of(tileSupplier).build(dataFixerType);
        registry.register(new ResourceLocation(REGISTRY_PREFIX, id), tileType);
        return tileType;
    }

    private <T extends BlockArchitecture> T registerBlock(RegisterEvent.RegisterHelper<Block> registry, String id, T block) {
        return this.registerBlock(registry, id, block, true);
    }

    private <T extends BlockArchitecture> T registerBlock(RegisterEvent.RegisterHelper<Block> registry, String id, T block, boolean withItemBlock) {
        registry.register(new ResourceLocation(REGISTRY_PREFIX, id), block);
        if (withItemBlock)
            itemBlocksToRegister.add(ImmutablePair.of(new ResourceLocation(REGISTRY_PREFIX, id), new ItemBlockArchitecture(block, new Item.Properties())));
        registeredBlocks.put(id, block);
        return (T) registeredBlocks.get(id);
    }

    private <T extends BlockArchitecture> T registerBlock(RegisterEvent.RegisterHelper<Block> registry, String id, T block, Function<T, ? extends ItemBlockArchitecture> itemBlockGenerator) {
        registry.register(new ResourceLocation(REGISTRY_PREFIX, id), block);
        var itemBlock = itemBlockGenerator.apply(block);
        itemBlocksToRegister.add(ImmutablePair.of(new ResourceLocation(REGISTRY_PREFIX, id), itemBlock));
        registeredBlocks.put(id, block);

        return (T) registeredBlocks.get(id);
    }

    private <T extends Item> T registerItem(RegisterEvent.RegisterHelper<Item> registry, String id) {
        ItemArchitecture item = new ItemArchitecture(new Item.Properties());
        registry.register(new ResourceLocation(REGISTRY_PREFIX, id), item);
        registeredItems.put(id, item);

        return (T) registeredItems.get(id);
    }

    private <T extends Item> T registerItem(RegisterEvent.RegisterHelper<Item> registry, String id, T item) {
        registry.register(new ResourceLocation(REGISTRY_PREFIX, id), item);
        registeredItems.put(id, item);

        return (T) registeredItems.get(id);
    }

}
