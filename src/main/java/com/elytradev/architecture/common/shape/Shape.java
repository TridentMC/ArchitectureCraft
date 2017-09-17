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

package com.elytradev.architecture.common.shape;

import com.elytradev.architecture.common.ArchitectureMod;
import com.elytradev.architecture.common.helpers.Profile;
import com.elytradev.architecture.common.helpers.Trans3;
import com.elytradev.architecture.common.helpers.Vector3;
import com.elytradev.architecture.common.tile.TileShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

import static com.elytradev.architecture.common.shape.ShapeFlags.PLACE_OFFSET;
import static com.elytradev.architecture.common.shape.ShapeFlags.PLACE_UNDERNEATH;
import static com.elytradev.architecture.common.shape.ShapeSymmetry.*;
import static java.lang.Math.abs;

public enum Shape {

    ROOF_TILE(0, "Roof Tile", ShapeKind.Roof, BILATERAL, 1, 2, 0xcf),
    ROOF_OUTER_CORNER(1, "Roof Outer Corner", ShapeKind.Roof, UNILATERAL, 1, 3, 0x4f),
    ROOF_INNER_CORNER(2, "Roof Inner Corner", ShapeKind.Roof, UNILATERAL, 2, 3, 0xdf),
    ROOF_RIDGE(3, "Gabled Roof Ridge", ShapeKind.Roof, BILATERAL, 1, 4, 0x0f),
    ROOF_SMART_RIDGE(4, "Hip Roof Ridge", ShapeKind.Roof, QUADRILATERAL, 1, 2, 0x0f),
    ROOF_VALLEY(5, "Gabled Roof Valley", ShapeKind.Roof, BILATERAL, 1, 2, 0xff),
    ROOF_SMART_VALLEY(6, "Hip Roof Valley", ShapeKind.Roof, QUADRILATERAL, 1, 1, 0xff),

    ROOF_OVERHANG(7, "Roof Overhang", ShapeKind.Model("roof_overhang"), BILATERAL, 1, 2, 0xcf),
    ROOF_OVERHANG_OUTER_CORNER(8, "Roof Overhang Outer Corner", ShapeKind.Model("roof_overhang_outer_corner"), UNILATERAL, 1, 3, 0x4f),
    ROOF_OVERHANG_INNER_CORNER(9, "Roof Overhang Inner Corner", ShapeKind.Model("roof_overhang_inner_corner"), UNILATERAL, 2, 3, 0xdf),

    CYLINDER(10, "CYLINDER", ShapeKind.Model("cylinder_full_r8h16"), QUADRILATERAL, 1, 1, 0xff),
    CYLINDER_HALF(11, "Half CYLINDER", ShapeKind.Model("cylinder_half_r8h16"), BILATERAL, 1, 1, 0xcc),
    CYLINDER_QUARTER(12, "Quarter CYLINDER", ShapeKind.Model("cylinder_quarter_r8h16"), UNILATERAL, 1, 1, 0x44),
    CYLINDER_LARGE_QUARTER(13, "Round Outer Corner", ShapeKind.Model("cylinder_quarter_r16h16"), UNILATERAL, 1, 1, 0xff),
    ANTICYLINDER_LARGE_QUARTER(14, "Round Inner Corner", ShapeKind.Model("round_inner_corner"), UNILATERAL, 1, 2, 0xdd),
    PILLAR(15, "Round PILLAR", ShapeKind.Model("cylinder_r6h16"), QUADRILATERAL, 1, 1, 0x106),
    POST(16, "Round POST", ShapeKind.Model("cylinder_r4h16"), QUADRILATERAL, 1, 4, 0x104),
    POLE(17, "Round POLE", ShapeKind.Model("cylinder_r2h16"), QUADRILATERAL, 1, 16, 0x102),

    BEVELLED_OUTER_CORNER(18, "Bevelled Outer Corner", ShapeKind.Model("bevelled_outer_corner"), UNILATERAL, 1, 3, 0x4f),
    BEVELLED_INNER_CORNER(19, "Bevelled Inner Corner", ShapeKind.Model("bevelled_inner_corner"), UNILATERAL, 1, 1, 0xdf),

    PILLAR_BASE(20, "Round PILLAR Base", ShapeKind.Model("pillar_base"), QUADRILATERAL, 1, 1, 0xff),
    DORIC_CAPITAL(21, "Doric Capital", ShapeKind.Model("doric_capital"), QUADRILATERAL, 1, 1, 0xff),
    IONIC_CAPITAL(22, "Ionic capital", ShapeKind.Model("ionic_capital"), BILATERAL, 1, 1, 0xff),
    CORINTHIAN_CAPITAL(23, "Corinthian capital", ShapeKind.Model("corinthian_capital"), QUADRILATERAL, 1, 1, 0xff),
    DORIC_TRIGLYPH(24, "Triglyph", ShapeKind.Model("doric_triglyph", Profile.Generic.lrStraight), BILATERAL, 1, 1, 0xff),
    DORIC_TRIGLYPH_CORNER(25, "Triglyph Corner", ShapeKind.Model("doric_triglyph_corner", Profile.Generic.lrCorner), BILATERAL, 1, 1, 0xff),
    DORIC_METOPE(26, "Metope", ShapeKind.Model("doric_metope", Profile.Generic.lrStraight), BILATERAL, 1, 1, 0xff),
    ARCHITRAVE(27, "ARCHITRAVE", ShapeKind.Model("architrave", Profile.Generic.lrStraight), BILATERAL, 1, 1, 0xff),
    ARCHITRAVE_CORNER(28, "ARCHITRAVE Corner", ShapeKind.Model("architrave_corner", Profile.Generic.lrCorner), UNILATERAL, 1, 1, 0xff),

    WINDOW_FRAME(30, "Window Frame", WindowShapeKinds.PlainWindow, BILATERAL, 1, 4, 0x202),
    WINDOW_CORNER(31, "Window Corner", WindowShapeKinds.CornerWindow, UNILATERAL, 1, 2, 0x202),
    WINDOW_MULLION(32, "Window Mullion", WindowShapeKinds.MullionWindow, BILATERAL, 1, 2, 0x202),

    SPHERE_FULL(33, "Sphere", ShapeKind.Model("sphere_full_r8"), QUADRILATERAL, 1, 1, 0xff),
    SPHERE_HALF(34, "Hemisphere", ShapeKind.Model("sphere_half_r8"), QUADRILATERAL, 1, 2, 0x0f),
    SPHERE_QUARTER(35, "Quarter Sphere", ShapeKind.Model("sphere_quarter_r8"), BILATERAL, 1, 4, 0x0c),
    SPHERE_EIGHTH(36, "Quarter Hemisphere", ShapeKind.Model("sphere_eighth_r8"), UNILATERAL, 1, 8, 0x04),
    SPHERE_EIGHTH_LARGE(37, "Round Outer Corner Cap", ShapeKind.Model("sphere_eighth_r16"), UNILATERAL, 1, 1, 0xff),
    SPHERE_EIGHTH_LARGE_REV(38, "Round Inner Corner Cap", ShapeKind.Model("sphere_eighth_r16_rev"), UNILATERAL, 1, 1, 0xdf),

    ROOF_OVERHANG_GABLE_LH(40, "Gable Overhang LH", ShapeKind.Model("roof_overhang_gable_lh"), BILATERAL, 1, 4, 0x48),
    ROOF_OVERHANG_GABLE_RH(41, "Gable Overhang RH", ShapeKind.Model("roof_overhang_gable_rh"), BILATERAL, 1, 4, 0x84),
    ROOF_OVERHANG_GABLE_END_LH(42, "Gable Overhang LH End", ShapeKind.Model("roof_overhang_gable_end_lh"), BILATERAL, 1, 4, 0x48),
    ROOF_OVERHANG_GABLE_END_RH(43, "Gable Overhang RH End", ShapeKind.Model("roof_overhang_gable_end_rh"), BILATERAL, 1, 4, 0x48),
    ROOF_OVERHANG_RIDGE(44, "Ridge Overhang", ShapeKind.Model("roof_overhang_gable_ridge"), BILATERAL, 1, 4, 0x0c),
    ROOF_OVERHANG_VALLEY(45, "Valley Overhang", ShapeKind.Model("roof_overhang_gable_valley"), BILATERAL, 1, 4, 0xcc),

    CORNICE_LH(50, "Cornice LH", ShapeKind.Model("cornice_lh"), BILATERAL, 1, 4, 0x48),
    CORNICE_RH(51, "Cornice RH", ShapeKind.Model("cornice_rh"), BILATERAL, 1, 4, 0x84),
    CORNICE_END_LH(52, "Cornice LH End", ShapeKind.Model("cornice_end_lh"), BILATERAL, 1, 4, 0x48),
    CORNICE_END_RH(53, "Cornice RH End", ShapeKind.Model("cornice_end_rh"), BILATERAL, 1, 4, 0x48),
    CORNICE_RIDGE(54, "Cornice Ridge", ShapeKind.Model("cornice_ridge"), BILATERAL, 1, 4, 0x0c),
    CORNICE_VALLEY(55, "Cornice Valley", ShapeKind.Model("cornice_valley"), BILATERAL, 1, 4, 0xcc),
    CORNICE_BOTTOM(56, "Cornice Bottom", ShapeKind.Model("cornice_bottom"), BILATERAL, 1, 4, 0x0c),

    CLADDING_SHEET(60, "Cladding", ShapeKind.Cladding, null, 1, 16, 0),

    ARCH_D_1(61, "Arch Diameter 1", ShapeKind.Model("arch_d1"), BILATERAL, 1, 1, 0xff, PLACE_UNDERNEATH),
    ARCH_D_2(62, "Arch Diameter 2", ShapeKind.Model("arch_d2"), BILATERAL, 1, 2, 0xfc, PLACE_UNDERNEATH),
    ARCH_D_3_A(63, "Arch Diameter 3 Part A", ShapeKind.Model("arch_d3a"), BILATERAL, 1, 2, 0xcc, PLACE_UNDERNEATH),
    ARCH_D_3_B(64, "Arch Diameter 3 Part B", ShapeKind.Model("arch_d3b"), BILATERAL, 1, 1, 0xfc, PLACE_UNDERNEATH),
    ARCH_D_3_C(65, "Arch Diameter 3 Part C", ShapeKind.Model("arch_d3c"), BILATERAL, 1, 1, 0xff, PLACE_UNDERNEATH),
    ARCH_D_4_A(66, "Arch Diameter 4 Part A", ShapeKind.Model("arch_d4a"), BILATERAL, 1, 2, 0xcc, PLACE_UNDERNEATH),
    ARCH_D_4_B(67, "Arch Diameter 4 Part B", ShapeKind.Model("arch_d4b"), BILATERAL, 1, 1, 0xfc, PLACE_UNDERNEATH),
    ARCH_D_4_C(68, "Arch Diameter 4 Part C", ShapeKind.Model("arch_d4c"), BILATERAL, 1, 2, 0x0, PLACE_UNDERNEATH),

    BANISTER_PLAIN_BOTTOM(70, "Plain Banister Bottom Transition", ShapeKind.Banister("balustrade_stair_plain_bottom"), BILATERAL, 1, 10, 0x0, PLACE_OFFSET),
    BANISTER_PLAIN(71, "Plain Banister", ShapeKind.Banister("balustrade_stair_plain"), BILATERAL, 1, 10, 0x0, PLACE_OFFSET),
    BANISTER_PLAIN_TOP(72, "Plain Banister Top Transition", ShapeKind.Banister("balustrade_stair_plain_top"), BILATERAL, 1, 10, 0x0, PLACE_OFFSET),

    BALUSTRADE_FANCY(73, "Fancy Balustrade", ShapeKind.Model("balustrade_fancy"), BILATERAL, 1, 5, 0x0),
    BALUSTRADE_FANCY_CORNER(74, "Fancy Corner Balustrade", ShapeKind.Model("balustrade_fancy_corner"), UNILATERAL, 1, 2, 0x0),
    BALUSTRADE_FANCY_WITH_NEWEL(75, "Fancy Balustrade with Newel", ShapeKind.Model("balustrade_fancy_with_newel"), BILATERAL, 1, 3, 0x0),
    BALUSTRADE_FANCY_NEWEL(76, "Fancy Newel", ShapeKind.Model("balustrade_fancy_newel"), UNILATERAL, 1, 4, 0x0),

    BALUSTRADE_PLAIN(77, "Plain Balustrade", ShapeKind.Model("balustrade_plain"), BILATERAL, 1, 10, 0x0),
    BALUSTRADE_PLAIN_OUTER_CORNER(78, "Plain Outer Corner Balustrade", ShapeKind.Model("balustrade_plain_outer_corner"), UNILATERAL, 1, 4, 0x0),
    BALUSTRADE_PLAIN_WITH_NEWEL(79, "Plain Balustrade with Newel", ShapeKind.Model("balustrade_plain_with_newel"), BILATERAL, 1, 6, 0x0),

    BANISTER_PLAIN_END(80, "Plain Banister End", ShapeKind.Banister("balustrade_stair_plain_end"), BILATERAL, 1, 8, 0x0, PLACE_OFFSET),

    BANISTER_FANCY_NEWEL_TALL(81, "Tall Fancy Newel", ShapeKind.Model("balustrade_fancy_newel_tall"), UNILATERAL, 1, 2, 0x0),

    BALUSTRADE_PLAIN_INNER_CORNER(82, "Plain Inner Corner Balustrade", ShapeKind.Model("balustrade_plain_inner_corner"), UNILATERAL, 1, 8, 0x0),
    BALUSTRADE_PLAIN_END(83, "Plain Balustrade End", ShapeKind.Banister("balustrade_plain_end"), BILATERAL, 1, 8, 0x0, PLACE_OFFSET),

    BANISTER_FANCY_BOTTOM(84, "Fancy Banister Bottom Transition", ShapeKind.Banister("balustrade_stair_fancy_bottom"), BILATERAL, 1, 5, 0x0, PLACE_OFFSET),
    BANISTER_FANCY(85, "Fancy Banister", ShapeKind.Banister("balustrade_stair_fancy"), BILATERAL, 1, 5, 0x0, PLACE_OFFSET),
    BANISTER_FANCY_TOP(86, "Fancy Banister Top Transition", ShapeKind.Banister("balustrade_stair_fancy_top"), BILATERAL, 1, 5, 0x0, PLACE_OFFSET),
    BANISTER_FANCY_END(87, "Fancy Banister End", ShapeKind.Banister("balustrade_stair_fancy_end"), BILATERAL, 1, 2, 0x0, PLACE_OFFSET),

    BANISTER_PLAIN_INNER_CORNER(88, "Plain Banister Inner Corner", ShapeKind.Model("balustrade_stair_plain_inner_corner"), UNILATERAL, 1, 6, 0x0),

    SLAB(90, "SLAB", ShapeKind.Model("slab"), QUADRILATERAL, 1, 2, 0x0),
    STAIRS(91, "STAIRS", ShapeKind.Model("stairs", Profile.Generic.lrStraight), BILATERAL, 3, 4, 0x0),
    STAIRS_OUTER_CORNER(92, "STAIRS Outer Corner", ShapeKind.Model("stairs_outer_corner", Profile.Generic.lrCorner), UNILATERAL, 2, 3, 0x0),
    STAIRS_INNER_CORNER(93, "STAIRS Inner Corner", ShapeKind.Model("stairs_inner_corner", Profile.Generic.rlCorner), UNILATERAL, 1, 1, 0x0),;

    public static Shape[] values = values();
    public static boolean debugPlacement = false;
    protected static Map<Integer, Shape> idMap = new HashMap<Integer, Shape>();

    static {
        for (Shape s : values)
            idMap.put(s.id, s);
    }

    public int id;
    public String title;
    public ShapeKind kind;
    public ShapeSymmetry symmetry;
    public int materialUsed;
    public int itemsProduced;
    public int occlusionMask;
    public int flags;

//	Shape(int id, String title, ShapeKind kind, ShapeSymmetry sym, int used, int made, int occ) {
//		this(id, title, kind, sym, used, made, occ, null);
//	}
//
//	Shape(int id, String title, ShapeKind kind, ShapeSymmetry sym, int used, int made, int occ, String model)

    Shape(int id, String title, ShapeKind kind, ShapeSymmetry sym, int used, int made, int occ) {
        this(id, title, kind, sym, used, made, occ, 0);
    }

    Shape(int id, String title, ShapeKind kind, ShapeSymmetry sym, int used, int made, int occ, int flags) {
        this.id = id;
        this.title = title;
        this.kind = kind;
        this.symmetry = sym;
        this.materialUsed = used;
        this.itemsProduced = made;
        this.occlusionMask = occ;
        this.flags = flags;
    }

//	protected void orientOnPlacement(EntityPlayer player, ShapeTE te, ShapeTE nte, EnumFacing face, Vector3 hit) {
//		if (te.shape.kind.orientOnPlacement(player, te, nte, face, hit))
//			return;
//		else
//			orientFromHitPosition(player, te, face, hit);
//	}

    public static Shape forId(int id) {
        Shape shape = idMap.get(id);
        if (shape == null)
            shape = ROOF_TILE;
        return shape;
    }

    public static int turnForPlacementHit(int side, Vector3 hit, ShapeSymmetry symmetry) {
        Vector3 h = Trans3.sideTurn(side, 0).ip(hit);
        return turnForPlacementHit(symmetry, h.x, h.z);
    }

    private static int turnForPlacementHit(ShapeSymmetry symmetry, double x, double z) {
        switch (symmetry) {
            case QUADRILATERAL: // All rotations are equivalent
                return 0;
            case BILATERAL: // Rotate according to nearest side
                if (abs(z) > abs(x))
                    return z < 0 ? 2 : 0;
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

    protected void orientOnPlacement(EntityPlayer player, TileShape te,
                                     BlockPos npos, IBlockState nstate, TileEntity nte, EnumFacing face, Vector3 hit) {
        if (te.shape.kind.orientOnPlacement(player, te, npos, nstate, nte, face, hit))
            return;
        else
            orientFromHitPosition(player, te, face, hit);
    }

    protected void orientFromHitPosition(EntityPlayer player, TileShape te, EnumFacing face, Vector3 hit) {
        int side, turn;
        switch (face) {
            case UP:
                side = rightSideUpSide();
                break;
            case DOWN:
                if (te.shape.kind.canPlaceUpsideDown())
                    side = upsideDownSide();
                else
                    side = rightSideUpSide();
                break;
            default:
                if (player.isSneaking())
                    side = face.getOpposite().ordinal();
                else if (hit.y > 0.0 && te.shape.kind.canPlaceUpsideDown())
                    side = upsideDownSide();
                else
                    side = rightSideUpSide();
        }
        turn = turnForPlacementHit(side, hit, symmetry);
        if (debugPlacement && !te.getWorld().isRemote) {
            ArchitectureMod.LOG.info("Shape.orientFromHitPosition: face %s global hit %s\n", face, hit);
            ArchitectureMod.LOG.info("Shape.orientFromHitPosition: side %s turn %s symmetry %s\n", side, turn, te.shape.symmetry);
        }
        te.setSide(side);
        te.setTurn(turn);
        if ((flags & PLACE_OFFSET) != 0) {
            te.setOffsetX(offsetXForPlacementHit(side, turn, hit));
            if (debugPlacement && !te.getWorld().isRemote)
                ArchitectureMod.LOG.info("Shape.orientFromHitPosition: kind = %s offsetX = %.3f\n", kind, te.getOffsetX());
        }
    }

    public double offsetXForPlacementHit(int side, int turn, Vector3 hit) {
        Vector3 h = Trans3.sideTurn(side, turn).ip(hit);
        return signedPlacementOffsetX(h.x);
    }

    public double signedPlacementOffsetX(double sign) {
        double offx = kind.placementOffsetX();
        if (sign < 0)
            offx = -offx;
        return offx;
    }

    protected int rightSideUpSide() {
        if (isPlacedUnderneath())
            return 1;
        else
            return 0;
    }

    protected int upsideDownSide() {
        if (isPlacedUnderneath())
            return 0;
        else
            return 1;
    }

    protected boolean isPlacedUnderneath() {
        return (flags & PLACE_UNDERNEATH) != 0;
    }

}
