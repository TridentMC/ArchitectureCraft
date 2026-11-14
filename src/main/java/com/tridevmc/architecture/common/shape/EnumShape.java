package com.tridevmc.architecture.common.shape;

import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.block.BlockArchitecture;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyRelativeConnection;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyTurn;
import com.tridevmc.architecture.common.shape.placement.IShapePlacementLogic;
import com.tridevmc.architecture.common.shape.placement.ShapeConnectionProperties;
import com.tridevmc.architecture.common.shape.placement.ShapePlacementLogicBanister;
import com.tridevmc.architecture.common.shape.placement.ShapePlacementLogicCladding;
import com.tridevmc.architecture.common.shape.placement.ShapePlacementLogicExtended;
import com.tridevmc.architecture.common.shape.placement.ShapePlacementLogicFacingWithFlip;
import com.tridevmc.architecture.common.shape.placement.ShapePlacementLogicFacingWithFlipAndTurn;
import com.tridevmc.architecture.common.shape.placement.ShapePlacementLogicFacingWithTurn;
import com.tridevmc.architecture.common.shape.placement.ShapePlacementLogicOnAxis;
import com.tridevmc.architecture.common.shape.placement.ShapePlacementLogicPointedByPlacedFace;
import com.tridevmc.architecture.common.shape.placement.ShapePlacementLogicSlab;
import com.tridevmc.architecture.common.shape.placement.ShapePlacementLogicStairLike;
import com.tridevmc.architecture.common.shape.placement.ShapePlacementLogicStatic;
import com.tridevmc.architecture.common.shape.placement.ShapePlacementLogicWindowCorner;
import com.tridevmc.architecture.common.shape.placement.ShapePlacementLogicWindowFrame;
import com.tridevmc.architecture.common.shape.transformation.IShapeTransformationResolver;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverBanister;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverCladding;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverFacingWithFlip;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverFacingWithFlipAndTurn;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverFacingWithTurn;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverOnAxis;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverPointed;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverPointedWithSpin;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverSlab;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverWindow;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import com.tridevmc.architecture.core.model.voxelize.IVoxelizer;

import net.minecraft.resources.Identifier;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum EnumShape {

    SLAB("slab", ShapePlacementLogicSlab.INSTANCE, ShapeTransformationResolverSlab.INSTANCE),

    STAIRS("stairs", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    STAIRS_OUTER_CORNER("stairs_outer_corner", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    STAIRS_INNER_CORNER("stairs_inner_corner", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    STAIRS_SMART("stairs_smart", new ShapePlacementLogicExtended<>(ShapePlacementLogicStairLike.INSTANCE, ShapeConnectionProperties.STAIRS), ShapeTransformationResolverPointedWithSpin.INSTANCE),

    PILLAR("pillar", ShapePlacementLogicOnAxis.INSTANCE, ShapeTransformationResolverOnAxis.INSTANCE),
    POST("post", ShapePlacementLogicOnAxis.INSTANCE, ShapeTransformationResolverOnAxis.INSTANCE),
    POLE("pole", ShapePlacementLogicOnAxis.INSTANCE, ShapeTransformationResolverOnAxis.INSTANCE),

    CYLINDER("cylinder", ShapePlacementLogicOnAxis.INSTANCE, ShapeTransformationResolverOnAxis.INSTANCE),
    CYLINDER_HALF("cylinder_half", ShapePlacementLogicFacingWithFlipAndTurn.INSTANCE, ShapeTransformationResolverFacingWithFlipAndTurn.INSTANCE),
    CYLINDER_QUARTER("cylinder_quarter", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    CYLINDER_LARGE_QUARTER("cylinder_large_quarter", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ANTICYLINDER_LARGE_QUARTER("anticylinder_large_quarter", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),

    SPHERE_FULL("sphere_full", ShapePlacementLogicStatic.INSTANCE, (s) -> ITrans3.ofIdentity()),
    SPHERE_HALF("sphere_half", ShapePlacementLogicFacingWithFlip.INSTANCE, ShapeTransformationResolverFacingWithFlip.INSTANCE),
    SPHERE_QUARTER("sphere_quarter", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    SPHERE_EIGHTH("sphere_eighth", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    SPHERE_EIGHTH_LARGE("sphere_eighth_large", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    SPHERE_EIGHTH_LARGE_REV("sphere_eighth_large_rev", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),

    BEVELLED_OUTER_CORNER("bevelled_outer_corner", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    BEVELLED_INNER_CORNER("bevelled_inner_corner", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),

    ROOF_TILE("roof_tile", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_OUTER_CORNER("roof_outer_corner", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_INNER_CORNER("roof_inner_corner", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_RIDGE("roof_ridge", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_SMART_RIDGE("roof_smart_ridge", new ShapePlacementLogicExtended<>(ShapePlacementLogicStairLike.INSTANCE, ShapeOrientationPropertyRelativeConnection.FRONT, ShapeOrientationPropertyRelativeConnection.BACK, ShapeOrientationPropertyRelativeConnection.LEFT, ShapeOrientationPropertyRelativeConnection.RIGHT), ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_VALLEY("roof_valley", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_SMART_VALLEY("roof_smart_valley", new ShapePlacementLogicExtended<>(ShapePlacementLogicStairLike.INSTANCE, ShapeOrientationPropertyRelativeConnection.FRONT, ShapeOrientationPropertyRelativeConnection.BACK, ShapeOrientationPropertyRelativeConnection.LEFT, ShapeOrientationPropertyRelativeConnection.RIGHT), ShapeTransformationResolverPointedWithSpin.INSTANCE),

    ROOF_OVERHANG("roof_overhang", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_OVERHANG_OUTER_CORNER("roof_overhang_outer_corner", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_OVERHANG_INNER_CORNER("roof_overhang_inner_corner", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_OVERHANG_GABLE_LH("roof_overhang_gable_lh", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_OVERHANG_GABLE_RH("roof_overhang_gable_rh", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_OVERHANG_GABLE_END_LH("roof_overhang_gable_end_lh", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_OVERHANG_GABLE_END_RH("roof_overhang_gable_end_rh", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_OVERHANG_RIDGE("roof_overhang_ridge", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ROOF_OVERHANG_VALLEY("roof_overhang_valley", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),

    PILLAR_BASE("pillar_base", ShapePlacementLogicPointedByPlacedFace.INSTANCE, ShapeTransformationResolverPointed.INSTANCE),
    DORIC_CAPITAL("doric_capital", ShapePlacementLogicPointedByPlacedFace.INSTANCE, ShapeTransformationResolverPointed.INSTANCE),
    IONIC_CAPITAL("ionic_capital", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    CORINTHIAN_CAPITAL("corinthian_capital", ShapePlacementLogicPointedByPlacedFace.INSTANCE, ShapeTransformationResolverPointed.INSTANCE),
    DORIC_TRIGLYPH("doric_triglyph", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    DORIC_TRIGLYPH_CORNER("doric_triglyph_corner", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    DORIC_METOPE("doric_metope", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ARCHITRAVE("architrave", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ARCHITRAVE_CORNER("architrave_corner", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),

    CORNICE_LH("cornice_lh", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    CORNICE_RH("cornice_rh", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    CORNICE_END_LH("cornice_end_lh", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    CORNICE_END_RH("cornice_end_rh", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    CORNICE_RIDGE("cornice_ridge", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    CORNICE_VALLEY("cornice_valley", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    CORNICE_BOTTOM("cornice_bottom", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),

    WINDOW_FRAME("window_frame", new ShapePlacementLogicExtended<>(new ShapePlacementLogicWindowFrame<>(), ShapeConnectionProperties.DOWN_LEFT, ShapeConnectionProperties.DOWN_RIGHT, ShapeConnectionProperties.UP_LEFT, ShapeConnectionProperties.UP_RIGHT), ShapeTransformationResolverWindow.FRAME),
    WINDOW_CORNER("window_corner", new ShapePlacementLogicExtended<>(new ShapePlacementLogicWindowCorner<>(), ShapeConnectionProperties.DOWN_LEFT, ShapeConnectionProperties.DOWN_RIGHT, ShapeConnectionProperties.UP_LEFT, ShapeConnectionProperties.UP_RIGHT), ShapeTransformationResolverWindow.CORNER),
    WINDOW_MULLION("window_mullion", new ShapePlacementLogicExtended<>(new ShapePlacementLogicWindowFrame<>(), ShapeConnectionProperties.DOWN_LEFT, ShapeConnectionProperties.DOWN_RIGHT, ShapeConnectionProperties.UP_LEFT, ShapeConnectionProperties.UP_RIGHT, ShapeOrientationPropertyTurn.INSTANCE), ShapeTransformationResolverWindow.FRAME),
    WINDOW_SMART("window_smart", new ShapePlacementLogicExtended<>(new ShapePlacementLogicWindowFrame<>(), ShapeConnectionProperties.DOWN_LEFT, ShapeConnectionProperties.DOWN_RIGHT, ShapeConnectionProperties.UP_LEFT, ShapeConnectionProperties.UP_RIGHT), ShapeTransformationResolverWindow.FRAME),
    WINDOW_MULLION_SMART("window_mullion_smart", new ShapePlacementLogicExtended<>(new ShapePlacementLogicWindowFrame<>(), ShapeConnectionProperties.DOWN_LEFT, ShapeConnectionProperties.DOWN_RIGHT, ShapeConnectionProperties.UP_LEFT, ShapeConnectionProperties.UP_RIGHT, ShapeOrientationPropertyTurn.INSTANCE), ShapeTransformationResolverWindow.FRAME),

    ARCH_D1("arch_d1", ShapePlacementLogicFacingWithTurn.INSTANCE, ShapeTransformationResolverFacingWithTurn.INSTANCE),
    ARCH_D2("arch_d2", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ARCH_D3A("arch_d3a", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ARCH_D3B("arch_d3b", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ARCH_D3C("arch_d3c", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ARCH_D4A("arch_d4a", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ARCH_D4B("arch_d4b", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    ARCH_D4C("arch_d4c", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),

    BANISTER_PLAIN_BOTTOM("banister_plain_bottom", ShapePlacementLogicBanister.INSTANCE, ShapeTransformationResolverBanister.INSTANCE),
    BANISTER_PLAIN("banister_plain", ShapePlacementLogicBanister.INSTANCE, ShapeTransformationResolverBanister.INSTANCE),
    BANISTER_PLAIN_TOP("banister_plain_top", ShapePlacementLogicBanister.INSTANCE, ShapeTransformationResolverBanister.INSTANCE),
    BANISTER_PLAIN_END("banister_plain_end", ShapePlacementLogicBanister.INSTANCE, ShapeTransformationResolverBanister.INSTANCE),
    BANISTER_PLAIN_INNER_CORNER("banister_plain_inner_corner", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),

    BANISTER_FANCY_BOTTOM("banister_fancy_bottom", ShapePlacementLogicBanister.INSTANCE, ShapeTransformationResolverBanister.INSTANCE),
    BANISTER_FANCY("banister_fancy", ShapePlacementLogicBanister.INSTANCE, ShapeTransformationResolverBanister.INSTANCE),
    BANISTER_FANCY_TOP("banister_fancy_top", ShapePlacementLogicBanister.INSTANCE, ShapeTransformationResolverBanister.INSTANCE),
    BANISTER_FANCY_END("banister_fancy_end", ShapePlacementLogicBanister.INSTANCE, ShapeTransformationResolverBanister.INSTANCE),
    BANISTER_FANCY_NEWEL_TALL("banister_fancy_newel_tall", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),

    BALUSTRADE_PLAIN("balustrade_plain", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    BALUSTRADE_PLAIN_OUTER_CORNER("balustrade_plain_outer_corner", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    BALUSTRADE_PLAIN_INNER_CORNER("balustrade_plain_inner_corner", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    BALUSTRADE_PLAIN_WITH_NEWEL("balustrade_plain_with_newel", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    BALUSTRADE_PLAIN_END("balustrade_plain_end", ShapePlacementLogicBanister.INSTANCE, ShapeTransformationResolverBanister.INSTANCE),

    BALUSTRADE_FANCY("balustrade_fancy", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    BALUSTRADE_FANCY_OUTER_CORNER("balustrade_fancy_outer_corner", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    BALUSTRADE_FANCY_WITH_NEWEL("balustrade_fancy_with_newel", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),
    BALUSTRADE_FANCY_NEWEL("balustrade_fancy_newel", ShapePlacementLogicStairLike.INSTANCE, ShapeTransformationResolverPointedWithSpin.INSTANCE),

    CLADDING_SHEET("cladding_sheet", ShapePlacementLogicCladding.INSTANCE, ShapeTransformationResolverCladding.INSTANCE);


    private static final Map<String, EnumShape> NAME_LOOKUP = Arrays.stream(values())
            .collect(Collectors.toMap(EnumShape::getName, Function.identity()));
    private static final Map<Identifier, EnumShape> ID_LOOKUP = Arrays.stream(values())
            .collect(Collectors.toMap(EnumShape::getId, Function.identity()));
    private final String name;
    private final String localizationKey;
    private final Identifier id;
    private final IShapePlacementLogic<?> placementLogic;
    private final IShapeTransformationResolver transformationResolver;

    EnumShape(String name, IShapePlacementLogic<?> placementLogic,
              IShapeTransformationResolver transformationResolver) {
        this.name = name;
        this.localizationKey = String.format("shape.%s.%s", ArchitectureMod.MOD_ID, name);
        this.id = Identifier.fromNamespaceAndPath(ArchitectureMod.MOD_ID, String.format("shape/%s", name));
        this.placementLogic = Objects.requireNonNull(placementLogic, name + " placement");
        this.transformationResolver = Objects.requireNonNull(transformationResolver, name + " transform");
    }

    /**
     * Gets the EnumShape with the given name.
     *
     * @param name The name of the EnumShape.
     * @return The EnumShape with the given name.
     * @throws NullPointerException if there is no EnumShape with the given name.
     */
    public static EnumShape byName(String name) {
        var shape = NAME_LOOKUP.get(name);
        return Objects.requireNonNull(shape, "No shape with name " + name);
    }

    /**
     * Gets the EnumShape with the given id.
     *
     * @param id The id of the EnumShape.
     * @return The EnumShape with the given id.
     * @throws NullPointerException if there is no EnumShape with the given id.
     */
    public static EnumShape byId(Identifier id) {
        var shape = ID_LOOKUP.get(id);
        return Objects.requireNonNull(shape, "No shape with id " + id);
    }

    public String getName() {
        return this.name;
    }

    public Identifier getId() {
        return this.id;
    }

    public Identifier getAssetLocation() {
        return Identifier.fromNamespaceAndPath(ArchitectureMod.MOD_ID, String.format("shape/%s.objson", this.name));
    }

    public String getLocalizationKey() {
        return this.localizationKey;
    }

    public <T extends BlockArchitecture> IShapePlacementLogic<T> getPlacementLogic() {
        // We're just using the minimum bounds of the interface so this is perfectly safe
        // noinspection unchecked
        return (IShapePlacementLogic<T>) this.placementLogic;
    }

    public IShapeTransformationResolver getTransformationResolver() {
        return this.transformationResolver;
    }

    public IMesh<String, PolygonData> getMesh() {
        return ShapeMeshes.getMesh(this);
    }

    public IVoxelizer getVoxelizer() {
        return ShapeMeshes.getVoxelizer(this);
    }
}
