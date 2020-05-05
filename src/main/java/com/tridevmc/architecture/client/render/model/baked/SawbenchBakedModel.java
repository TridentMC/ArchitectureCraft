package com.tridevmc.architecture.client.render.model.baked;

import com.tridevmc.architecture.client.render.model.data.ArchitectureModelData;
import com.tridevmc.architecture.client.render.model.impl.SawbenchModel;
import com.tridevmc.architecture.common.modeldata.ModelProperties;
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
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SawbenchBakedModel implements IDynamicBakedModel {

    private static SawbenchModel MODEL;

    public SawbenchBakedModel() {
        if (MODEL == null) {
            MODEL = new SawbenchModel();
        }
    }

    @Nonnull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @Nonnull Random rand, @Nonnull IModelData extraData) {
        World world = extraData.getData(ModelProperties.WORLD);
        BlockPos pos = extraData.getData(ModelProperties.POS);
        ArchitectureModelData.ModelDataQuads quads = MODEL.getQuads(state, world, pos);
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
    public boolean func_230044_c_() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return MODEL.getDefaultSprite();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.EMPTY;
    }
}
