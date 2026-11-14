package com.tridevmc.architecture.client.render.model.geometry;

import com.mojang.serialization.Codec;
import com.tridevmc.architecture.client.render.model.baked.IArchitectureBakedModel;
import com.tridevmc.architecture.client.render.model.impl.BakedModelSawbench;
import com.tridevmc.architecture.client.render.model.impl.BakedModelShapeGeneric;
import com.tridevmc.architecture.common.shape.EnumShape;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.resources.Identifier;

import java.util.Optional;

final class ArchitectureMeshModels {
    static final Identifier BASE_MODEL = Identifier.withDefaultNamespace("block/block");
    static final Codec<EnumShape> SHAPE_CODEC = Codec.STRING.xmap(EnumShape::byName, EnumShape::getName);

    private ArchitectureMeshModels() {}

    static IArchitectureBakedModel bake(Optional<EnumShape> shape, ModelBaker baker) {
        var transforms = baker.getModel(BASE_MODEL).getTopTransforms();
        return shape.<IArchitectureBakedModel>map(s -> new BakedModelShapeGeneric(s, transforms))
                .orElseGet(() -> new BakedModelSawbench(transforms));
    }
}
