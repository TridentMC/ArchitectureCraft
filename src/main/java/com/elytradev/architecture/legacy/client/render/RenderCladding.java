//------------------------------------------------------------------------------
//
//   ArchitectureCraft - Cladding Item Renderer
//
//------------------------------------------------------------------------------

package com.elytradev.architecture.legacy.client.render;

import com.elytradev.architecture.client.render.target.RenderTargetBase;
import com.elytradev.architecture.legacy.base.BaseModClient.ICustomRenderer;
import com.elytradev.architecture.legacy.base.BaseModClient.IModel;
import com.elytradev.architecture.client.render.texture.ITexture;
import com.elytradev.architecture.client.render.texture.TextureBase;
import com.elytradev.architecture.legacy.common.ArchitectureCraft;
import com.elytradev.architecture.legacy.common.helpers.Trans3;
import com.elytradev.architecture.legacy.common.helpers.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class RenderCladding implements ICustomRenderer {

    @Override
    public void renderBlock(IBlockAccess world, BlockPos pos, IBlockState state,
                            RenderTargetBase target, BlockRenderLayer layer, Trans3 t) {
    }

    @Override
    public void renderItemStack(ItemStack stack, RenderTargetBase target, Trans3 t) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null) {
            String blockName = nbt.getString("block");
            int meta = stack.getMetadata();
            Block block = Block.getBlockFromName(blockName);
            if (block != null) {
                IBlockState state = block.getStateFromMeta(meta);
                if (state != null) {
                    TextureAtlasSprite sprite = Utils.getSpriteForBlockState(state);
                    if (sprite != null) {
                        ITexture texture = TextureBase.fromSprite(sprite);
                        IModel model = ArchitectureCraft.mod.client.getModel("shape/cladding.smeg");
                        model.render(t, target, texture);
                    }
                }
            }
        }
    }

}

