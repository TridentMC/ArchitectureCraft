package com.tridevmc.architecture.common.block;

import com.google.common.collect.ImmutableList;
import com.tridevmc.architecture.client.debug.ArchitectureDebugEventListeners;
import com.tridevmc.architecture.client.ui.UISawbench;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.block.container.ContainerSawbench;
import com.tridevmc.architecture.common.block.state.BlockStateArchitecture;
import com.tridevmc.architecture.common.ui.ArchitectureUIHooks;
import com.tridevmc.architecture.common.ui.CreateMenuContext;
import com.tridevmc.architecture.common.ui.IElementProvider;
import com.tridevmc.architecture.core.math.IMatrix4Immutable;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.math.ITrans3Immutable;
import com.tridevmc.architecture.core.model.objson.OBJSON;
import com.tridevmc.architecture.core.physics.AABB;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BlockSawbench extends BlockArchitecture implements IElementProvider<ContainerSawbench> {

    public static final OBJSON MODEL = OBJSON.fromResource(new ResourceLocation(ArchitectureMod.MOD_ID, "block/sawbench_all.objson"));
    private final static DirectionProperty FACING = DirectionProperty.create("facing", Direction.Plane.HORIZONTAL);

    public BlockSawbench() {
        super(Properties.of());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public ITrans3Immutable getTransformForState(BlockStateArchitecture state) {
        var facing = state.getValue(FACING);
        // The sawbench model faces south by default, this isn't standard, so we handle the rotation with a switch.
        var degrees = switch (facing) {
            case NORTH -> 180;
            case EAST -> 90;
            case WEST -> 270;
            default -> 0;
        };
        return ITrans3.ofImmutable(IMatrix4Immutable.ofRotationXYZ(0.5, 0.5, 0.5, 0, degrees, 0));
    }

    @Override
    public ImmutableList<AABB> getBoxesForState(BlockStateArchitecture state) {
        // The results of this are cached on the state object, so don't worry too much about performance.
        var transform = this.getTransformForState(state);
        return MODEL.voxelizer().voxelize().stream().map(transform::transformAABB).collect(ImmutableList.toImmutableList());
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    @NotNull
    public InteractionResult use(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (!player.isCrouching()) {
            if (!level.isClientSide()) {
                ArchitectureUIHooks.openGui((ServerPlayer) player, this, pos);
            }
            return InteractionResult.SUCCESS;
        } else {
            return ArchitectureDebugEventListeners.onVoxelizedBlockClicked(level, pos, player, hit, MODEL.voxelizer());
        }
    }

    @Override
    public Screen createScreen(ContainerSawbench container, Player player) {
        return new UISawbench(container, player);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(CreateMenuContext context) {
        return new ContainerSawbench(context.getPlayerInventory(), context.getWindowId());
    }


}
