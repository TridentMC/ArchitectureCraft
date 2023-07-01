package com.tridevmc.architecture.legacy.common.shape;


import net.minecraft.client.resources.language.I18n;

@Deprecated
public class LegacyShapeGroup {

    public static final LegacyShapeGroup ROOFING = new LegacyShapeGroup(
            "architecturecraft.shapepage.roofing",
            LegacyEnumShape.ROOF_TILE, LegacyEnumShape.ROOF_OUTER_CORNER, LegacyEnumShape.ROOF_INNER_CORNER,
            LegacyEnumShape.ROOF_RIDGE, LegacyEnumShape.ROOF_SMART_RIDGE, LegacyEnumShape.ROOF_VALLEY,
            LegacyEnumShape.ROOF_SMART_VALLEY, LegacyEnumShape.ROOF_OVERHANG, LegacyEnumShape.ROOF_OVERHANG_OUTER_CORNER,
            LegacyEnumShape.ROOF_OVERHANG_INNER_CORNER, LegacyEnumShape.ROOF_OVERHANG_GABLE_LH, LegacyEnumShape.ROOF_OVERHANG_GABLE_RH,
            LegacyEnumShape.ROOF_OVERHANG_GABLE_END_LH, LegacyEnumShape.ROOF_OVERHANG_GABLE_END_RH, LegacyEnumShape.ROOF_OVERHANG_RIDGE,
            LegacyEnumShape.ROOF_OVERHANG_VALLEY, LegacyEnumShape.BEVELLED_OUTER_CORNER, LegacyEnumShape.BEVELLED_INNER_CORNER
    );
    public static final LegacyShapeGroup ROUNDED = new LegacyShapeGroup(
            "architecturecraft.shapepage.rounded",
            LegacyEnumShape.CYLINDER, LegacyEnumShape.CYLINDER_HALF, LegacyEnumShape.CYLINDER_QUARTER, LegacyEnumShape.CYLINDER_LARGE_QUARTER, LegacyEnumShape.ANTICYLINDER_LARGE_QUARTER,
            LegacyEnumShape.PILLAR, LegacyEnumShape.POST, LegacyEnumShape.POLE, LegacyEnumShape.SPHERE_FULL, LegacyEnumShape.SPHERE_HALF,
            LegacyEnumShape.SPHERE_QUARTER, LegacyEnumShape.SPHERE_EIGHTH, LegacyEnumShape.SPHERE_EIGHTH_LARGE, LegacyEnumShape.SPHERE_EIGHTH_LARGE_REV
    );
    public static final LegacyShapeGroup CLASSICAL = new LegacyShapeGroup(
            "architecturecraft.shapepage.classical",
            LegacyEnumShape.PILLAR_BASE, LegacyEnumShape.PILLAR, LegacyEnumShape.DORIC_CAPITAL, LegacyEnumShape.DORIC_TRIGLYPH, LegacyEnumShape.DORIC_TRIGLYPH_CORNER, LegacyEnumShape.DORIC_METOPE,
            LegacyEnumShape.IONIC_CAPITAL, LegacyEnumShape.CORINTHIAN_CAPITAL, LegacyEnumShape.ARCHITRAVE, LegacyEnumShape.ARCHITRAVE_CORNER, LegacyEnumShape.CORNICE_LH, LegacyEnumShape.CORNICE_RH,
            LegacyEnumShape.CORNICE_END_LH, LegacyEnumShape.CORNICE_END_RH, LegacyEnumShape.CORNICE_RIDGE, LegacyEnumShape.CORNICE_VALLEY, LegacyEnumShape.CORNICE_BOTTOM
    );
    public static final LegacyShapeGroup WINDOW = new LegacyShapeGroup(
            "architecturecraft.shapepage.window",
            LegacyEnumShape.WINDOW_FRAME, LegacyEnumShape.WINDOW_CORNER, LegacyEnumShape.WINDOW_MULLION
    );
    public static final LegacyShapeGroup ARCHES = new LegacyShapeGroup(
            "architecturecraft.shapepage.arches",
            LegacyEnumShape.ARCH_D_1, LegacyEnumShape.ARCH_D_2, LegacyEnumShape.ARCH_D_3_A, LegacyEnumShape.ARCH_D_3_B, LegacyEnumShape.ARCH_D_3_C, LegacyEnumShape.ARCH_D_4_A, LegacyEnumShape.ARCH_D_4_B, LegacyEnumShape.ARCH_D_4_C
    );
    public static final LegacyShapeGroup RAILINGS = new LegacyShapeGroup(
            "architecturecraft.shapepage.railings",
            LegacyEnumShape.BALUSTRADE_PLAIN, LegacyEnumShape.BALUSTRADE_PLAIN_OUTER_CORNER, LegacyEnumShape.BALUSTRADE_PLAIN_INNER_CORNER,
            LegacyEnumShape.BALUSTRADE_PLAIN_WITH_NEWEL, LegacyEnumShape.BALUSTRADE_PLAIN_END,
            LegacyEnumShape.BANISTER_PLAIN_TOP, LegacyEnumShape.BANISTER_PLAIN, LegacyEnumShape.BANISTER_PLAIN_BOTTOM, LegacyEnumShape.BANISTER_PLAIN_END, LegacyEnumShape.BANISTER_PLAIN_INNER_CORNER,
            LegacyEnumShape.BALUSTRADE_FANCY, LegacyEnumShape.BALUSTRADE_FANCY_CORNER, LegacyEnumShape.BALUSTRADE_FANCY_WITH_NEWEL, LegacyEnumShape.BALUSTRADE_FANCY_NEWEL,
            LegacyEnumShape.BANISTER_FANCY_TOP, LegacyEnumShape.BANISTER_FANCY, LegacyEnumShape.BANISTER_FANCY_BOTTOM, LegacyEnumShape.BANISTER_FANCY_END, LegacyEnumShape.BANISTER_FANCY_NEWEL_TALL);
    public static final LegacyShapeGroup OTHER = new LegacyShapeGroup(
            "architecturecraft.shapepage.other",
            LegacyEnumShape.CLADDING_SHEET, LegacyEnumShape.SLAB, LegacyEnumShape.STAIRS, LegacyEnumShape.STAIRS_OUTER_CORNER, LegacyEnumShape.STAIRS_INNER_CORNER
    );

    private final String translationKey;
    private final LegacyEnumShape[] members;

    public LegacyShapeGroup(String translationKey, LegacyEnumShape... members) {
        this.translationKey = translationKey;
        this.members = members;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }

    public LegacyEnumShape[] getMembers() {
        return this.members;
    }

    public LegacyEnumShape getShape(int index) {
        if (index < 0 || index >= this.size()) {
            return null;
        }
        return this.members[index];
    }

    public int size() {
        return this.members.length;
    }

    public String getLocalizedName() {
        return I18n.get(this.translationKey);
    }
}
