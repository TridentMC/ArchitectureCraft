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
import com.tridevmc.architecture.common.utils.ReflectionUtils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import java.lang.reflect.Field;

public abstract class TileArchitecture extends TileEntity {

    protected static Field changedSectionFilter = ReflectionUtils.getFieldDef(
            ReflectionUtils.classForName("net.minecraft.server.management.PlayerChunkMapEntry"),
            "changedSectionFilter", "field_187288_h");
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
            NBTTagCompound tag = new NBTTagCompound();
            te.writeToItemStackNBT(tag);
            stack.setTag(tag);
        }
        return stack;
    }

    public void sendTileEntityUpdate() {
        Packet packet = getUpdatePacket();
        if (packet != null) {
            BlockPos pos = getPos();
            int x = pos.getX() >> 4;
            int z = pos.getZ() >> 4;
            WorldServer world = (WorldServer) getWorld();
            PlayerList cm = world.getServer().getPlayerList();
            PlayerChunkMap pm = world.getPlayerChunkMap();
            for (EntityPlayerMP player : cm.getPlayers()) {
                if (pm.isPlayerWatchingChunk(player, x, z)) {
                    player.connection.sendPacket(packet);
                }
            }
        }
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
        this.setSide((byte) side);
    }

    public void setTurn(int turn) {
        this.setTurn((byte) turn);
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
            ArchitectureLog.info("BaseTileEntity.localToGlobalTransformation: Wrong block type at %s\n", pos);
            return new Trans3(origin);
        }
    }

    @Override
    public NBTTagCompound getUpdateTag() {
        NBTTagCompound nbt = new NBTTagCompound();
        if (syncWithClient())
            write(nbt);
        return nbt;
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        //ArchitectureLog.info("BaseTileEntity.getDescriptionPacket for %s\n", this);
        if (syncWithClient()) {
            NBTTagCompound nbt = new NBTTagCompound();
            write(nbt);
            if (updateChunk) {
                nbt.putBoolean("updateChunk", true);
                updateChunk = false;
            }
            return new SPacketUpdateTileEntity(pos, 0, nbt);
        } else
            return null;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        NBTTagCompound nbt = pkt.getNbtCompound();
        read(nbt);
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
                int oldFlags = ReflectionUtils.getIntField(entry, changedSectionFilter);
                entry.blockChanged(x & 0xf, y, z & 0xf);
                ReflectionUtils.setIntField(entry, changedSectionFilter, oldFlags);
            }
        }
    }

    public void playSoundEffect(SoundEvent name, float volume, float pitch) {
        world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, name, SoundCategory.BLOCKS, volume, pitch);
    }

    public abstract void onAddedToWorld();

    @Override
    public void read(NBTTagCompound nbt) {
        super.read(nbt);
        setSide(nbt.getByte("side"));
        setTurn(nbt.getByte("turn"));
        readContentsFromNBT(nbt);
    }

    public void readFromItemStack(ItemStack stack) {
        NBTTagCompound nbt = stack.getTag();
        if (nbt != null)
            readFromItemStackNBT(nbt);
    }

    public void readFromItemStackNBT(NBTTagCompound nbt) {
        readContentsFromNBT(nbt);
    }

    public void readContentsFromNBT(NBTTagCompound nbt) {
    }

    @Override
    public NBTTagCompound write(NBTTagCompound nbt) {
        super.write(nbt);
        if (getSide() != 0)
            nbt.putByte("side", getSide());
        if (getTurn() != 0)
            nbt.putByte("turn", getTurn());
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
    public void remove() {
        super.remove();
    }

    public ItemStack newItemStack(int size) {
        return blockStackWithTileEntity(getBlockState().getBlock(), size, this);
    }

    public byte getSide() {
        return side;
    }

    public void setSide(byte side) {
        this.side = side;
    }

    public byte getTurn() {
        return turn;
    }

    public void setTurn(byte turn) {
        this.turn = turn;
    }
}