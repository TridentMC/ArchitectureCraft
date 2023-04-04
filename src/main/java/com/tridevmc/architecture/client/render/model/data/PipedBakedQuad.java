package com.tridevmc.architecture.client.render.model.data;

import com.google.common.collect.ImmutableList;
import com.mojang.math.Transformation;
import net.minecraft.core.Direction;
import org.joml.Vector3f;

public record PipedBakedQuad<V extends IPipedVertex<V, PipedBakedQuad<V, D>, D>, D>(
        ImmutableList<V> vertices,
        float nX, float nY, float nZ,
        float minX, float minY, float minZ,
        float maxX, float maxY, float maxZ,
        Direction face,
        D metadata

) implements IPipedBakedQuad<PipedBakedQuad<V, D>, V, D> {

    public PipedBakedQuad(ImmutableList<V> vertices, Vector3f normal, Vector3f min, Vector3f max, Direction face, D metadata) {
        this(vertices, normal.x(), normal.y(), normal.z(), min.x(), min.y(), min.z(), max.x(), max.y(), max.z(), face, metadata);
    }

    @Override
    public PipedBakedQuad<V, D> transform(Transformation transform) {
        var normal = this.normal(transform);
        var min = this.min(transform);
        var max = this.max(transform);
        var face = this.face(transform);
        return new PipedBakedQuad<>(
                this.vertices()
                        .stream()
                        .map(v -> v.transform(this, transform))
                        .collect(ImmutableList.toImmutableList()),
                normal, min, max, face, this.metadata()
        );
    }

}
