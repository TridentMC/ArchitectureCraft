package com.tridevmc.architecture.common.shape;

import com.tridevmc.architecture.common.shape.placement.ShapePlacementLogic;
import com.tridevmc.architecture.common.shape.placement.ShapePlacementLogicPointedWithSpin;
import com.tridevmc.architecture.common.shape.placement.ShapePlacementLogicSlab;
import com.tridevmc.architecture.common.shape.placement.ShapePlacementLogicStatic;
import com.tridevmc.architecture.common.shape.transformation.IShapeTransformationResolver;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverPointedWithSpin;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverSlab;
import com.tridevmc.architecture.core.math.ITrans3;

public enum EnumShape {

    ROOF_TILE(0, "roof_tile", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_OUTER_CORNER(1, "roof_outer_corner", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_INNER_CORNER(2, "roof_inner_corner", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_RIDGE(3, "roof_ridge", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_SMART_RIDGE(4, "roof_smart_ridge", null, null),
    ROOF_VALLEY(5, "roof_valley", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_SMART_VALLEY(6, "roof_smart_valley", null, null),

    ROOF_OVERHANG(7, "roof_overhang", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_OVERHANG_OUTER_CORNER(8, "roof_overhang_outer_corner", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_OVERHANG_INNER_CORNER(9, "roof_overhang_inner_corner", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),

    CYLINDER(10, "cylinder", null, null),
    CYLINDER_HALF(11, "cylinder_half", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    CYLINDER_QUARTER(12, "cylinder_quarter", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    CYLINDER_LARGE_QUARTER(13, "cylinder_large_quarter", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ANTICYLINDER_LARGE_QUARTER(14, "anticylinder_large_quarter", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    PILLAR(15, "pillar", null, null),
    POST(16, "post", null, null),
    POLE(17, "pole", null, null),

    BEVELLED_OUTER_CORNER(18, "bevelled_outer_corner", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    BEVELLED_INNER_CORNER(19, "bevelled_inner_corner", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),

    PILLAR_BASE(20, "pillar_base", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    DORIC_CAPITAL(21, "doric_capital", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    IONIC_CAPITAL(22, "ionic_capital", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    CORINTHIAN_CAPITAL(23, "corinthian_capital", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    DORIC_TRIGLYPH(24, "doric_triglyph", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    DORIC_TRIGLYPH_CORNER(25, "doric_triglyph_corner", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    DORIC_METOPE(26, "doric_metope", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ARCHITRAVE(27, "architrave", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ARCHITRAVE_CORNER(28, "architrave_corner", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),

    WINDOW_FRAME(30, "window_frame", null, null),
    WINDOW_CORNER(31, "window_corner", null, null),
    WINDOW_MULLION(32, "window_mullion", null, null),

    SPHERE_FULL(33, "sphere_full", ShapePlacementLogicStatic.INSTANCE, (s) -> ITrans3.ofIdentity()),
    SPHERE_HALF(34, "sphere_half", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    SPHERE_QUARTER(35, "sphere_quarter", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    SPHERE_EIGHTH(36, "sphere_eighth", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    SPHERE_EIGHTH_LARGE(37, "sphere_eighth_large", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    SPHERE_EIGHTH_LARGE_REV(38, "sphere_eighth_large_rev", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),

    ROOF_OVERHANG_GABLE_LH(40, "roof_overhang_gable_lh", null, null),
    ROOF_OVERHANG_GABLE_RH(41, "roof_overhang_gable_rh", null, null),
    ROOF_OVERHANG_GABLE_END_LH(42, "roof_overhang_gable_end_lh", null, null),
    ROOF_OVERHANG_GABLE_END_RH(43, "roof_overhang_gable_end_rh", null, null),
    ROOF_OVERHANG_RIDGE(44, "roof_overhang_ridge", null, null),
    ROOF_OVERHANG_VALLEY(45, "roof_overhang_valley", null, null),

    CORNICE_LH(50, "cornice_lh", null, null),
    CORNICE_RH(51, "cornice_rh", null, null),
    CORNICE_END_LH(52, "cornice_end_lh", null, null),
    CORNICE_END_RH(53, "cornice_end_rh", null, null),
    CORNICE_RIDGE(54, "cornice_ridge", null, null),
    CORNICE_VALLEY(55, "cornice_valley", null, null),
    CORNICE_BOTTOM(56, "cornice_bottom", null, null),

    CLADDING_SHEET(60, "cladding_sheet", null, null),

    ARCH_D_1(61, "arch_d_1", null, null),
    ARCH_D_2(62, "arch_d_2", null, null),
    ARCH_D_3_A(63, "arch_d_3_a", null, null),
    ARCH_D_3_B(64, "arch_d_3_b", null, null),
    ARCH_D_3_C(65, "arch_d_3_c", null, null),
    ARCH_D_4_A(66, "arch_d_4_a", null, null),
    ARCH_D_4_B(67, "arch_d_4_b", null, null),
    ARCH_D_4_C(68, "arch_d_4_c", null, null),

    BANISTER_PLAIN_BOTTOM(70, "banister_plain_bottom", null, null),
    BANISTER_PLAIN(71, "banister_plain", null, null),
    BANISTER_PLAIN_TOP(72, "banister_plain_top", null, null),

    BALUSTRADE_FANCY(73, "balustrade_fancy", null, null),
    BALUSTRADE_FANCY_CORNER(74, "balustrade_fancy_corner", null, null),
    BALUSTRADE_FANCY_WITH_NEWEL(75, "balustrade_fancy_with_newel", null, null),
    BALUSTRADE_FANCY_NEWEL(76, "balustrade_fancy_newel", null, null),

    BALUSTRADE_PLAIN(77, "balustrade_plain", null, null),
    BALUSTRADE_PLAIN_OUTER_CORNER(78, "balustrade_plain_outer_corner", null, null),
    BALUSTRADE_PLAIN_WITH_NEWEL(79, "balustrade_plain_with_newel", null, null),

    BANISTER_PLAIN_END(80, "banister_plain_end", null, null),

    BANISTER_FANCY_NEWEL_TALL(81, "banister_fancy_newel_tall", null, null),

    BALUSTRADE_PLAIN_INNER_CORNER(82, "balustrade_plain_inner_corner", null, null),
    BALUSTRADE_PLAIN_END(83, "balustrade_plain_end", null, null),

    BANISTER_FANCY_BOTTOM(84, "banister_fancy_bottom", null, null),
    BANISTER_FANCY(85, "banister_fancy", null, null),
    BANISTER_FANCY_TOP(86, "banister_fancy_top", null, null),
    BANISTER_FANCY_END(87, "banister_fancy_end", null, null),

    BANISTER_PLAIN_INNER_CORNER(88, "banister_plain_inner_corner", null, null),

    SLAB(90, "slab", ShapePlacementLogicSlab.INSTANCE, ShapeTransformationResolverSlab.INSTANCE),
    STAIRS(91, "stairs", null, null),
    STAIRS_OUTER_CORNER(92, "stairs_outer_corner", null, null),
    STAIRS_INNER_CORNER(93, "stairs_inner_corner", null, null);

    private final int id;
    private final String translationKey;
    private final ShapePlacementLogic placementLogic;
    private final IShapeTransformationResolver transformationResolver;

    EnumShape(int id, String translationKey, ShapePlacementLogic placementLogic,
              IShapeTransformationResolver transformationResolver) {
        this.id = id;
        this.translationKey = translationKey;
        this.placementLogic = placementLogic;
        this.transformationResolver = transformationResolver;
    }
}
