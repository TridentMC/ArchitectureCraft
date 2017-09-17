/*
 * MIT License
 *
 * Copyright (c) 2017 Benjamin K
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.elytradev.architecture.client.render.model;

import com.elytradev.architecture.client.proxy.ClientProxy;
import com.elytradev.architecture.client.render.ICustomRenderer;
import com.elytradev.architecture.client.render.RenderingManager;
import com.elytradev.architecture.client.render.target.RenderTargetBaked;
import com.elytradev.architecture.common.block.BlockArchitecture;
import com.elytradev.architecture.common.helpers.Trans3;
import com.elytradev.architecture.common.helpers.Utils;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class VertexBakedModel implements IBakedModel {

    private IBakedModel emptyModel = new IBakedModel() {
        @Override
        public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
            return Lists.newArrayList();
        }

        @Override
        public boolean isAmbientOcclusion() {
            return false;
        }

        @Override
        public boolean isGui3d() {
            return false;
        }

        @Override
        public boolean isBuiltInRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return null;
        }

        @Override
        public ItemCameraTransforms getItemCameraTransforms() {
            return ItemCameraTransforms.DEFAULT;
        }

        @Override
        public ItemOverrideList getOverrides() {
            return ItemOverrideList.NONE;
        }
    };

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState eState = (IExtendedBlockState) state;
            return getModel(eState.getValue(BlockArchitecture.BLOCKACCESS_PROP), eState.getValue(BlockArchitecture.POS_PROP), state).getQuads(state, side, 0);
        } else {
            return Collections.emptyList();
        }
    }

    private IBakedModel getModel(IBlockAccess world, BlockPos pos, IBlockState state) {
        IBakedModel out = emptyModel;
        RenderingManager renderingManager = ClientProxy.RENDERING_MANAGER;
        ICustomRenderer rend = renderingManager.getCustomRenderer(world, pos, state);
        if (rend != null) {
            Trans3 t = Trans3.blockCenter;
            Block block = state.getBlock();
            // Render model and export bakedmodel if present.
            if (block.canRenderInLayer(state, MinecraftForgeClient.getRenderLayer())) {
                rend = ClientProxy.RENDERING_MANAGER.getCustomRendererForState(state);
                if (rend != null) {
                    RenderTargetBaked target = new RenderTargetBaked();
                    rend.renderBlock(world, pos, state, target, MinecraftForgeClient.getRenderLayer(), t);
                    out = target.getBakedModel();
                }
            }
        }

        return out;
    }

    @Override
    public boolean isAmbientOcclusion() {
        return true;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean isBuiltInRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        // no blockstate param
        // TODO: Override default stuff in BlockArchitecture.
        return Utils.getSpriteForBlockState(Blocks.PLANKS.getDefaultState());
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return ItemCameraTransforms.DEFAULT;
    }

    @Override
    public ItemOverrideList getOverrides() {
        return ItemOverrideList.NONE;
    }
}
