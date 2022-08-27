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
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Vector3;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.ItemShape;
import com.tridevmc.architecture.common.block.entity.ShapeBlockEntity;
import com.tridevmc.architecture.common.utils.DumbBlockReader;
import com.tridevmc.architecture.legacy.base.BaseOrientation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
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
        SHAPE_BLOCKS.put(architectureShape, this);
    }

    public EnumShape getArchitectureShape() {
        return this.architectureShape;
    }

    public static float acBlockStrength(BlockState state, Player player, BlockAndTintGetter world, BlockPos pos) {
        float hardness = 2F;
        try {
            hardness = state.getBlockHardness(new DumbBlockReader(state), pos);
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
        var block = state.getBlock();
        if (state.canHarvestBlock(new DumbBlockReader(state), BlockPos.ZERO, player))
            return true;
        var stack = player.getUseItem();
        var tool = block.getHarvestTool(state);
        if (stack.isEmpty() || tool == null)
            return player.func_234569_d_(state);
        int toolLevel = stack.getItem().getHarvestLevel(stack, tool, player, state);
        if (toolLevel < 0)
            return player.func_234569_d_(state);
        else
            return toolLevel >= block.getHarvestLevel(state);
    }

    @Override
    protected void defineProperties() {
        super.defineProperties();
        this.addProperty(LIGHT);
    }

    @Override
    public int getNumSubtypes() {
        return 16;
    }

    @Override
    public IOrientationHandler getOrientationHandler() {
        return BaseOrientation.orient24WaysByTE;
    }

    @Override
    @Nonnull
    protected VoxelShape getGlobalCollisionBoxes(BlockAndTintGetter world, BlockPos pos,
                                                 BlockState state, Entity entity) {
        ShapeBlockEntity te = this.getTileEntity(world, pos);
        if (te != null) {
            Trans3 t = te.localToGlobalTransformation();
            return this.getCollisionBoxes(te, world, pos, state, t, entity);
        }
        return Shapes.empty();
    }

    @Override
    @Nonnull
    protected VoxelShape getLocalCollisionBoxes(BlockAndTintGetter world, BlockPos pos,
                                                         BlockState state, Entity entity) {
        ShapeBlockEntity te = this.getTileEntity(world, pos);
        if (te != null) {
            Trans3 t = te.localToGlobalTransformation(Vector3.zero);
            return this.getCollisionBoxes(te, world, pos, state, t, entity);
        }
        return Shapes.empty();
    }

    @Override
    @Nonnull
    protected VoxelShape getLocalBounds(BlockAndTintGetter world, BlockPos pos,
                                        BlockState state, Entity entity) {
        ShapeBlockEntity te = this.getTileEntity(world, pos);
        if (te != null) {
            Trans3 t = te.localToGlobalTransformation(Vector3.zero);
            return this.getArchitectureShape().behaviour.getBounds(te, world, pos, state, entity, t);
        }
        return Shapes.empty();
    }

    @Nonnull
    protected VoxelShape getCollisionBoxes(ShapeBlockEntity te, BlockAndTintGetter world, BlockPos pos, BlockState state, Trans3 t, Entity entity) {
        return this.getArchitectureShape().behaviour.getCollisionBoxCached(te, world, pos, state, entity, t);
    }

    //@Override
    //protected NonNullList<ItemStack> getDropsFromTileEntity(LootContext.Builder context, BlockState state) {
    //    NonNullList<ItemStack> result = NonNullList.create();
    //    TileEntity te = context.get(LootParameters.BLOCK_ENTITY);
    //    if (te instanceof TileShape) {
    //        TileShape ste = (TileShape) te;
    //        ItemStack stack = ste.shape.kind.newStack(ste.shape, ste.baseBlockState, 1);
    //        result.add(stack);
    //        if (ste.secondaryBlockState != null) {
    //            stack = ste.shape.kind.newSecondaryMaterialStack(ste.secondaryBlockState);
    //            result.add(stack);
    //        }
    //    }
    //    return result;
    //}

    @Override
    public ItemStack getPickBlock(BlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, Player player) {
        ShapeBlockEntity te = ShapeBlockEntity.get(world, pos);
        if (te != null)
            return ItemShape.createStack(this.getArchitectureShape(), te.getBaseBlockState(), 1);
        else
            return ItemStack.EMPTY;
    }

    public BlockState getBaseBlockState(BlockAndTintGetter world, BlockPos pos) {
        ShapeBlockEntity te = this.getTileEntity(world, pos);
        if (te != null)
            return te.getBaseBlockState();
        return null;
    }

    private ShapeBlockEntity getTileEntity(BlockAndTintGetter world, BlockPos pos) {
        return (ShapeBlockEntity) world.getBlockEntity(pos);
    }

    @Override
    public float getPlayerRelativeBlockHardness(BlockState state, Player player, IBlockReader world, BlockPos pos) {
        float result = 1.0F;
        BlockState base = this.getBaseBlockState(world, pos);
        if (base != null) {
            result = acBlockStrength(base, player, world, pos);
        }
        return result;
    }

    @Override
    public BlockState getParticleState(BlockAndTintGetter world, BlockPos pos) {
        BlockState base = this.getBaseBlockState(world, pos);
        if (base != null)
            return base;
        else
            return this.defaultBlockState();
    }

    @Override
    public InteractionResult use(BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getUseItem();
        if (!stack.isEmpty()) {
            ShapeBlockEntity te = ShapeBlockEntity.get(world, pos);
            if (te != null)
                return te.applySecondaryMaterial(stack, player) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
        return InteractionResult.FAIL;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public float getAmbientOcclusionLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return 0.8f;
    }

    @Override
    public int getLightValue(BlockState state, IBlockReader world, BlockPos pos) {
        return state.getValue(LIGHT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ShapeBlockEntity(pos, state, this);
    }
}
