package com.tridevmc.architecture.client.ui;

import com.tridevmc.architecture.common.block.container.ContainerSawbench;
import com.tridevmc.compound.ui.element.Grid;
import com.tridevmc.compound.ui.element.InventorySlot;
import com.tridevmc.compound.ui.element.Panel;
import com.tridevmc.compound.ui.element.Stack;
import com.tridevmc.compound.ui.layout.Alignment;
import com.tridevmc.compound.ui.scope.ICompositionScope;
import com.tridevmc.compound.ui.screen.ComposedUIContainer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class UISawbench extends ComposedUIContainer<ContainerSawbench> {
    private final Player player;

    public UISawbench(ContainerSawbench container, Player player) {
        super(container, player.getInventory(), Component.translatable("architecturecraft.gui.sawbench.title"));
        this.player = player;
    }

    @Override
    protected void compose(ICompositionScope scope) {
        // TODO: The size seems about right - no real elements in quite yet.
        scope.e(new Stack(), root -> {
            root.layout().fillMax().contentAlignment(Alignment.CENTER);
            root.e(new Panel(), panel -> {
                panel.layout().fixedSize(256, 288);
                panel.fillSlot(Panel.CONTENT_SLOT, content -> content.e(new Stack(), slots -> {
                    slots.layout().fillMax();
                    slots.e(new Grid(9), inventory -> {
                        inventory.layout().fixedSize(162, 54).margin(47, 204, 0, 0);
                        for (int i = 0; i < 27; i++) inventory.e(new InventorySlot(this.getMenu(), i));
                    });
                    slots.e(new Grid(9), hotbar -> {
                        hotbar.layout().fixedSize(162, 18).margin(47, 262, 0, 0);
                        for (int i = 27; i < 36; i++) hotbar.e(new InventorySlot(this.getMenu(), i));
                    });
                }));
            });
        });
    }
}
