package com.tridevmc.architecture.client.render.model.piped;

import com.tridevmc.architecture.core.math.ITrans3;
import org.jetbrains.annotations.NotNull;

public record PipedVertex<Q extends IPipedBakedQuad<Q, PipedVertex<Q, D>, D>, D>(
        double x, double y, double z,
        float nX, float nY, float nZ,
        float u, float v
) implements IPipedVertex<PipedVertex<Q, D>, Q, D> {

    @Override
    public PipedVertex<Q, D> transform(@NotNull Q quadProvider, @NotNull ITrans3 trans) {
        if (trans.isIdentity()) {
            return this;
        } else {
            var fromFace = quadProvider.face();
            var toFace = quadProvider.face(trans);
            var pos = this.pos(trans);
            var normal = this.normal(trans);
            var uvs = this.uvs(trans, fromFace, toFace);
            return new PipedVertex<>(
                    (float) pos.x(), (float) pos.y(), (float) pos.z(),
                    normal.x(), normal.y(), normal.z(),
                    (float) uvs.u(), (float) uvs.v()
            );
        }
    }

}
