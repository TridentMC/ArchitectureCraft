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

import com.tridevmc.architecture.client.debug.ArchitectureDebugEventListeners;
import com.tridevmc.architecture.client.render.model.impl.BakedModelSawbench;
import com.tridevmc.architecture.client.render.model.loader.ArchitectureGeometryLoader;
import com.tridevmc.architecture.client.render.model.loader.ArchitectureShapeModelLoader;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.proxy.CommonProxy;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.Arrays;

public class ClientProxy extends CommonProxy {

    @Override
    public void setup(FMLCommonSetupEvent e) {
        super.setup(e);
        if (!FMLEnvironment.production)
            MinecraftForge.EVENT_BUS.register(ArchitectureDebugEventListeners.class);
    }

    public void registerDefaultModelLocations() {
        Item itemToRegister;
        ModelResourceLocation modelResourceLocation;
    }

    private void registerMesh(Item item, ModelResourceLocation resourceLocation) {
    }

    @SubscribeEvent
    public void onModelRegistryEvent(ModelEvent.RegisterGeometryLoaders e) {
        e.register("sawbench_loader", new ArchitectureGeometryLoader(new BakedModelSawbench(), this.getTextures("blocks/sawbench-metal", "blocks/sawbench-wood")));
        e.register("shape_loader", new ArchitectureShapeModelLoader());
        this.registerDefaultModelLocations();
    }

    @SubscribeEvent
    public void onStitch(TextureStitchEvent e) {
    }

    @Override
    public void registerHandlers() {
        super.registerHandlers();
    }

    @Override
    public void registerCustomRenderers() {
    }

    private ResourceLocation[] getTextures(String... textureNames) {
        ResourceLocation[] out = new ResourceLocation[textureNames.length];
        return Arrays.stream(textureNames).map(t -> t.contains(":") ? new ResourceLocation(t) : new ResourceLocation(ArchitectureMod.MOD_ID, t)).toList().toArray(out);
    }
}
