package com.tridevmc.architecture.common.shape.behaviour;

import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.helpers.Profile;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Utils;
import com.tridevmc.architecture.common.helpers.Vector3;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.tile.TileShape;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.util.Direction.*;

public class ShapeBehaviour {

    public static ShapeBehaviour DEFAULT = new ShapeBehaviour();

    public Object[] profiles; // indexed by local face

    public Object profileForLocalFace(EnumShape shape, Direction face) {
        if (this.profiles != null)
            return this.profiles[face.ordinal()];
        else
            return null;
    }

    public boolean orientOnPlacement(PlayerEntity player, TileShape tile,
                                     BlockPos neighbourPos, BlockState neighbourState, TileEntity neighbourTile,
                                     Direction otherFace, Vector3 hit) {
        if (neighbourTile instanceof TileShape)
            return this.orientOnPlacement(player, tile, (TileShape) neighbourTile, otherFace, hit);
        else
            return this.orientOnPlacement(player, tile, null, otherFace, hit);
    }

    public boolean orientOnPlacement(PlayerEntity player, TileShape tile, TileShape neighbourTile, Direction otherFace, Vector3 hit) {
        if (neighbourTile != null && !player.isCrouching()) {
            Object otherProfile = Profile.getProfileGlobal(neighbourTile.shape, neighbourTile.getSide(), neighbourTile.getTurn(), otherFace);
            if (otherProfile != null) {
                Direction thisFace = otherFace.getOpposite();
                for (int i = 0; i < 4; i++) {
                    int turn = (neighbourTile.getTurn() + i) & 3;
                    Object thisProfile = Profile.getProfileGlobal(tile.shape, neighbourTile.getSide(), turn, thisFace);
                    if (Profile.matches(thisProfile, otherProfile)) {
                        tile.setSide(neighbourTile.getSide());
                        tile.setTurn(turn);
                        tile.setOffsetX(neighbourTile.getOffsetX());
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public double placementOffsetX() {
        return 0;
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

    public void onChiselUse(TileShape te, PlayerEntity player, Direction face, Vector3 hit) {
        Direction side = this.zoneHit(face, hit);
        if (side != null)
            this.chiselUsedOnSide(te, player, side);
        else
            this.chiselUsedOnCentre(te, player);
    }

    public void chiselUsedOnSide(TileShape te, PlayerEntity player, Direction side) {
        te.toggleConnectionGlobal(side);
    }

    public void chiselUsedOnCentre(TileShape te, PlayerEntity player) {
        if (te.secondaryBlockState != null) {
            ItemStack stack = this.newSecondaryMaterialStack(te.secondaryBlockState);
            if (stack != null) {
                if (!Utils.playerIsInCreativeMode(player))
                    Block.spawnAsEntity(te.getWorld(), te.getPos(), stack);
                te.setSecondaryMaterial(null);
            }
        }
    }

    public ItemStack newSecondaryMaterialStack(BlockState state) {
        if (this.acceptsCladding())
            return ArchitectureMod.CONTENT.itemCladding.newStack(state, 1);
        else
            return null;
    }

    public void onHammerUse(TileShape te, PlayerEntity player, Direction face, Vector3 hit) {
        if (player.isCrouching())
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

    public Direction zoneHit(Direction face, Vector3 hit) {
        double r = 0.5 - this.sideZoneSize();
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

    public boolean isValidSecondaryMaterial(BlockState state) {
        return false;
    }

    public boolean secondaryDefaultsToBase() {
        return false;
    }

    public AxisAlignedBB getBounds(TileShape te, IBlockReader world, BlockPos pos, BlockState state,
                                   Entity entity, Trans3 t) {
        List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
        this.addCollisionBoxesToList(te, world, pos, state, entity, t, list);
        return Utils.unionOfBoxes(list);
    }

    public void addCollisionBoxesToList(TileShape te, IBlockReader world, BlockPos pos, BlockState state,
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
                        this.addBox(Vector3.zero, p, t, list);
                    }
                break;
            case 0x100: // Square, full size in Y
                r = param / 16.0;
                this.addBox(new Vector3(-r, -0.5, -r), new Vector3(r, 0.5, r), t, list);
                break;
            case 0x200: // SLAB, full size in X and Y
                r = param / 32.0;
                this.addBox(new Vector3(-0.5, -0.5, -r), new Vector3(0.5, 0.5, r), t, list);
                break;
            case 0x300: // SLAB in back corner
                r = ((param & 0xf) + 1) / 16.0; // width and length of slab
                h = ((param >> 4) + 1) / 16.0; // height of slab from bottom
                this.addBox(new Vector3(-0.5, -0.5, 0.5 - r), new Vector3(-0.5 + r, -0.5 + h, 0.5), t, list);
                break;
            case 0x400: // SLAB at back
            case 0x500: // Slabs at back and right
                r = ((param & 0xf) + 1) / 16.0; // thickness of slab
                h = ((param >> 4) + 1) / 16.0; // height of slab from bottom
                this.addBox(new Vector3(-0.5, -0.5, 0.5 - r), new Vector3(0.5, -0.5 + h, 0.5), t, list);
                if ((mask & 0x100) != 0)
                    this.addBox(new Vector3(-0.5, -0.5, -0.5), new Vector3(-0.5 + r, -0.5 + h, 0.5), t, list);
                break;
            default: // Full cube
                this.addBox(new Vector3(-0.5, -0.5, -0.5), new Vector3(0.5, 0.5, 0.5), t, list);
        }
    }

    protected void addBox(Vector3 p0, Vector3 p1, Trans3 t, List list) {
        //addBox(t.p(p0), t.p(p1), list);
        t.addBox(p0, p1, list);
    }
}
