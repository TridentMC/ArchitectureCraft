package com.tridevmc.architecture.client.render.model.resolver.functional;

import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * A functional interface that resolves a quad's metadata to a material, with the context of an item stack to resolve.
 *
 * @param <D> The type of metadata to resolve.
 */
@FunctionalInterface
public interface IQuadMetadataMaterialResolverWithItemStackContext<D> {

    /**
     * Resolves the quad's metadata to a material.
     *
     * @param stack    The stack to resolve the material for.
     * @param metadata The metadata to resolve.
     * @return The material to use.
     */
    Material.Baked getMaterial(@NotNull ItemStack stack, D metadata);

}
