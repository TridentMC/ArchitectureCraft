package com.tridevmc.architecture.common.model;


import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.model.data.ModelProperty;

import java.util.Objects;

public class ModelProperties {
    public static final ModelProperty<Level> LEVEL = new ModelProperty<Level>(Objects::nonNull);
    public static final ModelProperty<BlockPos> POS = new ModelProperty<BlockPos>(Objects::nonNull);
    public static final ModelProperty<BlockEntity> TILE = new ModelProperty<BlockEntity>();
}
