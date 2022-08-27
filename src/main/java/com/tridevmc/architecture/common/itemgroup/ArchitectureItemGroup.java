package com.tridevmc.architecture.common.itemgroup;


import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class ArchitectureItemGroup extends CreativeModeTab {

    private final Supplier<ItemStack> iconSupplier;

    public ArchitectureItemGroup(String groupName, Supplier<ItemStack> iconSupplier) {
        super(groupName);
        this.iconSupplier = iconSupplier;
    }

    @Override
    public ItemStack makeIcon() {
        return this.iconSupplier.get();
    }
}
