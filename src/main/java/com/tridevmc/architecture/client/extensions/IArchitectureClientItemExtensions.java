package com.tridevmc.architecture.client.extensions;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public interface IArchitectureClientItemExtensions extends IClientItemExtensions {

    /**
     * Renders the given item stack.
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
