package com.tridevmc.architecture.client.render.model.data;

import com.google.common.collect.ImmutableList;
import com.mojang.math.Transformation;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;

/**
 * Default implementation of {@link IBakedQuadContainerProvider}, no caching is performed.
 *
 * @param <D> The type of the quad metadata.
 */
public class BakedQuadContainerProvider<D> implements IBakedQuadContainerProvider<D> {

    private final ImmutableList<IPipedBakedQuad<?, ?, D>> quads;

    protected BakedQuadContainerProvider(ImmutableList<IPipedBakedQuad<?, ?, D>> quads) {
        this.quads = quads;
    }

    /**
     * Builder for {@link BakedQuadContainerProvider}.
     *
     * @param <D> The type of the quad metadata.
     */
    public static class Builder<D> {

        private final ImmutableList.Builder<IPipedBakedQuad<?, ?, D>> quads = ImmutableList.builder();

        /**
         * Adds a quad to the container provider.
         *
         * @param quad The quad to add.
         * @return This builder.
         */
        public Builder<D> addQuad(IPipedBakedQuad<?, ?, D> quad) {
            this.quads.add(quad);
            return this;
        }

        /**
         * Builds the container provider.
         *
         * @return The container provider.
         */
        public BakedQuadContainerProvider<D> build() {
            return new BakedQuadContainerProvider<>(this.quads.build());
        }

    }

    @Override
    public IBakedQuadContainer getQuads(IQuadMetadataResolver<D> metadataResolver, Transformation transform, boolean force) {
        var builder = new BakedQuadContainer.Builder();
        var quadBakingVertexConsumer = new QuadBakingVertexConsumer(builder::addQuad);
        for (int i = 0; i < this.quads.size(); i++) {
            var q = this.quads.get(i);
            q.pipe(quadBakingVertexConsumer, transform, metadataResolver);
        }
        return builder.build();
    }

}
