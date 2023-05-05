package com.tridevmc.architecture.client.render.model.piped;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.IVector3;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class PipedBakedQuadTransformable<D> extends PipedBakedQuad<PipedBakedQuadTransformable<D>, PipedVertexTransformable<D>, D> implements ITransformable<PipedBakedQuadTransformable<D>> {

    public PipedBakedQuadTransformable(ImmutableList<PipedVertexTransformable<D>> vertices, float nX, float nY, float nZ, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Direction face, boolean shouldCull, D metadata) {
        super(vertices, nX, nY, nZ, minX, minY, minZ, maxX, maxY, maxZ, face, shouldCull, metadata);
    }

    public PipedBakedQuadTransformable(ImmutableList<PipedVertexTransformable<D>> vertices, IVector3 normal, IVector3 min, IVector3 max, Direction face, boolean shouldCull, D metadata) {
        super(vertices, normal, min, max, face, shouldCull, metadata);
    }

    @Override
    public PipedBakedQuadTransformable<D> transform(ITrans3 trans, @Nullable Direction fromFace, @Nullable Direction toFace, boolean transformUVs) {
        if (trans.isIdentity()) {
            return this;
        } else {
            var normal = this.normal(trans);
            var min = this.min(trans);
            var max = this.max(trans);
            var face = this.face(trans);
            return new PipedBakedQuadTransformable<>(
                    this.vertices()
                            .stream()
                            .map(v -> v.transform(
                                    trans, fromFace, toFace, transformUVs
                            ))
                            .collect(ImmutableList.toImmutableList()),
                    normal, min, max, face, this.shouldCull(), this.metadata()
            );
        }
    }

}
