package com.tridevmc.architecture.common.block.state;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.MapCodec;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.core.ArchitectureLog;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.ITrans3Immutable;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public class BlockStateArchitecture extends BlockState {

    private final ITrans3Immutable cachedTransform;
    private final CompletableFuture<VoxelShape> cachedShape;

    public BlockStateArchitecture(BlockArchitecture block,
                                  ImmutableMap<Property<?>, Comparable<?>> properties,
                                  MapCodec<BlockState> codec) {
        super(block, properties, codec);
        this.cachedTransform = block.getTransformForState(this);
        var boxesFuture = block.getBoxesForState(this);
        this.cachedShape = boxesFuture.thenApply(boxes -> {
            var shape = Shapes.empty();
            for (var box : boxes) {
                shape = Shapes.or(shape, Shapes.create(box.toMC()));
            }
            ArchitectureLog.debug("Finished creating shape for state: {}", this.toString());
            return shape;
        });
    }

    @NotNull
    public ITrans3 getTransform() {
        return this.cachedTransform;
    }

    @NotNull
    public VoxelShape getShape() {
        return this.cachedShape.join();
    }


}
