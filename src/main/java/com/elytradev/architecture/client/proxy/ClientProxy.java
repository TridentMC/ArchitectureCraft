package com.elytradev.architecture.client.proxy;

import com.elytradev.architecture.client.render.model.VertexModelLoader;
import com.elytradev.architecture.common.ArchitectureContent;
import com.elytradev.architecture.common.block.BlockArchitecture;
import com.elytradev.architecture.common.item.ItemArchitecture;
import com.elytradev.architecture.common.proxy.CommonProxy;
import com.elytradev.architecture.legacy.common.ArchitectureCraft;
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClientProxy extends CommonProxy {

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
            new ConcreteResourcePack(ArchitectureCraft.MOD_ID);
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
            modelResourceLocation = new ModelResourceLocation(ArchitectureCraft.RESOURCE_DOMAIN + ArchitectureContent.registeredBlocks.keySet().toArray()[i], "inventory");
            Item itemFromBlock = Item.getItemFromBlock((Block) ArchitectureContent.registeredBlocks.values().toArray()[i]);

            modelMesher.register(itemFromBlock, 0, modelResourceLocation);
        }

        for (int i = 0; i < ArchitectureContent.registeredItems.size(); i++) {
            modelResourceLocation = new ModelResourceLocation(ArchitectureCraft.RESOURCE_DOMAIN + ArchitectureContent.registeredItems.keySet().toArray()[i], "inventory");
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
}
