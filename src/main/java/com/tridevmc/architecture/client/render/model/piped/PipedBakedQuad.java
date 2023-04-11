package com.tridevmc.architecture.client.render.model.piped;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.IVector3;
import net.minecraft.core.Direction;

public record PipedBakedQuad<V extends IPipedVertex<V, PipedBakedQuad<V, D>, D>, D>(
        ImmutableList<V> vertices,
        float nX, float nY, float nZ,
        float minX, float minY, float minZ,
        float maxX, float maxY, float maxZ,
        Direction face,
        boolean shouldCull,
        D metadata

) implements IPipedBakedQuad<PipedBakedQuad<V, D>, V, D> {

    public PipedBakedQuad(ImmutableList<V> vertices, IVector3 normal, IVector3 min, IVector3 max, Direction face, boolean shouldCull, D metadata) {
        this(vertices, (float) normal.x(), (float) normal.y(), (float) normal.z(),
                (float) min.x(), (float) min.y(), (float) min.z(),
                (float) max.x(), (float) max.y(), (float) max.z(),
                face, shouldCull, metadata);
    }

    @Override
    public PipedBakedQuad<V, D> transform(ITrans3 transform) {
        var normal = this.normal(transform);
        var min = this.min(transform);
        var max = this.max(transform);
        var face = this.face(transform);
        return new PipedBakedQuad<>(
                this.vertices()
                        .stream()
                        .map(v -> v.transform(this, transform))
                        .collect(ImmutableList.toImmutableList()),
                normal, min, max, face, this.shouldCull(), this.metadata()
        );
    }

}
