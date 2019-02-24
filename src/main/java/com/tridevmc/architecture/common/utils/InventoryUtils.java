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

package com.tridevmc.architecture.common.utils;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class InventoryUtils {

    public static InventorySide inventorySide(IInventory base, EnumFacing side) {
        if (base instanceof ISidedInventory)
            return new SidedInventorySide((ISidedInventory) base, side);
        else
            return new UnsidedInventorySide(base);
    }

    public static abstract class InventorySide {
        public int size;

        public abstract ItemStack get(int slot);

        public abstract boolean set(int slot, ItemStack stack);

        public abstract ItemStack extract(int slot);
    }

    public static class UnsidedInventorySide extends InventorySide {

        IInventory base;

        public UnsidedInventorySide(IInventory base) {
            this.base = base;
            size = base.getSizeInventory();
        }

        @Override
        public ItemStack get(int slot) {
            return base.getStackInSlot(slot);
        }

        @Override
        public boolean set(int slot, ItemStack stack) {
            base.setInventorySlotContents(slot, stack);
            return true;
        }

        @Override
        public ItemStack extract(int slot) {
            return get(slot);
        }

    }

    public static class SidedInventorySide extends InventorySide {

        ISidedInventory base;
        EnumFacing side;
        int[] slots;

        public SidedInventorySide(ISidedInventory base, EnumFacing side) {
            this.base = base;
            this.side = side;
            slots = base.getSlotsForFace(side);
            size = slots.length;
        }

        @Override
        public ItemStack get(int i) {
            return base.getStackInSlot(slots[i]);
        }

        @Override
        public boolean set(int i, ItemStack stack) {
            int slot = slots[i];
            if (base.canInsertItem(slot, stack, side)) {
                base.setInventorySlotContents(slot, stack);
                return true;
            } else
                return false;
        }

        @Override
        public ItemStack extract(int i) {
            int slot = slots[i];
            ItemStack stack = base.getStackInSlot(slot);
            if (base.canExtractItem(slot, stack, side))
                return stack;
            else
                return ItemStack.EMPTY;
        }

    }

}
