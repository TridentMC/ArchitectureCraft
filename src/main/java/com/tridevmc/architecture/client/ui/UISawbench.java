package com.tridevmc.architecture.client.ui;

import com.tridevmc.architecture.common.block.container.ContainerSawbench;
import com.tridevmc.compound.ui.scope.ICompositionScope;
import com.tridevmc.compound.ui.screen.ComposedUIContainer;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

public class UISawbench extends ComposedUIContainer<ContainerSawbench> {
    private final SawbenchPanel panel;

    public UISawbench(ContainerSawbench menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.panel = new SawbenchPanel(menu, shape -> {
            var client = Minecraft.getInstance();
            if (client.gameMode != null && client.player != null
                    && menu.clickMenuButton(client.player, shape.ordinal())) {
                client.gameMode.handleInventoryButtonClick(menu.containerId, shape.ordinal());
            }
        });
    }

    public UISawbench(ContainerSawbench menu, Player player) {
        this(menu, player.getInventory(), Component.translatable("architecturecraft.gui.sawbench.title"));
    }

    @Override
    protected void compose(ICompositionScope scope) {
        this.panel.compose(scope);
    }

    @Override
    public void containerTick() {
        super.containerTick();
        this.panel.tick();
    }
}
