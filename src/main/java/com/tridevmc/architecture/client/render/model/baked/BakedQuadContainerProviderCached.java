package com.tridevmc.architecture.client.render.model.baked;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Table;
import com.mojang.math.Transformation;
import com.tridevmc.architecture.client.render.model.piped.IPipedBakedQuad;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;

/**
 * Extension of {@link BakedQuadContainerProvider} that caches the quad containers for each
 * combination of metadata resolver and transformation.
 *
 * @param <D> The type of the quad metadata.
 */
class BakedQuadContainerProviderCached<D> extends BakedQuadContainerProvider<D> {

    // TODO: Benchmark vs nested HashMap, vs Guava Cache, vs HashMap with custom record for key
    // Record is likely the fastest, but will be more memory intensive - ZGC might make this a non-issue
    // If nested HashMap is faster, then we'll just make a custom class that makes things easier to read
    private final Table<IQuadMetadataResolver<D>, Transformation, IBakedQuadContainer> cache = HashBasedTable.create();

    protected BakedQuadContainerProviderCached(ImmutableList<IPipedBakedQuad<?, ?, D>> quads) {
        super(quads);
    }

    /**
     * Builder for {@link BakedQuadContainerProviderCached}.
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
        public BakedQuadContainerProviderCached<D> build() {
            return new BakedQuadContainerProviderCached<>(this.quads.build());
        }

    }

    @Override
    public IBakedQuadContainer getQuads(IQuadMetadataResolver<D> metadataResolver, Transformation transform, boolean force) {
        // If we're forcing a rebuild, or the cache doesn't contain the data, then rebuild the quad container and cache it.
        if (force || !this.cache.contains(metadataResolver, transform)) {
            var quadContainer = super.getQuads(metadataResolver, transform, true);
            this.cache.put(metadataResolver, transform, quadContainer);
            return quadContainer;
        }
        return this.cache.get(metadataResolver, transform);
    }

}
