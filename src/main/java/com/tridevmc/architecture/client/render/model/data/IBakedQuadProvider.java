package com.tridevmc.architecture.client.render.model.data;

import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;

/**
 * Defines an object that can provide a baked quad with data it already has prepared.
 * <p>
 * Allows for non-quad objects to be rendered as quads with some trickery.
 */
public interface IBakedQuadProvider {

    /**
     * Bakes the data into a quad with the given arguments. Implementations are expected to cache these results.
     *
     * @param transform a transform to apply to the quad while baking.
     * @param facing    the face of the baked quad.
     * @param sprite    the sprite to apply to the quad.
     * @param tintIndex the tint to apply to the quad.
     * @return a baked quad matching the given information.
     */
    BakedQuad bake(TransformationMatrix transform, Direction facing, TextureAtlasSprite sprite, int tintIndex);

    /**
     * Gets the normals for the baked object. Used by vertex implementations.
     *
     * @return the normals for the quad.
     */
    Vector3f getNormals();

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
     * @param index the index to set the vertex into.
     * @param data  the data to apply to the vertex. UVs will be generated automatically.
     */
    void setVertex(int index, float[] data);

    /**
     * Sets the vertex in the given index.
     *
     * @param index the index to set the vertex into.
     * @param data  the date to apply to the vertex.
     * @param uvs   the uv data to apply to the vertex.
     */
    void setVertex(int index, float[] data, float[] uvs);

    /**
     * Gets the ranges for each dimension as a two dimensional array.
     *
     * Example: getRanges()[2][0] == range minimum on the z axis.
     *
     * @return a two dimensional array of integers representing the range of variables present in each dimension.
     */
    int[][] getRanges(TransformationMatrix transform);

    /**
     * Gets the next vertex to be set into the model.
     *
     * @return the index to set the next vertex into.
     */
    int getNextVertex();
}
