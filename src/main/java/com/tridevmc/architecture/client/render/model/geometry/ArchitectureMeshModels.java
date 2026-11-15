package com.tridevmc.architecture.client.render.model.geometry;

import com.tridevmc.architecture.client.render.model.baked.IArchitectureBakedModel;
import com.tridevmc.architecture.client.render.model.impl.BakedModelSawbench;
import com.tridevmc.architecture.client.render.model.impl.BakedModelShapeGeneric;
import com.tridevmc.architecture.common.shape.EnumShape;

import com.mojang.serialization.Codec;

import net.minecraft.resources.Identifier;

import java.util.Optional;

final class ArchitectureMeshModels {
    static final Identifier BASE_MODEL = Identifier.withDefaultNamespace("block/block");
    static final Codec<EnumShape> SHAPE_CODEC = Codec.STRING.xmap(EnumShape::byName, EnumShape::getName);

    private ArchitectureMeshModels() {
    }

    static IArchitectureBakedModel bake(Optional<EnumShape> shape) {
        return shape.<IArchitectureBakedModel>map(BakedModelShapeGeneric::new)
                .orElseGet(BakedModelSawbench::new);
    }
}
