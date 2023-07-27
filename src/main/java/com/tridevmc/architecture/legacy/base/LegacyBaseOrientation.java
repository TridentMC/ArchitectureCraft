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

import com.tridevmc.architecture.core.ArchitectureLog;
import com.tridevmc.architecture.legacy.common.block.LegacyBlockArchitecture;
import com.tridevmc.architecture.legacy.common.block.entity.LegacyShapeBlockEntity;
import com.tridevmc.architecture.legacy.math.LegacyTrans3;
import com.tridevmc.architecture.legacy.math.LegacyVector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Arrays;
import java.util.Collection;

@Deprecated
public class LegacyBaseOrientation {

    public static boolean debugPlacement = false;

    public static LegacyBlockArchitecture.IOrientationHandler orient4WaysByState = new Orient4WaysByState();
    public static LegacyBlockArchitecture.IOrientationHandler orient24WaysByTE = new Orient24WaysByTE();

    //------------------------------------------------------------------------------------------------

    public static class PropertyTurn extends EnumProperty<Direction> {

        protected static Direction[] values = {
                Direction.WEST, Direction.EAST, Direction.SOUTH, Direction.NORTH
        };

        protected static Collection valueList = Arrays.asList(values);

        public PropertyTurn(String name) {
            super(name, Direction.class, valueList);
        }

    }

    //------------------------------------------------------------------------------------------------

    public static class Orient4WaysByState implements LegacyBlockArchitecture.IOrientationHandler {

        public static Property<Direction> FACING = new PropertyTurn("facing");

        @Override
        public void defineProperties(LegacyBlockArchitecture block) {
            block.addProperty(FACING);
        }

        @Override
        public BlockState onBlockPlaced(Block block, Level level, BlockPos pos, Direction side,
                                        double hitX, double hitY, double hitZ, BlockState baseState, LivingEntity placer) {
            var dir = placer.getDirection();
            if (debugPlacement)
                ArchitectureLog.info("BaseOrientation.Orient4WaysByState: Placing block with FACING = %s\n", dir);
            return baseState.setValue(FACING, dir);
        }

        @Override
        public LegacyTrans3 localToGlobalTransformation(BlockGetter level, BlockPos pos, BlockState state, LegacyVector3 origin) {
            if (1 == 1) {
                return LegacyTrans3.ident;
            }
            var f = state.getValue(FACING);
            int i = switch (f) {
                case NORTH -> 0;
                case WEST -> 1;
                case SOUTH -> 2;
                case EAST -> 3;
                default -> 0;
            };
            return new LegacyTrans3(origin).turnAround(LegacyVector3.BLOCK_CENTER, i);
        }

    }

//------------------------------------------------------------------------------------------------

    public static class Orient24WaysByTE extends LegacyBlockArchitecture.Orient1Way {

        @Override
        public LegacyTrans3 localToGlobalTransformation(BlockGetter level, BlockPos pos, BlockState state, LegacyVector3 origin) {
            var te = level.getBlockEntity(pos);
            if (te instanceof LegacyShapeBlockEntity bte) {
                return LegacyTrans3.sideTurn(origin, bte.getSide(), bte.getTurn());
            } else
                return super.localToGlobalTransformation(level, pos, state, origin);
        }

    }

}
