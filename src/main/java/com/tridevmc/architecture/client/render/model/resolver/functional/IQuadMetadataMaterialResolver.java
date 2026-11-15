package com.tridevmc.architecture.client.render.model.resolver.functional;

import net.minecraft.client.resources.model.sprite.Material;

/**
 * A functional interface that resolves a quad's metadata to a material.
 *
 * @param <D> The type of metadata to resolve.
 */
@FunctionalInterface
public interface IQuadMetadataMaterialResolver<D> {

    /**
     * Resolves the quad's metadata to a material.
     *
     * @param metadata The metadata to resolve.
     * @return The material to use.
     */
    Material.Baked getMaterial(D metadata);

}
