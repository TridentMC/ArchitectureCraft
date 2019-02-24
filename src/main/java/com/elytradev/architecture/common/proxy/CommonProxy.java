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
import com.elytradev.architecture.common.ArchitectureLog;
import com.elytradev.architecture.common.ArchitectureMod;
import com.google.common.collect.Maps;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.util.Map;

public class CommonProxy {

    private Map<ResourceLocation, IArchitectureModel> modelCache = Maps.newHashMap();

    public void setup(FMLCommonSetupEvent e) {
    }

    public void registerHandlers() {
        MinecraftForge.EVENT_BUS.register(this);
        //NetworkRegistry.INSTANCE.registerGuiHandler(ArchitectureMod.INSTANCE, new ArchitectureGuiHandler()); TODO: Register gui handler somewhere. Who knows where? I sure dont.
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
