package com.elytradev.architecture.client.render;

import com.elytradev.architecture.client.proxy.ClientProxy;
import com.elytradev.architecture.client.render.shape.ShapeRenderDispatch;
import com.elytradev.architecture.client.render.target.RenderTargetWorld;
import com.elytradev.architecture.common.ArchitectureMod;
import com.elytradev.architecture.common.block.BlockArchitecture;
import com.elytradev.architecture.common.block.BlockShape;
import com.elytradev.architecture.common.helpers.Trans3;
import com.elytradev.architecture.common.helpers.Utils;
import com.elytradev.architecture.common.helpers.Vector3;
import com.elytradev.architecture.common.shape.ItemShape;
import com.elytradev.architecture.common.tile.TileShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class PreviewRenderer {

    @SubscribeEvent
    public static void onDrawBlockHighlight(DrawBlockHighlightEvent e) {
        if (!e.isCanceled()) {
            EntityPlayer player = e.getPlayer();
            World world = player.getEntityWorld();
            ItemStack stack = player.getHeldItemMainhand().getItem() instanceof ItemShape ? player.getHeldItemMainhand() : player.getHeldItemOffhand();

            if (stack.getItem() instanceof ItemShape) {
                RayTraceResult hit = e.getTarget();
                if (hit.typeOfHit != RayTraceResult.Type.BLOCK)
                    return;
                float hX = (float) hit.hitVec.x;
                float hY = (float) hit.hitVec.y;
                float hZ = (float) hit.hitVec.z;
                BlockPos pos = hit.getBlockPos().offset(hit.sideHit);
                Tessellator tess = Tessellator.getInstance();
                BufferBuilder buff = tess.getBuffer();
                RenderTargetWorld target = new RenderTargetWorld(world, pos, buff, null);
                ShapeRenderDispatch shapeDispatcher = ClientProxy.SHAPE_RENDER_DISPATCHER;
                BlockShape blockShape = ArchitectureMod.CONTENT.blockShape;
                BlockArchitecture.IOrientationHandler oh = blockShape.getOrientationHandler();
                IBlockState state = oh.onBlockPlaced(blockShape, world, pos, hit.sideHit, hX, hY, hZ, blockShape.getDefaultState(), player);
                TileShape te = new TileShape();
                te.setPos(pos);
                te.setWorld(world);
                te.readFromItemStack(stack);
                adjustOrientation(player, world, te, TileShape.get(world, hit.getBlockPos()), hit);
                Trans3 t = Trans3.blockCenter(pos).t(te.localToGlobalTransformation(Vector3.zero, state));

                double tX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) e.getPartialTicks();
                double tY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) e.getPartialTicks();
                double tZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) e.getPartialTicks();

                GlStateManager.pushMatrix();
                GlStateManager.translate(-tX, -tY, -tZ);
                GlStateManager.depthMask(false);
                GlStateManager.enableCull();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                buff.begin(7, DefaultVertexFormats.ITEM);
                shapeDispatcher.renderShapeTE(te, target, t, true, false, Utils.getColourFromState(te.baseBlockState), -1);
                tess.draw();
                GlStateManager.depthMask(true);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }

    private static void adjustOrientation(EntityPlayer player, IBlockAccess world, TileShape tile, TileShape neighbourTile, RayTraceResult hit) {
        if (tile != null) {
            if (tile.shape != null) {
                Vec3i d = Vector3.getDirectionVec(hit.sideHit);
                Vec3d hitVec = hit.hitVec.subtract(hit.getBlockPos().getX(), hit.getBlockPos().getY(), hit.getBlockPos().getZ());
                Vector3 hitVecAdjusted = new Vector3(hitVec.x - d.getX() - 0.5, hitVec.y - d.getY() - 0.5, hitVec.z - d.getZ() - 0.5);
                BlockPos neighbourPos = hit.getBlockPos();
                IBlockState neighbourState = world.getBlockState(neighbourPos);
                tile.shape.orientOnPlacement(player, tile, neighbourPos, neighbourState, neighbourTile, hit.sideHit, hitVecAdjusted);
            }
        }
    }

}
