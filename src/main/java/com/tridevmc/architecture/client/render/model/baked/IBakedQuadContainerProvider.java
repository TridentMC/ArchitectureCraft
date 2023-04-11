package com.tridevmc.architecture.client.render.model.baked;

import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.core.math.ITrans3;

/**
 * Defines an object that can provide a {@link IBakedQuadContainer} from a given metadata resolver and transform.
 *
 * @param <D> The type of metadata stored on the quads of the source model.
 */
public interface IBakedQuadContainerProvider<D> {

    /**
     * Creates or fetches a cached {@link IBakedQuadContainer} using the given metadata resolver and transform and returns it.
     *
     * @param metadataResolver The metadata resolver to use for getting the texture and tint index for each quad.
     * @param transform        The transform to use.
     * @param forceRebuild     Whether to force a rebuild of the quad container, primarily used for debugging.
     * @return The baked quad container.
     */
    IBakedQuadContainer getQuads(IQuadMetadataResolver<D> metadataResolver, ITrans3 transform, boolean forceRebuild);

    /**
     * Creates or fetches a cached {@link IBakedQuadContainer} using the given metadata resolver and transform and returns it.
     *
     * @param metadataResolver The metadata resolver to use for getting the texture and tint index for each quad.
     * @param transform        The transform to use.
     * @return The baked quad container.
     */
    default IBakedQuadContainer getQuads(IQuadMetadataResolver<D> metadataResolver, ITrans3 transform) {
        return this.getQuads(metadataResolver, transform, false);
    }

}
