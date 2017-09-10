//------------------------------------------------------------------------------------------------
//
//   Greg's Mod Base for 1.10 - 3D Transformation
//
//------------------------------------------------------------------------------------------------

package com.elytradev.architecture.common.helpers;

import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;

import java.util.List;

import static com.elytradev.architecture.common.helpers.Vector3.getDirectionVec;
import static java.lang.Math.round;

public class Trans3 {

    public static Trans3 ident = new Trans3(Vector3.zero);
    public static Trans3 blockCenter = new Trans3(Vector3.blockCenter);

    public static Trans3 sideTurnRotations[][] = new Trans3[6][4];

    static {
        for (int side = 0; side < 6; side++)
            for (int turn = 0; turn < 4; turn++)
                sideTurnRotations[side][turn] = new Trans3(Vector3.zero, Matrix3.sideTurnRotations[side][turn]);
    }

    public Vector3 offset;
    public Matrix3 rotation;
    public double scaling;

    public Trans3(Vector3 v) {
        this(v, Matrix3.ident);
    }

    public Trans3(Vector3 v, Matrix3 m) {
        this(v, m, 1.0);
    }

    public Trans3(Vector3 v, Matrix3 m, double s) {
        offset = v;
        rotation = m;
        scaling = s;
    }

    public Trans3(double dx, double dy, double dz) {
        this(new Vector3(dx, dy, dz), Matrix3.ident, 1.0);
    }

    public Trans3(BlockPos pos) {
        this(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    public static Trans3 blockCenter(BlockPos pos) {
        return new Trans3(Vector3.blockCenter(pos));
    }

    public static Trans3 sideTurn(int side, int turn) {
        return sideTurnRotations[side][turn];
    }

    public static Trans3 sideTurn(double x, double y, double z, int side, int turn) {
        return sideTurn(new Vector3(x, y, z), side, turn);
    }

    public static Trans3 blockCenterSideTurn(int side, int turn) {
        return sideTurn(Vector3.blockCenter, side, turn);
    }

    public static Trans3 sideTurn(Vector3 v, int side, int turn) {
        return new Trans3(v, Matrix3.sideTurnRotations[side][turn]);
    }

    public static int turnFor(Entity e, int side) {
        if (side > 1)
            return 0;
        int rot = (int) round(e.rotationYaw / 90);
        if (side == 0)
            rot = 2 - rot;
        else
            rot = 2 + rot;
        return rot & 0x3;
    }

    public static AxisAlignedBB boxEnclosing(Vector3 p, Vector3 q) {
        return new AxisAlignedBB(p.x, p.y, p.z, q.x, q.y, q.z);
    }

    public Trans3 translate(Vector3 v) {
        if (v == Vector3.zero)
            return this;
        else
            return translate(v.x, v.y, v.z);
    }

    public Trans3 translate(double dx, double dy, double dz) {
        return new Trans3(
                offset.add(rotation.mul(dx * scaling, dy * scaling, dz * scaling)),
                rotation,
                scaling);
    }

    public Trans3 rotate(Matrix3 m) {
        return new Trans3(offset, rotation.mul(m), scaling);
    }

    public Trans3 rotX(double deg) {
        return rotate(Matrix3.rotX(deg));
    }

    public Trans3 rotY(double deg) {
        return rotate(Matrix3.rotY(deg));
    }

    public Trans3 rotZ(double deg) {
        return rotate(Matrix3.rotZ(deg));
    }

    public Trans3 scale(double s) {
        return new Trans3(offset, rotation, scaling * s);
    }

    public Trans3 side(EnumFacing dir) {
        return side(dir.ordinal());
    }

    public Trans3 side(int i) {
        return rotate(Matrix3.sideRotations[i]);
    }

    public Trans3 turn(int i) {
        return rotate(Matrix3.turnRotations[i]);
    }

    public Trans3 t(Trans3 t) {
        return new Trans3(
                offset.add(rotation.mul(t.offset).mul(scaling)),
                rotation.mul(t.rotation),
                scaling * t.scaling);
    }

    public Vector3 p(double x, double y, double z) {
        return p(new Vector3(x, y, z));
    }

    public Vector3 p(Vector3 u) {
        return offset.add(rotation.mul(u.mul(scaling)));
    }

    public Vector3 ip(double x, double y, double z) {
        return ip(new Vector3(x, y, z));
    }

    public Vector3 ip(Vector3 u) {
        return rotation.imul(u.sub(offset)).mul(1.0 / scaling);
    }

    public Vector3 v(double x, double y, double z) {
        return v(new Vector3(x, y, z));
    }

    public Vector3 iv(double x, double y, double z) {
        return iv(new Vector3(x, y, z));
    }

    public Vector3 v(Vec3i u) {
        return v(u.getX(), u.getY(), u.getZ());
    }

    public Vector3 iv(Vec3i u) {
        return iv(u.getX(), u.getY(), u.getZ());
    }

    public Vector3 v(Vector3 u) {
        return rotation.mul(u.mul(scaling));
    }

    public Vector3 v(EnumFacing f) {
        return v(getDirectionVec(f));
    }

    public Vector3 iv(EnumFacing f) {
        return iv(getDirectionVec(f));
    }

    public Vector3 iv(Vector3 u) {
        return rotation.imul(u).mul(1.0 / scaling);
    }

    public Vector3 iv(Vec3d u) {
        return iv(u.x, u.y, u.z);
    }

    public AxisAlignedBB t(AxisAlignedBB box) {
        return boxEnclosing(p(box.minX, box.minY, box.minZ), p(box.maxX, box.maxY, box.maxZ));
    }

    public AxisAlignedBB box(Vector3 p0, Vector3 p1) {
        return boxEnclosing(p(p0), p(p1));
    }

    public EnumFacing t(EnumFacing f) {
        return v(f).facing();
    }

    public EnumFacing it(EnumFacing f) {
        return iv(f).facing();
    }

    public void addBox(Vector3 p0, Vector3 p1, List list) {
        addBox(p0.x, p0.y, p0.z, p1.x, p1.y, p1.z, list);
    }

    public void addBox(double x0, double y0, double z0, double x1, double y1, double z1, List list) {
        AxisAlignedBB box = boxEnclosing(p(x0, y0, z0), p(x1, y1, z1));
        list.add(box);
    }

}
