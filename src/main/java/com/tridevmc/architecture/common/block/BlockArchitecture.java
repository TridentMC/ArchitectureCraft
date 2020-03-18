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

package com.tridevmc.architecture.common.block;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.client.render.model.OBJSON;
import com.tridevmc.architecture.common.ArchitectureLog;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Vector3;
import com.tridevmc.architecture.common.render.ITextureConsumer;
import com.tridevmc.architecture.common.render.ModelSpec;
import com.tridevmc.architecture.common.tile.TileArchitecture;
import com.tridevmc.architecture.common.utils.MiscUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.IProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootParameters;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("ALL")
public class BlockArchitecture<TE extends TileArchitecture>
        extends ContainerBlock implements ITextureConsumer {

    public static boolean debugState = false;
    // --------------------------- Orientation -------------------------------
    public static IOrientationHandler orient1Way = new Orient1Way();
    protected MaterialColor materialColor;
    protected IProperty[] properties;
    // --------------------------- Members -------------------------------
    protected Object[][] propertyValues;
    protected int numProperties; // Do not explicitly initialise
    protected BlockRenderType renderID = BlockRenderType.MODEL;
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
        super(Block.Properties.create(material, material.getColor()));
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
            ArchitectureLog.info("BaseBlock.addProperty: %s to %s\n", property, getClass().getName());
        if (numProperties < 4) {
            int i = numProperties++;
            properties[i] = property;
            Object[] values = MiscUtils.arrayOf(property.getAllowedValues());
            propertyValues[i] = values;
        } else
            throw new IllegalStateException("Block " + getClass().getName() +
                    " has too many properties");
        if (debugState)
            ArchitectureLog.info("BaseBlock.addProperty: %s now has %s properties\n",
                    getClass().getName(), numProperties);
    }

    private void dumpProperties() {
        ArchitectureLog.info("BaseBlock: Properties of %s:\n", getClass().getName());
        for (int i = 0; i < numProperties; i++) {
            ArchitectureLog.info("%s: %s\n", i, properties[i]);
            Object[] values = propertyValues[i];
            for (int j = 0; j < values.length; j++)
                ArchitectureLog.info("   %s: %s\n", j, values[j]);
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

    public int getNumSubtypes() {
        return 1;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        if (this.properties == null) this.defineProperties();
        Arrays.stream(this.properties).filter(Objects::nonNull).forEach(builder::add);
        super.fillStateContainer(builder);
    }

    // -------------------------- Subtypes ------------------------------

    @Override
    public void harvestBlock(World world, PlayerEntity player, BlockPos pos, BlockState state, TileEntity te, ItemStack stack) {
        harvestingTileEntity.set(te);
        super.harvestBlock(world, player, pos, state, te, stack);
        harvestingTileEntity.set(null);
    }

    // -------------------------- Harvesting ----------------------------

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder context) {
        NonNullList<ItemStack> drops = NonNullList.create();
        TileEntity te = context.getWorld().getTileEntity(context.get(LootParameters.POSITION));
        if (te == null)
            te = harvestingTileEntity.get();
        drops.addAll(getDropsFromTileEntity(context, state));
        return null;
    }

    protected NonNullList<ItemStack> getDropsFromTileEntity(LootContext.Builder context, BlockState state) {
        NonNullList<ItemStack> drops = NonNullList.create();
        super.getDrops(state, context);
        return drops;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
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

    public ModelSpec getModelSpec(BlockState state) {
        return modelSpec;
    }

    public Trans3 localToGlobalRotation(IBlockReader world, BlockPos pos) {
        return localToGlobalRotation(world, pos, world.getBlockState(pos));
    }

    public Trans3 localToGlobalRotation(IBlockReader world, BlockPos pos, BlockState state) {
        return localToGlobalTransformation(world, pos, state, Vector3.zero);
    }

    public Trans3 localToGlobalTransformation(IBlockReader world, BlockPos pos) {
        return localToGlobalTransformation(world, pos, world.getBlockState(pos));
    }

    public Trans3 localToGlobalTransformation(IBlockReader world, BlockPos pos, BlockState state) {
        return localToGlobalTransformation(world, pos, state, Vector3.blockCenter(pos));
    }

    public Trans3 localToGlobalTransformation(IBlockReader world, BlockPos pos, BlockState state, Vector3 origin) {
        IOrientationHandler oh = getOrientationHandler();
        return oh.localToGlobalTransformation(world, pos, state, origin);
    }

    public Trans3 itemTransformation() {
        return Trans3.ident;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return tileEntityClass != null;
    }

    public TE getTileEntity(IBlockReader world, BlockPos pos) {
        if (hasTileEntity()) {
            TileEntity te = world.getTileEntity(pos);
            if (tileEntityClass.isInstance(te))
                return (TE) te;
        }
        return null;
    }

    // -------------------------- Tile Entity -----------------------------

    @Override
    public TileEntity createNewTileEntity(IBlockReader reader) {
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
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Vec3d hit = context.getHitVec();
        BlockState state = getOrientationHandler().onBlockPlaced(this, context.getWorld(), context.getPos(), context.getFace(),
                hit.getX(), hit.getY(), hit.getZ(), getDefaultState(), context.getPlayer());
        return state;
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onBlockAdded(state, world, pos, oldState, isMoving);

        if (hasTileEntity(state)) {
            TileEntity te = getTileEntity(world, pos);
            if (te instanceof TileArchitecture)
                ((TileArchitecture) te).onAddedToWorld();
        }
    }

    // -------------------------------------------------------------------


    @Override
    public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
        if (hasTileEntity(state)) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof IInventory)
                InventoryHelper.dropInventoryItems(world, pos, (IInventory) te);
        }
        super.onReplaced(state, world, pos, newState, isMoving);
    }

    @Override
    public boolean addLandingEffects(BlockState state, ServerWorld world, BlockPos pos,
                                     BlockState iblockstate, LivingEntity entity, int numParticles) {
        BlockState particleState = getParticleState(world, pos);
        world.spawnParticle(new BlockParticleData(ParticleTypes.BLOCK, particleState), entity.serverPosX, entity.serverPosY, entity.serverPosZ,
                numParticles, 0, 0, 0, 0.15);
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean addHitEffects(BlockState blockState, World world, RayTraceResult target, ParticleManager pm) {
        if (!(target instanceof BlockRayTraceResult))
            return false;

        BlockRayTraceResult hit = (BlockRayTraceResult) target;
        BlockPos pos = hit.getPos();
        BlockState state = getParticleState(world, pos);
        DiggingParticle fx;
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        VoxelShape voxelShape = blockState.getShape(world, pos);
        AxisAlignedBB boundingBox = voxelShape.getBoundingBox();
        float f = 0.1F;
        double d0 = i + RANDOM.nextDouble() * (boundingBox.maxX - boundingBox.minX - (f * 2.0F)) + f + boundingBox.minX;
        double d1 = j + RANDOM.nextDouble() * (boundingBox.maxY - boundingBox.minY - (f * 2.0F)) + f + boundingBox.minY;
        double d2 = k + RANDOM.nextDouble() * (boundingBox.maxZ - boundingBox.minZ - (f * 2.0F)) + f + boundingBox.minZ;
        switch (hit.getFace()) {
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

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager pm) {
        DiggingParticle fx;
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

    public BlockState getParticleState(IBlockReader world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        return getExtendedState(state, world, pos);
    }

    @Override
    public RayTraceResult getRayTraceResult(BlockState state, World world, BlockPos pos, Vec3d start, Vec3d end, RayTraceResult original) {
        boxHit = null;
        BlockRayTraceResult result = null;
        double nearestDistance = 0;
        List<AxisAlignedBB> list = getGlobalCollisionBoxes(world, pos, state, null);
        if (list != null) {
            int n = list.size();
            for (int i = 0; i < n; i++) {
                AxisAlignedBB box = list.get(i);
                BlockRayTraceResult mp = AxisAlignedBB.rayTrace(ImmutableList.of(box), start, end, pos);
                if (mp != null) {
                    mp.subHit = i;
                    double d = start.squareDistanceTo(mp.getHitVec());
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
            result = new BlockRayTraceResult(result.getHitVec(), result.getFace(), pos, false);
            result.subHit = i;
        }
        return result;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        AxisAlignedBB box = boxHit;
        if (box == null)
            box = getLocalBounds(world, pos, state, null);

        if (box != null)
            return VoxelShapes.create(box);
        else
            return super.getShape(state, world, pos, context);
    }

    //----------------------------- Bounds and collision boxes -----------------------------------

    protected AxisAlignedBB getLocalBounds(IBlockReader world, BlockPos pos, BlockState state,
                                           Entity entity) {
        ModelSpec spec = getModelSpec(state);
        if (spec != null) {
            OBJSON model = ArchitectureMod.PROXY.getCachedOBJSON(spec.modelName);
            Trans3 t = localToGlobalTransformation(world, pos, state, Vector3.blockCenter).translate(spec.origin);
            return t.t(model.getBounds());
        }
        return null;
    }

    protected List<AxisAlignedBB> getGlobalCollisionBoxes(IBlockReader world, BlockPos pos,
                                                          BlockState state, Entity entity) {
        Trans3 t = localToGlobalTransformation(world, pos, state);
        return getCollisionBoxes(world, pos, state, t, entity);
    }

    protected List<AxisAlignedBB> getLocalCollisionBoxes(IBlockReader world, BlockPos pos,
                                                         BlockState state, Entity entity) {
        Trans3 t = localToGlobalTransformation(world, pos, state, Vector3.zero);
        return getCollisionBoxes(world, pos, state, t, entity);
    }

    protected List<AxisAlignedBB> getCollisionBoxes(IBlockReader world, BlockPos pos, BlockState state,
                                                    Trans3 t, Entity entity) {
        ModelSpec spec = getModelSpec(state);
        if (spec != null) {
            OBJSON model = ArchitectureMod.PROXY.getCachedOBJSON(spec.modelName);
            List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
            model.addBoxesToList(t.translate(spec.origin), list);
            return list;
        }
        return null;
    }

    public interface IOrientationHandler {

        void defineProperties(BlockArchitecture block);

        BlockState onBlockPlaced(Block block, World world, BlockPos pos, Direction side,
                                 double hitX, double hitY, double hitZ, BlockState baseState, LivingEntity placer);

        //Trans3 localToGlobalTransformation(IBlockReader world, BlockPos pos, IBlockState state);
        Trans3 localToGlobalTransformation(IBlockReader world, BlockPos pos, BlockState state, Vector3 origin);
    }

    public static class Orient1Way implements IOrientationHandler {

        public void defineProperties(BlockArchitecture block) {
        }

        public BlockState onBlockPlaced(Block block, World world, BlockPos pos, Direction side,
                                        double hitX, double hitY, double hitZ, BlockState baseState, LivingEntity placer) {
            return baseState;
        }

        public Trans3 localToGlobalTransformation(IBlockReader world, BlockPos pos, BlockState state, Vector3 origin) {
            return new Trans3(origin);
        }

    }

    @OnlyIn(Dist.CLIENT)
    public static class DiggingFX extends DiggingParticle {

        public DiggingFX(World world, double x1, double y1, double z1, double x2, double y2, double z2, BlockState state) {
            super(world, x1, y1, z1, x2, y2, z2, state);
        }

    }

}
