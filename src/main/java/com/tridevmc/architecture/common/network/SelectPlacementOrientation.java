package com.tridevmc.architecture.common.network;

import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.item.ItemShape;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SelectPlacementOrientation(boolean offHand, int stateId) implements CustomPacketPayload {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(ArchitectureMod.MOD_ID, "placement_orientation");
    public static final Type<SelectPlacementOrientation> TYPE = new Type<>(ID);
    public static final int AUTOMATIC = -1;
    public static final StreamCodec<ByteBuf, SelectPlacementOrientation> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SelectPlacementOrientation::offHand,
            ByteBufCodecs.VAR_INT, SelectPlacementOrientation::stateId,
            SelectPlacementOrientation::new
    );

    @Override
    public Type<SelectPlacementOrientation> type() {
        return TYPE;
    }

    public void handle(IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        if (this.stateId == AUTOMATIC) {
            player.removeData(ArchitectureMod.CONTENT.placementOrientation);
            return;
        }
        if (player.isSpectator()) {
            return;
        }
        var hand = this.offHand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        if (!(player.getItemInHand(hand).getItem() instanceof ItemShape item)) {
            return;
        }
        var state = Block.stateById(this.stateId);
        if (state.getBlock() == item.getBlock()) {
            player.setData(ArchitectureMod.CONTENT.placementOrientation, state);
        }
    }
}
