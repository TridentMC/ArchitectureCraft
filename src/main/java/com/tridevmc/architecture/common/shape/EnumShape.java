package com.tridevmc.architecture.common.shape;

import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.placement.*;
import com.tridevmc.architecture.common.shape.transformation.IShapeTransformationResolver;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverOnAxis;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverPointedWithSpin;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverSlab;
import com.tridevmc.architecture.core.math.ITrans3;
import net.minecraft.resources.ResourceLocation;

public enum EnumShape {

    ROOF_TILE("roof_tile", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_OUTER_CORNER("roof_outer_corner", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_INNER_CORNER("roof_inner_corner", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_RIDGE("roof_ridge", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_SMART_RIDGE("roof_smart_ridge", null, null),
    ROOF_VALLEY("roof_valley", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_SMART_VALLEY("roof_smart_valley", null, null),

    ROOF_OVERHANG("roof_overhang", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_OVERHANG_OUTER_CORNER("roof_overhang_outer_corner", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_OVERHANG_INNER_CORNER("roof_overhang_inner_corner", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),

    CYLINDER("cylinder", ShapePlacementLogicOnAxis.INSTANCE, ShapeTransformationResolverOnAxis.INSTANCE),
    CYLINDER_HALF("cylinder_half", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    CYLINDER_QUARTER("cylinder_quarter", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    CYLINDER_LARGE_QUARTER("cylinder_large_quarter", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ANTICYLINDER_LARGE_QUARTER("anticylinder_large_quarter", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    PILLAR("pillar", ShapePlacementLogicOnAxis.INSTANCE, ShapeTransformationResolverOnAxis.INSTANCE),
    POST("post", ShapePlacementLogicOnAxis.INSTANCE, ShapeTransformationResolverOnAxis.INSTANCE),
    POLE("pole", ShapePlacementLogicOnAxis.INSTANCE, ShapeTransformationResolverOnAxis.INSTANCE),

    BEVELLED_OUTER_CORNER("bevelled_outer_corner", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    BEVELLED_INNER_CORNER("bevelled_inner_corner", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),

    PILLAR_BASE("pillar_base", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    DORIC_CAPITAL("doric_capital", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    IONIC_CAPITAL("ionic_capital", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    CORINTHIAN_CAPITAL("corinthian_capital", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    DORIC_TRIGLYPH("doric_triglyph", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    DORIC_TRIGLYPH_CORNER("doric_triglyph_corner", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    DORIC_METOPE("doric_metope", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ARCHITRAVE("architrave", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ARCHITRAVE_CORNER("architrave_corner", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),

    WINDOW_FRAME("window_frame", null, null),
    WINDOW_CORNER("window_corner", null, null),
    WINDOW_MULLION("window_mullion", null, null),
    WINDOW_SMART("window_smart", new ShapePlacementLogicWindow<>(), (s) -> ITrans3.ofIdentity()), // TODO: Use window class for placement logic instead of empty diamond
    WINDOW_MULLION_SMART("window_mullion_smart", new ShapePlacementLogicWindow<>(), (s) -> ITrans3.ofIdentity()), // TODO: Use window class for placement logic instead of empty diamond

    SPHERE_FULL("sphere_full", ShapePlacementLogicStatic.INSTANCE, (s) -> ITrans3.ofIdentity()),
    SPHERE_HALF("sphere_half", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    SPHERE_QUARTER("sphere_quarter", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    SPHERE_EIGHTH("sphere_eighth", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    SPHERE_EIGHTH_LARGE("sphere_eighth_large", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    SPHERE_EIGHTH_LARGE_REV("sphere_eighth_large_rev", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),

    ROOF_OVERHANG_GABLE_LH("roof_overhang_gable_lh", null, null),
    ROOF_OVERHANG_GABLE_RH("roof_overhang_gable_rh", null, null),
    ROOF_OVERHANG_GABLE_END_LH("roof_overhang_gable_end_lh", null, null),
    ROOF_OVERHANG_GABLE_END_RH("roof_overhang_gable_end_rh", null, null),
    ROOF_OVERHANG_RIDGE("roof_overhang_ridge", null, null),
    ROOF_OVERHANG_VALLEY("roof_overhang_valley", null, null),

    CORNICE_LH("cornice_lh", null, null),
    CORNICE_RH("cornice_rh", null, null),
    CORNICE_END_LH("cornice_end_lh", null, null),
    CORNICE_END_RH("cornice_end_rh", null, null),
    CORNICE_RIDGE("cornice_ridge", null, null),
    CORNICE_VALLEY("cornice_valley", null, null),
    CORNICE_BOTTOM("cornice_bottom", null, null),

    CLADDING_SHEET("cladding_sheet", null, null),

    ARCH_D_1("arch_d_1", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ARCH_D_2("arch_d_2", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ARCH_D_3_A("arch_d_3_a", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ARCH_D_3_B("arch_d_3_b", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ARCH_D_3_C("arch_d_3_c", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ARCH_D_4_A("arch_d_4_a", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ARCH_D_4_B("arch_d_4_b", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ARCH_D_4_C("arch_d_4_c", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),

    BANISTER_PLAIN_BOTTOM("banister_plain_bottom", null, null),
    BANISTER_PLAIN("banister_plain", null, null),
    BANISTER_PLAIN_TOP("banister_plain_top", null, null),

    BALUSTRADE_FANCY("balustrade_fancy", null, null),
    BALUSTRADE_FANCY_CORNER("balustrade_fancy_corner", null, null),
    BALUSTRADE_FANCY_WITH_NEWEL("balustrade_fancy_with_newel", null, null),
    BALUSTRADE_FANCY_NEWEL("balustrade_fancy_newel", null, null),

    BALUSTRADE_PLAIN("balustrade_plain", null, null),
    BALUSTRADE_PLAIN_OUTER_CORNER("balustrade_plain_outer_corner", null, null),
    BALUSTRADE_PLAIN_WITH_NEWEL("balustrade_plain_with_newel", null, null),

    BANISTER_PLAIN_END("banister_plain_end", null, null),

    BANISTER_FANCY_NEWEL_TALL("banister_fancy_newel_tall", null, null),

    BALUSTRADE_PLAIN_INNER_CORNER("balustrade_plain_inner_corner", null, null),
    BALUSTRADE_PLAIN_END("balustrade_plain_end", null, null),

    BANISTER_FANCY_BOTTOM("banister_fancy_bottom", null, null),
    BANISTER_FANCY("banister_fancy", null, null),
    BANISTER_FANCY_TOP("banister_fancy_top", null, null),
    BANISTER_FANCY_END("banister_fancy_end", null, null),

    BANISTER_PLAIN_INNER_CORNER("banister_plain_inner_corner", null, null),

    SLAB("slab", ShapePlacementLogicSlab.INSTANCE, ShapeTransformationResolverSlab.INSTANCE),
    STAIRS("stairs", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    STAIRS_OUTER_CORNER("stairs_outer_corner", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    STAIRS_INNER_CORNER("stairs_inner_corner", ShapePlacementLogicPointedWithSpin.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    STAIRS_SMART("stairs_smart", null, null);

    private final ResourceLocation id;
    private final String translationKey;
    private final IShapePlacementLogic<?> placementLogic;
    private final IShapeTransformationResolver transformationResolver;

    EnumShape(String name, IShapePlacementLogic<?> placementLogic,
              IShapeTransformationResolver transformationResolver) {
        this.id = new ResourceLocation(ArchitectureMod.MOD_ID, String.format("shape/%s", name));
        this.translationKey = name;
        this.placementLogic = placementLogic;
        this.transformationResolver = transformationResolver;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }

    public <T extends BlockArchitecture> IShapePlacementLogic<T> getPlacementLogic() {
        // We're just using the minimum bounds of the interface so this is perfectly safe
        // noinspection unchecked
        return (IShapePlacementLogic<T>) this.placementLogic;
    }

    public IShapeTransformationResolver getTransformationResolver() {
        return this.transformationResolver;
    }
}
