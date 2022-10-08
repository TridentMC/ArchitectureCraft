package com.tridevmc.architecture.client.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tridevmc.architecture.client.render.model.objson.OBJSONVoxelizer;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.render.ModelSpec;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.behaviour.ShapeBehaviourModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.Objects;

import static com.tridevmc.architecture.client.debug.ArchitectureRenderTypes.ARCHITECTURE_LINE_TYPE;

/**
 * Quick and dirty debug renderer for voxelization.
 */
public class ArchitectureDebugEventListeners {
    public static BlockPos targetPos;
    public static OBJSONVoxelizer targetVoxelizer;
    public static Vec3i currentVoxelizationOffset;

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (targetVoxelizer == null || targetPos == null || event.getStage() != RenderLevelStageEvent.Stage.AFTER_CUTOUT_BLOCKS)
            return;

        if (currentVoxelizationOffset == null)
            currentVoxelizationOffset = targetVoxelizer.getMin();

        var matrix = event.getPoseStack();
        var bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera();
        var cameraPos = camera.getPosition();

        var box = targetVoxelizer.getBoxForOffset(currentVoxelizationOffset);
        var point = box.getCenter();

        var meshBounds = targetVoxelizer.getMesh().getBounds();
        var fromPoint = new Vec3(meshBounds.minX - 1, point.y, point.z);
        var toPoint = new Vec3(meshBounds.maxX + 1, point.y, point.z);
        var rayDirection = toPoint.subtract(fromPoint);
        var ray = new OBJSONVoxelizer.Ray(fromPoint, rayDirection);
        var hits = ray.intersectUnfiltered(targetVoxelizer.getMesh())
                .toList();
        var lineBuffer = bufferSource.getBuffer(ARCHITECTURE_LINE_TYPE);

        RenderSystem.disableDepthTest();
        matrix.pushPose();
        matrix.translate(-cameraPos.x + targetPos.getX(), -cameraPos.y + targetPos.getY(), -cameraPos.z + targetPos.getZ());
        renderBox(matrix, lineBuffer, box);
        hits.forEach(hit -> {
            renderRayHit(matrix, lineBuffer, point, hit);
        });
        if (targetVoxelizer.isBoxValidVoxel(box)) {
            LevelRenderer.renderLineBox(matrix, lineBuffer, box, 0, 0, 1F, 1);
        } else {
            LevelRenderer.renderLineBox(matrix, lineBuffer, box, 1F, 0.5F, 0F, 1);
        }
        matrix.popPose();
        bufferSource.endBatch(ARCHITECTURE_LINE_TYPE);
        RenderSystem.enableDepthTest();
    }

    private static void renderBox(PoseStack matrix, VertexConsumer lineBuffer, AABB box) {
        if (targetVoxelizer.doesBoxIntersect(box)) {
            LevelRenderer.renderLineBox(matrix, lineBuffer, box, 0, 1F, 0, .8F);
        } else {
            LevelRenderer.renderLineBox(matrix, lineBuffer, box, 1F, 0, 0, .8F);
        }
    }

    private static void renderRayHit(PoseStack matrix, VertexConsumer lineBuffer, Vec3 point, OBJSONVoxelizer.Ray.Hit hit) {
        if (hit.isValidHit()) {
            renderLine(matrix, lineBuffer, hit.ray().origin(), hit.point(), 0, 1F, 0, .8F);
            LevelRenderer.renderLineBox(matrix, lineBuffer, new AABB(hit.point().x, hit.point().y, hit.point().z, hit.point().x, hit.point().y, hit.point().z).inflate(1D / 256D),
                    hit.point().x < point.x ? 1F : 0F,
                    1F,
                    0F,
                    .8F);
        } else {
            renderLine(matrix, lineBuffer, hit.ray().origin(), hit.ray().origin().add(hit.ray().direction()), 1F, 0, 0, .8F);
        }
    }

    private static void renderLine(PoseStack matrix, VertexConsumer consumer, Vec3 from, Vec3 to, float r, float g, float b, float a) {
        consumer.vertex(matrix.last().pose(), (float) from.x, (float) from.y, (float) from.z).color(r, g, b, a).endVertex();
        consumer.vertex(matrix.last().pose(), (float) to.x, (float) to.y, (float) to.z).color(r, g, b, a).endVertex();
    }

    private static boolean shouldAssignVoxelizer(Level level, Player player) {
        return !FMLEnvironment.production && level.isClientSide && player.isCrouching();
    }

    public static InteractionResult onVoxelizedBlockClicked(Level level, BlockPos pos, Player player, BlockHitResult hit, EnumShape shape) {
        if (!shouldAssignVoxelizer(level, player))
            return InteractionResult.PASS;
        if (shape.behaviour instanceof ShapeBehaviourModel behaviour) {
            return onVoxelizedBlockClicked(level, pos, player, hit, ArchitectureMod.PROXY.getCachedOBJSON(behaviour.getModelName()).getVoxelizer());
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult onVoxelizedBlockClicked(Level level, BlockPos pos, Player player, BlockHitResult hit, ModelSpec spec) {
        if (!shouldAssignVoxelizer(level, player))
            return InteractionResult.PASS;
        return onVoxelizedBlockClicked(level, pos, player, hit, ArchitectureMod.PROXY.getCachedOBJSON(spec.modelName).getVoxelizer());
    }

    public static InteractionResult onVoxelizedBlockClicked(Level level, BlockPos pos, Player player, BlockHitResult hit, OBJSONVoxelizer voxelizer) {
        if (!Objects.equals(ArchitectureDebugEventListeners.targetPos, pos)) {
            ArchitectureDebugEventListeners.targetPos = pos;
            ArchitectureDebugEventListeners.targetVoxelizer = voxelizer;
            ArchitectureDebugEventListeners.currentVoxelizationOffset = voxelizer.getMin();
            player.sendSystemMessage(Component.literal("Voxelizer set to " + voxelizer.getObjson().getName() + " at " + pos));
        } else {
            // Set the new voxelization offset using the player's facing direction and the current offset.
            var facing = hit.getDirection().getOpposite();
            ArchitectureDebugEventListeners.currentVoxelizationOffset = ArchitectureDebugEventListeners.currentVoxelizationOffset.offset(
                    new Vec3i(
                            facing.getStepX(),
                            facing.getStepY(),
                            facing.getStepZ()
                    )
            );
            player.sendSystemMessage(Component.literal("Voxelizer offset set to " + ArchitectureDebugEventListeners.currentVoxelizationOffset));
        }
        return InteractionResult.SUCCESS;
    }

}
