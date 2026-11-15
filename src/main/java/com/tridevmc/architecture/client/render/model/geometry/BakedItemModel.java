package com.tridevmc.architecture.client.render.model.geometry;

import com.tridevmc.architecture.client.render.model.baked.IArchitectureBakedModel;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

final class BakedItemModel implements ItemModel {
    private final IArchitectureBakedModel model;
    private final ItemTransforms transforms;
    private final Matrix4fc transformation;

    BakedItemModel(IArchitectureBakedModel model, ItemTransforms transforms, Matrix4fc transformation) {
        this.model = model;
        this.transforms = transforms;
        this.transformation = new Matrix4f(transformation);
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack stack, ItemModelResolver resolver,
                       ItemDisplayContext displayContext, @Nullable ClientLevel level,
                       @Nullable ItemOwner owner, int seed) {
        var quads = this.model.getQuads(stack);
        output.appendModelIdentityElement(this.model);
        output.appendModelIdentityElement(quads);
        var layer = output.newLayer();
        layer.setQuads(ItemQuads.split(quads));
        layer.setUsesBlockLight(true);
        layer.setParticleMaterial(this.model.particleMaterial());
        layer.setItemTransform(this.transforms.getTransform(displayContext));
        layer.setExtents(() -> CuboidItemModelWrapper.computeExtents(quads));
        if (quads.stream().anyMatch(q -> (q.materialInfo().flags() & BakedQuad.FLAG_ANIMATED) != 0)) {
            output.setAnimated();
        }
        if (stack.hasFoil()) {
            layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
            output.appendModelIdentityElement(ItemStackRenderState.FoilType.STANDARD);
            output.setAnimated();
        }
        layer.setLocalTransform(this.transformation);
    }
}
