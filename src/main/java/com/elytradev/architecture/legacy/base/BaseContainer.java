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

package com.elytradev.architecture.legacy.base;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.lang.reflect.Constructor;

public class BaseContainer extends Container {

    protected SlotRange playerSlotRange;
    int xSize, ySize;

//  public BaseContainer() {}

    public BaseContainer(int width, int height) {
        xSize = width;
        ySize = height;
    }

    public void addPlayerSlots(EntityPlayer player) {
        addPlayerSlots(player, (xSize - 160) / 2, ySize - 82);
    }

    public void addPlayerSlots(EntityPlayer player, int x, int y) {
        playerSlotRange = new SlotRange();
        InventoryPlayer inventory = player.inventory;
        for (int var3 = 0; var3 < 3; ++var3)
            for (int var4 = 0; var4 < 9; ++var4)
                this.addSlotToContainer(new Slot(inventory, var4 + var3 * 9 + 9, x + var4 * 18, y + var3 * 18));
        for (int var3 = 0; var3 < 9; ++var3)
            this.addSlotToContainer(new Slot(inventory, var3, x + var3 * 18, y + 57));
        playerSlotRange.end();
    }

    public SlotRange addSlots(IInventory inventory, int x, int y, int numRows) {
        return addSlots(inventory, 0, inventory.getSizeInventory(), x, y, numRows);
    }

    public SlotRange addSlots(IInventory inventory, int x, int y, int numRows, Class slotClass) {
        return addSlots(inventory, 0, inventory.getSizeInventory(), x, y, numRows, slotClass);
    }

    public SlotRange addSlots(IInventory inventory, int firstSlot, int numSlots, int x, int y, int numRows) {
        return addSlots(inventory, firstSlot, numSlots, x, y, numRows, Slot.class);
    }

    public SlotRange addSlots(IInventory inventory, int firstSlot, int numSlots, int x, int y, int numRows,
                              Class slotClass) {
        SlotRange range = new SlotRange();
        try {
            Constructor slotCon = slotClass.getConstructor(IInventory.class, int.class, int.class, int.class);
            int numCols = (numSlots + numRows - 1) / numRows;
            for (int i = 0; i < numSlots; i++) {
                int row = i / numCols;
                int col = i % numCols;
                addSlotToContainer((Slot) slotCon.newInstance(inventory, firstSlot + i, x + col * 18, y + row * 18));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        range.end();
        return range;
    }

    @Override
    public boolean canInteractWith(EntityPlayer var1) {
        return true;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (int i = 0; i < listeners.size(); i++) {
            IContainerListener listener = listeners.get(i);
            sendStateTo(listener);
        }
    }

    // To enable shift-clicking, check validitity of items here and call
    // mergeItemStack as appropriate.
    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        ItemStack stack = slot.getStack();
        if (slot != null && slot.getHasStack()) {
            SlotRange destRange = transferSlotRange(index, stack);
            if (destRange != null) {
                result = stack.copy();
                if (!mergeItemStackIntoRange(stack, destRange))
                    return null;
                if (stack.getCount() == 0)
                    slot.putStack(ItemStack.EMPTY);
                else
                    slot.onSlotChanged();
            }
        }
        return result;
    }

    protected boolean mergeItemStackIntoRange(ItemStack stack, SlotRange range) {
        return mergeItemStack(stack, range.firstSlot, range.numSlots, range.reverseMerge);
    }

    // Return the range of slots into which the given stack should be moved by
    // a shift-click.
    protected SlotRange transferSlotRange(int srcSlotIndex, ItemStack stack) {
        return null;
    }

    void sendStateTo(IContainerListener listener) {
    }

    @Override
    public void updateProgressBar(int i, int value) {
    }

    public class SlotRange {
        public int firstSlot;
        public int numSlots;
        public boolean reverseMerge;

        public SlotRange() {
            firstSlot = inventorySlots.size();
        }

        public void end() {
            numSlots = inventorySlots.size() - firstSlot;
        }

        public boolean contains(int slot) {
            return slot >= firstSlot && slot < firstSlot + numSlots;
        }
    }

}
