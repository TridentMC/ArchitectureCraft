package com.tridevmc.architecture.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.tridevmc.architecture.client.proxy.ClientProxy;
import com.tridevmc.architecture.client.render.shape.ShapeRenderDispatcher;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.block.BlockShape;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Vector3;
import com.tridevmc.architecture.common.shape.ItemShape;
import com.tridevmc.architecture.common.tile.TileShape;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class PreviewRenderer {

    @SubscribeEvent
    public static void onDrawBlockHighlight(DrawHighlightEvent e) {
        if (!e.isCanceled() && e.getTarget() instanceof BlockRayTraceResult) {
            PlayerEntity player = Minecraft.getInstance().player;
            World world = player.getEntityWorld();
            ItemStack stack = player.getHeldItemMainhand().getItem() instanceof ItemShape ? player.getHeldItemMainhand() : player.getHeldItemOffhand();

            if (stack.getItem() instanceof ItemShape) {
                BlockRayTraceResult hit = (BlockRayTraceResult) e.getTarget();
                if (hit.getType() != RayTraceResult.Type.BLOCK || !world.isAirBlock(hit.getPos().offset(hit.getFace())))
                    return;
                float hX = (float) hit.getHitVec().x;
                float hY = (float) hit.getHitVec().y;
                float hZ = (float) hit.getHitVec().z;
                BlockPos pos = hit.getPos().offset(hit.getFace());
                Tessellator tess = Tessellator.getInstance();
                BufferBuilder buff = tess.getBuffer();
                ShapeRenderDispatcher shapeDispatcher = ClientProxy.SHAPE_RENDER_DISPATCHER;
                BlockShape blockShape = ArchitectureMod.CONTENT.blockShape;
                BlockArchitecture.IOrientationHandler oh = blockShape.getOrientationHandler();
                BlockState state = oh.onBlockPlaced(blockShape, world, pos, hit.getFace(), hX, hY, hZ, blockShape.getDefaultState(), player);
                TileShape shape = new TileShape();
                shape.setWorldAndPos(world, pos);
                shape.readFromItemStack(stack);
                simulatePlacement(player, world, shape, hit);
                Trans3 t = Trans3.blockCenter(pos).t(shape.localToGlobalTransformation(Vector3.zero));

                double tX = player.lastTickPosX + (player.serverPosX - player.lastTickPosX) * (double) e.getPartialTicks();
                double tY = player.lastTickPosY + (player.serverPosY - player.lastTickPosY) * (double) e.getPartialTicks();
                double tZ = player.lastTickPosZ + (player.serverPosZ - player.lastTickPosZ) * (double) e.getPartialTicks();

                GlStateManager.pushMatrix();
                GlStateManager.translated(-tX, -tY, -tZ);
                GlStateManager.depthMask(false);
                GlStateManager.enableCull();
                GlStateManager.enableBlend();
                GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA.param, GlStateManager.DestFactor.ONE.param, GlStateManager.SourceFactor.ONE.param, GlStateManager.DestFactor.ZERO.param);
                //buff.begin(7, DefaultVertexFormats.ITEM);
                //shapeDispatcher.renderShapeTE(shape, target, t, true, false, Utils.getColourFromState(shape.baseBlockState), -1);
                tess.draw();
                GlStateManager.depthMask(true);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }
    }

    private static void simulatePlacement(PlayerEntity player, IBlockReader world, TileShape shape, BlockRayTraceResult hit) {
        Vec3i direction = Vector3.getDirectionVec(hit.getFace());
        Vector3 hitVec = new Vector3(hit.getHitVec().subtract(hit.getPos().getX(), hit.getPos().getY(), hit.getPos().getZ()));
        hitVec = new Vector3(hitVec.x - direction.getX() - 0.5, hitVec.y - direction.getY() - 0.5, hitVec.z - direction.getZ() - 0.5);

        BlockPos neighbourPos = hit.getPos();
        BlockState neighbourState = world.getBlockState(neighbourPos);
        TileEntity neighbourTile = world.getTileEntity(neighbourPos);
        shape.getShape().orientOnPlacement(player, shape, neighbourPos, neighbourState, neighbourTile, hit.getFace(), hitVec);
    }

}
