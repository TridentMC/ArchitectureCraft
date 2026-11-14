package com.tridevmc.architecture.client.render.model.geometry;

import com.tridevmc.architecture.client.render.model.baked.IArchitectureBakedModel;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

final class BakedItemModel implements ItemModel {
    private final IArchitectureBakedModel model;
    private final Matrix4fc transformation;

    BakedItemModel(IArchitectureBakedModel model, Matrix4fc transformation) {
        this.model = model;
        this.transformation = new Matrix4f(transformation);
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver,
                       ItemDisplayContext displayContext, @Nullable ClientLevel level,
                       @Nullable ItemOwner owner, int seed) {
        this.model.appendItemLayer(output, stack, displayContext).setLocalTransform(this.transformation);
    }
}
