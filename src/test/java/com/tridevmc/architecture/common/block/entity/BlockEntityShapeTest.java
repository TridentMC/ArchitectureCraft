package com.tridevmc.architecture.common.block.entity;

import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.testsupport.MinecraftTestBootstrap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.IdentityHashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MinecraftTestBootstrap.class)
class BlockEntityShapeTest {

    @Test
    void materialsRoundTripNamedStatesAndPublishImmutableModelSnapshots() throws ReflectiveOperationException {
        var previousType = ArchitectureMod.CONTENT.blockEntityTypeShape;
        ArchitectureMod.CONTENT.blockEntityTypeShape = new BlockEntityType<>(BlockEntityShape::new, Set.of(Blocks.STONE));
        try {
            var base = Blocks.OAK_LOG.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Z);
            var secondary = Blocks.OAK_STAIRS.defaultBlockState()
                    .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST)
                    .setValue(BlockStateProperties.WATERLOGGED, true);
            var original = new BlockEntityShape(BlockPos.ZERO, Blocks.STONE.defaultBlockState())
                    .setBaseMaterialState(base).setSecondaryMaterialState(secondary);
            var snapshot = original.getModelData().get(BlockEntityShape.MATERIALS);
            var output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
            original.saveCustomOnly(output);
            var saved = output.buildResult();
            assertEquals("minecraft:oak_log", saved.getCompound("BaseMaterial").orElseThrow()
                    .getString("id").orElseThrow());

            var restored = new BlockEntityShape(BlockPos.ZERO, Blocks.STONE.defaultBlockState());
            restored.loadAdditional(TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, saved));
            assertSame(base, restored.getEffectiveBaseMaterialState());
            assertSame(secondary, restored.getEffectiveSecondaryMaterialState());
            original.setBaseMaterialState(Blocks.BRICKS.defaultBlockState());
            assertSame(base, snapshot.base());
            assertSame(secondary, snapshot.secondary());

            var ids = Block.BLOCK_STATE_REGISTRY;
            var originalIds = new IdentityHashMap<BlockState, Integer>();
            ids.forEach(state -> originalIds.put(state, ids.getId(state)));
            var clear = ids.getClass().getDeclaredMethod("clear");
            clear.setAccessible(true);
            try {
                // Plain JVM tests do not run NeoForge's registry bake that assigns legacy state IDs.
                if (ids.getId(base) < 0) {
                    int nextId = originalIds.values().stream().mapToInt(Integer::intValue).max().orElse(0) + 1;
                    ids.addMapping(base, nextId);
                }
                int legacyId = ids.getId(base);
                assertTrue(legacyId >= 0);
                assertSame(base, Block.stateById(legacyId));
                var legacy = new CompoundTag();
                legacy.putInt("BaseMaterialState", legacyId);
                legacy.putInt("SecondaryMaterialState", -1);
                restored.loadAdditional(TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, legacy));
                assertSame(base, restored.getEffectiveBaseMaterialState());
                assertTrue(restored.getSecondaryMaterialState().isEmpty());
                assertSame(base, restored.getModelData().get(BlockEntityShape.MATERIALS).secondary());
            } finally {
                clear.invoke(ids);
                originalIds.forEach(ids::addMapping);
            }
        } finally {
            ArchitectureMod.CONTENT.blockEntityTypeShape = previousType;
        }
    }
}
