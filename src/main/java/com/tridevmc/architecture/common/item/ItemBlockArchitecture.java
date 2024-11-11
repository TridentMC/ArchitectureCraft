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

public class ItemBlockArchitecture extends BlockItem implements IClientItemExtensionHolder {

    public ItemBlockArchitecture(BlockArchitecture pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public IClientItemExtensions getExtensions() {
        return new IArchitectureClientItemExtensions() {
            IArchitectureBakedModel cachedModel;

            IArchitectureBakedModel getModel() {
                if (this.cachedModel == null) {
                    if (Minecraft.getInstance().getBlockRenderer().getBlockModel(ItemBlockArchitecture.this.getBlock().defaultBlockState()) instanceof IArchitectureBakedModel model) {
                        this.cachedModel = model;
                    } else {
                        throw new IllegalStateException("Block " + ItemBlockArchitecture.this.getBlock().getDescriptionId() + " does not have a valid IArchitectureBakedModel");
                    }
                }
                return this.cachedModel;
            }

            @Override
            public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
                var model = getModel();
                var quads = model.getQuads(stack);
                for (var modelForPass : model.getRenderPasses(stack)) {
                    for (var rendertype : modelForPass.getRenderTypes(stack)) {
                        Minecraft.getInstance().getItemRenderer().renderQuadList(poseStack, buffer.getBuffer(rendertype), quads, stack, packedLight, packedOverlay);
                    }
                }
            }
        };
    }
}
