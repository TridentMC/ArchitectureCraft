package com.tridevmc.architecture.common.shape;

import net.minecraft.client.resources.I18n;

public class ShapeGroup {

    public static final ShapeGroup ROOFING = new ShapeGroup(
            "architecturecraft.shapepage.roofing",
            EnumShape.ROOF_TILE, EnumShape.ROOF_OUTER_CORNER, EnumShape.ROOF_INNER_CORNER,
            EnumShape.ROOF_RIDGE, EnumShape.ROOF_SMART_RIDGE, EnumShape.ROOF_VALLEY,
            EnumShape.ROOF_SMART_VALLEY, EnumShape.ROOF_OVERHANG, EnumShape.ROOF_OVERHANG_OUTER_CORNER,
            EnumShape.ROOF_OVERHANG_INNER_CORNER, EnumShape.ROOF_OVERHANG_GABLE_LH, EnumShape.ROOF_OVERHANG_GABLE_RH,
            EnumShape.ROOF_OVERHANG_GABLE_END_LH, EnumShape.ROOF_OVERHANG_GABLE_END_RH, EnumShape.ROOF_OVERHANG_RIDGE,
            EnumShape.ROOF_OVERHANG_VALLEY, EnumShape.BEVELLED_OUTER_CORNER, EnumShape.BEVELLED_INNER_CORNER
    );
    public static final ShapeGroup ROUNDED = new ShapeGroup(
            "architecturecraft.shapepage.rounded",
            EnumShape.CYLINDER, EnumShape.CYLINDER_HALF, EnumShape.CYLINDER_QUARTER, EnumShape.CYLINDER_LARGE_QUARTER, EnumShape.ANTICYLINDER_LARGE_QUARTER,
            EnumShape.PILLAR, EnumShape.POST, EnumShape.POLE, EnumShape.SPHERE_FULL, EnumShape.SPHERE_HALF,
            EnumShape.SPHERE_QUARTER, EnumShape.SPHERE_EIGHTH, EnumShape.SPHERE_EIGHTH_LARGE, EnumShape.SPHERE_EIGHTH_LARGE_REV
    );
    public static final ShapeGroup CLASSICAL = new ShapeGroup(
            "architecturecraft.shapepage.classical",
            EnumShape.PILLAR_BASE, EnumShape.PILLAR, EnumShape.DORIC_CAPITAL, EnumShape.DORIC_TRIGLYPH, EnumShape.DORIC_TRIGLYPH_CORNER, EnumShape.DORIC_METOPE,
            EnumShape.IONIC_CAPITAL, EnumShape.CORINTHIAN_CAPITAL, EnumShape.ARCHITRAVE, EnumShape.ARCHITRAVE_CORNER, EnumShape.CORNICE_LH, EnumShape.CORNICE_RH,
            EnumShape.CORNICE_END_LH, EnumShape.CORNICE_END_RH, EnumShape.CORNICE_RIDGE, EnumShape.CORNICE_VALLEY, EnumShape.CORNICE_BOTTOM
    );
    public static final ShapeGroup WINDOW = new ShapeGroup(
            "architecturecraft.shapepage.window",
            EnumShape.WINDOW_FRAME, EnumShape.WINDOW_CORNER, EnumShape.WINDOW_MULLION
    );
    public static final ShapeGroup ARCHES = new ShapeGroup(
            "architecturecraft.shapepage.arches",
            EnumShape.ARCH_D_1, EnumShape.ARCH_D_2, EnumShape.ARCH_D_3_A, EnumShape.ARCH_D_3_B, EnumShape.ARCH_D_3_C, EnumShape.ARCH_D_4_A, EnumShape.ARCH_D_4_B, EnumShape.ARCH_D_4_C
    );
    public static final ShapeGroup RAILINGS = new ShapeGroup(
            "architecturecraft.shapepage.railings",
            EnumShape.BALUSTRADE_PLAIN, EnumShape.BALUSTRADE_PLAIN_OUTER_CORNER, EnumShape.BALUSTRADE_PLAIN_INNER_CORNER,
            EnumShape.BALUSTRADE_PLAIN_WITH_NEWEL, EnumShape.BALUSTRADE_PLAIN_END,
            EnumShape.BANISTER_PLAIN_TOP, EnumShape.BANISTER_PLAIN, EnumShape.BANISTER_PLAIN_BOTTOM, EnumShape.BANISTER_PLAIN_END, EnumShape.BANISTER_PLAIN_INNER_CORNER,
            EnumShape.BALUSTRADE_FANCY, EnumShape.BALUSTRADE_FANCY_CORNER, EnumShape.BALUSTRADE_FANCY_WITH_NEWEL, EnumShape.BALUSTRADE_FANCY_NEWEL,
            EnumShape.BANISTER_FANCY_TOP, EnumShape.BANISTER_FANCY, EnumShape.BANISTER_FANCY_BOTTOM, EnumShape.BANISTER_FANCY_END, EnumShape.BANISTER_FANCY_NEWEL_TALL);
    public static final ShapeGroup OTHER = new ShapeGroup(
            "architecturecraft.shapepage.other",
            EnumShape.CLADDING_SHEET, EnumShape.SLAB, EnumShape.STAIRS, EnumShape.STAIRS_OUTER_CORNER, EnumShape.STAIRS_INNER_CORNER
    );

    private final String translationKey;
    private final EnumShape[] members;

    public ShapeGroup(String translationKey, EnumShape... members) {
        this.translationKey = translationKey;
        this.members = members;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }

    public EnumShape[] getMembers() {
        return this.members;
    }

    public EnumShape getShape(int index) {
        if (index < 0 || index >= this.size()) {
            return null;
        }
        return this.members[index];
    }

    public int size() {
        return this.members.length;
    }

    public String getLocalizedName() {
        return I18n.format(this.translationKey);
    }
}
