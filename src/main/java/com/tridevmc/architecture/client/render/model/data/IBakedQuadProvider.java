package com.tridevmc.architecture.client.render.model.data;

import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

/**
 * Defines an object that can provide a baked quad with data it already has prepared.
 * <p>
 * Allows for non-quad objects to be rendered as quads with some trickery.
 */
public interface IBakedQuadProvider<T> {

    /**
     * Bakes the data into a quad with the given arguments. Implementations are expected to cache these results.
     *
     * @param transform a transformation to apply to the quad.
     * @param facing    the face of the baked quad.
     * @param resolver  a metadata resolver to use for pulling the tintIndex and texture for the quad.
     * @return a baked quad matching the given information.
     */
    default BakedQuad bake(Transformation transform, Direction facing, IQuadMetadataResolver<T> resolver) {
        return this.bake(transform, facing, resolver.getTexture(this), resolver.getColour(this));
    }

    /**
     * Bakes the data into a quad with the given arguments. Implementations are expected to cache these results.
     *
     * @param transform a transform to apply to the quad while baking.
     * @param facing    the face of the baked quad.
     * @param sprite    the sprite to apply to the quad.
     * @param colour    the tintIndex to apply to the quad.
     * @return a baked quad matching the given information.
     */
    BakedQuad bake(Transformation transform, Direction facing, TextureAtlasSprite sprite, int colour);

    /**
     * Gets the normals for the baked object. Used by vertex implementations.
     *
     * @return the normals for the quad.
     */
    Vector3f getFaceNormal();

    /**
     * Gets the face of the backed object. Used by vertex implementations.
     *
     * @return the face for the quad.
     */
    Direction getFace();

    /**
     * Determines whether the object has all the information required for it to be baked.
     *
     * @return true if all the data is loaded, false otherwise.
     */
    boolean isComplete();

    /**
     * Sets the vertex in the given index.
     *
     * @param index  the index to set the vertex into.
     * @param vertex the vertex to set at the given index, this vertex object is not unique to the provider and is pooled by the model data.
     */
    void setVertex(int index, ArchitectureVertex vertex);

    /**
     * Iterates over the vertices in the quad provider and assigns normal values.
     */
    void assignNormals();

    /**
     * Gets the ranges for each dimension as a two dimensional array.
     * <p>
     * Example: getRanges()[2][0] == range minimum on the z axis.
     *
     * @return a two dimensional array of integers representing the range of variables present in each dimension.
     */
    int[][] getRanges(Transformation transform);

    /**
     * Gets the next vertex to be set into the model.
     *
     * @return the index to set the next vertex into.
     */
    int getNextVertex();

    /**
     * Gets the metadata about the quad provider used for fetching texture and tintIndex data prior to baking.
     *
     * @return the metadata for the quad provider.
     */
    T getMetadata();

}
