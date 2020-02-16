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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;

public abstract class TileArchitectureInventory extends TileArchitecture implements IInventory, ISidedInventory {

    protected int[] allSlots;

    public TileArchitectureInventory(TileEntityType<?> type) {
        super(type);
    }

    protected IInventory getInventory() {
        return null;
    }

    public void readInventoryFromNBT(CompoundNBT nbt) {
        IInventory inventory = this.getInventory();
        if (inventory != null) {
            ListNBT list = nbt.getList("inventory", 10);
            int n = list.size();
            for (int i = 0; i < n; i++) {
                CompoundNBT item = list.getCompound(i);
                int slot = item.getInt("slot");
                ItemStack stack = ItemStack.read(item);
                inventory.setInventorySlotContents(slot, stack);
            }
        }
    }

    @Override
    public void read(CompoundNBT nbt) {
        super.read(nbt);
        this.readInventoryFromNBT(nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt) {
        super.write(nbt);
        this.writeInventoryToNBT(nbt);
        return nbt;
    }

    public void writeInventoryToNBT(CompoundNBT nbt) {
        IInventory inventory = this.getInventory();
        if (inventory != null) {
            ListNBT list = new ListNBT();
            int n = inventory.getSizeInventory();
            for (int i = 0; i < n; i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (stack != null) {
                    CompoundNBT item = new CompoundNBT();
                    item.putInt("slot", i);
                    stack.write(item);
                    list.add(item);
                }
            }
            nbt.put("inventory", list);
        }
    }

    protected void onInventoryChanged(int slot) {
        this.markDirty();
    }

//------------------------------------- IInventory -----------------------------------------

    /**
     * Returns the number of slots in the inventory.
     */
    @Override
    public int getSizeInventory() {
        IInventory inventory = this.getInventory();
        return (inventory != null) ? inventory.getSizeInventory() : 0;
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < this.getSizeInventory(); i++) {
            if (!this.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    /**
     * Returns the stack in slot i
     */
    @Override
    public ItemStack getStackInSlot(int slot) {
        IInventory inventory = this.getInventory();
        return (inventory != null) ? inventory.getStackInSlot(slot) : null;
    }

    /**
     * Removes from an inventory slot (first arg) up to a specified number (second arg) of items and returns them in a
     * new stack.
     */
    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        IInventory inventory = this.getInventory();
        if (inventory != null) {
            ItemStack result = inventory.decrStackSize(slot, amount);
            this.onInventoryChanged(slot);
            return result;
        } else
            return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        IInventory inventory = this.getInventory();
        if (inventory != null) {
            ItemStack result = inventory.removeStackFromSlot(slot);
            this.onInventoryChanged(slot);
            return result;
        } else
            return ItemStack.EMPTY;
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        IInventory inventory = this.getInventory();
        if (inventory != null) {
            inventory.setInventorySlotContents(slot, stack);
            this.onInventoryChanged(slot);
        }
    }

    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be extended. *Isn't
     * this more of a set than a get?*
     */
    @Override
    public int getInventoryStackLimit() {
        IInventory inventory = this.getInventory();
        return (inventory != null) ? inventory.getInventoryStackLimit() : 0;
    }

    /**
     * Do not make give this method the name canInteractWith because it clashes with Container
     */
    @Override
    public boolean isUsableByPlayer(PlayerEntity player) {
        IInventory inventory = this.getInventory();
        return (inventory == null) || inventory.isUsableByPlayer(player);
    }

    @Override
    public void openInventory(PlayerEntity player) {
        IInventory inventory = this.getInventory();
        if (inventory != null)
            inventory.openInventory(player);
    }

    @Override
    public void closeInventory(PlayerEntity player) {
        IInventory inventory = this.getInventory();
        if (inventory != null)
            inventory.closeInventory(player);
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        IInventory inventory = this.getInventory();
        if (inventory != null)
            return inventory.isItemValidForSlot(slot, stack);
        else
            return false;
    }

    @Override
    public void clear() {
        IInventory inventory = this.getInventory();
        if (inventory != null)
            inventory.clear();
    }

//------------------------------------- ISidedInventory -----------------------------------------

    /**
     * Returns an array containing the indices of the slots that can be accessed by automation on the given side of this
     * block.
     */
    @Override
    public int[] getSlotsForFace(Direction side) {
        IInventory inventory = this.getInventory();
        if (inventory instanceof ISidedInventory)
            return ((ISidedInventory) inventory).getSlotsForFace(side);
        else {
            if (this.allSlots == null) {
                int n = this.getSizeInventory();
                this.allSlots = new int[n];
                for (int i = 0; i < n; i++)
                    this.allSlots[i] = i;
            }
            return this.allSlots;
        }
    }

    /**
     * Returns true if automation can insert the given item in the given slot from the given side. Args: Slot, item,
     * side
     */
    @Override
    public boolean canInsertItem(int slot, ItemStack stack, Direction side) {
        IInventory inventory = this.getInventory();
        if (inventory instanceof ISidedInventory)
            return ((ISidedInventory) inventory).canInsertItem(slot, stack, side);
        else
            return true;
    }

    /**
     * Returns true if automation can extract the given item in the given slot from the given side. Args: Slot, item,
     * side
     */
    @Override
    public boolean canExtractItem(int slot, ItemStack stack, Direction side) {
        IInventory inventory = this.getInventory();
        if (inventory instanceof ISidedInventory)
            return ((ISidedInventory) inventory).canExtractItem(slot, stack, side);
        else
            return true;
    }

}
