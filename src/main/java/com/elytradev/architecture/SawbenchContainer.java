//------------------------------------------------------------------------------
//
//   ArchitectureCraft - SawbenchContainer
//
//------------------------------------------------------------------------------

package com.elytradev.architecture;

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

public class SawbenchContainer extends BaseContainer {

    public static int guWidth = 242;
    public static int guiHeight = 224;
    public static int inputSlotLeft = 12;
    public static int inputSlotTop = 19;
    public static int outputSlotLeft = 12;
    public static int outputSlotTop = 57;

    SawbenchTE te;
    SlotRange sawbenchSlotRange;
    Slot materialSlot, resultSlot;

    public SawbenchContainer(EntityPlayer player, SawbenchTE te) {
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
        if (te instanceof SawbenchTE)
            return new SawbenchContainer(player, (SawbenchTE) te);
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

    //
    //   Server
    //

    @Override
    public void detectAndSendChanges() {
        for (int i = 0; i < this.inventorySlots.size(); ++i) {
            ItemStack newstack = ((Slot) this.inventorySlots.get(i)).getStack();
            ItemStack oldstack = (ItemStack) this.inventoryItemStacks.get(i);
            if (!ItemStack.areItemStacksEqual(oldstack, newstack)) {
                oldstack = newstack == null ? null : newstack.copy();
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
            //System.out.printf("SawbenchContainer.putStackInSlot: %d %s on %s\n", i, stack, te.worldObj);
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
        //if (!te.getWorld().isRemote)
        //    System.out.printf("SawbenchContainer.transferStackInSlot: %s material %s result %s\n",
        //        index, te.getStackInSlot(te.materialSlot), te.getStackInSlot(te.resultSlot));
        boolean materialWasPending = te.pendingMaterialUsage;
        ItemStack origMaterialStack = te.usePendingMaterial();
        ItemStack result = super.transferStackInSlot(player, index);
        //if (!te.getWorld().isRemote)
        //    System.out.printf(
        //        "SawbenchContainer.transferStackInSlot: returning %s material %s result %s\n",
        //        result, te.getStackInSlot(te.materialSlot), te.getStackInSlot(te.resultSlot));
        if (materialWasPending)
            te.returnUnusedMaterial(origMaterialStack);
        return result;
    }

//	@Override
//	public void updateProgressBar(int i, int value) {
//		//System.out.printf("SawbenchContainer.updateProgressBar: %d %d\n", i, value);
//		switch (i) {
//			case 0: te.selectedPage = value;
//			case 1: te.selectedSlots[selectedPage] = value;
//			case 2: te.pendingMaterialUsage = value != 0;
//		}
//	}

}

//------------------------------------------------------------------------------

class SlotSawbench extends Slot {

    SawbenchTE te;
    int index;

    public SlotSawbench(SawbenchTE te, int index, int x, int y) {
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

    public SlotSawbenchResult(SawbenchTE te, int index, int x, int y) {
        super(te, index, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack newstack) {
        return false;
    }

}

	