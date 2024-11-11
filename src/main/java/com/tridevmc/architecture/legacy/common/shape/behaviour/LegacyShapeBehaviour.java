package com.tridevmc.architecture.legacy.common.shape.behaviour;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tridevmc.architecture.common.ArchitectureMod;

import com.tridevmc.architecture.legacy.common.shape.LegacyEnumShape;
import com.tridevmc.architecture.legacy.math.LegacyTrans3;
import com.tridevmc.architecture.legacy.math.LegacyVector3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nonnull;
import java.util.Objects;

import static net.minecraft.core.Direction.*;

@Deprecated
public class LegacyShapeBehaviour {

    public static LegacyShapeBehaviour DEFAULT = new LegacyShapeBehaviour();

    public Object[] profiles; // indexed by local face

    public Object profileForLocalFace(LegacyEnumShape shape, Direction face) {
        if (this.profiles != null)
            return this.profiles[face.ordinal()];
        else
            return null;
    }

    public double placementOffsetX() {
        return 0;
    }

    public double sideZoneSize() {
        return 1 / 4d;
    }


    public ItemStack newSecondaryMaterialStack(BlockState state) {
        if (this.acceptsCladding())
            return ArchitectureMod.CONTENT.itemCladding.newStack(state, 1);
        else
            return null;
    }

    public Direction zoneHit(Direction face, LegacyVector3 hit) {
        double r = 0.5 - this.sideZoneSize();
        if (hit.x() <= -r && face != WEST) return WEST;
        if (hit.x() >= r && face != EAST) return EAST;
        if (hit.y() <= -r && face != DOWN) return DOWN;
        if (hit.y() >= r && face != UP) return UP;
        if (hit.z() <= -r && face != NORTH) return NORTH;
        if (hit.z() >= r && face != SOUTH) return SOUTH;
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
    protected VoxelShape addBox(LegacyVector3 p0, LegacyVector3 p1, LegacyTrans3 t, VoxelShape shape) {
        return Shapes.or(shape, t.t(Shapes.create(p0.x(), p0.y(), p0.z(), p1.x(), p1.y(), p1.z())));
    }

    private class BehaviourState {

        private final LegacyShapeBehaviour shapeBehaviour;
        private final Object tile;
        private final BlockGetter world;
        private final BlockPos pos;
        private final BlockState state;
        private final Entity entity;
        private final LegacyTrans3 transform;

        private BehaviourState(LegacyShapeBehaviour shapeBehaviour, Object tile, BlockGetter world, BlockPos pos, BlockState state, Entity entity, LegacyTrans3 transform) {
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
            if (!(o instanceof BehaviourState that)) return false;
            return Objects.equals(this.state, that.state) &&
                    Objects.equals(this.transform, that.transform);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.state, this.transform);
        }

    }

}
