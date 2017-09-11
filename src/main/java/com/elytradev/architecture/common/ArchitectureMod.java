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

//@Mod(modid = MOD_ID, name = MOD_NAME, version = MOD_VER)
public class ArchitectureMod {
    public static final String MOD_NAME = "ArchitectureCraft";
    public static final String MOD_ID = "architecturecraft";
    public static final String MOD_VER = "@VERSION@";
    public static final String RESOURCE_DOMAIN = "architecturecraft:";

    public static final ArchitectureContent CONTENT = new ArchitectureContent();
    public static boolean INDEV = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
    //@Mod.Instance()
    public static ArchitectureMod INSTANCE;

    public static Logger LOG;

    //@SidedProxy(serverSide = "com.elytradev.architecture.common.proxy.CommonProxy", clientSide = "com.elytradev.architecture.client.proxy.ClientProxy")
    public static CommonProxy PROXY;

    //@Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent e) {
        PROXY.registerHandlers();
        LOG = e.getModLog();
        MinecraftForge.EVENT_BUS.register(CONTENT);
        CONTENT.preInit(e);

        PROXY.registerRenderers(e.getModState());
    }

    //@Mod.EventHandler
    public void onInit(FMLInitializationEvent e) {
        CONTENT.init(e);
        PROXY.registerRenderers(e.getModState());
    }

    //@Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent e) {
        CONTENT.postInit(e);
        PROXY.registerRenderers(e.getModState());
    }
}

