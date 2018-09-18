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

package com.elytradev.architecture.client.render;

import com.elytradev.architecture.client.render.model.OBJSONModel;
import com.elytradev.architecture.client.render.model.IArchitectureModel;
import com.elytradev.architecture.client.render.target.RenderTargetBaked;
import com.elytradev.architecture.client.render.texture.ITexture;
import com.elytradev.architecture.client.render.texture.TextureBase;
import com.elytradev.architecture.common.ArchitectureLog;
import com.elytradev.architecture.common.ArchitectureMod;
import com.elytradev.architecture.common.block.BlockArchitecture;
import com.elytradev.architecture.common.helpers.Trans3;
import com.elytradev.architecture.common.item.ItemArchitecture;
import com.elytradev.architecture.common.render.ITextureConsumer;
import com.elytradev.architecture.common.render.ModelSpec;
import com.elytradev.architecture.legacy.base.ArchitectureModelRenderer;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_SMOOTH;

public class RenderingManager {

    protected static Trans3 itemTrans = Trans3.blockCenterSideTurn(0, 2);
    protected static String[] texturePrefixes = {"blocks/", "textures/"};
    protected Map<Block, ICustomRenderer> blockRenderers = new HashMap<Block, ICustomRenderer>();
    protected Map<Item, ICustomRenderer> itemRenderers = new HashMap<Item, ICustomRenderer>();
    protected Map<IBlockState, ICustomRenderer> stateRendererCache = new HashMap<IBlockState, ICustomRenderer>();
    protected Map<ResourceLocation, ITexture> textureCache = new HashMap<ResourceLocation, ITexture>();
    protected CustomBlockStateMapper blockStateMapper = new CustomBlockStateMapper();
    protected List<IBakedModel> bakedModels = new ArrayList<>();
    protected CustomItemBakedModel itemBakedModel;

    public List<IBakedModel> getBakedModels() {
        return bakedModels;
    }

    public CustomBlockStateMapper getBlockStateMapper() {
        return blockStateMapper;
    }

    public boolean blockNeedsCustomRendering(Block block) {
        return blockRenderers.containsKey(block) || specifiesTextures(block);
    }

    public boolean itemNeedsCustomRendering(Item item) {
        return itemRenderers.containsKey(item) || specifiesTextures(item);
    }

    public void registerModelLocationForItem(Item item, CustomItemBakedModel disp) {
        registerModelLocationForSubtypes(item, disp.location);
    }

    protected void registerModelLocationForSubtypes(Item item, ModelResourceLocation location) {
        int numVariants = getNumItemSubtypes(item);
        for (int i = 0; i < numVariants; i++) {
            registerMesh(item, i, location);
        }
    }

    private void registerMesh(Item item, int meta, ModelResourceLocation resourceLocation) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.getRenderItem() != null && mc.getRenderItem().getItemModelMesher() != null) {
            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, meta, resourceLocation);
        } else {
            ModelLoader.setCustomModelResourceLocation(item, meta, resourceLocation);
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
        else if (item instanceof ItemBlock)
            return getNumBlockSubtypes(Block.getBlockFromItem(item));
        else
            return 1;
    }

    protected boolean specifiesTextures(Object obj) {
        return obj instanceof ITextureConsumer && ((ITextureConsumer) obj).getTextureNames() != null;
    }

    public IArchitectureModel getModel(String name) {
        return ArchitectureMod.PROXY.getModel(name);
    }

    public ICustomRenderer getCustomRenderer(IBlockAccess world, BlockPos pos, IBlockState state) {
        Block block = state.getBlock();
        ICustomRenderer rend = blockRenderers.get(block);
        if (rend == null && block instanceof BlockArchitecture) {
            IBlockState astate = block.getActualState(state, world, pos);
            rend = getCustomRendererForState(astate);
        }
        return rend;
    }

    protected ICustomRenderer getCustomRendererForSpec(int textureType, ModelSpec spec) {
        IArchitectureModel model = getModel(spec.modelName);
        ITexture[] textures = new ITexture[spec.textureNames.length];
        for (int i = 0; i < textures.length; i++)
            textures[i] = getTexture(textureType, spec.textureNames[i]);
        return new ArchitectureModelRenderer(model, spec.origin, textures);
    }

    public ICustomRenderer getCustomRendererForState(IBlockState state) {
        ICustomRenderer rend = stateRendererCache.get(state);
        if (rend == null) {
            Block block = state.getBlock();
            if (block instanceof BlockArchitecture) {
                ModelSpec spec = ((BlockArchitecture) block).getModelSpec(state);
                if (spec != null) {
                    rend = getCustomRendererForSpec(0, spec);
                    stateRendererCache.put(state, rend);
                } else {
                    if (blockNeedsCustomRendering(block)) {
                        return blockRenderers.get(block);
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
        ResourceLocation loc = textureResourceLocation(type, name);
        return textureCache.get(loc);
    }

    public void registerSprites(int textureType, TextureMap reg, Object obj) {
        if (obj instanceof ITextureConsumer) {
            String names[] = ((ITextureConsumer) obj).getTextureNames();
            if (names != null) {
                for (String name : names) {
                    ResourceLocation loc = textureResourceLocation(textureType, name);
                    if (textureCache.get(loc) == null) {
                        TextureAtlasSprite icon = reg.registerSprite(loc);
                        ITexture texture = TextureBase.fromSprite(icon);
                        textureCache.put(loc, texture);
                    }
                }
            }
        }
    }


    public TextureAtlasSprite getIcon(int type, String name) {
        return ((TextureBase.Sprite) getTexture(type, name)).icon;
    }

    public IBlockState getBlockParticleState(IBlockState state, IBlockAccess world, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof BlockArchitecture)
            return ((BlockArchitecture) block).getParticleState(world, pos);
        else
            return block.getActualState(state, world, pos);
    }

    public boolean pathUsesVertexModel(String resourcePath) {
        if (resourcePath.contains("item")) {
            return false;
        }
        return blockRenderers.keySet().stream().anyMatch(block -> resourcePath.contains(block.getRegistryName().getPath()));
    }

    public void addBlockRenderer(Block block, ICustomRenderer renderer) {
        blockRenderers.put(block, renderer);
    }

    public void addItemRenderer(Item item, ICustomRenderer renderer) {
        itemRenderers.put(item, renderer);
    }

    public CustomItemBakedModel getItemBakedModel() {
        if (itemBakedModel == null)
            itemBakedModel = new CustomItemBakedModel();
        return itemBakedModel;
    }

    public void clearTextureCache() {
        textureCache.clear();
    }

    public static class CustomBlockStateMapper extends DefaultStateMapper {
        @Override
        public ModelResourceLocation getModelResourceLocation(IBlockState state) {
            return super.getModelResourceLocation(state);
        }
    }

    public abstract class CustomBakedModel implements IBakedModel {
        public ModelResourceLocation location;

        public void install(ModelBakeEvent event) {
            event.getModelRegistry().putObject(location, this);
        }

        @Override
        public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
            return null;
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

        protected IBlockState state;

        public BlockParticleModel(IBlockState state, ModelResourceLocation location) {
            this.state = state;
            this.location = location;
        }

        @Override
        public ItemOverrideList getOverrides() {
            return null;
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            Block block = state.getBlock();
            if (block instanceof BlockArchitecture) {
                String[] textures = ((BlockArchitecture) block).getTextureNames();
                if (textures != null && textures.length > 0)
                    return getIcon(0, textures[0]);
            }
            return null;

        }
    }

    public IBakedModel getCustomBakedModel(IBlockState state, ModelResourceLocation resourceLocation) {
        return new BlockParticleModel(state, resourceLocation);
    }

    public class CustomItemRenderOverrideList extends ItemOverrideList {

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

        public CustomItemRenderOverrideList() {
            super(ImmutableList.of());
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
            Item item = stack.getItem();
            ICustomRenderer rend = itemRenderers.get(item);
            if (rend == null && item instanceof ItemArchitecture) {
                ModelSpec spec = ((ItemArchitecture) item).getModelSpec(stack);
                if (spec != null)
                    rend = getCustomRendererForSpec(1, spec);
            }
            if (rend == null) {
                Block block = Block.getBlockFromItem(item);
                if (block != null)
                    rend = getCustomRendererForState(block.getDefaultState());
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
                return emptyModel;
        }

    }

    public class CustomItemBakedModel extends CustomBakedModel {

        protected ItemOverrideList itemOverrideList = new CustomItemRenderOverrideList();

        public CustomItemBakedModel() {
            this.location = new ModelResourceLocation(new ResourceLocation(ArchitectureMod.MOD_ID, "__custitem__"), "");
        }

        @Override
        public ItemOverrideList getOverrides() {
            return itemOverrideList;
        }
    }
}
