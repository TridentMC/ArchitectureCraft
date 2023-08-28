package com.tridevmc.architecture.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public interface IArchitectureClientItemExtensions extends IClientItemExtensions {

    static RandomSource RANDOM_SOURCE = RandomSource.create();
    static BlockEntityWithoutLevelRenderer BLOCK_ENTITY_WITHOUT_LEVEL_RENDERER = new BlockEntityWithoutLevelRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()) {
        @Override
        public void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay) {
            var itemExtensions = (IArchitectureClientItemExtensions) IClientItemExtensions.of(stack.getItem());
            itemExtensions.renderByItem(stack, displayContext, poseStack, buffer, packedLight, packedOverlay);
        }
    };

    @Override
    default BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return BLOCK_ENTITY_WITHOUT_LEVEL_RENDERER;
    }

    /**
     * Renders the given item stack, acts as a proxy for {@link BlockEntityWithoutLevelRenderer#renderByItem(ItemStack, ItemDisplayContext, PoseStack, MultiBufferSource, int, int)}
     *
     * @param stack          the item stack to render
     * @param displayContext the display context to render in
     * @param poseStack      the pose stack to render with
     * @param buffer         the buffer to render to
     * @param packedLight    the packed light value
     * @param packedOverlay  the packed overlay value
     */
    void renderByItem(@NotNull ItemStack stack, @NotNull ItemDisplayContext displayContext, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight, int packedOverlay);
}
