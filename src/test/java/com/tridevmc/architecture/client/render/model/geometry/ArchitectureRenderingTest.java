package com.tridevmc.architecture.client.render.model.geometry;

import com.tridevmc.architecture.client.render.model.baked.BakedQuadContainerProviderMesh;
import com.tridevmc.architecture.client.render.model.baked.IArchitectureBakedModel;
import com.tridevmc.architecture.client.render.model.baked.IBakedQuadContainer;
import com.tridevmc.architecture.client.render.model.impl.ModelResolverShapeGeneric;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.common.block.entity.BlockEntityShape;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.ShapeMeshes;
import com.tridevmc.architecture.core.math.IMatrix4Immutable;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.model.mesh.CullFace;
import com.tridevmc.architecture.core.model.mesh.Face;
import com.tridevmc.architecture.core.model.mesh.FaceDirection;
import com.tridevmc.architecture.core.model.mesh.Mesh;
import com.tridevmc.architecture.core.model.mesh.Part;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import com.tridevmc.architecture.core.model.mesh.Quad;
import com.tridevmc.architecture.core.model.mesh.Vertex;
import com.tridevmc.architecture.testsupport.MinecraftTestBootstrap;

import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.Transparency;
import com.mojang.serialization.JsonOps;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.model.data.ModelData;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MinecraftTestBootstrap.class)
class ArchitectureRenderingTest {

    @Test
    void itemAtlasIdentityAnimationAndParentTransformFollowRenderedContent() throws Exception {
        try (var resource = getClass().getResourceAsStream("/assets/architecturecraft/items/cladding.json")) {
            assertNotNull(resource);
            var definition = JsonParser.parseReader(new InputStreamReader(resource, StandardCharsets.UTF_8))
                    .getAsJsonObject().getAsJsonObject("model");
            assertEquals("architecturecraft:mesh", definition.get("type").getAsString());
            var cladding = ArchitectureItemModel.CODEC.codec().parse(JsonOps.INSTANCE, definition).getOrThrow();
            assertSame(EnumShape.CLADDING_SHEET, cladding.shape().orElseThrow());
        }
        var stack = mock(ItemStack.class);
        var material = mock(BakedQuad.MaterialInfo.class);
        when(material.itemRenderType()).thenReturn(mock(RenderType.class));
        var firstQuad = mock(BakedQuad.class);
        when(firstQuad.materialInfo()).thenReturn(material);
        when(firstQuad.position(anyInt())).thenReturn(new Vector3f(0.5F, 0.5F, 0.5F));
        var secondQuad = mock(BakedQuad.class);
        when(secondQuad.materialInfo()).thenReturn(material);
        when(secondQuad.position(anyInt())).thenReturn(new Vector3f(0.5F, 0.5F, 0.5F));
        var model = new TestModel(List.of(firstQuad));
        var first = new TrackingItemStackRenderState();
        model.update(first, stack, null, ItemDisplayContext.GUI, null, null, 0);
        assertFalse(first.isAnimated());
        var same = new TrackingItemStackRenderState();
        model.update(same, stack, null, ItemDisplayContext.GUI, null, null, 0);
        assertEquals(first.getModelIdentity(), same.getModelIdentity());

        model.quads = List.of(secondQuad);
        var changedMaterial = new TrackingItemStackRenderState();
        model.update(changedMaterial, stack, null, ItemDisplayContext.GUI, null, null, 0);
        assertNotEquals(first.getModelIdentity(), changedMaterial.getModelIdentity());
        when(material.flags()).thenReturn(BakedQuad.FLAG_ANIMATED);
        var animated = new TrackingItemStackRenderState();
        model.update(animated, stack, null, ItemDisplayContext.GUI, null, null, 0);
        assertTrue(animated.isAnimated());
        when(material.flags()).thenReturn(0);
        when(stack.hasFoil()).thenReturn(true);
        var foil = new TrackingItemStackRenderState();
        model.update(foil, stack, null, ItemDisplayContext.GUI, null, null, 0);
        assertTrue(foil.isAnimated());
        assertNotEquals(changedMaterial.getModelIdentity(), foil.getModelIdentity());

        var parentTransform = new Matrix4f().translation(2, 3, 4);
        var wrapped = ArchitectureItemModel.withTransform(model, parentTransform);
        parentTransform.identity();
        var transformed = new TrackingItemStackRenderState();
        wrapped.update(transformed, stack, null, ItemDisplayContext.GUI, null, null, 0);
        var bounds = transformed.getModelBoundingBox();
        assertEquals(2, bounds.minX, 1.0E-6);
        assertEquals(3, bounds.minY, 1.0E-6);
        assertEquals(4, bounds.minZ, 1.0E-6);
    }

    @Test
    void rotatedUnculledQuadsRetainTheirShadingDirectionAndForcedTransparency() {
        var sprite = mock(TextureAtlasSprite.class);
        when(sprite.atlasLocation()).thenReturn(TextureAtlas.LOCATION_BLOCKS);
        when(sprite.getU(anyFloat())).thenAnswer(call -> call.getArgument(0));
        when(sprite.getV(anyFloat())).thenAnswer(call -> call.getArgument(0));
        var forced = new Material.Baked(sprite, true);
        var resolver = new IQuadMetadataResolver<PolygonData>() {
            @Override
            public TextureAtlasSprite getTexture(PolygonData data) { return sprite; }
            @Override
            public int getTintIndex(PolygonData data) { return -1; }
            @Override
            public Material.Baked getMaterial(BlockGetter level, BlockPos pos, BlockState state,
                                               ItemStack stack, PolygonData data) { return forced; }
        };
        var face = new Face.Builder<PolygonData>().addPolygon(Quad.getProvider(),
                new PolygonData(CullFace.NONE, FaceDirection.UP, 0, -1),
                new Vertex(0, 0.25, 0, 0, 1, 0, 0, 0), new Vertex(0, 0.25, 1, 0, 1, 0, 0, 1),
                new Vertex(1, 0.25, 1, 0, 1, 0, 1, 1), new Vertex(1, 0.25, 0, 0, 1, 0, 1, 0)).build();
        assertEquals(1, face.getNormal().lengthSquared(), 1.0E-9);
        var mesh = new Mesh.Builder<String, PolygonData>("test")
                .addPart(new Part.Builder<String, PolygonData>().setId("root").addFace(face).build()).build();
        var transform = ITrans3.ofImmutable(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, 0, 90));
        var result = new BakedQuadContainerProviderMesh<>(mesh).getQuads(null, resolver, transform);
        assertEquals(1, result.generalQuads().size());
        var quad = result.allQuads().getFirst();
        assertSame(Direction.WEST, quad.direction());
        assertSame(sprite, quad.materialInfo().sprite());
        assertSame(ChunkSectionLayer.TRANSLUCENT, quad.materialInfo().layer());

        try (var minecraft = mockStatic(Minecraft.class); var meshes = mockStatic(ShapeMeshes.class)) {
            var client = mock(Minecraft.class, RETURNS_DEEP_STUBS);
            minecraft.when(Minecraft::getInstance).thenReturn(client);
            meshes.when(() -> ShapeMeshes.getMesh(EnumShape.STAIRS)).thenReturn(mesh);
            var models = client.getModelManager().getBlockStateModelSet();
            when(models.getParticleMaterial(any())).thenReturn(forced);
            var level = mock(BlockGetter.class);
            when(level.getModelData(BlockPos.ZERO)).thenReturn(ModelData.of(BlockEntityShape.MATERIALS,
                    new BlockEntityShape.MaterialStates(Blocks.STONE.defaultBlockState(), Blocks.GLASS.defaultBlockState())));
            var dynamic = new ModelResolverShapeGeneric(EnumShape.STAIRS);
            var first = dynamic.getQuads(level, BlockPos.ZERO, Blocks.STONE.defaultBlockState(), resolver, transform);
            assertSame(first, dynamic.getQuads(level, BlockPos.ZERO, Blocks.STONE.defaultBlockState(), resolver, transform));
            assertSame(ChunkSectionLayer.TRANSLUCENT, first.allQuads().getFirst().materialInfo().layer());
            when(sprite.transparency()).thenReturn(Transparency.NONE);
            when(models.getParticleMaterial(any())).thenReturn(new Material.Baked(sprite, false));
            var opaque = dynamic.getQuads(level, BlockPos.ZERO, Blocks.STONE.defaultBlockState(), resolver, transform);
            assertNotSame(first, opaque);
            assertSame(ChunkSectionLayer.SOLID, opaque.allQuads().getFirst().materialInfo().layer());
            verify(level, never()).getBlockEntity(any());
        } finally {
            ModelResolverShapeGeneric.clearCache();
        }
    }

    private static final class TestModel implements IArchitectureBakedModel {
        private List<BakedQuad> quads;
        private final TextureAtlasSprite sprite = mock(TextureAtlasSprite.class);

        private TestModel(List<BakedQuad> quads) { this.quads = quads; }
        @Override
        public IBakedQuadContainer getQuads(BlockGetter level, BlockPos pos, BlockState state) {
            throw new AssertionError("Item rendering must not query the world");
        }
        @Override
        public List<BakedQuad> getQuads(ItemStack stack) { return this.quads; }
        @Override
        public ItemTransforms getTransforms() { return ItemTransforms.NO_TRANSFORMS; }
        @Override
        public TextureAtlasSprite getParticleIcon() { return this.sprite; }
    }
}
