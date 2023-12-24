package com.tridevmc.architecture.common.ui;


import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;


import javax.annotation.Nullable;

public interface IElementProvider<T extends AbstractContainerMenu> extends MenuProvider {

    @OnlyIn(Dist.CLIENT)
    Screen createScreen(T container, Player player);

    @Nullable
    @Override
    default AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player) {
        return this.createMenu(new CreateMenuContext(windowId, player, playerInventory));
    }

    @Nullable
    AbstractContainerMenu createMenu(CreateMenuContext context);

    @Override
    default Component getDisplayName() {
        return Component.empty();
    }
}
