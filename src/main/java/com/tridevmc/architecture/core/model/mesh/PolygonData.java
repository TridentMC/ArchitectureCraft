package com.tridevmc.architecture.core.model.mesh;

import com.tridevmc.architecture.core.math.ITrans3;
import org.jetbrains.annotations.NotNull;

/**
 * Default implementation of {@link IPolygonData}.
 *
 * @param cullFace     The cull face of this polygon.
 * @param textureIndex The texture index of the polygon.
 * @param tintIndex    The tint index of the polygon.
 */
public record PolygonData(@NotNull CullFace cullFace, @NotNull FaceDirection face, int textureIndex,
                          int tintIndex) implements IPolygonData<PolygonData> {

    @Override
    public PolygonData transform(@NotNull ITrans3 trans) {
        if (this.cullFace() == CullFace.NONE) return this;
        // We're just going to transform the cull face then copy the rest of the data to a new instance.
        return new PolygonData(trans.transformCullFace(this.cullFace()),
                trans.transformFaceDirection(this.face()),
                this.textureIndex(),
                this.tintIndex());
    }

}
