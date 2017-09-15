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

package com.elytradev.architecture.common.tile;

import com.elytradev.architecture.common.item.ItemCladding;
import com.elytradev.architecture.common.helpers.Trans3;
import com.elytradev.architecture.common.helpers.Utils;
import com.elytradev.architecture.common.helpers.Vector3;
import com.elytradev.architecture.common.shape.Shape;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import static com.elytradev.architecture.common.block.BlockHelper.getNameForBlock;


public class TileShape extends TileArchitecture {

    public Shape shape;
    public IBlockState baseBlockState;
    public IBlockState secondaryBlockState;
    public int disabledConnections;
    private byte offsetX;

    public TileShape() {
        super();
        shape = Shape.ROOF_TILE;
        baseBlockState = Blocks.PLANKS.getDefaultState();
    }

    public TileShape(Shape s, IBlockState b) {
        super();
        shape = s;
        baseBlockState = b;
    }

    public TileShape(Shape s, Block b, int d) {
        super();
        shape = s;
        baseBlockState = b.getStateFromMeta(d);
    }

    public static TileShape get(IBlockAccess world, BlockPos pos) {
        if (world != null) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof TileShape)
                return (TileShape) te;
        }
        return null;
    }

    public double getOffsetX() {
        return offsetX * (1 / 16.0);
    }

    public void setOffsetX(double value) {
        offsetX = (byte) (16 * value);
    }

    @Override
    public Trans3 localToGlobalTransformation(Vector3 origin) {
        return super.localToGlobalTransformation(origin).translate(getOffsetX(), 0, 0);
    }

    @Override
    public void onAddedToWorld() {
        //NO-OP
    }

    public boolean connectionIsEnabledGlobal(EnumFacing dir) {
        return (disabledConnections & (1 << dir.ordinal())) == 0;
    }

    public void setConnectionEnabledGlobal(EnumFacing dir, boolean state) {
        int bit = 1 << dir.ordinal();
        if (state)
            disabledConnections &= ~bit;
        else
            disabledConnections |= bit;
        markBlockChanged();
    }

    public void toggleConnectionGlobal(EnumFacing dir) {
        boolean newState = !connectionIsEnabledGlobal(dir);
        setConnectionEnabledGlobal(dir, newState);
        TileShape nte = getNeighbourGlobal(dir);
        if (nte != null)
            nte.setConnectionEnabledGlobal(dir.getOpposite(), newState);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        //System.out.printf("ShapeTE.readFromNBT: %s\n", pos);
        super.readFromNBT(nbt);
        readShapeFromNBT(nbt);
        readSecondaryMaterialFromNBT(nbt);
        offsetX = nbt.getByte("offsetX");
    }

    @Override
    public void readFromItemStackNBT(NBTTagCompound nbt) {
        readShapeFromNBT(nbt);
    }

    protected void readShapeFromNBT(NBTTagCompound nbt) {
        shape = Shape.forId(nbt.getInteger("Shape"));
        baseBlockState = nbtGetBlockState(nbt, "BaseName", "BaseData");
        if (baseBlockState == null)
            baseBlockState = Blocks.PLANKS.getDefaultState();
        disabledConnections = nbt.getInteger("Disconnected");
    }

    protected void readSecondaryMaterialFromNBT(NBTTagCompound nbt) {
        secondaryBlockState = nbtGetBlockState(nbt, "Name2", "Data2");
    }

    protected IBlockState nbtGetBlockState(NBTTagCompound nbt, String nameField, String dataField) {
        String blockName = nbt.getString(nameField);
        if (blockName != null && blockName.length() > 0) {
            Block block = Block.getBlockFromName(blockName);
            int data = nbt.getInteger(dataField);
            IBlockState state = block.getStateFromMeta(data);
            return state;
        }
        return null;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        writeShapeToNBT(nbt);
        writeSecondaryMaterialToNBT(nbt);
        if (offsetX != 0)
            nbt.setByte("offsetX", offsetX);
        return nbt;
    }

    @Override
    public void writeToItemStackNBT(NBTTagCompound nbt) {
        writeShapeToNBT(nbt);
    }

    protected void writeShapeToNBT(NBTTagCompound nbt) {
        if (shape != null) {
            nbt.setInteger("Shape", shape.id);
            nbtSetBlockState(nbt, "BaseName", "BaseData", baseBlockState);
        }
        if (disabledConnections != 0)
            nbt.setInteger("Disconnected", disabledConnections);
    }

    protected void writeSecondaryMaterialToNBT(NBTTagCompound nbt) {
        nbtSetBlockState(nbt, "Name2", "Data2", secondaryBlockState);
    }

    protected void nbtSetBlockState(NBTTagCompound nbt, String nameField, String dataField, IBlockState state) {
        if (state != null) {
            Block block = state.getBlock();
            nbt.setString(nameField, getNameForBlock(block));
            nbt.setInteger(dataField, block.getMetaFromState(state));
        }
    }

    public void onChiselUse(EntityPlayer player, EnumFacing face, float hitX, float hitY, float hitZ) {
        shape.kind.onChiselUse(this, player, face, hitVec(hitX, hitY, hitZ));
    }

    public void onHammerUse(EntityPlayer player, EnumFacing face, float hitX, float hitY, float hitZ) {
        shape.kind.onHammerUse(this, player, face, hitVec(hitX, hitY, hitZ));
    }

    protected Vector3 hitVec(float hitX, float hitY, float hitZ) {
        return new Vector3(hitX - 0.5, hitY - 0.5, hitZ - 0.5);
    }

    public EnumFacing globalFace(EnumFacing face) {
        return localToGlobalRotation().t(face);
    }

    public EnumFacing localFace(EnumFacing face) {
        return localToGlobalRotation().it(face);
    }

    public boolean applySecondaryMaterial(ItemStack stack, EntityPlayer player) {
        IBlockState materialState = null;
        Item item = stack.getItem();
        if (item instanceof ItemCladding && shape.kind.acceptsCladding())
            materialState = ((ItemCladding) item).blockStateFromStack(stack);
        else {
            Block block = Block.getBlockFromItem(item);
            if (block != null) {
                IBlockState state = block.getStateFromMeta(stack.getMetadata());
                if (shape.kind.isValidSecondaryMaterial(state)) {
                    materialState = state;
                }
            }
        }
        if (materialState != null) {
            if (secondaryBlockState == null) {
                setSecondaryMaterial(materialState);
                if (!Utils.playerIsInCreativeMode(player))
                    stack.shrink(1);
            }
            return true;
        } else
            return false;
    }

    public void setSecondaryMaterial(IBlockState state) {
        secondaryBlockState = state;
        markBlockChanged();
    }

    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        if (baseBlockState.getBlock().canRenderInLayer(state, layer))
            return true;
        if (secondaryBlockState != null)
            return secondaryBlockState.getBlock().canRenderInLayer(state, layer);
        return false;
    }

    public TileShape getNeighbourGlobal(EnumFacing dir) {
        return TileShape.get(world, pos.offset(dir));
    }

    public TileShape getConnectedNeighbourGlobal(EnumFacing dir) {
        if (world != null) {
            if (connectionIsEnabledGlobal(dir)) {
                TileShape nte = getNeighbourGlobal(dir);
                if (nte != null && nte.connectionIsEnabledGlobal(dir.getOpposite()))
                    return nte;
            }
        }
        return null;
    }

}
