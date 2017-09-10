//------------------------------------------------------
//
//   ArchitectureCraft - Shape rendering dispatcher
//
//------------------------------------------------------

package com.elytradev.architecture.client.render;

import com.elytradev.architecture.base.BaseModClient.ICustomRenderer;
import com.elytradev.architecture.base.BaseModClient.IRenderTarget;
import com.elytradev.architecture.base.BaseModClient.ITexture;
import com.elytradev.architecture.base.BaseTexture;
import com.elytradev.architecture.common.helpers.Trans3;
import com.elytradev.architecture.common.helpers.Utils;
import com.elytradev.architecture.common.tile.TileShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class ShapeRenderDispatch implements ICustomRenderer {

    // Cannot have any per-render state, because it may be
    // called from more than one thread.

    @Override
    public void renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, IRenderTarget target,
                            BlockRenderLayer layer, Trans3 t) {
        TileShape te = TileShape.get(world, pos);
        if (te != null) {
            Trans3 t2 = t.t(te.localToGlobalRotation());
            boolean renderBase = canRenderInLayer(te.baseBlockState, layer);
            boolean renderSecondary = canRenderInLayer(te.secondaryBlockState, layer);
            renderShapeTE(te, target, t2, renderBase, renderSecondary);
        }
    }

    protected boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return state != null && state.getBlock().canRenderInLayer(state, layer);
    }

    @Override
    public void renderItemStack(ItemStack stack, IRenderTarget target, Trans3 t) {
        TileShape te = new TileShape();
        te.readFromItemStack(stack);
        renderShapeTE(te, target, t,
                te.baseBlockState != null, te.secondaryBlockState != null);
    }

    protected void renderShapeTE(TileShape te, IRenderTarget target, Trans3 t,
                                 boolean renderBase, boolean renderSecondary) {
        if (te.shape != null && (renderBase || renderSecondary)) {
            IBlockState base = te.baseBlockState;
            if (base != null) {
                //System.out.printf("ShapeRenderDispatch.renderShapeTE: in layer %s renderBase = %s renderSecondary = %s\n",
                //    MinecraftForgeClient.getRenderLayer(), renderBase, renderSecondary);
                TextureAtlasSprite icon = Utils.getSpriteForBlockState(base);
                TextureAtlasSprite icon2 = Utils.getSpriteForBlockState(te.secondaryBlockState);
                if (icon != null) {
                    ITexture[] textures = new ITexture[4];
                    if (renderBase) {
                        textures[0] = BaseTexture.fromSprite(icon);
                        textures[1] = textures[0].projected();
                    }
                    if (renderSecondary) {
                        if (icon2 != null) {
                            textures[2] = BaseTexture.fromSprite(icon2);
                            textures[3] = textures[2].projected();
                        } else
                            renderSecondary = false;
                    }
                    if (renderBase && te.shape.kind.secondaryDefaultsToBase()) {
                        if (icon2 == null || (te.secondaryBlockState != null &&
                                te.secondaryBlockState.getBlock().getBlockLayer() != BlockRenderLayer.SOLID)) {
                            textures[2] = textures[0];
                            textures[3] = textures[1];
                            renderSecondary = renderBase;
                        }
                    }
                    te.shape.kind.renderShape(te, textures, target, t, renderBase, renderSecondary);
                }
            }
        }
    }

}
