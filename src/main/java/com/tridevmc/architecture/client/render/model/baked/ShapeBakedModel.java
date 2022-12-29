package com.tridevmc.architecture.client.render.model.baked;

import com.tridevmc.architecture.client.render.model.impl.ShapeModel;
import com.tridevmc.architecture.common.block.state.BlockStateArchitecture;
import com.tridevmc.architecture.common.model.ModelProperties;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.behaviour.ShapeBehaviourModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShapeBakedModel implements IArchitectureBakedModel {

    private final EnumShape shape;
    private final ShapeModel model;

    public ShapeBakedModel(EnumShape shape, boolean generateUVs) {
        this.shape = shape;
        this.model = new ShapeModel(this.shape, (ShapeBehaviourModel) this.shape.behaviour, generateUVs);
    }

    @NotNull
    @Override
    public List<BakedQuad> getQuads(@Nullable BlockStateArchitecture state, @Nullable Direction side, @NotNull RandomSource rand, @NotNull ModelData extraData, @Nullable RenderType renderType) {
        var level = extraData.get(ModelProperties.LEVEL);
        var pos = extraData.get(ModelProperties.POS);

        var t = state.localToGlobalTransformation(level, pos);
        return this.model.getQuads(level, pos, state, t.toMCTrans()).getQuads(side);
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
        return this.model.getDefaultSprite();
    }

    @Override
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
