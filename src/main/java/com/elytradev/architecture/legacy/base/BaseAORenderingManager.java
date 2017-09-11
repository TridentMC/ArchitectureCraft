//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.10 - Rendering manager with support for custom ambient occlusion
//
//------------------------------------------------------------------------------------------------

package com.elytradev.architecture.legacy.base;

import com.elytradev.architecture.client.render.target.RenderTargetWorld;
import com.elytradev.architecture.legacy.common.helpers.Trans3;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;

public class BaseAORenderingManager extends BaseRenderingManager {

    public BaseAORenderingManager(BaseModClient client) {
        super(client);
    }

    @Override
    protected void enableCustomRendering() {
        super.enableCustomRendering();
        Minecraft mc = Minecraft.getMinecraft();
        blockRendererDispatcher = new CustomBlockRendererDispatcher(blockRendererDispatcher);
        BaseReflectionUtils.setField(mc, "blockRenderDispatcher", "field_175618_aM", blockRendererDispatcher);
    }

    //------------------------------------------------------------------------------------------------

    protected class CustomBlockRendererDispatcher extends BlockRendererDispatcher {

        protected BlockRendererDispatcher base;

        public CustomBlockRendererDispatcher(BlockRendererDispatcher base) {
            super(null, null);
            this.base = base;
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
        public void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite icon, IBlockAccess world) {
            BaseModClient.ICustomRenderer rend = getCustomRenderer(world, pos, state);
            if (rend != null) {
                BaseBakedRenderTarget target = new BaseBakedRenderTarget(pos, icon);
                Trans3 t = Trans3.blockCenter;
                Block block = state.getBlock();
                for (BlockRenderLayer layer : BlockRenderLayer.values())
                    if (block.canRenderInLayer(state, layer))
                        rend.renderBlock(world, pos, state, target, layer, t);
                IBakedModel model = target.getBakedModel();
                BufferBuilder tess = Tessellator.getInstance().getBuffer();
                getBlockModelRenderer().renderModel(world, model, state, pos, tess, false); //TODO chould checkSides be false?
            } else
                base.renderBlockDamage(state, pos, icon, world);
        }

        @Override
        public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess world, BufferBuilder tess) {
            BaseModClient.ICustomRenderer rend = getCustomRenderer(world, pos, state);
            if (rend != null)
                return customRenderBlockToWorld(world, pos, state, tess, null, rend);
            else
                return base.renderBlock(state, pos, world, tess);
        }

        protected boolean customRenderBlockToWorld(IBlockAccess world, BlockPos pos, IBlockState state, BufferBuilder tess,
                                                   TextureAtlasSprite icon, BaseModClient.ICustomRenderer rend) {
            RenderTargetWorld target = new RenderTargetWorld(world, pos, tess, icon);
            BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
            rend.renderBlock(world, pos, state, target, layer, Trans3.blockCenter(pos));
            return target.end();
        }

    }

}
