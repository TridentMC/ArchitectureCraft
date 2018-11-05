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

import com.elytradev.architecture.common.shape.Shape;
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
            new ShapePage("Roofing",
                    Shape.ROOF_TILE, Shape.ROOF_OUTER_CORNER, Shape.ROOF_INNER_CORNER,
                    Shape.ROOF_RIDGE, Shape.ROOF_SMART_RIDGE, Shape.ROOF_VALLEY,
                    Shape.ROOF_SMART_VALLEY, Shape.ROOF_OVERHANG, Shape.ROOF_OVERHANG_OUTER_CORNER,
                    Shape.ROOF_OVERHANG_INNER_CORNER, Shape.ROOF_OVERHANG_GABLE_LH, Shape.ROOF_OVERHANG_GABLE_RH,
                    Shape.ROOF_OVERHANG_GABLE_END_LH, Shape.ROOF_OVERHANG_GABLE_END_RH, Shape.ROOF_OVERHANG_RIDGE,
                    Shape.ROOF_OVERHANG_VALLEY, Shape.BEVELLED_OUTER_CORNER, Shape.BEVELLED_INNER_CORNER),
            new ShapePage("Rounded",
                    Shape.CYLINDER, Shape.CYLINDER_HALF, Shape.CYLINDER_QUARTER, Shape.CYLINDER_LARGE_QUARTER, Shape.ANTICYLINDER_LARGE_QUARTER,
                    Shape.PILLAR, Shape.POST, Shape.POLE, Shape.SPHERE_FULL, Shape.SPHERE_HALF,
                    Shape.SPHERE_QUARTER, Shape.SPHERE_EIGHTH, Shape.SPHERE_EIGHTH_LARGE, Shape.SPHERE_EIGHTH_LARGE_REV),
            new ShapePage("Classical",
                    Shape.PILLAR_BASE, Shape.PILLAR, Shape.DORIC_CAPITAL, Shape.DORIC_TRIGLYPH, Shape.DORIC_TRIGLYPH_CORNER, Shape.DORIC_METOPE,
                    Shape.IONIC_CAPITAL, Shape.CORINTHIAN_CAPITAL, Shape.ARCHITRAVE, Shape.ARCHITRAVE_CORNER, Shape.CORNICE_LH, Shape.CORNICE_RH,
                    Shape.CORNICE_END_LH, Shape.CORNICE_END_RH, Shape.CORNICE_RIDGE, Shape.CORNICE_VALLEY, Shape.CORNICE_BOTTOM),
            new ShapePage("Window",
                    Shape.WINDOW_FRAME, Shape.WINDOW_CORNER, Shape.WINDOW_MULLION),
            new ShapePage("Arches",
                    Shape.ARCH_D_1, Shape.ARCH_D_2, Shape.ARCH_D_3_A, Shape.ARCH_D_3_B, Shape.ARCH_D_3_C, Shape.ARCH_D_4_A, Shape.ARCH_D_4_B, Shape.ARCH_D_4_C),
            new ShapePage("Railings",
                    Shape.BALUSTRADE_PLAIN, Shape.BALUSTRADE_PLAIN_OUTER_CORNER, Shape.BALUSTRADE_PLAIN_INNER_CORNER,
                    Shape.BALUSTRADE_PLAIN_WITH_NEWEL, Shape.BALUSTRADE_PLAIN_END,
                    Shape.BANISTER_PLAIN_TOP, Shape.BANISTER_PLAIN, Shape.BANISTER_PLAIN_BOTTOM, Shape.BANISTER_PLAIN_END, Shape.BANISTER_PLAIN_INNER_CORNER,
                    Shape.BALUSTRADE_FANCY, Shape.BALUSTRADE_FANCY_CORNER, Shape.BALUSTRADE_FANCY_WITH_NEWEL, Shape.BALUSTRADE_FANCY_NEWEL,
                    Shape.BANISTER_FANCY_TOP, Shape.BANISTER_FANCY, Shape.BANISTER_FANCY_BOTTOM, Shape.BANISTER_FANCY_END, Shape.BANISTER_FANCY_NEWEL_TALL),
            new ShapePage("Other",
                    Shape.CLADDING_SHEET, Shape.SLAB, Shape.STAIRS, Shape.STAIRS_OUTER_CORNER, Shape.STAIRS_INNER_CORNER),
    };

    public IInventory inventory = new InventoryBasic("Items", false, 2);
    public int selectedPage = 0;
    public int[] selectedSlots = new int[pages.length];
    public boolean pendingMaterialUsage = false; // Material for the stack in the result slot
    // has not yet been removed from the material slot

    public Shape getSelectedShape() {
        if (selectedPage >= 0 && selectedPage < pages.length) {
            int slot = selectedSlots[selectedPage];
            if (slot >= 0 && slot < pages[selectedPage].size())
                return pages[selectedPage].get(slot);
        }
        return null;
    }

    @Override
    protected IInventory getInventory() {
        return inventory;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack stack) {
        super.setInventorySlotContents(i, stack);
        updateResultSlot();
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (slot == resultSlot)
            usePendingMaterial();
        ItemStack result = super.decrStackSize(slot, amount);
        updateResultSlot();
        return result;
    }

    public ItemStack usePendingMaterial() {
        ItemStack origMaterialStack = getStackInSlot(materialSlot).copy();
        if (pendingMaterialUsage) {
            pendingMaterialUsage = false;
            inventory.decrStackSize(materialSlot, materialMultiple());
        }
        return origMaterialStack;
    }

    public void returnUnusedMaterial(ItemStack origMaterialStack) {
        if (!pendingMaterialUsage) {
            ItemStack materialStack = getStackInSlot(materialSlot);
            ItemStack resultStack = getStackInSlot(resultSlot);
            int m = materialMultiple();
            int n = resultMultiple();
            if (!resultStack.isEmpty() && resultStack.getCount() == n) {
                if (!materialStack.isEmpty())
                    materialStack.grow(m);
                else {
                    materialStack = origMaterialStack;
                    materialStack.setCount(m);
                }
                inventory.setInventorySlotContents(materialSlot, materialStack);
                pendingMaterialUsage = true;
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
        selectedPage = tc.getInteger("Page");
        int[] ss = tc.getIntArray("Slots");
        if (ss != null)
            for (int page = 0; page < pages.length; page++) {
                int slot = page < ss.length ? ss[page] : 0;
                selectedSlots[page] = slot >= 0 && slot < pages[page].size() ? slot : 0;
            }
        pendingMaterialUsage = tc.getBoolean("PMU");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tc) {
        super.writeToNBT(tc);
        tc.setInteger("Page", selectedPage);
        tc.setIntArray("Slots", selectedSlots);
        tc.setBoolean("PMU", pendingMaterialUsage);
        return tc;
    }

    public void setSelectedShape(int page, int slot) {
        if (page >= 0 && page < pages.length) {
            selectedPage = page;
            if (slot >= 0 && slot < pages[selectedPage].size()) {
                selectedSlots[selectedPage] = slot;
                markDirty();
                updateResultSlot();
                sendTileEntityUpdate();
            }
        }
    }

    void updateResultSlot() {
        ItemStack oldResult = getStackInSlot(resultSlot).copy();
        if (oldResult.isEmpty() || pendingMaterialUsage) {
            ItemStack resultStack = makeResultStack();
            if (!ItemStack.areItemStacksEqual(resultStack, oldResult))
                inventory.setInventorySlotContents(resultSlot, resultStack);
            pendingMaterialUsage = !resultStack.isEmpty();
        }
    }

    protected ItemStack makeResultStack() {
        Shape resultShape = getSelectedShape();
        if (resultShape != null) {
            ItemStack materialStack = getStackInSlot(materialSlot);
            if (!materialStack.isEmpty() && materialStack.getCount() >= resultShape.materialUsed) {
                Item materialItem = materialStack.getItem();
                if (materialItem instanceof ItemBlock) {
                    Block materialBlock = Block.getBlockFromItem(materialItem);
                    if (isAcceptableMaterial(materialBlock)) {
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
        
        // Tinker Compat
        // Controllers exclusion
        if (name.startsWith("tconstruct:") && name.contains("controller"))
            return false;
        // Get all full blocks
        if (name.startsWith("tconstruct:") && block.getDefaultState().isFullCube())
            return true;
        // Add glass (non FullCube)
        if (name.startsWith("tconstruct:") && name.contains("glass"))
            return true;
        
        return block.getDefaultState().isFullCube() && !block.hasTileEntity();
    }

    public int materialMultiple() {
        int factor = 1;
        ItemStack materialStack = getStackInSlot(materialSlot);
        if (!materialStack.isEmpty()) {
            Block materialBlock = Block.getBlockFromItem(materialStack.getItem());
            if (materialBlock instanceof BlockSlab)
                factor = 2;
        }
        Shape shape = getSelectedShape();
        if (shape != null)
            return factor * shape.materialUsed;
        return 0;
    }

    public int resultMultiple() {
        Shape shape = getSelectedShape();
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

