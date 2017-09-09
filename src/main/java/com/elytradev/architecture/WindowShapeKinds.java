//------------------------------------------------------------------------------
//
//	 ArchitectureCraft - Window shape kinds
//
//------------------------------------------------------------------------------

package com.elytradev.architecture;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;

import java.util.List;

import static com.elytradev.architecture.ShapeKind.Window;
import static com.elytradev.architecture.ShapeKind.Window.FrameKind.*;
import static net.minecraft.util.EnumFacing.*;

public class WindowShapeKinds {

    //------------------------------------------------------------------------------

    public static Window PlainWindow = new PlainWindow();
    public static Window MullionWindow = new MullionWindow();

    //------------------------------------------------------------------------------
    public static Window CornerWindow = new CornerWindow();

    public static class PlainWindow extends Window {

        {
            frameSides = new EnumFacing[]{DOWN, EAST, UP, WEST};
            frameAlways = new boolean[]{false, false, false, false};
            frameKinds = new FrameKind[]{Plain, Plain, None, None, Plain, Plain};
            frameOrientations = new EnumFacing[]{EAST, EAST, null, null, UP, UP};
            frameTrans = new Trans3[]{
                    Trans3.ident,
                    Trans3.ident.rotZ(90),
                    Trans3.ident.rotZ(180),
                    Trans3.ident.rotZ(270),
            };
        }

        @Override
        public boolean orientOnPlacement(EntityPlayer player, ShapeTE te, ShapeTE nte, EnumFacing face,
                                         Vector3 hit) {
            if (nte != null && !player.isSneaking()) {
                if (nte.shape.kind instanceof PlainWindow) {
                    te.setSide(nte.side);
                    te.setTurn(nte.turn);
                    return true;
                }
                if (nte.shape.kind instanceof CornerWindow) {
                    EnumFacing nlf = nte.localFace(face);
                    FrameKind nfk = ((Window) nte.shape.kind).frameKindForLocalSide(nlf);
                    if (nfk == FrameKind.Plain) {
                        EnumFacing lf = face.getOpposite();
                        te.setSide(nte.side);
                        switch (nlf) {
                            case SOUTH:
                                te.setTurn(BaseUtils.turnToFace(WEST, lf));
                                return true;
                            case WEST:
                                te.setTurn(BaseUtils.turnToFace(EAST, lf));
                                return true;
                        }
                    }
                }
            }
            return super.orientOnPlacement(player, te, nte, face, hit);
        }

    }

    //------------------------------------------------------------------------------

    public static class MullionWindow extends PlainWindow {

        @Override
        protected void addCentreBoxesToList(double r, double s, Trans3 t, List list) {
            t.addBox(-r, -0.5, -s, r, 0.5, s, list);
        }

        @Override
        protected void addGlassBoxesToList(double r, double s, double w, double e[], Trans3 t, List list) {
            t.addBox(-e[3], -e[0], -w, -r, e[2], w, list);
            t.addBox(r, -e[0], -w, e[1], e[2], w, list);
        }

    }

    public static class CornerWindow extends Window {

        {
            frameSides = new EnumFacing[]{DOWN, SOUTH, UP, WEST};
            frameAlways = new boolean[]{false, false, false, false};
            frameKinds = new FrameKind[]{Corner, Corner, None, Plain, Plain, None};
            frameOrientations = new EnumFacing[]{EAST, EAST, null, UP, UP, null};
            frameTrans = new Trans3[]{
                    Trans3.ident,
                    Trans3.ident.rotY(-90).rotZ(90),
                    Trans3.ident.rotY(-90).rotZ(180),
                    Trans3.ident.rotZ(270),
            };

        }

        @Override
        protected void addCentreBoxesToList(double r, double s, Trans3 t, List list) {
            t.addBox(-r, -0.5, -r, r, 0.5, r, list);
        }

        @Override
        protected void addFrameBoxesToList(int i, double r, double s, Trans3 ts, List list) {
            if ((i & 1) == 0) {
                ts.addBox(-0.5, -0.5, -s, s, -0.5 + r, s, list);
                ts.addBox(-s, -0.5, -s, s, -0.5 + r, 0.5, list);
            } else
                super.addFrameBoxesToList(i, r, s, ts, list);
        }

        @Override
        protected void addGlassBoxesToList(double r, double s, double w, double e[], Trans3 t, List list) {
            t.addBox(-e[3], -e[0], -w, -s, e[2], w, list);
            t.addBox(-w, -e[0], s, w, e[2], e[1], list);
        }

        @Override
        public boolean orientOnPlacement(EntityPlayer player, ShapeTE te, ShapeTE nte, EnumFacing face,
                                         Vector3 hit) {
            if (nte != null && !player.isSneaking()) {
                if (nte.shape.kind instanceof Window) {
                    Window nsk = (Window) nte.shape.kind;
                    EnumFacing nlf = nte.localFace(face);
                    FrameKind nfk = nsk.frameKindForLocalSide(nlf);
                    switch (nfk) {
                        case Corner:
                            te.setSide(nte.side);
                            te.setTurn(nte.turn);
                            return true;
                        case Plain:
                            EnumFacing nfo = nte.globalFace(nsk.frameOrientationForLocalSide(nlf));
                            return orientFromAdjacentCorner(te, nfo, hit)
                                    || orientFromAdjacentCorner(te, nfo.getOpposite(), hit);
                    }
                }
            }
            return super.orientOnPlacement(player, te, nte, face, hit);
        }

        protected boolean orientFromAdjacentCorner(ShapeTE te, EnumFacing face, Vector3 hit) {
            ShapeTE nte = ShapeTE.get(te.getWorld(), te.getPos().offset(face.getOpposite()));
            if (nte != null && nte.shape.kind instanceof Window) {
                Window nsk = (Window) nte.shape.kind;
                EnumFacing nlf = nte.localFace(face);
                FrameKind nfk = nsk.frameKindForLocalSide(nlf);
                if (nfk == FrameKind.Corner) {
                    te.setSide(nte.side);
                    te.setTurn(nte.turn);
                    return true;
                }
            }
            return false;
        }
    }

    //------------------------------------------------------------------------------

}
