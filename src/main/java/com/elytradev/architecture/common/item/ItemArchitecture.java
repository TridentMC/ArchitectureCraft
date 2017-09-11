//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.8 - Generic Item
//
//------------------------------------------------------------------------------------------------

package com.elytradev.architecture.common.item;

import com.elytradev.architecture.common.render.ITextureConsumer;
import com.elytradev.architecture.common.render.ModelSpec;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemArchitecture extends Item implements ITextureConsumer {

    @Override
    public String[] getTextureNames() {
        return null;
    }

    public ModelSpec getModelSpec(ItemStack stack) {
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
