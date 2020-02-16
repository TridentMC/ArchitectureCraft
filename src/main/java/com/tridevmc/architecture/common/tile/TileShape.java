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

package com.tridevmc.architecture.common.tile;

import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.block.BlockHelper;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Utils;
import com.tridevmc.architecture.common.helpers.Vector3;
import com.tridevmc.architecture.common.item.ItemCladding;
import com.tridevmc.architecture.common.shape.EnumShape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;


public class TileShape extends TileArchitecture {

    public EnumShape shape;
    public BlockState baseBlockState;
    public BlockState secondaryBlockState;
    public int disabledConnections;
    private byte offsetX;

    public TileShape() {
        super(ArchitectureMod.CONTENT.tileTypeShape);
        this.shape = EnumShape.ROOF_TILE;
        this.baseBlockState = Blocks.OAK_PLANKS.getDefaultState();
    }

    public TileShape(EnumShape s, BlockState b) {
        super(ArchitectureMod.CONTENT.tileTypeShape);
        this.shape = s;
        this.baseBlockState = b;
    }

    public static TileShape get(IBlockReader world, BlockPos pos) {
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
    public Trans3 localToGlobalTransformation(Vector3 origin) {
        return super.localToGlobalTransformation(origin).translate(this.getOffsetX(), 0, 0);
    }

    @Override
    public void onAddedToWorld() {
        //NO-OP
    }

    public boolean connectionIsEnabledGlobal(Direction dir) {
        return (this.disabledConnections & (1 << dir.ordinal())) == 0;
    }

    public void setConnectionEnabledGlobal(Direction dir, boolean state) {
        int bit = 1 << dir.ordinal();
        if (state)
            this.disabledConnections &= ~bit;
        else
            this.disabledConnections |= bit;
        this.markBlockChanged();
    }

    public void toggleConnectionGlobal(Direction dir) {
        boolean newState = !this.connectionIsEnabledGlobal(dir);
        this.setConnectionEnabledGlobal(dir, newState);
        TileShape nte = this.getNeighbourGlobal(dir);
        if (nte != null)
            nte.setConnectionEnabledGlobal(dir.getOpposite(), newState);
    }

    @Override
    public void read(CompoundNBT nbt) {
        //System.out.printf("ShapeTE.read: %s\n", pos);
        super.read(nbt);
        this.readShapeFromNBT(nbt);
        this.offsetX = nbt.getByte("offsetX");
    }

    @Override
    public void readFromItemStackNBT(CompoundNBT nbt) {
        this.readShapeFromNBT(nbt);
    }

    protected void readShapeFromNBT(CompoundNBT nbt) {
        this.shape = EnumShape.forId(nbt.getInt("Shape"));
        this.baseBlockState = Block.getStateById(nbt.getInt("BaseStateId"));
        this.secondaryBlockState = Block.getStateById(nbt.getInt("SecondaryStateId"));
        if (this.baseBlockState == null)
            this.baseBlockState = Blocks.OAK_PLANKS.getDefaultState();
        this.disabledConnections = nbt.getInt("Disconnected");
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        super.write(nbt);
        this.writeShapeToNBT(nbt);

        if (this.offsetX != 0)
            nbt.putByte("offsetX", this.offsetX);
        return nbt;
    }

    @Override
    public void writeToItemStackNBT(CompoundNBT nbt) {
        this.writeShapeToNBT(nbt);
    }

    protected void writeShapeToNBT(CompoundNBT nbt) {
        if (this.shape != null) {
            nbt.putInt("Shape", this.shape.id);
            nbt.putInt("BaseStateId", Block.getStateId(this.baseBlockState));
            nbt.putInt("SecondaryStateId", Block.getStateId(this.secondaryBlockState));
        }
        if (this.disabledConnections != 0)
            nbt.putInt("Disconnected", this.disabledConnections);
    }

    public void onChiselUse(PlayerEntity player, Direction face, float hitX, float hitY, float hitZ) {
        this.shape.kind.onChiselUse(this, player, face, this.hitVec(hitX, hitY, hitZ));
    }

    public void onHammerUse(PlayerEntity player, Direction face, float hitX, float hitY, float hitZ) {
        this.shape.kind.onHammerUse(this, player, face, this.hitVec(hitX, hitY, hitZ));
    }

    protected Vector3 hitVec(float hitX, float hitY, float hitZ) {
        return new Vector3(hitX - 0.5, hitY - 0.5, hitZ - 0.5);
    }

    public Direction globalFace(Direction face) {
        return this.localToGlobalRotation().t(face);
    }

    public Direction localFace(Direction face) {
        return this.localToGlobalRotation().it(face);
    }

    public boolean applySecondaryMaterial(ItemStack stack, PlayerEntity player) {
        BlockState materialState = null;
        Item item = stack.getItem();
        if (item instanceof ItemCladding && this.shape.kind.acceptsCladding()) {
            materialState = ((ItemCladding) item).blockStateFromStack(stack);
        } else {
            Block block = Block.getBlockFromItem(item);
            if (block != null) {
                BlockState state = block.getDefaultState();
                if (this.shape.kind.isValidSecondaryMaterial(state)) {
                    materialState = state;
                }
            }
        }
        if (materialState != null) {
            if (this.secondaryBlockState == null) {
                this.setSecondaryMaterial(materialState);
                if (!Utils.playerIsInCreativeMode(player))
                    stack.shrink(1);
            }
            return true;
        } else
            return false;
    }

    public void setSecondaryMaterial(BlockState state) {
        this.secondaryBlockState = state;
        this.markBlockChanged();
    }

    public boolean canRenderInLayer(BlockState state, RenderType layer) {
        if (BlockHelper.blockCanRenderInLayer(this.baseBlockState, layer))
            return true;
        if (this.secondaryBlockState != null)
            return BlockHelper.blockCanRenderInLayer(this.secondaryBlockState, layer);
        return false;
    }

    public TileShape getNeighbourGlobal(Direction dir) {
        return TileShape.get(this.world, this.pos.offset(dir));
    }

    public TileShape getConnectedNeighbourGlobal(Direction dir) {
        if (this.world != null) {
            if (this.connectionIsEnabledGlobal(dir)) {
                TileShape nte = this.getNeighbourGlobal(dir);
                if (nte != null && nte.connectionIsEnabledGlobal(dir.getOpposite()))
                    return nte;
            }
        }
        return null;
    }

    public EnumShape getShape() {
        return this.shape;
    }
}
