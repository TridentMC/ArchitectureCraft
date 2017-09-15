package com.elytradev.architecture.client.render;

import com.elytradev.architecture.client.render.model.IRenderableModel;
import com.elytradev.architecture.client.render.texture.ITexture;
import com.elytradev.architecture.client.render.texture.TextureBase;
import com.elytradev.architecture.common.block.BlockArchitecture;
import com.elytradev.architecture.common.item.ItemArchitecture;
import com.elytradev.architecture.common.render.ITextureConsumer;
import com.elytradev.architecture.common.render.ModelSpec;
import com.elytradev.architecture.legacy.base.*;
import com.elytradev.architecture.legacy.common.ArchitectureCraft;
import com.elytradev.architecture.legacy.common.helpers.Trans3;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;

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
    protected boolean customRenderingRequired;
    protected CustomBlockStateMapper blockStateMapper = new CustomBlockStateMapper();
    protected List<BaseRenderingManager.CustomBakedModel> bakedModels = new ArrayList<>();
    protected CustomItemBakedModel itemBakedModel;
    private Map<ResourceLocation, IRenderableModel> modelCache;


    protected boolean blockNeedsCustomRendering(Block block) {
        return blockRenderers.containsKey(block) || specifiesTextures(block);
    }

    protected boolean itemNeedsCustomRendering(Item item) {
        return itemRenderers.containsKey(item) || specifiesTextures(item);
    }

    protected boolean specifiesTextures(Object obj) {
        return obj instanceof ITextureConsumer && ((ITextureConsumer) obj).getTextureNames() != null;
    }

    public ResourceLocation modelLocation(String path) {
        return new ResourceLocation(ArchitectureCraft.MOD_ID, "models/" + path);
    }

    public IRenderableModel getModel(String name) {
        ResourceLocation loc = modelLocation(name);
        IRenderableModel model = modelCache.get(loc);
        if (model == null) {
            model = BaseRenderable.fromResource(loc);
            modelCache.put(loc, model);
        }
        return model;
    }

    public ICustomRenderer getCustomRenderer(IBlockAccess world, BlockPos pos, IBlockState state) {
        //System.out.printf("BaseModClient.getCustomRenderer: %s\n", state);
        Block block = state.getBlock();
        ICustomRenderer rend = blockRenderers.get(block);
        if (rend == null && block instanceof BlockArchitecture /*&& block.getRenderType() == -1*/) {
            IBlockState astate = block.getActualState(state, world, pos);
            rend = getCustomRendererForState(astate);
        }
        return rend;
    }

    protected ICustomRenderer getCustomRendererForSpec(int textureType, ModelSpec spec) {
        IRenderableModel model = getModel(spec.modelName);
        ITexture[] textures = new ITexture[spec.textureNames.length];
        for (int i = 0; i < textures.length; i++)
            textures[i] = getTexture(textureType, spec.textureNames[i]);
        return new BaseModelRenderer(model, spec.origin, textures);
    }

    protected ICustomRenderer getCustomRendererForState(IBlockState astate) {
        ICustomRenderer rend = stateRendererCache.get(astate);
        if (rend == null) {
            Block block = astate.getBlock();
            if (block instanceof BlockArchitecture) {
                ModelSpec spec = ((BlockArchitecture) block).getModelSpec(astate);
                if (spec != null) {
                    rend = getCustomRendererForSpec(0, spec);
                    stateRendererCache.put(astate, rend);
                }
            }
        }
        return rend;
    }

    public ResourceLocation textureResourceLocation(int type, String name) {
        // TextureMap adds "textures/"
        return new ResourceLocation(ArchitectureCraft.MOD_ID, texturePrefixes[type] + name);
    }

    public ITexture getTexture(int type, String name) {
        // Cache is keyed by resource locaton without "textures/"
        ResourceLocation loc = textureResourceLocation(type, name);
        return textureCache.get(loc);
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

    protected static class CustomBlockStateMapper extends DefaultStateMapper {
        @Override
        public ModelResourceLocation getModelResourceLocation(IBlockState state) {
            return super.getModelResourceLocation(state);
        }
    }

    protected abstract class CustomBakedModel implements IBakedModel {

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

    protected class BlockParticleModel extends CustomBakedModel {

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

    protected class CustomItemRenderOverrideList extends ItemOverrideList {

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
                GlStateManager.shadeModel(GL_SMOOTH);
                BaseBakedRenderTarget target = new BaseBakedRenderTarget();
                rend.renderItemStack(stack, target, itemTrans);
                return target.getBakedModel();
            } else
                return emptyModel;
        }

    }

    protected class CustomItemBakedModel extends CustomBakedModel {

        protected ItemOverrideList itemOverrideList = new CustomItemRenderOverrideList();

        public CustomItemBakedModel() {
            this.location = new ModelResourceLocation(new ResourceLocation(ArchitectureCraft.MOD_ID, "__custitem__"), "");
        }

        @Override
        public ItemOverrideList getOverrides() {
            return itemOverrideList;
        }
    }
}
