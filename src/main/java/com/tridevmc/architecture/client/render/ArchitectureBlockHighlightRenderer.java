package com.tridevmc.architecture.client.render;

import com.tridevmc.architecture.client.debug.ArchitectureDebugRenderTypes;
import com.tridevmc.architecture.client.render.model.objson.LegacyOBJSON;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.block.state.BlockStateArchitecture;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ArchitectureBlockHighlightRenderer {
    @SubscribeEvent
    public static void onRenderBlockHighlight(RenderHighlightEvent.Block event) {
        if (event.isCanceled() || !event.isCanceled())
            return;

        var level = Minecraft.getInstance().level;
        var target = event.getTarget();
        var pos = target.getBlockPos();
        var state = level.getBlockState(target.getBlockPos());
        var matrix = event.getPoseStack();
        var cameraPosition = event.getCamera().getPosition();
        matrix.pushPose();
        matrix.translate(pos.getX() - cameraPosition.x + 0.5, pos.getY() - cameraPosition.y + 0.5, pos.getZ() - cameraPosition.z + 0.5);
        if (state instanceof BlockStateArchitecture aState) {
            var objson = ArchitectureMod.PROXY.getCachedOBJSON(aState.getModelSpec().modelName);
            if (objson != null) {
                var edges = objson.calculateOuterEdges();
                var vertexConsumer = event.getMultiBufferSource().getBuffer(ArchitectureDebugRenderTypes.ARCHITECTURE_DEBUG_LINE);
                for (LegacyOBJSON.Edge edge : edges) {
                    vertexConsumer.vertex(matrix.last().pose(), (float) edge.aX(), (float) edge.aY(), (float) edge.aZ()).color(1F, 1F, 0, .8F).endVertex();
                    vertexConsumer.vertex(matrix.last().pose(), (float) edge.bX(), (float) edge.bY(), (float) edge.bZ()).color(1F, 1F, 0, .8F).endVertex();
                }
                event.setCanceled(true);
            }
        }
        matrix.popPose();
    }
}
