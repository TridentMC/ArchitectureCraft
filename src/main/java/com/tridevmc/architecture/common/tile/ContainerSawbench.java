package com.tridevmc.architecture.common.tile;

import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.compound.ui.container.CompoundContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;

import javax.annotation.Nullable;

public class ContainerSawbench extends CompoundContainer {
    public final PlayerInventory playerInventory;
    public ContainerSawbench(PlayerInventory playerInv, int id) {
        super(ArchitectureMod.CONTENT.universalContainerType, id);
        this.playerInventory = playerInv;

        // Player inventory
        for (int i1 = 0; i1 < 3; ++i1) {
            for (int k1 = 0; k1 < 9; ++k1) {
                this.addSlot(new Slot(playerInv, k1 + i1 * 9 + 9, this.inventorySlots.size(), 0));
            }
        }
        // Hotbar
        for (int j1 = 0; j1 < 9; ++j1) {
            this.addSlot(new Slot(playerInv, j1, this.inventorySlots.size(), 0));
        }
    }

    @Override
    public boolean canInteractWith(PlayerEntity playerIn) {
        return true;
    }
}
