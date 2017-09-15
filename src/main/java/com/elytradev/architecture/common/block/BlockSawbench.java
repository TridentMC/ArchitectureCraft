//------------------------------------------------------------------------------
//
//   ArchitectureCraft - Sawbench Block
//
//------------------------------------------------------------------------------

package com.elytradev.architecture.common.block;

import com.elytradev.architecture.common.render.ModelSpec;
import com.elytradev.architecture.common.block.BlockArchitecture;
import com.elytradev.architecture.legacy.base.BaseOrientation;
import com.elytradev.architecture.legacy.common.ArchitectureCraft;
import com.elytradev.architecture.common.tile.TileSawbench;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockSawbench extends BlockArchitecture<TileSawbench> {

    static String model = "block/sawbench.smeg";
    static String[] textures = {"sawbench-wood", "sawbench-metal"};
    static ModelSpec modelSpec = new ModelSpec(model, textures);

    public BlockSawbench() {
        super(Material.WOOD, TileSawbench.class);
    }

    @Override
    public IOrientationHandler getOrientationHandler() {
        return BaseOrientation.orient4WaysByState;
    }

    @Override
    public String[] getTextureNames() {
        return textures;
    }

    @Override
    public ModelSpec getModelSpec(IBlockState state) {
        return modelSpec;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!player.isSneaking()) {
            if (!world.isRemote) {
                ArchitectureCraft.mod.openGuiSawbench(world, pos, player);
            }
            return true;
        } else
            return false;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileSawbench();
    }

}
