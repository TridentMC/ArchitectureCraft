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

package com.tridevmc.architecture.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.tridevmc.architecture.client.render.model.IArchitectureModel;
import com.tridevmc.architecture.client.render.target.RenderTargetBaked;
import com.tridevmc.architecture.client.render.texture.ITexture;
import com.tridevmc.architecture.client.render.texture.TextureBase;
import com.tridevmc.architecture.common.ArchitectureLog;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.item.ItemArchitecture;
import com.tridevmc.architecture.common.render.ITextureConsumer;
import com.tridevmc.architecture.common.render.ModelSpec;
import com.tridevmc.architecture.legacy.base.ArchitectureModelRenderer;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;

import javax.annotation.Nullable;
import java.util.*;

import static org.lwjgl.opengl.GL11.GL_SMOOTH;

public class RenderingManager {

    protected static Trans3 itemTrans = Trans3.blockCenterSideTurn(0, 2);
    protected static String[] texturePrefixes = {"blocks/", "textures/"};
    protected Map<Block, ICustomRenderer> blockRenderers = new HashMap<Block, ICustomRenderer>();
    protected Map<Item, ICustomRenderer> itemRenderers = new HashMap<Item, ICustomRenderer>();
    protected Map<BlockState, ICustomRenderer> stateRendererCache = new HashMap<BlockState, ICustomRenderer>();
    protected Map<ResourceLocation, ITexture> textureCache = new HashMap<ResourceLocation, ITexture>();
    protected List<IBakedModel> bakedModels = new ArrayList<>();
    protected CustomItemBakedModel itemBakedModel;

    public List<IBakedModel> getBakedModels() {
        return this.bakedModels;
    }


    public boolean blockNeedsCustomRendering(Block block) {
        return this.blockRenderers.containsKey(block) || this.specifiesTextures(block);
    }

    public boolean itemNeedsCustomRendering(Item item) {
        return this.itemRenderers.containsKey(item) || this.specifiesTextures(item);
    }

    public void registerModelLocationForItem(Item item, CustomItemBakedModel disp) {
        this.registerModelLocationForSubtypes(item, disp.location);
    }

    protected void registerModelLocationForSubtypes(Item item, ModelResourceLocation location) {
        int numVariants = this.getNumItemSubtypes(item);
        for (int i = 0; i < numVariants; i++) {
            this.registerMesh(item, location);
        }
    }

    private void registerMesh(Item item, ModelResourceLocation resourceLocation) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.getItemRenderer() != null && mc.getItemRenderer().getItemModelMesher() != null) {
            mc.getItemRenderer().getItemModelMesher().register(item, resourceLocation);
        }
    }

    private int getNumBlockSubtypes(Block block) {
        if (block instanceof BlockArchitecture)
            return ((BlockArchitecture) block).getNumSubtypes();
        else
            return 1;
    }

    private int getNumItemSubtypes(Item item) {
        if (item instanceof ItemArchitecture)
            return ((ItemArchitecture) item).getNumSubtypes();
        else if (item instanceof BlockItem)
            return this.getNumBlockSubtypes(Block.getBlockFromItem(item));
        else
            return 1;
    }

    protected boolean specifiesTextures(Object obj) {
        return obj instanceof ITextureConsumer && ((ITextureConsumer) obj).getTextureNames() != null;
    }

    public IArchitectureModel getModel(String name) {
        return ArchitectureMod.PROXY.getModel(name);
    }

    public ICustomRenderer getCustomRenderer(IBlockReader world, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        ICustomRenderer rend = this.blockRenderers.get(block);
        if (rend == null && block instanceof BlockArchitecture) {
            BlockState astate = block.getExtendedState(state, world, pos);
            rend = this.getCustomRendererForState(astate);
        }
        return rend;
    }

    protected ICustomRenderer getCustomRendererForSpec(int textureType, ModelSpec spec) {
        IArchitectureModel model = this.getModel(spec.modelName);
        ITexture[] textures = new ITexture[spec.textureNames.length];
        for (int i = 0; i < textures.length; i++)
            textures[i] = this.getTexture(textureType, spec.textureNames[i]);
        return new ArchitectureModelRenderer(model, spec.origin, textures);
    }

    public ICustomRenderer getCustomRendererForState(BlockState state) {
        ICustomRenderer rend = this.stateRendererCache.get(state);
        if (rend == null) {
            Block block = state.getBlock();
            if (block instanceof BlockArchitecture) {
                ModelSpec spec = ((BlockArchitecture) block).getModelSpec(state);
                if (spec != null) {
                    rend = this.getCustomRendererForSpec(0, spec);
                    this.stateRendererCache.put(state, rend);
                } else {
                    if (this.blockNeedsCustomRendering(block)) {
                        return this.blockRenderers.get(block);
                    }
                }
            }
        }
        return rend;
    }

    public ResourceLocation textureResourceLocation(int type, String name) {
        // TextureMap adds "textures/"
        return new ResourceLocation(ArchitectureMod.MOD_ID, texturePrefixes[type] + name);
    }

    public ITexture getTexture(int type, String name) {
        // Cache is keyed by resource locaton without "textures/"
        ResourceLocation loc = this.textureResourceLocation(type, name);
        return this.textureCache.get(loc);
    }

    public void registerSprites(int textureType, AtlasTexture reg, Object obj) {
        if (obj instanceof ITextureConsumer) {
            String[] names = ((ITextureConsumer) obj).getTextureNames();
            if (names != null) {
                for (String name : names) {
                    ResourceLocation loc = this.textureResourceLocation(textureType, name);
                    if (this.textureCache.get(loc) == null) {
                        TextureAtlasSprite icon = reg.getSprite(loc);
                        ITexture texture = TextureBase.fromSprite(icon);
                        this.textureCache.put(loc, texture);
                    }
                }
            }
        }
    }


    public TextureAtlasSprite getIcon(int type, String name) {
        return ((TextureBase.Sprite) this.getTexture(type, name)).icon;
    }

    public BlockState getBlockParticleState(BlockState state, IBlockReader world, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof BlockArchitecture)
            return ((BlockArchitecture) block).getParticleState(world, pos);
        else
            return block.getExtendedState(state, world, pos);
    }

    public boolean pathUsesVertexModel(String resourcePath) {
        if (resourcePath.contains("item")) {
            return false;
        }
        return this.blockRenderers.keySet().stream().anyMatch(block -> resourcePath.contains(block.getRegistryName().getPath()));
    }

    public void addBlockRenderer(Block block, ICustomRenderer renderer) {
        this.blockRenderers.put(block, renderer);
    }

    public void addItemRenderer(Item item, ICustomRenderer renderer) {
        this.itemRenderers.put(item, renderer);
    }

    public CustomItemBakedModel getItemBakedModel() {
        if (this.itemBakedModel == null)
            this.itemBakedModel = new CustomItemBakedModel();
        return this.itemBakedModel;
    }

    public void clearTextureCache() {
        this.textureCache.clear();
    }

    public abstract class CustomBakedModel implements IBakedModel {
        public ModelResourceLocation location;

        public void install(ModelBakeEvent event) {
            event.getModelRegistry().put(this.location, this);
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
            return null;
        }

    }

    public class BlockParticleModel extends CustomBakedModel {

        protected BlockState state;

        public BlockParticleModel(BlockState state, ModelResourceLocation location) {
            this.state = state;
            this.location = location;
        }

        @Override
        public ItemOverrideList getOverrides() {
            return null;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState iBlockState, @Nullable Direction enumFacing, Random random) {
            return Collections.emptyList();
        }

        @Override
        public boolean func_230044_c_() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            Block block = this.state.getBlock();
            if (block instanceof BlockArchitecture) {
                String[] textures = ((BlockArchitecture) block).getTextureNames();
                if (textures != null && textures.length > 0)
                    return RenderingManager.this.getIcon(0, textures[0]);
            }
            return null;

        }
    }

    public IBakedModel getCustomBakedModel(BlockState state, ModelResourceLocation resourceLocation) {
        return new BlockParticleModel(state, resourceLocation);
    }

    public class CustomItemRenderOverrideList extends ItemOverrideList {

        private IBakedModel emptyModel = new IBakedModel() {
            @Override
            public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
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
            public boolean func_230044_c_() {
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
                return ItemOverrideList.EMPTY;
            }
        };

        public CustomItemRenderOverrideList() {
            super();
        }

        @Override
        public IBakedModel getModelWithOverrides(IBakedModel originalModel, ItemStack stack, World world, LivingEntity entity) {
            Item item = stack.getItem();
            ICustomRenderer rend = RenderingManager.this.itemRenderers.get(item);
            if (rend == null && item instanceof ItemArchitecture) {
                ModelSpec spec = ((ItemArchitecture) item).getModelSpec(stack);
                if (spec != null)
                    rend = RenderingManager.this.getCustomRendererForSpec(1, spec);
            }
            if (rend == null) {
                Block block = Block.getBlockFromItem(item);
                if (block != null)
                    rend = RenderingManager.this.getCustomRendererForState(block.getDefaultState());
            }
            if (rend != null) {
                try {
                    GlStateManager.shadeModel(GL_SMOOTH);
                } catch (RuntimeException e) {
                    ArchitectureLog.warn("Failed to enable smooth shading for item models, {}", e.getMessage());
                }
                RenderTargetBaked target = new RenderTargetBaked();
                rend.renderItemStack(stack, target, itemTrans);
                return target.getBakedModel();
            } else
                return this.emptyModel;
        }

    }

    public class CustomItemBakedModel extends CustomBakedModel {

        protected ItemOverrideList itemOverrideList = new CustomItemRenderOverrideList();

        public CustomItemBakedModel() {
            this.location = new ModelResourceLocation(new ResourceLocation(ArchitectureMod.MOD_ID, "__custitem__"), "");
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState iBlockState, @Nullable Direction enumFacing, Random random) {
            return Collections.emptyList();
        }

        @Override
        public boolean func_230044_c_() {
            return false;
        }

        @Override
        public ItemOverrideList getOverrides() {
            return this.itemOverrideList;
        }
    }
}
