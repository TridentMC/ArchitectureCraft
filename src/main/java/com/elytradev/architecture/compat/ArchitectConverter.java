package com.elytradev.architecture.compat;

import com.elytradev.architecture.common.ArchitectureMod;
import com.elytradev.architecture.common.tile.TileShape;
import li.cil.architect.api.ConverterAPI;
import li.cil.architect.api.converter.Converter;
import li.cil.architect.api.converter.MaterialSource;
import li.cil.architect.api.converter.SortIndex;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;

import java.util.Collections;
import java.util.UUID;

/**
 * Architect integration. Ensures that the right item is pulled from the player's inventory and that
 * rotations are applied correctly.
 *
 * @author Stan Hebben
 */
public class ArchitectConverter implements Converter {
	private static final ArchitectConverter INSTANCE = new ArchitectConverter();

	// never change this (if you do, it will break existing Architect blueprints)
	private static final UUID CONVERTER_UUID = UUID.fromString("182148b6-fba8-4acb-95a5-66409d34eb59");

	private Item shapeItem;
	private Block shapeBlock;

	public static void init() {
		ConverterAPI.addConverter(INSTANCE);
		INSTANCE.initInstance();
	}

	private ArchitectConverter() {}

	private void initInstance() {
		shapeBlock = ArchitectureMod.CONTENT.blockShape;
		shapeItem = Item.getItemFromBlock(shapeBlock);
	}

	@Override
	public UUID getUUID() {
		return CONVERTER_UUID;
	}

	@Override
	public Iterable<ItemStack> getItemCosts(NBTBase nbtBase) {
		NBTTagCompound data = (NBTTagCompound)nbtBase;
		return Collections.singletonList(getItemStackForBlock(data));
	}

	@Override
	public Iterable<FluidStack> getFluidCosts(NBTBase nbtBase) {
		return Collections.emptyList();
	}

	@Override
	public int getSortIndex(NBTBase nbtBase) {
		return SortIndex.SOLID_BLOCK;
	}

	@Override
	public boolean canSerialize(World world, BlockPos blockPos) {
		return world.getBlockState(blockPos).getBlock() == shapeBlock;
	}

	@Override
	public NBTBase serialize(World world, BlockPos blockPos) {
		IBlockState state = world.getBlockState(blockPos);
		if (state.getBlock() != shapeBlock)
			return null;

		TileEntity tileEntity = world.getTileEntity(blockPos);
		if (tileEntity == null)
			return null;

		NBTTagCompound original = tileEntity.serializeNBT();
		NBTTagCompound data = new NBTTagCompound();
		if (original.getByte("turn") != 0)
			data.setByte("turn", original.getByte("turn"));
		if (original.getByte("side") != 0)
			data.setByte("side", original.getByte("side"));

		data.setInteger("Shape", original.getInteger("Shape"));
		data.setString("BaseName", original.getString("BaseName"));
		data.setInteger("BaseData", original.getInteger("BaseData"));

		// accents: maybe later...
		/*if (original.hasKey("Name2")) {
			data.setString("Name2", original.getString("Name2"));
			data.setInteger("Data2", original.getInteger("Data2"));
		}*/

		return data;
	}

	@Override
	public boolean preDeserialize(MaterialSource materialSource, World world, BlockPos blockPos, Rotation rotation, NBTBase nbtBase) {
		if (materialSource.isCreative())
			return true;

		IItemHandler itemHandler = materialSource.getItemHandler();
		NBTTagCompound data = (NBTTagCompound)nbtBase;
		return consume(itemHandler, data);
	}

	private boolean consume(IItemHandler itemHandler, NBTTagCompound data) {
		int shape = data.getInteger("Shape");
		String name = data.getString("BaseName");
		int meta = data.getInteger("BaseData");

		for (int i = 0; i < itemHandler.getSlots(); i++) {
			ItemStack stack = itemHandler.getStackInSlot(i);
			if (stack.getCount() == 0)
				continue;
			if (stack.getItem() != shapeItem)
				continue;

			NBTTagCompound stackData = stack.getTagCompound();
			if (stackData == null)
				continue;
			if (stackData.getInteger("Shape") != shape)
				continue;
			if (!stackData.getString("BaseName").equals(name))
				continue;
			if (stackData.getInteger("BaseData") != meta)
				continue;

			itemHandler.extractItem(i, 1, false);
			return true;
		}

		return false;
	}

	@Override
	public void deserialize(World world, BlockPos blockPos, Rotation rotation, NBTBase nbtBase) {
		NBTTagCompound data = (NBTTagCompound)nbtBase;
		rotate(data, rotation);

		world.setBlockState(blockPos, shapeBlock.getStateFromMeta(data.getInteger("BaseData")));
		TileShape shape = new TileShape();
		shape.readFromNBT(data);
		world.setTileEntity(blockPos, shape);
	}

	@Override
	public void cancelDeserialization(World world, BlockPos blockPos, Rotation rotation, NBTBase nbtBase) {
		world.setBlockToAir(blockPos);
	}

	private void rotate(NBTTagCompound data, Rotation rotation) {
		byte turn = data.getByte("turn");
		byte side = data.getByte("side");
		if (side == 0)
			turn = (byte)((turn - rotation.ordinal() + 4) % 4);
		else
			turn = (byte)((turn + rotation.ordinal() + 4) % 4);

		data.setByte("turn", turn);
	}

	private ItemStack getItemStackForBlock(NBTTagCompound data) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("Shape", data.getInteger("Shape"));
		nbt.setString("BaseName", data.getString("BaseName"));
		nbt.setInteger("BaseData", data.getInteger("BaseData"));
		return new ItemStack(shapeItem, 1, 0, nbt);
	}
}
