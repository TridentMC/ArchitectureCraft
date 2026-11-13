package com.tridevmc.architecture.client.ui;

import com.tridevmc.architecture.common.block.container.ContainerSawbench;
import com.tridevmc.architecture.common.item.ItemShape;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.compound.ui.element.Box;
import com.tridevmc.compound.ui.element.Column;
import com.tridevmc.compound.ui.element.Grid;
import com.tridevmc.compound.ui.element.InventorySlot;
import com.tridevmc.compound.ui.element.Label;
import com.tridevmc.compound.ui.element.Row;
import com.tridevmc.compound.ui.element.Spacer;
import com.tridevmc.compound.ui.element.Stack;
import com.tridevmc.compound.ui.element.Panel;
import com.tridevmc.compound.ui.layout.Alignment;
import com.tridevmc.compound.ui.scope.ICompositionScope;
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
            root.e(new Panel(), panel -> {
                panel.layout().fixedSize(width, height);
                panel.fillSlot(Panel.CONTENT_SLOT, content -> content.e(new Column(), column -> {
                    column.layout().padding(6).fillMax();
                    column.e(new Label(Component.translatable("container.architecturecraft.sawbench"),
                            0x404040, false));
                    column.e(new Spacer(0, 4));
                    this.browser.compose(column, width - 12, height - 140);
                    column.e(new Spacer(0, 6));
                    column.e(new Row(), footer -> {
                        footer.layout().fillMaxWidth().fixedHeight(87).spacing(12);
                        this.composeInventory(footer);
                        this.composeCutting(footer);
                    });
                }));
            });
        });
    }

    private void composeInventory(ICompositionScope scope) {
        scope.e(new Column(), inventory -> {
            inventory.layout().fixedWidth(162);
            inventory.e(new Label(Component.translatable("container.inventory"), 0x404040, false));
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
        scope.e(new Box(), cutting -> {
            cutting.layout().weight(1).fillMaxHeight().contentAlignment(Alignment.CENTER);
            cutting.e(new Row(), slots -> {
                slots.layout().spacing(6).verticalAlignment(Alignment.CENTER);
                slots.e(new InventorySlot(this.menu, 0));
                slots.e(new Label(Component.literal("→"), 0x404040, false));
                slots.e(new InventorySlot(this.menu, 1));
            });
        });
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
