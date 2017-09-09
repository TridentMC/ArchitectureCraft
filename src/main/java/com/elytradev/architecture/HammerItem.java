//------------------------------------------------------------------------------
//
//   ArchitectureCraft - Hammer
//
//------------------------------------------------------------------------------

package com.elytradev.architecture;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HammerItem extends Item {

    public HammerItem() {
        setMaxStackSize(1);
    }

    @Override
    public CreativeTabs getCreativeTab() {
        return CreativeTabs.TOOLS;
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        System.out.printf("HammerItem.onItemUse\n");
        ShapeTE te = ShapeTE.get(world, pos);
        if (te != null) {
            System.out.printf("HammerItem.onItemUse: te = %s\n", te);
            te.onHammerUse(player, side, hitX, hitY, hitZ);
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }

}
