package com.tridevmc.architecture.common.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.tridevmc.architecture.client.extensions.IArchitectureClientItemExtensions;
import com.tridevmc.architecture.client.render.model.baked.IArchitectureBakedModel;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class ItemBlockArchitecture extends BlockItem {

    public ItemBlockArchitecture(BlockArchitecture pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

}
