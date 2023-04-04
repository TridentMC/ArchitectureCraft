package com.tridevmc.architecture.client.render.model.data;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

/**
 * Default implementation of {@link IBakedQuadContainer}, used to store a collection of baked quads for each face of a model.
 * <p>
 *
 * @param allQuads     a flat collection of all the baked quads.
 * @param generalQuads a collection of the general baked quads.
 * @param northQuads   a collection of all the baked quads for the north face.
 * @param southQuads   a collection of all the baked quads for the south face.
 * @param eastQuads    a collection of all the baked quads for the east face.
 * @param westQuads    a collection of all the baked quads for the west face.
 * @param upQuads      a collection of all the baked quads for the up face.
 * @param downQuads    a collection of all the baked quads for the down face.
 */
public record BakedQuadContainer(ImmutableList<BakedQuad> allQuads,
                                 ImmutableList<BakedQuad> generalQuads,
                                 ImmutableList<BakedQuad> northQuads,
                                 ImmutableList<BakedQuad> southQuads,
                                 ImmutableList<BakedQuad> eastQuads,
                                 ImmutableList<BakedQuad> westQuads,
                                 ImmutableList<BakedQuad> upQuads,
                                 ImmutableList<BakedQuad> downQuads) implements IBakedQuadContainer {

    /**
     * Creates a new builder for a {@link BakedQuadContainer}.
     */
    public static class Builder {

        private final ImmutableList.Builder<BakedQuad> allQuads = ImmutableList.builder();
        private final ImmutableList.Builder<BakedQuad> generalQuads = ImmutableList.builder();
        private final ImmutableList.Builder<BakedQuad> northQuads = ImmutableList.builder();
        private final ImmutableList.Builder<BakedQuad> southQuads = ImmutableList.builder();
        private final ImmutableList.Builder<BakedQuad> eastQuads = ImmutableList.builder();
        private final ImmutableList.Builder<BakedQuad> westQuads = ImmutableList.builder();
        private final ImmutableList.Builder<BakedQuad> upQuads = ImmutableList.builder();
        private final ImmutableList.Builder<BakedQuad> downQuads = ImmutableList.builder();

        /**
         * Adds a baked quad to the container, and adds it to the appropriate face collection.
         *
         * @param quad the baked quad to add.
         * @return this builder.
         */
        public Builder addQuad(BakedQuad quad) {
            this.addForDirection(quad, quad.getDirection());
            return this;
        }

        /**
         * Adds a baked quad to the container, and adds it to the appropriate face collection.
         *
         * @param quad      the baked quad to add.
         * @param direction the face of the quad.
         * @return this builder.
         */
        public Builder addForDirection(BakedQuad quad, @Nullable Direction direction) {
            this.allQuads.add(quad);
            if (direction == null) {
                this.generalQuads.add(quad);
            } else {
                switch (direction) {
                    case NORTH -> this.northQuads.add(quad);
                    case SOUTH -> this.southQuads.add(quad);
                    case EAST -> this.eastQuads.add(quad);
                    case WEST -> this.westQuads.add(quad);
                    case UP -> this.upQuads.add(quad);
                    case DOWN -> this.downQuads.add(quad);
                }
            }
            return this;
        }

        /**
         * Adds a general baked quad to the container.
         *
         * @param quad the baked quad to add.
         * @return this builder.
         */
        public Builder addGeneralQuad(BakedQuad quad) {
            this.allQuads.add(quad);
            this.generalQuads.add(quad);
            return this;
        }

        /**
         * Adds a baked quad on the north face to the container.
         *
         * @param quad the baked quad to add.
         * @return this builder.
         */
        public Builder addNorth(BakedQuad quad) {
            this.northQuads.add(quad);
            this.allQuads.add(quad);
            return this;
        }

        /**
         * Adds a baked quad on the south face to the container.
         *
         * @param quad the baked quad to add.
         * @return this builder.
         */
        public Builder addSouth(BakedQuad quad) {
            this.southQuads.add(quad);
            this.allQuads.add(quad);
            return this;
        }

        /**
         * Adds a baked quad on the east face to the container.
         *
         * @param quad the baked quad to add.
         * @return this builder.
         */
        public Builder addEast(BakedQuad quad) {
            this.eastQuads.add(quad);
            this.allQuads.add(quad);
            return this;
        }

        /**
         * Adds a baked quad on the west face to the container.
         *
         * @param quad the baked quad to add.
         * @return this builder.
         */
        public Builder addWest(BakedQuad quad) {
            this.westQuads.add(quad);
            this.allQuads.add(quad);
            return this;
        }

        /**
         * Adds a baked quad on the up face to the container.
         *
         * @param quad the baked quad to add.
         * @return this builder.
         */
        public Builder addUp(BakedQuad quad) {
            this.upQuads.add(quad);
            this.allQuads.add(quad);
            return this;
        }

        /**
         * Adds a baked quad on the down face to the container.
         *
         * @param quad the baked quad to add.
         * @return this builder.
         */
        public Builder addDown(BakedQuad quad) {
            this.downQuads.add(quad);
            this.allQuads.add(quad);
            return this;
        }

        /**
         * Builds the {@link BakedQuadContainer} from the data in this builder.
         *
         * @return the baked quad container.
         */
        public BakedQuadContainer build() {
            return new BakedQuadContainer(
                    this.allQuads.build(),
                    this.generalQuads.build(),
                    this.northQuads.build(),
                    this.southQuads.build(),
                    this.eastQuads.build(),
                    this.westQuads.build(),
                    this.upQuads.build(),
                    this.downQuads.build()
            );
        }

    }

}
