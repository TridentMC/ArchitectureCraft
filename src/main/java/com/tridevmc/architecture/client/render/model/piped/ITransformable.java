package com.tridevmc.architecture.client.render.model.piped;

import com.tridevmc.architecture.core.math.ITrans3;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Defines an object that can be transformed by an ITrans3.
 *
 * @param <S> A self-referential type parameter, referring to the type of the implementing class.
 */
public interface ITransformable<S extends ITransformable<S>> {

    /**
     * Creates a new object by applying the given transformation to this object.
     *
     * @param trans The transformation to apply.
     * @return A new object with the transformation applied.
     */
    default S transform(ITrans3 trans) {
        return this.transform(trans, true);
    }

    /**
     * Creates a new object by applying the given transformation to this object.
     *
     * @param trans    The transformation to apply.
     * @param transformUVs Whether the UVs coordinates should be transformed.
     * @return A new object with the transformation applied.
     */
    default S transform(ITrans3 trans, boolean transformUVs) {
        return this.transform(trans, null, null, transformUVs);
    }

    /**
     * Creates a new object by applying the given transformation to this object.
     *
     * @param trans    The transformation to apply.
     * @param fromFace     The face of the vertex before the transformation is applied.
     * @param toFace       The face of the vertex after the transformation is applied.
     * @param transformUVs Whether the UVs coordinates should be transformed.
     * @return A new object with the transformation applied.
     */
    S transform(ITrans3 trans, @Nullable Direction fromFace, @Nullable Direction toFace, boolean transformUVs);


}
