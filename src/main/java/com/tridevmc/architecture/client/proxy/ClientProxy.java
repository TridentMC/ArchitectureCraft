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

package com.tridevmc.architecture.client.proxy;

import com.tridevmc.architecture.client.render.RenderingManager;
import com.tridevmc.architecture.client.render.model.baked.SawbenchBakedModel;
import com.tridevmc.architecture.client.render.model.loader.ArchitectureModelLoader;
import com.tridevmc.architecture.client.render.shape.ShapeRenderDispatcher;
import com.tridevmc.architecture.common.ArchitectureContent;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.proxy.CommonProxy;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ClientProxy extends CommonProxy {

    public static final ShapeRenderDispatcher SHAPE_RENDER_DISPATCHER = new ShapeRenderDispatcher();
    public static final RenderingManager RENDERING_MANAGER = new RenderingManager();

    @Override
    public void setup(FMLCommonSetupEvent e) {
        super.setup(e);
    }

    public void registerDefaultModelLocations() {
        Item itemToRegister;
        ModelResourceLocation modelResourceLocation;

        // Do some general render registrations for Content.
        //for (int i = 0; i < ArchitectureContent.registeredBlocks.size(); i++) {
        //    modelResourceLocation = new ModelResourceLocation(ArchitectureMod.RESOURCE_DOMAIN
        //            + ArchitectureContent.registeredBlocks.keySet().toArray()[i], "inventory");
        //    Block block = (Block) ArchitectureContent.registeredBlocks.values().toArray()[i];
        //    Item itemFromBlock = Item.getItemFromBlock(block);

        //    if (RENDERING_MANAGER.blockNeedsCustomRendering(block)) {
        //        //ModelLoader.setCustomStateMapper(block, RENDERING_MANAGER.getBlockStateMapper());
        //        for (BlockState state : block.getStateContainer().getValidStates()) {
        //            //ModelResourceLocation location = RENDERING_MANAGER.getBlockStateMapper().getModelResourceLocation(state);
        //            //IBakedModel model = RENDERING_MANAGER.getCustomBakedModel(state, location);
        //            //RENDERING_MANAGER.getBakedModels().add(model);
        //        }

        //        if (itemFromBlock != Items.AIR) {
        //            RENDERING_MANAGER.registerModelLocationForItem(itemFromBlock, RENDERING_MANAGER.getItemBakedModel());
        //        }
        //    } else {
        //        this.registerMesh(itemFromBlock, modelResourceLocation);
        //    }
        //}

        //for (int i = 0; i < ArchitectureContent.registeredItems.size(); i++) {
        //    modelResourceLocation = new ModelResourceLocation(ArchitectureMod.RESOURCE_DOMAIN + ArchitectureContent.registeredItems.keySet().toArray()[i], "inventory");
        //    itemToRegister = (Item) ArchitectureContent.registeredItems.values().toArray()[i];
        //    if (RENDERING_MANAGER.itemNeedsCustomRendering(itemToRegister)) {
        //        RENDERING_MANAGER.registerModelLocationForItem(itemToRegister, RENDERING_MANAGER.getItemBakedModel());
        //    } else {
        //        registerMesh(itemToRegister, 0, modelResourceLocation);
        //    }
        //}
    }

    private void registerMesh(Item item, ModelResourceLocation resourceLocation) {
        //Minecraft mc = Minecraft.getInstance();
        //if (mc.getItemRenderer() != null && mc.getItemRenderer().getItemModelMesher() != null) {
        //    mc.getItemRenderer().getItemModelMesher().register(item, resourceLocation);
        //} else {
        //    ModelLoader.setCustomModelResourceLocation(item, meta, resourceLocation);
        //}
    }

    @SubscribeEvent
    public void onModelBakeEvent(ModelBakeEvent e) {
        //RENDERING_MANAGER.getItemBakedModel().install(e);
    }

    @SubscribeEvent
    public void onModelRegistryEvent(ModelRegistryEvent e) {
        ModelLoaderRegistry.registerLoader(new ResourceLocation(ArchitectureMod.MOD_ID, "sawbench_loader"), new ArchitectureModelLoader(new SawbenchBakedModel(), this.getTextures("blocks/sawbench-metal", "blocks/sawbench-wood")));
        this.registerDefaultModelLocations();
    }

    @SubscribeEvent
    public void onStitch(TextureStitchEvent.Pre e) {
        //RENDERING_MANAGER.clearTextureCache();
        for (Block block : ArchitectureContent.registeredBlocks.values())
            RENDERING_MANAGER.registerSprites(0, e.getMap(), block);

        for (Item item : ArchitectureContent.registeredItems.values())
            RENDERING_MANAGER.registerSprites(1, e.getMap(), item);
    }

    @Override
    public void registerHandlers() {
        super.registerHandlers();
    }

    @Override
    public void registerCustomRenderers() {
        //RENDERING_MANAGER.addBlockRenderer(ArchitectureMod.CONTENT.blockShape, SHAPE_RENDER_DISPATCHER);
        //RENDERING_MANAGER.addItemRenderer(ArchitectureMod.CONTENT.itemCladding, new RenderCladding());

        RenderTypeLookup.setRenderLayer(ArchitectureMod.CONTENT.blockShape, (l) -> true);
    }

    private ResourceLocation[] getTextures(String... textureNames) {
        ResourceLocation[] out = new ResourceLocation[textureNames.length];
        return Arrays.stream(textureNames).map(t -> t.contains(":") ? new ResourceLocation(t) : new ResourceLocation(ArchitectureMod.MOD_ID, t)).collect(Collectors.toList()).toArray(out);
    }
}
