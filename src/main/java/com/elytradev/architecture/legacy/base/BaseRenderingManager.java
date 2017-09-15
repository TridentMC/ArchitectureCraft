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

package com.elytradev.architecture.legacy.base;

import com.elytradev.architecture.client.render.ICustomRenderer;
import com.elytradev.architecture.client.render.model.IRenderableModel;
import com.elytradev.architecture.client.render.target.RenderTargetBaked;
import com.elytradev.architecture.client.render.target.RenderTargetBase;
import com.elytradev.architecture.client.render.texture.ITexture;
import com.elytradev.architecture.client.render.texture.TextureBase;
import com.elytradev.architecture.common.block.BlockArchitecture;
import com.elytradev.architecture.common.block.BlockHelper;
import com.elytradev.architecture.common.item.ItemArchitecture;
import com.elytradev.architecture.common.render.ITextureConsumer;
import com.elytradev.architecture.common.render.ModelSpec;
import com.elytradev.architecture.common.helpers.Trans3;
import com.elytradev.concrete.resgen.ConcreteResourcePack;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_SMOOTH;

//TODO: delet this
public class BaseRenderingManager<MOD extends BaseMod<? extends BaseModClient>> {

    protected static Trans3 itemTrans = Trans3.blockCenterSideTurn(0, 2);
    protected static String[] texturePrefixes = {"blocks/", "textures/"};
    public boolean debugRenderingManager = false;
    public boolean debugModelRegistration = false;
    protected BaseModClient<MOD> client;
    protected Map<Block, ICustomRenderer> blockRenderers = new HashMap<Block, ICustomRenderer>();
    protected Map<Item, ICustomRenderer> itemRenderers = new HashMap<Item, ICustomRenderer>();
    protected Map<IBlockState, ICustomRenderer> stateRendererCache = new HashMap<IBlockState, ICustomRenderer>();
    protected Map<ResourceLocation, ITexture> textureCache = new HashMap<ResourceLocation, ITexture>();
    protected boolean customRenderingRequired;
    protected CustomBlockStateMapper blockStateMapper = new CustomBlockStateMapper();
    protected List<CustomBakedModel> bakedModels = new ArrayList<>();

    //-------------- Renderer registration -------------------------------
    protected CustomItemBakedModel itemBakedModel;
    protected BlockRendererDispatcher blockRendererDispatcher;

    //--------------------------------------- Internal --------------------------------------------

    public BaseRenderingManager(BaseModClient client) {
        if (debugRenderingManager)
            System.out.printf("BaseRenderingManager: Creating\n");
        this.client = client;
        MinecraftForge.EVENT_BUS.register(this);
    }

    public void preInit() {
        if (debugRenderingManager)
            System.out.printf("BaseRenderingManager.preInit\n");
        ConcreteResourcePack concreteResourcePack = new ConcreteResourcePack(client.base.modID);
//         registerDummyStateMappers();
    }

    //------------------------------------------------------------------------------------------------

    public void init() {
        //Moved from preInit due to block registration not being complete.
        registerDefaultRenderers();
        registerDefaultModelLocations();
    }

    public void postInit() {
        if (debugRenderingManager)
            System.out.printf("BaseRenderingManager.postInit: customRenderingRequired = %s\n", customRenderingRequired);
        if (customRenderingRequired)
            enableCustomRendering();
    }

    protected void registerDefaultRenderers() {
        for (Block block : client.base.registeredBlocks) {
            if (block instanceof BlockArchitecture) {
                if (!blockRenderers.containsKey(block)) {
                    String name = ((BlockArchitecture) block).getQualifiedRendererClassName();
                    if (name != null) {
                        try {
                            Class cls = Class.forName(name);
                            addBlockRenderer(block, (ICustomRenderer) cls.newInstance());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    public void addBlockRenderer(Block block, ICustomRenderer renderer) {
        blockRenderers.put(block, renderer);
        customRenderingRequired = true;
        Item item = Item.getItemFromBlock(block);
        if (item != null)
            addItemRenderer(item, renderer);
    }

    public void addItemRenderer(Item item, ICustomRenderer renderer) {
        itemRenderers.put(item, renderer);
    }

    protected CustomItemBakedModel getItemBakedModel() {
        if (itemBakedModel == null)
            itemBakedModel = new CustomItemBakedModel();
        return itemBakedModel;
    }

    //------------------------------------------------------------------------------------------------

    protected void registerDefaultModelLocations() {
        CustomItemBakedModel itemDisp = getItemBakedModel();
        for (Block block : client.base.registeredBlocks) {
            Item item = Item.getItemFromBlock(block);
            if (blockNeedsCustomRendering(block)) {
                registerBakedModelsForBlock(block);
                if (item != null)
                    registerModelLocationForItem(item, itemDisp);
            } else
                registerInventoryLocationForItem(item, block.getUnlocalizedName());
        }
        for (Item item : client.base.registeredItems) {
            if (itemNeedsCustomRendering(item))
                registerModelLocationForItem(item, itemDisp);
            else
                registerInventoryLocationForItem(item, item.getUnlocalizedName());
        }
    }

    protected void registerBakedModelsForBlock(Block block) {
        ModelLoader.setCustomStateMapper(block, blockStateMapper);
        for (IBlockState state : block.getBlockState().getValidStates()) {
            ModelResourceLocation location = blockStateMapper.getModelResourceLocation(state);
            CustomBakedModel model = new BlockParticleModel(state, location);
            if (debugModelRegistration)
                System.out.printf("BaseModClient.registerBakedModelsForBlock: Squirrelling %s --> %s\n",
                        location, model);
            bakedModels.add(model);
        }
    }

    protected boolean blockNeedsCustomRendering(Block block) {
        return blockRenderers.containsKey(block) || specifiesTextures(block);
    }

    protected boolean itemNeedsCustomRendering(Item item) {
        return itemRenderers.containsKey(item) || specifiesTextures(item);
    }

    protected boolean specifiesTextures(Object obj) {
        return obj instanceof ITextureConsumer && ((ITextureConsumer) obj).getTextureNames() != null;
    }

    protected void registerModelLocationForItem(Item item, CustomItemBakedModel disp) {
        registerModelLocationForSubtypes(item, disp.location);
    }

    protected void registerInventoryLocationForItem(Item item, String extdName) {
        String name = extdName.substring(5); // strip "item." or "tile."
        registerModelLocationForSubtypes(item, new ModelResourceLocation(name, "inventory"));
    }

    protected void registerModelLocationForSubtypes(Item item, ModelResourceLocation location) {
        int numVariants = getNumItemSubtypes(item);
        if (debugModelRegistration)
            System.out.printf("BaseModClient: Registering model location %s for %d subtypes of %s\n",
                    location, numVariants, item.getUnlocalizedName());
        for (int i = 0; i < numVariants; i++) {
            if (Minecraft.getMinecraft().getRenderItem() != null)
                Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, i, location);
            else
                ModelLoader.setCustomModelResourceLocation(item, i, location);
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

    //------------------------------------------------------------------------------------------------
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
//             System.out.printf("BaseModClient.getCustomRendererForState: %s\n", astate);
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

    public void renderBlockUsingModelSpec(IBlockAccess world, BlockPos pos, IBlockState state,
                                          RenderTargetBase target, BlockRenderLayer layer, Trans3 t) {
        ICustomRenderer rend = getCustomRendererForState(state);
        if (rend != null)
            rend.renderBlock(world, pos, state, target, layer, t);
    }

    public void renderItemStackUsingModelSpec(ItemStack stack, RenderTargetBase target, Trans3 t) {
        IBlockState state = BlockHelper.getBlockStateFromItemStack(stack);
        BlockArchitecture block = (BlockArchitecture) state.getBlock();
        ModelSpec spec = block.getModelSpec(state);
        ICustomRenderer rend = getCustomRendererForSpec(0, spec);
        rend.renderItemStack(stack, target, t);
    }

    public IRenderableModel getModel(String name) {
        return client.base.getModel(name);
    }

    public ResourceLocation textureResourceLocation(int type, String name) {
        // TextureMap adds "textures/"
        return client.base.resourceLocation(texturePrefixes[type] + name);
    }

    public ITexture getTexture(int type, String name) {
        // Cache is keyed by resource locaton without "textures/"
        ResourceLocation loc = textureResourceLocation(type, name);
        return textureCache.get(loc);
    }

    public TextureAtlasSprite getIcon(int type, String name) {
        return ((TextureBase.Sprite) getTexture(type, name)).icon;
    }

    @SubscribeEvent
    public void onTextureStitchEventPre(TextureStitchEvent.Pre e) {
        textureCache.clear();
        for (Block block : client.base.registeredBlocks) {
            registerSprites(0, e.getMap(), block);
        }
        for (Item item : client.base.registeredItems)
            registerSprites(1, e.getMap(), item);
    }

    protected void registerSprites(int textureType, TextureMap reg, Object obj) {
        if (debugModelRegistration)
            System.out.printf("BaseModClient.registerSprites: for %s\n", obj);
        if (obj instanceof ITextureConsumer) {
            String names[] = ((ITextureConsumer) obj).getTextureNames();
            if (debugModelRegistration)
                System.out.printf("BaseModClient.registerSprites: texture names = %s\n", (Object) names);
            if (names != null) {
                customRenderingRequired = true;
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

    protected IBakedModel customRenderBlockToBakedModel(IBlockAccess world, BlockPos pos, IBlockState state,
                                                        ICustomRenderer rend) {
        RenderTargetBaked target = new RenderTargetBaked(pos);
        Trans3 t = Trans3.blockCenter;
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
        BlockModelShapes shapes = blockRendererDispatcher.getBlockModelShapes();
        TextureAtlasSprite particle = shapes.getTexture(getBlockParticleState(state, world, pos));
        rend.renderBlock(world, pos, state, target, layer, t);
        return target.getBakedModel(particle);
    }

    public IBlockState getBlockParticleState(IBlockState state, IBlockAccess world, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof BlockArchitecture)
            return ((BlockArchitecture) block).getParticleState(world, pos);
        else
            return block.getActualState(state, world, pos);
    }

    @SubscribeEvent
    public void onModelRegistryEvent(ModelRegistryEvent event) {
        registerDefaultModelLocations();
    }

    @SubscribeEvent
    public void onModelBakeEvent(ModelBakeEvent event) {
        if (debugModelRegistration)
            System.out.printf("BaseModClient.ModelBakeEvent\n");

        //getCustomBlockRenderDispatch().install(event);
        getItemBakedModel().install(event);
        for (CustomBakedModel model : bakedModels) {
            if (debugModelRegistration)
                System.out.printf("BaseModClient.onModelBakeEvent: Installing %s --> %s\n",
                        model.location, model);
            model.install(event);
        }
    }

    protected void enableCustomRendering() {
        Minecraft mc = Minecraft.getMinecraft();
        blockRendererDispatcher = mc.getBlockRendererDispatcher();
    }

    protected static class CustomBlockStateMapper extends DefaultStateMapper {
        @Override
        public ModelResourceLocation getModelResourceLocation(IBlockState state) {
            return super.getModelResourceLocation(state);
        }
    }

    public abstract class CustomBakedModel implements IBakedModel {

        public ModelResourceLocation location;

        public void install(ModelBakeEvent event) {
            if (debugModelRegistration)
                System.out.printf("BaseModClient: Installing %s at %s\n", this, location);
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
                RenderTargetBaked target = new RenderTargetBaked();
                rend.renderItemStack(stack, target, itemTrans);
                return target.getBakedModel();
            } else
                return emptyModel;
        }

    }

    protected class CustomItemBakedModel extends CustomBakedModel {

        protected ItemOverrideList itemOverrideList = new CustomItemRenderOverrideList();

        public CustomItemBakedModel() {
            location = client.modelResourceLocation("__custitem__", "");
        }

        @Override
        public ItemOverrideList getOverrides() {
            return itemOverrideList;
        }
    }

}
