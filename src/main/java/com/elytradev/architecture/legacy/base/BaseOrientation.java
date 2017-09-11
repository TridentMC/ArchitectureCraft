//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.8 - Block orientation handlers
//
//------------------------------------------------------------------------------------------------

package com.elytradev.architecture.legacy.base;

import com.elytradev.architecture.common.block.BlockArchitecture;
import com.elytradev.architecture.common.tile.TileArchitecture;
import com.elytradev.architecture.common.block.BlockArchitecture.IOrientationHandler;
import com.elytradev.architecture.legacy.common.helpers.Trans3;
import com.elytradev.architecture.legacy.common.helpers.Vector3;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Collection;

public class BaseOrientation {

    public static boolean debugPlacement = false;

    public static IOrientationHandler orient4WaysByState = new Orient4WaysByState();
    public static IOrientationHandler orient24WaysByTE = new Orient24WaysByTE();

    //------------------------------------------------------------------------------------------------

    public static class PropertyTurn extends PropertyEnum<EnumFacing> {

        protected static EnumFacing[] values = {
                EnumFacing.WEST, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.NORTH
        };

        protected static Collection valueList = Arrays.asList(values);

        public PropertyTurn(String name) {
            super(name, EnumFacing.class, valueList);
        }

    }

    //------------------------------------------------------------------------------------------------

    public static class Orient4WaysByState implements IOrientationHandler {

        public static IProperty FACING = new PropertyTurn("facing");

        @Override
        public void defineProperties(BlockArchitecture block) {
            block.addProperty(FACING);
        }

        @Override
        public IBlockState onBlockPlaced(Block block, World world, BlockPos pos, EnumFacing side,
                                         float hitX, float hitY, float hitZ, IBlockState baseState, EntityLivingBase placer) {
            EnumFacing dir = placer.getHorizontalFacing();
            if (debugPlacement)
                System.out.printf("BaseOrientation.Orient4WaysByState: Placing block with FACING = %s\n", dir);
            return baseState.withProperty(FACING, dir);
        }

        @Override
        public Trans3 localToGlobalTransformation(IBlockAccess world, BlockPos pos, IBlockState state, Vector3 origin) {
            EnumFacing f = (EnumFacing) state.getValue(FACING);
            int i;
            switch (f) {
                case NORTH:
                    i = 0;
                    break;
                case WEST:
                    i = 1;
                    break;
                case SOUTH:
                    i = 2;
                    break;
                case EAST:
                    i = 3;
                    break;
                default:
                    i = 0;
            }
            return new Trans3(origin).turn(i);
        }

    }

//------------------------------------------------------------------------------------------------

    public static class Orient24WaysByTE extends BlockArchitecture.Orient1Way {

        @Override
        public Trans3 localToGlobalTransformation(IBlockAccess world, BlockPos pos, IBlockState state, Vector3 origin) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileArchitecture) {
                TileArchitecture bte = (TileArchitecture) te;
                return Trans3.sideTurn(origin, bte.side, bte.turn);
            } else
                return super.localToGlobalTransformation(world, pos, state, origin);
        }

    }

}
