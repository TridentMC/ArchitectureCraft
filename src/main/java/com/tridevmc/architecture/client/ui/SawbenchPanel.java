package com.tridevmc.architecture.client.ui;

import com.tridevmc.architecture.common.block.container.ContainerSawbench;
import com.tridevmc.architecture.common.item.ItemShape;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.compound.ui.element.Box;
import com.tridevmc.compound.ui.element.Column;
import com.tridevmc.compound.ui.element.Grid;
import com.tridevmc.compound.ui.element.InventorySlot;
import com.tridevmc.compound.ui.element.Label;
import com.tridevmc.compound.ui.element.Rect;
import com.tridevmc.compound.ui.element.Row;
import com.tridevmc.compound.ui.element.Spacer;
import com.tridevmc.compound.ui.element.Stack;
import com.tridevmc.compound.ui.element.Surface;
import com.tridevmc.compound.ui.layout.Alignment;
import com.tridevmc.compound.ui.scope.ICompositionScope;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

final class SawbenchPanel {
    private final ContainerSawbench menu;
    private final Map<EnumShape, ItemStack> previews = new EnumMap<>(EnumShape.class);
    private final SawbenchShapeBrowser browser;
    private BlockState material;

    SawbenchPanel(ContainerSawbench menu, Consumer<EnumShape> onSelection) {
        this.menu = menu;
        this.browser = new SawbenchShapeBrowser(this.previews::get, onSelection);
        this.tick();
    }

    void compose(ICompositionScope scope) {
        var viewport = scope.getTree().getViewportSize();
        int width = Math.clamp(viewport.width() - 8, 312, 420);
        int height = Math.clamp(viewport.height() - 8, 232, 288);
        scope.e(new Stack(), root -> {
            root.layout().fillMax().contentAlignment(Alignment.CENTER);
            root.e(new Surface(SawbenchPalette.BACKGROUND, SawbenchPalette.BORDER, 1), panel -> {
                panel.layout().fixedSize(width, height);
                panel.fillSlot(Surface.CONTENT_SLOT, content -> content.e(new Column(), column -> {
                    column.layout().padding(6).fillMax();
                    column.e(new Row(), header -> {
                        header.layout().fillMaxWidth().fixedHeight(9).spacing(6);
                        header.e(new Rect(SawbenchPalette.ACCENT), accent -> accent.layout().fixedSize(2, 9));
                        header.e(new Label(Component.translatable("container.architecturecraft.sawbench")
                                .withStyle(ChatFormatting.BOLD), SawbenchPalette.TEXT, false));
                        header.e(new Spacer(0, 0), gap -> gap.layout().weight(1));
                        header.e(new Label(this.browser::resultSummary, () -> SawbenchPalette.MUTED, () -> false));
                    });
                    column.e(new Spacer(0, 4));
                    this.browser.compose(column, width - 12, height - 140);
                    column.e(new Spacer(0, 6));
                    column.e(new Row(), footer -> {
                        footer.layout().fillMaxWidth().fixedHeight(87).spacing(12);
                        this.composeInventory(footer);
                        footer.e(new Rect(SawbenchPalette.BORDER), divider -> divider.layout().fixedWidth(1).fillMaxHeight());
                        this.composeCutting(footer);
                    });
                }));
            });
        });
    }

    private void composeInventory(ICompositionScope scope) {
        scope.e(new Column(), inventory -> {
            inventory.layout().fixedWidth(162);
            inventory.e(new Label(Component.translatable("container.inventory"), SawbenchPalette.MUTED, false));
            inventory.e(new Spacer(0, 2));
            inventory.e(new Grid(9), grid -> {
                for (int i = 2; i < 29; i++) grid.e(new InventorySlot(this.menu, i));
            });
            inventory.e(new Spacer(0, 4));
            inventory.e(new Grid(9), hotbar -> {
                for (int i = 29; i < 38; i++) hotbar.e(new InventorySlot(this.menu, i));
            });
        });
    }

    private void composeCutting(ICompositionScope scope) {
        scope.e(new Column(), cutting -> {
            cutting.layout().weight(1).spacing(4);
            cutting.e(new Label(Component.translatable("container.architecturecraft.sawbench.cutting"),
                    SawbenchPalette.ACCENT, false));
            cutting.e(new Row(), slots -> {
                slots.layout().spacing(6).verticalAlignment(Alignment.CENTER);
                this.composeSlot(slots, 0, SawbenchPalette.BORDER);
                slots.e(new Label(Component.literal("→"), SawbenchPalette.MUTED, false));
                this.composeSlot(slots, 1, SawbenchPalette.ACCENT);
            });
            cutting.e(new Label(this::selectedName, () -> SawbenchPalette.TEXT, () -> false).setWrap(true),
                    selected -> selected.layout().fillMaxWidth().fixedHeight(18).clip());
            cutting.e(new Label(this::materialName, () -> SawbenchPalette.MUTED, () -> false).setWrap(true),
                    material -> material.layout().fillMaxWidth().fixedHeight(18).clip());
        });
    }

    private void composeSlot(ICompositionScope scope, int index, int border) {
        scope.e(new Surface(SawbenchPalette.SURFACE, border, 1), frame -> {
            frame.layout().fixedSize(20, 20);
            frame.fillSlot(Surface.CONTENT_SLOT, content -> content.e(new Box(), inset -> {
                inset.layout().padding(1).contentAlignment(Alignment.CENTER);
                inset.e(new InventorySlot(this.menu, index));
            }));
        });
    }

    private Component selectedName() {
        var selected = this.menu.getSelectedShape();
        return Component.translatable(selected == null
                ? "container.architecturecraft.sawbench.choose_shape" : selected.getLocalizationKey());
    }

    private Component materialName() {
        var input = this.menu.getInputStack();
        return input.isEmpty() ? Component.translatable("container.architecturecraft.sawbench.add_material")
                : input.getHoverName();
    }

    void tick() {
        var input = this.menu.getInputStack();
        var replacement = input.getItem() instanceof BlockItem block
                ? block.getBlock().defaultBlockState() : Blocks.OAK_PLANKS.defaultBlockState();
        if (replacement != this.material) {
            this.material = replacement;
            for (var shape : EnumShape.values()) this.previews.put(shape, ItemShape.createStack(shape, replacement));
        }
        this.browser.synchronizeSelection(this.menu.getSelectedShape());
    }
}
