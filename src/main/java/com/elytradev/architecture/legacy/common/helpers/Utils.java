//------------------------------------------------------
//
//   ArchitectureCraft - Utilities
//
//------------------------------------------------------

package com.elytradev.architecture.legacy.common.helpers;

import com.elytradev.architecture.common.tile.TileShape;
import com.elytradev.architecture.common.block.BlockShape;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;

import java.util.List;
import java.util.Random;

import static java.lang.Math.*;

public class Utils {

    public static Random random = new Random();

//	public static void dumpInventoryIntoWorld(World world, int x, int y, int z) {
//		// Based on BlockChest.breakBlock()
//		IInventory te = (IInventory)world.getTileEntity(x, y, z);
//		if (te != null) {
//			for (int i = 0; i < te.getSizeInventory(); ++i) {
//				ItemStack stack = te.getStackInSlot(i);
//				if (stack != null) {
//					float dx = random.nextFloat() * 0.8F + 0.1F;
//					float dy = random.nextFloat() * 0.8F + 0.1F;
//					float dz = random.nextFloat() * 0.8F + 0.1F;
//					while (stack.stackSize > 0) {
//						int n = random.nextInt(21) + 10;
//						if (n > stack.stackSize) {
//							n = stack.stackSize;
//						}
//						stack.stackSize -= n;
//						EntityItem entity = new EntityItem(world, x + dx, y + dy, z + dz,
//							new ItemStack(stack.getItem(), n, stack.getItemDamage()));
//						if (stack.hasTagCompound()) {
//							entity.getEntityItem().setTagCompound((NBTTagCompound)stack.getTagCompound().copy());
//						}
//						float f = 0.05F;
//						entity.motionX = random.nextGaussian() * f;
//						entity.motionY = random.nextGaussian() * f + 0.2F;
//						entity.motionZ = random.nextGaussian() * f;
//						world.spawnEntityInWorld(entity);
//					}
//				}
//			}
//		}
//	}

    public static int playerTurn(EntityLivingBase player) {
        return MathHelper.floor((player.rotationYaw * 4.0 / 360.0) + 0.5) & 3;
    }

    public static int lookTurn(Vector3 look) {
        double a = atan2(look.x, look.z);
        return (int) round(a * 2 / PI) & 3;
    }

    public static boolean playerIsInCreativeMode(EntityPlayer player) {
        return (player instanceof EntityPlayerMP)
                && ((EntityPlayerMP) player).interactionManager.isCreative();
    }

    public static TextureAtlasSprite getSpriteForBlockState(IBlockState state) {
        if (state != null)
            return Minecraft.getMinecraft().getBlockRendererDispatcher()
                    .getBlockModelShapes().getTexture(state);
        else
            return null;
    }

    public static TextureAtlasSprite getSpriteForPos(IBlockAccess world, BlockPos pos, boolean renderPrimary) {
        IBlockState blockState = world.getBlockState(pos);

        if (blockState == null)
            return null;

        if (blockState.getBlock() instanceof BlockShape) {
            TileShape shape = TileShape.get(world, pos);

            if (renderPrimary) {
                return getSpriteForBlockState(shape.baseBlockState);
            } else {
                return getSpriteForBlockState(shape.secondaryBlockState);
            }
        } else if (!renderPrimary) {
            return null;
        }

        return getSpriteForBlockState(blockState);
    }

    public static String displayNameOfBlock(Block block, int meta) {
        String name = null;
        Item item = Item.getItemFromBlock(block);
        if (item != null) {
            ItemStack stack = new ItemStack(item, 1, meta);
            name = stack.getDisplayName();
        }
        if (name == null)
            name = block.getLocalizedName();
        return "Cut from " + name;
    }

    public static AxisAlignedBB unionOfBoxes(List<AxisAlignedBB> list) {
        AxisAlignedBB box = list.get(0);
        int n = list.size();
        for (int i = 1; i < n; i++)
            box = box.union(list.get(i));
        return box;
    }
}
