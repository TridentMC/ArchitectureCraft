//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.10 - Generic Mod
//
//------------------------------------------------------------------------------------------------

package com.elytradev.architecture.legacy.base;

import com.elytradev.architecture.common.block.BlockArchitecture;
import com.elytradev.architecture.client.render.model.IRenderableModel;
import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.Packet;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableManager;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

public class BaseMod<CLIENT extends BaseModClient<? extends BaseMod>>
        extends BaseSubsystem implements IGuiHandler {

    static Field lootGsonField = BaseReflectionUtils.getFieldDef(LootTableManager.class, "GSON_INSTANCE", "field_186526_b");
    static Field lootPoolsField = BaseReflectionUtils.getFieldDef(LootTable.class, "pools", "field_186466_c");
    static Field lootNameField = BaseReflectionUtils.getFieldDef(LootPool.class, "name", "");
    public boolean debugLoot = true;
    public String modID;
    public BaseConfiguration config;
    public String modPackage;
    public String assetKey;
    public String resourceDir; // path to resources directory with leading and trailing slashes
    public URL resourceURL; // URL to the resources directory
    public CLIENT client;
    public IGuiHandler proxy;
    public boolean serverSide, clientSide;
    public CreativeTabs creativeTab;
    public File cfgFile;
    public List<Block> registeredBlocks = new ArrayList<Block>();
    public List<Item> registeredItems = new ArrayList<Item>();
    public List<BaseSubsystem> subsystems = new ArrayList<BaseSubsystem>();
    public boolean debugGui = false;
    public boolean debugBlockRegistration = false;
    public boolean debugCreativeTabs = false;
    protected Map<ResourceLocation, IRenderableModel> modelCache = new HashMap<ResourceLocation, IRenderableModel>();
    Map<Integer, Class<? extends Container>> containerClasses =
            new HashMap<Integer, Class<? extends Container>>();
    int recipeCounter = 0;
    private List<SoundEvent> preRegisteredSounds = new ArrayList<>();
    private List<IRecipe> preRegisteredRecipes = new ArrayList<>();
    private List<Block> preRegisteredBlocks = new ArrayList<>();
    private List<Item> preRegisteredItems = new ArrayList<>();

    public BaseMod() {
        Class modClass = getClass();
        modPackage = modClass.getPackage().getName();
        //assetKey = modPackage.replace(".", "_");
        modID = getModID(modClass);
        assetKey = modID.toLowerCase();
        String resourceRelDir = "assets/" + assetKey + "/";
        resourceDir = "/" + resourceRelDir;
        resourceURL = getClass().getClassLoader().getResource(resourceRelDir);
        subsystems.add(this);
        creativeTab = CreativeTabs.MISC;
    }

    static String getModID(Class cls) {
        Annotation ann = cls.getAnnotation(Mod.class);
        if (ann instanceof Mod)
            return ((Mod) ann).modid();
        else {
            System.out.printf("BaseMod: Mod annotation not found\n");
            return "<unknown>";
        }
    }

    public static boolean isModLoaded(String modid) {
        return Loader.isModLoaded(modid);
    }

    public static boolean classAvailable(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean blockMatchesOre(Block block, String name) {
        return stackMatchesOre(new ItemStack(block), name);
    }

    public static boolean itemMatchesOre(Item item, String name) {
        return stackMatchesOre(new ItemStack(item), name);
    }

    public static boolean stackMatchesOre(ItemStack stack, String name) {
//      int id = OreDictionary.getOreID(stack);
//      return id == OreDictionary.getOreID(name);
        int id2 = OreDictionary.getOreID(name);
        for (int id1 : OreDictionary.getOreIDs(stack))
            if (id1 == id2)
                return true;
        return false;
    }

    public static void sendTileEntityUpdate(TileEntity te) {
        Packet packet = te.getUpdatePacket();
        if (packet != null) {
            BlockPos pos = te.getPos();
            int x = pos.getX() >> 4;
            int z = pos.getZ() >> 4;
            //System.out.printf("BaseMod.sendTileEntityUpdate: for chunk coords (%s, %s)\n", x, z);
            WorldServer world = (WorldServer) te.getWorld();
            PlayerList cm = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
            PlayerChunkMap pm = world.getPlayerChunkMap();
            for (EntityPlayerMP player : cm.getPlayers())
                if (pm.isPlayerWatchingChunk(player, x, z)) {
                    //System.out.printf("BaseMod.sendTileEntityUpdate: to %s\n", player);
                    player.connection.sendPacket(packet);
                }
        }
    }

    public static void reportExceptionCause(Exception e) {
        Throwable cause = e.getCause();
        System.out.printf("BaseMod.createGuiElement: %s: %s\n", e, cause);
        if (cause != null)
            cause.printStackTrace();
        else
            e.printStackTrace();
    }

    @SubscribeEvent
    public void onSoundRegistration(RegistryEvent.Register<SoundEvent> event) {
        for (SoundEvent preRegisteredSound : preRegisteredSounds) {
            preRegisteredSound.setRegistryName(modID, preRegisteredSound.getSoundName().getResourcePath());
            event.getRegistry().register(preRegisteredSound);
        }
    }

    @SubscribeEvent
    public void onRecipeRegistration(RegistryEvent.Register<IRecipe> event) {
        for (IRecipe preRegisteredRecipe : preRegisteredRecipes) {
            int suffix = 0;
            while (event.getRegistry().containsKey(new ResourceLocation(modID,
                    preRegisteredRecipe.getRecipeOutput().getItem().getRegistryName().getResourcePath()
                            + (suffix == 0 ? "" : suffix)))) {
                suffix++;
            }
            ResourceLocation registryName = new ResourceLocation(modID,
                    preRegisteredRecipe.getRecipeOutput().getItem().getRegistryName().getResourcePath()
                            + (suffix == 0 ? "" : suffix));
            preRegisteredRecipe.setRegistryName(registryName);
            System.out.println("Registered recipe " + preRegisteredRecipe);
            event.getRegistry().register(preRegisteredRecipe);
        }
    }

    @SubscribeEvent
    public void onBlockRegistration(RegistryEvent.Register<Block> event) {
        for (Block preRegisteredBlock : preRegisteredBlocks) {
            preRegisteredBlock.setRegistryName(new ResourceLocation(preRegisteredBlock.getUnlocalizedName().substring(5)));
            event.getRegistry().register(preRegisteredBlock);
        }
    }

    @SubscribeEvent
    public void onItemRegistration(RegistryEvent.Register<Item> event) {
        for (Item preRegisteredItem : preRegisteredItems) {
            preRegisteredItem.setRegistryName(new ResourceLocation(preRegisteredItem.getUnlocalizedName().substring(5)));
            event.getRegistry().register(preRegisteredItem);
        }

        for (BaseSubsystem sub : subsystems) {
            sub.registerRecipes();
        }
    }

    public void setModOf(Object obj) {
        if (obj instanceof ISetMod)
            ((ISetMod) obj).setMod(this);
    }

    public String resourcePath(String fileName) {
        return resourceDir + fileName;
    }

    @Override
    public void preInit(FMLPreInitializationEvent e) {
        MinecraftForge.EVENT_BUS.register(this);

        serverSide = e.getSide().isServer();
        clientSide = e.getSide().isClient();
        if (clientSide) {
            client = initClient();
            proxy = client;
        }
        cfgFile = e.getSuggestedConfigurationFile();
        loadConfig();
        configure();
        for (BaseSubsystem sub : subsystems) {
            if (sub != this)
                sub.preInit(e);
            sub.configure(config);
            sub.registerBlocks();
            sub.registerTileEntities();
            sub.registerItems();
            sub.registerOres();
            sub.registerWorldGenerators();
            sub.registerContainers();
            sub.registerEntities();
            sub.registerVillagers();
            sub.registerSounds();
        }
        if (client != null)
            client.preInit(e);
    }

    @Override
    public void init(FMLInitializationEvent e) {
        FMLCommonHandler.instance().bus().register(this);
        if (client != null)
            client.init(e);
        for (BaseSubsystem sub : subsystems)
            if (sub != this)
                sub.init(e);
    }

    //----- BaseSubsystem -----

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        for (BaseSubsystem sub : subsystems) {
            if (sub != this)
                sub.postInit(e);
            sub.registerOther();
        }
        if (client != null)
            client.postInit(e);
        if (proxy == null)
            proxy = this;
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
        saveConfig();
    }

    public void loadConfig() {
        config = new BaseConfiguration(cfgFile);
    }

    public void saveConfig() {
        if (config.extended)
            config.save();
    }

    String qualifiedName(String name) {
        return modPackage + "." + name;
    }

    @Override
    protected void registerModelLocations() {
        if (client != null)
            client.registerModelLocations();
    }

    @Override
    protected void registerScreens() {
        if (client != null)
            client.registerScreens();
    }

    @Override
    protected void registerBlockRenderers() {
        if (client != null)
            client.registerBlockRenderers();
    }

    //-------------------- Configuration ---------------------------------------------------------

    @Override
    protected void registerItemRenderers() {
        if (client != null)
            client.registerItemRenderers();
    }

    //----------------- Client Proxy -------------------------------------------------------------

    @Override
    protected void registerEntityRenderers() {
        if (client != null)
            client.registerEntityRenderers();
    }

    //--------------- Third-party mod integration ------------------------------------------------

    @Override
    protected void registerTileEntityRenderers() {
        if (client != null)
            client.registerTileEntityRenderers();
    }

    @Override
    protected void registerOtherClient() {
        if (client != null)
            client.registerOther();
    }

    public void configure() {
    }

    public CLIENT initClient() {
        return (CLIENT) (new BaseModClient(this));
    }

    public BaseSubsystem integrateWithMod(String modId, String subsystemClassName) {
        if (isModLoaded(modId))
            return loadSubsystem(subsystemClassName);
        else
            return null;
    }

    //--------------- Item registration ----------------------------------------------------------

    public BaseSubsystem integrateWithClass(String className, String subsystemClassName) {
        if (classAvailable(className))
            return loadSubsystem(subsystemClassName);
        else
            return null;
    }

    public BaseSubsystem loadSubsystem(String className) {
        BaseSubsystem sub = newSubsystem(className);
        sub.mod = this;
        sub.client = client;
        subsystems.add(sub);
        return sub;
    }

    protected BaseSubsystem newSubsystem(String className) {
        try {
            return (BaseSubsystem) Class.forName(className).newInstance();
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }
    }

    //--------------- Block registration ----------------------------------------------------------

    public Item newItem(String name) {
        return newItem(name, Item.class);
    }

    public <ITEM extends Item> ITEM newItem(String name, Class<ITEM> cls) {
        ITEM item;
        try {
            Constructor<ITEM> ctor = cls.getConstructor();
            item = ctor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return addItem(item, name, true);
    }

    public <ITEM extends Item> ITEM addItem(ITEM item, String name, boolean addToList) {
        String qualName = assetKey + ":" + name;
        item.setUnlocalizedName(qualName);
        preRegisteredItems.add(item);
        if (debugBlockRegistration)
            System.out.printf("BaseMod.addItem: Registered %s as %s\n", item, name);
        if (creativeTab != null) {
            if (debugCreativeTabs)
                System.out.printf("BaseMod.addItem: Setting creativeTab of %s to %s\n", name, creativeTab);
            item.setCreativeTab(creativeTab);
        }
        if (addToList)
            registeredItems.add(item);
        return item;
    }

    public Block newBlock(String name) {
        return newBlock(name, Block.class);
    }

    public <BLOCK extends Block> BLOCK newBlock(String name, Class<BLOCK> cls) {
        return newBlock(name, cls, ItemBlock.class);
    }


    //--------------- Ore registration ----------------------------------------------------------

    public <BLOCK extends Block> BLOCK newBlock(String name, Class<BLOCK> cls, Class<? extends Item> itemClass) {
        BLOCK block;
        try {
            Constructor<BLOCK> ctor = cls.getConstructor();
            block = ctor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return addBlock(block, name, itemClass);
    }

    public <BLOCK extends Block> BLOCK addBlock(BLOCK block, String name) {
        return addBlock(block, name, ItemBlock.class);
    }

    public <BLOCK extends Block> BLOCK addBlock(BLOCK block, String name, Class<? extends Item> itemClass) {
        String qualName = assetKey + ":" + name;
        block.setUnlocalizedName(qualName);
        preRegisteredBlocks.add(block);
        if (creativeTab != null) {
            block.setCreativeTab(creativeTab);
        }
        if (block instanceof BlockArchitecture)
            ((BlockArchitecture) block).mod = this;
        registeredBlocks.add(block);
        try {
            if (Arrays.stream(itemClass.getConstructors()).anyMatch((Predicate<Constructor<?>>)
                    input -> input != null && input.getParameterCount() == 1 && Block.class.isAssignableFrom(input.getParameterTypes()[0]))) {
                addItem(itemClass.getConstructor(Block.class).newInstance(block), name + ".item", false);
            } else {
                addItem(itemClass.getConstructor().newInstance(), name + ".item", false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return block;
    }

    public void addOre(String name, Block block) {
        OreDictionary.registerOre(name, new ItemStack(block));
    }

    public void addOre(String name, Item item) {
        OreDictionary.registerOre(name, item);
    }

    //--------------- Recipe construction ----------------------------------------------------------

    public void newRecipe(Item product, int qty, Object... params) {
        newRecipe(new ItemStack(product, qty), params);
    }

    public void newRecipe(Block product, int qty, Object... params) {
        newRecipe(new ItemStack(product, qty), params);
    }

    public void newRecipe(ItemStack product, Object... params) {
        preRegisteredRecipes.add(new ShapedOreRecipe(null, product, params));
    }

    public void newShapelessRecipe(Block product, int qty, Object... params) {
        newShapelessRecipe(new ItemStack(product, qty), params);
    }

    public void newShapelessRecipe(Item product, int qty, Object... params) {
        newShapelessRecipe(new ItemStack(product, qty), params);
    }

    public void newShapelessRecipe(ItemStack product, Object... params) {
        preRegisteredRecipes.add(new ShapelessOreRecipe(null, product, params));
    }

    public void newSmeltingRecipe(Item product, int qty, Item input) {
        newSmeltingRecipe(product, qty, input, 0);
    }

    public void newSmeltingRecipe(Item product, int qty, Item input, int xp) {
        GameRegistry.addSmelting(input, new ItemStack(product, qty), xp);
    }

    public void newSmeltingRecipe(Item product, int qty, Block input) {
        newSmeltingRecipe(product, qty, input, 0);
    }

    public void newSmeltingRecipe(Item product, int qty, Block input, int xp) {
        GameRegistry.addSmelting(input, new ItemStack(product, qty), xp);
    }

    public void addEntity(Class<? extends Entity> cls, String name, Enum id) {
        addEntity(cls, name, id.ordinal());
    }

    //--------------- Dungeon loot ----------------------------------------------------------

//     public void addRandomChestItem(ItemStack stack, int minQty, int maxQty, int weight, String... category) {
//         WeightedRandomChestContent item = new WeightedRandomChestContent(stack, minQty, maxQty, weight);
//         for (int i = 0; i < category.length; i++)
//             ChestGenHooks.addItem(category[i], item);
//    }

    //--------------- Entity registration ----------------------------------------------------------

    public void addEntity(Class<? extends Entity> cls, String name, int id) {
        addEntity(cls, name, id, 1, true);
    }

    public void addEntity(Class<? extends Entity> cls, String name, Enum id,
                          int updateFrequency, boolean sendVelocityUpdates) {
        addEntity(cls, name, id.ordinal(), updateFrequency, sendVelocityUpdates);
    }

    public void addEntity(Class<? extends Entity> cls, String name, int id,
                          int updateFrequency, boolean sendVelocityUpdates) {
        System.out.printf("%s: BaseMod.addEntity: %s, \"%s\", %s\n",
                getClass().getSimpleName(), cls.getSimpleName(), name, id);
        EntityRegistry.registerModEntity(new ResourceLocation(modID, getClass().getSimpleName()), cls, name, id, /*base*/this, 256, updateFrequency, sendVelocityUpdates);
    }

    public SoundEvent newSound(String name) {
        try {
            ResourceLocation loc = resourceLocation(name);
            SoundEvent result = new SoundEvent(loc);
            result.setRegistryName(loc);
            preRegisteredSounds.add(result);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //--------------- Sound Registration -------------------------------------------------

    public ResourceLocation resourceLocation(String path) {
        return new ResourceLocation(assetKey, path);
    }

    //--------------- Resources ----------------------------------------------------------

    public String soundName(String name) {
        return assetKey + ":" + name;
    }

    public ResourceLocation textureLocation(String path) {
        return resourceLocation("textures/" + path);
    }

    public ResourceLocation modelLocation(String path) {
        return resourceLocation("models/" + path);
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

    @Override
    protected void registerContainers() {
        //  Make calls to addContainer() here.
        //
        //  Container classes registered using these methods must implement either:
        //
        //  (1) A static method create(EntityPlayer player, World world, int x, int y, int z [,int param])
        //  (2) A constructor MyContainer(EntityPlayer player, World world, int x, int y, int z [, int param])
    }

//  @SideOnly(Side.CLIENT)
//  public IIcon getIcon(IIconRegister reg, String name) {
//      return reg.registerIcon(assetKey + ":" + name);
//  }

//  public Set<String> listResources(String subdir) {
//      try {
//          Set<String>result = new HashSet<String>();
//          if (resourceURL != null) {
//              String protocol = resourceURL.getProtocol();
//              if (protocol.equals("jar")) {
//                  String resPath = resourceURL.getPath();
//                  int pling = resPath.lastIndexOf("!");
//                  URL jarURL = new URL(resPath.substring(0, pling));
//                  String resDirInJar = resPath.substring(pling + 2);
//                  String prefix = resDirInJar + subdir + "/";
//                  //System.out.printf("BaseMod.listResources: looking for names starting with %s\n", prefix);
//                  JarFile jar = new JarFile(new File(jarURL.toURI()));
//                  Enumeration<JarEntry> entries = jar.entries();
//                  while (entries.hasMoreElements()) {
//                      String name = entries.nextElement().getName();
//                      if (name.startsWith(prefix) && !name.endsWith("/") && !name.contains("/.")) {
//                          //System.out.printf("BaseMod.listResources: name = %s\n", name);
//                          result.add(name.substring(prefix.length()));
//                      }
//                  }
//              }
//              else
//                  throw new RuntimeException("Resource URL protocol " + protocol + " not supported");
//          }
//          return result;
//      }
//      catch (Exception e) {
//          throw new RuntimeException(e);
//      }
//  }

    //------------------------- Network --------------------------------------------------

    public void addContainer(Enum id, Class<? extends Container> cls) {
        addContainer(id.ordinal(), cls);
    }

    //--------------- GUIs - Registration ------------------------------------------------

    public void addContainer(int id, Class<? extends Container> cls) {
        containerClasses.put(id, cls);
    }

    public void openGui(EntityPlayer player, Enum id, TileEntity te) {
        openGui(player, id, te, 0);
    }

    public void openGui(EntityPlayer player, Enum id, TileEntity te, int param) {
        openGui(player, id.ordinal(), te, param);
    }

    //--------------- GUIs  - Invoking -------------------------------------------------

    public void openGui(EntityPlayer player, int id, TileEntity te) {
        openGui(player, id, te, 0);
    }

    public void openGui(EntityPlayer player, int id, TileEntity te, int param) {
        openGui(player, id, te.getWorld(), te.getPos(), param);
    }

    public void openGui(EntityPlayer player, Enum id, World world, BlockPos pos) {
        openGui(player, id, world, pos, 0);
    }

    public void openGui(EntityPlayer player, Enum id, World world, BlockPos pos, int param) {
        openGui(player, id.ordinal(), world, pos, param);
    }

    public void openGui(EntityPlayer player, int id, World world, BlockPos pos, int param) {
        openGui(player, id | (param << 16), world, pos);
    }

    public void openGui(EntityPlayer player, int id, World world, BlockPos pos) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        if (debugGui)
            System.out.printf("BaseMod.openGui: for %s with id 0x%x in %s at (%s, %s, %s)\n",
                    this, id, world, x, y, z);
        player.openGui(this, id, world, x, y, z);
    }

    /**
     * Returns a Container to be displayed to the user.
     * On the client side, this needs to return a instance of GuiScreen
     * On the server side, this needs to return a instance of Container
     *
     * @param player The player viewing the Gui
     * @param world  The current world
     * @return A GuiScreen/Container to be displayed to the user, null if none.
     */

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return getServerGuiElement(id, player, world, new BlockPos(x, y, z));
    }

    public Object getServerGuiElement(int id, EntityPlayer player, World world, BlockPos pos) {
        if (debugGui)
            System.out.printf("BaseMod.getServerGuiElement: for id 0x%x\n", id);
        int param = id >> 16;
        id = id & 0xffff;
        Class cls = containerClasses.get(id);
        Object result;
        if (cls != null)
            result = createGuiElement(cls, player, world, pos, param);
        else
            result = getGuiContainer(id, player, world, pos, param);
        if (debugGui)
            System.out.printf("BaseMod.getServerGuiElement: Returning %s\n", result);
        setModOf(result);
        return result;
    }

    //--------------- GUIs  - Internal -------------------------------------------------

    Container getGuiContainer(int id, EntityPlayer player, World world, BlockPos pos, int param) {
        //  Called when container id not found in registry
        if (debugGui)
            System.out.printf("%s: BaseMod.getGuiContainer: No Container class found for gui id %d\n",
                    this, id);
        return null;
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    Object createGuiElement(Class cls, EntityPlayer player, World world, BlockPos pos, int param) {
        try {
            if (debugGui)
                System.out.printf("BaseMod.createGuiElement: Looking for create method on %s for %s in %s\n",
                        cls, player, world);
            Method m = getMethod(cls, "create",
                    EntityPlayer.class, World.class, BlockPos.class, int.class);
            if (m != null)
                return m.invoke(null, player, world, pos, param);
            m = getMethod(cls, "create", EntityPlayer.class, World.class, BlockPos.class);
            if (m != null)
                return m.invoke(null, player, world, pos);
            if (debugGui)
                System.out.printf("BaseMod.createGuiElement: Looking for constructor on %s\n", cls);
            Constructor c = getConstructor(cls,
                    EntityPlayer.class, World.class, BlockPos.class, int.class);
            if (c != null)
                return c.newInstance(player, world, pos, param);
            c = getConstructor(cls, EntityPlayer.class, World.class, BlockPos.class);
            if (c != null)
                return c.newInstance(player, world, pos);
            throw new RuntimeException(String.format("%s: No suitable gui element constructor found for %s\n",
                    modID, cls));
        } catch (Exception e) {
            reportExceptionCause(e);
            return null;
        }
    }

    Method getMethod(Class cls, String name, Class... argTypes) {
        try {
            return cls.getMethod(name, argTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    Constructor getConstructor(Class cls, Class... argTypes) {
        try {
            return cls.getConstructor(argTypes);
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    @SubscribeEvent
    public void onLootTableLoad(LootTableLoadEvent event) {
        //if (debugLoot)
        //    System.out.printf("BaseMod.onLootTableLoad\n");
        ResourceLocation locn = event.getName();
        if (locn.getResourceDomain().equals("minecraft")) {
            String path = String.format("/assets/%s/loot_tables/%s.json", assetKey, locn.getResourcePath());
            //if (debugLoot)
            //    System.out.printf("BaseMod.onLootTableLoad: Looking for %s\n", path);
            URL url = getClass().getResource(path);
            if (url != null) {
                if (debugLoot)
                    System.out.printf("BaseMod.onLootTableLoad: Loading %s\n", path);
                String data;
                try {
                    data = Resources.toString(url, Charsets.UTF_8);
                } catch (Exception e) {
                    throw new RuntimeException("Error loading " + path + ": " + e);
                }
                //if (debugLoot)
                //    System.out.printf("BaseMod.onLootTableLoad: data = %s\n", data);
                Gson gson = (Gson) BaseReflectionUtils.getField(null, lootGsonField);
                LootTable table = event.getTable();
                LootTable newTable = ForgeHooks.loadLootTable(gson, locn, data, true);
                List<LootPool> newPools = (List<LootPool>) BaseReflectionUtils.getField(newTable, lootPoolsField);
                int i = 0;
                for (LootPool pool : newPools) {
                    BaseReflectionUtils.setField(pool, lootNameField, modID + (i++));
                    if (debugLoot)
                        System.out.printf("BaseMod.onLootTableLoad: Adding pool %s\n", pool.getName());
                    table.addPool(pool);
                }
            }
        }
    }

    interface ISetMod {
        void setMod(BaseMod mod);
    }

}
