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

import com.elytradev.architecture.common.shape.EnumShape;
import com.elytradev.architecture.common.shape.ShapePage;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

public class TileSawbench extends TileArchitectureInventory {

    final public static int materialSlot = 0;
    final public static int resultSlot = 1;

    final public static int[] materialSideSlots = {materialSlot};
    final public static int[] resultSideSlots = {resultSlot};

    public static boolean allowAutomation = false;

    public static ShapePage[] pages = {
            new ShapePage("roofing",
                    EnumShape.ROOF_TILE, EnumShape.ROOF_OUTER_CORNER, EnumShape.ROOF_INNER_CORNER,
                    EnumShape.ROOF_RIDGE, EnumShape.ROOF_SMART_RIDGE, EnumShape.ROOF_VALLEY,
                    EnumShape.ROOF_SMART_VALLEY, EnumShape.ROOF_OVERHANG, EnumShape.ROOF_OVERHANG_OUTER_CORNER,
                    EnumShape.ROOF_OVERHANG_INNER_CORNER, EnumShape.ROOF_OVERHANG_GABLE_LH, EnumShape.ROOF_OVERHANG_GABLE_RH,
                    EnumShape.ROOF_OVERHANG_GABLE_END_LH, EnumShape.ROOF_OVERHANG_GABLE_END_RH, EnumShape.ROOF_OVERHANG_RIDGE,
                    EnumShape.ROOF_OVERHANG_VALLEY, EnumShape.BEVELLED_OUTER_CORNER, EnumShape.BEVELLED_INNER_CORNER),
            new ShapePage("rounded",
                    EnumShape.CYLINDER, EnumShape.CYLINDER_HALF, EnumShape.CYLINDER_QUARTER, EnumShape.CYLINDER_LARGE_QUARTER, EnumShape.ANTICYLINDER_LARGE_QUARTER,
                    EnumShape.PILLAR, EnumShape.POST, EnumShape.POLE, EnumShape.SPHERE_FULL, EnumShape.SPHERE_HALF,
                    EnumShape.SPHERE_QUARTER, EnumShape.SPHERE_EIGHTH, EnumShape.SPHERE_EIGHTH_LARGE, EnumShape.SPHERE_EIGHTH_LARGE_REV),
            new ShapePage("classical",
                    EnumShape.PILLAR_BASE, EnumShape.PILLAR, EnumShape.DORIC_CAPITAL, EnumShape.DORIC_TRIGLYPH, EnumShape.DORIC_TRIGLYPH_CORNER, EnumShape.DORIC_METOPE,
                    EnumShape.IONIC_CAPITAL, EnumShape.CORINTHIAN_CAPITAL, EnumShape.ARCHITRAVE, EnumShape.ARCHITRAVE_CORNER, EnumShape.CORNICE_LH, EnumShape.CORNICE_RH,
                    EnumShape.CORNICE_END_LH, EnumShape.CORNICE_END_RH, EnumShape.CORNICE_RIDGE, EnumShape.CORNICE_VALLEY, EnumShape.CORNICE_BOTTOM),
            new ShapePage("window",
                    EnumShape.WINDOW_FRAME, EnumShape.WINDOW_CORNER, EnumShape.WINDOW_MULLION),
            new ShapePage("arches",
                    EnumShape.ARCH_D_1, EnumShape.ARCH_D_2, EnumShape.ARCH_D_3_A, EnumShape.ARCH_D_3_B, EnumShape.ARCH_D_3_C, EnumShape.ARCH_D_4_A, EnumShape.ARCH_D_4_B, EnumShape.ARCH_D_4_C),
            new ShapePage("railings",
                    EnumShape.BALUSTRADE_PLAIN, EnumShape.BALUSTRADE_PLAIN_OUTER_CORNER, EnumShape.BALUSTRADE_PLAIN_INNER_CORNER,
                    EnumShape.BALUSTRADE_PLAIN_WITH_NEWEL, EnumShape.BALUSTRADE_PLAIN_END,
                    EnumShape.BANISTER_PLAIN_TOP, EnumShape.BANISTER_PLAIN, EnumShape.BANISTER_PLAIN_BOTTOM, EnumShape.BANISTER_PLAIN_END, EnumShape.BANISTER_PLAIN_INNER_CORNER,
                    EnumShape.BALUSTRADE_FANCY, EnumShape.BALUSTRADE_FANCY_CORNER, EnumShape.BALUSTRADE_FANCY_WITH_NEWEL, EnumShape.BALUSTRADE_FANCY_NEWEL,
                    EnumShape.BANISTER_FANCY_TOP, EnumShape.BANISTER_FANCY, EnumShape.BANISTER_FANCY_BOTTOM, EnumShape.BANISTER_FANCY_END, EnumShape.BANISTER_FANCY_NEWEL_TALL),
            new ShapePage("other",
                    EnumShape.CLADDING_SHEET, EnumShape.SLAB, EnumShape.STAIRS, EnumShape.STAIRS_OUTER_CORNER, EnumShape.STAIRS_INNER_CORNER),
    };

    public IInventory inventory = new InventoryBasic("Items", false, 2);
    public int selectedPage = 0;
    public int[] selectedSlots = new int[pages.length];
    public boolean pendingMaterialUsage = false; // Material for the stack in the result slot
    // has not yet been removed from the material slot

    public EnumShape getSelectedShape() {
        if (this.selectedPage >= 0 && this.selectedPage < pages.length) {
            int slot = this.selectedSlots[this.selectedPage];
            if (slot >= 0 && slot < pages[this.selectedPage].size())
                return pages[this.selectedPage].get(slot);
        }
        return null;
    }

    @Override
    protected IInventory getInventory() {
        return this.inventory;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack stack) {
        super.setInventorySlotContents(i, stack);
        this.updateResultSlot();
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (slot == resultSlot)
            this.usePendingMaterial();
        ItemStack result = super.decrStackSize(slot, amount);
        this.updateResultSlot();
        return result;
    }

    public ItemStack usePendingMaterial() {
        ItemStack origMaterialStack = this.getStackInSlot(materialSlot).copy();
        if (this.pendingMaterialUsage) {
            this.pendingMaterialUsage = false;
            this.inventory.decrStackSize(materialSlot, this.materialMultiple());
        }
        return origMaterialStack;
    }

    public void returnUnusedMaterial(ItemStack origMaterialStack) {
        if (!this.pendingMaterialUsage) {
            ItemStack materialStack = this.getStackInSlot(materialSlot);
            ItemStack resultStack = this.getStackInSlot(resultSlot);
            int m = this.materialMultiple();
            int n = this.resultMultiple();
            if (!resultStack.isEmpty() && resultStack.getCount() == n) {
                if (!materialStack.isEmpty())
                    materialStack.grow(m);
                else {
                    materialStack = origMaterialStack;
                    materialStack.setCount(m);
                }
                this.inventory.setInventorySlotContents(materialSlot, materialStack);
                this.pendingMaterialUsage = true;
            }
        }
    }

    /**
     * Returns an array containing the indices of the slots that can be accessed by automation on the given side of this
     * block.
     */
    public int[] getAccessibleSlotsFromSide(int side) {
        if (side == 1) // UP
            return materialSideSlots;
        else
            return resultSideSlots;
    }

    @Override
    public void onAddedToWorld() {
        // NO-OP
    }

    @Override
    public void readFromNBT(NBTTagCompound tc) {
        super.readFromNBT(tc);
        this.selectedPage = tc.getInteger("Page");
        int[] ss = tc.getIntArray("Slots");
        if (ss != null)
            for (int page = 0; page < pages.length; page++) {
                int slot = page < ss.length ? ss[page] : 0;
                this.selectedSlots[page] = slot >= 0 && slot < pages[page].size() ? slot : 0;
            }
        this.pendingMaterialUsage = tc.getBoolean("PMU");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tc) {
        super.writeToNBT(tc);
        tc.setInteger("Page", this.selectedPage);
        tc.setIntArray("Slots", this.selectedSlots);
        tc.setBoolean("PMU", this.pendingMaterialUsage);
        return tc;
    }

    public void setSelectedShape(int page, int slot) {
        if (page >= 0 && page < pages.length) {
            this.selectedPage = page;
            if (slot >= 0 && slot < pages[this.selectedPage].size()) {
                this.selectedSlots[this.selectedPage] = slot;
                this.markDirty();
                this.updateResultSlot();
                this.sendTileEntityUpdate();
            }
        }
    }

    void updateResultSlot() {
        ItemStack oldResult = this.getStackInSlot(resultSlot).copy();
        if (oldResult.isEmpty() || this.pendingMaterialUsage) {
            ItemStack resultStack = this.makeResultStack();
            if (!ItemStack.areItemStacksEqual(resultStack, oldResult))
                this.inventory.setInventorySlotContents(resultSlot, resultStack);
            this.pendingMaterialUsage = !resultStack.isEmpty();
        }
    }

    protected ItemStack makeResultStack() {
        EnumShape resultShape = this.getSelectedShape();
        if (resultShape != null) {
            ItemStack materialStack = this.getStackInSlot(materialSlot);
            if (!materialStack.isEmpty() && materialStack.getCount() >= resultShape.materialUsed) {
                Item materialItem = materialStack.getItem();
                if (materialItem instanceof ItemBlock) {
                    Block materialBlock = Block.getBlockFromItem(materialItem);
                    if (this.isAcceptableMaterial(materialBlock)) {
                        return resultShape.kind.newStack(resultShape, materialBlock,
                                materialStack.getItemDamage(), resultShape.itemsProduced);
                    }
                }
            }
        }
        return ItemStack.EMPTY;
    }

    protected boolean isAcceptableMaterial(Block block) {
        String name = Block.REGISTRY.getNameForObject(block).toString();
        if (block == Blocks.GLASS || block == Blocks.STAINED_GLASS || block instanceof BlockSlab ||
                name.startsWith("chisel:glass"))
            return true;
        return block.getDefaultState().isFullCube() && !block.hasTileEntity();
    }

    public int materialMultiple() {
        int factor = 1;
        ItemStack materialStack = this.getStackInSlot(materialSlot);
        if (!materialStack.isEmpty()) {
            Block materialBlock = Block.getBlockFromItem(materialStack.getItem());
            if (materialBlock instanceof BlockSlab)
                factor = 2;
        }
        EnumShape shape = this.getSelectedShape();
        if (shape != null)
            return factor * shape.materialUsed;
        return 0;
    }

    public int resultMultiple() {
        EnumShape shape = this.getSelectedShape();
        if (shape != null)
            return shape.itemsProduced;
        return 0;
    }

    /**
     * Returns true if automation can insert the given item in the given slot from the given side. Args: Slot, item,
     * side
     */
    @Override
    public boolean canInsertItem(int slot, ItemStack stack, EnumFacing side) {
        return slot == materialSlot;
    }

    /**
     * Returns true if automation can extract the given item in the given slot from the given side. Args: Slot, item,
     * side
     */
    @Override
    public boolean canExtractItem(int slot, ItemStack stack, EnumFacing side) {
        if (side == EnumFacing.DOWN)
            return allowAutomation && slot == resultSlot;
        else
            return slot == materialSlot;
    }

}

