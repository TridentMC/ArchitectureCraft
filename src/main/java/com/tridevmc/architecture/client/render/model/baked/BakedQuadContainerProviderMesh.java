package com.tridevmc.architecture.client.render.model.baked;

import com.google.common.collect.Maps;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.ITrans3Immutable;
import com.tridevmc.architecture.core.model.mesh.CullFace;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.IPolygonData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.pipeline.QuadBakingVertexConsumer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

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
    public IBakedQuadContainer getQuads(@Nullable I partId, LevelAccessor level, BlockPos pos, BlockState state, IQuadMetadataResolver<D> metadataResolver, ITrans3 transform, boolean forceRebuild) {
        return this.getQuadsTakesAll(partId, level, pos, state, null, metadataResolver, transform, forceRebuild);
    }

    @Override
    public IBakedQuadContainer getQuads(@Nullable I partId, ItemStack stack, IQuadMetadataResolver<D> metadataResolver, ITrans3 transform, boolean forceRebuild) {
        return this.getQuadsTakesAll(partId, null, null, null, stack, metadataResolver, transform, forceRebuild);
    }

    @Override
    public IBakedQuadContainer getQuads(@Nullable I partId, IQuadMetadataResolver<D> metadataResolver, ITrans3 transform, boolean forceRebuild) {
        return this.getQuadsTakesAll(partId, null, null, null, null, metadataResolver, transform, forceRebuild);
    }

    private final IBakedQuadContainer getQuadsTakesAll(@Nullable I partId, @Nullable LevelAccessor level, @Nullable BlockPos pos, @Nullable BlockState state, @Nullable ItemStack stack, IQuadMetadataResolver<D> metadataResolver, ITrans3 transform, boolean forceRebuild) {
        // We ignore forceRebuild here, as we're not actually caching the resulting containers themselves.
        var containerBuilder = new BakedQuadContainer.Builder();
        final var isCulled = new AtomicBoolean(false);
        var quadBaker = new QuadBakingVertexConsumer(bakedQuad -> containerBuilder.addQuad(bakedQuad, isCulled.get()));
        var m = this.getMesh(transform.asImmutable());
        var faces = partId == null ? m.getFaces() : Objects.requireNonNull(m.getPart(partId), "Unable to find part with id: " + partId + " on mesh: " + this.mesh.getName()).getFaces();
        for (var face : faces) {
            for (var polygon : face.getPolygons()) {
                var polygonData = polygon.getPolygonData();
                isCulled.set(polygonData.cullFace() != CullFace.NONE);
                var texture = metadataResolver.getTexture(level, pos, state, stack, polygonData);
                var tintIndex = metadataResolver.getTintIndex(level, pos, state, stack, polygonData);
                var vertexCount = polygon.getVertexCount();
                quadBaker.setSprite(texture);
                quadBaker.setTintIndex(tintIndex);
                quadBaker.setDirection(polygonData.face().toDirection());
                quadBaker.setShade(true);
                quadBaker.setHasAmbientOcclusion(true);
                var startIndex = 0;
                if (polygon.getVertexCount() == 3) {
                    startIndex = -1;
                }
                for (int i = startIndex; i < vertexCount; i++) {
                    var vertexIndex = Math.max(0, i);
                    var v = polygon.getVertex(vertexIndex);
                    quadBaker.vertex(v.getX(), v.getY(), v.getZ())
                            .color(-1)
                            .normal((float) v.getNormalX(), (float) v.getNormalY(), (float) v.getNormalZ())
                            .uv(texture.getU((float) v.getU()), texture.getV((float) v.getV()))
                            .uv2(1, 0)
                            .overlayCoords(1, 0)
                            .endVertex();
                }
            }
        }
        return containerBuilder.build();
    }

}
