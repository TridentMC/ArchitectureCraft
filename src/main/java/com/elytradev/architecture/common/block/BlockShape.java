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

package com.elytradev.architecture.common.block;

import com.elytradev.architecture.common.helpers.Trans3;
import com.elytradev.architecture.common.helpers.Vector3;
import com.elytradev.architecture.common.tile.TileShape;
import com.elytradev.architecture.legacy.base.BaseOrientation;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class BlockShape extends BlockArchitecture<TileShape> {

    public static IProperty<Integer> LIGHT = PropertyInteger.create("light", 0, 15);
    protected AxisAlignedBB boxHit;

    public BlockShape() {
        super(Material.GROUND, TileShape.class);
    }

    public static float acBlockStrength(IBlockState state, EntityPlayer player, World world, BlockPos pos) {
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
        String tool = block.getHarvestTool(state);
        if (stack == null || tool == null)
            return player.canHarvestBlock(state);
        int toolLevel = stack.getItem().getHarvestLevel(stack, tool, player, state);
        if (toolLevel < 0)
            return player.canHarvestBlock(state);
        else
            return toolLevel >= block.getHarvestLevel(state);
    }

    @Nullable
    @Override
    public String getHarvestTool(IBlockState state) {
        if (state instanceof IExtendedBlockState) {
            IBlockAccess world = ((IExtendedBlockState) state).getValue(BLOCKACCESS_PROP);
            BlockPos pos = ((IExtendedBlockState) state).getValue(POS_PROP);

            if (world != null && pos != null) {
                TileShape shape = TileShape.get(world, pos);
                if (shape != null) {
                    IBlockState baseBlockState = shape.getBaseBlockState();
                    return baseBlockState.getBlock().getHarvestTool(baseBlockState);
                }
            }
        }

        return super.getHarvestTool(state);
    }

    @Override
    public Material getMaterial(IBlockState state) {
        if (state instanceof IExtendedBlockState) {
            IBlockAccess world = ((IExtendedBlockState) state).getValue(BLOCKACCESS_PROP);
            BlockPos pos = ((IExtendedBlockState) state).getValue(POS_PROP);

            if (world != null && pos != null) {
                TileShape shape = TileShape.get(world, pos);
                if (shape != null) {
                    IBlockState baseBlockState = shape.getBaseBlockState();
                    return baseBlockState.getBlock().getMaterial(baseBlockState);
                }
            }
        }

        return super.getMaterial(state);
    }

    @Override
    public MapColor getMapColor(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileShape shape = TileShape.get(world, pos);
        if (shape != null) {
            IBlockState baseBlockState = shape.getBaseBlockState();
            return baseBlockState.getBlock().getMapColor(baseBlockState, world, pos);
        }

        return super.getMapColor(state, world, pos);
    }

    @Override
    public int getHarvestLevel(IBlockState state) {
        if (state instanceof IExtendedBlockState) {
            IBlockAccess world = ((IExtendedBlockState) state).getValue(BLOCKACCESS_PROP);
            BlockPos pos = ((IExtendedBlockState) state).getValue(POS_PROP);

            if (world != null && pos != null) {
                TileShape shape = TileShape.get(world, pos);
                if (shape != null) {
                    IBlockState baseBlockState = shape.getBaseBlockState();
                    return baseBlockState.getBlock().getHarvestLevel(baseBlockState);
                }
            }
        }

        return super.getHarvestLevel(state);
    }

    @Override
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
        TileShape shape = TileShape.get(worldIn, pos);
        if (shape != null && shape.hasBaseBlockState()) {
            IBlockState baseBlockState = shape.getBaseBlockState();
            return baseBlockState.getBlock().getBlockHardness(baseBlockState, worldIn, pos);
        }

        return super.getBlockHardness(blockState, worldIn, pos);
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
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        RayTraceResult result = null;
        double nearestDistance = 0;
        List<AxisAlignedBB> list = this.getGlobalCollisionBoxes(world, pos, state, null);
        if (list != null) {
            int n = list.size();
            for (int i = 0; i < n; i++) {
                AxisAlignedBB box = list.get(i);
                RayTraceResult mp = box.calculateIntercept(start, end);
                if (mp != null) {
                    mp.subHit = i;
                    double d = start.squareDistanceTo(mp.hitVec);
                    if (result == null || d < nearestDistance) {
                        result = mp;
                        nearestDistance = d;
                    }
                }
            }
        }
        if (result != null) {
            //setBlockBounds(list.get(result.subHit));
            int i = result.subHit;
            this.boxHit = list.get(i).offset(-pos.getX(), -pos.getY(), -pos.getZ());
            result = new RayTraceResult(result.hitVec, result.sideHit, pos);
            result.subHit = i;
        }
        return result;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (this.boxHit != null) {
            TileShape te = TileShape.get(world, pos);
            if (te != null && te.getShape().kind.highlightZones())
                return this.boxHit;
        }
        AxisAlignedBB box = this.getLocalBounds(world, pos, state, null);
        if (box != null)
            return box;
        else
            return super.getBoundingBox(state, world, pos);
    }

    @Override
    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos,
                                      AxisAlignedBB clip, List result, Entity entity, boolean b) {
        List<AxisAlignedBB> list = this.getGlobalCollisionBoxes(world, pos, state, entity);
        if (list != null)
            for (AxisAlignedBB box : list)
                if (clip.intersects(box))
                    result.add(box);
    }

    @Override
    protected List<AxisAlignedBB> getGlobalCollisionBoxes(IBlockAccess world, BlockPos pos,
                                                          IBlockState state, Entity entity) {
        TileShape te = this.getTileEntity(world, pos);
        if (te != null) {
            Trans3 t = te.localToGlobalTransformation();
            return this.getCollisionBoxes(te, world, pos, state, t, entity);
        }
        return new ArrayList<AxisAlignedBB>();
    }

    @Override
    protected List<AxisAlignedBB> getLocalCollisionBoxes(IBlockAccess world, BlockPos pos,
                                                         IBlockState state, Entity entity) {
        TileShape te = this.getTileEntity(world, pos);
        if (te != null) {
            Trans3 t = te.localToGlobalTransformation(Vector3.zero);
            return this.getCollisionBoxes(te, world, pos, state, t, entity);
        }
        return new ArrayList<AxisAlignedBB>();
    }

    @Override
    protected AxisAlignedBB getLocalBounds(IBlockAccess world, BlockPos pos,
                                           IBlockState state, Entity entity) {
        TileShape te = this.getTileEntity(world, pos);
        if (te != null) {
            Trans3 t = te.localToGlobalTransformation(Vector3.blockCenter);
            return te.getShape().kind.getBounds(te, world, pos, state, entity, t);
        }
        return null; // Causes getBoundingBox to fall back on super implementation
    }

    protected List<AxisAlignedBB> getCollisionBoxes(TileShape te,
                                                    IBlockAccess world, BlockPos pos, IBlockState state, Trans3 t, Entity entity) {
        List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
        te.getShape().kind.addCollisionBoxesToList(te, world, pos, state, entity, t, list);
        return list;
    }

    @Override
    protected List<ItemStack> getDropsFromTileEntity(IBlockAccess world, BlockPos pos, IBlockState state, TileEntity te, int fortune) {
        //System.out.printf("ShapeBlock.getDropsFromTileEntity: %s with fortune %s\n", te, fortune);
        List<ItemStack> result = new ArrayList<ItemStack>();
        if (te instanceof TileShape) {
            TileShape ste = (TileShape) te;
            ItemStack stack = ste.getShape().kind.newStack(ste.getShape(), ste.getBaseBlockState(), 1);
            result.add(stack);
            if (ste.hasSecondaryBlockState() ) {
                stack = ste.getShape().kind.newSecondaryMaterialStack(ste.getSecondaryBlockState());
                result.add(stack);
            }
        }
        return result;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        TileShape te = TileShape.get(world, pos);
        if (te != null)
            return te.newItemStack(1);
        else
            return ItemStack.EMPTY;
    }

    public IBlockState getBaseBlockState(IBlockAccess world, BlockPos pos) {
        TileShape te = this.getTileEntity(world, pos);
        if (te != null)
            return te.getBaseBlockState();
        return null;
    }

    @Override
    public float getPlayerRelativeBlockHardness(IBlockState state, EntityPlayer player, World world, BlockPos pos) {
        float result = 1.0F;
        IBlockState base = this.getBaseBlockState(world, pos);
        if (base != null) {
            //System.out.printf("ShapeBlock.getPlayerRelativeBlockHardness: base = %s\n", base);
            result = acBlockStrength(base, player, world, pos);
        }
        return result;
    }

    @Override
    public IBlockState getParticleState(IBlockAccess world, BlockPos pos) {
        IBlockState base = this.getBaseBlockState(world, pos);
        if (base != null)
            return base;
        else
            return this.getDefaultState();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.inventory.getCurrentItem();
        if (!stack.isEmpty()) {
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

    @SideOnly(Side.CLIENT)
    @Override
    public float getAmbientOcclusionLightValue(IBlockState state) {
        return 0.8f;
    }

    @Override
    public int getLightValue(IBlockState state) {
        return state.getValue(LIGHT);
    }

}
