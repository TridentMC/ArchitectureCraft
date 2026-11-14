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
import com.tridevmc.compound.ui.element.Panel;
import com.tridevmc.compound.ui.element.Tooltip;
import com.tridevmc.compound.ui.event.MouseClickEvent;
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
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MinecraftTestBootstrap.class)
class SawbenchPanelTest {
    private static MockedStatic<Minecraft> client;
    private static Language previousLanguage;

    @BeforeAll
    static void mockClient() throws Exception {
        previousLanguage = Language.getInstance();
        var translations = new HashMap<String, String>();
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
    void smallViewportKeepsSlotsVisibleAndShapeSelectionSurvivesFilteringAndMaterialChanges() throws Exception {
        var menu = mock(ContainerSawbench.class);
        var expectedSlots = new ArrayList<Slot>();
        for (int i = 0; i < 38; i++) {
            var slot = mock(Slot.class);
            when(slot.getItem()).thenReturn(ItemStack.EMPTY);
            when(menu.getSlot(i)).thenReturn(slot);
            expectedSlots.add(slot);
        }
        when(menu.getInputStack()).thenReturn(ItemStack.EMPTY);
        var selected = new ArrayList<EnumShape>();
        try (var shapes = mockStatic(ItemShape.class)) {
            shapes.when(() -> ItemShape.createStack(any(EnumShape.class), any()))
                    .thenAnswer(call -> new ItemStack(((net.minecraft.world.level.block.state.BlockState)
                            call.getArgument(1)).getBlock()));
            var panel = new SawbenchPanel(menu, selected::add);
            var tree = new UITree();
            panel.compose(ICompositionScope.root(tree));
            var context = mock(IScreenContext.class);
            tree.prepareFrame(320, 240, context);
            var background = tree.findNodesByElementType(Panel.class).getFirst().getBounds();
            assertEquals(280, background.width());
            assertEquals(232, background.height());
            var slots = tree.findNodesByElementType(InventorySlot.class);
            assertEquals(38, slots.size());
            assertEquals(expectedSlots, slots.stream().map(node ->
                    ((InventorySlot) node.getElement()).getVanillaSlot()).toList());
            for (var node : slots) {
                var bounds = node.getBounds();
                assertEquals(18, bounds.width());
                assertEquals(18, bounds.height());
                assertTrue(bounds.x() >= background.x() && bounds.right() <= background.right());
                assertTrue(bounds.y() >= background.y() && bounds.bottom() <= background.bottom());
            }
            UiScreenshot.save(tree, 320, 240, Path.of("build/reports/ui/sawbench-layout.png"));
            var first = tree.findNodesByElementType(Tooltip.class).getFirst().getBounds();
            int x = first.x() + first.width() / 2;
            int y = first.y() + first.height() / 2;
            assertTrue(tree.dispatchClick(x, y, new MouseClickEvent(x, y,
                    InputConstants.MOUSE_BUTTON_LEFT, false, false, false)));
            assertEquals(List.of(EnumShape.SLAB), selected);

            panel.setCategory(ShapeCategory.ARCHES);
            tree.prepareFrame(320, 240, context);
            var visible = tree.findNodesByElementType(Tooltip.class);
            assertEquals(ShapeCategory.ARCHES.getShapes().size(), visible.size());
            when(menu.getInputStack()).thenReturn(new ItemStack(Items.STONE));
            when(menu.getSelectedShape()).thenReturn(EnumShape.ARCH_D1);
            panel.tick();
            tree.prepareFrame(320, 240, context);
            assertEquals(List.of(EnumShape.SLAB), selected, "Server sync must not send another selection");
            for (var node : visible) {
                var displays = new ArrayList<ItemDisplay>();
                tree.walkDepthFirst(node, child -> {
                    if (child.getElement() instanceof ItemDisplay item) displays.add(item);
                });
                assertTrue(displays.getFirst().getItemStackSupplier().get().is(Blocks.STONE.asItem()));
            }
            tree.reset();
        }
    }
}
