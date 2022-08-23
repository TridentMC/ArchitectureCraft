package com.tridevmc.architecture.common.ui;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Used as additional context for opening a UI or container, passed in IElementProvider instead of direct fields.
 */
public class CreateMenuContext {

    private final int windowId;
    private final Player player;
    private final Inventory playerInventory;
    private BlockPos pos;
    private BlockState blockState;
    private BlockEntity blockEntity;
    private Entity entity;

    public CreateMenuContext(int windowId, Player player, Inventory playerInventory) {
        this.windowId = windowId;
        this.player = player;
        this.playerInventory = playerInventory;
    }

    public CreateMenuContext setPos(@Nullable BlockPos pos) {
        this.pos = pos;
        return this;
    }

    public CreateMenuContext setBlockState(@Nullable BlockState blockState) {
        this.blockState = blockState;
        return this;
    }

    public CreateMenuContext setBlockEntity(@Nullable BlockEntity blockEntity) {
        this.blockEntity = blockEntity;
        return this;
    }

    public CreateMenuContext setEntity(@Nullable Entity entity) {
        this.entity = entity;
        return this;
    }

    public int getWindowId() {
        return this.windowId;
    }

    @Nonnull
    public Player getPlayer() {
        return this.player;
    }

    @Nonnull
    public Inventory getPlayerInventory() {
        return this.playerInventory;
    }

    @Nullable
    public BlockPos getPos() {
        return this.pos;
    }

    @Nullable
    public BlockState getBlockState() {
        return this.blockState;
    }

    @Nullable
    public BlockEntity getBlockEntity() {
        return this.blockEntity;
    }

    @Nullable
    public Entity getEntity() {
        return this.entity;
    }

    public boolean hasPos() {
        return this.pos != null;
    }

    public boolean hasBlockState() {
        return this.blockState != null;
    }

    public boolean hasTile() {
        return this.blockEntity != null;
    }

    public boolean hasEntity() {
        return this.entity != null;
    }
}
