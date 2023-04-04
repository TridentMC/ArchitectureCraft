package com.tridevmc.architecture.client.render.model.data;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import org.joml.Vector4f;

/**
 * Defines an object that can provide a baked quad with data it already has prepared.
 * <p>
 * Allows for non-quad objects to be rendered as quads with some trickery.
 *
 * @param <Q> A self-referential type parameter, referring to the type of the implementing class.
 * @param <V> The type of vertex that is piped by the quad.
 * @param <D> The type of metadata this provider uses.
 */
public interface IPipedBakedQuad<Q extends IPipedBakedQuad<Q, V, D>, V extends IPipedVertex<V, Q, D>, D> {

    /**
     * Creates a new quad piper by applying the given transformation to this quad piper.
     *
     * @param transform The transformation to apply.
     * @return A new quad piper with the transformation applied.
     */
    Q transform(Transformation transform);

    /**
     * Gets an immutable list of all the vertices in this quad.
     *
     * @return an immutable list of all the vertices in this quad.
     */
    ImmutableList<V> vertices();

    /**
     * Gets the x component of the normal vector for this quad.
     *
     * @return the x component of the normal vector for this quad.
     */
    float nX();

    /**
     * Gets the y component of the normal vector for this quad.
     *
     * @return the y component of the normal vector for this quad.
     */
    float nY();

    /**
     * Gets the z component of the normal vector for this quad.
     *
     * @return the z component of the normal vector for this quad.
     */
    float nZ();

    /**
     * Gets the face of the quad.
     *
     * @return the face of the quad.
     */
    Direction face();

    /**
     * Gets the minimum bounds of the quad on the X axis.
     *
     * @return the minimum bounds of the quad on the X axis.
     */
    float minX();

    /**
     * Gets the minimum bounds of the quad on the Y axis.
     *
     * @return the minimum bounds of the quad on the Y axis.
     */
    float minY();

    /**
     * Gets the minimum bounds of the quad on the Z axis.
     *
     * @return the minimum bounds of the quad on the Z axis.
     */
    float minZ();

    /**
     * Gets the maximum bounds of the quad on the X axis.
     *
     * @return the maximum bounds of the quad on the X axis.
     */
    float maxX();

    /**
     * Gets the maximum bounds of the quad on the Y axis.
     *
     * @return the maximum bounds of the quad on the Y axis.
     */
    float maxY();

    /**
     * Gets the maximum bounds of the quad on the Z axis.
     *
     * @return the maximum bounds of the quad on the Z axis.
     */
    float maxZ();

    /**
     * Gets the metadata about the quad provider used for fetching texture and tintIndex data prior to baking.
     *
     * @return the metadata for the quad provider.
     */
    D metadata();

    /**
     * Bakes the data into a quad with the given arguments. Implementations are expected to cache these results.
     *
     * @param transform a transform to apply to the quad while baking.
     * @param sprite    the sprite to apply to the quad.
     * @param tintIndex the tintIndex to apply to the quad.
     */
    default void pipe(@NotNull VertexConsumer consumer, @NotNull Transformation transform,
                      @NotNull TextureAtlasSprite sprite, int tintIndex) {
        if (consumer instanceof QuadBakingVertexConsumer bakingConsumer) {
            bakingConsumer.setDirection(this.face(transform));
            bakingConsumer.setSprite(sprite);
            bakingConsumer.setTintIndex(tintIndex);
        }
        @SuppressWarnings("unchecked") Q self = (Q) this;
        var vertices = this.vertices();
        for (V v : vertices) {
            v.pipe(consumer, self, transform, sprite, tintIndex);
        }
        if (vertices.size() == 3) {
            // If we have a triangle, we need to add a fourth vertex to make it a quad.
            vertices.get(2).pipe(consumer, self, transform, sprite, tintIndex);
        }
    }

    /**
     * Bakes the data into a quad with the given arguments. Implementations are expected to cache these results.
     *
     * @param sprite    the sprite to apply to the quad.
     * @param tintIndex the tintIndex to apply to the quad.
     */
    default void pipe(@NotNull VertexConsumer consumer,
                      @NotNull TextureAtlasSprite sprite, int tintIndex) {
        if (consumer instanceof QuadBakingVertexConsumer bakingConsumer) {
            bakingConsumer.setDirection(this.face());
            bakingConsumer.setSprite(sprite);
            bakingConsumer.setTintIndex(tintIndex);
        }
        @SuppressWarnings("unchecked") Q self = (Q) this;
        var vertices = this.vertices();
        for (V v : vertices) {
            v.pipe(consumer, self, sprite, tintIndex);
        }
        if (vertices.size() == 3) {
            // If we have a triangle, we need to add a fourth vertex to make it a quad.
            vertices.get(2).pipe(consumer, self, sprite, tintIndex);
        }
    }

    /**
     * Bakes the data into a quad with the given arguments. Implementations are expected to cache these results.
     *
     * @param transform a transformation to apply to the quad.
     * @param resolver  a metadata resolver to use for pulling the tintIndex and texture for the quad.
     */
    default void pipe(@NotNull VertexConsumer consumer, @NotNull Transformation transform,
                      @NotNull IQuadMetadataResolver<D> resolver) {
        this.pipe(consumer, transform, resolver.getTexture(this), resolver.getTintIndex(this));
    }

    /**
     * Bakes the data into a quad with the given arguments. Implementations are expected to cache these results.
     *
     * @param resolver a metadata resolver to use for pulling the tintIndex and texture for the quad.
     */
    default void pipe(@NotNull VertexConsumer consumer,
                      @NotNull IQuadMetadataResolver<D> resolver) {
        this.pipe(consumer, resolver.getTexture(this), resolver.getTintIndex(this));
    }

    /**
     * Gets the normal of the quad.
     *
     * @return the normal of the quad.
     */
    default Vector3f normal() {
        return new Vector3f(this.nX(), this.nY(), this.nZ());
    }

    /**
     * Gets the normal of the quad, transformed by the given transformation.
     *
     * @param transform the transformation to apply to the normal.
     * @return the transformed normal.
     */
    default Vector3f normal(Transformation transform) {
        var normal = this.normal();
        transform.transformNormal(normal);
        return normal;
    }

    /**
     * Gets the face of the quad, transformed by the given transformation.
     *
     * @param transform the transformation to apply to the face.
     * @return the transformed face.
     */
    default Direction face(Transformation transform) {
        return transform.rotateTransform(this.face());
    }

    /**
     * Gets the minimum bound of the quad.
     *
     * @return the minimum bound of the quad.
     */
    default Vector3f min() {
        return new Vector3f(this.minX(), this.minY(), this.minZ());
    }

    /**
     * Gets the maximum bound of the quad.
     *
     * @return the maximum bound of the quad.
     */
    default Vector3f max() {
        return new Vector3f(this.maxX(), this.maxY(), this.maxZ());
    }

    /**
     * Gets the minimum bound of the quad, after the given transform has been applied.
     *
     * @return the minimum bound of the quad.
     */
    default Vector3f min(Transformation transform) {
        var min = new Vector4f(this.minX(), this.minY(), this.minZ(), 1);
        transform.transformPosition(min);
        return new Vector3f(min.x(), min.y(), min.z());
    }

    /**
     * Gets the maximum bound of the quad, after the given transform has been applied.
     *
     * @return the maximum bound of the quad.
     */
    default Vector3f max(Transformation transform) {
        var max = new Vector4f(this.maxX(), this.maxY(), this.maxZ(), 1);
        transform.transformPosition(max);
        return new Vector3f(max.x(), max.y(), max.z());
    }

}
