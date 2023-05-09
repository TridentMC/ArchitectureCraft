package com.tridevmc.architecture.client.render.model.baked;

import com.google.common.collect.Maps;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.ITrans3Immutable;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.IPolygonData;
import net.minecraftforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * A baked quad container provider that uses a mesh to generate the quads.
 *
 * @param <I> The type of the part id.
 * @param <D> The type of the polygon data.
 */
public class BakedQuadContainerProviderMesh<I, D extends IPolygonData<D>> implements IMultipartBakedQuadContainerProvider<I, D> {

    private final IMesh<I, D> mesh;
    private final Map<ITrans3Immutable, IMesh<I, D>> cache;

    public BakedQuadContainerProviderMesh(IMesh<I, D> mesh) {
        this.mesh = mesh;
        // Chunk building is multithreaded, so we need a concurrent map here.
        this.cache = Maps.newConcurrentMap();
        this.cache.put(ITrans3Immutable.IDENTITY, mesh);
    }

    private IMesh<I, D> getMesh(ITrans3Immutable transform) {
        return this.cache.computeIfAbsent(transform, t -> this.mesh.transform(t, true));
    }

    @Override
    public IBakedQuadContainer getQuads(@Nullable I partId, IQuadMetadataResolver<D> metadataResolver, ITrans3 transform, boolean forceRebuild) {
        // We ignore forceRebuild here, as we're not actually caching the resulting containers themselves.
        var containerBuilder = new BakedQuadContainer.Builder();
        var quadBaker = new QuadBakingVertexConsumer(containerBuilder::addQuad);
        var m = this.getMesh(transform.asImmutable());
        var faces = partId == null ? m.getFaces() : m.getPart(partId).getFaces();
        for (var face : faces) {
            for (var polygon : face.getPolygons()) {
                var polygonData = polygon.getPolygonData();
                var texture = metadataResolver.getTexture(polygonData);
                var tintIndex = metadataResolver.getTintIndex(polygonData);
                quadBaker.setSprite(texture);
                quadBaker.setTintIndex(tintIndex);
                quadBaker.setDirection(polygonData.getCullFace().toDirection());
                for (int i = 0; i < polygon.getVertexCount(); i++) {
                    var v = polygon.getVertex(i);
                    quadBaker.vertex(v.getX(), v.getY(), v.getZ())
                            .normal((float) v.getNormalX(), (float) v.getNormalY(), (float) v.getNormalZ())
                            .uv(texture.getU(v.getU()), texture.getV(v.getV()))
                            .uv2(1, 0)
                            .overlayCoords(1, 0)
                            .endVertex();
                }
            }
        }
        return containerBuilder.build();
    }

}
