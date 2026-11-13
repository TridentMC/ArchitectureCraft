package com.tridevmc.architecture.client.render.model.builder;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.world.phys.Vec3;

public record QuadPointDumper(ImmutableList<Vec3> points) {
    public QuadPointDumper(BakedQuad quad) {
        this(java.util.stream.IntStream.range(0, 4).mapToObj(i -> {
            var p = quad.position(i);
            return new Vec3(p.x(), p.y(), p.z());
        }).collect(ImmutableList.toImmutableList()));
    }

    public ImmutableList<Vec3> getPoints() { return this.points; }
}
