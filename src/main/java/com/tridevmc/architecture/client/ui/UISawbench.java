package com.tridevmc.architecture.client.ui;

import com.tridevmc.architecture.common.tile.ContainerSawbench;
import com.tridevmc.compound.ui.Rect2D;
import com.tridevmc.compound.ui.container.CompoundUIContainer;
import com.tridevmc.compound.ui.element.ElementBox;
import com.tridevmc.compound.ui.layout.*;
import net.minecraft.entity.player.PlayerEntity;

public class UISawbench extends CompoundUIContainer<ContainerSawbench> {
    private PlayerEntity player;

    public UISawbench(ContainerSawbench container, PlayerEntity player) {
        super(container);
        this.player = player;
    }

    @Override

    public void initElements() {
        // TODO: The size seems about right - no real elements in quite yet.
        LayoutCentered boxLayout = new LayoutCentered(true, true);
        ElementBox bg = new ElementBox(new Rect2D(0, 0, 256, 288), boxLayout);
        this.addElement(bg);

        LayoutGrid playerGrid = new LayoutGrid(new Rect2D(47, 204, 18 * 9, 18 * 3));
        ILayout playerLayout = new LayoutMulti(playerGrid, new LayoutRelative(bg));

        LayoutGrid hotbarGrid = new LayoutGrid(new Rect2D(47, 262, 18 * 9, 18));
        ILayout hotbarLayout = new LayoutMulti(hotbarGrid, new LayoutRelative(bg));

        for (int i = 0; i < this.getContainer().inventorySlots.size(); i++) {
            if (i < 27) {
                playerGrid.registerElement(this.addSlotElement(playerLayout, i));
            } else {
                hotbarGrid.registerElement(this.addSlotElement(hotbarLayout, i));
            }
        }
    }
}
