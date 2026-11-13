package com.tridevmc.architecture.common.block.container;

import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.item.ItemShape;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.compound.ui.container.CompoundContainerMenu;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ContainerSawbench extends CompoundContainerMenu {

    private final Container sawbenchInventory = new SimpleContainer(2);
    public final Inventory playerInventory;
    private final ContainerLevelAccess access;
    private final DataSlot selectedShapeIndex = DataSlot.standalone();


    public ContainerSawbench(Inventory playerInv, int id, ContainerLevelAccess access) {
        super(ArchitectureMod.CONTENT.universalMenuType, id);
        this.playerInventory = playerInv;
        this.access = access;

        // Add sawbench input slot (accepts blocks only)
        this.addSlot(new Slot(this.sawbenchInventory, 0, 0, 0) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.getItem() instanceof BlockItem;
            }

            @Override
            public void setChanged() {
                super.setChanged();
                ContainerSawbench.this.updateResult();
            }
        });

        // Add sawbench output slot (result only, cannot place items)
        this.addSlot(new Slot(this.sawbenchInventory, 1, 0, 0) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }

            @Override
            public void onTake(@NotNull Player player, @NotNull ItemStack stack) {
                // Consume one item from input slot
                ItemStack inputStack = ContainerSawbench.this.sawbenchInventory.getItem(0);
                if (!inputStack.isEmpty()) {
                    inputStack.shrink(1);
                }
                ContainerSawbench.this.updateResult();
                super.onTake(player, stack);
            }
        });

        // Player inventory (3x9)
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInv, col + row * 9 + 9, this.slots.size(), 0));
            }
        }

        // Hotbar (1x9)
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInv, col, this.slots.size(), 0));
        }

        this.selectedShapeIndex.set(-1);
        this.addDataSlot(this.selectedShapeIndex);
    }

    public ContainerSawbench(Inventory playerInv, int id) {
        this(playerInv, id, ContainerLevelAccess.NULL);
    }

    public void selectShape(@Nullable EnumShape shape) {
        if (shape != null) {
            this.selectedShapeIndex.set(shape.ordinal());
        } else {
            this.selectedShapeIndex.set(-1);
        }
        this.updateResult();
    }

    @Override
    public boolean clickMenuButton(@NotNull Player player, int buttonId) {
        if (buttonId >= 0 && buttonId < EnumShape.values().length) {
            EnumShape shape = EnumShape.values()[buttonId];
            this.selectShape(shape);
            return true;
        }
        return false;
    }

    @Nullable
    public EnumShape getSelectedShape() {
        int index = this.selectedShapeIndex.get();
        return index >= 0 && index < EnumShape.values().length ? EnumShape.values()[index] : null;
    }

    public ItemStack getInputStack() {
        return this.sawbenchInventory.getItem(0);
    }

    private void updateResult() {
        ItemStack inputStack = this.sawbenchInventory.getItem(0);

        var selectedShape = this.getSelectedShape();
        if (inputStack.isEmpty() || selectedShape == null) {
            this.sawbenchInventory.setItem(1, ItemStack.EMPTY);
            return;
        }

        // Get the block state from the input item
        if (inputStack.getItem() instanceof BlockItem blockItem) {
            var blockState = blockItem.getBlock().defaultBlockState();
            var outputStack = ItemShape.createStack(selectedShape, blockState, 1);
            this.sawbenchInventory.setItem(1, outputStack);
        }
    }

    @Override
    @NotNull
    public ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
        if (slotIndex < 0 || slotIndex >= this.slots.size()) return ItemStack.EMPTY;
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot != null && slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            result = slotStack.copy();

            if (slotIndex == 1) {
                // Taking from output slot
                if (!this.moveItemStackTo(slotStack, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(slotStack, result);
            } else if (slotIndex == 0) {
                // Taking from input slot
                if (!this.moveItemStackTo(slotStack, 2, 38, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // From player inventory - try to put in input
                if (slotStack.getItem() instanceof BlockItem) {
                    if (!this.moveItemStackTo(slotStack, 0, 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    return ItemStack.EMPTY;
                }
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return result;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return stillValid(this.access, player, ArchitectureMod.CONTENT.blockSawbench);
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> {
            // The result is a preview; only the unspent input belongs to the player.
            this.sawbenchInventory.removeItemNoUpdate(1);
            this.clearContainer(player, this.sawbenchInventory);
        });
    }
}
