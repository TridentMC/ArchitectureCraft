package com.tridevmc.architecture.common.shape;

import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

/**
 * Categories for grouping architectural shapes in the sawbench UI.
 */
public enum ShapeCategory {
    ALL("all"),
    BASIC("basic"),
    STAIRS("stairs"),
    ROOFS("roofs"),
    ROUNDED("rounded"),
    PILLARS("pillars"),
    CLASSICAL("classical"),
    CORNICES("cornices"),
    WINDOWS("windows"),
    ARCHES("arches"),
    RAILINGS("railings"),
    OTHER("other");

    private final String name;

    ShapeCategory(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public String getTranslationKey() {
        return "shape_category.architecturecraft." + this.name;
    }

    /**
     * Gets all shapes that belong to this category.
     * Only returns shapes that have placement logic (are craftable).
     */
    public List<EnumShape> getShapes() {
        if (this == ALL) {
            return Arrays.stream(EnumShape.values())
                    .filter(s -> s.getPlacementLogic() != null)
                    .toList();
        }

        List<EnumShape> shapes = Lists.newArrayList();

        for (EnumShape shape : EnumShape.values()) {
            if (shape.getPlacementLogic() == null) {
                continue; // Skip non-craftable shapes
            }

            String shapeName = shape.getName();

            switch (this) {
                case BASIC:
                    if (shapeName.equals("slab") ||
                        shapeName.startsWith("bevelled")) {
                        shapes.add(shape);
                    }
                    break;

                case STAIRS:
                    if (shapeName.startsWith("stairs")) {
                        shapes.add(shape);
                    }
                    break;

                case ROOFS:
                    if (shapeName.startsWith("roof")) {
                        shapes.add(shape);
                    }
                    break;

                case ROUNDED:
                    if (shapeName.startsWith("cylinder") ||
                        shapeName.startsWith("anticylinder") ||
                        shapeName.startsWith("sphere")) {
                        shapes.add(shape);
                    }
                    break;

                case PILLARS:
                    if (shapeName.equals("pillar") ||
                        shapeName.equals("post") ||
                        shapeName.equals("pole") ||
                        shapeName.equals("pillar_base")) {
                        shapes.add(shape);
                    }
                    break;

                case CLASSICAL:
                    if (shapeName.contains("capital") ||
                        shapeName.contains("triglyph") ||
                        shapeName.contains("metope") ||
                        shapeName.contains("architrave")) {
                        shapes.add(shape);
                    }
                    break;

                case CORNICES:
                    if (shapeName.startsWith("cornice")) {
                        shapes.add(shape);
                    }
                    break;

                case WINDOWS:
                    if (shapeName.startsWith("window")) {
                        shapes.add(shape);
                    }
                    break;

                case ARCHES:
                    if (shapeName.startsWith("arch_")) {
                        shapes.add(shape);
                    }
                    break;

                case RAILINGS:
                    if (shapeName.startsWith("banister") ||
                        shapeName.startsWith("balustrade")) {
                        shapes.add(shape);
                    }
                    break;

                case OTHER:
                    if (shapeName.equals("cladding_sheet")) {
                        shapes.add(shape);
                    }
                    break;
            }
        }

        return shapes;
    }

    /**
     * Gets the category that a shape belongs to.
     */
    public static ShapeCategory getCategoryForShape(EnumShape shape) {
        for (ShapeCategory category : values()) {
            if (category == ALL) continue;
            if (category.getShapes().contains(shape)) {
                return category;
            }
        }
        return OTHER;
    }
}
