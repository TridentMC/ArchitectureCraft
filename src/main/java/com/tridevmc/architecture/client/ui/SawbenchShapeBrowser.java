package com.tridevmc.architecture.client.ui;

import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.ShapeCategory;
import com.tridevmc.compound.ui.element.Box;
import com.tridevmc.compound.ui.element.Button;
import com.tridevmc.compound.ui.element.EnumSelectionMode;
import com.tridevmc.compound.ui.element.ILazyCollection;
import com.tridevmc.compound.ui.element.ILazySource;
import com.tridevmc.compound.ui.element.ISelection;
import com.tridevmc.compound.ui.element.ItemDisplay;
import com.tridevmc.compound.ui.element.Label;
import com.tridevmc.compound.ui.element.Row;
import com.tridevmc.compound.ui.element.ScrollArea;
import com.tridevmc.compound.ui.element.Spacer;
import com.tridevmc.compound.ui.element.Surface;
import com.tridevmc.compound.ui.element.TextInput;
import com.tridevmc.compound.ui.element.Tooltip;
import com.tridevmc.compound.ui.layout.Alignment;
import com.tridevmc.compound.ui.scope.ICompositionScope;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;

final class SawbenchShapeBrowser {
    private static final int CATEGORY_WIDTH = 84;
    private static final int CELL_SIZE = 24;
    private final ILazySource.Mutable<EnumShape, EnumShape> source =
            ILazySource.mutable(ShapeCategory.ALL.getShapes(), shape -> shape);
    private final ISelection<ShapeCategory> categories = ISelection.create(EnumSelectionMode.SINGLE);
    private final ISelection<EnumShape> selection = ISelection.create(EnumSelectionMode.SINGLE);
    private final ScrollArea shapeScroll = new ScrollArea();
    private final ScrollArea categoryScroll = new ScrollArea();
    private final TextInput search = new TextInput();
    private final Button clear = new Button(false);
    private final Function<EnumShape, ItemStack> preview;
    private ShapeCategory category = ShapeCategory.ALL;
    private boolean syncingSelection;

    SawbenchShapeBrowser(Function<EnumShape, ItemStack> preview, Consumer<EnumShape> onSelection) {
        this.preview = preview;
        this.categories.setKeys(List.of(ShapeCategory.values()));
        this.categories.setSelectedKeys(List.of(this.category));
        this.categories.setOnSelectionChanged(values -> {
            if (!values.isEmpty()) this.setCategory(values.getFirst());
        });
        this.selection.setOnSelectionChanged(shapes -> {
            if (!this.syncingSelection && !shapes.isEmpty()) onSelection.accept(shapes.getFirst());
        });
        this.search.setMaxLength(96).setTextColor(SawbenchPalette.MUTED)
                .setHint(Component.translatable("container.architecturecraft.sawbench.search")
                        .withStyle(ChatFormatting.GRAY))
                .setBackground(scope -> scope.e(new Surface(SawbenchPalette.SURFACE,
                        SawbenchPalette.BORDER, 1), surface -> surface.layout().fillMax()))
                .setResponder(this::filter);
        this.clear.addPressListener((x, y) -> this.search.setValue(""));
    }

    void compose(ICompositionScope scope, int width, int height) {
        scope.e(new Row(), toolbar -> {
            toolbar.layout().fillMaxWidth().fixedHeight(18).spacing(4);
            toolbar.e(this.search, input -> input.layout().weight(1).fixedHeight(18));
            toolbar.e(this.clear, button -> {
                button.layout().fixedSize(40, 18);
                button.fillSlot(Button.CONTENT_SLOT, text -> text.e(new Label(
                        Component.translatable("container.architecturecraft.sawbench.clear"),
                        SawbenchPalette.TEXT, false)));
            });
        });
        scope.e(new Spacer(0, 4));
        scope.e(new Row(), browser -> {
            browser.layout().fillMaxWidth().fixedHeight(height).spacing(8);
            browser.e(new Surface(SawbenchPalette.SURFACE, SawbenchPalette.BORDER, 1), navigation -> {
                navigation.layout().fixedWidth(CATEGORY_WIDTH).fillMaxHeight();
                navigation.fillSlot(Surface.CONTENT_SLOT, content -> content.e(new Box(), inset -> {
                    inset.layout().padding(2);
                    inset.e(this.categoryScroll, scroll -> {
                        scroll.layout().fillMax();
                        var values = ShapeCategory.values();
                        scroll.fillSlot(ScrollArea.CONTENT_SLOT, list -> list.e(ILazyCollection.list(
                                ILazySource.immutable(values.length, i -> values[i], i -> values[i]),
                                CATEGORY_WIDTH - 16, 18, 0, this::composeCategory).selection(this.categories)));
                    });
                }));
            });
            browser.e(this.shapeScroll, scroll -> {
                scroll.layout().weight(1).fillMaxHeight();
                int columns = Math.max(1, (width - CATEGORY_WIDTH - 8 - 12 - 4) / CELL_SIZE);
                scroll.fillSlot(ScrollArea.CONTENT_SLOT, content -> content.e(new Box(), inset -> {
                    inset.layout().padding(2);
                    inset.e(ILazyCollection.grid(this.source, columns, CELL_SIZE, CELL_SIZE, 0,
                            this::composeShape).selection(this.selection));
                }));
            });
        });
    }

    private void composeCategory(ShapeCategory value, ICompositionScope scope) {
        scope.e(new Surface(() -> value == this.category ? SawbenchPalette.SELECTED : SawbenchPalette.SURFACE), cell -> {
            cell.layout().fillMax().clip();
            cell.fillSlot(Surface.CONTENT_SLOT, content -> content.e(new Box(), inset -> {
                inset.layout().padding(4).contentAlignment(Alignment.CENTER_LEFT);
                inset.e(new Label(Component.translatable(value.getTranslationKey()),
                        () -> value == this.category ? SawbenchPalette.ACCENT : SawbenchPalette.MUTED,
                        () -> false));
            }));
        });
    }

    private void composeShape(EnumShape shape, ICompositionScope scope) {
        scope.e(new Tooltip(Component.translatable(shape.getLocalizationKey())), tooltip -> {
            tooltip.layout().fixedSize(CELL_SIZE, CELL_SIZE);
            tooltip.fillSlot(Tooltip.CONTENT_SLOT, content -> content.e(new Surface(
                    () -> this.selection.isSelected(shape) ? SawbenchPalette.SELECTED : SawbenchPalette.SURFACE,
                    () -> SawbenchPalette.BORDER, 1), cell -> {
                cell.layout().fillMax();
                cell.fillSlot(Surface.CONTENT_SLOT, icon -> icon.e(new Box(), centered -> {
                    centered.layout().contentAlignment(Alignment.CENTER);
                    centered.e(new ItemDisplay(() -> this.preview.apply(shape)));
                }));
            }));
        });
    }

    void setCategory(ShapeCategory category) {
        if (this.category == category) return;
        this.category = category;
        this.categories.setSelectedKeys(List.of(category));
        this.filter(this.search.getValue());
    }

    private void filter(String query) {
        var words = Arrays.stream(normalize(query).split("\s+"))
                .filter(word -> !word.isEmpty()).toList();
        this.source.reset(this.category.getShapes().stream().filter(shape -> {
            var name = normalize(Component.translatable(shape.getLocalizationKey()).getString()
                    + " " + shape.getName());
            return words.stream().allMatch(name::contains);
        }).toList());
        this.shapeScroll.scrollTo(0, 0);
        this.clear.setEnabled(!query.isEmpty());
        this.search.setTextColor(query.isEmpty() ? SawbenchPalette.MUTED : SawbenchPalette.TEXT);
    }

    private static String normalize(String text) {
        return text.toLowerCase(Locale.ROOT).replace('_', ' ').replace('-', ' ').strip();
    }

    void synchronizeSelection(EnumShape selected) {
        this.syncingSelection = true;
        try {
            this.selection.setSelectedKeys(selected != null && this.selection.getKeys().contains(selected)
                    ? List.of(selected) : List.of());
        } finally {
            this.syncingSelection = false;
        }
    }

    Component resultSummary() {
        return this.source.size() == 0
                ? Component.translatable("container.architecturecraft.sawbench.no_matches")
                : Component.translatable(this.source.size() == 1
                        ? "container.architecturecraft.sawbench.shape_count_one"
                        : "container.architecturecraft.sawbench.shape_count", this.source.size());
    }
}
