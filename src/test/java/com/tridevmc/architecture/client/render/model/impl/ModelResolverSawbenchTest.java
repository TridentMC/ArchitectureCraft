package com.tridevmc.architecture.client.render.model.impl;

import com.tridevmc.architecture.core.model.mesh.CullFace;
import com.tridevmc.architecture.core.model.mesh.FaceDirection;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import com.tridevmc.architecture.testsupport.MinecraftTestBootstrap;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.data.AtlasIds;
import net.minecraft.resources.Identifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MinecraftTestBootstrap.class)
class ModelResolverSawbenchTest {
    @Test
    void sawbenchResolvesAtlasIdsAndRefreshesSpritesAfterReload() {
        var minecraft = mock(Minecraft.class, RETURNS_DEEP_STUBS);
        var atlases = minecraft.getAtlasManager();
        var atlas = mock(TextureAtlas.class);
        when(atlases.getAtlasOrThrow(any())).thenAnswer(call -> {
            assertEquals(AtlasIds.BLOCKS, call.getArgument(0), "AtlasManager requires an atlas ID");
            return atlas;
        });
        var oak = Identifier.withDefaultNamespace("block/oak_planks");
        var iron = Identifier.withDefaultNamespace("block/iron_block");
        var oakSprite = mock(TextureAtlasSprite.class);
        var ironSprite = mock(TextureAtlasSprite.class);
        when(atlas.getSprite(oak)).thenReturn(oakSprite);
        when(atlas.getSprite(iron)).thenReturn(ironSprite);
        try (var client = mockStatic(Minecraft.class)) {
            client.when(Minecraft::getInstance).thenReturn(minecraft);
            ModelResolverSawbench.clearCache();
            var resolver = new ModelResolverSawbench();
            assertSame(oakSprite, resolver.getDefaultSprite());
            assertSame(ironSprite, resolver.getMetadataResolver().getTexture(
                    new PolygonData(CullFace.NONE, FaceDirection.UP, 1, -1)));
            var reloaded = mock(TextureAtlasSprite.class);
            when(atlas.getSprite(oak)).thenReturn(reloaded);
            ModelResolverSawbench.clearCache();
            assertSame(reloaded, resolver.getDefaultSprite());
            verify(atlases, times(2)).getAtlasOrThrow(AtlasIds.BLOCKS);
        } finally {
            ModelResolverSawbench.clearCache();
        }
    }
}
