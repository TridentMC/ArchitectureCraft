package com.tridevmc.architecture.client.render.model.impl;

import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.core.model.mesh.PolygonData;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

final class ShapeQuadMetadataResolver implements IQuadMetadataResolver<PolygonData> {
    private final Material.Baked base;
    private final Material.Baked secondary;

    ShapeQuadMetadataResolver(Material.Baked base, Material.Baked secondary) {
        this.base = base;
        this.secondary = secondary;
    }

    @Override
    public TextureAtlasSprite getTexture(PolygonData data) {
        return (data.textureIndex() == 0 ? this.base : this.secondary).sprite();
    }

    @Override
    public Material.Baked getMaterial(BlockGetter level, BlockPos pos, BlockState state,
                                      ItemStack stack, PolygonData data) {
        return data.textureIndex() == 0 ? this.base : this.secondary;
    }

    @Override
    public int getTintIndex(PolygonData data) {
        return -1;
    }
}
