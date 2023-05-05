package com.tridevmc.architecture.client.render.model.piped;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.IVector3;
import com.tridevmc.architecture.core.math.IVector3Mutable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @NotNull
    Direction face();

    /**
     * Determines if the quad should be culled on its face.
     *
     * @return true if the quad should be culled on its face, false otherwise.
     */
    boolean shouldCull();

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
    default void pipe(@NotNull VertexConsumer consumer, @NotNull ITrans3 transform,
                      @NotNull TextureAtlasSprite sprite, int tintIndex) {
        if (consumer instanceof QuadBakingVertexConsumer bakingConsumer) {
            //noinspection DataFlowIssue - we can set the direction to null as that's a valid value.
            bakingConsumer.setDirection(this.cullFace(transform));
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
            //noinspection DataFlowIssue - we can set the direction to null as that's a valid value.
            bakingConsumer.setDirection(this.cullFace());
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
    default void pipe(@NotNull VertexConsumer consumer, @NotNull ITrans3 transform,
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
    default IVector3Mutable normal() {
        return IVector3.ofMutable(this.nX(), this.nY(), this.nZ());
    }

    /**
     * Gets the normal of the quad, transformed by the given transformation.
     *
     * @param transform the transformation to apply to the normal.
     * @return the transformed normal.
     */
    default IVector3 normal(ITrans3 transform) {
        return transform.transformNormal(this.normal());
    }

    /**
     * Gets the face of the quad, transformed by the given transformation.
     *
     * @param transform the transformation to apply to the face.
     * @return the transformed face.
     */
    @NotNull
    default Direction face(ITrans3 transform) {
        return transform.transformDirection(this.face());
    }

    /**
     * Gets the cull face of the quad, if any.
     *
     * @return the cull face of the quad, or null if the quad should not be culled.
     */
    @Nullable
    default Direction cullFace() {
        return this.shouldCull() ? this.face() : null;
    }

    /**
     * Gets the cull face of the quad, if any.
     *
     * @param transform the transformation to apply to the face.
     * @return the cull face of the quad, or null if the quad should not be culled.
     */
    @Nullable
    default Direction cullFace(ITrans3 transform) {
        return this.shouldCull() ? this.face(transform) : null;
    }

    /**
     * Gets the minimum bound of the quad.
     *
     * @return the minimum bound of the quad.
     */
    default IVector3Mutable min() {
        return IVector3.ofMutable(this.minX(), this.minY(), this.minZ());
    }

    /**
     * Gets the maximum bound of the quad.
     *
     * @return the maximum bound of the quad.
     */
    default IVector3Mutable max() {
        return IVector3.ofMutable(this.maxX(), this.maxY(), this.maxZ());
    }

    /**
     * Gets the minimum bound of the quad, after the given transform has been applied.
     *
     * @return the minimum bound of the quad.
     */
    default IVector3 min(ITrans3 transform) {
        return transform.transformPos(this.min());
    }

    /**
     * Gets the maximum bound of the quad, after the given transform has been applied.
     *
     * @return the maximum bound of the quad.
     */
    default IVector3 max(ITrans3 transform) {
        return transform.transformPos(this.max());
    }

}
