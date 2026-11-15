package com.tridevmc.architecture.common.block.state;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
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

    private volatile CachedProperties cachedProperties;

    private record CachedProperties(
            Supplier<ITrans3Immutable> cachedTransform,
            Supplier<CompletableFuture<VoxelShape>> cachedShape
    ) {
    }

    protected BlockStateArchitecture(BlockArchitecture block,
                                     Property<?>[] propertyKeys, Comparable<?>[] propertyValues) {
        super(block, propertyKeys, propertyValues);
    }

    public static BlockStateArchitecture create(BlockArchitecture block,
                                                Property<?>[] propertyKeys, Comparable<?>[] propertyValues) {
        var state = new BlockStateArchitecture(block, propertyKeys, propertyValues);
        state.postConstruct();
        return state;
    }

    protected void postConstruct() {
        this.cachedProperties = new CachedProperties(
                Suppliers.memoize(() -> this.self().getTransformForState(this)),
                Suppliers.memoize(() -> this.self().getBoxesForState(this).thenApply(boxes -> {
                    var shape = Shapes.empty();
                    for (var box : boxes) {
                        shape = Shapes.or(shape, Shapes.create(box.toMC()));
                    }
                    ArchitectureLog.debug("Finished creating shape for state: {}", this.toString());
                    return shape;
                }))
        );
    }

    private BlockArchitecture self() {
        return (BlockArchitecture) this.getBlock();
    }

    @NotNull
    public ITrans3 getTransform() {
        return this.cachedProperties.cachedTransform.get();
    }

    @NotNull
    public VoxelShape getShape() {
        return this.cachedProperties.cachedShape.get().join();
    }


}
