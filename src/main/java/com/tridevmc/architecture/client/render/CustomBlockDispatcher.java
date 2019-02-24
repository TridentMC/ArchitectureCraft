package com.tridevmc.architecture.client.render;

import com.tridevmc.architecture.client.proxy.ClientProxy;
import com.tridevmc.architecture.client.render.target.RenderTargetBaked;
import com.tridevmc.architecture.client.render.target.RenderTargetWorld;
import com.tridevmc.architecture.common.ArchitectureLog;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.compound.core.reflect.WrappedField;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.client.MinecraftForgeClient;
import org.apache.commons.lang3.reflect.FieldUtils;

import java.lang.reflect.Field;
import java.util.Random;

public class CustomBlockDispatcher extends BlockRendererDispatcher {

    protected BlockRendererDispatcher base;
    private Random random;

    public CustomBlockDispatcher(BlockRendererDispatcher base) {
        super(base.getBlockModelShapes(), Minecraft.getInstance().getBlockColors());

        this.base = base;
        this.random = (Random) WrappedField.create(BlockRendererDispatcher.class, new String[]{"random", "field_195476_e"}).getValue(base);
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

        mcBlockRenderDispatcher.setValue(Minecraft.getInstance(), customBlockDispatcher);
    }

    @Override
    public BlockModelShapes getBlockModelShapes() {
        return base.getBlockModelShapes();
    }

    @Override
    public BlockModelRenderer getBlockModelRenderer() {
        return base.getBlockModelRenderer();
    }

    @Override
    public IBakedModel getModelForState(IBlockState state) {
        return base.getModelForState(state);
    }

    @Override
    public void renderBlockBrightness(IBlockState state, float brightness) {
        base.renderBlockBrightness(state, brightness);
    }

    @Override
    public void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite icon, IWorldReader world) {
        ICustomRenderer rend = ClientProxy.RENDERING_MANAGER.getCustomRenderer(world, pos, state);
        if (rend != null) {
            RenderTargetBaked target = new RenderTargetBaked(pos, icon);
            Trans3 t = Trans3.blockCenter;
            Block block = state.getBlock();
            for (BlockRenderLayer layer : BlockRenderLayer.values())
                if (block.canRenderInLayer(state, layer))
                    rend.renderBlock(world, pos, state, target, layer, t);
            IBakedModel model = target.getBakedModel();
            BufferBuilder tess = Tessellator.getInstance().getBuffer();
            getBlockModelRenderer().renderModel(world, model, state, pos, tess, false, this.random, state.getPositionRandom(pos)); //TODO should checkSides be false?
        } else
            base.renderBlockDamage(state, pos, icon, world);
    }

    @Override
    public boolean renderBlock(IBlockState state, BlockPos pos, IWorldReader world, BufferBuilder buff, Random rand) {
        ICustomRenderer rend = ClientProxy.RENDERING_MANAGER.getCustomRenderer(world, pos, state);
        if (rend != null)
            return customRenderBlockToWorld(world, pos, state, buff, null, rend);
        else
            return base.renderBlock(state, pos, world, buff, rand);
    }

    protected boolean customRenderBlockToWorld(IWorldReader world, BlockPos pos, IBlockState state, BufferBuilder tess,
                                               TextureAtlasSprite icon, ICustomRenderer rend) {
        RenderTargetWorld target = new RenderTargetWorld(world, pos, tess, icon);
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        rend.renderBlock(world, pos, state, target, layer, Trans3.blockCenter(pos));
        return target.end();
    }

}
