package com.tridevmc.architecture.common.tile;

import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.compound.ui.container.CompoundContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.PlayerContainer;

import javax.annotation.Nullable;

public class ContainerSawbench extends CompoundContainer {
    public final TileSawbench sawbench;
    public final PlayerInventory playerInventory;
    protected ContainerSawbench(TileSawbench sawbench, PlayerInventory playerInventory, int id) {
        super(ArchitectureMod.CONTENT.universalContainerType, id);
        this.sawbench = sawbench;
        this.playerInventory = playerInventory;
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return false;
    }
}
