package com.tridevmc.architecture.testsupport;

import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.ShapeMeshes;
import com.tridevmc.architecture.common.shape.orientation.EnumFlip;
import com.tridevmc.architecture.common.shape.orientation.EnumPlacementOffset;
import com.tridevmc.architecture.common.shape.orientation.EnumSpin;
import com.tridevmc.architecture.common.shape.orientation.EnumTurn;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyAxis;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFacing;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFlip;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyHorizontalFacing;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyOffset;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertySpin;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyTurn;
import net.minecraft.core.Direction;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;

public final class ShapeScreenshot {
    private record Surface(Polygon polygon, double depth, Color color) {}
    private ShapeScreenshot() {}

    public static void write(Path path) throws IOException {
        int columns = 6, width = 300, height = 150, header = 48;
        var image = new BufferedImage(columns * width, ((EnumShape.values().length + columns - 1) / columns) * height + header,
                BufferedImage.TYPE_INT_RGB);
        var g = image.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(20, 23, 30)); g.fillRect(0, 0, image.getWidth(), image.getHeight());
            g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13)); g.setColor(Color.WHITE);
            g.drawString("Geometry only: actual meshes and resolver presets A WEST/Y, B NORTH/X, C UP/Z; no Minecraft textures", 12, 20);
            g.drawString("Purple = base surfaces; blue = secondary surfaces. Camera and scale remain fixed within each shape.", 12, 38);
            for (var shape : EnumShape.values()) {
                int left = shape.ordinal() % columns * width, top = shape.ordinal() / columns * height + header;
                g.setColor(new Color(37, 42, 53)); g.fillRoundRect(left + 3, top + 3, width - 6, height - 6, 8, 8);
                g.setColor(Color.WHITE); g.drawString(shape.getName(), left + 10, top + 20);
                var mesh = ShapeMeshes.getMesh(shape);
                var bounds = mesh.getBounds();
                double scale = 39 / Math.max(1, Math.max(bounds.maxX() - bounds.minX(),
                        Math.max(bounds.maxY() - bounds.minY(), bounds.maxZ() - bounds.minZ())));
                for (int preset = 0; preset < 3; preset++) {
                    var orientation = orientation(preset);
                    var transform = shape.getTransformationResolver().resolve(orientation);
                    var surfaces = new ArrayList<Surface>();
                    for (var polygon : ShapeMeshes.getMesh(shape, orientation).getFaces().stream().flatMap(f -> f.getPolygons().stream()).toList()) {
                        var projected = new Polygon(); double depth = 0;
                        for (int vertex = 0; vertex < polygon.getVertexCount(); vertex++) {
                            var p = transform.transformPosImmutable(polygon.getVertex(vertex).getPos());
                            projected.addPoint(left + 50 + preset * 100 + (int) Math.round((p.x() - p.z()) * 0.866 * scale),
                                    top + 90 + (int) Math.round((p.x() + p.z() - 2 * p.y()) * 0.408 * scale));
                            depth += p.x() + p.y() + p.z();
                        }
                        var normal = transform.transformNormalImmutable(polygon.getNormal());
                        double shade = 0.48 + 0.48 * Math.max(0, normal.x() * 0.3 + normal.y() * 0.8 + normal.z() * 0.5);
                        var base = polygon.getPolygonData().textureIndex() == 0 ? new Color(180, 147, 231) : new Color(102, 197, 231);
                        surfaces.add(new Surface(projected, depth / polygon.getVertexCount(), new Color(
                                (int) (base.getRed() * shade), (int) (base.getGreen() * shade), (int) (base.getBlue() * shade))));
                    }
                    surfaces.sort(Comparator.comparingDouble(Surface::depth));
                    for (var surface : surfaces) { g.setColor(surface.color()); g.fillPolygon(surface.polygon()); }
                    g.setColor(new Color(180, 188, 205)); g.drawString(String.valueOf((char) ('A' + preset)), left + 46 + preset * 100, top + 137);
                }
            }
        } finally { g.dispose(); }
        Files.createDirectories(path.getParent()); ImageIO.write(image, "png", path.toFile());
    }

    private static ShapeOrientation orientation(int preset) {
        return ShapeOrientation.forProperties(
                ShapeOrientationPropertyFacing.of(new Direction[]{Direction.WEST, Direction.NORTH, Direction.UP}[preset]),
                ShapeOrientationPropertyHorizontalFacing.of(new Direction[]{Direction.NORTH, Direction.EAST, Direction.SOUTH}[preset]),
                ShapeOrientationPropertyAxis.of(new Direction.Axis[]{Direction.Axis.Y, Direction.Axis.X, Direction.Axis.Z}[preset]),
                ShapeOrientationPropertySpin.of(preset == 2 ? EnumSpin.HALF : EnumSpin.NONE),
                ShapeOrientationPropertyFlip.of(EnumFlip.STANDARD), ShapeOrientationPropertyTurn.of(EnumTurn.UNTURNED),
                ShapeOrientationPropertyOffset.of(EnumPlacementOffset.MIDDLE));
    }
}
