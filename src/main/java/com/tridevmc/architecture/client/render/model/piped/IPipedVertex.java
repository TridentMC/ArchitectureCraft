package com.tridevmc.architecture.client.render.model.piped;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.IVector3;
import com.tridevmc.architecture.core.math.IVector3Mutable;
import com.tridevmc.architecture.core.math.floating.IVector2F;
import com.tridevmc.architecture.core.math.floating.IVector2FMutable;
import com.tridevmc.architecture.core.math.floating.IVector3F;
import com.tridevmc.architecture.core.math.floating.IVector3FMutable;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.NotNull;


/**
 * Provides a common interface for vertices that can be piped into a vertex consumer.
 *
 * @param <V> A self-referential type parameter, referring to the type of the implementing class.
 * @param <Q> the type of quad provider that this vertex is associated with.
 * @param <D> the type of quad metadata that this vertex is associated with.
 */
public interface IPipedVertex<V extends IPipedVertex<V, Q, D>, Q extends IPipedBakedQuad<Q, V, D>, D> {

    /**
     * Gets the x position of this vertex.
     *
     * @return the x position of this vertex.
     */
    double x();

    /**
     * Gets the y position of this vertex.
     *
     * @return the y position of this vertex.
     */
    double y();

    /**
     * Gets the z position of this vertex.
     *
     * @return the z position of this vertex.
     */
    double z();

    /**
     * Gets the x normal of this vertex.
     *
     * @return the x normal of this vertex.
     */
    float nX();

    /**
     * Gets the y normal of this vertex.
     *
     * @return the y normal of this vertex.
     */
    float nY();

    /**
     * Gets the z normal of this vertex.
     *
     * @return the z normal of this vertex.
     */
    float nZ();

    /**
     * Gets the u texture coordinate of this vertex.
     *
     * @return the u texture coordinate of this vertex.
     */
    float u();

    /**
     * Gets the v texture coordinate of this vertex.
     *
     * @return the v texture coordinate of this vertex.
     */
    float v();

    /**
     * Pipes this vertex into the given vertex consumer.
     *
     * @param consumer     the consumer to pipe this vertex into.
     * @param quadProvider the quad provider that this vertex is associated with.
     * @param sprite       the sprite to use for this vertex.
     * @param colour       the colour to use for this vertex.
     */
    default void pipe(@NotNull VertexConsumer consumer, @NotNull Q quadProvider,
                      @NotNull TextureAtlasSprite sprite, int colour) {
        consumer.vertex(this.x(), this.y(), this.z())
                .color(colour)
                .normal(this.nX(), this.nY(), this.nZ())
                .uv(sprite.getU(this.u()), sprite.getV(this.v()))
                .uv2(1, 0)
                .overlayCoords(1, 0)
                .endVertex();
    }

    /**
     * Pipes this vertex into the given vertex consumer.
     *
     * @param consumer     the consumer to pipe this vertex into.
     * @param quadProvider the quad provider that this vertex is associated with.
     * @param transform    the transformation to apply to this vertex.
     * @param sprite       the sprite to use for this vertex.
     * @param colour       the colour to use for this vertex.
     */
    default void pipe(@NotNull VertexConsumer consumer, @NotNull Q quadProvider,
                      @NotNull ITrans3 transform, @NotNull TextureAtlasSprite sprite, int colour) {
        if (transform.isIdentity()) {
            // If the transformation is identity, we can skip the transformation step entirely.
            this.pipe(consumer, quadProvider, sprite, colour);
        } else {
            // Otherwise, we need to transform the vertex.
            var fromFace = quadProvider.face();
            var toFace = quadProvider.face(transform);
            var pos = this.pos(transform);
            var normal = this.normal(transform);
            var uvs = transform.transformUV(this.uvs(transform, fromFace, toFace));
            var x = pos.x();
            var y = pos.y();
            var z = pos.z();
            var nX = normal.x();
            var nY = normal.y();
            var nZ = normal.z();
            var u = uvs.getU();
            var v = uvs.getV();
            consumer.vertex(x, y, z)
                    .color(colour)
                    .normal(nX, nY, nZ)
                    .uv(sprite.getU((float) u), sprite.getV((float) v))
                    .uv2(1, 0)
                    .overlayCoords(1, 0)
                    .endVertex();
        }
    }

    /**
     * Gets the position of this vertex.
     *
     * @return the position of this vertex.
     */
    default IVector3Mutable pos() {
        return IVector3.ofMutable(this.x(), this.y(), this.z());
    }

    /**
     * Gets the position of this vertex after applying the given transformation.
     *
     * @param transform the transformation to apply.
     * @return the transformed position of this vertex.
     */
    default IVector3 pos(ITrans3 transform) {
        return transform.transformPos(this.pos());
    }

    /**
     * Gets the normal of this vertex.
     *
     * @return the normal of this vertex.
     */
    default IVector3FMutable normal() {
        return IVector3F.ofMutable(this.nX(), this.nY(), this.nZ());
    }

    /**
     * Gets the normal of this vertex after applying the given transformation.
     *
     * @param transform the transformation to apply.
     * @return the transformed normal of this vertex.
     */
    default IVector3F normal(ITrans3 transform) {
        return transform.transformNormal(this.normal());
    }

    /**
     * Gets the texture coordinates of this vertex.
     *
     * @return the texture coordinates of this vertex.
     */
    default IVector2FMutable uvs() {
        return IVector2F.ofMutable(this.u(), this.v());
    }

    /**
     * Gets the texture coordinates of this vertex after applying the appropriate transformation based on the original and recalculated face.
     *
     * @param from the original face of this vertex.
     * @param to   the recalculated face of this vertex.
     * @return the transformed texture coordinates of this vertex.
     */
    default IVector2FMutable uvs(ITrans3 transform, Direction from, Direction to) {
        // The goal here is to effectively lock the texture coordinates to the face of the quad, so that rotated blocks next to each other
        // look like they're part of the same texture.
        // We don't need the actual transformation matrix for this, just as long as we know what face the quad was originally on, and where it ended up.
        var u = this.u();
        var v = this.v();

        if (from == to) {
            // If the face didn't change, we can just return the original texture coordinates.
            return transform.transformUV(this.uvs());
        }

        return transform.transformUV(switch (from) {
            case DOWN -> {
                // u = 16x
                // v = 16 - 16z
                switch (to) {
                    case UP, SOUTH -> {
                        // u is the same as before, but v is now directly proportional to 16z
                        // When rotating from down to south, we keep using the x coordinate, but the z coordinate gets rotated to the y coordinate.
                        // u = 16x
                        // v = 16 - 16y
                        yield IVector2F.ofMutable(u, 16F - v);
                    }
                    case NORTH -> {
                        // When rotating from down to north, we keep using the x coordinate, but the z coordinate gets rotated to the y coordinate.
                        // u = 16 - 16x
                        // v = 16 - 16y
                        yield IVector2F.ofMutable(16F - u, 16F - v);
                    }
                    case WEST -> {
                        // When rotating from down to west, we keep using the z coordinate, but the x coordinate gets rotated to the y coordinate.
                        // u = 16z
                        // v = 16 - 16y
                        yield IVector2F.ofMutable(v, 16F - u);
                    }
                    case EAST -> {
                        // When rotating from down to east, we keep using the z coordinate, but the x coordinate gets rotated to the y coordinate.
                        // u = 16 - 16z
                        // v = 16 - 16y
                        yield IVector2F.ofMutable(16F - v, 16F - u);
                    }
                    default -> throw new IllegalArgumentException("Invalid face: " + to);
                }
            }
            case UP -> {
                // u = 16x
                // v = 16z
                switch (to) {
                    case DOWN -> {
                        // u is the same as before, but v is now 16 - 16z
                        yield IVector2F.ofMutable(u, 16F - v);
                    }
                    case NORTH -> {
                        // When rotating from up to north, we keep using the x coordinate, but the z coordinate gets rotated to the y coordinate.
                        // u = 16 - 16x
                        // v = 16y
                        yield IVector2F.ofMutable(16F - u, v);
                    }
                    case SOUTH -> {
                        // When rotating from up to south, we keep using the x coordinate, but the z coordinate gets rotated to the y coordinate.
                        // u = 16x
                        // v = 16y
                        yield IVector2F.ofMutable(u, v);
                    }
                    case WEST -> {
                        // When rotating from up to west, we keep using the z coordinate, but the x coordinate gets rotated to the y coordinate.
                        // u = 16z
                        // v = 16y
                        yield IVector2F.ofMutable(v, u);
                    }
                    case EAST -> {
                        // When rotating from up to east, we keep using the z coordinate, but the x coordinate gets rotated to the y coordinate.
                        // u = 16 - 16z
                        // v = 16y
                        yield IVector2F.ofMutable(16F - v, u);
                    }
                    default -> throw new IllegalArgumentException("Invalid face: " + to);
                }
            }
            case NORTH -> {
                // u = 16 - 16x
                // v = 16 - 16y
                switch (to) {
                    case DOWN -> {
                        // When rotating from north to down, we keep using the x coordinate, but the y coordinate gets rotated to the z coordinate.
                        // u = 16x
                        // v = 16 - 16z
                        yield IVector2F.ofMutable(u, 16F - v);
                    }
                    case UP -> {
                        // When rotating from north to up, we keep using the x coordinate, but the y coordinate gets rotated to the z coordinate.
                        // u = 16x
                        // v = 16z
                        yield IVector2F.ofMutable(u, v);
                    }
                    case SOUTH -> {
                        // When rotating from north to south, we keep using the x coordinate, but the y coordinate gets rotated to the z coordinate.
                        // u = 16 - 16x
                        // v = 16z
                        yield IVector2F.ofMutable(16F - u, v);
                    }
                    case WEST -> {
                        // When rotating from north to west, we keep using the y coordinate, but the x coordinate gets rotated to the z coordinate.
                        // u = 16y
                        // v = 16z
                        yield IVector2F.ofMutable(v, u);
                    }
                    case EAST -> {
                        // When rotating from north to east, we keep using the y coordinate, but the x coordinate gets rotated to the z coordinate.
                        // u = 16 - 16y
                        // v = 16z
                        yield IVector2F.ofMutable(16F - v, u);
                    }
                    default -> throw new IllegalArgumentException("Invalid face: " + to);
                }
            }
            case SOUTH -> {
                // u = 16x
                // v = 16 - 16y
                switch (to) {
                    case DOWN -> {
                        // When rotating from south to down, we keep using the x coordinate, but the y coordinate gets rotated to the z coordinate.
                        // u = 16x
                        // v = 16 - 16z
                        yield IVector2F.ofMutable(u, 16F - v);
                    }
                    case UP -> {
                        // When rotating from south to up, we keep using the x coordinate, but the y coordinate gets rotated to the z coordinate.
                        // u = 16x
                        // v = 16z
                        yield IVector2F.ofMutable(u, v);
                    }
                    case NORTH -> {
                        // When rotating from south to north, we keep using the x coordinate, but the y coordinate gets rotated to the z coordinate.
                        // u = 16 - 16x
                        // v = 16 - 16z
                        yield IVector2F.ofMutable(16F - u, 16F - v);
                    }
                    case WEST -> {
                        // When rotating from south to west, we keep using the y coordinate, but the x coordinate gets rotated to the z coordinate.
                        // u = 16y
                        // v = 16 - 16z
                        yield IVector2F.ofMutable(v, 16F - u);
                    }
                    case EAST -> {
                        // When rotating from south to east, we keep using the y coordinate, but the x coordinate gets rotated to the z coordinate.
                        // u = 16 - 16y
                        // v = 16 - 16z
                        yield IVector2F.ofMutable(16F - v, 16F - u);
                    }
                    default -> throw new IllegalArgumentException("Invalid face: " + to);
                }
            }
            case WEST -> {
                // u = 16z
                // v = 16 - 16y
                switch (to) {
                    case DOWN -> {
                        // When rotating from west to down, we keep using the z coordinate, but the y coordinate gets rotated to the x coordinate.
                        // u = 16 - 16z
                        // v = 16 - 16x
                        yield IVector2F.ofMutable(16F - v, 16F - u);
                    }
                    case UP, NORTH -> {
                        // When rotating from west to up, we keep using the z coordinate, but the y coordinate gets rotated to the x coordinate.
                        // u = 16 - 16z
                        // v = 16x
                        // When rotating from west to north, we keep using the y coordinate, but the x coordinate gets rotated to the z coordinate.
                        // u = 16 - 16y
                        // v = 16z
                        yield IVector2F.ofMutable(16F - v, u);
                    }
                    case SOUTH -> {
                        // When rotating from west to south, we keep using the y coordinate, but the x coordinate gets rotated to the z coordinate.
                        // u = 16y
                        // v = 16 - 16z
                        yield IVector2F.ofMutable(v, 16F - u);
                    }
                    case EAST -> {
                        // When rotating from west to east, we keep using the y coordinate, but the x coordinate gets rotated to the z coordinate.
                        // u = 16y
                        // v = 16z
                        yield IVector2F.ofMutable(v, u);
                    }
                    default -> throw new IllegalArgumentException("Invalid face: " + to);
                }
            }
            case EAST -> {
                // u = 16 - 16z
                // v = 16 - 16y
                switch (to) {
                    case DOWN, NORTH -> {
                        // When rotating from east to down, we keep using the z coordinate, but the y coordinate gets rotated to the x coordinate.
                        // u = 16 - 16z
                        // v = 16 - 16x
                        // When rotating from east to north, we keep using the y coordinate, but the x coordinate gets rotated to the z coordinate.
                        // u = 16 - 16y
                        // v = 16 - 16z
                        yield IVector2F.ofMutable(16F - v, 16F - u);
                    }
                    case UP -> {
                        // When rotating from east to up, we keep using the z coordinate, but the y coordinate gets rotated to the x coordinate.
                        // u = 16 - 16z
                        // v = 16x
                        yield IVector2F.ofMutable(16F - v, u);
                    }
                    // When rotating from east to north, we keep using the y coordinate, but the x coordinate gets rotated to the z coordinate.
                    // u = 16 - 16y
                    // v = 16 - 16z
                    case SOUTH -> {
                        // When rotating from east to south, we keep using the y coordinate, but the x coordinate gets rotated to the z coordinate.
                        // u = 16y
                        // v = 16 - 16z
                        yield IVector2F.ofMutable(v, 16F - u);
                    }
                    case WEST -> {
                        // When rotating from east to west, we keep using the y coordinate, but the x coordinate gets rotated to the z coordinate.
                        // u = 16y
                        // v = 16z
                        yield IVector2F.ofMutable(v, u);
                    }
                    default -> throw new IllegalArgumentException("Invalid face: " + to);
                }
            }
        });
    }

}
