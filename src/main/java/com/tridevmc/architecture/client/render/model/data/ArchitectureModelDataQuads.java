package com.tridevmc.architecture.client.render.model.data;

import com.google.common.collect.Lists;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

public class ArchitectureModelDataQuads {
    private final List<BakedQuad> northQuads;
    private final List<BakedQuad> southQuads;
    private final List<BakedQuad> eastQuads;
    private final List<BakedQuad> westQuads;
    private final List<BakedQuad> upQuads;
    private final List<BakedQuad> downQuads;
    private final List<BakedQuad> generalQuads;

    public ArchitectureModelDataQuads(ArchitectureModelData.DirectionalQuads directionalQuads, Transformation transform) {
        this.northQuads = Lists.newArrayListWithCapacity(directionalQuads.getQuadCount(transform.rotateTransform(Direction.NORTH)));
        this.southQuads = Lists.newArrayListWithCapacity(directionalQuads.getQuadCount(transform.rotateTransform(Direction.SOUTH)));
        this.eastQuads = Lists.newArrayListWithCapacity(directionalQuads.getQuadCount(transform.rotateTransform(Direction.EAST)));
        this.westQuads = Lists.newArrayListWithCapacity(directionalQuads.getQuadCount(transform.rotateTransform(Direction.WEST)));
        this.upQuads = Lists.newArrayListWithCapacity(directionalQuads.getQuadCount(transform.rotateTransform(Direction.UP)));
        this.downQuads = Lists.newArrayListWithCapacity(directionalQuads.getQuadCount(transform.rotateTransform(Direction.DOWN)));
        this.generalQuads = Lists.newArrayListWithCapacity(directionalQuads.getQuadCount(null));
    }

    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        if (direction == null)
            return this.generalQuads;
        return switch (direction) {
            case NORTH -> this.northQuads;
            case SOUTH -> this.southQuads;
            case EAST -> this.eastQuads;
            case WEST -> this.westQuads;
            case UP -> this.upQuads;
            case DOWN -> this.downQuads;
        };
    }

    private Stream<BakedQuad> getAllQuads() {
        return Stream.of(this.northQuads,
                        this.southQuads,
                        this.eastQuads,
                        this.westQuads,
                        this.upQuads,
                        this.downQuads,
                        this.generalQuads)
                .flatMap(List::stream);
    }

    public void addQuad(Direction direction, BakedQuad quad) {
        this.getQuads(direction).add(quad);
    }
}
