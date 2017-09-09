//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.10 - Rendering Manager
//
//------------------------------------------------------------------------------------------------

package com.elytradev.architecture;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockModelShapes;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.BufferBuilder;
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
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.opengl.GL11.GL_SMOOTH;

public class BaseRenderingManager<MOD extends BaseMod<? extends BaseModClient>> implements BaseModClient.IRenderingManager {

    protected static Trans3 itemTrans = Trans3.blockCenterSideTurn(0, 2);
    protected static String[] texturePrefixes = {"blocks/", "textures/"};
    public boolean debugRenderingManager = false;
    public boolean debugModelRegistration = false;
    protected BaseModClient<MOD> client;
    protected Map<Block, BaseModClient.ICustomRenderer> blockRenderers = new HashMap<Block, BaseModClient.ICustomRenderer>();
    protected Map<Item, BaseModClient.ICustomRenderer> itemRenderers = new HashMap<Item, BaseModClient.ICustomRenderer>();
    protected Map<IBlockState, BaseModClient.ICustomRenderer> stateRendererCache = new HashMap<IBlockState, BaseModClient.ICustomRenderer>();
    protected Map<ResourceLocation, BaseModClient.ITexture> textureCache = new HashMap<ResourceLocation, BaseModClient.ITexture>();
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
        FMLCommonHandler.instance().bus().register(this);
    }

    ;

    public static void renderBlockUsingModelSpec(BaseModClient client,
                                                 IBlockAccess world, BlockPos pos, IBlockState state,
                                                 BaseModClient.IRenderTarget target, BlockRenderLayer layer, Trans3 t) {
        ((BaseRenderingManager) client.renderingManager).renderBlockUsingModelSpec(
                world, pos, state, target, layer, t);
    }

    //------------------------------------------------------------------------------------------------

    // Call this from renderItemStack of an ICustomRenderer to fall back to model spec
    public static void renderItemStackUsingModelSpec(BaseModClient client,
                                                     ItemStack stack, BaseModClient.IRenderTarget target, Trans3 t) {
        ((BaseRenderingManager) client.renderingManager).renderItemStackUsingModelSpec(
                stack, target, t);
    }

    public static boolean renderAlternateBlock(BaseModClient client,
                                               IBlockAccess world, BlockPos pos, IBlockState state, BaseModClient.IRenderTarget target) {
        return ((BaseRenderingManager) client.renderingManager).renderAlternateBlock(
                world, pos, state, target);
    }

    //------------------------------------------------------------------------------------------------

    public void preInit() {
        if (debugRenderingManager)
            System.out.printf("BaseRenderingManager.preInit\n");
        registerDefaultRenderers();
//         registerDummyStateMappers();
    }

    //------------------------------------------------------------------------------------------------

    public void postInit() {
        if (debugRenderingManager)
            System.out.printf("BaseRenderingManager.postInit: customRenderingRequired = %s\n", customRenderingRequired);
        if (customRenderingRequired)
            enableCustomRendering();
    }

    protected void registerDefaultRenderers() {
        for (Block block : client.base.registeredBlocks) {
            if (block instanceof BaseMod.IBlock) {
                if (!blockRenderers.containsKey(block)) {
                    String name = ((BaseMod.IBlock) block).getQualifiedRendererClassName();
                    if (name != null) {
                        try {
                            Class cls = Class.forName(name);
                            addBlockRenderer(block, (BaseModClient.ICustomRenderer) cls.newInstance());
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }

    public void addBlockRenderer(Block block, BaseModClient.ICustomRenderer renderer) {
        blockRenderers.put(block, renderer);
        customRenderingRequired = true;
        Item item = Item.getItemFromBlock(block);
        if (item != null)
            addItemRenderer(item, renderer);
    }

    public void addItemRenderer(Item item, BaseModClient.ICustomRenderer renderer) {
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
        return obj instanceof BaseMod.ITextureConsumer && ((BaseMod.ITextureConsumer) obj).getTextureNames() != null;
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
        for (int i = 0; i < numVariants; i++)
            ModelLoader.setCustomModelResourceLocation(item, i, location);
    }

    private int getNumBlockSubtypes(Block block) {
        if (block instanceof BaseMod.IBlock)
            return ((BaseMod.IBlock) block).getNumSubtypes();
        else
            return 1;
    }

    private int getNumItemSubtypes(Item item) {
        if (item instanceof BaseMod.IItem)
            return ((BaseMod.IItem) item).getNumSubtypes();
        else if (item instanceof ItemBlock)
            return getNumBlockSubtypes(Block.getBlockFromItem(item));
        else
            return 1;
    }

    //------------------------------------------------------------------------------------------------

    protected BaseModClient.ICustomRenderer getCustomRenderer(IBlockAccess world, BlockPos pos, IBlockState state) {
        //System.out.printf("BaseModClient.getCustomRenderer: %s\n", state);
        Block block = state.getBlock();
        BaseModClient.ICustomRenderer rend = blockRenderers.get(block);
        if (rend == null && block instanceof BaseMod.IBlock /*&& block.getRenderType() == -1*/) {
            IBlockState astate = block.getActualState(state, world, pos);
            rend = getCustomRendererForState(astate);
        }
        return rend;
    }

    protected BaseModClient.ICustomRenderer getCustomRendererForSpec(int textureType, BaseMod.ModelSpec spec) {
//         System.out.printf("BaseModClient.getCustomRendererForSpec: %s\n", spec.modelName);
//         for (int i = 0; i < spec.textureNames.length; i++)
//           System.out.printf(" %s", spec.textureNames[i]);
//         System.out.printf("\n");
        BaseModClient.IModel model = getModel(spec.modelName);
        BaseModClient.ITexture[] textures = new BaseModClient.ITexture[spec.textureNames.length];
        for (int i = 0; i < textures.length; i++)
            textures[i] = getTexture(textureType, spec.textureNames[i]);
//         System.out.printf("BaseModClient.getCustomRendererForSpec: model = %s\n", model);
//         for (int i = 0; i < spec.textureNames.length; i++)
//           System.out.printf("BaseModClient.getCustomRendererForSpec: texture[%s] = %s\n",
//               i, textures[i]);
        return new BaseModelRenderer(model, spec.origin, textures);
    }

    protected BaseModClient.ICustomRenderer getCustomRendererForState(IBlockState astate) {
        BaseModClient.ICustomRenderer rend = stateRendererCache.get(astate);
        if (rend == null) {
//             System.out.printf("BaseModClient.getCustomRendererForState: %s\n", astate);
            Block block = astate.getBlock();
            if (block instanceof BaseMod.IBlock) {
                BaseMod.ModelSpec spec = ((BaseMod.IBlock) block).getModelSpec(astate);
                if (spec != null) {
                    rend = getCustomRendererForSpec(0, spec);
                    stateRendererCache.put(astate, rend);
                }
            }
        }
        return rend;
    }

    public void renderBlockUsingModelSpec(IBlockAccess world, BlockPos pos, IBlockState state,
                                          BaseModClient.IRenderTarget target, BlockRenderLayer layer, Trans3 t) {
        BaseModClient.ICustomRenderer rend = getCustomRendererForState(state);
        if (rend != null)
            rend.renderBlock(world, pos, state, target, layer, t);
    }

    public void renderItemStackUsingModelSpec(ItemStack stack, BaseModClient.IRenderTarget target, Trans3 t) {
        IBlockState state = BaseBlockUtils.getBlockStateFromItemStack(stack);
        BaseMod.IBlock block = (BaseMod.IBlock) state.getBlock();
        BaseMod.ModelSpec spec = block.getModelSpec(state);
        BaseModClient.ICustomRenderer rend = getCustomRendererForSpec(0, spec);
        rend.renderItemStack(stack, target, t);
    }

    public BaseModClient.IModel getModel(String name) {
        return client.base.getModel(name);
    }

    public ResourceLocation textureResourceLocation(int type, String name) {
        // TextureMap adds "textures/"
        return client.base.resourceLocation(texturePrefixes[type] + name);
    }

    public BaseModClient.ITexture getTexture(int type, String name) {
        // Cache is keyed by resource locaton without "textures/"
        ResourceLocation loc = textureResourceLocation(type, name);
        return textureCache.get(loc);
    }

    public TextureAtlasSprite getIcon(int type, String name) {
        return ((BaseTexture.Sprite) getTexture(type, name)).icon;
    }

    @SubscribeEvent
    public void onTextureStitchEventPre(TextureStitchEvent.Pre e) {
        //System.out.printf("BaseModClient.onTextureStitchEventPre: %s\n", e.map);
        textureCache.clear();
        for (Block block : client.base.registeredBlocks) {
            //System.out.printf("BaseModClient.onTextureStitchEvent: Block %s\n", block.getUnlocalizedName());
            registerSprites(0, e.getMap(), block);
        }
        for (Item item : client.base.registeredItems)
            registerSprites(1, e.getMap(), item);
    }

    protected void registerSprites(int textureType, TextureMap reg, Object obj) {
        if (debugModelRegistration)
            System.out.printf("BaseModClient.registerSprites: for %s\n", obj);
        if (obj instanceof BaseMod.ITextureConsumer) {
            String names[] = ((BaseMod.ITextureConsumer) obj).getTextureNames();
            if (debugModelRegistration)
                System.out.printf("BaseModClient.registerSprites: texture names = %s\n", (Object) names);
            if (names != null) {
                customRenderingRequired = true;
                for (String name : names) {
                    ResourceLocation loc = textureResourceLocation(textureType, name);
                    if (textureCache.get(loc) == null) {
                        TextureAtlasSprite icon = reg.registerSprite(loc);
                        BaseModClient.ITexture texture = BaseTexture.fromSprite(icon);
                        textureCache.put(loc, texture);
                    }
                }
            }
        }
    }

    protected IBakedModel customRenderBlockToBakedModel(IBlockAccess world, BlockPos pos, IBlockState state,
                                                        BaseModClient.ICustomRenderer rend) {
        BaseBakedRenderTarget target = new BaseBakedRenderTarget(pos);
        Trans3 t = Trans3.blockCenter;
        BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
//        BlockModelShapes shapes = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes();
        BlockModelShapes shapes = blockRendererDispatcher.getBlockModelShapes();
        TextureAtlasSprite particle = shapes.getTexture(getBlockParticleState(state, world, pos));
        rend.renderBlock(world, pos, state, target, layer, t);
        return target.getBakedModel(particle);
    }

    public IBlockState getBlockParticleState(IBlockState state, IBlockAccess world, BlockPos pos) {
        Block block = state.getBlock();
        if (block instanceof BaseMod.IBlock)
            return ((BaseMod.IBlock) block).getParticleState(world, pos);
        else
            return block.getActualState(state, world, pos);
    }

    public boolean renderAlternateBlock(IBlockAccess world, BlockPos pos, IBlockState state, BaseModClient.IRenderTarget target) {
        Block block = state.getBlock();
        if (!block.hasTileEntity(state)) {
            try {
                BufferBuilder tess = ((BaseWorldRenderTarget) target).getWorldRenderer();
                return blockRendererDispatcher.renderBlock(state, pos, world, tess);
            } catch (Exception e) {
                // Some blocks are averse to being abused this way. Try to avoid crashing in that case.
                return false;
            }
        }
        return false;
    }

    //------------------------------------------------------------------------------------------------

    @SubscribeEvent
    public void onModelBakeEvent(ModelBakeEvent event) {
        if (debugModelRegistration)
            System.out.printf("BaseModClient.ModelBakeEvent\n");

        //Moved from preInit due to block registration not being complete.
        registerDefaultModelLocations();

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

        // DefaultStateMapper.getModelResourceLocation is protected
        public ModelResourceLocation getModelResourceLocation(IBlockState state) {
            return super.getModelResourceLocation(state);
        }

    }

    protected abstract class CustomBakedModel implements IBakedModel {

        public ModelResourceLocation location;

        public void install(ModelBakeEvent event) {
            if (debugModelRegistration)
                System.out.printf("BaseModClient: Installing %s at %s\n", this, location);
            event.getModelRegistry().putObject(location, this);
        }

        // ----- IBakedModel -----

        public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
            return null;
        }

        public boolean isAmbientOcclusion() {
            return false;
        }

        public boolean isGui3d() {
            return false;
        }

        public boolean isBuiltInRenderer() {
            return false;
        }

        public TextureAtlasSprite getParticleTexture() {
            return null;
        }

        public ItemCameraTransforms getItemCameraTransforms() {
            return null;
        }

    }

    //------------------------------------------------------------------------------------------------

    protected class BlockParticleModel extends CustomBakedModel {

        protected IBlockState state;

        public BlockParticleModel(IBlockState state, ModelResourceLocation location) {
            this.state = state;
            this.location = location;
        }

        // ----- IBakedModel -----

        public ItemOverrideList getOverrides() {
            return null;
        }

        public TextureAtlasSprite getParticleTexture() {
            Block block = state.getBlock();
            if (block instanceof BaseMod.IBlock) {
                String[] textures = ((BaseMod.IBlock) block).getTextureNames();
                if (textures != null && textures.length > 0)
                    return getIcon(0, textures[0]);
            }
            return null;
        }

    }

    protected class CustomItemRenderOverrideList extends ItemOverrideList {

        public CustomItemRenderOverrideList() {
            super(ImmutableList.<ItemOverride>of());
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
            //System.out.printf("BaseModClient.CustomItemBakedModel.handleItemState: %s\n", stack);
            Item item = stack.getItem();
            BaseModClient.ICustomRenderer rend = itemRenderers.get(item);
            if (rend == null && item instanceof BaseMod.IItem) {
                BaseMod.ModelSpec spec = ((BaseMod.IItem) item).getModelSpec(stack);
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
                return null;
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
