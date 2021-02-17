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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tridevmc.architecture.client.render.model.OBJSON;
import com.tridevmc.architecture.common.ArchitectureLog;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.block.state.BlockStateArchitecture;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Vector3;
import com.tridevmc.architecture.common.render.ITextureConsumer;
import com.tridevmc.architecture.common.render.ModelSpec;
import com.tridevmc.architecture.common.utils.MiscUtils;
import com.tridevmc.compound.core.reflect.WrappedField;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.client.particle.DiggingParticle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;

public abstract class BlockArchitecture extends ContainerBlock implements ITextureConsumer {

    private static final WrappedField<StateContainer<Block, BlockState>> STATE_CONTAINER = WrappedField.create(Block.class, "stateContainer", "field_176227_L");
    private static final LoadingCache<ShapeContext, VoxelShape> SHAPE_CACHE = CacheBuilder.newBuilder().build(new CacheLoader<ShapeContext, VoxelShape>() {
        public VoxelShape load(@Nonnull ShapeContext shapeContext) {
            VoxelShape shape = shapeContext.state.getBlock().getLocalBounds(shapeContext.world, shapeContext.pos, shapeContext.state, null);
            return shape;
        }
    });

    public static boolean debugState = false;
    // --------------------------- Orientation -------------------------------
    public static IOrientationHandler orient1Way = new Orient1Way();
    protected MaterialColor materialColor;
    protected Property[] stateProperties;
    // --------------------------- Members -------------------------------
    protected Object[][] propertyValues;
    protected int numProperties; // Do not explicitly initialise
    protected BlockRenderType renderID = BlockRenderType.MODEL;
    protected IOrientationHandler orientationHandler = orient1Way;
    protected String[] textureNames;
    protected ModelSpec modelSpec;


    // --------------------------- Constructors -------------------------------

    public BlockArchitecture(Material material) {
        this(material, null);
    }

    public BlockArchitecture(Material material, IOrientationHandler orient) {
        super(Block.Properties.create(material, material.getColor()).notSolid());
        if (orient == null)
            orient = orient1Way;
        this.orientationHandler = orient;
        StateContainer.Builder<Block, BlockState> builder = new StateContainer.Builder<>(this);
        this.fillStateContainer(builder);
        STATE_CONTAINER.set(this, builder.func_235882_a_(Block::getDefaultState, (block, propertyValues, codec) -> new BlockStateArchitecture((BlockArchitecture) block, propertyValues, codec)));
        this.setDefaultState(this.stateContainer.getBaseState());
    }

    public IOrientationHandler getOrientationHandler() {
        return orient1Way;
    }

    // --------------------------- States -------------------------------

    protected void defineProperties() {
        this.stateProperties = new Property[4];
        this.propertyValues = new Object[4][];
        this.getOrientationHandler().defineProperties(this);
    }

    public void addProperty(Property property) {
        if (debugState)
            ArchitectureLog.info("BaseBlock.addProperty: %s to %s\n", property, this.getClass().getName());
        if (this.numProperties < 4) {
            int i = this.numProperties++;
            this.stateProperties[i] = property;
            Object[] values = MiscUtils.arrayOf(property.getAllowedValues());
            this.propertyValues[i] = values;
        } else
            throw new IllegalStateException("Block " + this.getClass().getName() +
                    " has too many properties");
        if (debugState)
            ArchitectureLog.info("BaseBlock.addProperty: %s now has %s properties\n",
                    this.getClass().getName(), this.numProperties);
    }

    private void dumpProperties() {
        ArchitectureLog.info("BaseBlock: Properties of %s:\n", this.getClass().getName());
        for (int i = 0; i < this.numProperties; i++) {
            ArchitectureLog.info("%s: %s\n", i, this.stateProperties[i]);
            Object[] values = this.propertyValues[i];
            for (int j = 0; j < values.length; j++)
                ArchitectureLog.info("   %s: %s\n", j, values[j]);
        }
    }

    protected void checkProperties() {
        int n = 1;
        for (int i = 0; i < this.numProperties; i++)
            n *= this.propertyValues[i].length;
        if (n > 16)
            throw new IllegalStateException(String.format(
                    "Block %s has %s combinations of property values (16 allowed)",
                    this.getClass().getName(), n));
    }

    public int getNumSubtypes() {
        return 1;
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        if (this.stateProperties == null) this.defineProperties();
        Arrays.stream(this.stateProperties).filter(Objects::nonNull).forEach(builder::add);
        super.fillStateContainer(builder);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return this.renderID;
    }

    public String getQualifiedRendererClassName() {
        String name = this.getRendererClassName();
        if (name != null)
            name = this.getClass().getPackage().getName() + "." + name;
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
        return this.textureNames;
    }

    public ModelSpec getModelSpec(BlockState state) {
        return this.modelSpec;
    }

    public Trans3 localToGlobalRotation(IBlockReader world, BlockPos pos) {
        return this.localToGlobalRotation(world, pos, world.getBlockState(pos));
    }

    public Trans3 localToGlobalRotation(IBlockReader world, BlockPos pos, BlockState state) {
        return this.localToGlobalTransformation(world, pos, state, Vector3.zero);
    }

    public Trans3 localToGlobalTransformation(IBlockReader world, BlockPos pos) {
        return this.localToGlobalTransformation(world, pos, world.getBlockState(pos));
    }

    public Trans3 localToGlobalTransformation(IBlockReader world, BlockPos pos, BlockState state) {
        return this.localToGlobalTransformation(world, pos, state, Vector3.zero);
    }

    public Trans3 localToGlobalTransformation(IBlockReader world, BlockPos pos, BlockState state, Vector3 origin) {
        IOrientationHandler oh = this.getOrientationHandler();
        return oh.localToGlobalTransformation(world, pos, state, origin);
    }

    public Trans3 itemTransformation() {
        return Trans3.ident;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Vector3d hit = context.getHitVec();
        BlockState state = this.getOrientationHandler().onBlockPlaced(this, context.getWorld(), context.getPos(), context.getFace(),
                hit.getX(), hit.getY(), hit.getZ(), this.getDefaultState(), context.getPlayer());
        return state;
    }

    @Override
    public boolean addLandingEffects(BlockState state, ServerWorld world, BlockPos pos,
                                     BlockState iblockstate, LivingEntity entity, int numParticles) {
        BlockState particleState = this.getParticleState(world, pos);
        world.spawnParticle(new BlockParticleData(ParticleTypes.BLOCK, particleState), entity.getPosX(), entity.getPosY(), entity.getPosZ(),
                numParticles, 0, 0, 0, 0.15);
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean addHitEffects(BlockState blockState, World world, RayTraceResult target, ParticleManager pm) {
        if (!(target instanceof BlockRayTraceResult) || !(world instanceof ClientWorld))
            return false;

        BlockRayTraceResult hit = (BlockRayTraceResult) target;
        BlockPos pos = hit.getPos();
        BlockState state = this.getParticleState(world, pos);
        DiggingParticle fx;
        int i = pos.getX();
        int j = pos.getY();
        int k = pos.getZ();
        VoxelShape voxelShape = blockState.getShape(world, pos);
        AxisAlignedBB boundingBox = voxelShape.getBoundingBox();
        float f = 0.1F;
        double d0 = i + this.RANDOM.nextDouble() * (boundingBox.maxX - boundingBox.minX - (f * 2.0F)) + f + boundingBox.minX;
        double d1 = j + this.RANDOM.nextDouble() * (boundingBox.maxY - boundingBox.minY - (f * 2.0F)) + f + boundingBox.minY;
        double d2 = k + this.RANDOM.nextDouble() * (boundingBox.maxZ - boundingBox.minZ - (f * 2.0F)) + f + boundingBox.minZ;
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
        fx = new DiggingFX((ClientWorld) world, d0, d1, d2, 0, 0, 0, state);
        pm.addEffect(fx.setBlockPos(pos).multiplyVelocity(0.2F).multiplyParticleScaleBy(0.6F));
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public boolean addDestroyEffects(BlockState state, World world, BlockPos pos, ParticleManager pm) {
        if (!(world instanceof ClientWorld))
            return false;

        DiggingParticle fx;
        byte b0 = 4;
        for (int i = 0; i < b0; ++i) {
            for (int j = 0; j < b0; ++j) {
                for (int k = 0; k < b0; ++k) {
                    double d0 = pos.getX() + (i + 0.5D) / b0;
                    double d1 = pos.getY() + (j + 0.5D) / b0;
                    double d2 = pos.getZ() + (k + 0.5D) / b0;
                    fx = new DiggingFX((ClientWorld) world, d0, d1, d2,
                            d0 - pos.getX() - 0.5D, d1 - pos.getY() - 0.5D, d2 - pos.getZ() - 0.5D,
                            state);
                    pm.addEffect(fx.setBlockPos(pos));
                }
            }
        }
        return true;
    }

    public BlockState getParticleState(IBlockReader world, BlockPos pos) {
        return world.getBlockState(pos);
    }

    //@Override TODO: Replace with raytraceshape. We need mesh voxelization...
    //public RayTraceResult getRayTraceResult(BlockState state, World world, BlockPos pos, Vector3d start, Vector3d end, RayTraceResult original) {
    //    boxHit = null;
    //    BlockRayTraceResult result = null;
    //    double nearestDistance = 0;
    //    List<AxisAlignedBB> list = getGlobalCollisionBoxes(world, pos, state, null);
    //    if (list != null) {
    //        int n = list.size();
    //        for (int i = 0; i < n; i++) {
    //            AxisAlignedBB box = list.get(i);
    //            BlockRayTraceResult mp = AxisAlignedBB.rayTrace(ImmutableList.of(box), start, end, pos);
    //            if (mp != null) {
    //                mp.subHit = i;
    //                double d = start.squareDistanceTo(mp.getHitVec());
    //                if (result == null || d < nearestDistance) {
    //                    result = mp;
    //                    nearestDistance = d;
    //                }
    //            }
    //        }
    //    }
    //    if (result != null) {
    //        //setBlockBounds(list.get(result.subHit));
    //        int i = result.subHit;
    //        boxHit = list.get(i).offset(-pos.getX(), -pos.getY(), -pos.getZ());
    //        result = new BlockRayTraceResult(result.getHitVec(), result.getFace(), pos, false);
    //        result.subHit = i;
    //    }
    //    return result;
    //}

    public static VoxelShape getCachedShape(ShapeContext context) {
        VoxelShape shape = SHAPE_CACHE.getUnchecked(context);
        if (shape.isEmpty()) {
            SHAPE_CACHE.invalidate(context);
            shape = VoxelShapes.fullCube();
        }
        return shape;
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        return getCachedShape(new ShapeContext((BlockStateArchitecture) state, world, pos));
    }

    //----------------------------- Bounds and collision boxes -----------------------------------

    @Nonnull
    protected VoxelShape getLocalBounds(IBlockReader world, BlockPos pos, BlockState state, Entity entity) {
        ModelSpec spec = this.getModelSpec(state);
        if (spec != null) {
            OBJSON model = ArchitectureMod.PROXY.getCachedOBJSON(spec.modelName);
            Trans3 t = this.localToGlobalTransformation(world, pos, state, Vector3.zero).translate(spec.origin);
            return t.t(model.getVoxelized());
        }
        return VoxelShapes.empty();
    }

    @Nonnull
    protected VoxelShape getGlobalCollisionBoxes(IBlockReader world, BlockPos pos,
                                                 BlockState state, Entity entity) {
        Trans3 t = this.localToGlobalTransformation(world, pos, state);
        return this.getCollisionBoxes(world, pos, state, t, entity);
    }

    @Nonnull
    protected VoxelShape getLocalCollisionBoxes(IBlockReader world, BlockPos pos,
                                                BlockState state, Entity entity) {
        Trans3 t = this.localToGlobalTransformation(world, pos, state, Vector3.zero);
        return this.getCollisionBoxes(world, pos, state, t, entity);
    }

    @Nonnull
    protected VoxelShape getCollisionBoxes(IBlockReader world, BlockPos pos, BlockState state,
                                           Trans3 t, Entity entity) {
        ModelSpec spec = this.getModelSpec(state);
        if (spec != null) {
            OBJSON model = ArchitectureMod.PROXY.getCachedOBJSON(spec.modelName);
            return model.getShape(t.translate(spec.origin), VoxelShapes.empty());
        }
        return VoxelShapes.empty();
    }

    public float getBlockHardness(BlockState state, IBlockReader world, BlockPos pos, float hardness) {
        return hardness;
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

        public DiggingFX(ClientWorld world, double x1, double y1, double z1, double x2, double y2, double z2, BlockState state) {
            super(world, x1, y1, z1, x2, y2, z2, state);
        }

    }

    public class ShapeContext {
        private final BlockStateArchitecture state;
        private final IBlockReader world;
        private final BlockPos pos;

        public ShapeContext(BlockStateArchitecture state, IBlockReader world, BlockPos pos) {
            this.state = state;
            this.world = world;
            this.pos = pos;
        }

        @Override
        public int hashCode() {
            return this.state.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ShapeContext) {
                return ((ShapeContext) obj).state == this.state;
            }
            return false;
        }
    }

}
