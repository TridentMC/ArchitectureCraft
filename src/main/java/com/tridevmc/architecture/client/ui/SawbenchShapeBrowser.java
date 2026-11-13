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
import com.tridevmc.compound.ui.element.ListView;
import com.tridevmc.compound.ui.element.Row;
import com.tridevmc.compound.ui.element.ScrollArea;
import com.tridevmc.compound.ui.element.Spacer;
import com.tridevmc.compound.ui.element.Stack;
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
    private final ListView<ShapeCategory> categories = new ListView<>();
    private final ISelection<EnumShape> selection = ISelection.create(EnumSelectionMode.SINGLE);
    private final ScrollArea shapeScroll = new ScrollArea();
    private final TextInput search = new TextInput();
    private final Button clear = new Button(false);
    private final Function<EnumShape, ItemStack> preview;
    private ShapeCategory category = ShapeCategory.ALL;
    private boolean syncingSelection;

    SawbenchShapeBrowser(Function<EnumShape, ItemStack> preview, Consumer<EnumShape> onSelection) {
        this.preview = preview;
        this.categories.setItems(List.of(ShapeCategory.values()));
        this.categories.setDisplayTextProvider(value ->
                Component.translatable(value.getTranslationKey()).getString());
        this.categories.setItemHeight(18);
        this.categories.setSelectedIndex(0);
        this.categories.setOnSelectionChanged(this::setCategory);
        this.selection.setOnSelectionChanged(shapes -> {
            if (!this.syncingSelection && !shapes.isEmpty()) onSelection.accept(shapes.getFirst());
        });
        this.search.setMaxLength(96)
                .setHint(Component.translatable("container.architecturecraft.sawbench.search")
                        .withStyle(ChatFormatting.GRAY))
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
                        0xFFFFFF, true)));
            });
        });
        scope.e(new Spacer(0, 4));
        scope.e(new Row(), browser -> {
            browser.layout().fillMaxWidth().fixedHeight(height).spacing(8);
            browser.e(this.categories, list ->
                    list.layout().fixedWidth(CATEGORY_WIDTH).fillMaxHeight());
            browser.e(new Stack(), shapes -> {
                shapes.layout().weight(1).fillMaxHeight();
                shapes.e(new Label(() -> this.source.size() == 0
                        ? Component.translatable("container.architecturecraft.sawbench.no_matches")
                        : Component.empty(), () -> 0x404040, () -> false).setWrap(true),
                        empty -> empty.layout().fillMaxWidth());
                shapes.e(this.shapeScroll, scroll -> {
                    scroll.layout().fillMax();
                    int columns = Math.max(1, (width - CATEGORY_WIDTH - 8 - 12 - 4) / CELL_SIZE);
                    scroll.fillSlot(ScrollArea.CONTENT_SLOT, content -> content.e(new Box(), inset -> {
                        inset.layout().padding(2);
                        inset.e(ILazyCollection.grid(this.source, columns, CELL_SIZE, CELL_SIZE, 0,
                                this::composeShape).selection(this.selection));
                    }));
                });
            });
        });
    }

    private void composeShape(EnumShape shape, ICompositionScope scope) {
        scope.e(new Tooltip(Component.translatable(shape.getLocalizationKey())), tooltip -> {
            tooltip.layout().fixedSize(CELL_SIZE, CELL_SIZE);
            tooltip.fillSlot(Tooltip.CONTENT_SLOT, content -> content.e(new Box(), icon -> {
                icon.layout().fillMax().contentAlignment(Alignment.CENTER);
                icon.e(new ItemDisplay(() -> this.preview.apply(shape)));
            }));
        });
    }

    private void setCategory(ShapeCategory category) {
        if (this.category == category) return;
        this.category = category;
        this.filter(this.search.getValue());
    }

    private void filter(String query) {
        var words = Arrays.stream(normalize(query).split("\\s+"))
                .filter(word -> !word.isEmpty()).toList();
        this.source.reset(this.category.getShapes().stream().filter(shape -> {
            var name = normalize(Component.translatable(shape.getLocalizationKey()).getString()
                    + " " + shape.getName());
            return words.stream().allMatch(name::contains);
        }).toList());
        this.shapeScroll.scrollTo(0, 0);
        this.clear.setEnabled(!query.isEmpty());
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
}
