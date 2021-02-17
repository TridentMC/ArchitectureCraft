package com.tridevmc.architecture.client.render.model.baked;

import com.tridevmc.architecture.client.render.model.data.ArchitectureModelData;
import com.tridevmc.architecture.client.render.model.impl.ShapeModel;
import com.tridevmc.architecture.common.modeldata.ModelProperties;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.behaviour.ShapeBehaviourModel;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.IDynamicBakedModel;
import net.minecraftforge.client.model.data.IModelData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class ShapeBakedModel implements IDynamicBakedModel {

    private final EnumShape shape;
    private final ShapeModel model;

    public ShapeBakedModel(EnumShape shape, boolean generateUVs) {
        this.shape = shape;
        this.model = new ShapeModel(this.shape, (ShapeBehaviourModel) this.shape.behaviour, generateUVs);
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        World world = extraData.getData(ModelProperties.WORLD);
        BlockPos pos = extraData.getData(ModelProperties.POS);
        ArchitectureModelData.ModelDataQuads quads = this.model.getQuads(state, world, pos);

        return quads.getFaceQuads().get(side);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isSideLit() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return this.model.getDefaultSprite();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }
}
