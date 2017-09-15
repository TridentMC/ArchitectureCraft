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

import com.elytradev.architecture.legacy.base.BaseContainer;
import com.elytradev.architecture.legacy.base.BaseDataChannel;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ContainerSawbench extends BaseContainer {

    public static int guWidth = 242;
    public static int guiHeight = 224;
    public static int inputSlotLeft = 12;
    public static int inputSlotTop = 19;
    public static int outputSlotLeft = 12;
    public static int outputSlotTop = 57;

    TileSawbench te;
    SlotRange sawbenchSlotRange;
    Slot materialSlot, resultSlot;

    public ContainerSawbench(EntityPlayer player, TileSawbench te) {
        super(guWidth, guiHeight);
        this.te = te;
        sawbenchSlotRange = new SlotRange();
        materialSlot = addSlotToContainer(new Slot(te, 0, inputSlotLeft, inputSlotTop));
        resultSlot = addSlotToContainer(new SlotSawbenchResult(te, 1, outputSlotLeft, outputSlotTop));
        sawbenchSlotRange.end();
        addPlayerSlots(player, 8, guiHeight - 81);
    }

    public static Container create(EntityPlayer player, World world, BlockPos pos) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileSawbench)
            return new ContainerSawbench(player, (TileSawbench) te);
        else
            return null;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return this.te.isUsableByPlayer(player);
    }

    @Override
    protected SlotRange transferSlotRange(int srcSlotIndex, ItemStack stack) {
        if (playerSlotRange.contains(srcSlotIndex))
            return sawbenchSlotRange;
        else
            return playerSlotRange;
    }

    @Override
    public void detectAndSendChanges() {
        for (int i = 0; i < this.inventorySlots.size(); ++i) {
            ItemStack newstack = this.inventorySlots.get(i).getStack();
            ItemStack oldstack = this.inventoryItemStacks.get(i);
            if (!ItemStack.areItemStacksEqual(oldstack, newstack)) {
                oldstack = newstack.isEmpty() ? ItemStack.EMPTY : newstack.copy();
                this.inventoryItemStacks.set(i, oldstack);
                for (Object listener : listeners) {
                    if (listener instanceof EntityPlayerMP) {
                        //System.out.printf("SawbenchContainer.updateCraftingResults: sending %s in slot %d to player\n", newstack, i);
                        ((EntityPlayerMP) listener).connection.sendPacket(
                                new SPacketSetSlot(windowId, i, newstack));
                    } else
                        ((IContainerListener) listener).sendSlotContents(this, i, newstack);
                }
            }
        }
    }

    @BaseDataChannel.ServerMessageHandler("SelectShape")
    public void onSelectShape(EntityPlayer player, BaseDataChannel.ChannelInput data) {
        int page = data.readInt();
        int slot = data.readInt();
        te.setSelectedShape(page, slot);
    }

    //
    //   Client
    //

    @Override
    public void putStackInSlot(int i, ItemStack stack) {
        // Slot update packet has arrived from server. Do not trigger crafting behaviour.
        Slot slot = getSlot(i);
        if (slot instanceof SlotSawbench) {
            ((SlotSawbench) slot).updateFromServer(stack);
        } else
            super.putStackInSlot(i, stack);
    }

    // Default transferStackInSlot does not invoke decrStackSize, so we need this
    // to get pending material used.
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = getSlot(index);
        if (slot == resultSlot)
            return transferStackInResultSlot(player, index);
        else
            return super.transferStackInSlot(player, index);
    }

    protected ItemStack transferStackInResultSlot(EntityPlayer player, int index) {
        boolean materialWasPending = te.pendingMaterialUsage;
        ItemStack origMaterialStack = te.usePendingMaterial();
        ItemStack result = super.transferStackInSlot(player, index);
        if (materialWasPending)
            te.returnUnusedMaterial(origMaterialStack);
        return result;
    }
}

class SlotSawbench extends Slot {

    TileSawbench te;
    int index;

    public SlotSawbench(TileSawbench te, int index, int x, int y) {
        super(te, index, x, y);
        this.te = te;
        this.index = index;
    }

    void updateFromServer(ItemStack stack) {
        te.inventory.setInventorySlotContents(index, stack);
    }

}

//------------------------------------------------------------------------------

class SlotSawbenchResult extends SlotSawbench {

    public SlotSawbenchResult(TileSawbench te, int index, int x, int y) {
        super(te, index, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack newstack) {
        return false;
    }

}

	