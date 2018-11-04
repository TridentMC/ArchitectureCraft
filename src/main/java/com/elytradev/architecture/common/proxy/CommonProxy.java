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

package com.elytradev.architecture.common.proxy;

import com.elytradev.architecture.client.render.model.IArchitectureModel;
import com.elytradev.architecture.client.render.model.OBJSONModel;
import com.elytradev.architecture.common.ArchitectureGuiHandler;
import com.elytradev.architecture.common.ArchitectureLog;
import com.elytradev.architecture.common.ArchitectureMod;
import com.elytradev.architecture.common.network.ArchitectureNetworking;
import com.elytradev.architecture.compat.ArchitectConverter;
import com.google.common.collect.Maps;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

import java.util.Map;

public class CommonProxy {

    private Map<ResourceLocation, IArchitectureModel> modelCache = Maps.newHashMap();

    public void preInit(FMLPreInitializationEvent e) {
        ArchitectureNetworking.setupNetwork();
    }

    public void init(FMLInitializationEvent e) {
    }

    public void postInit(FMLPostInitializationEvent e) {
        if (Loader.isModLoaded("architect")) {
            ArchitectConverter.init();
        }
    }

    public void registerHandlers() {
        MinecraftForge.EVENT_BUS.register(this);
        NetworkRegistry.INSTANCE.registerGuiHandler(ArchitectureMod.INSTANCE, new ArchitectureGuiHandler());
    }

    public void registerRenderers(LoaderState.ModState modState) {
    }

    public void registerCustomRenderers() {
    }

    public IArchitectureModel getModel(String name) {
        ResourceLocation loc = modelLocation(name);
        IArchitectureModel model = modelCache.get(loc);
        if (model == null) {
            long t0 = System.nanoTime();
            model = OBJSONModel.fromResource(loc);
            modelCache.put(loc, model);
            long t1 = System.nanoTime();
            ArchitectureLog.info("Loaded and cached {} in {} nanos.", name, t1 - t0);
        }
        return model;
    }

    public ResourceLocation modelLocation(String path) {
        return new ResourceLocation(ArchitectureMod.MOD_ID, "models/" + path);
    }
}
