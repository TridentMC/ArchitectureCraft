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

import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Vector3;
import com.tridevmc.architecture.common.tile.TileShape;
import com.tridevmc.architecture.legacy.base.BaseOrientation;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IntegerProperty;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.common.property.IExtendedBlockState;

import java.util.ArrayList;
import java.util.List;

public class BlockShape extends BlockArchitecture<TileShape> {

    public static IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);
    protected AxisAlignedBB boxHit;

    public BlockShape() {
        super(Material.GROUND, TileShape.class);
    }

    @Override
    public boolean isToolEffective(IBlockState state, ToolType tool) {
        if (state instanceof IExtendedBlockState) {
            IBlockReader world = ((IExtendedBlockState) state).getValue(BLOCKACCESS_PROP);
            BlockPos pos = ((IExtendedBlockState) state).getValue(POS_PROP);

            if (world != null && pos != null) {
                TileShape shape = TileShape.get(world, pos);
                if (shape != null) {
                    return shape.baseBlockState.getBlock().isToolEffective(shape.baseBlockState, tool);
                }
            }
        }

        return super.isToolEffective(state, tool);
    }

    @Override
    public int getHarvestLevel(IBlockState state) {
        if (state instanceof IExtendedBlockState) {
            IBlockReader world = ((IExtendedBlockState) state).getValue(BLOCKACCESS_PROP);
            BlockPos pos = ((IExtendedBlockState) state).getValue(POS_PROP);

            if (world != null && pos != null) {
                TileShape shape = TileShape.get(world, pos);
                if (shape != null) {
                    return shape.baseBlockState.getBlock().getHarvestLevel(shape.baseBlockState);
                }
            }
        }

        return super.getHarvestLevel(state);
    }

    @Override
    public float getBlockHardness(IBlockState blockState, IBlockReader worldIn, BlockPos pos) {
        TileShape shape = TileShape.get(worldIn, pos);
        if (shape != null && shape.baseBlockState != null) {
            return shape.baseBlockState.getBlockHardness(worldIn, pos);
        }

        return super.getBlockHardness(blockState, worldIn, pos);
    }

    public static float acBlockStrength(IBlockState state, EntityPlayer player, IBlockReader world, BlockPos pos) {
        float hardness = 2F;
        try {
            hardness = state.getBlockHardness(world, pos);
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

    public static boolean acCanHarvestBlock(IBlockState state, EntityPlayer player) {
        Block block = state.getBlock();
        if (block.getMaterial(state).isToolNotRequired())
            return true;
        ItemStack stack = player.inventory.getCurrentItem();
        //state = state.getBlock().getActualState(state, world, pos);
        ToolType tool = block.getHarvestTool(state);
        if (stack == null || tool == null)
            return player.canHarvestBlock(state);
        int toolLevel = stack.getItem().getHarvestLevel(stack, tool, player, state);
        if (toolLevel < 0)
            return player.canHarvestBlock(state);
        else
            return toolLevel >= block.getHarvestLevel(state);
    }

    @Override
    protected void defineProperties() {
        super.defineProperties();
        addProperty(LIGHT);
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
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    /*@Override TODO: Not sure if this method is gone, or just got a new name.
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }*/

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockReader worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public VoxelShape getShape(IBlockState state, IBlockReader world, BlockPos pos) {
        if (boxHit != null) {
            TileShape te = TileShape.get(world, pos);
            if (te != null && te.shape.kind.highlightZones())
                return VoxelShapes.create(boxHit);
        }
        AxisAlignedBB box = getLocalBounds(world, pos, state, null);
        if (box != null)
            return VoxelShapes.create(boxHit);
        else
            return super.getShape(state, world, pos);
    }

    @Override
    protected List<AxisAlignedBB> getGlobalCollisionBoxes(IBlockReader world, BlockPos pos,
                                                          IBlockState state, Entity entity) {
        TileShape te = getTileEntity(world, pos);
        if (te != null) {
            Trans3 t = te.localToGlobalTransformation();
            return getCollisionBoxes(te, world, pos, state, t, entity);
        }
        return new ArrayList<AxisAlignedBB>();
    }

    @Override
    protected List<AxisAlignedBB> getLocalCollisionBoxes(IBlockReader world, BlockPos pos,
                                                         IBlockState state, Entity entity) {
        TileShape te = getTileEntity(world, pos);
        if (te != null) {
            Trans3 t = te.localToGlobalTransformation(Vector3.zero);
            return getCollisionBoxes(te, world, pos, state, t, entity);
        }
        return new ArrayList<AxisAlignedBB>();
    }

    @Override
    protected AxisAlignedBB getLocalBounds(IBlockReader world, BlockPos pos,
                                           IBlockState state, Entity entity) {
        TileShape te = getTileEntity(world, pos);
        if (te != null) {
            Trans3 t = te.localToGlobalTransformation(Vector3.blockCenter);
            return te.shape.kind.getBounds(te, world, pos, state, entity, t);
        }
        return null; // Causes getBoundingBox to fall back on super implementation
    }

    protected List<AxisAlignedBB> getCollisionBoxes(TileShape te,
                                                    IBlockReader world, BlockPos pos, IBlockState state, Trans3 t, Entity entity) {
        List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
        te.shape.kind.addCollisionBoxesToList(te, world, pos, state, entity, t, list);
        return list;
    }


    @Override
    protected NonNullList<ItemStack> getDropsFromTileEntity(World world, BlockPos pos, IBlockState state, TileEntity te, int fortune) {
        NonNullList<ItemStack> result = NonNullList.create();
        if (te instanceof TileShape) {
            TileShape ste = (TileShape) te;
            ItemStack stack = ste.shape.kind.newStack(ste.shape, ste.baseBlockState, 1);
            result.add(stack);
            if (ste.secondaryBlockState != null) {
                stack = ste.shape.kind.newSecondaryMaterialStack(ste.secondaryBlockState);
                result.add(stack);
            }
        }
        return result;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, IBlockReader world, BlockPos pos, EntityPlayer player) {
        TileShape te = TileShape.get(world, pos);
        if (te != null)
            return te.newItemStack(1);
        else
            return ItemStack.EMPTY;
    }

    public IBlockState getBaseBlockState(IBlockReader world, BlockPos pos) {
        TileShape te = getTileEntity(world, pos);
        if (te != null)
            return te.baseBlockState;
        return null;
    }


    @Override
    public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, IBlockReader world, BlockPos pos) {
        float result = 1.0F;
        IBlockState base = getBaseBlockState(world, pos);
        if (base != null) {
            //System.out.printf("ShapeBlock.getPlayerRelativeBlockHardness: base = %s\n", base);
            result = acBlockStrength(base, player, world, pos);
        }
        return result;
    }

    @Override
    public IBlockState getParticleState(IBlockReader world, BlockPos pos) {
        IBlockState base = getBaseBlockState(world, pos);
        if (base != null)
            return base;
        else
            return getDefaultState();
    }

    @Override
    public boolean onBlockActivated(IBlockState state, World world, BlockPos pos, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.inventory.getCurrentItem();
        if (stack != null) {
            TileShape te = TileShape.get(world, pos);
            if (te != null)
                return te.applySecondaryMaterial(stack, player);
        }
        return false;
    }

    @Override
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public float getAmbientOcclusionLightValue(IBlockState state) {
        return 0.8f;
    }

    @Override
    public int getLightValue(IBlockState state) {
        return state.get(LIGHT);
    }

}
