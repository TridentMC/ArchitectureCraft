package com.elytradev.architecture.compat.architect;

import com.elytradev.architecture.common.drop.IModDrop;
import com.elytradev.architecture.common.drop.RegisterDrop;
import li.cil.architect.api.ConverterAPI;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;

/**
 * Created by Hoofdgebruiker on 7/11/2018.
 */
@RegisterDrop(requiredMod = "architect")
public class ArchitectDrop implements IModDrop {
    public void postInit(FMLPostInitializationEvent e) {
        ConverterAPI.addConverter(new ArchitectConverter());
    }
}
