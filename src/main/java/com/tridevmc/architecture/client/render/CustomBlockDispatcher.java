package com.tridevmc.architecture.client.render;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.tridevmc.architecture.client.proxy.ClientProxy;
import com.tridevmc.architecture.client.render.target.RenderTargetWorld;
import com.tridevmc.architecture.common.ArchitectureLog;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.compound.core.reflect.WrappedField;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.model.data.IModelData;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.Random;

public class CustomBlockDispatcher extends BlockRendererDispatcher {

    protected BlockRendererDispatcher base;
    private Random random;

    public CustomBlockDispatcher(BlockRendererDispatcher base) {
        super(base.getBlockModelShapes(), Minecraft.getInstance().getBlockColors());

        this.base = base;
        this.random = (Random) WrappedField.create(BlockRendererDispatcher.class, new String[]{"random", "field_195476_e"}).get(base);
    }

    public static void inject() {
        Minecraft mc = Minecraft.getInstance();
        BlockRendererDispatcher baseDispatcher = mc.getBlockRendererDispatcher();
        CustomBlockDispatcher customBlockDispatcher = new CustomBlockDispatcher(mc.getBlockRendererDispatcher());

        for (Field field : FieldUtils.getAllFields(BlockRendererDispatcher.class)) {
            try {
                field.setAccessible(true);
                FieldUtils.writeField(field, customBlockDispatcher, FieldUtils.readField(field, baseDispatcher));
            } catch (Exception e) {
                ArchitectureLog.error("Failed to transfer field from original render dispatcher to normal. {}", field);
            }
        }

        WrappedField<BlockRendererDispatcher> mcBlockRenderDispatcher = WrappedField.create(Minecraft.class,
                new String[]{"blockRenderDispatcher", "field_175618_aM"});

        mcBlockRenderDispatcher.set(Minecraft.getInstance(), customBlockDispatcher);
    }

    @Override
    public BlockModelShapes getBlockModelShapes() {
        return this.base.getBlockModelShapes();
    }

    @Override
    public BlockModelRenderer getBlockModelRenderer() {
        return this.base.getBlockModelRenderer();
    }

    @Override
    public IBakedModel getModelForState(BlockState state) {
        return this.base.getModelForState(state);
    }

    @Override
    public boolean renderModel(BlockState state, BlockPos pos, ILightReader world, MatrixStack matrixStack, IVertexBuilder vertexBuilder, boolean checkSides, Random rand, IModelData modelData) {
        ICustomRenderer rend = ClientProxy.RENDERING_MANAGER.getCustomRenderer(world, pos, state);
        if (rend != null)
            return this.customRenderBlockToWorld(world, pos, state, vertexBuilder, null, rend);

        return super.renderModel(state, pos, world, matrixStack, vertexBuilder, checkSides, rand, modelData);
    }

    protected boolean customRenderBlockToWorld(ILightReader world, BlockPos pos, BlockState state, IVertexBuilder tess,
                                               TextureAtlasSprite icon, ICustomRenderer rend) {
        RenderTargetWorld target = new RenderTargetWorld(world, pos, tess, icon);
        RenderType layer = MinecraftForgeClient.getRenderLayer();
        rend.renderBlock(world, pos, state, target, layer, Trans3.blockCenter(pos));
        return target.end();
    }

}
