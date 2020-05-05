package com.tridevmc.architecture.common.ui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public interface IElementProvider<T extends Container> extends INamedContainerProvider {

    @OnlyIn(Dist.CLIENT)
    Screen createScreen(T container, PlayerEntity player);

    @Nullable
    @Override
    default Container createMenu(int windowId, PlayerInventory playerInventory, PlayerEntity player) {
        return this.createMenu(new CreateMenuContext(windowId, player, playerInventory));
    }

    @Nullable
    Container createMenu(CreateMenuContext context);

    @Override
    default ITextComponent getDisplayName() {
        return new StringTextComponent("");
    }
}
