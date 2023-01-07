package com.tridevmc.architecture.core.model.mesh;

import com.tridevmc.architecture.core.math.ITrans3;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Default implementation of {@link IPolygonData}.
 */
public class PolygonData implements IPolygonData<PolygonData> {
    private final int textureIndex;
    private final int tintIndex;
    private final CullFace cullFace;

    /**
     * Creates a new polygon data with the given texture index, tint index, and cull face.
     *
     * @param textureIndex The texture index of the polygon.
     * @param tintIndex    The tint index of the polygon.
     * @param cullFace     The cull face of this polygon.
     */
    public PolygonData(int textureIndex, int tintIndex, CullFace cullFace) {
        this.textureIndex = textureIndex;
        this.tintIndex = tintIndex;
        this.cullFace = cullFace;
    }

    @Override
    public int getTextureIndex() {
        return this.textureIndex;
    }

    @Override
    public int getTintIndex() {
        return this.tintIndex;
    }

    @Override
    public @NotNull CullFace getCullFace() {
        return this.cullFace;
    }

    @Override
    public PolygonData transform(@NotNull ITrans3 trans) {
        // We're just going to transform the cull face then copy the rest of the data to a new instance.
        var cullFace = trans.transformCullFace(this.getCullFace());
        return new PolygonData(this.getTextureIndex(), this.getTintIndex(), cullFace);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PolygonData) obj;
        return this.textureIndex == that.textureIndex &&
                this.tintIndex == that.tintIndex &&
                Objects.equals(this.cullFace, that.cullFace);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.textureIndex, this.tintIndex, this.cullFace);
    }

    @Override
    public String toString() {
        return "PolygonData[" +
                "textureIndex=" + this.textureIndex + ", " +
                "tintIndex=" + this.tintIndex + ", " +
                "cullFace=" + this.cullFace + ']';
    }

}
