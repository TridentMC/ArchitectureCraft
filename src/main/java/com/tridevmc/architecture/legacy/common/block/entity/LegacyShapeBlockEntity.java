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

package com.tridevmc.architecture.legacy.common.block.entity;

import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.legacy.common.block.LegacyBlockArchitecture;
import com.tridevmc.architecture.legacy.common.block.LegacyBlockHelper;
import com.tridevmc.architecture.legacy.common.block.LegacyBlockShape;
import com.tridevmc.architecture.legacy.math.LegacyTrans3;
import com.tridevmc.architecture.common.helpers.Utils;
import com.tridevmc.architecture.legacy.math.LegacyVector3;
import com.tridevmc.architecture.common.item.ItemCladding;
import com.tridevmc.architecture.common.model.ModelProperties;
import com.tridevmc.architecture.legacy.common.shape.LegacyEnumShape;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;


@Deprecated
public class LegacyShapeBlockEntity extends BlockEntity {
    private LegacyBlockShape block;
    private BlockState baseBlockState;
    private BlockState secondaryBlockState;
    private int disabledConnections;
    private byte offsetX;
    private byte side;
    private byte turn;

    public LegacyShapeBlockEntity(BlockPos pos, BlockState state) {
        super(ArchitectureMod.CONTENT.tileTypeShape, pos, state);
        this.secondaryBlockState = Blocks.AIR.defaultBlockState();
    }

    public LegacyShapeBlockEntity(BlockPos pos, BlockState state, LegacyBlockShape block) {
        this(pos, state);
        this.block = block;
        this.baseBlockState = Blocks.OAK_PLANKS.defaultBlockState();
        this.secondaryBlockState = Blocks.AIR.defaultBlockState();
    }

    public static LegacyShapeBlockEntity get(BlockGetter level, BlockPos pos) {
        if (level != null) {
            var te = level.getBlockEntity(pos);
            if (te instanceof LegacyShapeBlockEntity)
                return (LegacyShapeBlockEntity) te;
        }
        return null;
    }

    public double getOffsetX() {
        return this.offsetX * (1 / 16.0);
    }

    public void setOffsetX(double value) {
        this.offsetX = (byte) (16 * value);
    }

    public boolean connectionIsEnabledGlobal(Direction dir) {
        return (this.disabledConnections & (1 << dir.ordinal())) == 0;
    }

    public void setConnectionEnabledGlobal(Direction dir, boolean state) {
        int bit = 1 << dir.ordinal();
        if (state)
            this.disabledConnections &= ~bit;
        else
            this.disabledConnections |= bit;
    }

    public void toggleConnectionGlobal(Direction dir) {
        boolean newState = !this.connectionIsEnabledGlobal(dir);
        this.setConnectionEnabledGlobal(dir, newState);
        LegacyShapeBlockEntity nte = this.getNeighbourGlobal(dir);
        if (nte != null)
            nte.setConnectionEnabledGlobal(dir.getOpposite(), newState);
    }

    @Override
    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.readShapeFromNBT(tag);
        this.side = tag.getByte("Side");
        this.turn = tag.getByte("Turn");
        this.offsetX = tag.getByte("OffsetX");
    }

    protected void readShapeFromNBT(CompoundTag nbt) {
        this.baseBlockState = Block.stateById(nbt.getInt("BaseBlockState"));
        this.secondaryBlockState = Block.stateById(nbt.getInt("SecondaryBlockState"));
        if (this.baseBlockState == null)
            this.baseBlockState = Blocks.OAK_PLANKS.defaultBlockState();
        this.disabledConnections = nbt.getInt("Disconnected");
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        this.writeShapeToNBT(tag);

        if (this.offsetX != 0) {
            tag.putByte("OffsetX", this.offsetX);
        }
        if (this.side != 0) {
            tag.putByte("Side", (byte) this.getSide());
        }
        if (this.turn != 0) {
            tag.putByte("Turn", (byte) this.getTurn());
        }
    }

    protected void writeShapeToNBT(CompoundTag nbt) {
        if (this.getArchitectureShape() != null) {
            nbt.putInt("BaseBlockState", Block.getId(this.baseBlockState));
            nbt.putInt("SecondaryBlockState", Block.getId(this.secondaryBlockState));
        }
        if (this.disabledConnections != 0) {
            nbt.putInt("Disconnected", this.disabledConnections);
        }
    }

    public void onChiselUse(Player player, Direction face, float hitX, float hitY, float hitZ) {
        this.getArchitectureShape().behaviour.onChiselUse(this, player, face, this.hitVec(hitX, hitY, hitZ));
    }

    public void onHammerUse(Player player, Direction face, float hitX, float hitY, float hitZ) {
        this.getArchitectureShape().behaviour.onHammerUse(this, player, face, this.hitVec(hitX, hitY, hitZ));
    }

    protected LegacyVector3 hitVec(float hitX, float hitY, float hitZ) {
        return new LegacyVector3(hitX - 0.5, hitY - 0.5, hitZ - 0.5);
    }

    public Direction globalFace(Direction face) {
        return this.localToGlobalRotation().t(face);
    }

    public Direction localFace(Direction face) {
        return this.localToGlobalRotation().it(face);
    }

    public boolean applySecondaryMaterial(ItemStack stack, Player player) {
        BlockState materialState = null;
        Item item = stack.getItem();
        LegacyEnumShape architectureShape = this.getArchitectureShape();
        if (item instanceof ItemCladding && architectureShape.behaviour.acceptsCladding()) {
            materialState = ((ItemCladding) item).blockStateFromStack(stack);
        } else {
            Block block = Block.byItem(item);
            BlockState state = block.defaultBlockState();
            if (architectureShape.behaviour.isValidSecondaryMaterial(state)) {
                materialState = state;
            }
        }
        if (materialState != null) {
            if (!this.hasSecondaryMaterial()) {
                this.setSecondaryMaterial(materialState);
                if (!Utils.playerIsInCreativeMode(player))
                    stack.shrink(1);
            }
            return true;
        } else {
            return false;
        }
    }

    public void setBaseBlockState(BlockState state) {
        this.baseBlockState = state;
    }

    public void setSecondaryMaterial(BlockState state) {
        this.secondaryBlockState = state;
    }

    public boolean hasSecondaryMaterial() {
        return this.getSecondaryBlockState().getBlock() != Blocks.AIR;
    }

    public BlockState getBaseBlockState() {
        return this.baseBlockState;
    }

    public BlockState getSecondaryBlockState() {
        return this.secondaryBlockState;
    }

    public LegacyEnumShape getArchitectureShape() {
        return this.getBlock().getArchitectureShape();
    }

    public int getDisabledConnections() {
        return this.disabledConnections;
    }

    public int getSide() {
        return this.side;
    }

    public int getTurn() {
        return this.turn;
    }

    public void setSide(int side) {
        this.setSide((byte) side);
    }

    public void setSide(byte side) {
        this.side = side;
    }

    public void setTurn(int turn) {
        this.setTurn((byte) turn);
    }

    public void setTurn(byte turn) {
        this.turn = turn;
    }

    public boolean canRenderInLayer(BlockState state, RenderType layer) {
        if (LegacyBlockHelper.blockCanRenderInLayer(this.baseBlockState, layer))
            return true;
        if (this.secondaryBlockState != null)
            return LegacyBlockHelper.blockCanRenderInLayer(this.secondaryBlockState, layer);
        return false;
    }

    public LegacyShapeBlockEntity getNeighbourGlobal(Direction dir) {
        return LegacyShapeBlockEntity.get(this.getLevel(), this.getBlockPos().relative(dir));
    }

    public LegacyShapeBlockEntity getConnectedNeighbourGlobal(Direction dir) {
        if (this.level != null) {
            if (this.connectionIsEnabledGlobal(dir)) {
                LegacyShapeBlockEntity nte = this.getNeighbourGlobal(dir);
                if (nte != null && nte.connectionIsEnabledGlobal(dir.getOpposite()))
                    return nte;
            }
        }
        return null;
    }

    public LegacyTrans3 localToGlobalRotation() {
        return this.localToGlobalTransformation(LegacyVector3.ZERO);
    }

    public LegacyTrans3 localToGlobalTransformation(LegacyVector3 origin) {
        BlockState state = this.level.getBlockState(this.worldPosition);
        Block block = state.getBlock();
        if (block instanceof LegacyBlockArchitecture)
            return ((LegacyBlockArchitecture) block).localToGlobalTransformation(this.getLevel(), this.getBlockPos(), state, origin).translate(this.getOffsetX(), 0, 0);
        else {
            return new LegacyTrans3(origin).translate(this.getOffsetX(), 0, 0);
        }
    }

    @Nonnull
    @Override
    public ModelData getModelData() {
        var builder = ModelData.builder();
        builder.with(ModelProperties.LEVEL, this.getLevel());
        builder.with(ModelProperties.POS, this.getBlockPos());
        builder.with(ModelProperties.TILE, this);
        return builder.build();
    }

    public LegacyBlockShape getBlock() {
        if (this.block == null) {
            this.block = this.level != null ? (LegacyBlockShape) this.level.getBlockState(this.worldPosition).getBlock() : null;
        }
        return this.block;
    }

}
