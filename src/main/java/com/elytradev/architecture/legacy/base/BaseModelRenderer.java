//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.10 - Render block using model + textures
//
//------------------------------------------------------------------------------------------------

package com.elytradev.architecture.legacy.base;

import com.elytradev.architecture.client.render.target.RenderTargetBase;
import com.elytradev.architecture.client.render.texture.ITexture;
import com.elytradev.architecture.common.block.BlockArchitecture;
import com.elytradev.architecture.legacy.base.BaseModClient.ICustomRenderer;
import com.elytradev.architecture.legacy.base.BaseModClient.IModel;
import com.elytradev.architecture.legacy.common.helpers.Trans3;
import com.elytradev.architecture.legacy.common.helpers.Vector3;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class BaseModelRenderer implements ICustomRenderer {

    protected IModel model;
    protected ITexture[] textures;
    protected Vector3 origin;

//     private static Trans3 itemTrans = Trans3.blockCenterSideTurn(0, 2);

    public BaseModelRenderer(IModel model, ITexture... textures) {
        this(model, Vector3.zero, textures);
    }

    public BaseModelRenderer(IModel model, Vector3 origin, ITexture... textures) {
        this.model = model;
        this.textures = textures;
        this.origin = origin;
    }

    @Override
    public void renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, RenderTargetBase target,
                            BlockRenderLayer layer, Trans3 t) {
        BlockArchitecture block = (BlockArchitecture) state.getBlock();
        Trans3 t2 = t.t(block.localToGlobalTransformation(world, pos, state, Vector3.zero)).translate(origin);
        model.render(t2, target, textures);
    }

    @Override
    public void renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, RenderTargetBase target, BlockRenderLayer layer, Trans3 t, boolean renderPrimary, boolean renderSecondary) {
        if (renderPrimary) renderBlock(world, pos, state, target, layer, t);
    }

    @Override
    public void renderItemStack(ItemStack stack, RenderTargetBase target, Trans3 t) {
        Item item = stack.getItem();
        if (item instanceof ItemBlock) {
            Block block = Block.getBlockFromItem(item);
            if (block instanceof BlockArchitecture)
                t = t.t(((BlockArchitecture) block).itemTransformation());
        }
        model.render(t.translate(origin), target, textures);
    }

}

