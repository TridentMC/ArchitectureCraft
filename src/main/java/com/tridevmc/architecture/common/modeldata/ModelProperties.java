package com.tridevmc.architecture.common.modeldata;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.model.data.ModelProperty;

import java.util.Objects;

public class ModelProperties {
    public static final ModelProperty<World> WORLD = new ModelProperty<World>(Objects::nonNull);
    public static final ModelProperty<BlockPos> POS = new ModelProperty<BlockPos>(Objects::nonNull);
    public static final ModelProperty<TileEntity> TILE = new ModelProperty<TileEntity>();
}
