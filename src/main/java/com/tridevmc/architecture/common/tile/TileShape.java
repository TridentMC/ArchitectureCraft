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
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.block.BlockHelper;
import com.tridevmc.architecture.common.block.BlockShape;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Utils;
import com.tridevmc.architecture.common.helpers.Vector3;
import com.tridevmc.architecture.common.item.ItemCladding;
import com.tridevmc.architecture.common.modeldata.ModelProperties;
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
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;

import javax.annotation.Nonnull;


public class TileShape extends TileEntity {

    private BlockShape block;
    private BlockState baseBlockState;
    private BlockState secondaryBlockState;
    private int disabledConnections;
    private byte offsetX;
    private byte side;
    private byte turn;

    public TileShape(){
        super(ArchitectureMod.CONTENT.tileTypeShape);

    }

    public TileShape(BlockShape block) {
        super(ArchitectureMod.CONTENT.tileTypeShape);
        this.block = block;
        this.baseBlockState = Blocks.OAK_PLANKS.getDefaultState();
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

    public boolean connectionIsEnabledGlobal(Direction dir) {
        return (this.disabledConnections & (1 << dir.ordinal())) == 0;
    }

    public void setConnectionEnabledGlobal(Direction dir, boolean state) {
        int bit = 1 << dir.ordinal();
        if (state)
            this.disabledConnections &= ~bit;
        else
            this.disabledConnections |= bit;
    }

    public void toggleConnectionGlobal(Direction dir) {
        boolean newState = !this.connectionIsEnabledGlobal(dir);
        this.setConnectionEnabledGlobal(dir, newState);
        TileShape nte = this.getNeighbourGlobal(dir);
        if (nte != null)
            nte.setConnectionEnabledGlobal(dir.getOpposite(), newState);
    }

    @Override
    public void read(CompoundNBT tag) {
        super.read(tag);
        this.readShapeFromNBT(tag);
        this.side = tag.getByte("Side");
        this.turn = tag.getByte("Turn");
        this.offsetX = tag.getByte("OffsetX");
    }

    protected void readShapeFromNBT(CompoundNBT nbt) {
        this.baseBlockState = Block.getStateById(nbt.getInt("BaseBlockState"));
        this.secondaryBlockState = Block.getStateById(nbt.getInt("SecondaryBlockState"));
        if (this.baseBlockState == null)
            this.baseBlockState = Blocks.OAK_PLANKS.getDefaultState();
        this.disabledConnections = nbt.getInt("Disconnected");
    }

    @Override
    public CompoundNBT write(CompoundNBT tag) {
        super.write(tag);
        this.writeShapeToNBT(tag);

        if (this.offsetX != 0) {
            tag.putByte("OffsetX", this.offsetX);
        }
        if (this.side != 0) {
            tag.putByte("Side", (byte) this.getSide());
        }
        if (this.turn != 0) {
            tag.putByte("Turn", (byte) this.getTurn());
        }
        return tag;
    }

    protected void writeShapeToNBT(CompoundNBT nbt) {
        if (this.getArchitectureShape() != null) {
            nbt.putInt("BaseBlockState", Block.getStateId(this.baseBlockState));
            nbt.putInt("SecondaryBlockState", Block.getStateId(this.secondaryBlockState));
        }
        if (this.disabledConnections != 0) {
            nbt.putInt("Disconnected", this.disabledConnections);
        }
    }

    public void onChiselUse(PlayerEntity player, Direction face, float hitX, float hitY, float hitZ) {
        this.getArchitectureShape().behaviour.onChiselUse(this, player, face, this.hitVec(hitX, hitY, hitZ));
    }

    public void onHammerUse(PlayerEntity player, Direction face, float hitX, float hitY, float hitZ) {
        this.getArchitectureShape().behaviour.onHammerUse(this, player, face, this.hitVec(hitX, hitY, hitZ));
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
        EnumShape architectureShape = this.getArchitectureShape();
        if (item instanceof ItemCladding && architectureShape.behaviour.acceptsCladding()) {
            materialState = ((ItemCladding) item).blockStateFromStack(stack);
        } else {
            Block block = Block.getBlockFromItem(item);
            BlockState state = block.getDefaultState();
            if (architectureShape.behaviour.isValidSecondaryMaterial(state)) {
                materialState = state;
            }
        }
        if (materialState != null) {
            if (!this.hasSecondaryMaterial()) {
                this.setSecondaryMaterial(materialState);
                if (!Utils.playerIsInCreativeMode(player))
                    stack.shrink(1);
            }
            return true;
        } else {
            return false;
        }
    }

    public void setBaseBlockState(BlockState state) {
        this.baseBlockState = state;
    }

    public void setSecondaryMaterial(BlockState state) {
        this.secondaryBlockState = state;
    }

    public boolean hasSecondaryMaterial(){
        return this.getSecondaryBlockState().getBlock() != Blocks.AIR;
    }

    public BlockState getBaseBlockState() {
        return this.baseBlockState;
    }

    public BlockState getSecondaryBlockState() {
        return this.secondaryBlockState;
    }

    public EnumShape getArchitectureShape() {
        return this.block.getArchitectureShape();
    }

    public int getDisabledConnections() {
        return this.disabledConnections;
    }

    public int getSide() {
        return this.side;
    }

    public int getTurn() {
        return this.turn;
    }

    public void setSide(int side) {
        this.setSide((byte) side);
    }

    public void setSide(byte side) {
        this.side = side;
    }

    public void setTurn(int turn){
        this.setTurn((byte) turn);
    }

    public void setTurn(byte turn) {
        this.turn = turn;
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

    public Trans3 localToGlobalRotation() {
        return this.localToGlobalTransformation(Vector3.zero);
    }

    public Trans3 localToGlobalTransformation() {
        return this.localToGlobalTransformation(Vector3.blockCenter(this.pos));
    }

    public Trans3 localToGlobalTransformation(Vector3 origin) {
        BlockState state = this.world.getBlockState(this.pos);
        Block block = state.getBlock();
        if (block instanceof BlockArchitecture)
            return ((BlockArchitecture) block).localToGlobalTransformation(this.world, this.pos, state, origin).translate(this.getOffsetX(), 0, 0);
        else {
            return new Trans3(origin).translate(this.getOffsetX(), 0, 0);
        }
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        ModelDataMap.Builder builder = new ModelDataMap.Builder();
        builder.withInitial(ModelProperties.WORLD, this.world);
        builder.withInitial(ModelProperties.POS, this.pos);
        builder.withInitial(ModelProperties.TILE, this);
        return builder.build();
    }

}
