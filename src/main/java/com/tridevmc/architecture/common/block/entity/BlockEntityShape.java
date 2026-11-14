package com.tridevmc.architecture.common.block.entity;

import com.tridevmc.architecture.common.ArchitectureMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.model.data.ModelData;
import net.neoforged.neoforge.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BlockEntityShape extends BlockEntityArchitecture {

    public static final ModelProperty<MaterialStates> MATERIALS = new ModelProperty<>();

    public record MaterialStates(BlockState base, BlockState secondary) {
    }

    private BlockState baseMaterialState, secondaryMaterialState;

    public BlockEntityShape(BlockPos pos, BlockState state) {
        super(ArchitectureMod.CONTENT.blockEntityTypeShape, pos, state);
    }

    /**
     * Gets the BlockEntityShape at the given position, if it exists. Returns an empty optional if it does not.
     *
     * @param world The world to get the BlockEntityShape from.
     * @param pos   The position to get the BlockEntityShape from.
     * @return An optional containing the BlockEntityShape, or an empty optional if it does not exist.
     */
    public static Optional<BlockEntityShape> getAtOptionally(BlockGetter world, BlockPos pos) {
        return Optional.ofNullable(world.getBlockEntity(pos)).map(te -> te instanceof BlockEntityShape ? (BlockEntityShape) te : null);
    }

    /**
     * Gets the BlockEntityShape at the given position, if it exists. Returns null if it does not.
     *
     * @param world The world to get the BlockEntityShape from.
     * @param pos   The position to get the BlockEntityShape from.
     * @return The BlockEntityShape, or null if it does not exist.
     */
    @Nullable
    public static BlockEntityShape getAt(BlockGetter world, BlockPos pos) {
        var te = world.getBlockEntity(pos);
        if (te instanceof BlockEntityShape) {
            return (BlockEntityShape) te;
        }
        return null;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("BaseMaterialState", Block.getId(this.getEffectiveBaseMaterialState()));
        output.putInt("SecondaryMaterialState", this.getSecondaryMaterialState().map(Block::getId).orElse(-1));
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.baseMaterialState = Block.stateById(input.getIntOr("BaseMaterialState", 0));
        var secondaryMaterialId = input.getIntOr("SecondaryMaterialState", 0);
        this.secondaryMaterialState = secondaryMaterialId == -1 ? null : Block.stateById(secondaryMaterialId);
        this.requestModelDataUpdate();
        if (this.level != null && this.level.isClientSide()) {
            this.notifyBlockUpdate();
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveCustomOnly(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public ModelData getModelData() {
        return ModelData.of(MATERIALS, new MaterialStates(
                this.getEffectiveBaseMaterialState(), this.getEffectiveSecondaryMaterialState()));
    }

    private void materialsChanged() {
        this.setChanged();
        this.requestModelDataUpdate();
        this.notifyBlockUpdate();
    }

    private void notifyBlockUpdate() {
        if (this.level != null) {
            var state = this.getBlockState();
            this.level.sendBlockUpdated(this.getBlockPos(), state, state, Block.UPDATE_CLIENTS);
        }
    }

    /**
     * Gets the base material state of this shape wrapped in an optional for additional safety.
     *
     * @return The base material state of this shape.
     */
    public Optional<BlockState> getBaseMaterialState() {
        return Optional.ofNullable(this.baseMaterialState);
    }

    /**
     * Sets the base material state of this shape.
     *
     * @param baseMaterialState The base material state to set.
     * @return This BlockEntityShape instance.
     */
    public BlockEntityShape setBaseMaterialState(BlockState baseMaterialState) {
        if (this.baseMaterialState != baseMaterialState) {
            this.baseMaterialState = baseMaterialState;
            this.materialsChanged();
        }
        return this;
    }

    /**
     * Gets the secondary material state of this shape wrapped in an optional for additional safety, if the secondary material state is not set then the base material should be used.
     *
     * @return The secondary material state of this shape.
     */
    public Optional<BlockState> getSecondaryMaterialState() {
        return Optional.ofNullable(this.secondaryMaterialState);
    }

    /**
     * Sets the secondary material state of this shape.
     *
     * @param secondaryMaterialState The secondary material state to set.
     * @return This BlockEntityShape instance.
     */
    public BlockEntityShape setSecondaryMaterialState(BlockState secondaryMaterialState) {
        if (this.secondaryMaterialState != secondaryMaterialState) {
            this.secondaryMaterialState = secondaryMaterialState;
            this.materialsChanged();
        }
        return this;
    }

    /**
     * Gets the effective base material state of this shape, if the base material state is not set then oak planks should be used to prevent crashes.
     *
     * @return The effective base material state of this shape.
     */
    public BlockState getEffectiveBaseMaterialState() {
        return this.baseMaterialState == null ? Blocks.OAK_PLANKS.defaultBlockState() : this.baseMaterialState;
    }

    /**
     * Gets the effective secondary material state of this shape, if the secondary material state is not set then the effective base material state should be used.
     *
     * @return The effective secondary material state of this shape.
     */
    public BlockState getEffectiveSecondaryMaterialState() {
        return this.secondaryMaterialState == null ? this.getEffectiveBaseMaterialState() : this.secondaryMaterialState;
    }

    /**
     * Gets the material state for the given index, if the index is 0 then the base material state should be used, otherwise the secondary material state should be used.
     *
     * @param i The index to get the material state for.
     * @return The material state for the given index.
     */
    public BlockState getMaterialStateForIndex(int i) {
        return i == 0 ? this.getEffectiveBaseMaterialState() : this.getEffectiveSecondaryMaterialState();
    }
}
