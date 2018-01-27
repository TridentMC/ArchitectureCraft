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

package com.elytradev.architecture.common.block;

import com.elytradev.architecture.client.render.model.IArchitectureModel;
import com.elytradev.architecture.common.ArchitectureMod;
import com.elytradev.architecture.common.helpers.Trans3;
import com.elytradev.architecture.common.helpers.Vector3;
import com.elytradev.architecture.common.render.ITextureConsumer;
import com.elytradev.architecture.common.render.ModelSpec;
import com.elytradev.architecture.common.tile.TileArchitecture;
import com.elytradev.architecture.common.utils.MiscUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.particle.ParticleDigging;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("ALL")
public class BlockArchitecture<TE extends TileArchitecture>
        extends BlockContainer implements ITextureConsumer {

    public static final IUnlistedProperty<IBlockAccess> BLOCKACCESS_PROP = new IUnlistedProperty<IBlockAccess>() {
        @Override
        public String getName() {
            return "blockaccessprop";
        }

        @Override
        public boolean isValid(IBlockAccess value) {
            return true;
        }

        @Override
        public Class<IBlockAccess> getType() {
            return IBlockAccess.class;
        }

        @Override
        public String valueToString(IBlockAccess value) {
            return value.toString();
        }
    };

    public static final IUnlistedProperty<BlockPos> POS_PROP = new IUnlistedProperty<BlockPos>() {
        @Override
        public String getName() {
            return "unlistedpos";
        }

        @Override
        public boolean isValid(BlockPos value) {
            return true;
        }

        @Override
        public Class<BlockPos> getType() {
            return BlockPos.class;
        }

        @Override
        public String valueToString(BlockPos value) {
            return value.toString();
        }
    };
    public static boolean debugState = false;
    // --------------------------- Orientation -------------------------------
    public static IOrientationHandler orient1Way = new Orient1Way();
    protected MapColor mapColor;
    protected IProperty[] properties;
    // --------------------------- Members -------------------------------
    protected Object[][] propertyValues;
    protected int numProperties; // Do not explicitly initialise
    protected EnumBlockRenderType renderID = EnumBlockRenderType.MODEL;
    protected Class<? extends TileEntity> tileEntityClass = null;
    protected IOrientationHandler orientationHandler = orient1Way;
    protected String[] textureNames;
    protected ModelSpec modelSpec;
    protected AxisAlignedBB boxHit;
    protected ThreadLocal<TileEntity> harvestingTileEntity = new ThreadLocal();

    public BlockArchitecture(Material material) {
        this(material, null, null, null);
    }

    // --------------------------- Constructors -------------------------------

    public BlockArchitecture(Material material, IOrientationHandler orient) {
        this(material, orient, null, null);
    }

    public BlockArchitecture(Material material, Class<TE> teClass) {
        this(material, null, teClass, null);
    }

    public BlockArchitecture(Material material, IOrientationHandler orient, Class<TE> teClass) {
        this(material, orient, teClass, null);
    }

    public BlockArchitecture(Material material, Class<TE> teClass, String teID) {
        this(material, null, teClass, teID);
    }

    public BlockArchitecture(Material material, IOrientationHandler orient, Class<TE> teClass, String teID) {
        super(material);
        if (orient == null)
            orient = orient1Way;
        this.orientationHandler = orient;
        this.tileEntityClass = teClass;
    }

    public IOrientationHandler getOrientationHandler() {
        return orient1Way;
    }

    // --------------------------- States -------------------------------

    protected void defineProperties() {
        properties = new IProperty[4];
        propertyValues = new Object[4][];
        getOrientationHandler().defineProperties(this);
    }

    public void addProperty(IProperty property) {
        if (debugState)
            ArchitectureMod.LOG.info("BaseBlock.addProperty: %s to %s\n", property, getClass().getName());
        if (numProperties < 4) {
            int i = numProperties++;
            properties[i] = property;
            Object[] values = MiscUtils.arrayOf(property.getAllowedValues());
            propertyValues[i] = values;
        } else
            throw new IllegalStateException("Block " + getClass().getName() +
                    " has too many properties");
        if (debugState)
            ArchitectureMod.LOG.info("BaseBlock.addProperty: %s now has %s properties\n",
                    getClass().getName(), numProperties);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        if (debugState)
            ArchitectureMod.LOG.info("BaseBlock.createBlockState: Defining properties\n");
        defineProperties();
        if (debugState)
            dumpProperties();
        checkProperties();
        IProperty[] props = Arrays.copyOf(properties, numProperties);
        if (debugState)
            ArchitectureMod.LOG.info("BaseBlock.createBlockState: Creating BlockState with %s properties\n", props.length);
        return new ExtendedBlockState(this, props, new IUnlistedProperty[]{BLOCKACCESS_PROP, POS_PROP});
    }

    private void dumpProperties() {
        ArchitectureMod.LOG.info("BaseBlock: Properties of %s:\n", getClass().getName());
        for (int i = 0; i < numProperties; i++) {
            ArchitectureMod.LOG.info("%s: %s\n", i, properties[i]);
            Object[] values = propertyValues[i];
            for (int j = 0; j < values.length; j++)
                ArchitectureMod.LOG.info("   %s: %s\n", j, values[j]);
        }
    }

    protected void checkProperties() {
        int n = 1;
        for (int i = 0; i < numProperties; i++)
            n *= propertyValues[i].length;
        if (n > 16)
            throw new IllegalStateException(String.format(
                    "Block %s has %s combinations of property values (16 allowed)",
                    getClass().getName(), n));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        int meta = 0;
        for (int i = numProperties - 1; i >= 0; i--) {
            Object value = state.getValue(properties[i]);
            Object[] values = propertyValues[i];
            int k = values.length - 1;
            while (k > 0 && !values[k].equals(value))
                --k;
            if (debugState)
                ArchitectureMod.LOG.info("BaseBlock.getMetaFromState: property %s value %s --> %s of %s\n",
                        i, value, k, values.length);
            meta = meta * values.length + k;
        }
        if (debugState)
            ArchitectureMod.LOG.info("BaseBlock.getMetaFromState: %s --> %s\n", state, meta);
        return meta & 15; // To be on the safe side
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        IBlockState state = getDefaultState();
        int m = meta;
        for (int i = numProperties - 1; i >= 0; i--) {
            Object[] values = propertyValues[i];
            int n = values.length;
            int k = m % n;
            m /= n;
            state = state.withProperty(properties[i], (Comparable) values[k]);
        }
        if (debugState)
            ArchitectureMod.LOG.info("BaseBlock.getStateFromMeta: %s --> %s\n", meta, state);
        return state;
    }

    public int getNumSubtypes() {
        return 1;
    }

//  @Override
//  public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
//      return new BaseBlockState(state, world, pos);
//  }

    // -------------------------- Subtypes ------------------------------

    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack stack) {
        harvestingTileEntity.set(te);
        super.harvestBlock(world, player, pos, state, te, stack);
        harvestingTileEntity.set(null);
    }

    // -------------------------- Harvesting ----------------------------

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        TileEntity te = world.getTileEntity(pos);
        if (te == null)
            te = harvestingTileEntity.get();
        return getDropsFromTileEntity(world, pos, state, te, fortune);
    }

    protected List<ItemStack> getDropsFromTileEntity(IBlockAccess world, BlockPos pos, IBlockState state, TileEntity te, int fortune) {
        return super.getDrops(world, pos, state, fortune);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return renderID;
    }

    public String getQualifiedRendererClassName() {
        String name = getRendererClassName();
        if (name != null)
            name = getClass().getPackage().getName() + "." + name;
        return name;
    }

    // -------------------------- Rendering -----------------------------

    protected String getRendererClassName() {
        return null;
    }

    public void setModelAndTextures(String modelName, String... textureNames) {
        this.textureNames = textureNames;
        this.modelSpec = new ModelSpec(modelName, textureNames);
    }

    public void setModelAndTextures(String modelName, Vector3 origin, String... textureNames) {
        this.textureNames = textureNames;
        this.modelSpec = new ModelSpec(modelName, origin, textureNames);
    }

    public String[] getTextureNames() {
        return textureNames;
    }

    public ModelSpec getModelSpec(IBlockState state) {
        return modelSpec;
    }

    public Trans3 localToGlobalRotation(IBlockAccess world, BlockPos pos) {
        return localToGlobalRotation(world, pos, world.getBlockState(pos));
    }

    public Trans3 localToGlobalRotation(IBlockAccess world, BlockPos pos, IBlockState state) {
        return localToGlobalTransformation(world, pos, state, Vector3.zero);
    }

    public Trans3 localToGlobalTransformation(IBlockAccess world, BlockPos pos) {
        return localToGlobalTransformation(world, pos, world.getBlockState(pos));
    }

    public Trans3 localToGlobalTransformation(IBlockAccess world, BlockPos pos, IBlockState state) {
        return localToGlobalTransformation(world, pos, state, Vector3.blockCenter(pos));
    }

    public Trans3 localToGlobalTransformation(IBlockAccess world, BlockPos pos, IBlockState state, Vector3 origin) {
        IOrientationHandler oh = getOrientationHandler();
        return oh.localToGlobalTransformation(world, pos, state, origin);
    }

    public Trans3 itemTransformation() {
        return Trans3.ident;
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        state = super.getExtendedState(state, world, pos);
        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState eState = (IExtendedBlockState) state;
            eState = eState.withProperty(POS_PROP, pos).withProperty(BLOCKACCESS_PROP, world);
            return eState;
        } else {
            return super.getExtendedState(state, world, pos);
        }
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return tileEntityClass != null;
    }

    public TE getTileEntity(IBlockAccess world, BlockPos pos) {
        if (hasTileEntity()) {
            TileEntity te = world.getTileEntity(pos);
            if (tileEntityClass.isInstance(te))
                return (TE) te;
        }
        return null;
    }

    // -------------------------- Tile Entity -----------------------------

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if (tileEntityClass != null) {
            try {
                return tileEntityClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else
            return null;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        IBlockState state = getOrientationHandler().onBlockPlaced(this, world, pos, side,
                hitX, hitY, hitZ, getStateFromMeta(meta), placer);
        return state;
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);

        if (hasTileEntity(state)) {
            TileEntity te = getTileEntity(world, pos);
            if (te instanceof TileArchitecture)
                ((TileArchitecture) te).onAddedToWorld();
        }
    }

    // -------------------------------------------------------------------

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (hasTileEntity(state)) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof IInventory)
                InventoryHelper.dropInventoryItems(world, pos, (IInventory) te);
        }
    }

    @Override
    public boolean addLandingEffects(IBlockState state, WorldServer world, BlockPos pos,
                                     IBlockState iblockstate, EntityLivingBase entity, int numParticles) {
        IBlockState particleState = getParticleState(world, pos);
        world.spawnParticle(EnumParticleTypes.BLOCK_DUST, entity.posX, entity.posY, entity.posZ,
                numParticles, 0, 0, 0, 0.15, new int[]{Block.getStateId(particleState)});
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean addHitEffects(IBlockState blockState, World world, RayTraceResult target, ParticleManager pm) {
        BlockPos pos = target.getBlockPos();
        IBlockState state = getParticleState(world, pos);
        ParticleDigging fx;
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        AxisAlignedBB boundingBox = blockState.getBoundingBox(world, pos);
        float f = 0.1F;
        double d0 = i + RANDOM.nextDouble() * (boundingBox.maxX - boundingBox.minX - (f * 2.0F)) + f + boundingBox.minX;
        double d1 = j + RANDOM.nextDouble() * (boundingBox.maxY - boundingBox.minY - (f * 2.0F)) + f + boundingBox.minY;
        double d2 = k + RANDOM.nextDouble() * (boundingBox.maxZ - boundingBox.minZ - (f * 2.0F)) + f + boundingBox.minZ;
        switch (target.sideHit) {
            case DOWN:
                d1 = j + boundingBox.minY - f;
                break;
            case UP:
                d1 = j + boundingBox.maxY + f;
                break;
            case NORTH:
                d2 = k + boundingBox.minZ - f;
                break;
            case SOUTH:
                d2 = k + boundingBox.maxZ + f;
                break;
            case WEST:
                d0 = i + boundingBox.minX - f;
                break;
            case EAST:
                d0 = i + boundingBox.maxX + f;
                break;
        }
        fx = new DiggingFX(world, d0, d1, d2, 0, 0, 0, state);
        pm.addEffect(fx.setBlockPos(pos).multiplyVelocity(0.2F).multipleParticleScaleBy(0.6F));
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager pm) {
        IBlockState state = getParticleState(world, pos);
        ParticleDigging fx;
        byte b0 = 4;
        for (int i = 0; i < b0; ++i) {
            for (int j = 0; j < b0; ++j) {
                for (int k = 0; k < b0; ++k) {
                    double d0 = pos.getX() + (i + 0.5D) / b0;
                    double d1 = pos.getY() + (j + 0.5D) / b0;
                    double d2 = pos.getZ() + (k + 0.5D) / b0;
                    fx = new DiggingFX(world, d0, d1, d2,
                            d0 - pos.getX() - 0.5D, d1 - pos.getY() - 0.5D, d2 - pos.getZ() - 0.5D,
                            state);
                    pm.addEffect(fx.setBlockPos(pos));
                }
            }
        }
        return true;
    }

    public IBlockState getParticleState(IBlockAccess world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        return getActualState(state, world, pos);
    }

    @Override
    public RayTraceResult collisionRayTrace(IBlockState state, World world, BlockPos pos, Vec3d start, Vec3d end) {
        boxHit = null;
        RayTraceResult result = null;
        double nearestDistance = 0;
        List<AxisAlignedBB> list = getGlobalCollisionBoxes(world, pos, state, null);
        if (list != null) {
            int n = list.size();
            for (int i = 0; i < n; i++) {
                AxisAlignedBB box = list.get(i);
                RayTraceResult mp = box.calculateIntercept(start, end);
                if (mp != null) {
                    mp.subHit = i;
                    double d = start.squareDistanceTo(mp.hitVec);
                    if (result == null || d < nearestDistance) {
                        result = mp;
                        nearestDistance = d;
                    }
                }
            }
        }
        if (result != null) {
            //setBlockBounds(list.get(result.subHit));
            int i = result.subHit;
            boxHit = list.get(i).offset(-pos.getX(), -pos.getY(), -pos.getZ());
            result = new RayTraceResult(result.hitVec, result.sideHit, pos);
            result.subHit = i;
        }
        return result;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        AxisAlignedBB box = boxHit;
        if (box == null)
            box = getLocalBounds(world, pos, state, null);
        if (box != null)
            return box;
        else
            return super.getBoundingBox(state, world, pos);
    }

    // Workaround for ParticleDigging having protected constructor

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        return getBoundingBox(state, world, pos);
    }

    //----------------------------- Bounds and collision boxes -----------------------------------

    protected AxisAlignedBB getLocalBounds(IBlockAccess world, BlockPos pos, IBlockState state,
                                           Entity entity) {
        ModelSpec spec = getModelSpec(state);
        if (spec != null) {
            IArchitectureModel model = ArchitectureMod.PROXY.getModel(spec.modelName);
            Trans3 t = localToGlobalTransformation(world, pos, state, Vector3.blockCenter).translate(spec.origin);
            return t.t(model.getBounds());
        }
        return null;
    }

    public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB clip, List<AxisAlignedBB> result, @Nullable Entity entity, boolean b) {
        List<AxisAlignedBB> list = getGlobalCollisionBoxes(world, pos, state, entity);
        if (list != null)
            for (AxisAlignedBB box : list)
                if (clip.intersects(box))
                    result.add(box);
                else
                    super.addCollisionBoxToList(state, world, pos, clip, result, entity, b);
    }

    protected List<AxisAlignedBB> getGlobalCollisionBoxes(IBlockAccess world, BlockPos pos,
                                                          IBlockState state, Entity entity) {
        Trans3 t = localToGlobalTransformation(world, pos, state);
        return getCollisionBoxes(world, pos, state, t, entity);
    }

    protected List<AxisAlignedBB> getLocalCollisionBoxes(IBlockAccess world, BlockPos pos,
                                                         IBlockState state, Entity entity) {
        Trans3 t = localToGlobalTransformation(world, pos, state, Vector3.zero);
        return getCollisionBoxes(world, pos, state, t, entity);
    }

    protected List<AxisAlignedBB> getCollisionBoxes(IBlockAccess world, BlockPos pos, IBlockState state,
                                                    Trans3 t, Entity entity) {
        ModelSpec spec = getModelSpec(state);
        if (spec != null) {
            IArchitectureModel model = ArchitectureMod.PROXY.getModel(spec.modelName);
            List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
            model.addBoxesToList(t.translate(spec.origin), list);
            return list;
        }
        return null;
    }


    public interface IOrientationHandler {

        void defineProperties(BlockArchitecture block);

        IBlockState onBlockPlaced(Block block, World world, BlockPos pos, EnumFacing side,
                                  float hitX, float hitY, float hitZ, IBlockState baseState, EntityLivingBase placer);

        //Trans3 localToGlobalTransformation(IBlockAccess world, BlockPos pos, IBlockState state);
        Trans3 localToGlobalTransformation(IBlockAccess world, BlockPos pos, IBlockState state, Vector3 origin);
    }

    public static class Orient1Way implements IOrientationHandler {

        public void defineProperties(BlockArchitecture block) {
        }

        public IBlockState onBlockPlaced(Block block, World world, BlockPos pos, EnumFacing side,
                                         float hitX, float hitY, float hitZ, IBlockState baseState, EntityLivingBase placer) {
            return baseState;
        }

        public Trans3 localToGlobalTransformation(IBlockAccess world, BlockPos pos, IBlockState state, Vector3 origin) {
            return new Trans3(origin);
        }

    }

    @SideOnly(Side.CLIENT)
    public static class DiggingFX extends ParticleDigging {

        public DiggingFX(World world, double x1, double y1, double z1, double x2, double y2, double z2, IBlockState state) {
            super(world, x1, y1, z1, x2, y2, z2, state);
        }

    }

}
