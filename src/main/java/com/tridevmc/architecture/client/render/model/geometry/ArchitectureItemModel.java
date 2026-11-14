package com.tridevmc.architecture.client.render.model.geometry;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.client.render.model.baked.IArchitectureBakedModel;
import net.minecraft.client.renderer.item.ItemModel;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.Optional;

public record ArchitectureItemModel(Optional<EnumShape> shape) implements ItemModel.Unbaked {
    public static final MapCodec<ArchitectureItemModel> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(ArchitectureMeshModels.SHAPE_CODEC.optionalFieldOf("shape")
                    .forGetter(ArchitectureItemModel::shape)).apply(instance, ArchitectureItemModel::new));

    @Override
    public MapCodec<ArchitectureItemModel> type() { return CODEC; }

    @Override
    public void resolveDependencies(Resolver resolver) {
        resolver.markDependency(ArchitectureMeshModels.BASE_MODEL);
    }

    @Override
    public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
        return withTransform(ArchitectureMeshModels.bake(this.shape, context.blockModelBaker()), transformation);
    }

    static ItemModel withTransform(IArchitectureBakedModel model, Matrix4fc transformation) {
        var localTransform = new Matrix4f(transformation);
        return (output, stack, resolver, displayContext, level, owner, seed) ->
                model.appendItemLayer(output, stack, displayContext).setLocalTransform(localTransform);
    }
}
