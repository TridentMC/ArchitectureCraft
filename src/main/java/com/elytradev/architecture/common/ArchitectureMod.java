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

import com.elytradev.architecture.common.proxy.CommonProxy;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Logger;

import static com.elytradev.architecture.common.ArchitectureMod.*;

@Mod(modid = MOD_ID, name = MOD_NAME, version = MOD_VER)
public class ArchitectureMod {
    public static final String MOD_NAME = "ArchitectureCraft";
    public static final String MOD_ID = "architecturecraft";
    public static final String MOD_VER = "@VERSION@";
    public static final String RESOURCE_DOMAIN = "architecturecraft:";

    public static final ArchitectureContent CONTENT = new ArchitectureContent();
    public static boolean INDEV = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
    @Mod.Instance()
    public static ArchitectureMod INSTANCE;

    public static Logger LOG;

    @SidedProxy(serverSide = "com.elytradev.architecture.common.proxy.CommonProxy", clientSide = "com.elytradev.architecture.client.proxy.ClientProxy")
    public static CommonProxy PROXY;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(CONTENT);

        LOG = e.getModLog();
        PROXY.preInit(e);
        PROXY.registerHandlers();
        CONTENT.preInit(e);
        PROXY.registerRenderers(e.getModState());
    }

    @Mod.EventHandler
    public void onInit(FMLInitializationEvent e) {
        CONTENT.init(e);
        PROXY.init(e);
        PROXY.registerRenderers(e.getModState());
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent e) {
        CONTENT.postInit(e);
        PROXY.postInit(e);
        PROXY.registerRenderers(e.getModState());
    }
}

