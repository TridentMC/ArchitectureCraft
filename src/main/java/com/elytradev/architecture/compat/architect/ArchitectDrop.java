package com.elytradev.architecture.compat.architect;

import com.elytradev.architecture.common.drop.IModDrop;
import com.elytradev.architecture.common.drop.RegisterDrop;
import li.cil.architect.api.ConverterAPI;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

/**
 * Mod drop to add compat for Architect.
 */
@RegisterDrop(requiredMod = "architect")
public class ArchitectDrop implements IModDrop {
    public void postInit(FMLPostInitializationEvent e) {
        ConverterAPI.addConverter(new ArchitectConverter());
    }
}
