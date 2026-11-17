package com.tridevmc.architecture.client.placement;

import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.item.ItemShape;
import com.tridevmc.architecture.common.network.SelectPlacementOrientation;
import com.tridevmc.architecture.common.shape.orientation.EnumSpin;
import com.tridevmc.architecture.common.shape.orientation.EnumTurn;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyAxis;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFacing;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFlip;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyOffset;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertySpin;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyTurn;
import com.tridevmc.architecture.common.shape.placement.ShapePlacementSelection;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyModifier;

public final class ShapePlacementControls {
    private static final KeyMapping ROTATE = key("rotate_placement", KeyModifier.NONE);
    private static final KeyMapping MOUNT = key("change_placement_face", KeyModifier.SHIFT);
    private static final KeyMapping OFFSET = key("change_placement_offset", KeyModifier.CONTROL);
    private static final KeyMapping RESET = key("automatic_placement", KeyModifier.ALT);

    private ShapePlacementControls() {
    }

    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ROTATE);
        event.register(MOUNT);
        event.register(OFFSET);
        event.register(RESET);
    }

    @SubscribeEvent
    public static void onPlayerClone(ClientPlayerNetworkEvent.Clone event) {
        ClientPacketDistributor.sendToServer(new SelectPlacementOrientation(false, SelectPlacementOrientation.AUTOMATIC));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        var reset = RESET.consumeClick();
        var rotate = ROTATE.consumeClick();
        var mount = MOUNT.consumeClick();
        var offset = OFFSET.consumeClick();
        if (!(reset || rotate || mount || offset)) {
            return;
        }
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null || player.isSpectator() || mc.gui.screen() != null) {
            return;
        }
        var hand = player.getMainHandItem().getItem() instanceof ItemShape
                ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        var stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof ItemShape item)) {
            return;
        }
        var attachment = ArchitectureMod.CONTENT.placementOrientation;
        if (reset) {
            player.removeData(attachment);
            ClientPacketDistributor.sendToServer(new SelectPlacementOrientation(
                    hand == InteractionHand.OFF_HAND, SelectPlacementOrientation.AUTOMATIC));
            player.sendOverlayMessage(Component.translatable("placement.architecturecraft.automatic"));
            return;
        }
        var state = player.getExistingDataOrNull(attachment);
        if (state == null || state.getBlock() != item.getBlock()) {
            state = mc.hitResult instanceof BlockHitResult hit && hit.getType() == HitResult.Type.BLOCK
                    ? item.getBlock().getStateForPlacement(new BlockPlaceContext(player, hand, stack, hit))
                    : item.getBlock().defaultBlockState();
        }
        if (state == null) {
            return;
        }
        var selected = offset ? cycle(state, ShapeOrientationPropertyOffset.INSTANCE)
                : mount ? cycleMount(state) : cycleRotation(state);
        if (selected == state) {
            return;
        }
        player.setData(attachment, selected);
        ClientPacketDistributor.sendToServer(new SelectPlacementOrientation(
                hand == InteractionHand.OFF_HAND, Block.getId(selected)));
        var label = Component.translatable("placement.architecturecraft.selected");
        for (var property : ShapePlacementSelection.properties(selected)) {
            label.append("  ").append(describe(selected, property));
        }
        player.sendOverlayMessage(label);
    }

    @SubscribeEvent
    public static void onExtractLevelRenderState(ExtractLevelRenderStateEvent event) {
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player == null || player.isSpectator() || mc.gui.screen() != null
                || !(mc.hitResult instanceof BlockHitResult hit) || hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        var selected = player.getExistingDataOrNull(ArchitectureMod.CONTENT.placementOrientation);
        if (selected == null) {
            return;
        }
        var hand = player.getMainHandItem().getItem() instanceof ItemShape
                ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        var stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof ItemShape item) || item.getBlock() != selected.getBlock()) {
            return;
        }
        var context = new BlockPlaceContext(player, hand, stack, hit);
        var pos = context.getClickedPos();
        var level = context.getLevel();
        var state = item.getBlock().getStateForPlacement(context);
        if (state == null) {
            return;
        }
        var allowed = context.canPlace() && state.canSurvive(level, pos)
                && level.isUnobstructed(state, pos, CollisionContext.placementContext(player));
        var color = allowed ? 0xC03FDCC2 : 0xC0FF5757;
        try (var collector = event.getLevelExtractor().collectPerFrameMainThreadGizmos()) {
            state.getShape(level, pos, CollisionContext.of(player)).forAllEdges((x1, y1, z1, x2, y2, z2) ->
                    Gizmos.line(new Vec3(pos.getX() + x1, pos.getY() + y1, pos.getZ() + z1),
                            new Vec3(pos.getX() + x2, pos.getY() + y2, pos.getZ() + z2), color, 1.5F));
        }
    }

    private static KeyMapping key(String name, KeyModifier modifier) {
        return new KeyMapping("key.architecturecraft." + name, KeyConflictContext.IN_GAME, modifier,
                InputConstants.Type.KEYBOARD, InputConstants.KEY_R, KeyMapping.Category.GAMEPLAY);
    }

    private static BlockState cycleMount(BlockState state) {
        return state.hasProperty(ShapeOrientationPropertyFacing.INSTANCE)
                ? state.cycle(ShapeOrientationPropertyFacing.INSTANCE)
                : cycle(state, ShapeOrientationPropertyAxis.INSTANCE);
    }

    private static BlockState cycleRotation(BlockState state) {
        if (state.hasProperty(ShapeOrientationPropertySpin.INSTANCE)) {
            return state.cycle(ShapeOrientationPropertySpin.INSTANCE);
        }
        if (state.hasProperty(ShapeOrientationPropertyTurn.INSTANCE)) {
            var rotated = state.cycle(ShapeOrientationPropertyTurn.INSTANCE);
            return rotated.getValue(ShapeOrientationPropertyTurn.INSTANCE) == EnumTurn.UNTURNED
                    ? cycle(rotated, ShapeOrientationPropertyFlip.INSTANCE) : rotated;
        }
        return state.hasProperty(ShapeOrientationPropertyFlip.INSTANCE)
                ? state.cycle(ShapeOrientationPropertyFlip.INSTANCE) : cycleMount(state);
    }

    private static <T extends Comparable<T>> BlockState cycle(BlockState state, Property<T> property) {
        return state.hasProperty(property) ? state.cycle(property) : state;
    }

    private static <T extends Comparable<T>> Component describe(BlockState state, Property<T> property) {
        var value = state.getValue(property);
        var description = value instanceof EnumSpin spin ? spin.getDegrees() + "\u00B0" : property.getName(value);
        return Component.translatable("placement.architecturecraft." + property.getName(), description);
    }
}
