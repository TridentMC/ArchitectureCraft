package com.tridevmc.architecture.client.render.model.piped;

import com.tridevmc.architecture.core.math.ITrans3;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class PipedVertexTransformable<D> extends PipedVertex<PipedVertexTransformable<D>, PipedBakedQuadTransformable<D>, D> implements ITransformable<PipedVertexTransformable<D>> {

    public PipedVertexTransformable(double x, double y, double z, float nX, float nY, float nZ, float u, float v) {
        super(x, y, z, nX, nY, nZ, u, v);
    }

    @Override
    public PipedVertexTransformable<D> transform(ITrans3 trans, @Nullable Direction fromFace, @Nullable Direction toFace, boolean transformUVs) {
        if (trans.isIdentity()) {
            return this;
        } else {
            var canTransformUVs = transformUVs && fromFace != null && toFace != null;
            var pos = this.pos(trans);
            var normal = this.normal(trans);
            var uvs = canTransformUVs ? this.uvs(trans, fromFace, toFace) : this.uvs();
            return new PipedVertexTransformable<>((float) pos.x(), (float) pos.y(), (float) pos.z(), normal.x(), normal.y(), normal.z(), (float) uvs.u(), (float) uvs.v());
        }
    }

}
