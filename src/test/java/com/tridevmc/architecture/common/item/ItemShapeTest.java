package com.tridevmc.architecture.common.item;

import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.block.entity.BlockEntityShape;
import com.tridevmc.architecture.common.item.component.ComponentMaterial;
import com.tridevmc.architecture.testsupport.MinecraftTestBootstrap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MinecraftTestBootstrap.class)
class ItemShapeTest {
    @Test
    void placingShapePreservesBothMaterialsAndClearsAbsentSecondary() {
        var previousType = ArchitectureMod.CONTENT.blockEntityTypeShape;
        ArchitectureMod.CONTENT.blockEntityTypeShape = new BlockEntityType<>(BlockEntityShape::new, Set.of(Blocks.STONE));
        try {
            var state = Blocks.STONE.defaultBlockState();
            var base = Blocks.OAK_PLANKS.defaultBlockState();
            var secondary = Blocks.GLASS.defaultBlockState();
            var entity = new BlockEntityShape(BlockPos.ZERO, state);
            var level = mock(Level.class);
            when(level.setBlock(BlockPos.ZERO, state, 11)).thenReturn(true);
            when(level.getBlockEntity(BlockPos.ZERO)).thenReturn(entity);
            var stack = mock(ItemStack.class);
            var context = mock(BlockPlaceContext.class);
            when(context.getLevel()).thenReturn(level);
            when(context.getClickedPos()).thenReturn(BlockPos.ZERO);
            when(context.getItemInHand()).thenReturn(stack);
            when(stack.getOrDefault(ArchitectureMod.CONTENT.componentMaterial, ComponentMaterial.DEFAULT))
                    .thenReturn(new ComponentMaterial(base, secondary));
            var item = mock(ItemShape.class, CALLS_REAL_METHODS);
            assertTrue(item.placeBlock(context, state));
            assertSame(base, entity.getEffectiveBaseMaterialState());
            assertSame(secondary, entity.getEffectiveSecondaryMaterialState());
            when(stack.getOrDefault(ArchitectureMod.CONTENT.componentMaterial, ComponentMaterial.DEFAULT))
                    .thenReturn(new ComponentMaterial(base));
            assertTrue(item.placeBlock(context, state));
            assertTrue(entity.getSecondaryMaterialState().isEmpty());
        } finally {
            ArchitectureMod.CONTENT.blockEntityTypeShape = previousType;
        }
    }
}
