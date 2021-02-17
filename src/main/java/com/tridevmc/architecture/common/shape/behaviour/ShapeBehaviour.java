package com.tridevmc.architecture.common.shape.behaviour;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nonnull;
import java.util.Objects;

import static net.minecraft.util.Direction.*;

public class ShapeBehaviour {

    private static final LoadingCache<BehaviourState, VoxelShape> SHAPE_CACHE = CacheBuilder.newBuilder().build(new CacheLoader<BehaviourState, VoxelShape>() {
        public VoxelShape load(@Nonnull BehaviourState behaviourState) {
            return behaviourState.shapeBehaviour.getCollisionBox(behaviourState.tile, behaviourState.world, behaviourState.pos, behaviourState.state, behaviourState.entity, behaviourState.transform);
        }
    });

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
            EnumShape neighbourShape = neighbourTile.getArchitectureShape();
            Object otherProfile = Profile.getProfileGlobal(neighbourShape, neighbourTile.getSide(), neighbourTile.getTurn(), otherFace);
            if (otherProfile != null) {
                Direction thisFace = otherFace.getOpposite();
                for (int i = 0; i < 4; i++) {
                    int turn = (neighbourTile.getTurn() + i) & 3;
                    Object thisProfile = Profile.getProfileGlobal(neighbourShape, neighbourTile.getSide(), turn, thisFace);
                    if (Profile.matches(thisProfile, otherProfile)) {
                        tile.setSide(neighbourTile.getSide());
                        tile.setTurn((byte) turn);
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
        if (te.getSecondaryBlockState() != null) {
            ItemStack stack = this.newSecondaryMaterialStack(te.getSecondaryBlockState());
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

    @Nonnull
    public final VoxelShape getBounds(TileShape te, IBlockReader world, BlockPos pos, BlockState state,
                                Entity entity, Trans3 t) {
        return this.getCollisionBoxCached(te, world, pos, state, entity, t);
    }

    @Nonnull
    public final VoxelShape getCollisionBoxCached(TileShape te, IBlockReader world, BlockPos pos, BlockState state, Entity entity, Trans3 t) {
        BehaviourState bState = new BehaviourState(this, te, world, pos, state, entity, t);
        VoxelShape out = SHAPE_CACHE.getUnchecked(bState);
        if (out.isEmpty()) {
            SHAPE_CACHE.invalidate(bState);
        }
        return out;
    }

    @Nonnull
    @Deprecated //TODO: Default implementation needs to be nuked. All the old collision code is too janky.
    protected VoxelShape getCollisionBox(TileShape te, IBlockReader world, BlockPos pos, BlockState state,
                                       Entity entity, Trans3 t) {
        VoxelShape shapeOut = VoxelShapes.empty();
        int mask = te.getArchitectureShape().occlusionMask;
        int param = mask & 0xff;
        double r, h;
        switch (mask & 0xff00) {
            case 0x000: // 2x2x2 cubelet bitmap
                for (int i = 0; i < 8; i++)
                    if ((mask & (1 << i)) != 0) {
                        Vector3 p = new Vector3(
                                (i & 1) != 0 ? 1 : 0,
                                (i & 4) != 0 ? 1 : 0,
                                (i & 2) != 0 ? 1 : 0);
                        shapeOut = this.addBox(Vector3.zero, p, t, shapeOut);
                    }
                break;
            case 0x100: // Square, full size in Y
                r = param / 16.0;
                shapeOut = this.addBox(new Vector3(-r, 0, -r), new Vector3(r, 1, r), t, shapeOut);
                break;
            case 0x200: // SLAB, full size in X and Y
                r = param / 32.0;
                shapeOut = this.addBox(new Vector3(0, 0, -r), new Vector3(1, 1, r), t, shapeOut);
                break;
            case 0x300: // SLAB in back corner
                r = ((param & 0xf) + 1) / 16.0; // width and length of slab
                h = ((param >> 4) + 1) / 16.0; // height of slab from bottom
                shapeOut = this.addBox(new Vector3(0, 0, 1 - r), new Vector3(0 + r, 0 + h, 1), t, shapeOut);
                break;
            case 0x400: // SLAB at back
            case 0x500: // Slabs at back and right
                r = ((param & 0xf) + 1) / 16.0; // thickness of slab
                h = ((param >> 4) + 1) / 16.0; // height of slab from bottom
                shapeOut = this.addBox(new Vector3(0, 0, 1 - r), new Vector3(1, 0 + h, 1), t, shapeOut);
                if ((mask & 0x100) != 0)
                    shapeOut = this.addBox(new Vector3(0, 0, 0), new Vector3(0 + r, 0 + h, 1), t, shapeOut);
                break;
            default: // Full cube
                shapeOut = this.addBox(new Vector3(0, 0, 0), new Vector3(1, 1, 1), t, shapeOut);
        }
        return shapeOut;
    }

    @Nonnull
    protected VoxelShape addBox(Vector3 p0, Vector3 p1, Trans3 t, VoxelShape shape) {
        return VoxelShapes.or(shape, t.t(VoxelShapes.create(p0.x, p0.y, p0.z, p1.x, p1.y, p1.z)));
    }

    private class BehaviourState {
        private final ShapeBehaviour shapeBehaviour;
        private final TileShape tile;
        private final IBlockReader world;
        private final BlockPos pos;
        private final BlockState state;
        private final Entity entity;
        private final Trans3 transform;

        private BehaviourState(ShapeBehaviour shapeBehaviour, TileShape tile, IBlockReader world, BlockPos pos, BlockState state, Entity entity, Trans3 transform) {
            this.shapeBehaviour = shapeBehaviour;
            this.tile = tile;
            this.world = world;
            this.pos = pos;
            this.state = state;
            this.entity = entity;
            this.transform = transform;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BehaviourState)) return false;
            BehaviourState that = (BehaviourState) o;
            return Objects.equals(this.state, that.state) &&
                    Objects.equals(this.transform, that.transform) &&
                    Objects.equals(this.tile.getDisabledConnections(), that.tile.getDisabledConnections()) &&
                    Objects.equals(this.tile.getSide(), that.tile.getSide()) &&
                    Objects.equals(this.tile.getTurn(), that.tile.getTurn());
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.state, this.transform, this.tile.getDisabledConnections(), this.tile.getSide(), this.tile.getTurn());
        }
    }
}
