package com.tridevmc.architecture.client.ui;

import com.tridevmc.architecture.common.block.container.ContainerSawbench;
import com.tridevmc.architecture.common.item.ItemShape;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.ShapeCategory;
import com.tridevmc.architecture.testsupport.MinecraftTestBootstrap;
import com.tridevmc.architecture.testsupport.UiScreenshot;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.InputConstants;
import com.tridevmc.compound.ui.element.InventorySlot;
import com.tridevmc.compound.ui.element.ItemDisplay;
import com.tridevmc.compound.ui.element.Button;
import com.tridevmc.compound.ui.element.ListView;
import com.tridevmc.compound.ui.element.Label;
import com.tridevmc.compound.ui.element.Panel;
import com.tridevmc.compound.ui.layout.Bounds;
import com.tridevmc.compound.ui.element.TextInput;
import com.tridevmc.compound.ui.element.Tooltip;
import com.tridevmc.compound.ui.event.MouseClickEvent;
import com.tridevmc.compound.ui.event.CharEvent;
import com.tridevmc.compound.ui.event.KeyInputEvent;
import com.tridevmc.compound.ui.scope.ICompositionScope;
import com.tridevmc.compound.ui.screen.IScreenContext;
import com.tridevmc.compound.ui.tree.UITree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MinecraftTestBootstrap.class)
class SawbenchPanelTest {
    private static MockedStatic<Minecraft> client;
    private static Language previousLanguage;
    private static Map<String, String> translations;

    @BeforeAll
    static void mockClient() throws Exception {
        previousLanguage = Language.getInstance();
        translations = new HashMap<>();
        var resource = SawbenchPanelTest.class.getResourceAsStream("/assets/architecturecraft/lang/en_us.json");
        if (resource == null) resource = SawbenchPanelTest.class.getResourceAsStream("/assets/architecturecraft/lang/en_US.json");
        try (var reader = new InputStreamReader(resource, StandardCharsets.UTF_8)) {
            JsonParser.parseReader(reader).getAsJsonObject().entrySet().forEach(entry ->
                    translations.put(entry.getKey(), entry.getValue().getAsString()));
        }
        translations.put("container.inventory", "Inventory");
        Language.inject(new Language() {
            @Override public String getOrDefault(String key, String fallback) { return translations.getOrDefault(key, fallback); }
            @Override public boolean has(String key) { return translations.containsKey(key); }
            @Override public boolean isDefaultRightToLeft() { return false; }
            @Override public FormattedCharSequence getVisualOrder(FormattedText text) { return previousLanguage.getVisualOrder(text); }
        });
        var minecraft = mock(Minecraft.class, RETURNS_DEEP_STUBS);
        var font = mock(Font.class);
        when(font.width(any(FormattedText.class))).thenAnswer(call ->
                ((FormattedText) call.getArgument(0)).getString().length() * 6);
        when(font.getSplitter()).thenReturn(new StringSplitter((codePoint, style) -> 6F));
        var lineHeight = Font.class.getDeclaredField("lineHeight");
        lineHeight.setAccessible(true);
        lineHeight.set(font, 9);
        var fontField = Minecraft.class.getDeclaredField("font");
        fontField.setAccessible(true);
        fontField.set(minecraft, font);
        var sprite = mock(TextureAtlasSprite.class, RETURNS_DEEP_STUBS);
        when(sprite.contents().getAdditionalMetadata(any())).thenReturn(Optional.empty());
        when(sprite.contents().width()).thenReturn(18);
        when(sprite.contents().height()).thenReturn(18);
        when(sprite.atlasLocation()).thenReturn(Identifier.withDefaultNamespace(
                "textures/gui/sprites/container/slot.png"));
        when(sprite.getU1()).thenReturn(1F);
        when(sprite.getV1()).thenReturn(1F);
        when(minecraft.getAtlasManager().getAtlasOrThrow(any()).getSprite(any())).thenReturn(sprite);
        client = mockStatic(Minecraft.class);
        client.when(Minecraft::getInstance).thenReturn(minecraft);
    }

    @AfterAll
    static void closeClient() {
        client.close();
        Language.inject(previousLanguage);
    }

    @Test
    void responsiveLayoutKeepsNativeSlotsAndMaterialSelectionWorking() throws Exception {
        var menu = menu();
        var selected = new ArrayList<EnumShape>();
        try (var shapes = mockPreviews()) {
            var panel = new SawbenchPanel(menu, shape -> {
                selected.add(shape);
                when(menu.getSelectedShape()).thenReturn(shape);
            });
            var tree = new UITree();
            var context = mock(IScreenContext.class);
            for (int[] viewport : new int[][]{{320, 240}, {640, 360}}) {
                tree.setViewportSize(viewport[0], viewport[1]);
                panel.compose(ICompositionScope.root(tree));
                tree.prepareFrame(viewport[0], viewport[1], context);
                var background = tree.findNodesByElementType(Panel.class).getFirst().getBounds();
                assertEquals(viewport[0] == 320 ? 312 : 420, background.width());
                assertEquals(viewport[1] == 240 ? 232 : 288, background.height());
                var slots = tree.findNodesByElementType(InventorySlot.class);
                assertEquals(38, slots.size());
                var expected = new HashSet<Slot>();
                for (int index = 0; index < 38; index++) expected.add(menu.getSlot(index));
                assertEquals(expected, slots.stream().map(node ->
                        ((InventorySlot) node.getElement()).getVanillaSlot()).collect(Collectors.toSet()));
                for (var node : slots) {
                    var bounds = node.getBounds();
                    assertEquals(18, bounds.width());
                    assertEquals(18, bounds.height());
                    assertTrue(bounds.x() >= background.x() && bounds.right() <= background.right());
                    assertTrue(bounds.y() >= background.y() && bounds.bottom() <= background.bottom());
                }
                UiScreenshot.save(tree, viewport[0], viewport[1], Path.of(
                        "build/reports/ui/sawbench-" + viewport[0] + "x" + viewport[1] + ".png"));
                tree.reset();
            }
            tree.setViewportSize(320, 240);
            panel.compose(ICompositionScope.root(tree));
            tree.prepareFrame(320, 240, context);
            var first = tree.findNodesByElementType(Tooltip.class).getFirst().getBounds();
            assertTrue(tree.dispatchClick(first.x() + 3, first.y() + 3, new MouseClickEvent(
                    first.x() + 3, first.y() + 3, InputConstants.MOUSE_BUTTON_LEFT, false, false, false)));
            assertEquals(List.of(EnumShape.SLAB), selected);
            key(tree, InputConstants.KEY_RIGHT);
            assertEquals(List.of(EnumShape.SLAB, EnumShape.values()[1]), selected);
            when(menu.getInputStack()).thenReturn(new ItemStack(Items.STONE));
            when(menu.getSelectedShape()).thenReturn(EnumShape.ARCH_D1);
            panel.tick();
            tree.prepareFrame(320, 240, context);
            assertEquals(List.of(EnumShape.SLAB, EnumShape.values()[1]), selected, "Server sync must not send another selection");
            for (var node : tree.findNodesByElementType(Tooltip.class)) {
                tree.walkDepthFirst(node, child -> {
                    if (child.getElement() instanceof ItemDisplay item) {
                        assertTrue(item.getItemStackSupplier().get().is(Blocks.STONE.asItem()));
                    }
                });
            }
            tree.reset();
        }
    }

    @Test
    void typingSearchCombinesWithCategoriesAndClearRestoresTheSelection() throws Exception {
        var menu = menu();
        var selected = new ArrayList<EnumShape>();
        try (var shapes = mockPreviews()) {
            var panel = new SawbenchPanel(menu, selected::add);
            var tree = new UITree();
            tree.setViewportSize(320, 240);
            panel.compose(ICompositionScope.root(tree));
            var context = mock(IScreenContext.class);
            tree.prepareFrame(320, 240, context);
            var inputNode = tree.findNodesByElementType(TextInput.class).getFirst();
            var search = (TextInput) inputNode.getElement();
            tree.requestFocus(inputNode);
            type(tree, context, "  D3_ArCh  ");
            assertEquals(3, tree.findNodesByElementType(Tooltip.class).size());
            tree.reset();
            tree.setViewportSize(640, 360);
            panel.compose(ICompositionScope.root(tree));
            tree.prepareFrame(640, 360, context);
            assertEquals("  D3_ArCh  ", search.getValue());
            assertEquals(3, tree.findNodesByElementType(Tooltip.class).size());
            UiScreenshot.save(tree, 640, 360, Path.of("build/reports/ui/sawbench-search-wide.png"));
            tree.reset();
            tree.setViewportSize(320, 240);
            panel.compose(ICompositionScope.root(tree));
            tree.prepareFrame(320, 240, context);
            click(tree, tree.findNodesByElementType(Tooltip.class).getFirst().getBounds());
            assertEquals(List.of(EnumShape.ARCH_D3A), selected);
            when(menu.getSelectedShape()).thenReturn(EnumShape.ARCH_D3A);
            panel.tick();
            tree.prepareFrame(320, 240, context);
            UiScreenshot.save(tree, 320, 240, Path.of("build/reports/ui/sawbench-search.png"));

            var categoryList = tree.findNodesByElementType(ListView.class).getFirst();
            var categoryBounds = categoryList.getBounds();
            click(tree, new Bounds(categoryBounds.x(), categoryBounds.y(), categoryBounds.width(), 18));
            key(tree, InputConstants.KEY_END);
            tree.prepareFrame(320, 240, context);
            assertEquals("No matching shapes", summary(tree));
            assertTrue(tree.findNodesByElementType(Tooltip.class).isEmpty());
            key(tree, InputConstants.KEY_UP);
            key(tree, InputConstants.KEY_UP);
            tree.prepareFrame(320, 240, context);
            assertEquals(3, tree.findNodesByElementType(Tooltip.class).size());
            var clear = tree.findNodesByElementType(Button.class).getFirst();
            click(tree, clear.getBounds());
            panel.tick();
            tree.prepareFrame(320, 240, context);
            assertEquals("", search.getValue());
            assertEquals(ShapeCategory.ARCHES.getShapes().size(), tree.findNodesByElementType(Tooltip.class).size());
            assertFalse(((Button) clear.getElement()).isEnabled());
            assertEquals(List.of(EnumShape.ARCH_D3A), selected, "Filtering and clearing must not craft or resend selection");

            tree.requestFocus(tree.findNodesByElementType(TextInput.class).getFirst());
            type(tree, context, "sphere");
            assertEquals("No matching shapes", summary(tree));
            UiScreenshot.save(tree, 320, 240, Path.of("build/reports/ui/sawbench-empty.png"));
            var nameKey = EnumShape.ARCH_D3A.getLocalizationKey();
            var originalName = translations.put(nameKey, "Stone Vault");
            try {
                search.setValue("");
                tree.prepareFrame(320, 240, context);
                type(tree, context, "VAULT");
                assertEquals(1, tree.findNodesByElementType(Tooltip.class).size(), "Search must include translated names");
            } finally {
                translations.put(nameKey, originalName);
                tree.reset();
            }
        }
    }

    private static ContainerSawbench menu() {
        var menu = mock(ContainerSawbench.class);
        for (int i = 0; i < 38; i++) {
            var slot = mock(Slot.class);
            when(slot.getItem()).thenReturn(ItemStack.EMPTY);
            when(menu.getSlot(i)).thenReturn(slot);
        }
        when(menu.getInputStack()).thenReturn(ItemStack.EMPTY);
        return menu;
    }

    private static MockedStatic<ItemShape> mockPreviews() {
        var shapes = mockStatic(ItemShape.class);
        shapes.when(() -> ItemShape.createStack(any(EnumShape.class), any()))
                .thenAnswer(call -> new ItemStack(((BlockState)
                        call.getArgument(1)).getBlock()));
        return shapes;
    }

    private static String summary(UITree tree) {
        return tree.findNodesByElementType(Label.class).stream().map(node ->
                ((Label) node.getElement()).getTextSupplier().get().getString())
                .filter(text -> text.equals("No matching shapes")).findFirst().orElseThrow();
    }

    private static void click(UITree tree, Bounds bounds) {
        int x = bounds.x() + bounds.width() / 2;
        int y = bounds.y() + bounds.height() / 2;
        assertTrue(tree.dispatchClick(x, y, new MouseClickEvent(x, y,
                InputConstants.MOUSE_BUTTON_LEFT, false, false, false)));
    }

    private static void type(UITree tree, IScreenContext context, String text) {
        text.codePoints().forEach(codePoint -> {
            assertTrue(tree.dispatchCharTyped(new CharEvent(codePoint, 0)));
            tree.prepareFrame(320, 240, context);
        });
    }

    private static void key(UITree tree, int key) {
        assertTrue(tree.dispatchKeyPress(new KeyInputEvent(key, key, false, false, false)));
    }
}
