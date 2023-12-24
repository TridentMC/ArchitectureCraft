package com.tridevmc.architecture.client.render.model.baked;

import com.tridevmc.architecture.client.render.model.resolver.IModelResolver;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.common.block.state.BlockStateArchitecture;
import com.tridevmc.architecture.common.model.ModelProperties;
import com.tridevmc.architecture.core.math.ITrans3;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Basic baked model that delegates to a {@link IModelResolver} and {@link IQuadMetadataResolver} to resolve quads from data in the world.
 *
 * @param <D> The type of quad metadata to resolve.
 */
public interface IModelResolverBaked<D> extends IArchitectureBakedModel {

    /**
     * Gets the model resolver to use.
     *
     * @return The model resolver.
     */
    IModelResolver<D> getModelResolver();

    /**
     * Gets the quad metadata resolver to use.
     *
     * @return The quad metadata resolver for the model.
     */
    default IQuadMetadataResolver<D> getMetadataResolver() {
        return this.getModelResolver().getMetadataResolver();
    }

    @Override
    @NotNull
    default List<BakedQuad> getQuads(@Nullable BlockStateArchitecture state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        var level = extraData.get(ModelProperties.LEVEL);
        var pos = extraData.get(ModelProperties.POS);

        var trans = state == null ? ITrans3.ofIdentity() : state.getTransform();
        var modelResolver = this.getModelResolver();
        return modelResolver.getQuads(level, pos, state, trans).quadsFor(side);
    }

    @Override
    @NotNull
    default List<BakedQuad> getQuads(@NotNull ItemStack stack) {
        return getModelResolver().getQuads(stack).allQuads();
    }
}
