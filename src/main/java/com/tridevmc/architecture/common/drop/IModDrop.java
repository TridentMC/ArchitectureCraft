package com.tridevmc.architecture.common.drop;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

/**
 * Used for adding compatibility or features when other mods are present, registered with {@link RegisterDrop}
 */
public interface IModDrop {

    default void setup(FMLCommonSetupEvent e) {
    }

}
