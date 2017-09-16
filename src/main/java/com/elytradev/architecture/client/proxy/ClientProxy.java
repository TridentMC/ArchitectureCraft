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

package com.elytradev.architecture.client.proxy;

import com.elytradev.architecture.client.render.RenderingManager;
import com.elytradev.architecture.client.render.model.VertexModelLoader;
import com.elytradev.architecture.client.render.shape.RenderCladding;
import com.elytradev.architecture.client.render.shape.RenderWindow;
import com.elytradev.architecture.client.render.shape.ShapeRenderDispatch;
import com.elytradev.architecture.common.ArchitectureContent;
import com.elytradev.architecture.common.ArchitectureMod;
import com.elytradev.architecture.common.block.BlockArchitecture;
import com.elytradev.architecture.common.item.ItemArchitecture;
import com.elytradev.architecture.common.proxy.CommonProxy;
import com.elytradev.concrete.resgen.ConcreteResourcePack;
import com.elytradev.concrete.resgen.IResourceHolder;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy {

    public static final ShapeRenderDispatch SHAPE_RENDER_DISPATCHER = new ShapeRenderDispatch();
    public static final RenderingManager RENDERING_MANAGER = new RenderingManager();

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        super.preInit(e);

        RenderWindow.init();
    }

    private int getNumBlockSubtypes(Block block) {
        if (block instanceof BlockArchitecture)
            return ((BlockArchitecture) block).getNumSubtypes();
        else
            return 1;
    }

    private int getNumItemSubtypes(Item item) {
        if (item instanceof ItemArchitecture)
            return ((ItemArchitecture) item).getNumSubtypes();
        else if (item instanceof ItemBlock)
            return getNumBlockSubtypes(Block.getBlockFromItem(item));
        else
            return 1;
    }

    @Override
    public void registerRenderers(LoaderState.ModState state) {
        if (state == LoaderState.ModState.PREINITIALIZED) {
            ModelLoaderRegistry.registerLoader(new VertexModelLoader());
            new ConcreteResourcePack(ArchitectureMod.MOD_ID);
        }

        if (state == LoaderState.ModState.INITIALIZED) {
            registerTileEntitySpecialRenderers();
            registerItemRenderers();
        }
    }

    public void registerTileEntitySpecialRenderers() {
    }

    public void registerItemRenderers() {
        Item itemToRegister;
        ModelResourceLocation modelResourceLocation;

        // Do some general render registrations for OBJECTS, not considering meta.
        ItemModelMesher modelMesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
        for (int i = 0; i < ArchitectureContent.registeredBlocks.size(); i++) {
            modelResourceLocation = new ModelResourceLocation(ArchitectureMod.RESOURCE_DOMAIN + ArchitectureContent.registeredBlocks.keySet().toArray()[i], "inventory");
            Item itemFromBlock = Item.getItemFromBlock((Block) ArchitectureContent.registeredBlocks.values().toArray()[i]);

            modelMesher.register(itemFromBlock, 0, modelResourceLocation);
        }

        for (int i = 0; i < ArchitectureContent.registeredItems.size(); i++) {
            modelResourceLocation = new ModelResourceLocation(ArchitectureMod.RESOURCE_DOMAIN + ArchitectureContent.registeredItems.keySet().toArray()[i], "inventory");
            itemToRegister = (Item) ArchitectureContent.registeredItems.values().toArray()[i];
            if (itemToRegister instanceof ItemArchitecture) {

            } else {
                if (itemToRegister instanceof IResourceHolder)
                    continue;
            }

            modelMesher.register(itemToRegister, 0, modelResourceLocation);
        }
    }

    @SubscribeEvent
    public void onModelBakeEvent(ModelBakeEvent e) {
    }

    @SubscribeEvent
    public void onModelRegistryEvent(ModelRegistryEvent event) {

    }

    @SubscribeEvent
    public void onStitch(TextureStitchEvent.Pre e) {
    }

    @Override
    public void registerHandlers() {
        super.registerHandlers();
    }

    @Override
    public void registerCustomRenderers() {
        RENDERING_MANAGER.addBlockRenderer(ArchitectureMod.CONTENT.blockShape, SHAPE_RENDER_DISPATCHER);
        RENDERING_MANAGER.addItemRenderer(ArchitectureMod.CONTENT.itemCladding, new RenderCladding());
    }
}
