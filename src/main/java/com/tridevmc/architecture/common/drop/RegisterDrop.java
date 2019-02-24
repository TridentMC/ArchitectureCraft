package com.tridevmc.architecture.common.drop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to register mod drops.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface RegisterDrop {

    /**
     * The mod id that must be loaded for this drop to be registered.
     *
     * @return the mod id to check for.
     */
    String requiredMod();

}
