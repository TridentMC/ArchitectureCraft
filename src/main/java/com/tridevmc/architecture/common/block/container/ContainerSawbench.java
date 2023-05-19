package com.tridevmc.architecture.common.block.container;

import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.compound.ui.container.CompoundContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ContainerSawbench extends CompoundContainerMenu {

    public final Inventory playerInventory;

    public ContainerSawbench(Inventory playerInv, int id) {
        super(ArchitectureMod.CONTENT.universalMenuType, id);
        this.playerInventory = playerInv;

        // Player inventory
        for (int i1 = 0; i1 < 3; ++i1) {
            for (int k1 = 0; k1 < 9; ++k1) {
                this.addSlot(new Slot(playerInv, k1 + i1 * 9 + 9, this.playerInventory.getContainerSize(), 0));
            }
        }
        // Hotbar
        for (int j1 = 0; j1 < 9; ++j1) {
            this.addSlot(new Slot(playerInv, j1, this.playerInventory.getContainerSize(), 0));
        }
    }

    @Override
    @NotNull
    public ItemStack quickMoveStack(@NotNull Player player, int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

}
