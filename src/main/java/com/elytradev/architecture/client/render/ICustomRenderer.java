package com.elytradev.architecture.client.render;

import com.elytradev.architecture.client.render.target.RenderTargetBase;
import com.elytradev.architecture.legacy.common.helpers.Trans3;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public interface ICustomRenderer {
    void renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, RenderTargetBase target,
                     BlockRenderLayer layer, Trans3 t);

    void renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, RenderTargetBase target,
                     BlockRenderLayer layer, Trans3 t, boolean renderPrimary, boolean renderSecondary);

    void renderItemStack(ItemStack stack, RenderTargetBase target, Trans3 t);
}
