/*
 * MIT License
 *
 * Copyright (c) 2017 Benjamin K
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.tridevmc.architecture.legacy.common.shape;

import com.google.common.collect.ImmutableMap;
import com.tridevmc.architecture.common.helpers.Profile;
import com.tridevmc.architecture.legacy.common.block.entity.LegacyShapeBlockEntity;
import com.tridevmc.architecture.legacy.common.shape.behaviour.*;
import com.tridevmc.architecture.legacy.math.LegacyTrans3;
import com.tridevmc.architecture.legacy.math.LegacyVector3;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.Map;

import static com.tridevmc.architecture.legacy.common.shape.LegacyShapeFlags.PLACE_OFFSET;
import static com.tridevmc.architecture.legacy.common.shape.LegacyShapeFlags.PLACE_UNDERNEATH;
import static com.tridevmc.architecture.legacy.common.shape.LegacyShapeSymmetry.*;
import static java.lang.Math.abs;

@Deprecated
public enum LegacyEnumShape implements StringRepresentable {

    ROOF_TILE(0, "roof_tile", LegacyShapeBehaviourRoof.INSTANCE, BILATERAL, 1, 2, 0xcf),
    ROOF_OUTER_CORNER(1, "roof_outer_corner", LegacyShapeBehaviourRoof.INSTANCE, UNILATERAL, 1, 3, 0x4f),
    ROOF_INNER_CORNER(2, "roof_inner_corner", LegacyShapeBehaviourRoof.INSTANCE, UNILATERAL, 2, 3, 0xdf),
    ROOF_RIDGE(3, "roof_ridge", LegacyShapeBehaviourRoof.INSTANCE, BILATERAL, 1, 4, 0x0f),
    ROOF_SMART_RIDGE(4, "roof_smart_ridge", LegacyShapeBehaviourRoof.INSTANCE, QUADRILATERAL, 1, 2, 0x0f),
    ROOF_VALLEY(5, "roof_valley", LegacyShapeBehaviourRoof.INSTANCE, BILATERAL, 1, 2, 0xff),
    ROOF_SMART_VALLEY(6, "roof_smart_valley", LegacyShapeBehaviourRoof.INSTANCE, QUADRILATERAL, 1, 1, 0xff),

    ROOF_OVERHANG(7, "roof_overhang", new LegacyShapeBehaviourModel("roof_overhang"), BILATERAL, 1, 2, 0xcf),
    ROOF_OVERHANG_OUTER_CORNER(8, "roof_overhang_outer_corner", new LegacyShapeBehaviourModel("roof_overhang_outer_corner"), UNILATERAL, 1, 3, 0x4f),
    ROOF_OVERHANG_INNER_CORNER(9, "roof_overhang_inner_corner", new LegacyShapeBehaviourModel("roof_overhang_inner_corner"), UNILATERAL, 2, 3, 0xdf),

    CYLINDER(10, "cylinder", new LegacyShapeBehaviourModel("cylinder_full_r8h16"), QUADRILATERAL, 1, 1, 0xff),
    CYLINDER_HALF(11, "cylinder_half", new LegacyShapeBehaviourModel("cylinder_half_r8h16"), BILATERAL, 1, 1, 0xcc),
    CYLINDER_QUARTER(12, "cylinder_quarter", new LegacyShapeBehaviourModel("cylinder_quarter_r8h16"), UNILATERAL, 1, 1, 0x44),
    CYLINDER_LARGE_QUARTER(13, "cylinder_large_quarter", new LegacyShapeBehaviourModel("cylinder_quarter_r16h16"), UNILATERAL, 1, 1, 0xff),
    ANTICYLINDER_LARGE_QUARTER(14, "anticylinder_large_quarter", new LegacyShapeBehaviourModel("round_inner_corner"), UNILATERAL, 1, 2, 0xdd),
    PILLAR(15, "pillar", new LegacyShapeBehaviourModel("cylinder_r6h16"), QUADRILATERAL, 1, 1, 0x106),
    POST(16, "post", new LegacyShapeBehaviourModel("cylinder_r4h16"), QUADRILATERAL, 1, 4, 0x104),
    POLE(17, "pole", new LegacyShapeBehaviourModel("cylinder_r2h16"), QUADRILATERAL, 1, 16, 0x102),

    BEVELLED_OUTER_CORNER(18, "bevelled_outer_corner", new LegacyShapeBehaviourModel("bevelled_outer_corner"), UNILATERAL, 1, 3, 0x4f),
    BEVELLED_INNER_CORNER(19, "bevelled_inner_corner", new LegacyShapeBehaviourModel("bevelled_inner_corner"), UNILATERAL, 1, 1, 0xdf),

    PILLAR_BASE(20, "pillar_base", new LegacyShapeBehaviourModel("pillar_base"), QUADRILATERAL, 1, 1, 0xff),
    DORIC_CAPITAL(21, "doric_capital", new LegacyShapeBehaviourModel("doric_capital"), QUADRILATERAL, 1, 1, 0xff),
    IONIC_CAPITAL(22, "ionic_capital", new LegacyShapeBehaviourModel("ionic_capital"), BILATERAL, 1, 1, 0xff),
    CORINTHIAN_CAPITAL(23, "corinthian_capital", new LegacyShapeBehaviourModel("corinthian_capital"), QUADRILATERAL, 1, 1, 0xff),
    DORIC_TRIGLYPH(24, "doric_triglyph", new LegacyShapeBehaviourModel("doric_triglyph", Profile.Generic.lrStraight), BILATERAL, 1, 1, 0xff),
    DORIC_TRIGLYPH_CORNER(25, "doric_triglyph_corner", new LegacyShapeBehaviourModel("doric_triglyph_corner", Profile.Generic.lrCorner), BILATERAL, 1, 1, 0xff),
    DORIC_METOPE(26, "doric_metope", new LegacyShapeBehaviourModel("doric_metope", Profile.Generic.lrStraight), BILATERAL, 1, 1, 0xff),
    ARCHITRAVE(27, "architrave", new LegacyShapeBehaviourModel("architrave", Profile.Generic.lrStraight), BILATERAL, 1, 1, 0xff),
    ARCHITRAVE_CORNER(28, "architrave_corner", new LegacyShapeBehaviourModel("architrave_corner", Profile.Generic.lrCorner), UNILATERAL, 1, 1, 0xff),

    WINDOW_FRAME(30, "window_frame", new LegacyShapeBehaviourPlainWindow(), BILATERAL, 1, 4, 0x202),
    WINDOW_CORNER(31, "window_corner", new LegacyShapeBehaviourCornerWindow(), UNILATERAL, 1, 2, 0x202),
    WINDOW_MULLION(32, "window_mullion", new LegacyShapeBehaviourMullionWindow(), BILATERAL, 1, 2, 0x202),

    SPHERE_FULL(33, "sphere_full", new LegacyShapeBehaviourModel("sphere_full_r8"), QUADRILATERAL, 1, 1, 0xff),
    SPHERE_HALF(34, "sphere_half", new LegacyShapeBehaviourModel("sphere_half_r8"), QUADRILATERAL, 1, 2, 0x0f),
    SPHERE_QUARTER(35, "sphere_quarter", new LegacyShapeBehaviourModel("sphere_quarter_r8"), BILATERAL, 1, 4, 0x0c),
    SPHERE_EIGHTH(36, "sphere_eighth", new LegacyShapeBehaviourModel("sphere_eighth_r8"), UNILATERAL, 1, 8, 0x04),
    SPHERE_EIGHTH_LARGE(37, "sphere_eighth_large", new LegacyShapeBehaviourModel("sphere_eighth_r16"), UNILATERAL, 1, 1, 0xff),
    SPHERE_EIGHTH_LARGE_REV(38, "sphere_eighth_large_rev", new LegacyShapeBehaviourModel("sphere_eighth_r16_rev"), UNILATERAL, 1, 1, 0xdf),

    ROOF_OVERHANG_GABLE_LH(40, "roof_overhang_gable_lh", new LegacyShapeBehaviourModel("roof_overhang_gable_lh"), BILATERAL, 1, 4, 0x48),
    ROOF_OVERHANG_GABLE_RH(41, "roof_overhang_gable_rh", new LegacyShapeBehaviourModel("roof_overhang_gable_rh"), BILATERAL, 1, 4, 0x84),
    ROOF_OVERHANG_GABLE_END_LH(42, "roof_overhang_gable_end_lh", new LegacyShapeBehaviourModel("roof_overhang_gable_end_lh"), BILATERAL, 1, 4, 0x48),
    ROOF_OVERHANG_GABLE_END_RH(43, "roof_overhang_gable_end_rh", new LegacyShapeBehaviourModel("roof_overhang_gable_end_rh"), BILATERAL, 1, 4, 0x48),
    ROOF_OVERHANG_RIDGE(44, "roof_overhang_ridge", new LegacyShapeBehaviourModel("roof_overhang_gable_ridge"), BILATERAL, 1, 4, 0x0c),
    ROOF_OVERHANG_VALLEY(45, "roof_overhang_valley", new LegacyShapeBehaviourModel("roof_overhang_gable_valley"), BILATERAL, 1, 4, 0xcc),

    CORNICE_LH(50, "cornice_lh", new LegacyShapeBehaviourModel("cornice_lh"), BILATERAL, 1, 4, 0x48),
    CORNICE_RH(51, "cornice_rh", new LegacyShapeBehaviourModel("cornice_rh"), BILATERAL, 1, 4, 0x84),
    CORNICE_END_LH(52, "cornice_end_lh", new LegacyShapeBehaviourModel("cornice_end_lh"), BILATERAL, 1, 4, 0x48),
    CORNICE_END_RH(53, "cornice_end_rh", new LegacyShapeBehaviourModel("cornice_end_rh"), BILATERAL, 1, 4, 0x48),
    CORNICE_RIDGE(54, "cornice_ridge", new LegacyShapeBehaviourModel("cornice_ridge"), BILATERAL, 1, 4, 0x0c),
    CORNICE_VALLEY(55, "cornice_valley", new LegacyShapeBehaviourModel("cornice_valley"), BILATERAL, 1, 4, 0xcc),
    CORNICE_BOTTOM(56, "cornice_bottom", new LegacyShapeBehaviourModel("cornice_bottom"), BILATERAL, 1, 4, 0x0c),

    CLADDING_SHEET(60, "cladding_sheet", LegacyShapeBehaviour.DEFAULT, null, 1, 16, 0),

    ARCH_D_1(61, "arch_d1", new LegacyShapeBehaviourModel("arch_d1"), BILATERAL, 1, 1, 0xff, PLACE_UNDERNEATH),
    ARCH_D_2(62, "arch_d2", new LegacyShapeBehaviourModel("arch_d2"), BILATERAL, 1, 2, 0xfc, PLACE_UNDERNEATH),
    ARCH_D_3_A(63, "arch_d3a", new LegacyShapeBehaviourModel("arch_d3a"), BILATERAL, 1, 2, 0xcc, PLACE_UNDERNEATH),
    ARCH_D_3_B(64, "arch_d3b", new LegacyShapeBehaviourModel("arch_d3b"), BILATERAL, 1, 1, 0xfc, PLACE_UNDERNEATH),
    ARCH_D_3_C(65, "arch_d3c", new LegacyShapeBehaviourModel("arch_d3c"), BILATERAL, 1, 1, 0xff, PLACE_UNDERNEATH),
    ARCH_D_4_A(66, "arch_d4a", new LegacyShapeBehaviourModel("arch_d4a"), BILATERAL, 1, 2, 0xcc, PLACE_UNDERNEATH),
    ARCH_D_4_B(67, "arch_d4b", new LegacyShapeBehaviourModel("arch_d4b"), BILATERAL, 1, 1, 0xfc, PLACE_UNDERNEATH),
    ARCH_D_4_C(68, "arch_d4c", new LegacyShapeBehaviourModel("arch_d4c"), BILATERAL, 1, 2, 0x0, PLACE_UNDERNEATH),

    BANISTER_PLAIN_BOTTOM(70, "banister_plain_bottom", new LegacyShapeBehaviourBanister("balustrade_stair_plain_bottom"), BILATERAL, 1, 10, 0x0, PLACE_OFFSET),
    BANISTER_PLAIN(71, "banister_plain", new LegacyShapeBehaviourBanister("balustrade_stair_plain"), BILATERAL, 1, 10, 0x0, PLACE_OFFSET),
    BANISTER_PLAIN_TOP(72, "banister_plain_top", new LegacyShapeBehaviourBanister("balustrade_stair_plain_top"), BILATERAL, 1, 10, 0x0, PLACE_OFFSET),

    BALUSTRADE_FANCY(73, "balustrade_fancy", new LegacyShapeBehaviourModel("balustrade_fancy"), BILATERAL, 1, 5, 0x0),
    BALUSTRADE_FANCY_CORNER(74, "balustrade_fancy_corner", new LegacyShapeBehaviourModel("balustrade_fancy_corner"), UNILATERAL, 1, 2, 0x0),
    BALUSTRADE_FANCY_WITH_NEWEL(75, "balustrade_fancy_with_newel", new LegacyShapeBehaviourModel("balustrade_fancy_with_newel"), BILATERAL, 1, 3, 0x0),
    BALUSTRADE_FANCY_NEWEL(76, "balustrade_fancy_newel", new LegacyShapeBehaviourModel("balustrade_fancy_newel"), UNILATERAL, 1, 4, 0x0),

    BALUSTRADE_PLAIN(77, "balustrade_plain", new LegacyShapeBehaviourModel("balustrade_plain"), BILATERAL, 1, 10, 0x0),
    BALUSTRADE_PLAIN_OUTER_CORNER(78, "balustrade_plain_outer_corner", new LegacyShapeBehaviourModel("balustrade_plain_outer_corner"), UNILATERAL, 1, 4, 0x0),
    BALUSTRADE_PLAIN_WITH_NEWEL(79, "balustrade_plain_with_newel", new LegacyShapeBehaviourModel("balustrade_plain_with_newel"), BILATERAL, 1, 6, 0x0),

    BANISTER_PLAIN_END(80, "banister_plain_end", new LegacyShapeBehaviourBanister("balustrade_stair_plain_end"), BILATERAL, 1, 8, 0x0, PLACE_OFFSET),

    BANISTER_FANCY_NEWEL_TALL(81, "banister_fancy_newel_tall", new LegacyShapeBehaviourModel("balustrade_fancy_newel_tall"), UNILATERAL, 1, 2, 0x0),

    BALUSTRADE_PLAIN_INNER_CORNER(82, "balustrade_plain_inner_corner", new LegacyShapeBehaviourModel("balustrade_plain_inner_corner"), UNILATERAL, 1, 8, 0x0),
    BALUSTRADE_PLAIN_END(83, "balustrade_plain_end", new LegacyShapeBehaviourBanister("balustrade_plain_end"), BILATERAL, 1, 8, 0x0, PLACE_OFFSET),

    BANISTER_FANCY_BOTTOM(84, "banister_fancy_bottom", new LegacyShapeBehaviourBanister("balustrade_stair_fancy_bottom"), BILATERAL, 1, 5, 0x0, PLACE_OFFSET),
    BANISTER_FANCY(85, "banister_fancy", new LegacyShapeBehaviourBanister("balustrade_stair_fancy"), BILATERAL, 1, 5, 0x0, PLACE_OFFSET),
    BANISTER_FANCY_TOP(86, "banister_fancy_top", new LegacyShapeBehaviourBanister("balustrade_stair_fancy_top"), BILATERAL, 1, 5, 0x0, PLACE_OFFSET),
    BANISTER_FANCY_END(87, "banister_fancy_end", new LegacyShapeBehaviourBanister("balustrade_stair_fancy_end"), BILATERAL, 1, 2, 0x0, PLACE_OFFSET),

    BANISTER_PLAIN_INNER_CORNER(88, "banister_plain_inner_corner", new LegacyShapeBehaviourModel("balustrade_stair_plain_inner_corner"), UNILATERAL, 1, 6, 0x0),

    SLAB(90, "slab", new LegacyShapeBehaviourModel("slab"), QUADRILATERAL, 1, 2, 0x0),
    STAIRS(91, "stairs", new LegacyShapeBehaviourModel("stairs", Profile.Generic.lrStraight), BILATERAL, 3, 4, 0x0),
    STAIRS_OUTER_CORNER(92, "stairs_outer_corner", new LegacyShapeBehaviourModel("stairs_outer_corner", Profile.Generic.lrCorner), UNILATERAL, 2, 3, 0x0),
    STAIRS_INNER_CORNER(93, "stairs_inner_corner", new LegacyShapeBehaviourModel("stairs_inner_corner", Profile.Generic.rlCorner), UNILATERAL, 1, 1, 0x0),
    ;

    private static final Map<Integer, LegacyEnumShape> SHAPES_BY_ID;
    private static final Map<String, LegacyEnumShape> SHAPES_BY_NAME;

    static {
        ImmutableMap.Builder<Integer, LegacyEnumShape> shapesById = ImmutableMap.builder();
        ImmutableMap.Builder<String, LegacyEnumShape> shapesByName = ImmutableMap.builder();
        Arrays.stream(LegacyEnumShape.values()).forEach(
                s -> {
                    shapesById.put(s.id, s);
                    shapesByName.put(s.getSerializedName(), s);
                }
        );
        SHAPES_BY_ID = shapesById.build();
        SHAPES_BY_NAME = shapesByName.build();
    }

    public int id;
    public String translationKey;
    public LegacyShapeBehaviour behaviour;
    public LegacyShapeSymmetry symmetry;
    public int materialCost;
    public int itemsProduced;
    public int occlusionMask;
    public int flags;

    LegacyEnumShape(int id, String translationKey, LegacyShapeBehaviour behaviour, LegacyShapeSymmetry sym, int materialCost, int itemsProduced, int occlusionMask) {
        this(id, translationKey, behaviour, sym, materialCost, itemsProduced, occlusionMask, 0);
    }

    LegacyEnumShape(int id, String translationKey, LegacyShapeBehaviour behaviour, LegacyShapeSymmetry sym, int materialCost, int itemsProduced, int occ, int flags) {
        this.id = id;
        this.translationKey = translationKey;
        this.behaviour = behaviour;
        this.symmetry = sym;
        this.materialCost = materialCost;
        this.itemsProduced = itemsProduced;
        this.occlusionMask = occ;
        this.flags = flags;
    }

    public static LegacyEnumShape forId(int id) {
        return SHAPES_BY_ID.getOrDefault(id, ROOF_TILE);
    }

    public static LegacyEnumShape forName(String name) {
        return SHAPES_BY_NAME.getOrDefault(name, ROOF_TILE);
    }

    public static int turnForPlacementHit(int side, LegacyVector3 hit, LegacyShapeSymmetry symmetry) {
        LegacyVector3 h = LegacyTrans3.sideTurn(side, 0).ip(hit);
        return turnForPlacementHit(symmetry, h.x(), h.z());
    }

    private static int turnForPlacementHit(LegacyShapeSymmetry symmetry, double x, double z) {
        switch (symmetry) {
            case QUADRILATERAL: // All rotations are equivalent
                return 0;
            case BILATERAL: // Rotate according to nearest side
                if (abs(z) > abs(x))
                    return (z < 0) ? 2 : 0;
                else
                    return x > 0 ? 1 : 3;
            case UNILATERAL: // Rotate according to nearest corner
                if (z > 0)
                    return x < 0 ? 0 : 1;
                else
                    return x > 0 ? 2 : 3;
            default:
                return 0;
        }
    }

    public void orientOnPlacement(Player player, LegacyShapeBlockEntity te,
                                  BlockPos npos, BlockState nstate, BlockEntity nte, Direction face, LegacyVector3 hit) {
        if (!te.getArchitectureShape().behaviour.orientOnPlacement(player, te, npos, nstate, nte, face, hit)) {
            this.orientFromHitPosition(player, te, face, hit);
        }
    }

    private void orientFromHitPosition(Player player, LegacyShapeBlockEntity te, Direction face, LegacyVector3 hit) {
        byte side, turn;
        switch (face) {
            case UP:
                side = (byte) this.rightSideUpSide();
                break;
            case DOWN:
                if (te.getArchitectureShape().behaviour.canPlaceUpsideDown())
                    side = (byte) this.upsideDownSide();
                else
                    side = (byte) this.rightSideUpSide();
                break;
            default:
                if (player.isCrouching())
                    side = (byte) face.getOpposite().ordinal();
                else if (hit.y() > 0.0 && te.getArchitectureShape().behaviour.canPlaceUpsideDown())
                    side = (byte) this.upsideDownSide();
                else
                    side = (byte) this.rightSideUpSide();
        }
        turn = (byte) turnForPlacementHit(side, hit, this.symmetry);
        te.setSide(side);
        te.setTurn(turn);
        if ((this.flags & PLACE_OFFSET) != 0) {
            te.setOffsetX(this.offsetXForPlacementHit(side, turn, hit));
        }
    }

    public double offsetXForPlacementHit(int side, int turn, LegacyVector3 hit) {
        LegacyVector3 h = LegacyTrans3.sideTurn(side, turn).ip(hit);
        return this.signedPlacementOffsetX(h.x());
    }

    public double signedPlacementOffsetX(double sign) {
        double offx = this.behaviour.placementOffsetX();
        if (sign < 0)
            offx = -offx;
        return offx;
    }

    private int rightSideUpSide() {
        if (this.isPlacedUnderneath())
            return 1;
        else
            return 0;
    }

    private int upsideDownSide() {
        if (this.isPlacedUnderneath())
            return 0;
        else
            return 1;
    }

    private boolean isPlacedUnderneath() {
        return (this.flags & PLACE_UNDERNEATH) != 0;
    }

    public boolean isCladding() {
        return this == CLADDING_SHEET;
    }

    public String getLocalizedShapeName() {
        return I18n.get("architecturecraft.shape." + this.translationKey);
    }

    @Override
    public String getSerializedName() {
        return this.translationKey;
    }
}
