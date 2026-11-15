package com.tridevmc.architecture.client.render.model.resolver.functional;

import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * A functional interface that resolves a quad's metadata to a material, with additional context from the block.
 *
 * @param <D> The type of metadata to resolve.
 */
@FunctionalInterface
public interface IQuadMetadataMaterialResolverWithBlockContext<D> {

    /**
     * Resolves the quad's metadata to a material.
     *
     * @param level    The level the block is in.
     * @param pos      The position of the block.
     * @param state    The state of the block.
     * @param metadata The metadata to resolve.
     * @return The material to use.
     */
    Material.Baked getMaterial(@NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull BlockState state, D metadata);

}
