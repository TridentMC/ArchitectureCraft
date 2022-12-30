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

package com.tridevmc.architecture.legacy.math;

import com.google.common.base.MoreObjects;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import com.tridevmc.architecture.core.physics.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.tridevmc.architecture.legacy.math.LegacyVector3.getDirectionVec;
import static java.lang.Math.round;

@Deprecated
public class LegacyTrans3 {

    public static LegacyTrans3 ident = new LegacyTrans3(LegacyVector3.ZERO);
    public static LegacyTrans3 blockCenter = new LegacyTrans3(LegacyVector3.BLOCK_CENTER);
    public static LegacyTrans3[][] sideTurnRotations = new LegacyTrans3[6][4];

    static {
        for (int side = 0; side < 6; side++)
            for (int turn = 0; turn < 4; turn++)
                sideTurnRotations[side][turn] = new LegacyTrans3(LegacyVector3.ZERO, LegacyMatrix3.sideTurnRotations[side][turn]);
    }

    public final LegacyVector3 offset;
    public final LegacyMatrix3 rotation;
    public final double scaling;
    private Transformation mcTrans;

    /**
     * Constructs a new transformation with the given offset and identity
     * rotation and scaling.
     *
     * @param v the offset of the transformation.
     */
    public LegacyTrans3(LegacyVector3 v) {
        this(v, LegacyMatrix3.ident);
    }

    /**
     * Constructs a new transformation with the given offset and rotation
     * and identity scaling.
     *
     * @param v the offset of the transformation.
     * @param m the rotation of the transformation.
     */
    public LegacyTrans3(LegacyVector3 v, LegacyMatrix3 m) {
        this(v, m, 1.0);
    }

    /**
     * Constructs a new transformation with the given offset, rotation,
     * and scaling.
     *
     * @param v the offset of the transformation.
     * @param m the rotation of the transformation.
     * @param s the scaling factor of the transformation.
     */
    public LegacyTrans3(LegacyVector3 v, LegacyMatrix3 m, double s) {
        this.offset = v;
        this.rotation = m;
        this.scaling = s;
    }

    /**
     * Constructs a new transformation with the given offset and identity
     * rotation and scaling.
     *
     * @param dx the x component of the offset.
     * @param dy the y component of the offset.
     * @param dz the z component of the offset.
     */
    public LegacyTrans3(double dx, double dy, double dz) {
        this(new LegacyVector3(dx, dy, dz), LegacyMatrix3.ident, 1.0);
    }

    /**
     * Constructs a new transformation with the given offset and identity
     * rotation and scaling.
     *
     * @param pos the position of the offset.
     */
    public LegacyTrans3(BlockPos pos) {
        this(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    /**
     * Creates a new transformation at the given block position.
     *
     * @param pos the position of the block.
     * @return the transformation with the block position as its offset.
     */
    public static LegacyTrans3 blockCenter(BlockPos pos) {
        return new LegacyTrans3(LegacyVector3.blockCenter(pos));
    }

    /**
     * Gets the side turn transformation for the given side and turn.
     *
     * @param side the side of the block.
     * @param turn the turn around the center of the block.
     * @return the side turn transformation.
     */
    public static LegacyTrans3 sideTurn(int side, int turn) {
        return sideTurnRotations[side][turn];
    }

    /**
     * Gets the side turn transformation for the given position, side, and turn.
     *
     * @param x    the x coordinate of the position.
     * @param y    the y coordinate of the position.
     * @param z    the z coordinate of the position.
     * @param side the side of the block.
     * @param turn the turn around the center of the block.
     * @return the side turn transformation.
     */
    public static LegacyTrans3 sideTurn(double x, double y, double z, int side, int turn) {
        return sideTurn(new LegacyVector3(x, y, z), side, turn);
    }

    /**
     * Gets the side turn transformation for the center of a block, for the
     * given side and turn.
     *
     * @param side the side of the block.
     * @param turn the turn around the center of the block.
     * @return the side turn transformation.
     */
    public static LegacyTrans3 blockCenterSideTurn(int side, int turn) {
        return sideTurn(LegacyVector3.BLOCK_CENTER, side, turn);
    }

    /**
     * Gets the side turn transformation for the given position, side, and turn.
     *
     * @param v    the position.
     * @param side the side of the block.
     * @param turn the turn around the center of the block.
     * @return the side turn transformation.
     */
    public static LegacyTrans3 sideTurn(LegacyVector3 v, int side, int turn) {
        var t = new LegacyTrans3(v);
        return t.translate(LegacyVector3.BLOCK_CENTER)
                .rotate(LegacyMatrix3.sideTurnRotations[side][turn])
                .translate(LegacyVector3.BLOCK_CENTER.x() * -1, LegacyVector3.BLOCK_CENTER.y() * -1, LegacyVector3.BLOCK_CENTER.z() * -1);
        //return new Trans3(v, Matrix3.sideTurnRotations[side][turn]);
    }

    /**
     * Gets the turn around the center of a block for the given entity and side.
     *
     * @param e    the entity.
     * @param side the side of the block.
     * @return the turn around the center of the block.
     */
    public static int turnFor(Entity e, int side) {
        if (side > 1)
            return 0;
        int rot = round(e.getYRot() / 90);
        if (side == 0)
            rot = 2 - rot;
        else
            rot = 2 + rot;
        return rot & 0x3;
    }

    /**
     * Creates a bounding box enclosing the two given points.
     *
     * @param p the first point.
     * @param q the second point.
     * @return the bounding box enclosing the two points.
     */
    public static AABB boxEnclosing(LegacyVector3 p, LegacyVector3 q) {
        return new AABB(p.x(), p.y(), p.z(), q.x(), q.y(), q.z());
    }

    /**
     * Creates a new transformation by translating the current transformation by the given vector.
     *
     * @param v the vector to add to the offset.
     * @return the offset transformation.
     */
    public LegacyTrans3 translate(LegacyVector3 v) {
        if (v == LegacyVector3.ZERO)
            return this;
        else
            return this.translate(v.x(), v.y(), v.z());
    }

    /**
     * Creates a new transformation by translating the current transformation by the given coordinates.
     *
     * @param dx the x component of the vector to add to the offset.
     * @param dy the y component of the vector to add to the offset.
     * @param dz the z component of the vector to add to the offset.
     * @return the offset transformation.
     */
    public LegacyTrans3 translate(double dx, double dy, double dz) {
        return new LegacyTrans3(
                this.offset.add(this.rotation.mul(dx * this.scaling, dy * this.scaling, dz * this.scaling)),
                this.rotation,
                this.scaling);
    }

    /**
     * Creates a new transformation by multiplying the rotation of the current transformation by the given matrix.
     *
     * @param m the matrix to multiply by the rotation.
     * @return the rotated transformation.
     */
    public LegacyTrans3 rotate(LegacyMatrix3 m) {
        return new LegacyTrans3(this.offset, this.rotation.mul(m), this.scaling);
    }

    /**
     * Creates a new transformation by rotating the current transformation by the given angle around the x-axis.
     *
     * @param deg the angle in degrees to rotate around the x-axis.
     * @return the rotated transformation.
     */
    public LegacyTrans3 rotX(double deg) {
        return this.rotate(LegacyMatrix3.rotX(deg));
    }

    /**
     * Creates a new transformation by rotating the current transformation by the given angle around the y-axis.
     *
     * @param deg the angle in degrees to rotate around the y-axis.
     * @return the rotated transformation.
     */
    public LegacyTrans3 rotY(double deg) {
        return this.rotate(LegacyMatrix3.rotY(deg));
    }

    /**
     * Creates a new transformation by rotating the current transformation by the given angle around the z-axis.
     *
     * @param deg the angle in degrees to rotate around the z-axis.
     * @return the rotated transformation.
     */
    public LegacyTrans3 rotZ(double deg) {
        return this.rotate(LegacyMatrix3.rotZ(deg));
    }

    /**
     * Creates a new transformation by scaling the current transformation by the given factor.
     *
     * @param s the scale factor to apply to the transformation.
     * @return the scaled transformation.
     */
    public LegacyTrans3 scale(double s) {
        return new LegacyTrans3(this.offset, this.rotation, this.scaling * s);
    }

    /**
     * Creates a new transformation by rotating the current transformation to face the given side.
     *
     * @param dir the direction to face.
     * @return the rotated transformation.
     */
    public LegacyTrans3 side(Direction dir) {
        return this.side(dir.ordinal());
    }

    /**
     * Creates a new transformation by rotating the current transformation to face a pre-defined side.
     *
     * @param i the index in the "sideRotations" array to use for the rotation.
     * @return the rotated transformation.
     */
    public LegacyTrans3 side(int i) {
        return this.rotate(LegacyMatrix3.sideRotations[i]);
    }

    /**
     * Creates a new transformation by rotating the current transformation by a multiple of 90 degrees.
     *
     * @param i the index in the "turnRotations" array to use for the rotation.
     * @return the rotated transformation.
     */
    public LegacyTrans3 turn(int i) {
        return this.rotate(LegacyMatrix3.turnRotations[i]);
    }

    /**
     * Creates a new transformation by rotating the current transformation around a given origin point by a multiple of 90 degrees.
     *
     * @param origin the point around which to rotate the transformation.
     * @param i      the index in the "turnRotations" array to use for the rotation.
     * @return the rotated transformation.
     */
    public LegacyTrans3 turnAround(LegacyVector3 origin, int i) {
        // Use multiplied variables instead of cross, saves some heap allocation.
        return this.translate(origin)
                .turn(i)
                .translate(origin.x() * -1, origin.y() * -1, origin.z() * -1);
    }

    /**
     * Creates a new transformation by combining the current transformation with the given transformation.
     *
     * @param t the transformation to combine with the current transformation.
     * @return the combined transformation.
     */
    public LegacyTrans3 t(LegacyTrans3 t) {
        return new LegacyTrans3(
                this.offset.add(this.rotation.mul(t.offset).mul(this.scaling)),
                this.rotation.mul(t.rotation),
                this.scaling * t.scaling);
    }

    /**
     * Returns the transformed point.
     *
     * @param x the x coordinate of the point to transform.
     * @param y the y coordinate of the point to transform.
     * @param z the z coordinate of the point to transform.
     * @return the transformed point.
     */
    public LegacyVector3 p(double x, double y, double z) {
        return this.p(new LegacyVector3(x, y, z));
    }

    /**
     * Returns the transformed point.
     *
     * @param u the point to transform.
     * @return the transformed point.
     */
    public LegacyVector3 p(LegacyVector3 u) {
        return this.offset.add(this.rotation.mul(u.mul(this.scaling)));
    }

    /**
     * Returns the inverse transformed point.
     *
     * @param x the x coordinate of the point to inverse transform.
     * @param y the y coordinate of the point to inverse transform.
     * @param z the z coordinate of the point to inverse transform.
     * @return the inverse transformed point.
     */
    public LegacyVector3 ip(double x, double y, double z) {
        return this.ip(new LegacyVector3(x, y, z));
    }

    /**
     * Returns the inverse transformed point.
     *
     * @param u the point to inverse transform.
     * @return the inverse transformed point.
     */
    public LegacyVector3 ip(LegacyVector3 u) {
        return this.rotation.imul(u.sub(this.offset)).mul(1.0 / this.scaling);
    }

    /**
     * Returns the transformed vector.
     *
     * @param x the x coordinate of the vector to transform.
     * @param y the y coordinate of the vector to transform.
     * @param z the z coordinate of the vector to transform.
     * @return the transformed vector.
     */
    public LegacyVector3 v(double x, double y, double z) {
        return this.v(new LegacyVector3(x, y, z));
    }

    /**
     * Returns the inverse transformed vector.
     *
     * @param x the x coordinate of the vector to inverse transform.
     * @param y the y coordinate of the vector to inverse transform.
     * @param z the z coordinate of the vector to inverse transform.
     * @return the inverse transformed vector.
     */
    public LegacyVector3 iv(double x, double y, double z) {
        return this.iv(new LegacyVector3(x, y, z));
    }

    /**
     * Returns the transformed vector.
     *
     * @param u the vector to transform.
     * @return the transformed vector.
     */
    public LegacyVector3 v(Vec3i u) {
        return this.v(u.getX(), u.getY(), u.getZ());
    }

    /**
     * Returns the inverse transformed vector.
     *
     * @param u the vector to inverse transform.
     * @return the inverse transformed vector.
     */
    public LegacyVector3 iv(Vec3i u) {
        return this.iv(u.getX(), u.getY(), u.getZ());
    }

    /**
     * Returns the transformed vector.
     *
     * @param u the vector to transform.
     * @return the transformed vector.
     */
    public LegacyVector3 v(LegacyVector3 u) {
        return this.rotation.mul(u.mul(this.scaling));
    }

    /**
     * Returns the transformed vector for the given direction.
     *
     * @param f the direction to transform.
     * @return the transformed vector.
     */
    public LegacyVector3 v(Direction f) {
        return this.v(getDirectionVec(f));
    }

    /**
     * Returns the inverse transformed vector for the given direction.
     *
     * @param f the direction to inverse transform.
     * @return the inverse transformed vector.
     */
    public LegacyVector3 iv(Direction f) {
        return this.iv(getDirectionVec(f));
    }

    /**
     * Returns the inverse transformed vector.
     *
     * @param u the vector to inverse transform.
     * @return the inverse transformed vector.
     */
    public LegacyVector3 iv(LegacyVector3 u) {
        return this.rotation.imul(u).mul(1.0 / this.scaling);
    }

    /**
     * Returns the transformed axis aligned bounding box.
     *
     * @param box the axis aligned bounding box to transform.
     * @return the transformed axis aligned bounding box.
     */
    public AABB t(AABB box) {
        return boxEnclosing(this.p(box.minX(), box.minY(), box.minZ()), this.p(box.maxX(), box.maxY(), box.maxZ()));
    }

    public net.minecraft.world.phys.AABB t(net.minecraft.world.phys.AABB box) {
        var min = this.p(box.minX, box.minY, box.minZ);
        var max = this.p(box.maxX, box.maxY, box.maxZ);
        return new net.minecraft.world.phys.AABB(min.x(), min.y(), min.z(), max.x(), max.y(), max.z());
    }

    /**
     * Converts the Trans3 object to a Transformation object for use within the Minecraft codebase.
     *
     * @return the Transformation object, this value is cached.
     */
    public Transformation toMCTrans() {
        if (this.mcTrans == null) {
            this.mcTrans = new Transformation(new Matrix4f(new float[]{
                    (float) this.rotation.m[0][0],
                    (float) this.rotation.m[0][1],
                    (float) this.rotation.m[0][2],
                    (float) this.offset.x(),
                    (float) this.rotation.m[1][0],
                    (float) this.rotation.m[1][1],
                    (float) this.rotation.m[1][2],
                    (float) this.offset.y(),
                    (float) this.rotation.m[2][0],
                    (float) this.rotation.m[2][1],
                    (float) this.rotation.m[2][2],
                    (float) this.offset.z(),
                    0.0f,
                    0.0f,
                    0.0f,
                    1.0f}));
        }

        return this.mcTrans;
    }

    /**
     * Transforms the given list of axis-aligned bounding boxes using this transformation.
     *
     * @param boxes the list of bounding boxes to transform.
     * @return the transformed bounding boxes.
     */
    public List<AABB> t(List<AABB> boxes) {
        return boxes.stream().map(this::t).collect(Collectors.toList());
    }

    /**
     * Transforms the given voxel shape using this transformation.
     *
     * @param shape the voxel shape to transform.
     * @return the transformed voxel shape.
     */
    public VoxelShape t(VoxelShape shape) {
        if (this.scaling == 1 && this.rotation.isIdent()) {
            return shape.move(this.offset.x(), this.offset.y(), this.offset.z());
        }
        var boxes = shape.toAabbs().stream().map(this::t).toList();
        VoxelShape out = boxes.isEmpty() ? Shapes.empty() : Shapes.create(boxes.get(0));
        if (boxes.size() > 1) {
            for (int i = 1; i < boxes.size(); i++) {
                out = Shapes.or(out, Shapes.create(boxes.get(i)));
            }
        }
        return out.optimize();
    }

    /**
     * Transforms the given bounding box using this transformation.
     *
     * @param box the bounding box to transform.
     * @return the transformed bounding box.
     */
    public double[] t(double[] box) {
        double[] min = this.p(box[0], box[1], box[2]).toArray();
        double[] max = this.p(box[3], box[4], box[5]).toArray();
        return new double[]{min[0], min[1], min[2], max[0], max[1], max[2]};
    }

    /**
     * Returns an axis-aligned bounding box enclosing the given two points, transformed by this transformation.
     *
     * @param p0 the first point of the box.
     * @param p1 the second point of the box.
     * @return the transformed bounding box.
     */
    public AABB box(LegacyVector3 p0, LegacyVector3 p1) {
        return boxEnclosing(this.p(p0), this.p(p1));
    }

    /**
     * Transforms the given direction using this transformation.
     *
     * @param f the direction to transform.
     * @return the transformed direction.
     */
    public Direction t(Direction f) {
        return this.v(f).facing();
    }

    /**
     * Inversely transforms the given direction using this transformation.
     *
     * @param f the direction to transform.
     * @return the inversely transformed direction.
     */
    public Direction it(Direction f) {
        return this.iv(f).facing();
    }

    /**
     * Adds a transformed axis-aligned bounding box to the given list.
     *
     * @param p0   the first point of the bounding box.
     * @param p1   the second point of the bounding box.
     * @param list the list to add the bounding box to.
     */
    public void addBox(LegacyVector3 p0, LegacyVector3 p1, List<?> list) {
        this.addBox(p0.x(), p0.y(), p0.z(), p1.x(), p1.y(), p1.z(), list);
    }

    /**
     * Adds a transformed axis-aligned bounding box to the given list.
     *
     * @param x0   the x coordinate of the first point of the bounding box.
     * @param y0   the y coordinate of the first point of the bounding box.
     * @param z0   the z coordinate of the first point of the bounding box.
     * @param x1   the x coordinate of the second point of the bounding box.
     * @param y1   the y coordinate of the second point of the bounding box.
     * @param z1   the z coordinate of the second point of the bounding box.
     * @param list the list to add the bounding box to.
     */
    public void addBox(double x0, double y0, double z0, double x1, double y1, double z1, List list) {
        AABB box = boxEnclosing(this.p(x0, y0, z0), this.p(x1, y1, z1));
        list.add(box);
    }

    /**
     * Creates a new VoxelShape by adding the given axis-aligned bounding box to the given voxel shape.
     *
     * @param x0    the x coordinate of the first point of the bounding box.
     * @param y0    the y coordinate of the first point of the bounding box.
     * @param z0    the z coordinate of the first point of the bounding box.
     * @param x1    the x coordinate of the second point of the bounding box.
     * @param y1    the y coordinate of the second point of the bounding box.
     * @param z1    the z coordinate of the second point of the bounding box.
     * @param shape the VoxelShape to add the bounding box to.
     * @return the new VoxelShape.
     */
    public VoxelShape addBox(double x0, double y0, double z0, double x1, double y1, double z1, VoxelShape shape) {
        return Shapes.or(shape, Shapes.create(x0, y0, z0, x1, y1, z1));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("offset", this.offset)
                .add("rotation", this.rotation.toString())
                .add("scaling", this.scaling)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LegacyTrans3 trans3)) return false;
        return Double.compare(trans3.scaling, this.scaling) == 0 &&
                Objects.equals(this.offset, trans3.offset) &&
                Objects.equals(this.rotation, trans3.rotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.offset, this.rotation, this.scaling);
    }


}
