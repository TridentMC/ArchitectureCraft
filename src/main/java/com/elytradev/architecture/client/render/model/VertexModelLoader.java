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

package com.elytradev.architecture.client.render.model;

import com.elytradev.architecture.client.proxy.ClientProxy;
import com.elytradev.architecture.common.ArchitectureMod;
import com.google.common.collect.Lists;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

import java.util.List;

public class VertexModelLoader implements ICustomModelLoader {

    public static VertexModel model;
    public static List<String> acceptedLocations = Lists.newArrayList("sawbench");

    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        if (modelLocation.getResourceDomain().equals(ArchitectureMod.MOD_ID)) {
            boolean accept = ClientProxy.RENDERING_MANAGER.pathUsesVertexModel(modelLocation.getResourcePath());
            accept = accept || acceptedLocations.stream().anyMatch(s -> modelLocation.getResourcePath().contains(s));
            if (ArchitectureMod.INDEV) {
                ArchitectureMod.LOG.info("Asked for {}, responded with {}", modelLocation,
                        accept ? "yes." : "no.");
            }

            return accept;
        }

        return false;
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws Exception {
        if (model == null)
            model = new VertexModel();

        return model;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        model = null;
    }
}
