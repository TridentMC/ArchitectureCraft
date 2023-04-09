package com.tridevmc.architecture.client.render.model.baked;

import com.tridevmc.architecture.client.render.model.resolver.IModelResolver;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.common.block.state.BlockStateArchitecture;
import com.tridevmc.architecture.common.model.ModelProperties;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

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
     * @param level The level.
     * @param pos   The position.
     * @param state The state.
     * @return The quad metadata resolver for the given data.
     */
    IQuadMetadataResolver<D> getMetadataResolver(LevelAccessor level, BlockPos pos, BlockStateArchitecture state);

    @Override
    @NotNull
    default List<BakedQuad> getQuads(@Nullable BlockStateArchitecture state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        var level = extraData.get(ModelProperties.LEVEL);
        var pos = extraData.get(ModelProperties.POS);

        // TODO: Don't use old LegacyTrans3 - migrate BlockStateArchitecture to use new Transformation system.
        var t = Objects.requireNonNull(state).localToGlobalTransformation(level, pos).toMCTrans();
        var modelResolver = this.getModelResolver();
        var metadataResolver = this.getMetadataResolver(level, pos, state);
        return modelResolver.getQuads(metadataResolver, t).quadsFor(side);
    }

}
