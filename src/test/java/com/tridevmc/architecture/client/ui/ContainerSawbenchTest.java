package com.tridevmc.architecture.client.ui;

import com.tridevmc.architecture.common.block.container.ContainerSawbench;
import com.tridevmc.architecture.common.item.ItemShape;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.testsupport.MinecraftTestBootstrap;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MinecraftTestBootstrap.class)
class ContainerSawbenchTest {
    @Test
    void selectionSyncAndTakingResultConsumeOneMaterial() {
        var inventory = inventory();
        var player = mock(ServerPlayer.class);
        var menu = new ContainerSawbench(inventory, 1);
        assertNull(menu.getSelectedShape());
        assertFalse(menu.clickMenuButton(player, -1));
        assertFalse(menu.clickMenuButton(player, EnumShape.values().length));
        menu.setData(0, EnumShape.SLAB.ordinal());
        assertEquals(EnumShape.SLAB, menu.getSelectedShape());
        try (var shapes = mockStatic(ItemShape.class)) {
            shapes.when(() -> ItemShape.createStack(EnumShape.SLAB, Blocks.STONE.defaultBlockState(), 1))
                    .thenAnswer(call -> new ItemStack(Items.STONE_SLAB));
            menu.getSlot(0).set(new ItemStack(Items.STONE, 3));
            assertTrue(menu.getSlot(1).getItem().is(Items.STONE_SLAB));
            var taken = menu.getSlot(1).remove(1);
            menu.getSlot(1).onTake(player, taken);
            assertEquals(2, menu.getInputStack().getCount());
            assertEquals(1, menu.getSlot(1).getItem().getCount());
            assertTrue(menu.quickMoveStack(player, -1).isEmpty());
            assertTrue(menu.quickMoveStack(player, 38).isEmpty());
        }
    }

    @Test
    void closingReturnsUnspentInputWithoutGrantingTheResultPreview() {
        var inventory = inventory();
        var player = mock(ServerPlayer.class);
        when(player.getInventory()).thenReturn(inventory);
        var menu = new ContainerSawbench(inventory, 1,
                ContainerLevelAccess.create(mock(Level.class), BlockPos.ZERO));
        try (var shapes = mockStatic(ItemShape.class)) {
            shapes.when(() -> ItemShape.createStack(EnumShape.SLAB, Blocks.STONE.defaultBlockState(), 1))
                    .thenAnswer(call -> new ItemStack(Items.STONE_SLAB));
            menu.selectShape(EnumShape.SLAB);
            menu.getSlot(0).set(new ItemStack(Items.STONE, 3));
            menu.removed(player);
            var returned = ArgumentCaptor.forClass(ItemStack.class);
            verify(inventory, atLeastOnce()).placeItemBackInInventory(returned.capture(), eq(Prediction.SERVER_ONLY));
            var nonempty = returned.getAllValues().stream().filter(stack -> !stack.isEmpty()).toList();
            assertEquals(1, nonempty.size());
            assertTrue(nonempty.getFirst().is(Items.STONE));
            assertEquals(3, nonempty.getFirst().getCount());
            assertTrue(menu.getSlot(0).getItem().isEmpty());
            assertTrue(menu.getSlot(1).getItem().isEmpty());
        }
    }

    private static Inventory inventory() {
        var inventory = mock(Inventory.class);
        when(inventory.getContainerSize()).thenReturn(41);
        when(inventory.getItem(anyInt())).thenReturn(ItemStack.EMPTY);
        return inventory;
    }
}
