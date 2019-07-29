package com.elytradev.architecture.compat.architect;

import com.elytradev.architecture.common.ArchitectureMod;
import com.elytradev.architecture.common.shape.Shape;
import com.elytradev.architecture.common.shape.ShapeKind;
import com.elytradev.architecture.common.tile.TileShape;
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
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import scala.actors.threadpool.Arrays;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Architect integration. Ensures that the right item is pulled from the player's inventory and that
 * rotations are applied correctly.
 */
public class ArchitectConverter implements Converter {
    // never change this (if you do, it will break existing Architect blueprints)
    private static final UUID CONVERTER_UUID = UUID.fromString("182148b6-fba8-4acb-95a5-66409d34eb59");

    // precalculated array for quick & easy lookup of rotations. Contains 4 rotations x 64 possible values for Disconnected
    private static final int[] ROTATE_DISCONNECTED;

    static {
        ROTATE_DISCONNECTED = new int[64 * 4];

        for (int i = 0; i < 64; i++) {
            for (int rotation = 0; rotation < 4; rotation++) {
                int result = i & 3; // keep up + down bits
                for (EnumFacing facing : EnumFacing.HORIZONTALS) {
                    // permutate bits according to the given rotation
                    int bit = 1 << facing.ordinal();
                    if ((i & bit) > 0) {
                        EnumFacing rotated = EnumFacing.byHorizontalIndex((facing.getHorizontalIndex() + rotation) & 3);
                        result |= (1 << rotated.ordinal());
                    }
                }
                ROTATE_DISCONNECTED[i + 64 * rotation] = result;
            }
        }
    }

    private final Item shapeItem;
    private final Item claddingItem;
    private final Block shapeBlock;

    ArchitectConverter() {
        this.shapeBlock = ArchitectureMod.CONTENT.blockShape;
        this.shapeItem = Item.getItemFromBlock(this.shapeBlock);
        this.claddingItem = ArchitectureMod.CONTENT.itemCladding;
    }

    private static boolean consume(IItemHandler itemHandler, Item item, int amount, int meta, Predicate<NBTTagCompound> test) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (stack.getCount() == 0)
                continue;
            if (stack.getItem() != item || stack.getMetadata() != meta)
                continue;
            if (test != null) {
                NBTTagCompound stackData = stack.getTagCompound();
                if (stackData == null)
                    continue;
                if (!test.test(stackData))
                    continue;
            }

            amount -= itemHandler.extractItem(i, amount, false).getCount();
            if (amount == 0)
                return true;
        }

        return false;
    }

    @Override
    public UUID getUUID() {
        return CONVERTER_UUID;
    }

    @Override
    public Iterable<ItemStack> getItemCosts(NBTBase nbtBase) {
        NBTTagCompound data = (NBTTagCompound) nbtBase;
        if (data.hasKey("Name2")) {
            return Arrays.asList(new ItemStack[]{
                    this.getItemStackForBlock(data),
                    this.getCladdingForBlock(data)
            });
        } else {
            return Collections.singletonList(this.getItemStackForBlock(data));
        }
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
        return world.getBlockState(blockPos).getBlock() == this.shapeBlock;
    }

    @Override
    public NBTBase serialize(World world, BlockPos blockPos) {
        IBlockState state = world.getBlockState(blockPos);
        if (state.getBlock() != this.shapeBlock)
            return null;

        TileEntity tileEntity = world.getTileEntity(blockPos);
        if (tileEntity == null)
            return null;

        NBTTagCompound original = tileEntity.serializeNBT();
        NBTTagCompound data = new NBTTagCompound();
        byte turn = original.getByte("turn");
        byte side = original.getByte("side");
        int disconnected = original.getInteger("Disconnected");
        if (turn != 0)
            data.setByte("turn", turn);
        if (side != 0)
            data.setByte("side", side);
        if (disconnected != 0)
            data.setInteger("Disconnected", disconnected);

        data.setInteger("Shape", original.getInteger("Shape"));
        data.setString("BaseName", original.getString("BaseName"));
        data.setInteger("BaseData", original.getInteger("BaseData"));

        if (original.hasKey("Name2")) {
            data.setString("Name2", original.getString("Name2"));
            data.setInteger("Data2", original.getInteger("Data2"));
        }

        return data;
    }

    @Override
    public boolean preDeserialize(MaterialSource materialSource, World world, BlockPos blockPos, Rotation rotation, NBTBase nbtBase) {
        if (materialSource.isCreative())
            return true;

        IItemHandler itemHandler = materialSource.getItemHandler();
        NBTTagCompound data = (NBTTagCompound) nbtBase;
        return this.consume(itemHandler, data) && this.consumeCladding(materialSource, data);
    }

    private boolean consume(IItemHandler itemHandler, NBTTagCompound data) {
        int shape = data.getInteger("Shape");
        String name = data.getString("BaseName");
        int meta = data.getInteger("BaseData");
        return consume(
                itemHandler,
                this.shapeItem,
                1,
                0,
                stackData -> stackData.getInteger("Shape") == shape
                        && stackData.getString("BaseName").equals(name)
                        && stackData.getInteger("BaseData") == meta);
    }

    private boolean consumeCladding(MaterialSource source, NBTTagCompound data) {
        if (!data.hasKey("Name2"))
            return true;

        Shape shape = Shape.forId(data.getInteger("Shape"));
        String name = data.getString("Name2");
        int meta = data.getInteger("Data2");

        if (shape.kind instanceof ShapeKind.Window) {
            Item item = Item.REGISTRY.getObject(new ResourceLocation(name));
            return consume(source.getItemHandler(), item, 1, meta, null);
        } else {
            IItemHandler itemHandler = source.getItemHandler();
            return consume(itemHandler, this.claddingItem, 1, meta, stackData -> stackData.getString("block").equals(name));
        }
    }

    @Override
    public void deserialize(World world, BlockPos blockPos, Rotation rotation, NBTBase nbtBase) {
        NBTTagCompound data = (NBTTagCompound) nbtBase.copy();
        this.rotate(data, rotation);

        world.setBlockState(blockPos, this.shapeBlock.getStateFromMeta(data.getInteger("BaseData")));
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
        if (side == 0) {
            turn = (byte) ((turn - rotation.ordinal() + 4) & 3);
        } else if (side == 1) {
            turn = (byte) ((turn + rotation.ordinal()) & 3);
        } else {
            EnumFacing sideValue = EnumFacing.byHorizontalIndex(side);
            side = (byte) EnumFacing.byHorizontalIndex((sideValue.getHorizontalIndex() + rotation.ordinal()) & 3).ordinal();
        }

        int disconnected = data.getInteger("Disconnected");
        if (disconnected != 0 && rotation != Rotation.NONE) {
            disconnected = ROTATE_DISCONNECTED[disconnected + 64 * rotation.ordinal()];
            data.setInteger("Disconnected", disconnected);
        }

        data.setByte("turn", turn);
        data.setByte("side", side);
    }

    private ItemStack getItemStackForBlock(NBTTagCompound data) {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger("Shape", data.getInteger("Shape"));
        nbt.setString("BaseName", data.getString("BaseName"));
        nbt.setInteger("BaseData", data.getInteger("BaseData"));

        ItemStack stack = new ItemStack(this.shapeItem, 1, 0);
        stack.setTagCompound(nbt);
        return stack;
    }

    private ItemStack getCladdingForBlock(NBTTagCompound data) {
        Shape shape = Shape.forId(data.getInteger("Shape"));
        if (shape.kind instanceof ShapeKind.Window) {
            Item item = Item.REGISTRY.getObject(new ResourceLocation(data.getString("Name2")));
            return new ItemStack(item, 1, data.getInteger("Data2"));
        } else {
            NBTTagCompound nbt = new NBTTagCompound();
            nbt.setString("block", data.getString("Name2"));
            ItemStack stack = new ItemStack(this.claddingItem, 1, data.getInteger("Data2"));
            stack.setTagCompound(nbt);
            return stack;
        }
    }
}
