package com.tridevmc.architecture.client.render.model.geometry;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tridevmc.architecture.common.shape.EnumShape;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.neoforged.neoforge.client.model.block.CustomUnbakedBlockStateModel;

import java.util.Optional;

public record ArchitectureBlockModel(Optional<EnumShape> shape) implements CustomUnbakedBlockStateModel {
    public static final MapCodec<ArchitectureBlockModel> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(ArchitectureMeshModels.SHAPE_CODEC.optionalFieldOf("shape")
                    .forGetter(ArchitectureBlockModel::shape)).apply(instance, ArchitectureBlockModel::new));

    @Override
    public MapCodec<ArchitectureBlockModel> codec() { return CODEC; }

    @Override
    public void resolveDependencies(Resolver resolver) {
        resolver.markDependency(ArchitectureMeshModels.BASE_MODEL);
    }

    @Override
    public BlockStateModel bake(ModelBaker baker) { return ArchitectureMeshModels.bake(this.shape, baker); }
}
