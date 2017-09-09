//------------------------------------------------------------------------------
//
//   ArchitectureCraft - Chisel
//
//------------------------------------------------------------------------------

package com.elytradev.architecture.common;

import com.elytradev.architecture.base.BaseBlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ChiselItem extends Item {

    public ChiselItem() {
        setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof ShapeTE) {
            if (!world.isRemote) {
                ShapeTE ste = (ShapeTE) te;
                ste.onChiselUse(player, side, hitX, hitY, hitZ);
            }
            return EnumActionResult.SUCCESS;
        }
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block == Blocks.GLASS || block == Blocks.GLASS_PANE
                || block == Blocks.GLOWSTONE || block == Blocks.ICE) {
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 0x3);
            if (!world.isRemote) {
                dropBlockAsItem(world, pos, state);
                world.playEvent(2001, pos, Block.getStateId(Blocks.STONE.getDefaultState())); // block breaking sound and particles
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.FAIL;
    }

    void dropBlockAsItem(World world, BlockPos pos, IBlockState state) {
        ItemStack stack = BaseBlockUtils.blockStackWithState(state, 1);
        Block.spawnAsEntity(world, pos, stack);
    }

}
