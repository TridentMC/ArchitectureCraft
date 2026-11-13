package com.tridevmc.architecture.testsupport;

import com.tridevmc.compound.ui.Rect2F;
import com.tridevmc.compound.ui.sprite.IScreenSprite;
import net.minecraft.resources.Identifier;
import com.tridevmc.compound.ui.screen.IScreenContext;
import com.tridevmc.compound.ui.tree.UITree;
import net.minecraft.util.FormattedCharSequence;
import org.mockito.Answers;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import static org.mockito.Mockito.mock;

public final class UiScreenshot {
    private UiScreenshot() {
    }

    public static void save(UITree tree, int width, int height, Path path) throws IOException {
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        var graphics = image.createGraphics();
        var clips = new ArrayList<Shape>();
        var textures = new HashMap<Identifier, BufferedImage>();
        graphics.setColor(new Color(0xFF252A30, true));
        graphics.fillRect(0, 0, width, height);
        graphics.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 10));
        IScreenContext context = mock(IScreenContext.class, call -> {
            var args = call.getArguments();
            if (call.getMethod().isDefault()) {
                for (var argument : args) {
                    if (argument instanceof Rect2F) return call.callRealMethod();
                }
            }
            switch (call.getMethod().getName()) {
                case "getWidth" -> { return width; }
                case "getHeight" -> { return height; }
                case "enableScissor" -> {
                    clips.add(graphics.getClip());
                    graphics.clip(new Rectangle((int) args[0], (int) args[1],
                            (int) args[2] - (int) args[0], (int) args[3] - (int) args[1]));
                }
                case "disableScissor" -> graphics.setClip(clips.removeLast());
                case "drawGradientRect" -> {
                    graphics.setColor(new Color((int) args[4], true));
                    rectangle(graphics, args, 0);
                }
                case "drawSprite" -> {
                    var sprite = (IScreenSprite) args[0];
                    var id = sprite.getSpriteIdentifier();
                    if (id == null) return call.callRealMethod();
                    var location = Identifier.fromNamespaceAndPath(id.getNamespace(),
                            "textures/gui/sprites/" + id.getPath() + ".png");
                    var texture = texture(textures, location);
                    if (texture != null) graphics.drawImage(texture, number(args[1]), number(args[2]),
                            number(args[3]), number(args[4]), null);
                }
                case "drawTexturedRect" -> {
                    var texture = texture(textures, (Identifier) args[0]);
                    if (texture != null) {
                        int x = number(args[1]);
                        int y = number(args[2]);
                        graphics.drawImage(texture, x, y, x + number(args[3]), y + number(args[4]),
                                Math.round(((Number) args[5]).floatValue() * texture.getWidth()),
                                Math.round(((Number) args[6]).floatValue() * texture.getHeight()),
                                Math.round(((Number) args[7]).floatValue() * texture.getWidth()),
                                Math.round(((Number) args[8]).floatValue() * texture.getHeight()), null);
                    }
                }
                case "drawFormattedCharSequence", "drawFormattedCharSequenceWithShadow" -> {
                    var text = new StringBuilder();
                    int color = args.length > 3 ? ((Number) args[3]).intValue() : 0xE0E0E0;
                    graphics.setColor(new Color(color | 0xFF000000, true));
                    ((FormattedCharSequence) args[0]).accept((index, style, codePoint) -> {
                        if (style.getColor() != null) graphics.setColor(new Color(style.getColor().getValue()));
                        text.appendCodePoint(codePoint);
                        return true;
                    });
                    graphics.drawString(text.toString(), number(args[1]), number(args[2]) + 8);
                }
                case "drawItemStack" -> {
                    // Layout screenshots use a stand-in; actual mesh renders have their own tests.
                    graphics.setColor(new Color(0xFFAA8860, true));
                    if (args[1] instanceof Rect2F bounds) {
                        graphics.fillRect((int) bounds.getX() + 2, (int) bounds.getY() + 2, 12, 12);
                    } else {
                        graphics.fillRect(number(args[1]) + 2, number(args[2]) + 2, 12, 12);
                    }
                }
                default -> {
                    if (call.getMethod().isDefault()) return call.callRealMethod();
                    return Answers.RETURNS_DEFAULTS.answer(call);
                }
            }
            return null;
        });
        try {
            tree.prepareFrame(width, height, context);
            tree.renderTree(context);
            Files.createDirectories(path.getParent());
            ImageIO.write(image, "PNG", path.toFile());
        } finally {
            graphics.dispose();
        }
    }

    private static BufferedImage texture(java.util.Map<Identifier, BufferedImage> textures,
            Identifier location) {
        return textures.computeIfAbsent(location, key -> {
            try (var resource = UiScreenshot.class.getResourceAsStream(
                    "/assets/" + key.getNamespace() + "/" + key.getPath())) {
                return resource == null ? null : ImageIO.read(resource);
            } catch (IOException error) {
                throw new java.io.UncheckedIOException(error);
            }
        });
    }

    private static void rectangle(Graphics2D graphics, Object[] args, int offset) {
        graphics.fillRect(number(args[offset]), number(args[offset + 1]),
                number(args[offset + 2]), number(args[offset + 3]));
    }

    private static int number(Object value) {
        return Math.round(((Number) value).floatValue());
    }
}
