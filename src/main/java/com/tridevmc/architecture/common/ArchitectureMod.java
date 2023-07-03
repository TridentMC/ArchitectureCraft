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

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.client.proxy.ClientProxy;
import com.tridevmc.architecture.common.proxy.CommonProxy;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.placement.ShapePlacementLogicWindow;
import com.tridevmc.compound.network.core.CompoundNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import static com.tridevmc.architecture.common.ArchitectureMod.MOD_ID;

@Mod(MOD_ID)
public class ArchitectureMod {
    public static final String MOD_ID = "architecturecraft";

    public static final ArchitectureContent CONTENT = new ArchitectureContent();
    public static ArchitectureMod INSTANCE;

    public static CommonProxy PROXY;

    public ArchitectureMod() {
        ArchitectureMod.INSTANCE = this;
        PROXY = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);

        FMLJavaModLoadingContext loadingContext = FMLJavaModLoadingContext.get();
        loadingContext.getModEventBus().addListener(this::onSetup);
        loadingContext.getModEventBus().register(CONTENT);
        loadingContext.getModEventBus().register(PROXY);
        MinecraftForge.EVENT_BUS.register(CONTENT);
        MinecraftForge.EVENT_BUS.register(PROXY);
    }

    public void onSetup(FMLCommonSetupEvent e) {
        PROXY.setup(e);

        CompoundNetwork.createNetwork(ModLoadingContext.get().getActiveContainer(), "network");
    }

}

