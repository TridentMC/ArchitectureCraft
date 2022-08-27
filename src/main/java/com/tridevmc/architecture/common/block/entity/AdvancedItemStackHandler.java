package com.tridevmc.architecture.common.block.entity;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

/**
 * Ripped out of Teckle - it's not stealing if I wrote the code in the first place.
 */
public class AdvancedItemStackHandler extends ItemStackHandler {

    public BiPredicate<Integer, ItemStack> insertCheck = (integer, stack) -> true;
    public IContentChangeListener changeListener = slot -> {/*NOOP*/};
    public ISlotLimit slotLimit = slot -> 64;

    public AdvancedItemStackHandler(int i) {
        super(i);
    }

    /**
     * Resets this handler to default settings, also clears all stacks.
     *
     * @param size the new size.
     * @return this handler.
     */
    public AdvancedItemStackHandler reset(int size) {
        this.stacks = NonNullList.withSize(size, ItemStack.EMPTY);
        this.changeListener = slot -> {/*NOOP*/};
        this.slotLimit = slot -> 64;
        this.insertCheck = (integer, stack) -> true;
        return this;
    }

    public AdvancedItemStackHandler withChangeListener(IContentChangeListener changeListener) {
        this.changeListener = changeListener;
        return this;
    }

    public AdvancedItemStackHandler withInsertCheck(BiPredicate<Integer, ItemStack> canPutInSlot) {
        this.insertCheck = canPutInSlot;
        return this;
    }

    public AdvancedItemStackHandler withSlotLimit(ISlotLimit slotLimit) {
        this.slotLimit = slotLimit;
        return this;
    }

    @Nonnull
    @Override
    public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
        if (this.insertCheck.test(slot, stack))
            return super.insertItem(slot, stack, simulate);
        else
            return stack;
    }

    public boolean canInsertItem(@Nonnull ItemStack stack) {
        for (int i = 0; i < this.getSlots(); i++) {
            if (this.insertItem(i, stack, true).isEmpty())
                return true;
        }
        return false;
    }

    /**
     * Attempt to insert all of the items provided and no less, if it's unable to fit everything the original stack will be returned.
     * <p>
     * Parameters are the same as normal insertItem.
     */
    public ItemStack insertItem(@Nonnull ItemStack stack, boolean simulate) {
        ItemStack remaining = stack.copy();
        for (int i = 0; i < this.getSlots(); i++) {
            if (remaining.isEmpty())
                break;
            remaining = this.insertItem(i, remaining, true);
        }

        if (remaining.isEmpty() && !simulate) {
            remaining = stack.copy();
            for (int i = 0; i < this.getSlots(); i++) {
                if (remaining.isEmpty())
                    break;
                remaining = this.insertItem(i, remaining, simulate);
            }
        }

        return remaining;
    }

    @Override
    protected void onContentsChanged(int slot) {
        this.changeListener.onContentChange(slot);
        super.onContentsChanged(slot);
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.slotLimit.slotLimit(slot);
    }

    public IItemHandler subHandler(int startSlot, int size) {
        return new IItemHandler() {
            @Override
            public int getSlots() {
                return size;
            }

            @Nonnull
            @Override
            public ItemStack getStackInSlot(int slot) {
                this.checkSlot(slot);
                return AdvancedItemStackHandler.this.getStackInSlot(slot + startSlot);
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                this.checkSlot(slot);
                return AdvancedItemStackHandler.this.insertItem(slot + startSlot, stack, simulate);
            }

            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                this.checkSlot(slot);
                return AdvancedItemStackHandler.this.extractItem(slot + startSlot, amount, simulate);
            }

            @Override
            public int getSlotLimit(int slot) {
                this.checkSlot(slot);
                return AdvancedItemStackHandler.this.getSlotLimit(slot + startSlot);
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                this.checkSlot(slot);
                return AdvancedItemStackHandler.this.isItemValid(slot + startSlot, stack);
            }

            private void checkSlot(int slot) {
                if (slot < 0 || slot >= size)
                    throw new RuntimeException("Slot " + slot + " not in valid range - [0," + size + ")");
            }
        };
    }

    public Stream<ItemStack> stream() {
        return this.stacks.stream();
    }

    public NonNullList<ItemStack> getStacks() {
        return this.stacks;
    }

    public AdvancedItemStackHandler copy() {
        AdvancedItemStackHandler advancedItemStackHandler = new AdvancedItemStackHandler(this.getSlots());
        for (int i = 0; i < this.getSlots(); i++) {
            advancedItemStackHandler.setStackInSlot(i, this.getStackInSlot(i).copy());
        }

        return advancedItemStackHandler;
    }

    public interface IContentChangeListener {
        void onContentChange(int slot);
    }

    public interface ISlotLimit {
        int slotLimit(int slot);
    }

}