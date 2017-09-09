//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.8 - Generic Item
//
//------------------------------------------------------------------------------------------------

package com.elytradev.architecture;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class BaseItem extends Item implements BaseMod.IItem {

    public String[] getTextureNames() {
        return null;
    }

    public BaseMod.ModelSpec getModelSpec(ItemStack stack) {
        return null;
    }

    public int getNumSubtypes() {
        return 1;
    }

    @Override
    public boolean getHasSubtypes() {
        return getNumSubtypes() > 1;
    }

}
