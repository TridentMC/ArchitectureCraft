package com.tridevmc.architecture.client.render.model.baked;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.IPolygonData;
import org.jetbrains.annotations.Nullable;


/**
 * An extension of {@link BakedQuadContainerProviderMesh} that caches the quad containers it generates.
 *
 * @param <I> The type of the part ID.
 * @param <D> The type of the polygon data.
 */
public class BakedQuadContainerProviderMeshCached<I, D extends IPolygonData<D>> extends BakedQuadContainerProviderMesh<I, D> {

    private final Table<IQuadMetadataResolver<D>, ITrans3, IBakedQuadContainer> cache = Tables.synchronizedTable(HashBasedTable.create());

    public BakedQuadContainerProviderMeshCached(IMesh<I, D> mesh) {
        super(mesh);
    }

    @Override
    public IBakedQuadContainer getQuads(@Nullable I partId, IQuadMetadataResolver<D> metadataResolver, ITrans3 transform, boolean forceRebuild) {
        // If we're forcing a rebuild, or the cache doesn't contain the data, then rebuild the quad container and cache it.
        if (forceRebuild || !this.cache.contains(metadataResolver, transform)) {
            var quadContainer = super.getQuads(partId, metadataResolver, transform, forceRebuild);
            this.cache.put(metadataResolver, transform, quadContainer);
            return quadContainer;
        }
        return this.cache.get(metadataResolver, transform);
    }

}
