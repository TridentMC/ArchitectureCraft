package com.tridevmc.architecture.client.render.model.baked;

import com.mojang.math.Transformation;
import com.tridevmc.architecture.client.render.model.data.ArchitectureModelDataQuads;
import com.tridevmc.architecture.client.render.model.impl.SawbenchModel;
import com.tridevmc.architecture.common.block.state.BlockStateArchitecture;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Vector3;
import com.tridevmc.architecture.common.modeldata.ModelProperties;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SawbenchBakedModel implements IArchitectureBakedModel {

    private static SawbenchModel MODEL;

    public SawbenchBakedModel() {
        if (MODEL == null) {
            MODEL = new SawbenchModel();
        }
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockStateArchitecture state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        Level level = extraData.get(ModelProperties.LEVEL);
        BlockPos pos = extraData.get(ModelProperties.POS);

        var t = state.localToGlobalTransformation(level, pos);
        var trans = Transformation.identity();
        ArchitectureModelDataQuads quads = MODEL.getQuads(level, pos, state, t.toTransformation());
        return quads.getQuads(side);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return MODEL.getDefaultSprite();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
