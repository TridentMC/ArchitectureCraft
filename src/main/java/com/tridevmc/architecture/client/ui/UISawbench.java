package com.tridevmc.architecture.client.ui;

import com.tridevmc.architecture.common.tile.ContainerSawbench;
import com.tridevmc.compound.ui.Rect2D;
import com.tridevmc.compound.ui.container.CompoundUIContainer;
import com.tridevmc.compound.ui.element.ElementBox;
import com.tridevmc.compound.ui.layout.LayoutCentered;
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
        this.addElement(new ElementBox(new Rect2D(0,0, 256, 288), boxLayout));
    }
}
