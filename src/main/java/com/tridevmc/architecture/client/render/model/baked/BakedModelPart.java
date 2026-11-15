package com.tridevmc.architecture.client.render.model.baked;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Direction;

import java.util.List;

final class BakedModelPart implements BlockStateModelPart {
    private final IBakedQuadContainer quads;
    private final Material.Baked particleMaterial;

    BakedModelPart(IBakedQuadContainer quads, Material.Baked particleMaterial) {
        this.quads = quads;
        this.particleMaterial = particleMaterial;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable Direction direction) {
        return this.quads.quadsFor(direction);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }

    @Override
    public Material.Baked particleMaterial() {
        return this.particleMaterial;
    }

    @Override
    public int materialFlags() {
        return this.quads.allQuads().stream().mapToInt(q -> q.materialInfo().flags())
                .reduce(0, (a, b) -> a | b);
    }
}
