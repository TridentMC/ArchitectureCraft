package com.tridevmc.architecture.common.ui;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Used as additional context for opening a UI or container, passed in IElementProvider instead of direct fields.
 */
public class CreateMenuContext {

    private final int windowId;
    private final PlayerEntity player;
    private final PlayerInventory playerInventory;
    private BlockPos pos;
    private BlockState blockState;
    private TileEntity tile;
    private Entity entity;

    public CreateMenuContext(int windowId, PlayerEntity player, PlayerInventory playerInventory) {
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

    public CreateMenuContext setTile(@Nullable TileEntity tile) {
        this.tile = tile;
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
    public PlayerEntity getPlayer() {
        return this.player;
    }

    @Nonnull
    public PlayerInventory getPlayerInventory() {
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
    public TileEntity getTile() {
        return this.tile;
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
        return this.tile != null;
    }

    public boolean hasEntity() {
        return this.entity != null;
    }
}
