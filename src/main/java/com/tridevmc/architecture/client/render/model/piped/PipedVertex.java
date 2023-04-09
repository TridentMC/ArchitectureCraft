package com.tridevmc.architecture.client.render.model.piped;

import com.mojang.math.Transformation;
import org.jetbrains.annotations.NotNull;

public record PipedVertex<Q extends IPipedBakedQuad<Q, PipedVertex<Q, D>, D>, D>(
        float x, float y, float z,
        float nX, float nY, float nZ,
        float u, float v
) implements IPipedVertex<PipedVertex<Q, D>, Q, D> {

    @Override
    public PipedVertex<Q, D> transform(@NotNull Q quadProvider, @NotNull Transformation transformation) {
        if (transformation.isIdentity()) {
            return this;
        } else {
            var fromFace = quadProvider.face();
            var toFace = quadProvider.face(transformation);
            var pos = this.pos(transformation);
            var normal = this.normal(transformation);
            var uvs = this.uvs(fromFace, toFace);
            return new PipedVertex<>(
                    pos.x(), pos.y(), pos.z(),
                    normal.x(), normal.y(), normal.z(),
                    uvs[0], uvs[1]
            );
        }
    }

}
