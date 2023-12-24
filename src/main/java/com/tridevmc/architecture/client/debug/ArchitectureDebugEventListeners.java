package com.tridevmc.architecture.client.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tridevmc.architecture.core.math.IVector3;
import com.tridevmc.architecture.core.math.integer.IVector3i;
import com.tridevmc.architecture.core.model.Voxelizer;
import com.tridevmc.architecture.core.model.mesh.IPolygonData;
import com.tridevmc.architecture.core.model.mesh.Quad;
import com.tridevmc.architecture.core.model.mesh.Tri;
import com.tridevmc.architecture.core.physics.Ray;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.Objects;

import static com.tridevmc.architecture.client.debug.ArchitectureDebugRenderTypes.ARCHITECTURE_DEBUG_LINE;

/**
 * Quick and dirty debug renderer for voxelization.
 */
public class ArchitectureDebugEventListeners {
    public static BlockPos targetPos;
    public static Voxelizer targetVoxelizer;
    public static IVector3i currentVoxelizationOffset;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (targetVoxelizer == null || targetPos == null || event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS)
            return;

        if (currentVoxelizationOffset == null)
            currentVoxelizationOffset = targetVoxelizer.min();

        var matrix = event.getPoseStack();
        var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        var cameraPos = camera.getPosition();

        var box = targetVoxelizer.getBoxForOffset(currentVoxelizationOffset.x(), currentVoxelizationOffset.y(), currentVoxelizationOffset.z());
        var point = box.center();

        var meshBounds = targetVoxelizer.mesh().getBounds();
        var fromPoint = IVector3.ofImmutable(meshBounds.minX() - 1, point.y(), point.z());
        var rayDirection = IVector3.ofImmutable(1, 0, 0);
        var ray = new Ray(fromPoint, rayDirection);
        var hits = ray.intersectUnfiltered(targetVoxelizer.mesh())
                .toList();
        var matchingPolys = targetVoxelizer.mesh().getAABBTree().searchStream(new com.tridevmc.architecture.core.physics.AABB(fromPoint, fromPoint.add(rayDirection.mul(1000D)))).toList();
        var lineBuffer = bufferSource.getBuffer(ARCHITECTURE_DEBUG_LINE);

        RenderSystem.disableDepthTest();
        matrix.pushPose();
        matrix.translate(-cameraPos.x + targetPos.getX(), -cameraPos.y + targetPos.getY(), -cameraPos.z + targetPos.getZ());
        renderBox(matrix, lineBuffer, box);
        hits.forEach(hit -> {
            renderRayHit(matrix, lineBuffer, point, hit);
        });
        // Render each potential hit box in the mesh as purple.
        matchingPolys.forEach(b -> {
            if (b instanceof Quad<? extends IPolygonData<?>> quad) {
                var v0 = quad.getVertex(0).getPos();
                var v1 = quad.getVertex(1).getPos();
                var v2 = quad.getVertex(2).getPos();
                var v3 = quad.getVertex(3).getPos();
                renderLine(matrix, lineBuffer, v0, v1, 1F, 0, 1F, 1F);
                renderLine(matrix, lineBuffer, v1, v2, 1F, 0, 1F, 1F);
                renderLine(matrix, lineBuffer, v2, v3, 1F, 0, 1F, 1F);
                renderLine(matrix, lineBuffer, v3, v0, 1F, 0, 1F, 1F);
            } else if (b instanceof Tri<? extends IPolygonData<?>> tri) {
                var v0 = tri.getVertex(0).getPos();
                var v1 = tri.getVertex(1).getPos();
                var v2 = tri.getVertex(2).getPos();
                renderLine(matrix, lineBuffer, v0, v1, 1F, 0, 1F, 1F);
                renderLine(matrix, lineBuffer, v1, v2, 1F, 0, 1F, 1F);
                renderLine(matrix, lineBuffer, v2, v0, 1F, 0, 1F, 1F);
            }
        });
        if (targetVoxelizer.isBoxValidVoxel(box)) {
            LevelRenderer.renderLineBox(matrix, lineBuffer, box.deflate(1 / 32D).toMC(), 0, 0, 1F, 1);
        } else {
            LevelRenderer.renderLineBox(matrix, lineBuffer, box.deflate(1 / 32D).toMC(), 1F, 0.5F, 0F, 1);
        }
        matrix.popPose();
        bufferSource.endBatch(ARCHITECTURE_DEBUG_LINE);
        RenderSystem.enableDepthTest();
    }

    private static void renderBox(PoseStack matrix, VertexConsumer lineBuffer, com.tridevmc.architecture.core.physics.AABB box) {
        if (targetVoxelizer.doesBoxIntersect(box)) {
            LevelRenderer.renderLineBox(matrix, lineBuffer, box.toMC(), 0, 1F, 0, .8F);
        } else {
            LevelRenderer.renderLineBox(matrix, lineBuffer, box.toMC(), 1F, 0, 0, .8F);
        }
    }

    private static void renderRayHit(PoseStack matrix, VertexConsumer lineBuffer, IVector3 point, Ray.Hit hit) {
        if (hit.isValidHit()) {
            renderLine(matrix, lineBuffer, hit.ray().origin(), hit.point(), 0, 1F, 0, .8F);
            LevelRenderer.renderLineBox(matrix, lineBuffer, new AABB(hit.point().x(), hit.point().y(), hit.point().z(), hit.point().x(), hit.point().y(), hit.point().z()).inflate(1D / 256D),
                    hit.point().x() < point.x() ? 1F : 0F,
                    1F,
                    0F,
                    .8F);
        } else {
            renderLine(matrix, lineBuffer, hit.ray().origin(), hit.ray().origin().add(hit.ray().direction()), 1F, 0, 0, .8F);
        }
    }

    private static void renderLine(PoseStack matrix, VertexConsumer consumer, IVector3 from, IVector3 to, float r, float g, float b, float a) {
        consumer.vertex(matrix.last().pose(), (float) from.x(), (float) from.y(), (float) from.z()).color(r, g, b, a).endVertex();
        consumer.vertex(matrix.last().pose(), (float) to.x(), (float) to.y(), (float) to.z()).color(r, g, b, a).endVertex();
    }

    private static boolean shouldAssignVoxelizer(Level level, Player player) {
        return !FMLEnvironment.production && level.isClientSide && player.isCrouching();
    }

    public static InteractionResult onVoxelizedBlockClicked(Level level, BlockPos pos, Player player, BlockHitResult hit, Voxelizer voxelizer) {
        if (!shouldAssignVoxelizer(level, player))
            return InteractionResult.PASS;
        if (!Objects.equals(ArchitectureDebugEventListeners.targetPos, pos)) {
            ArchitectureDebugEventListeners.targetPos = pos;
            ArchitectureDebugEventListeners.targetVoxelizer = voxelizer;
            ArchitectureDebugEventListeners.currentVoxelizationOffset = voxelizer.min();
            player.sendSystemMessage(Component.literal("Voxelizer set to " + pos));
        } else {
            // Set the new voxelization offset using the player's facing direction and the current offset.
            var facing = hit.getDirection().getOpposite();
            ArchitectureDebugEventListeners.currentVoxelizationOffset = ArchitectureDebugEventListeners.currentVoxelizationOffset.asMutable().add(
                    facing.getStepX(),
                    facing.getStepY(),
                    facing.getStepZ()
            );
            player.displayClientMessage(Component.literal("Voxelizer offset set to " + ArchitectureDebugEventListeners.currentVoxelizationOffset), true);
        }
        return InteractionResult.SUCCESS;
    }

}
