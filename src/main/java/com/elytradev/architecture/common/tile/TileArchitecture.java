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

import com.elytradev.architecture.common.block.BlockArchitecture;
import com.elytradev.architecture.common.block.BlockHelper;
import com.elytradev.architecture.common.helpers.Trans3;
import com.elytradev.architecture.common.helpers.Vector3;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

import java.lang.reflect.Field;

import static com.elytradev.architecture.common.utils.ReflectionUtils.*;

public abstract class TileArchitecture extends TileEntity {

    protected static Field changedSectionFilter = getFieldDef(
            classForName("net.minecraft.server.management.PlayerChunkMapEntry"),
            "changedSectionFilter", "field_187288_h");
    public byte side, turn;
    public Ticket chunkTicket;
    protected boolean updateChunk;

    public static ItemStack blockStackWithTileEntity(Block block, int size, TileArchitecture te) {
        return blockStackWithTileEntity(block, size, 0, te);
    }

    public static ItemStack blockStackWithTileEntity(Block block, int size, int meta, TileArchitecture te) {
        ItemStack stack = new ItemStack(block, size, meta);
        if (te != null) {
            NBTTagCompound tag = new NBTTagCompound();
            te.writeToItemStackNBT(tag);
            stack.setTagCompound(tag);
        }
        return stack;
    }

    public int getX() {
        return pos.getX();
    }

    public int getY() {
        return pos.getY();
    }

    public int getZ() {
        return pos.getZ();
    }

    public void setSide(int side) {
        this.side = (byte) side;
    }

//  public Trans3 localToGlobalTransformation(double x, double y, double z) {
//      return localToGlobalTransformation(new Vector3(x + 0.5, y + 0.5, z + 0.5);
//  }

    public void setTurn(int turn) {
        this.turn = (byte) turn;
    }

    public Trans3 localToGlobalRotation() {
        return localToGlobalTransformation(Vector3.zero);
    }

    public Trans3 localToGlobalTransformation() {
        return localToGlobalTransformation(Vector3.blockCenter(pos));
    }

    public Trans3 localToGlobalTransformation(Vector3 origin) {
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (block instanceof BlockArchitecture)
            return ((BlockArchitecture) block).localToGlobalTransformation(world, pos, state, origin);
        else {
            System.out.printf("BaseTileEntity.localToGlobalTransformation: Wrong block type at %s\n", pos);
            return new Trans3(origin);
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound nbt = new NBTTagCompound();
        if (syncWithClient())
            writeToNBT(nbt);
        return nbt;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        //System.out.printf("BaseTileEntity.getDescriptionPacket for %s\n", this);
        if (syncWithClient()) {
            NBTTagCompound nbt = new NBTTagCompound();
            writeToNBT(nbt);
            if (updateChunk) {
                nbt.setBoolean("updateChunk", true);
                updateChunk = false;
            }
            return new SPacketUpdateTileEntity(pos, 0, nbt);
        } else
            return null;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        NBTTagCompound nbt = pkt.getNbtCompound();
        readFromNBT(nbt);
        if (nbt.getBoolean("updateChunk"))
            world.markBlockRangeForRenderUpdate(pos, pos);
    }

    boolean syncWithClient() {
        return true;
    }

    public void markBlockForUpdate() {
        updateChunk = true;
        BlockHelper.markBlockForUpdate(world, pos);
    }

    public void markForUpdate() {
        if (!world.isRemote) {
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();
            PlayerChunkMap pm = ((WorldServer) world).getPlayerChunkMap();
            PlayerChunkMapEntry entry = pm.getEntry(x >> 4, z >> 4);
            if (entry != null) {
                int oldFlags = getIntField(entry, changedSectionFilter);
                entry.blockChanged(x & 0xf, y, z & 0xf);
                setIntField(entry, changedSectionFilter, oldFlags);
            }
        }
    }

    public void playSoundEffect(SoundEvent name, float volume, float pitch) {
        world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, name, SoundCategory.BLOCKS, volume, pitch);
    }

    public abstract void onAddedToWorld();

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        side = nbt.getByte("side");
        turn = nbt.getByte("turn");
        readContentsFromNBT(nbt);
    }

    public void readFromItemStack(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null)
            readFromItemStackNBT(nbt);
    }

    public void readFromItemStackNBT(NBTTagCompound nbt) {
        readContentsFromNBT(nbt);
    }

    public void readContentsFromNBT(NBTTagCompound nbt) {
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        if (side != 0)
            nbt.setByte("side", side);
        if (turn != 0)
            nbt.setByte("turn", turn);
        writeContentsToNBT(nbt);
        return nbt;
    }

    public void writeToItemStackNBT(NBTTagCompound nbt) {
        writeContentsToNBT(nbt);
    }

    public void writeContentsToNBT(NBTTagCompound nbt) {
    }

    public void markChanged() {
        markDirty();
        markForUpdate();
    }

    public void markBlockChanged() {
        markDirty();
        markBlockForUpdate();
    }

    @Override
    public void invalidate() {
        releaseChunkTicket();
        super.invalidate();
    }

    public void releaseChunkTicket() {
        if (chunkTicket != null) {
            ForgeChunkManager.releaseTicket(chunkTicket);
            chunkTicket = null;
        }
    }

    public ItemStack newItemStack(int size) {
        return blockStackWithTileEntity(getBlockType(), size, this);
    }

}
