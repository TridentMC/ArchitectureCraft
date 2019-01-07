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

package com.elytradev.architecture.common;

import com.elytradev.architecture.client.proxy.ClientProxy;
import com.elytradev.architecture.common.drop.ModDrops;
import com.elytradev.architecture.common.proxy.CommonProxy;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.javafmlmod.FMLModLoadingContext;

import static com.elytradev.architecture.common.ArchitectureMod.MOD_ID;

@Mod(MOD_ID)
public class ArchitectureMod {
    public static final String MOD_NAME = "ArchitectureCraft";
    public static final String MOD_ID = "architecturecraft";
    public static final String MOD_VER = "@VERSION@";
    public static final String RESOURCE_DOMAIN = "architecturecraft:";

    public static final ArchitectureContent CONTENT = new ArchitectureContent();
    public static boolean INDEV = true; // TODO: not always true. Blackboard is gone.
    public static ArchitectureMod INSTANCE;

    public static CommonProxy PROXY;

    public static ModDrops DROPS = new ModDrops();

    public ArchitectureMod() {
        ArchitectureMod.INSTANCE = this;
        PROXY = DistExecutor.runForDist(() -> ClientProxy::new, () -> CommonProxy::new);

        FMLModLoadingContext loadingContext = FMLModLoadingContext.get();
        loadingContext.getModEventBus().addListener(this::onPreInit);
        loadingContext.getModEventBus().addListener(this::onInit);
        loadingContext.getModEventBus().addListener(this::onPostInit);
    }

    public void onPreInit(FMLPreInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(CONTENT);

        PROXY.preInit(e);
        PROXY.registerHandlers();
        CONTENT.preInit(e);
        PROXY.registerRenderers(e);

        DROPS.preInit(e);
    }

    public void onInit(FMLInitializationEvent e) {
        CONTENT.init(e);
        PROXY.init(e);
        PROXY.registerRenderers(e);

        DROPS.init(e);
    }

    public void onPostInit(FMLPostInitializationEvent e) {
        CONTENT.postInit(e);
        PROXY.postInit(e);
        PROXY.registerRenderers(e);

        DROPS.postInit(e);
    }
}

