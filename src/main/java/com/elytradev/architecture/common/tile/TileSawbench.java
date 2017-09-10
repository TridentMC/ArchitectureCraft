//------------------------------------------------------------------------------
//
//   ArchitectureCraft - Sawbench Tile Entity
//
//------------------------------------------------------------------------------

package com.elytradev.architecture.common.tile;

import com.elytradev.architecture.base.BaseMod;
import com.elytradev.architecture.base.BaseTileInventory;
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

public class TileSawbench extends BaseTileInventory {

    final public static int materialSlot = 0;
    final public static int resultSlot = 1;

    final public static int[] materialSideSlots = {materialSlot};
    final public static int[] resultSideSlots = {resultSlot};

    public static boolean allowAutomation = false;

    public static ShapePage[] pages = {
            new ShapePage("Roofing",
                    Shape.RoofTile, Shape.RoofOuterCorner, Shape.RoofInnerCorner,
                    Shape.RoofRidge, Shape.RoofSmartRidge, Shape.RoofValley,
                    Shape.RoofSmartValley, Shape.RoofOverhang, Shape.RoofOverhangOuterCorner,
                    Shape.RoofOverhangInnerCorner, Shape.RoofOverhangGableLH, Shape.RoofOverhangGableRH,
                    Shape.RoofOverhangGableEndLH, Shape.RoofOverhangGableEndRH, Shape.RoofOverhangRidge,
                    Shape.RoofOverhangValley, Shape.BevelledOuterCorner, Shape.BevelledInnerCorner),
            new ShapePage("Rounded",
                    Shape.Cylinder, Shape.CylinderHalf, Shape.CylinderQuarter, Shape.CylinderLargeQuarter, Shape.AnticylinderLargeQuarter,
                    Shape.Pillar, Shape.Post, Shape.Pole, Shape.SphereFull, Shape.SphereHalf,
                    Shape.SphereQuarter, Shape.SphereEighth, Shape.SphereEighthLarge, Shape.SphereEighthLargeRev),
            new ShapePage("Classical",
                    Shape.PillarBase, Shape.Pillar, Shape.DoricCapital, Shape.DoricTriglyph, Shape.DoricTriglyphCorner, Shape.DoricMetope,
                    Shape.IonicCapital, Shape.CorinthianCapital, Shape.Architrave, Shape.ArchitraveCorner, Shape.CorniceLH, Shape.CorniceRH,
                    Shape.CorniceEndLH, Shape.CorniceEndRH, Shape.CorniceRidge, Shape.CorniceValley, Shape.CorniceBottom),
            new ShapePage("Window",
                    Shape.WindowFrame, Shape.WindowCorner, Shape.WindowMullion),
            new ShapePage("Arches",
                    Shape.ArchD1, Shape.ArchD2, Shape.ArchD3A, Shape.ArchD3B, Shape.ArchD3C, Shape.ArchD4A, Shape.ArchD4B, Shape.ArchD4C),
            new ShapePage("Railings",
                    Shape.BalustradePlain, Shape.BalustradePlainOuterCorner, Shape.BalustradePlainInnerCorner,
                    Shape.BalustradePlainWithNewel, Shape.BalustradePlainEnd,
                    Shape.BanisterPlainTop, Shape.BanisterPlain, Shape.BanisterPlainBottom, Shape.BanisterPlainEnd, Shape.BanisterPlainInnerCorner,
                    Shape.BalustradeFancy, Shape.BalustradeFancyCorner, Shape.BalustradeFancyWithNewel, Shape.BalustradeFancyNewel,
                    Shape.BanisterFancyTop, Shape.BanisterFancy, Shape.BanisterFancyBottom, Shape.BanisterFancyEnd, Shape.BanisterFancyNewelTall),
            new ShapePage("Other",
                    Shape.CladdingSheet, Shape.Slab, Shape.Stairs, Shape.StairsOuterCorner, Shape.StairsInnerCorner),
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
        //System.out.printf("SawbenchTE.decrStackSize: %d by %d on %s\n", slot, amount, worldObj);
        if (slot == resultSlot)
            usePendingMaterial();
        ItemStack result = super.decrStackSize(slot, amount);
        updateResultSlot();
        return result;
    }

    public ItemStack usePendingMaterial() {
        //System.out.printf("SawbenchTE.usePendingMaterial: pmu = %s on %s\n", pendingMaterialUsage, worldObj);
        ItemStack origMaterialStack = getStackInSlot(materialSlot);
        if (pendingMaterialUsage) {
            pendingMaterialUsage = false;
            inventory.decrStackSize(materialSlot, materialMultiple());
        }
        return origMaterialStack;
    }

    public void returnUnusedMaterial(ItemStack origMaterialStack) {
        //if (!worldObj.isRemote)
        //    System.out.printf("SawbenchTE.returnUnusedMaterial: before: pmu = %s, material = %s, result = %s\n",
        //        pendingMaterialUsage, getStackInSlot(materialSlot), getStackInSlot(resultSlot));
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
        //if (!worldObj.isRemote)
        //    System.out.printf("SawbenchTE.returnUnusedMaterial: after: pmu = %s, material = %s, result = %s\n",
        //        pendingMaterialUsage, getStackInSlot(materialSlot), getStackInSlot(resultSlot));
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
                BaseMod.sendTileEntityUpdate(this);
            }
        }
    }

    void updateResultSlot() {
        //System.out.printf("SawbenchTE.updateResultSlot: pmu = %s on %s\n", pendingMaterialUsage, worldObj);
        ItemStack oldResult = getStackInSlot(resultSlot);
        if (oldResult.isEmpty() || pendingMaterialUsage) {
            ItemStack resultStack = makeResultStack();
            if (!ItemStack.areItemStacksEqual(resultStack, oldResult))
                inventory.setInventorySlotContents(resultSlot, resultStack);
            pendingMaterialUsage = !resultStack.isEmpty();
            //System.out.printf("SawbenchTE.updateResultSlot: now pmu = %s on %s\n", pendingMaterialUsage, worldObj);
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
        //return productMadeForShape[selectedShape];
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

