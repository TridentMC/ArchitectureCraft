package com.tridevmc.architecture.common.drop;

import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

/**
 * Used for adding compatibility or features when other mods are present, registered with {@link RegisterDrop}
 */
public interface IModDrop {

    /**
     * Called during the preInit phase.
     *
     * @param e the pre initialization event.
     */
    default void preInit(FMLPreInitializationEvent e) {
    }

    /**
     * Called during the init phase.
     *
     * @param e the init event.
     */
    default void init(FMLInitializationEvent e) {
    }

    /**
     * Called during the post init phase.
     *
     * @param e the post init event.
     */
    default void postInit(FMLPostInitializationEvent e) {
    }

}
