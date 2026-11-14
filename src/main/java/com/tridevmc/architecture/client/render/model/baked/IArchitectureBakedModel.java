package com.tridevmc.architecture.client.render.model.baked;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.DynamicBlockStateModel;

import java.util.List;

/**
 * Extension to {@link DynamicBlockStateModel} that delegates to our own block and item quad methods.
 */
public interface IArchitectureBakedModel extends DynamicBlockStateModel {
    /**
     * Gets the quads for the given level, position, and state.
     *
     * @param level The level to get quads for.
     * @param pos   The position of the block.
     * @param state The state of the block.
     * @return A container of quads, grouped into culled faces and general quads.
     */
    IBakedQuadContainer getQuads(BlockGetter level, BlockPos pos, BlockState state);

    /**
     * Gets a list of quads for the given stack.
     *
     * @param stack The stack to get quads for.
     * @return A list of quads.
     */
    List<BakedQuad> getQuads(ItemStack stack);

    ItemTransforms getTransforms();

    TextureAtlasSprite getParticleIcon();

    default boolean useAmbientOcclusion() { return true; }

    default boolean usesBlockLight() { return true; }

    default boolean isGui3d() { return true; }

    @Override
    default void collectParts(BlockAndTintGetter level, BlockPos pos, BlockState state,
                              RandomSource random, List<BlockStateModelPart> parts) {
        var quads = this.getQuads(level, pos, state);
        var material = this.particleMaterial(level, pos, state);
        parts.add(new BlockStateModelPart() {
            @Override
            public List<BakedQuad> getQuads(@Nullable Direction direction) {
                return quads.quadsFor(direction);
            }
            @Override
            public boolean useAmbientOcclusion() { return IArchitectureBakedModel.this.useAmbientOcclusion(); }
            @Override
            public Material.Baked particleMaterial() { return material; }
            @Override
            public int materialFlags() {
                return quads.allQuads().stream().mapToInt(q -> q.materialInfo().flags()).reduce(0, (a, b) -> a | b);
            }
        });
    }

    @Override
    default Material.Baked particleMaterial() {
        return new Material.Baked(this.getParticleIcon(), false);
    }

    @Override
    default int materialFlags() { return BakedQuad.FLAG_TRANSLUCENT | BakedQuad.FLAG_ANIMATED; }

    default ItemStackRenderState.LayerRenderState appendItemLayer(ItemStackRenderState output, ItemStack stack,
                                                                  ItemDisplayContext displayContext) {
        var quads = this.getQuads(stack);
        output.appendModelIdentityElement(this);
        output.appendModelIdentityElement(quads);
        var layer = output.newLayer();
        layer.setQuads(ItemQuads.split(quads));
        layer.setUsesBlockLight(this.usesBlockLight());
        layer.setParticleMaterial(this.particleMaterial());
        layer.setItemTransform(this.getTransforms().getTransform(displayContext));
        layer.setExtents(() -> CuboidItemModelWrapper.computeExtents(quads));
        if (quads.stream().anyMatch(q -> (q.materialInfo().flags() & BakedQuad.FLAG_ANIMATED) != 0)) {
            output.setAnimated();
        }
        if (stack.hasFoil()) {
            layer.setFoilType(ItemStackRenderState.FoilType.STANDARD);
            output.appendModelIdentityElement(ItemStackRenderState.FoilType.STANDARD);
            output.setAnimated();
        }
        return layer;
    }
}
