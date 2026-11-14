package com.tridevmc.architecture.client.debug;

import com.tridevmc.architecture.core.math.integer.IVector3i;
import com.tridevmc.architecture.core.math.integer.IVector3iMutable;
import com.tridevmc.architecture.core.model.voxelize.IVoxelizer;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;

import java.util.LinkedHashMap;
import java.util.Map;

public class ArchitectureDebugEventListeners {
    private static final int MAX_DEBUG_MODELS = 6;
    private static final Map<BlockPos, DebugModel> MODELS = new LinkedHashMap<>();

    private record DebugModel(IVoxelizer voxelizer, IVector3iMutable offset) {}

    @SubscribeEvent
    public static void onExtractLevel(ExtractLevelRenderStateEvent event) {
        try (var collector = event.getLevelExtractor().collectPerFrameMainThreadGizmos()) {
            MODELS.forEach((pos, model) -> {
                var box = model.voxelizer.getBoxForOffset(model.offset.x(), model.offset.y(), model.offset.z());
                int color = model.voxelizer.isBoxValidVoxel(box) ? 0xFF0080FF : 0xFFFF8000;
                Gizmos.cuboid(box.toMC().move(pos), GizmoStyle.stroke(color)).setAlwaysOnTop();
                Gizmos.cuboid(model.voxelizer.getMesh().getBounds().toMC().move(pos),
                        GizmoStyle.stroke(0xFF00FF00));
            });
        }
    }

    public static InteractionResult onVoxelizedBlockClicked(Level level, BlockPos pos, Player player,
                                                            BlockHitResult hit, IVoxelizer voxelizer) {
        if (FMLEnvironment.isProduction() || !level.isClientSide() || !player.isCrouching()) {
            return InteractionResult.PASS;
        }
        var model = MODELS.get(pos);
        if (model == null) {
            if (MODELS.size() == MAX_DEBUG_MODELS) MODELS.remove(MODELS.keySet().iterator().next());
            MODELS.put(pos.immutable(), new DebugModel(voxelizer, IVector3i.ofMutable(0, 0, 0)));
        } else {
            var direction = hit.getDirection().getOpposite();
            model.offset.add(direction.getStepX(), direction.getStepY(), direction.getStepZ());
            player.sendOverlayMessage(Component.literal("Voxelizer offset: " + model.offset));
        }
        return InteractionResult.SUCCESS;
    }
}
