/*
 * MIT License
 *
 * Copyright (c) 2017 Benjamin K
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.tridevmc.architecture.legacy.base;

import com.tridevmc.architecture.common.ArchitectureLog;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Vector3;
import com.tridevmc.architecture.common.tile.TileArchitecture;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.IProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Collection;

public class BaseOrientation {

    public static boolean debugPlacement = false;

    public static BlockArchitecture.IOrientationHandler orient4WaysByState = new Orient4WaysByState();
    public static BlockArchitecture.IOrientationHandler orient24WaysByTE = new Orient24WaysByTE();

    //------------------------------------------------------------------------------------------------

    public static class PropertyTurn extends EnumProperty<EnumFacing> {

        protected static EnumFacing[] values = {
                EnumFacing.WEST, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.NORTH
        };

        protected static Collection valueList = Arrays.asList(values);

        public PropertyTurn(String name) {
            super(name, EnumFacing.class, valueList);
        }

    }

    //------------------------------------------------------------------------------------------------

    public static class Orient4WaysByState implements BlockArchitecture.IOrientationHandler {

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
                ArchitectureLog.info("BaseOrientation.Orient4WaysByState: Placing block with FACING = %s\n", dir);
            return baseState.with(FACING, dir);
        }

        @Override
        public Trans3 localToGlobalTransformation(IWorldReader world, BlockPos pos, IBlockState state, Vector3 origin) {
            EnumFacing f = (EnumFacing) state.get(FACING);
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
        public Trans3 localToGlobalTransformation(IWorldReader world, BlockPos pos, IBlockState state, Vector3 origin) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileArchitecture) {
                TileArchitecture bte = (TileArchitecture) te;
                return Trans3.sideTurn(origin, bte.getSide(), bte.getTurn());
            } else
                return super.localToGlobalTransformation(world, pos, state, origin);
        }

    }

}
