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

package com.tridevmc.architecture.legacy.common.block;

import com.tridevmc.architecture.client.debug.ArchitectureDebugEventListeners;
import com.tridevmc.architecture.client.ui.UISawbench;
import com.tridevmc.architecture.common.block.container.ContainerSawbench;
import com.tridevmc.architecture.common.model.ModelSpec;
import com.tridevmc.architecture.common.ui.ArchitectureUIHooks;
import com.tridevmc.architecture.common.ui.CreateMenuContext;
import com.tridevmc.architecture.common.ui.IElementProvider;
import com.tridevmc.architecture.legacy.base.LegacyBaseOrientation;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;

@Deprecated
public class LegacyBlockSawbench extends LegacyBlockArchitecture implements IElementProvider<ContainerSawbench> {

    static String model = "block/sawbench_all.objson";
    static ModelSpec modelSpec = new ModelSpec(model);

    public LegacyBlockSawbench() {
        super(Material.WOOD);
    }

    @Override
    public float getBlockHardness(BlockState blockState, BlockAndTintGetter level, BlockPos pos, float hardness) {
        return 2.0F;
    }

    @Override
    public IOrientationHandler getOrientationHandler() {
        return LegacyBaseOrientation.orient4WaysByState;
    }

    @Override
    public ModelSpec getModelSpec(BlockState state) {
        return modelSpec;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isCrouching()) {
            if (!level.isClientSide()) {
                ArchitectureUIHooks.openGui((ServerPlayer) player, this, pos);
            }
            return InteractionResult.SUCCESS;
        } else {
            return ArchitectureDebugEventListeners.onVoxelizedBlockClicked(level, pos, player, hit, this.getModelSpec(state));
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

    @Override
    public boolean propagatesSkylightDown(BlockState p_49928_, BlockGetter p_49929_, BlockPos p_49930_) {
        return false;
    }

}
