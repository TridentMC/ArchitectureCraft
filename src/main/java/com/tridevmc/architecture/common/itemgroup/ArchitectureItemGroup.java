package com.tridevmc.architecture.common.itemgroup;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

import java.util.function.Supplier;

public class ArchitectureItemGroup extends ItemGroup {

    private final Supplier<ItemStack> iconSupplier;

    public ArchitectureItemGroup(String groupName, Supplier<ItemStack> iconSupplier) {
        super(groupName);
        this.iconSupplier = iconSupplier;
    }

    @Override
    public ItemStack createIcon() {
        return iconSupplier.get();
    }
}
