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
import com.tridevmc.architecture.client.render.model.geometry.ArchitectureGeometryLoader;
import com.tridevmc.architecture.client.render.model.geometry.ArchitectureShapeGeometryLoader;
import com.tridevmc.architecture.client.render.model.impl.BakedModelSawbench;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.proxy.CommonProxy;
import com.tridevmc.architecture.common.ui.ArchitectureUIHooks;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Arrays;

public class ClientProxy extends CommonProxy {

    @Override
    public void setup(FMLCommonSetupEvent e) {
        super.setup(e);
        if (!FMLEnvironment.production)
            NeoForge.EVENT_BUS.register(ArchitectureDebugEventListeners.class);
    }

    public void registerDefaultModelLocations() {
        Item itemToRegister;
        ModelResourceLocation modelResourceLocation;
    }

    private void registerMesh(Item item, ModelResourceLocation resourceLocation) {
    }

    @SubscribeEvent
    public void onModelRegistryEvent(ModelEvent.RegisterGeometryLoaders e) {
        e.register(ResourceLocation.fromNamespaceAndPath(ArchitectureMod.MOD_ID, "sawbench_loader"), new ArchitectureGeometryLoader(
                        () ->
                                (context, modelBaker, function, modelState, itemOverrides) -> new BakedModelSawbench(context.getTransforms())
                )
        );
        e.register(ResourceLocation.fromNamespaceAndPath(ArchitectureMod.MOD_ID, "shape_loader"), new ArchitectureShapeGeometryLoader());
        this.registerDefaultModelLocations();
    }

    @SubscribeEvent
    public void onMenuScreenRegisterEvent(RegisterMenuScreensEvent e) {
        ArchitectureUIHooks.register(e);
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
        return Arrays.stream(textureNames).map(t -> t.contains(":") ? ResourceLocation.parse(t) : ResourceLocation.fromNamespaceAndPath(ArchitectureMod.MOD_ID, t)).toList().toArray(out);
    }
}
