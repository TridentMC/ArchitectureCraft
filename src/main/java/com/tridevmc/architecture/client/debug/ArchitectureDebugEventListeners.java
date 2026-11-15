package com.tridevmc.architecture.client.debug;

import com.tridevmc.architecture.core.math.IVector3;
import com.tridevmc.architecture.core.math.integer.IVector3i;
import com.tridevmc.architecture.core.math.integer.IVector3iMutable;
import com.tridevmc.architecture.core.model.mesh.IPolygonData;
import com.tridevmc.architecture.core.model.mesh.Quad;
import com.tridevmc.architecture.core.model.mesh.Tri;
import com.tridevmc.architecture.core.model.voxelize.IVoxelizer;
import com.tridevmc.architecture.core.physics.Ray;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;

import java.util.HashMap;
import java.util.Map;

/**
 * Quick and dirty debug renderer for voxelization.
 */
public class ArchitectureDebugEventListeners {

    private record DebugRenderData(BlockPos pos, IVoxelizer voxelizer, IVector3iMutable offset) {
        void onActivate(Player player, BlockHitResult hit) {
            var face = hit.getDirection().getOpposite();
            offset.add(face.getStepX(), face.getStepY(), face.getStepZ());
            player.sendOverlayMessage(Component.literal("Voxelizer offset set to " + offset));
        }
    }

    private static final int MAX_DEBUG_RENDER_DATA = 6;
    private static final Map<BlockPos, DebugRenderData> debugRenderData = new HashMap<>();
    private static final BlockPos[] debugRenderDataKeysByAge = new BlockPos[MAX_DEBUG_RENDER_DATA];


    @SubscribeEvent
    public static void onExtractLevelRenderState(ExtractLevelRenderStateEvent event) {
        try (var collector = event.getLevelExtractor().collectPerFrameMainThreadGizmos()) {
            for (var data : debugRenderData.values()) {
                if (data != null) {
                    renderDebugData(data);
                }
            }
        }
    }

    private static void renderDebugData(DebugRenderData data) {
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

        renderBox(voxelizer, pos, box);
        hits.forEach(hit -> {
            renderRayHit(pos, point, hit);
        });
        // Render each potential hit box in the mesh as purple.
        matchingPolys.forEach(b -> {
            if (b instanceof Quad<? extends IPolygonData<?>> quad) {
                var v0 = quad.getVertex(0).getPos();
                var v1 = quad.getVertex(1).getPos();
                var v2 = quad.getVertex(2).getPos();
                var v3 = quad.getVertex(3).getPos();
                renderLine(pos, v0, v1, 1F, 0, 1F, 1F);
                renderLine(pos, v1, v2, 1F, 0, 1F, 1F);
                renderLine(pos, v2, v3, 1F, 0, 1F, 1F);
                renderLine(pos, v3, v0, 1F, 0, 1F, 1F);
            } else if (b instanceof Tri<? extends IPolygonData<?>> tri) {
                var v0 = tri.getVertex(0).getPos();
                var v1 = tri.getVertex(1).getPos();
                var v2 = tri.getVertex(2).getPos();
                renderLine(pos, v0, v1, 1F, 0, 1F, 1F);
                renderLine(pos, v1, v2, 1F, 0, 1F, 1F);
                renderLine(pos, v2, v0, 1F, 0, 1F, 1F);
            }
        });
        if (voxelizer.isBoxValidVoxel(box)) {
            Gizmos.cuboid(box.deflate(1 / 32D).toMC().move(pos), GizmoStyle.stroke(0xFF0000FF, 1F)).setAlwaysOnTop();
        } else {
            Gizmos.cuboid(box.deflate(1 / 32D).toMC().move(pos), GizmoStyle.stroke(ARGB.colorFromFloat(1, 1F, 0.5F, 0), 1F)).setAlwaysOnTop();
        }
    }

    private static void renderBox(IVoxelizer voxelizer, BlockPos pos, com.tridevmc.architecture.core.physics.AABB box) {
        if (voxelizer.doesBoxIntersect(box)) {
            Gizmos.cuboid(box.toMC().move(pos), GizmoStyle.stroke(ARGB.colorFromFloat(.8F, 0, 1F, 0), 1F)).setAlwaysOnTop();
        } else {
            Gizmos.cuboid(box.toMC().move(pos), GizmoStyle.stroke(ARGB.colorFromFloat(.8F, 1F, 0, 0), 1F)).setAlwaysOnTop();
        }
    }

    private static void renderRayHit(BlockPos pos, IVector3 point, Ray.Hit hit) {
        if (hit.isValidHit()) {
            renderLine(pos, hit.ray().origin(), hit.point(), 0, 1F, 0, .8F);
            Gizmos.cuboid(new AABB(hit.point().x(), hit.point().y(), hit.point().z(), hit.point().x(), hit.point().y(), hit.point().z()).inflate(1D / 256D).move(pos),
                    GizmoStyle.stroke(ARGB.colorFromFloat(.8F,
                            hit.point().x() < point.x() ? 1F : 0F,
                            1F,
                            0F), 1F)).setAlwaysOnTop();
        } else {
            renderLine(pos, hit.ray().origin(), hit.ray().origin().add(hit.ray().direction()), 1F, 0, 0, .8F);
        }
    }

    private static void renderLine(BlockPos pos, IVector3 from, IVector3 to, float r, float g, float b, float a) {
        var offset = Vec3.atLowerCornerOf(pos);
        Gizmos.line(new Vec3(from.x(), from.y(), from.z()).add(offset),
                new Vec3(to.x(), to.y(), to.z()).add(offset), ARGB.colorFromFloat(a, r, g, b), 1F).setAlwaysOnTop();
    }

    private static boolean shouldAssignVoxelizer(Level level, Player player) {
        return !FMLEnvironment.isProduction() && level.isClientSide() && player.isCrouching();
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

                player.sendOverlayMessage(Component.literal("Removed voxelizer debug data for " + oldest));
            }

            var debugData = new DebugRenderData(pos, voxelizer, IVector3i.ofMutable(0, 0, 0));
            debugRenderData.put(pos, debugData);
            for (int i = 0; i < MAX_DEBUG_RENDER_DATA; i++) {
                if (debugRenderDataKeysByAge[i] == null) {
                    debugRenderDataKeysByAge[i] = pos;
                    break;
                }
            }

            player.sendOverlayMessage(Component.literal("Added voxelizer debug data for " + pos));
        } else {
            data.onActivate(player, hit);
        }

        return InteractionResult.SUCCESS;
    }

}
