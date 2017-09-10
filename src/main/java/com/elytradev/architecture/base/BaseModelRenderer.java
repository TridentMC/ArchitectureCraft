//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.10 - Render block using model + textures
//
//------------------------------------------------------------------------------------------------

package com.elytradev.architecture.base;

import com.elytradev.architecture.base.BaseMod.IBlock;
import com.elytradev.architecture.base.BaseModClient.ICustomRenderer;
import com.elytradev.architecture.base.BaseModClient.IModel;
import com.elytradev.architecture.base.BaseModClient.IRenderTarget;
import com.elytradev.architecture.base.BaseModClient.ITexture;
import com.elytradev.architecture.common.helpers.Trans3;
import com.elytradev.architecture.common.helpers.Vector3;
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

    public void renderBlock(IBlockAccess world, BlockPos pos, IBlockState state, IRenderTarget target,
                            BlockRenderLayer layer, Trans3 t) {
        IBlock block = (IBlock) state.getBlock();
        Trans3 t2 = t.t(block.localToGlobalTransformation(world, pos, state, Vector3.zero)).translate(origin);
        model.render(t2, target, textures);
    }

    public void renderItemStack(ItemStack stack, IRenderTarget target, Trans3 t) {
        Item item = stack.getItem();
        if (item instanceof ItemBlock) {
            Block block = Block.getBlockFromItem(item);
            if (block instanceof IBlock)
                t = t.t(((IBlock) block).itemTransformation());
        }
        model.render(t.translate(origin), target, textures);
    }

}

