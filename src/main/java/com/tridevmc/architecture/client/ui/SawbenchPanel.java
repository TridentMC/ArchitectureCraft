package com.tridevmc.architecture.client.ui;

import com.tridevmc.architecture.common.block.container.ContainerSawbench;
import com.tridevmc.architecture.common.item.ItemShape;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.ShapeCategory;
import com.tridevmc.compound.ui.element.Box;
import com.tridevmc.compound.ui.element.Column;
import com.tridevmc.compound.ui.element.EnumSelectionMode;
import com.tridevmc.compound.ui.element.Grid;
import com.tridevmc.compound.ui.element.ILazyCollection;
import com.tridevmc.compound.ui.element.ILazySource;
import com.tridevmc.compound.ui.element.ISelection;
import com.tridevmc.compound.ui.element.InventorySlot;
import com.tridevmc.compound.ui.element.ItemDisplay;
import com.tridevmc.compound.ui.element.Label;
import com.tridevmc.compound.ui.element.Panel;
import com.tridevmc.compound.ui.element.Row;
import com.tridevmc.compound.ui.element.ScrollArea;
import com.tridevmc.compound.ui.element.Spacer;
import com.tridevmc.compound.ui.element.Stack;
import com.tridevmc.compound.ui.element.Tooltip;
import com.tridevmc.compound.ui.layout.Alignment;
import com.tridevmc.compound.ui.scope.ICompositionScope;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

final class SawbenchPanel {
    private final ContainerSawbench menu;
    private final ILazySource.Mutable<EnumShape, EnumShape> source =
            ILazySource.mutable(ShapeCategory.ALL.getShapes(), shape -> shape);
    private final ISelection<ShapeCategory> categorySelection = ISelection.create(EnumSelectionMode.SINGLE);
    private final ISelection<EnumShape> selection = ISelection.create(EnumSelectionMode.SINGLE);
    private final ScrollArea shapeScroll = new ScrollArea();
    private final ScrollArea categoryScroll = new ScrollArea();
    private final Map<EnumShape, ItemStack> previews = new EnumMap<>(EnumShape.class);
    private ShapeCategory category = ShapeCategory.ALL;
    private BlockState material;
    private boolean syncingSelection;

    SawbenchPanel(ContainerSawbench menu, Consumer<EnumShape> onSelection) {
        this.menu = menu;
        this.categorySelection.setKeys(List.of(ShapeCategory.values()));
        this.categorySelection.setSelectedKeys(List.of(ShapeCategory.ALL));
        this.categorySelection.setOnSelectionChanged(categories -> {
            if (!categories.isEmpty()) this.setCategory(categories.getFirst());
        });
        this.selection.setOnSelectionChanged(shapes -> {
            if (!this.syncingSelection && !shapes.isEmpty()) onSelection.accept(shapes.getFirst());
        });
        this.tick();
    }

    void compose(ICompositionScope scope) {
        scope.e(new Stack(), root -> {
            root.layout().fillMax().contentAlignment(Alignment.CENTER);
            root.e(new Panel(), panel -> {
                panel.layout().fixedSize(280, 232);
                panel.fillSlot(Panel.CONTENT_SLOT, content -> {
                    content.e(new Column(), column -> {
                        column.layout().padding(8).horizontalAlignment(Alignment.CENTER);
                        column.e(new Label(Component.translatable("container.architecturecraft.sawbench"),
                                0x404040, false));
                        column.e(new Spacer(0, 4));
                        column.e(new Row(), slots -> {
                            slots.layout().spacing(8).verticalAlignment(Alignment.CENTER);
                            slots.e(new Label(Component.translatable("container.architecturecraft.sawbench.input"),
                                    0x404040, false));
                            slots.e(new InventorySlot(this.menu, 0));
                            slots.e(new Label(Component.literal("->"), 0x404040, false));
                            slots.e(new InventorySlot(this.menu, 1));
                            slots.e(new Label(Component.translatable("container.architecturecraft.sawbench.output"),
                                    0x404040, false));
                        });
                        column.e(new Spacer(0, 4));
                        column.e(new Row(), browser -> {
                            browser.layout().spacing(8).fixedHeight(90);
                            browser.e(this.categoryScroll, scroll -> {
                                scroll.layout().fixedSize(88, 90);
                                var categories = ShapeCategory.values();
                                var categorySource = ILazySource.immutable(categories.length,
                                        index -> categories[index], index -> categories[index]);
                                scroll.fillSlot(ScrollArea.CONTENT_SLOT, contentList -> contentList.e(
                                        ILazyCollection.list(categorySource, 76, 18, 1, (value, entry) -> {
                                            entry.e(new Box(), label -> {
                                                label.layout().padding(4).contentAlignment(Alignment.CENTER_LEFT);
                                                label.e(new Label(Component.translatable(value.getTranslationKey()),
                                                        () -> value == this.category ? 0x404040 : 0x606060,
                                                        () -> false));
                                            });
                                        }).selection(this.categorySelection)));
                            });
                            browser.e(this.shapeScroll, scroll -> {
                                scroll.layout().fixedSize(156, 90);
                                scroll.fillSlot(ScrollArea.CONTENT_SLOT, shapes -> shapes.e(
                                        ILazyCollection.grid(this.source, 8, 18, 18, 0,
                                                this::composeShape).selection(this.selection)));
                            });
                        });
                        column.e(new Spacer(0, 4));
                        column.e(new Label(Component.translatable("container.inventory"), 0x404040, false));
                        column.e(new Spacer(0, 2));
                        column.e(new Grid(9), inventory -> {
                            for (int i = 2; i < 29; i++) inventory.e(new InventorySlot(this.menu, i));
                        });
                        column.e(new Spacer(0, 4));
                        column.e(new Grid(9), hotbar -> {
                            for (int i = 29; i < 38; i++) hotbar.e(new InventorySlot(this.menu, i));
                        });
                    });
                });
            });
        });
    }

    private void composeShape(EnumShape shape, ICompositionScope scope) {
        scope.e(new Tooltip(Component.translatable(shape.getLocalizationKey())), tooltip -> {
            tooltip.layout().fixedSize(18, 18);
            tooltip.fillSlot(Tooltip.CONTENT_SLOT, content -> content.e(new Box(), icon -> {
                icon.layout().padding(1);
                icon.e(new ItemDisplay(() -> this.previews.get(shape)));
            }));
        });
    }

    void setCategory(ShapeCategory category) {
        if (this.category == category) return;
        this.category = category;
        this.categorySelection.setSelectedKeys(List.of(category));
        this.source.reset(category.getShapes());
        this.shapeScroll.scrollTo(0, 0);
    }

    void tick() {
        var input = this.menu.getInputStack();
        var replacement = input.getItem() instanceof BlockItem block
                ? block.getBlock().defaultBlockState() : Blocks.OAK_PLANKS.defaultBlockState();
        if (replacement != this.material) {
            this.material = replacement;
            for (var shape : EnumShape.values()) {
                this.previews.put(shape, ItemShape.createStack(shape, replacement));
            }
        }
        var selected = this.menu.getSelectedShape();
        if (selected != null && this.selection.getKeys().contains(selected)) {
            this.syncingSelection = true;
            try {
                this.selection.setSelectedKeys(List.of(selected));
            } finally {
                this.syncingSelection = false;
            }
        }
    }
}
