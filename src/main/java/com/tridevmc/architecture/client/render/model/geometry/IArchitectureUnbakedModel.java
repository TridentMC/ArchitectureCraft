package com.tridevmc.architecture.client.render.model.geometry;


import net.neoforged.neoforge.client.model.ExtendedUnbakedModel;

/**
 * Extension of {@link ExtendedUnbakedModel}, used for making stacktrace errors more readable.
 */
public interface IArchitectureUnbakedModel extends ExtendedUnbakedModel {
    @Override
    default void resolveDependencies(Resolver resolver) {
        // NO-OP, we don't have any dependencies to resolve.
    }
}
