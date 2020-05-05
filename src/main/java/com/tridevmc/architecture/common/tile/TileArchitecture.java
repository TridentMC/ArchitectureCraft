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

import com.tridevmc.architecture.common.ArchitectureLog;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.block.BlockHelper;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Vector3;
import com.tridevmc.architecture.common.modeldata.ModelProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nonnull;

public abstract class TileArchitecture extends TileEntity {

    protected boolean updateChunk;
    private byte side;
    private byte turn;

    public TileArchitecture(TileEntityType<?> type) {
        super(type);
    }

    public static ItemStack blockStackWithTileEntity(Block block, int size, TileArchitecture te) {
        return blockStackWithTileEntity(block, size, 0, te);
    }

    public static ItemStack blockStackWithTileEntity(Block block, int size, int meta, TileArchitecture te) {
        ItemStack stack = new ItemStack(block, size);
        if (te != null) {
            CompoundNBT tag = new CompoundNBT();
            te.writeToItemStackNBT(tag);
            stack.setTag(tag);
        }
        return stack;
    }

    public void sendTileEntityUpdate() {
        IPacket<?> packet = this.getUpdatePacket();
        if (packet != null && this.world instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) this.getWorld();
            world.getChunkProvider().
                    chunkManager.
                    getTrackingPlayers(new ChunkPos(this.getPos()), false)
                    .forEach((p) -> p.connection.sendPacket(packet));
        }
    }

    public int getX() {
        return this.pos.getX();
    }

    public int getY() {
        return this.pos.getY();
    }

    public int getZ() {
        return this.pos.getZ();
    }

    public void setSide(int side) {
        this.setSide((byte) side);
    }

    public void setTurn(int turn) {
        this.setTurn((byte) turn);
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
            return ((BlockArchitecture) block).localToGlobalTransformation(this.world, this.pos, state, origin);
        else {
            ArchitectureLog.info("BaseTileEntity.localToGlobalTransformation: Wrong block type at %s\n", this.pos);
            return new Trans3(origin);
        }
    }

    @Override
    public CompoundNBT getUpdateTag() {
        CompoundNBT nbt = new CompoundNBT();
        if (this.syncWithClient())
            this.write(nbt);
        return nbt;
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        if (this.syncWithClient()) {
            CompoundNBT nbt = new CompoundNBT();
            this.write(nbt);
            if (this.updateChunk) {
                nbt.putBoolean("updateChunk", true);
                this.updateChunk = false;
            }
            return new SUpdateTileEntityPacket(this.pos, 0, nbt);
        } else
            return null;
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        CompoundNBT nbt = pkt.getNbtCompound();
        this.read(nbt);
        if (nbt.getBoolean("updateChunk"))
            this.world.markBlockRangeForRenderUpdate(this.pos, Blocks.AIR.getDefaultState(), this.getBlockState());
    }

    boolean syncWithClient() {
        return true;
    }

    public void markBlockForUpdate() {
        this.updateChunk = true;
        BlockHelper.markBlockForUpdate(this.world, this.pos);
    }

    public void markForUpdate() {
        if (this.world instanceof ServerWorld) {
            ServerWorld world = (ServerWorld) this.getWorld();
            world.getChunkProvider().markBlockChanged(this.pos);
        }
    }

    public void playSoundEffect(SoundEvent name, float volume, float pitch) {
        this.world.playSound(null, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, name, SoundCategory.BLOCKS, volume, pitch);
    }


    @Override
    public void read(CompoundNBT nbt) {
        super.read(nbt);
        this.setSide(nbt.getByte("side"));
        this.setTurn(nbt.getByte("turn"));
        this.readContentsFromNBT(nbt);
    }

    public void readFromItemStack(ItemStack stack) {
        CompoundNBT nbt = stack.getTag();
        if (nbt != null)
            this.readFromItemStackNBT(nbt);
    }

    public void readFromItemStackNBT(CompoundNBT nbt) {
        this.readContentsFromNBT(nbt);
    }

    public void readContentsFromNBT(CompoundNBT nbt) {
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        super.write(nbt);
        if (this.getSide() != 0)
            nbt.putByte("side", this.getSide());
        if (this.getTurn() != 0)
            nbt.putByte("turn", this.getTurn());
        this.writeContentsToNBT(nbt);
        return nbt;
    }

    public void writeToItemStackNBT(CompoundNBT nbt) {
        this.writeContentsToNBT(nbt);
    }

    public void writeContentsToNBT(CompoundNBT nbt) {
    }

    public void markChanged() {
        this.markDirty();
        this.markForUpdate();
    }

    public void markBlockChanged() {
        this.markDirty();
        this.markBlockForUpdate();
    }

    @Override
    public void remove() {
        super.remove();
    }

    public ItemStack newItemStack(int size) {
        return blockStackWithTileEntity(this.getBlockState().getBlock(), size, this);
    }

    public byte getSide() {
        return this.side;
    }

    public void setSide(byte side) {
        this.side = side;
    }

    public byte getTurn() {
        return this.turn;
    }

    public void setTurn(byte turn) {
        this.turn = turn;
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
