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

package com.tridevmc.architecture.common.item;

import com.tridevmc.architecture.common.block.state.BlockStateShape;
import com.tridevmc.architecture.common.shape.orientation.EnumTurn;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyAxis;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFacing;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFlip;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyOffset;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertySpin;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyTurn;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.state.BlockState;


public class ItemHammer extends Item {

    public ItemHammer(Identifier id) {
        super(new Item.Properties().stacksTo(1).setId(ResourceKey.create(Registries.ITEM, id)));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        var world = context.getLevel();
        var pos = context.getClickedPos();
        var player = context.getPlayer();
        var side = context.getClickedFace();
        if (player == null || !world.mayInteract(player, pos)
                || !player.mayUseItemAt(pos.relative(side), side, context.getItemInHand())) {
            return InteractionResult.PASS;
        }
        if (!(world.getBlockState(pos) instanceof BlockStateShape state)) {
            return InteractionResult.PASS;
        }
        var rotated = this.rotate(state, player.isSecondaryUseActive());
        if (rotated == state) {
            return InteractionResult.PASS;
        }
        if (!world.isClientSide() && !world.setBlockAndUpdate(pos, rotated)) {
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }

    private BlockState rotate(BlockStateShape state, boolean changeFacing) {
        var facing = ShapeOrientationPropertyFacing.INSTANCE;
        var spin = ShapeOrientationPropertySpin.INSTANCE;
        if (state.hasProperty(facing) && state.hasProperty(spin)) {
            for (var property : state.getProperties()) {
                if (property != facing && property != spin && property != ShapeOrientationPropertyOffset.INSTANCE) {
                    return state;
                }
            }
            return changeFacing ? state.cycle(facing) : state.cycle(spin);
        }
        if (state.hasProperty(facing)) {
            var turn = ShapeOrientationPropertyTurn.INSTANCE;
            var flip = ShapeOrientationPropertyFlip.INSTANCE;
            if (state.hasProperty(turn) && state.hasProperty(flip) && state.getProperties().size() == 3) {
                if (changeFacing) {
                    return state.cycle(facing);
                }
                var rotated = state.cycle(turn);
                return rotated.getValue(turn) == EnumTurn.UNTURNED ? rotated.cycle(flip) : rotated;
            }
            if (state.getProperties().size() == 2) {
                if (state.hasProperty(turn)) {
                    return changeFacing ? state.cycle(facing) : state.cycle(turn);
                }
                if (state.hasProperty(flip)) {
                    return changeFacing ? state.cycle(facing) : state.cycle(flip);
                }
            }
        }
        if (state.getProperties().size() == 1) {
            if (state.hasProperty(ShapeOrientationPropertyAxis.INSTANCE)) {
                return state.cycle(ShapeOrientationPropertyAxis.INSTANCE);
            }
            if (state.hasProperty(facing)) {
                return state.cycle(facing);
            }
        }
        return state;
    }

}
