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

import com.elytradev.architecture.common.helpers.Trans3;
import com.elytradev.architecture.common.helpers.Utils;
import com.elytradev.architecture.common.helpers.Vector3;
import com.elytradev.architecture.common.item.ItemCladding;
import com.elytradev.architecture.common.shape.Shape;
import com.google.common.base.MoreObjects;
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

    private Shape shape;
    private IBlockState baseBlockState;
    private IBlockState secondaryBlockState;
    private byte offsetX;
    private int disabledConnections;

    public TileShape() {
        super();
        this.shape = Shape.ROOF_TILE;
        this.baseBlockState = Blocks.PLANKS.getDefaultState();
        this.secondaryBlockState = Blocks.AIR.getDefaultState();
    }

    public TileShape(Shape s, IBlockState b) {
        super();
        this.shape = s;
        this.baseBlockState = b;
        this.secondaryBlockState = Blocks.AIR.getDefaultState();
    }

    public TileShape(Shape s, Block b, int d) {
        super();
        this.shape = s;
        this.baseBlockState = b.getStateFromMeta(d);
        this.secondaryBlockState = Blocks.AIR.getDefaultState();
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
        return this.offsetX * (1 / 16.0);
    }

    public void setOffsetX(double value) {
        this.offsetX = (byte) (16 * value);
    }

    @Override
    public Trans3 localToGlobalTransformation(Vector3 origin, IBlockState state) {
        return super.localToGlobalTransformation(origin, state).translate(this.getOffsetX(), 0, 0);
    }

    @Override
    public void onAddedToWorld() {
        //NO-OP
    }

    public boolean connectionIsEnabledGlobal(EnumFacing dir) {
        return (this.disabledConnections & (1 << dir.ordinal())) == 0;
    }

    public void setConnectionEnabledGlobal(EnumFacing dir, boolean state) {
        int bit = 1 << dir.ordinal();
        if (state)
            this.disabledConnections &= ~bit;
        else
            this.disabledConnections |= bit;
        this.markBlockChanged();
    }

    public void toggleConnectionGlobal(EnumFacing dir) {
        boolean newState = !this.connectionIsEnabledGlobal(dir);
        this.setConnectionEnabledGlobal(dir, newState);
        TileShape nte = this.getNeighbourGlobal(dir);
        if (nte != null)
            nte.setConnectionEnabledGlobal(dir.getOpposite(), newState);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        //System.out.printf("ShapeTE.readFromNBT: %s\n", pos);
        super.readFromNBT(nbt);
        this.readShapeFromNBT(nbt);
        this.readSecondaryMaterialFromNBT(nbt);
        this.offsetX = nbt.getByte("offsetX");
    }

    @Override
    public void readFromItemStackNBT(NBTTagCompound nbt) {
        this.readShapeFromNBT(nbt);
    }

    protected void readShapeFromNBT(NBTTagCompound nbt) {
        this.shape = Shape.forId(nbt.getInteger("Shape"));
        this.baseBlockState = this.nbtGetBlockState(nbt, "BaseName", "BaseData");
        if (this.baseBlockState.getBlock() == Blocks.AIR.getDefaultState())
            this.baseBlockState = Blocks.PLANKS.getDefaultState();
        this.disabledConnections = nbt.getInteger("Disconnected");
    }

    protected void readSecondaryMaterialFromNBT(NBTTagCompound nbt) {
        this.secondaryBlockState = this.nbtGetBlockState(nbt, "Name2", "Data2");
    }

    protected IBlockState nbtGetBlockState(NBTTagCompound nbt, String nameField, String dataField) {
        String blockName = nbt.getString(nameField);
        if (!blockName.isEmpty()) {
            Block block = Block.getBlockFromName(blockName);
            int data = nbt.getInteger(dataField);
            return block != null ? block.getStateFromMeta(data) : Blocks.AIR.getDefaultState();
        }
        return Blocks.AIR.getDefaultState();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        this.writeShapeToNBT(nbt);
        this.writeSecondaryMaterialToNBT(nbt);
        if (this.offsetX != 0)
            nbt.setByte("offsetX", this.offsetX);
        return nbt;
    }

    @Override
    public void writeToItemStackNBT(NBTTagCompound nbt) {
        this.writeShapeToNBT(nbt);
    }

    protected void writeShapeToNBT(NBTTagCompound nbt) {
        if (this.shape != null) {
            nbt.setInteger("Shape", this.shape.id);
            this.nbtSetBlockState(nbt, "BaseName", "BaseData", this.baseBlockState);
        }
        if (this.disabledConnections != 0)
            nbt.setInteger("Disconnected", this.disabledConnections);
    }

    protected void writeSecondaryMaterialToNBT(NBTTagCompound nbt) {
        this.nbtSetBlockState(nbt, "Name2", "Data2", this.secondaryBlockState);
    }

    protected void nbtSetBlockState(NBTTagCompound nbt, String nameField, String dataField, IBlockState state) {
        if (state != null) {
            Block block = state.getBlock();
            nbt.setString(nameField, getNameForBlock(block));
            nbt.setInteger(dataField, block.getMetaFromState(state));
        }
    }

    public void onChiselUse(EntityPlayer player, EnumFacing face, float hitX, float hitY, float hitZ) {
        this.shape.kind.onChiselUse(this, player, face, this.hitVec(hitX, hitY, hitZ));
    }

    public void onHammerUse(EntityPlayer player, EnumFacing face, float hitX, float hitY, float hitZ) {
        this.shape.kind.onHammerUse(this, player, face, this.hitVec(hitX, hitY, hitZ));
    }

    protected Vector3 hitVec(float hitX, float hitY, float hitZ) {
        return new Vector3(hitX - 0.5, hitY - 0.5, hitZ - 0.5);
    }

    public EnumFacing globalFace(EnumFacing face) {
        return this.localToGlobalRotation().t(face);
    }

    public EnumFacing localFace(EnumFacing face) {
        return this.localToGlobalRotation().it(face);
    }

    public boolean applySecondaryMaterial(ItemStack stack, EntityPlayer player) {
        IBlockState materialState = Blocks.AIR.getDefaultState();
        Item item = stack.getItem();
        if (item instanceof ItemCladding && this.shape.kind.acceptsCladding()) {
            materialState = ((ItemCladding) item).blockStateFromStack(stack);
        } else {
            Block block = Block.getBlockFromItem(item);
            if (block != Blocks.AIR) {
                IBlockState state = block.getStateFromMeta(stack.getMetadata());
                if (this.shape.kind.isValidSecondaryMaterial(state)) {
                    materialState = state;
                }
            }
        }
        if (materialState.getBlock() != Blocks.AIR) {
            if (this.secondaryBlockState == Blocks.AIR.getDefaultState()) {
                this.setSecondaryMaterial(materialState);
                if (!Utils.playerIsInCreativeMode(player))
                    stack.shrink(1);
            }
            return true;
        } else {
            return false;
        }
    }

    public void setSecondaryMaterial(IBlockState state) {
        this.secondaryBlockState = state;
        this.markBlockChanged();
    }

    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        if (this.baseBlockState.getBlock().canRenderInLayer(state, layer))
            return true;
        if (this.secondaryBlockState.getBlock() != Blocks.AIR)
            return this.secondaryBlockState.getBlock().canRenderInLayer(state, layer);
        return false;
    }

    public TileShape getNeighbourGlobal(EnumFacing dir) {
        return TileShape.get(this.world, this.pos.offset(dir));
    }

    public TileShape getConnectedNeighbourGlobal(EnumFacing dir) {
        if (this.world != null) {
            if (this.connectionIsEnabledGlobal(dir)) {
                TileShape nte = this.getNeighbourGlobal(dir);
                if (nte != null && nte.connectionIsEnabledGlobal(dir.getOpposite()))
                    return nte;
            }
        }
        return null;
    }

    public boolean hasBaseBlockState() {
        return this.baseBlockState.getBlock() != Blocks.AIR;
    }

    public boolean hasSecondaryBlockState() {
        return this.secondaryBlockState.getBlock() != Blocks.AIR;
    }

    public boolean hasShape() {
        return this.shape != null;
    }

    public Shape getShape() {
        return this.shape;
    }

    public IBlockState getBaseBlockState() {
        return this.baseBlockState;
    }

    public IBlockState getSecondaryBlockState() {
        return this.secondaryBlockState;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("shape", this.shape)
                .add("side", this.getSide())
                .add("turn", this.getTurn())
                .add("baseBlockState", this.baseBlockState)
                .add("secondaryBlockState", this.secondaryBlockState)
                .add("offsetX", this.offsetX)
                .add("disabledConnections", this.disabledConnections)
                .toString();
    }
}
