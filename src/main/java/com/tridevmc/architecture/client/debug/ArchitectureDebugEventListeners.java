package com.tridevmc.architecture.client.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tridevmc.architecture.core.math.IVector3;
import com.tridevmc.architecture.core.math.integer.IVector3i;
import com.tridevmc.architecture.core.math.integer.IVector3iMutable;
import com.tridevmc.architecture.core.model.mesh.IPolygonData;
import com.tridevmc.architecture.core.model.mesh.Quad;
import com.tridevmc.architecture.core.model.mesh.Tri;
import com.tridevmc.architecture.core.model.voxelize.IVoxelizer;
import com.tridevmc.architecture.core.model.voxelize.Voxelizer;
import com.tridevmc.architecture.core.physics.Ray;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.HashMap;
import java.util.Map;

import static com.tridevmc.architecture.client.debug.ArchitectureDebugRenderTypes.ARCHITECTURE_DEBUG_LINE;

/**
 * Quick and dirty debug renderer for voxelization.
 */
public class ArchitectureDebugEventListeners {

    private record DebugRenderData(BlockPos pos, IVoxelizer voxelizer, IVector3iMutable offset) {
        void onActivate(Player player, BlockHitResult hit) {
            var face = hit.getDirection().getOpposite();
            offset.add(face.getStepX(), face.getStepY(), face.getStepZ());
            player.displayClientMessage(Component.literal("Voxelizer offset set to " + offset), true);
        }
    }

    private static final int MAX_DEBUG_RENDER_DATA = 6;
    private static final Map<BlockPos, DebugRenderData> debugRenderData = new HashMap<>();
    private static final BlockPos[] debugRenderDataKeysByAge = new BlockPos[MAX_DEBUG_RENDER_DATA];


    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES)
            return;

        var pose = event.getPoseStack();
        var camera = event.getCamera().getPosition();
        var consumer = Minecraft.getInstance().renderBuffers().bufferSource().getBuffer(ARCHITECTURE_DEBUG_LINE);

        for (var data : debugRenderData.values()) {
            if (data != null) {
                renderDebugData(pose, camera, consumer, data);
            }
        }
    }

    private static void renderDebugData(PoseStack pose, Vec3 camera, VertexConsumer consumer, DebugRenderData data) {
        var voxelizer = data.voxelizer();
        var pos = data.pos();
        var voxOffset = data.offset();

        var box = voxelizer.getBoxForOffset(voxOffset.x(), voxOffset.y(), voxOffset.z());
        var point = box.center();

        var meshBounds = voxelizer.getMesh().getBounds();
        var fromPoint = IVector3.ofImmutable(meshBounds.minX() - 1, point.y(), point.z());
        var rayDirection = IVector3.ofImmutable(1, 0, 0);
        var ray = new Ray(fromPoint, rayDirection);
        var hits = ray.intersectUnfiltered(voxelizer.getMesh()).toList();
        var matchingPolys = voxelizer.getMesh().getAABBTree().searchStream(new com.tridevmc.architecture.core.physics.AABB(fromPoint, fromPoint.add(rayDirection.mul(1000D)))).toList();

        RenderSystem.disableDepthTest();
        pose.pushPose();
        var offset = Vec3.atLowerCornerOf(pos).subtract(camera);
        pose.translate(offset.x, offset.y, offset.z);
        renderBox(voxelizer, pose, consumer, box);
        hits.forEach(hit -> {
            renderRayHit(pose, consumer, point, hit);
        });
        // Render each potential hit box in the mesh as purple.
        matchingPolys.forEach(b -> {
            if (b instanceof Quad<? extends IPolygonData<?>> quad) {
                var v0 = quad.getVertex(0).getPos();
                var v1 = quad.getVertex(1).getPos();
                var v2 = quad.getVertex(2).getPos();
                var v3 = quad.getVertex(3).getPos();
                renderLine(pose, consumer, v0, v1, 1F, 0, 1F, 1F);
                renderLine(pose, consumer, v1, v2, 1F, 0, 1F, 1F);
                renderLine(pose, consumer, v2, v3, 1F, 0, 1F, 1F);
                renderLine(pose, consumer, v3, v0, 1F, 0, 1F, 1F);
            } else if (b instanceof Tri<? extends IPolygonData<?>> tri) {
                var v0 = tri.getVertex(0).getPos();
                var v1 = tri.getVertex(1).getPos();
                var v2 = tri.getVertex(2).getPos();
                renderLine(pose, consumer, v0, v1, 1F, 0, 1F, 1F);
                renderLine(pose, consumer, v1, v2, 1F, 0, 1F, 1F);
                renderLine(pose, consumer, v2, v0, 1F, 0, 1F, 1F);
            }
        });
        if (voxelizer.isBoxValidVoxel(box)) {
            ShapeRenderer.renderLineBox(pose, consumer, box.deflate(1 / 32D).toMC(), 0, 0, 1F, 1);
        } else {
            ShapeRenderer.renderLineBox(pose, consumer, box.deflate(1 / 32D).toMC(), 1F, 0.5F, 0F, 1);
        }
        pose.popPose();
        RenderSystem.enableDepthTest();
    }

    private static void renderBox(IVoxelizer voxelizer, PoseStack matrix, VertexConsumer lineBuffer, com.tridevmc.architecture.core.physics.AABB box) {
        if (voxelizer.doesBoxIntersect(box)) {
            ShapeRenderer.renderLineBox(matrix, lineBuffer, box.toMC(), 0, 1F, 0, .8F);
        } else {
            ShapeRenderer.renderLineBox(matrix, lineBuffer, box.toMC(), 1F, 0, 0, .8F);
        }
    }

    private static void renderRayHit(PoseStack matrix, VertexConsumer lineBuffer, IVector3 point, Ray.Hit hit) {
        if (hit.isValidHit()) {
            renderLine(matrix, lineBuffer, hit.ray().origin(), hit.point(), 0, 1F, 0, .8F);
            ShapeRenderer.renderLineBox(matrix, lineBuffer, new AABB(hit.point().x(), hit.point().y(), hit.point().z(), hit.point().x(), hit.point().y(), hit.point().z()).inflate(1D / 256D),
                    hit.point().x() < point.x() ? 1F : 0F,
                    1F,
                    0F,
                    .8F);
        } else {
            renderLine(matrix, lineBuffer, hit.ray().origin(), hit.ray().origin().add(hit.ray().direction()), 1F, 0, 0, .8F);
        }
    }

    private static void renderLine(PoseStack matrix, VertexConsumer consumer, IVector3 from, IVector3 to, float r, float g, float b, float a) {
        consumer.addVertex(matrix.last().pose(), (float) from.x(), (float) from.y(), (float) from.z()).setColor(r, g, b, a);
        consumer.addVertex(matrix.last().pose(), (float) to.x(), (float) to.y(), (float) to.z()).setColor(r, g, b, a);
    }

    private static boolean shouldAssignVoxelizer(Level level, Player player) {
        return !FMLEnvironment.production && level.isClientSide && player.isCrouching();
    }

    public static InteractionResult onVoxelizedBlockClicked(Level level, BlockPos pos, Player player, BlockHitResult hit, IVoxelizer voxelizer) {
        if (!shouldAssignVoxelizer(level, player))
            return InteractionResult.PASS;

        var data = debugRenderData.get(pos);

        if (data == null) {
            if (debugRenderData.size() == MAX_DEBUG_RENDER_DATA) {
                var oldest = debugRenderDataKeysByAge[0];
                debugRenderData.remove(oldest);
                // Shift all keys down by one and assign the last key to null.
                for (int i = 0; i < MAX_DEBUG_RENDER_DATA - 1; i++) {
                    debugRenderDataKeysByAge[i] = debugRenderDataKeysByAge[i + 1];
                }
                debugRenderDataKeysByAge[MAX_DEBUG_RENDER_DATA - 1] = null;

                player.displayClientMessage(Component.literal("Removed voxelizer debug data for " + oldest), true);
            }

            var debugData = new DebugRenderData(pos, voxelizer, IVector3i.ofMutable(0, 0, 0));
            debugRenderData.put(pos, debugData);
            for (int i = 0; i < MAX_DEBUG_RENDER_DATA; i++) {
                if (debugRenderDataKeysByAge[i] == null) {
                    debugRenderDataKeysByAge[i] = pos;
                    break;
                }
            }

            player.displayClientMessage(Component.literal("Added voxelizer debug data for " + pos), true);
        } else {
            data.onActivate(player, hit);
        }

        return InteractionResult.SUCCESS;
    }

}
