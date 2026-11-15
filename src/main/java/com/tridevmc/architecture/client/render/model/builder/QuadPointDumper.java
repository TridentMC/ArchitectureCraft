package com.tridevmc.architecture.client.render.model.builder;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.phys.Vec3;

import java.util.stream.IntStream;

public class QuadPointDumper {

    private final ImmutableList<Vec3> immutablePoints;
    public QuadPointDumper(BakedQuad quad) {
        this.immutablePoints = IntStream.range(0, 4).mapToObj(i -> {
            var p = quad.position(i);
            return new Vec3(p.x(), p.y(), p.z());
        }).collect(ImmutableList.toImmutableList());
    }

    public ImmutableList<Vec3> getPoints() {
        return this.immutablePoints;
    }
}
