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

import com.tridevmc.architecture.client.render.model.objson.LegacyOBJSON;
import com.tridevmc.architecture.core.ArchitectureLog;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.block.state.BlockStateArchitecture;
import com.tridevmc.architecture.core.math.Trans3;
import com.tridevmc.architecture.core.math.LegacyVector3;
import com.tridevmc.architecture.common.model.ITextureConsumer;
import com.tridevmc.architecture.common.model.ModelSpec;
import com.tridevmc.architecture.common.utils.MiscUtils;
import com.tridevmc.compound.core.reflect.WrappedField;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientBlockExtensions;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class BlockArchitecture extends BaseEntityBlock implements ITextureConsumer {

    private static final WrappedField<StateDefinition<Block, BlockState>> STATE_CONTAINER = WrappedField.create(Block.class, "stateDefinition", "f_49792_");

    private final Int2ObjectLinkedOpenHashMap<Trans3> transCache = new Int2ObjectLinkedOpenHashMap<>();
    private final Int2ObjectLinkedOpenHashMap<VoxelShape> shapeCache = new Int2ObjectLinkedOpenHashMap<>();

    private static final RandomSource RANDOM = RandomSource.create();
    public static boolean debugState = false;
    // --------------------------- Orientation -------------------------------
    public static IOrientationHandler orient1Way = new Orient1Way();
    protected MaterialColor materialColor;
    protected Property[] stateProperties;
    // --------------------------- Members -------------------------------
    protected Object[][] propertyValues;
    protected int numProperties; // Do not explicitly initialise
    protected RenderShape renderID = RenderShape.MODEL;
    protected IOrientationHandler orientationHandler = orient1Way;
    protected String[] textureNames;
    protected ModelSpec modelSpec;


    // --------------------------- Constructors -------------------------------

    public BlockArchitecture(Material material) {
        this(material, null);
    }

    public BlockArchitecture(Material material, IOrientationHandler orient) {
        super(Block.Properties.of(material, material.getColor()).dynamicShape());
        if (orient == null)
            orient = orient1Way;
        this.orientationHandler = orient;
        var builder = new StateDefinition.Builder<Block, BlockState>(this);
        this.createBlockStateDefinition(builder);
        STATE_CONTAINER.set(this, builder.create(Block::defaultBlockState, (block, propertyValues, codec) -> new BlockStateArchitecture((BlockArchitecture) block, propertyValues, codec)));
        this.registerDefaultState(this.getStateDefinition().any());
    }

    public IOrientationHandler getOrientationHandler() {
        return orient1Way;
    }

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
        consumer.accept(new IClientBlockExtensions() {
            @Override
            public boolean addHitEffects(BlockState state, Level level, HitResult target, ParticleEngine manager) {
                return BlockArchitecture.this.addHitEffects(state, level, target, manager);
            }

            @Override
            public boolean addDestroyEffects(BlockState state, Level Level, BlockPos pos, ParticleEngine manager) {
                return BlockArchitecture.this.addDestroyEffects(state, Level, pos, manager);
            }
        });
    }

    // region States

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
            Object[] values = MiscUtils.arrayOf(property.getPossibleValues());
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

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        if (this.stateProperties == null) this.defineProperties();
        Arrays.stream(this.stateProperties).filter(Objects::nonNull).forEach(builder::add);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return this.renderID;
    }

    public String getQualifiedRendererClassName() {
        String name = this.getRendererClassName();
        if (name != null)
            name = this.getClass().getPackage().getName() + "." + name;
        return name;
    }
    // endregion

    // region Rendering

    protected String getRendererClassName() {
        return null;
    }

    public void setModelAndTextures(String modelName, String... textureNames) {
        this.textureNames = textureNames;
        this.modelSpec = new ModelSpec(modelName, textureNames);
    }

    public void setModelAndTextures(String modelName, LegacyVector3 origin, String... textureNames) {
        this.textureNames = textureNames;
        this.modelSpec = new ModelSpec(modelName, origin, textureNames);
    }

    public String[] getTextureNames() {
        return this.textureNames;
    }

    public ModelSpec getModelSpec(BlockState state) {
        return this.modelSpec;
    }

    public Trans3 localToGlobalRotation(BlockAndTintGetter level, BlockPos pos) {
        return this.localToGlobalRotation(level, pos, level.getBlockState(pos));
    }

    public Trans3 localToGlobalRotation(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        return this.localToGlobalTransformation(level, pos, state, LegacyVector3.ZERO);
    }

    public Trans3 localToGlobalTransformation(BlockGetter level, BlockPos pos) {
        return this.localToGlobalTransformation(level, pos, level.getBlockState(pos));
    }

    public Trans3 localToGlobalTransformation(BlockGetter level, BlockPos pos, BlockState state) {
        return this.localToGlobalTransformation(level, pos, state, LegacyVector3.ZERO);
    }

    public Trans3 localToGlobalTransformation(BlockGetter level, BlockPos pos, BlockState state, LegacyVector3 origin) {
        var transIdentity = this.getTransIdentity(state, level, pos, origin);
        var trans = this.transCache.get(transIdentity);
        if (trans == null) {
            trans = this.getOrientationHandler().localToGlobalTransformation(level, pos, state, origin);
            this.transCache.put(transIdentity, trans);
        }
        return trans;
    }

    public Trans3 itemTransformation() {
        return Trans3.ident;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var hit = context.getClickLocation();
        return this.getOrientationHandler().onBlockPlaced(this, context.getLevel(), context.getClickedPos(), context.getNearestLookingDirection(),
                hit.x(), hit.y(), hit.z(), this.defaultBlockState(), context.getPlayer());
    }

    @Override
    public boolean addLandingEffects(BlockState state, ServerLevel level, BlockPos pos,
                                     BlockState iblockstate, LivingEntity entity, int numParticles) {
        BlockState particleState = this.getParticleState(level, pos);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, particleState).setPos(pos), entity.getX(), entity.getY(), entity.getZ(),
                numParticles, 0.0D, 0.0D, 0.0D, 0.15D);
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean addHitEffects(BlockState blockState, Level level, HitResult target, ParticleEngine pm) {
        if (!(target instanceof BlockHitResult hit) || !(level instanceof ClientLevel clientLevel))
            return false;

        var dir = hit.getDirection();
        var pos = hit.getBlockPos();
        var particleState = this.getParticleState(level, pos);
        int posX = pos.getX();
        int posY = pos.getY();
        int posZ = pos.getZ();
        AABB aabb = blockState.getShape(level, pos).bounds();
        double particleX = (double) posX + RANDOM.nextDouble() * (aabb.maxX - aabb.minX - (double) 0.2F) + (double) 0.1F + aabb.minX;
        double particleY = (double) posY + RANDOM.nextDouble() * (aabb.maxY - aabb.minY - (double) 0.2F) + (double) 0.1F + aabb.minY;
        double particleZ = (double) posZ + RANDOM.nextDouble() * (aabb.maxZ - aabb.minZ - (double) 0.2F) + (double) 0.1F + aabb.minZ;
        switch (dir) {
            case DOWN -> particleY = (double) posY + aabb.minY - (double) 0.1F;
            case UP -> particleY = (double) posY + aabb.maxY + (double) 0.1F;
            case NORTH -> particleZ = (double) posZ + aabb.minZ - (double) 0.1F;
            case SOUTH -> particleZ = (double) posZ + aabb.maxZ + (double) 0.1F;
            case WEST -> particleX = (double) posX + aabb.minX - (double) 0.1F;
            case EAST -> particleX = (double) posX + aabb.maxX + (double) 0.1F;
        }

        pm.add(new TerrainParticle(clientLevel, particleX, particleY, particleZ,
                0.0D, 0.0D, 0.0D, particleState, pos)
                .updateSprite(particleState, pos).setPower(0.2F).scale(0.6F));
        return true;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos, ParticleEngine pm) {
        if (!(level instanceof ClientLevel clientLevel))
            return false;

        var particleState = this.getParticleState(level, pos);
        VoxelShape voxelshape = state.getShape(level, pos);
        voxelshape.forAllBoxes((p_172273_, p_172274_, p_172275_, p_172276_, p_172277_, p_172278_) -> {
            double d1 = Math.min(1.0D, p_172276_ - p_172273_);
            double d2 = Math.min(1.0D, p_172277_ - p_172274_);
            double d3 = Math.min(1.0D, p_172278_ - p_172275_);
            int i = Math.max(2, Mth.ceil(d1 / 0.25D));
            int j = Math.max(2, Mth.ceil(d2 / 0.25D));
            int k = Math.max(2, Mth.ceil(d3 / 0.25D));

            for (int l = 0; l < i; ++l) {
                for (int i1 = 0; i1 < j; ++i1) {
                    for (int j1 = 0; j1 < k; ++j1) {
                        double d4 = ((double) l + 0.5D) / (double) i;
                        double d5 = ((double) i1 + 0.5D) / (double) j;
                        double d6 = ((double) j1 + 0.5D) / (double) k;
                        double d7 = d4 * d1 + p_172273_;
                        double d8 = d5 * d2 + p_172274_;
                        double d9 = d6 * d3 + p_172275_;
                        pm.add(new TerrainParticle(clientLevel,
                                (double) pos.getX() + d7, (double) pos.getY() + d8, (double) pos.getZ() + d9,
                                d4 - 0.5D, d5 - 0.5D, d6 - 0.5D,
                                particleState, pos).updateSprite(particleState, pos)
                        );
                    }
                }
            }
        });
        return true;
    }

    public BlockState getParticleState(BlockAndTintGetter level, BlockPos pos) {
        return level.getBlockState(pos);
    }

    // endregion

    // region Bounds and collision boxes

    @NotNull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        var shapeIdentity = this.getTransIdentity(state, level, pos, LegacyVector3.ZERO);
        var shape = this.shapeCache.get(shapeIdentity);
        if (shape == null) {
            shape = this.getLocalBounds(level, pos, state, null);
            if (shape.isEmpty()) {
                return Shapes.block();
            } else {
                this.shapeCache.put(shapeIdentity, shape);
            }
        }
        return shape;
    }

    @NotNull
    protected VoxelShape getLocalBounds(BlockGetter level, BlockPos pos, BlockState state, Entity entity) {
        ModelSpec spec = this.getModelSpec(state);
        if (spec != null) {
            LegacyOBJSON model = ArchitectureMod.PROXY.getCachedOBJSON(spec.modelName);
            Trans3 t = this.localToGlobalTransformation(level, pos, state);
            var voxelized = model.getVoxelized();
            return t.t(voxelized);
        }
        return Shapes.empty();
    }

    // endregion

    public float getBlockHardness(BlockState state, BlockAndTintGetter level, BlockPos pos, float hardness) {
        return hardness;
    }

    public boolean hasBlockEntity(BlockState state) {
        return false;
    }

    public boolean is(BlockState state, TagKey<Block> tag) {
        return this.builtInRegistryHolder().is(tag);
    }

    public boolean is(BlockState state, TagKey<Block> tag, Predicate<BlockStateBase> predicate) {
        return this.is(state, tag) && predicate.test(state);
    }

    public boolean is(BlockState state, HolderSet<Block> holderSet) {
        return holderSet.contains(this.builtInRegistryHolder());
    }

    public int getTransIdentity(BlockState state, BlockGetter level, BlockPos pos, LegacyVector3 origin) {
        return state.hashCode() * 31 + origin.hashCode();
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return null;
    }

    public interface IOrientationHandler {

        void defineProperties(BlockArchitecture block);

        BlockState onBlockPlaced(Block block, Level level, BlockPos pos, Direction side,
                                 double hitX, double hitY, double hitZ, BlockState baseState, LivingEntity placer);

        Trans3 localToGlobalTransformation(BlockGetter level, BlockPos pos, BlockState state, LegacyVector3 origin);
    }

    public static class Orient1Way implements IOrientationHandler {

        public void defineProperties(BlockArchitecture block) {
        }

        public BlockState onBlockPlaced(Block block, Level level, BlockPos pos, Direction side,
                                        double hitX, double hitY, double hitZ, BlockState baseState, LivingEntity placer) {
            return baseState;
        }

        public Trans3 localToGlobalTransformation(BlockGetter level, BlockPos pos, BlockState state, LegacyVector3 origin) {
            return new Trans3(origin);
        }
    }

}
