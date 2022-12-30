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

package com.tridevmc.architecture.common.block;

import com.google.common.collect.Maps;
import com.tridevmc.architecture.client.debug.ArchitectureDebugEventListeners;
import com.tridevmc.architecture.common.block.entity.ShapeBlockEntity;
import com.tridevmc.architecture.legacy.math.LegacyTrans3;
import com.tridevmc.architecture.legacy.math.LegacyVector3;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.ItemShape;
import com.tridevmc.architecture.common.shape.behaviour.ShapeBehaviourModel;
import com.tridevmc.architecture.common.utils.DumbBlockReader;
import com.tridevmc.architecture.legacy.base.BaseOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class BlockShape extends BlockArchitecture {

    private static final Map<EnumShape, BlockShape> SHAPE_BLOCKS = Maps.newHashMap();
    public static IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);
    private final EnumShape architectureShape;

    public BlockShape(EnumShape architectureShape) {
        super(Material.DIRT);
        this.architectureShape = architectureShape;
        if(this.architectureShape.behaviour instanceof ShapeBehaviourModel sbm)
            this.setModelAndTextures(sbm.getModelName());
        SHAPE_BLOCKS.put(architectureShape, this);
    }

    public EnumShape getArchitectureShape() {
        return this.architectureShape;
    }

    public static float acBlockStrength(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        float hardness = 2F;
        try {
            hardness = state.getDestroySpeed(new DumbBlockReader(state), pos);
        } catch (IllegalArgumentException e) {
            // Catch exceptions from mods that check their hardness based on the blocks in the world.
        }
        if (hardness < 0.0F)
            return 0.0F;
        float strength = player.getDigSpeed(state, pos) / hardness;
        if (!acCanHarvestBlock(state, player))
            return strength / 100F;
        else
            return strength / 30F;
    }

    public static boolean acCanHarvestBlock(BlockState state, Player player) {
        // TODO: Commented out for now.
        //var block = state.getBlock();
        //if (state.canHarvestBlock(new DumbBlockReader(state), BlockPos.ZERO, player))
        //    return true;
        //var stack = player.getUseItem();
        //var tool = block.getHarvestTool(state);
        //if (stack.isEmpty() || tool == null)
        //    return player.func_234569_d_(state);
        //int toolLevel = stack.getItem().getHarvestLevel(stack, tool, player, state);
        //if (toolLevel < 0)
        //    return player.func_234569_d_(state);
        //else
        //    return toolLevel >= block.getHarvestLevel(state);
        return true;
    }

    @Override
    protected void defineProperties() {
        super.defineProperties();
        this.addProperty(LIGHT);
    }

    @Override
    public IOrientationHandler getOrientationHandler() {
        return BaseOrientation.orient24WaysByTE;
    }

    @Override
    @Nonnull
    protected VoxelShape getLocalBounds(BlockGetter level, BlockPos pos,
                                        BlockState state, Entity entity) {
        ShapeBlockEntity te = this.getTileEntity(level, pos);
        if (te != null) {
            LegacyTrans3 t = te.localToGlobalTransformation(LegacyVector3.ZERO);
            return this.getArchitectureShape().behaviour.getBounds(te, level, pos, state, entity, t);
        }
        return Shapes.empty();
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
        ShapeBlockEntity te = ShapeBlockEntity.get(level, pos);
        if (te != null)
            return ItemShape.createStack(this.getArchitectureShape(), te.getBaseBlockState(), 1);
        else
            return ItemStack.EMPTY;
    }

    public BlockState getBaseBlockState(BlockGetter level, BlockPos pos) {
        ShapeBlockEntity te = this.getTileEntity(level, pos);
        if (te != null)
            return te.getBaseBlockState();
        return null;
    }

    private ShapeBlockEntity getTileEntity(BlockGetter level, BlockPos pos) {
        return (ShapeBlockEntity) level.getBlockEntity(pos);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        float result = 1.0F;
        BlockState base = this.getBaseBlockState(level, pos);
        if (base != null) {
            result = acBlockStrength(base, player, level, pos);
        }
        return result;
    }

    @Override
    public BlockState getParticleState(BlockAndTintGetter level, BlockPos pos) {
        BlockState base = this.getBaseBlockState(level, pos);
        if (base != null)
            return base;
        else
            return this.defaultBlockState();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getUseItem();
        if (!stack.isEmpty()) {
            ShapeBlockEntity te = ShapeBlockEntity.get(level, pos);
            if (te != null)
                return te.applySecondaryMaterial(stack, player) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        return ArchitectureDebugEventListeners.onVoxelizedBlockClicked(level, pos, player, hit, this.getArchitectureShape());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 0.8F;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(LIGHT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShapeBlockEntity(pos, state, this);
    }

    @Override
    public boolean hasBlockEntity(BlockState state) {
        return true;
    }

    @Override
    public int getTransIdentity(BlockState state, BlockGetter level, BlockPos pos, LegacyVector3 origin) {
        var shapeBE = ShapeBlockEntity.get(level, pos);
        var identity = super.getTransIdentity(state, level, pos, origin);
        if (shapeBE != null) {
            identity = 31 * identity + (int) shapeBE.getOffsetX();
            identity = 31 * identity + shapeBE.getSide();
            identity = 31 * identity + shapeBE.getTurn();
        }
        return identity;
    }
}
