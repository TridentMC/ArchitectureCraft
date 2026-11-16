package com.tridevmc.architecture.common.shape;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Locale;

public enum EnumShapeCategory {
    ALL(EnumShape.values()),
    BASIC(EnumShape.SLAB, EnumShape.BEVELLED_OUTER_CORNER, EnumShape.BEVELLED_INNER_CORNER),
    STAIRS(EnumShape.STAIRS, EnumShape.STAIRS_OUTER_CORNER, EnumShape.STAIRS_INNER_CORNER,
            EnumShape.STAIRS_SMART),
    ROOFS(EnumShape.ROOF_TILE, EnumShape.ROOF_OUTER_CORNER, EnumShape.ROOF_INNER_CORNER,
            EnumShape.ROOF_RIDGE, EnumShape.ROOF_SMART_RIDGE, EnumShape.ROOF_VALLEY,
            EnumShape.ROOF_SMART_VALLEY, EnumShape.ROOF_OVERHANG,
            EnumShape.ROOF_OVERHANG_OUTER_CORNER, EnumShape.ROOF_OVERHANG_INNER_CORNER,
            EnumShape.ROOF_OVERHANG_GABLE_LH, EnumShape.ROOF_OVERHANG_GABLE_RH,
            EnumShape.ROOF_OVERHANG_GABLE_END_LH, EnumShape.ROOF_OVERHANG_GABLE_END_RH,
            EnumShape.ROOF_OVERHANG_RIDGE, EnumShape.ROOF_OVERHANG_VALLEY),
    ROUNDED(EnumShape.CYLINDER, EnumShape.CYLINDER_HALF, EnumShape.CYLINDER_QUARTER,
            EnumShape.CYLINDER_LARGE_QUARTER, EnumShape.ANTICYLINDER_LARGE_QUARTER,
            EnumShape.SPHERE_FULL, EnumShape.SPHERE_HALF, EnumShape.SPHERE_QUARTER,
            EnumShape.SPHERE_EIGHTH, EnumShape.SPHERE_EIGHTH_LARGE,
            EnumShape.SPHERE_EIGHTH_LARGE_REV),
    PILLARS(EnumShape.PILLAR, EnumShape.POST, EnumShape.POLE, EnumShape.PILLAR_BASE),
    CLASSICAL(EnumShape.DORIC_CAPITAL, EnumShape.IONIC_CAPITAL, EnumShape.CORINTHIAN_CAPITAL,
            EnumShape.DORIC_TRIGLYPH, EnumShape.DORIC_TRIGLYPH_CORNER, EnumShape.DORIC_METOPE,
            EnumShape.ARCHITRAVE, EnumShape.ARCHITRAVE_CORNER),
    CORNICES(EnumShape.CORNICE_LH, EnumShape.CORNICE_RH, EnumShape.CORNICE_END_LH,
            EnumShape.CORNICE_END_RH, EnumShape.CORNICE_RIDGE, EnumShape.CORNICE_VALLEY,
            EnumShape.CORNICE_BOTTOM),
    WINDOWS(EnumShape.WINDOW_FRAME, EnumShape.WINDOW_CORNER, EnumShape.WINDOW_MULLION,
            EnumShape.WINDOW_SMART, EnumShape.WINDOW_MULLION_SMART),
    ARCHES(EnumShape.ARCH_D1, EnumShape.ARCH_D2, EnumShape.ARCH_D3A, EnumShape.ARCH_D3B,
            EnumShape.ARCH_D3C, EnumShape.ARCH_D4A, EnumShape.ARCH_D4B, EnumShape.ARCH_D4C),
    RAILINGS(EnumShape.BANISTER_PLAIN_BOTTOM, EnumShape.BANISTER_PLAIN,
            EnumShape.BANISTER_PLAIN_TOP, EnumShape.BANISTER_PLAIN_END,
            EnumShape.BANISTER_PLAIN_INNER_CORNER, EnumShape.BANISTER_FANCY_BOTTOM,
            EnumShape.BANISTER_FANCY, EnumShape.BANISTER_FANCY_TOP, EnumShape.BANISTER_FANCY_END,
            EnumShape.BANISTER_FANCY_NEWEL_TALL, EnumShape.BALUSTRADE_PLAIN,
            EnumShape.BALUSTRADE_PLAIN_OUTER_CORNER, EnumShape.BALUSTRADE_PLAIN_INNER_CORNER,
            EnumShape.BALUSTRADE_PLAIN_WITH_NEWEL, EnumShape.BALUSTRADE_PLAIN_END,
            EnumShape.BALUSTRADE_FANCY, EnumShape.BALUSTRADE_FANCY_OUTER_CORNER,
            EnumShape.BALUSTRADE_FANCY_WITH_NEWEL, EnumShape.BALUSTRADE_FANCY_NEWEL),
    OTHER(EnumShape.CLADDING_SHEET);

    private final List<EnumShape> shapes;

    EnumShapeCategory(EnumShape... shapes) {
        this.shapes = ImmutableList.copyOf(shapes);
    }

    public String getTranslationKey() {
        return "shape_category.architecturecraft." + this.name().toLowerCase(Locale.ROOT);
    }

    public List<EnumShape> getShapes() {
        return this.shapes.stream()
                .filter(shape -> shape.getPlacementLogic() != null
                        && shape.getTransformationResolver() != null && shape.getMesh() != null)
                .toList();
    }
}
