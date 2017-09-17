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

import com.elytradev.architecture.client.proxy.ClientProxy;
import com.elytradev.architecture.client.render.model.IRenderableModel;
import com.elytradev.architecture.client.render.model.RenderableModel;
import com.elytradev.architecture.client.render.shape.RenderRoof;
import com.elytradev.architecture.client.render.shape.RenderWindow;
import com.elytradev.architecture.client.render.target.RenderTargetBase;
import com.elytradev.architecture.client.render.texture.ITexture;
import com.elytradev.architecture.common.ArchitectureMod;
import com.elytradev.architecture.common.block.BlockHelper;
import com.elytradev.architecture.common.block.BlockShape;
import com.elytradev.architecture.common.helpers.Profile;
import com.elytradev.architecture.common.helpers.Trans3;
import com.elytradev.architecture.common.helpers.Utils;
import com.elytradev.architecture.common.helpers.Vector3;
import com.elytradev.architecture.common.tile.TileArchitecture;
import com.elytradev.architecture.common.tile.TileShape;
import com.elytradev.architecture.common.utils.MiscUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.EnumFacing.*;

public abstract class ShapeKind {

    public static Roof Roof = new Roof();
    public static Cladding Cladding = new Cladding();
    public Object[] profiles; // indexed by local face

    public static Model Model(String name) {
        return new Model(name, null);
    }

    public static Model Model(String name, Object[] profiles) {
        return new Model(name, profiles);
    }

    public static Model Banister(String name) {
        return new Banister(name);
    }

    public Object profileForLocalFace(Shape shape, EnumFacing face) {
        if (profiles != null)
            return profiles[face.ordinal()];
        else
            return null;
    }

    public double placementOffsetX() {
        return 0;
    }

    public abstract void renderShape(TileShape te,
                                     ITexture[] textures, RenderTargetBase target, Trans3 t,
                                     boolean renderBase, boolean renderSecondary);

    public ItemStack newStack(Shape shape, IBlockState materialState, int stackSize) {
        TileShape te = new TileShape(shape, materialState);
        int light = te.baseBlockState.getLightValue();
        return TileArchitecture.blockStackWithTileEntity(ArchitectureMod.CONTENT.blockShape, stackSize, light, te);
    }

    public ItemStack newStack(Shape shape, Block materialBlock, int materialMeta, int stackSize) {
        return newStack(shape, materialBlock.getStateFromMeta(materialMeta), stackSize);
    }

    public boolean orientOnPlacement(EntityPlayer player, TileShape te,
                                     BlockPos npos, IBlockState nstate, TileEntity nte, EnumFacing otherFace, Vector3 hit) {
        if (nte instanceof TileShape)
            return orientOnPlacement(player, te, (TileShape) nte, otherFace, hit);
        else
            return orientOnPlacement(player, te, null, otherFace, hit);
    }

    public boolean orientOnPlacement(EntityPlayer player, TileShape te, TileShape nte, EnumFacing otherFace,
                                     Vector3 hit) {
        //boolean debug = !te.getWorld().isRemote;
        if (nte != null && !player.isSneaking()) {
            Object otherProfile = Profile.getProfileGlobal(nte.shape, nte.getSide(), nte.getTurn(), otherFace);
            if (otherProfile != null) {
                EnumFacing thisFace = otherFace.getOpposite();
                for (int i = 0; i < 4; i++) {
                    int turn = (nte.getTurn() + i) & 3;
                    Object thisProfile = Profile.getProfileGlobal(te.shape, nte.getSide(), turn, thisFace);
                    if (Profile.matches(thisProfile, otherProfile)) {
                        //if (debug)
                        //	System.out.printf("ShapeKind.orientOnPlacement: side %s turn %s\n", nte.side, turn);
                        te.setSide(nte.getSide());
                        te.setTurn(turn);
                        te.setOffsetX(nte.getOffsetX());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean canPlaceUpsideDown() {
        return true;
    }

    public double sideZoneSize() {
        return 1 / 4d;
    }

    public boolean highlightZones() {
        return false;
    }

    public void onChiselUse(TileShape te, EntityPlayer player, EnumFacing face, Vector3 hit) {
        EnumFacing side = zoneHit(face, hit);
        //System.out.printf("ShapeKind.onChiselUse: face = %s, hit = %s, side = %s\n", face, hit, side);
        if (side != null)
            chiselUsedOnSide(te, player, side);
        else
            chiselUsedOnCentre(te, player);
    }

    public void chiselUsedOnSide(TileShape te, EntityPlayer player, EnumFacing side) {
        te.toggleConnectionGlobal(side);
    }

    public void chiselUsedOnCentre(TileShape te, EntityPlayer player) {
        if (te.secondaryBlockState != null) {
            ItemStack stack = newSecondaryMaterialStack(te.secondaryBlockState);
            if (stack != null) {
                if (!Utils.playerIsInCreativeMode(player))
                    Block.spawnAsEntity(te.getWorld(), te.getPos(), stack);
                te.setSecondaryMaterial(null);
            }
        }
    }

    public ItemStack newSecondaryMaterialStack(IBlockState state) {
        if (acceptsCladding())
            return ArchitectureMod.CONTENT.itemCladding.newStack(state, 1);
        else
            return null;
    }

    public void onHammerUse(TileShape te, EntityPlayer player, EnumFacing face, Vector3 hit) {
        if (player.isSneaking())
            te.setSide((te.getSide() + 1) % 6);
        else {
            double dx = te.getOffsetX();
            if (dx != 0) {
                dx = -dx;
                te.setOffsetX(dx);
            }
            if (dx >= 0)
                te.setTurn((te.getTurn() + 1) % 4);
        }
        te.markBlockChanged();
    }

    public EnumFacing zoneHit(EnumFacing face, Vector3 hit) {
        double r = 0.5 - sideZoneSize();
        if (hit.x <= -r && face != WEST) return WEST;
        if (hit.x >= r && face != EAST) return EAST;
        if (hit.y <= -r && face != DOWN) return DOWN;
        if (hit.y >= r && face != UP) return UP;
        if (hit.z <= -r && face != NORTH) return NORTH;
        if (hit.z >= r && face != SOUTH) return SOUTH;
        return null;
    }

    public boolean acceptsCladding() {
        return false;
    }

    //------------------------------------------------------------------------------

    public boolean isValidSecondaryMaterial(IBlockState state) {
        return false;
    }

    public boolean secondaryDefaultsToBase() {
        return false;
    }

    //------------------------------------------------------------------------------

    public AxisAlignedBB getBounds(TileShape te, IBlockAccess world, BlockPos pos, IBlockState state,
                                   Entity entity, Trans3 t) {
        List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
        addCollisionBoxesToList(te, world, pos, state, entity, t, list);
        return Utils.unionOfBoxes(list);
    }

    public void addCollisionBoxesToList(TileShape te, IBlockAccess world, BlockPos pos, IBlockState state,
                                        Entity entity, Trans3 t, List list) {
        int mask = te.shape.occlusionMask;
        int param = mask & 0xff;
        double r, h;
        switch (mask & 0xff00) {
            case 0x000: // 2x2x2 cubelet bitmap
                for (int i = 0; i < 8; i++)
                    if ((mask & (1 << i)) != 0) {
                        Vector3 p = new Vector3(
                                (i & 1) != 0 ? 0.5 : -0.5,
                                (i & 4) != 0 ? 0.5 : -0.5,
                                (i & 2) != 0 ? 0.5 : -0.5);
                        addBox(Vector3.zero, p, t, list);
                    }
                break;
            case 0x100: // Square, full size in Y
                r = param / 16.0;
                addBox(new Vector3(-r, -0.5, -r), new Vector3(r, 0.5, r), t, list);
                break;
            case 0x200: // SLAB, full size in X and Y
                r = param / 32.0;
                addBox(new Vector3(-0.5, -0.5, -r), new Vector3(0.5, 0.5, r), t, list);
                break;
            case 0x300: // SLAB in back corner
                r = ((param & 0xf) + 1) / 16.0; // width and length of slab
                h = ((param >> 4) + 1) / 16.0; // height of slab from bottom
                addBox(new Vector3(-0.5, -0.5, 0.5 - r), new Vector3(-0.5 + r, -0.5 + h, 0.5), t, list);
                break;
            case 0x400: // SLAB at back
            case 0x500: // Slabs at back and right
                r = ((param & 0xf) + 1) / 16.0; // thickness of slab
                h = ((param >> 4) + 1) / 16.0; // height of slab from bottom
                addBox(new Vector3(-0.5, -0.5, 0.5 - r), new Vector3(0.5, -0.5 + h, 0.5), t, list);
                if ((mask & 0x100) != 0)
                    addBox(new Vector3(-0.5, -0.5, -0.5), new Vector3(-0.5 + r, -0.5 + h, 0.5), t, list);
                break;
            default: // Full cube
                addBox(new Vector3(-0.5, -0.5, -0.5), new Vector3(0.5, 0.5, 0.5), t, list);
        }
    }

    protected void addBox(Vector3 p0, Vector3 p1, Trans3 t, List list) {
        //addBox(t.p(p0), t.p(p1), list);
        t.addBox(p0, p1, list);
    }

    //------------------------------------------------------------------------------

    public static class Roof extends ShapeKind {

        static {
            Profile.declareOpposite(RoofProfile.Left, RoofProfile.Right);
        }

        @Override
        public boolean acceptsCladding() {
            return true;
        }

        @Override
        public boolean secondaryDefaultsToBase() {
            return true;
        }

        @Override
        public void renderShape(TileShape te,
                                ITexture[] textures, RenderTargetBase target, Trans3 t,
                                boolean renderBase, boolean renderSecondary) {
            new RenderRoof(te, textures, t, target, renderBase, renderSecondary).render();
        }

        @Override
        public Object profileForLocalFace(Shape shape, EnumFacing face) {
            switch (shape) {
                case ROOF_TILE:
                case ROOF_OVERHANG:
                    switch (face) {
                        case EAST:
                            return RoofProfile.Left;
                        case WEST:
                            return RoofProfile.Right;
                    }
                    break;
                case ROOF_OUTER_CORNER:
                case ROOF_OVERHANG_OUTER_CORNER:
                    switch (face) {
                        case SOUTH:
                            return RoofProfile.Left;
                        case WEST:
                            return RoofProfile.Right;
                    }
                    break;
                case ROOF_INNER_CORNER:
                case ROOF_OVERHANG_INNER_CORNER:
                    switch (face) {
                        case EAST:
                            return RoofProfile.Left;
                        case NORTH:
                            return RoofProfile.Right;
                    }
                    break;
                case ROOF_RIDGE:
                case ROOF_SMART_RIDGE:
                case ROOF_OVERHANG_RIDGE:
                    return RoofProfile.Ridge;
                case ROOF_VALLEY:
                case ROOF_SMART_VALLEY:
                case ROOF_OVERHANG_VALLEY:
                    return RoofProfile.Valley;
            }
            return RoofProfile.None;
        }

//		protected RoofProfile opposite(RoofProfile p) {
//			switch (p) {
//				case Left: return RoofProfile.Right;
//				case Right: return RoofProfile.Left;
//			}
//			return p;
//		}

        protected enum RoofProfile {None, Left, Right, Ridge, Valley}

//		protected EnumFacing localFaceForProfile(Shape shape, Profile p) {
//			switch (shape) {
//				case ROOF_TILE:
//				case ROOF_OVERHANG:
//					switch (p) {
//						case Left: return EAST;
//						case Right: return WEST;
//					}
//					break;
//				case ROOF_OUTER_CORNER:
//				case ROOF_OVERHANG_OUTER_CORNER:
//					switch (p) {
//						case Left: return SOUTH;
//						case Right: return WEST;
//					}
//					break;
//				case ROOF_INNER_CORNER:
//				case ROOF_OVERHANG_INNER_CORNER:
//					switch (p) {
//						case Left: return EAST;
//						case Right: return NORTH;
//					}
//					break;
//				case ROOF_RIDGE:
//				case ROOF_OVERHANG_RIDGE:
//					switch (p) {
//						case Ridge:
//							return EAST;
//					}
//					break;
//				case ROOF_VALLEY:
//				case ROOF_OVERHANG_VALLEY:
//					switch (p) {
//						case Valley:
//							return EAST;
//					}
//					break;
//			}
//			return null;
//		}

    }

    //------------------------------------------------------------------------------

    public static class Model extends ShapeKind {

        protected String modelName;
        private IRenderableModel model;

        public Model(String name, Object[] profiles) {
            this.modelName = "shape/" + name + ".objson";
            this.profiles = profiles;
        }

        @Override
        public boolean secondaryDefaultsToBase() {
            return true;
        }

        @Override
        public AxisAlignedBB getBounds(TileShape te, IBlockAccess world, BlockPos pos, IBlockState state,
                                       Entity entity, Trans3 t) {
            return t.t(getModel().getBounds());
        }

        @Override
        public void renderShape(TileShape te,
                                ITexture[] textures, RenderTargetBase target, Trans3 t,
                                boolean renderBase, boolean renderSecondary) {
            IRenderableModel model = getModel();
            model.render(t, target, textures);
        }

        protected IRenderableModel getModel() {
            if (model == null)
                model = ClientProxy.RENDERING_MANAGER.getModel(modelName);
            return model;
        }

        @Override
        public boolean acceptsCladding() {
            RenderableModel model = (RenderableModel) getModel();
            for (RenderableModel.Face face : model.faces)
                if (face.texture >= 2)
                    return true;
            return false;
        }

        @Override
        public void addCollisionBoxesToList(TileShape te, IBlockAccess world, BlockPos pos, IBlockState state,
                                            Entity entity, Trans3 t, List list) {
            if (te.shape.occlusionMask == 0)
                getModel().addBoxesToList(t, list);
            else
                super.addCollisionBoxesToList(te, world, pos, state, entity, t, list);
        }

        @Override
        public double placementOffsetX() {
            List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
            getModel().addBoxesToList(Trans3.ident, list);
            AxisAlignedBB bounds = Utils.unionOfBoxes(list);
            if (Shape.debugPlacement) {
                for (AxisAlignedBB box : list)
                    System.out.printf("ShapeKind.Model.placementOffsetX: %s\n", box);
                System.out.printf("ShapeKind.Model.placementOffsetX: bounds = %s\n", bounds);
            }
            return 0.5 * (1 - (bounds.maxX - bounds.minX));
        }

    }

    public static abstract class Window extends ShapeKind {

        public EnumFacing[] frameSides;

        public boolean[] frameAlways;
        public FrameKind[] frameKinds;
        public EnumFacing[] frameOrientations;
        public Trans3[] frameTrans;

        @Override
        public boolean orientOnPlacement(EntityPlayer player, TileShape te, TileShape nte, EnumFacing otherFace,
                                         Vector3 hit) {
            int turn = -1;
            // If click is on side of a non-window block, orient perpendicular to it
            if (!player.isSneaking() && (nte == null || !(nte.shape.kind instanceof ShapeKind.Window))) {
                switch (otherFace) {
                    case EAST:
                    case WEST:
                        turn = 0;
                        break;
                    case NORTH:
                    case SOUTH:
                        turn = 1;
                        break;
                }
            }
            if (turn >= 0) {
                te.setSide(0);
                te.setTurn(turn);
                return true;
            } else
                return false;
        }

        public FrameKind frameKindForLocalSide(EnumFacing side) {
            return frameKinds[side.ordinal()];
        }

        public EnumFacing frameOrientationForLocalSide(EnumFacing side) {
            return frameOrientations[side.ordinal()];
        }

        @Override
        public boolean canPlaceUpsideDown() {
            return false;
        }

        @Override
        public double sideZoneSize() {
            return 1 / 8d; // 3/32d;
        }

        @Override
        public boolean highlightZones() {
            return true;
        }

        @Override
        public void renderShape(TileShape te,
                                ITexture[] textures, RenderTargetBase target, Trans3 t,
                                boolean renderBase, boolean renderSecondary) {
            new RenderWindow(te, textures, t, target, renderBase, renderSecondary).render();
        }

        @Override
        public ItemStack newSecondaryMaterialStack(IBlockState state) {
            return BlockHelper.blockStackWithState(state, 1);
        }

//		@Override
//		public void chiselUsedOnCentre(ShapeTE te, EntityPlayer player) {
//			if (te.secondaryBlockState != null) {
//				ItemStack stack = BaseUtils.blockStackWithState(te.secondaryBlockState, 1);
//				dropSecondaryMaterial(te, player, stack);
//			}
//		}

        @Override
        public boolean isValidSecondaryMaterial(IBlockState state) {
            Block block = state.getBlock();
            return block == Blocks.GLASS_PANE || block == Blocks.STAINED_GLASS_PANE;
        }

        @Override
        public void addCollisionBoxesToList(TileShape te, IBlockAccess world, BlockPos pos, IBlockState state,
                                            Entity entity, Trans3 t, List list) {
            final double r = 1 / 8d, s = 3 / 32d;
            double[] e = new double[4];
            addCentreBoxesToList(r, s, t, list);
            for (int i = 0; i <= 3; i++) {
                boolean frame = frameAlways[i] || !isConnectedGlobal(te, t.t(frameSides[i]));
                if (entity == null || frame) {
                    Trans3 ts = t.t(frameTrans[i]);
                    addFrameBoxesToList(i, r, s, ts, list);
                }
                e[i] = frame ? 0.5 - r : 0.5;
            }
            if (te.secondaryBlockState != null)
                addGlassBoxesToList(r, s, 1 / 32d, e, t, list);
        }

        protected void addCentreBoxesToList(double r, double s, Trans3 t, List list) {
        }

        protected void addFrameBoxesToList(int i, double r, double s, Trans3 ts, List list) {
            ts.addBox(-0.5, -0.5, -s, 0.5, -0.5 + r, s, list);
        }

        protected void addGlassBoxesToList(double r, double s, double w, double e[], Trans3 t, List list) {
            t.addBox(-e[3], -e[0], -w, e[1], e[2], w, list);
        }

        protected boolean isConnectedGlobal(TileShape te, EnumFacing globalDir) {
            return getConnectedWindowGlobal(te, globalDir) != null;
        }

        public TileShape getConnectedWindowGlobal(TileShape te, EnumFacing globalDir) {
            EnumFacing thisLocalDir = te.localFace(globalDir);
            FrameKind thisFrameKind = frameKindForLocalSide(thisLocalDir);
            if (thisFrameKind != FrameKind.None) {
                EnumFacing thisOrient = frameOrientationForLocalSide(thisLocalDir);
                TileShape nte = te.getConnectedNeighbourGlobal(globalDir);
                if (nte != null && nte.shape.kind instanceof Window) {
                    Window otherKind = (Window) nte.shape.kind;
                    EnumFacing otherLocalDir = nte.localFace(globalDir.getOpposite());
                    FrameKind otherFrameKind = otherKind.frameKindForLocalSide(otherLocalDir);
                    if (otherFrameKind != FrameKind.None) {
                        EnumFacing otherOrient = otherKind.frameOrientationForLocalSide(otherLocalDir);
                        if (framesMatch(thisFrameKind, otherFrameKind,
                                te.globalFace(thisOrient), nte.globalFace(otherOrient)))
                            return nte;
                    }
                }
            }
            return null;
        }

        protected boolean framesMatch(FrameKind kind1, FrameKind kind2,
                                      EnumFacing orient1, EnumFacing orient2) {
            if (kind1 == kind2) {
                switch (kind1) {
                    case Plain:
                        return orient1.getAxis() == orient2.getAxis();
                    default:
                        return orient1 == orient2;
                }
            }
            return false;
        }

        public enum FrameKind {None, Plain, Corner}

//		protected EnumFacing getFrameOrientationGlobal(ShapeTE te, EnumFacing globalDir) {
//			Trans3 t = te.localToGlobalRotation();
//			EnumFacing localDir = t.it(globalDir);
//			return frameOrientations[localDir.ordinal()];
//		}

    }

    //------------------------------------------------------------------------------

    public static class Cladding extends ShapeKind {

        @Override
        public void renderShape(TileShape te,
                                ITexture[] textures, RenderTargetBase target, Trans3 t,
                                boolean renderBase, boolean renderSecondary) {
        }

        @Override
        public ItemStack newStack(Shape shape, Block materialBlock, int materialMeta, int stackSize) {
            return ArchitectureMod.CONTENT.itemCladding.newStack(materialBlock, materialMeta, stackSize);
        }

    }

    public static class Banister extends Model {

        public Banister(String modelName) {
            super(modelName, Profile.Generic.tbOffset);
        }

        private static EnumFacing stairsFacing(IBlockState state) {
            return state.getValue(BlockStairs.FACING);
        }

        private static int stairsSide(IBlockState state) {
            if (state.getValue(BlockStairs.HALF) == BlockStairs.EnumHalf.TOP)
                return 1;
            else
                return 0;
        }

        @Override
        public boolean orientOnPlacement(EntityPlayer player, TileShape te,
                                         BlockPos npos, IBlockState nstate, TileEntity nte, EnumFacing otherFace, Vector3 hit) {
            //System.out.printf("Banister.orientOnPlacement: nstate = %s\n", nstate);
            if (!player.isSneaking()) {
                Block nblock = nstate.getBlock();
                boolean placedOnStair = false;
                int nside = -1; // Side that the neighbouring block is placed on
                int nturn = -1; // Turn of the neighbouring block
                if (BlockStairs.isBlockStairs(nstate) && (otherFace == UP || otherFace == DOWN)) {
                    placedOnStair = true;
                    nside = stairsSide(nstate);
                    nturn = MiscUtils.turnToFace(SOUTH, stairsFacing(nstate));
                    if (nside == 1 && (nturn & 1) == 0)
                        nturn ^= 2;
                } else if (nblock instanceof BlockShape) {
                    if (nte instanceof TileShape) {
                        placedOnStair = true;
                        nside = ((TileShape) nte).getSide();
                        nturn = ((TileShape) nte).getTurn();
                    }
                }
                if (placedOnStair) {
                    int side = otherFace.getOpposite().ordinal();
                    if (side == nside) {
                        Vector3 h = Trans3.sideTurn(side, 0).ip(hit);
                        double offx = te.shape.offsetXForPlacementHit(side, nturn, hit);
                        te.setSide(side);
                        te.setTurn(nturn & 3);
                        te.setOffsetX(offx);
                        return true;
                    }
                }
            }
            return super.orientOnPlacement(player, te, npos, nstate, nte, otherFace, hit);
        }

        @Override
        public double placementOffsetX() {
            return 6 / 16d;
        }

    }

    //------------------------------------------------------------------------------

}
