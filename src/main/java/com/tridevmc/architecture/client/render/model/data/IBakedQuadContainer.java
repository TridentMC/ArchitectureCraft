package com.tridevmc.architecture.client.render.model.data;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Defines an object that contains a collection of already baked quads for each of its faces.
 */
public interface IBakedQuadContainer {

    default ImmutableList<BakedQuad> quadsFor(@Nullable Direction face) {
        // We can't use a switch for null values in Java 17, so we have to do this.
        if (face == null) return this.generalQuads();
        return switch (face) {
            case NORTH -> this.northQuads();
            case SOUTH -> this.southQuads();
            case EAST -> this.eastQuads();
            case WEST -> this.westQuads();
            case UP -> this.upQuads();
            case DOWN -> this.downQuads();
        };
    }

    /**
     * Gets the baked quads for all the faces of the model.
     * <p>
     * This is used for rendering the model in the inventory where no culling is performed.
     *
     * @return a flat collection of all the baked quads.
     */
    ImmutableList<BakedQuad> allQuads();

    /**
     * Gets the baked quads of the model not associated with a specific face.
     *
     * @return a collection of the general baked quads.
     */
    ImmutableList<BakedQuad> generalQuads();

    /**
     * Gets the baked quads for the north face of the model.
     *
     * @return a collection of all the baked quads for the north face.
     */
    ImmutableList<BakedQuad> northQuads();

    /**
     * Gets the baked quads for the south face of the model.
     *
     * @return a collection of all the baked quads for the south face.
     */
    ImmutableList<BakedQuad> southQuads();

    /**
     * Gets the baked quads for the west face of the model.
     *
     * @return a collection of all the baked quads for the west face.
     */
    ImmutableList<BakedQuad> westQuads();

    /**
     * Gets the baked quads for the east face of the model.
     *
     * @return a collection of all the baked quads for the east face.
     */
    ImmutableList<BakedQuad> eastQuads();

    /**
     * Gets the baked quads for the up face of the model.
     *
     * @return a collection of all the baked quads for the up face.
     */
    ImmutableList<BakedQuad> upQuads();

    /**
     * Gets the baked quads for the down face of the model.
     *
     * @return a collection of all the baked quads for the down face.
     */
    ImmutableList<BakedQuad> downQuads();

}
