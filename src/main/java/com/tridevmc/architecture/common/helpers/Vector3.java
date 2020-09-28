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

package com.tridevmc.architecture.common.helpers;

import com.google.common.base.MoreObjects;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3i;

import static java.lang.Math.abs;

public class Vector3 {

    public static Vector3 zero = new Vector3(0, 0, 0);
    public static Vector3 blockCenter = new Vector3(0.5, 0.5, 0.5);

    public static Vector3 unitX = new Vector3(1, 0, 0);
    public static Vector3 unitY = new Vector3(0, 1, 0);
    public static Vector3 unitZ = new Vector3(0, 0, 1);

    public static Vector3 unitNX = new Vector3(-1, 0, 0);
    public static Vector3 unitNY = new Vector3(0, -1, 0);
    public static Vector3 unitNZ = new Vector3(0, 0, -1);

    public static Vector3 unitPYNZ = new Vector3(0, 0.707, -0.707);
    public static Vector3 unitPXPY = new Vector3(0.707, 0.707, 0);
    public static Vector3 unitPYPZ = new Vector3(0, 0.707, 0.707);
    public static Vector3 unitNXPY = new Vector3(-0.707, 0.707, 0);
    public static Vector3[][] faceBases = {
            {unitX, unitZ}, // DOWN
            {unitX, unitNZ}, // UP
            {unitNX, unitY}, // NORTH
            {unitX, unitY}, // SOUTH
            {unitZ, unitY}, // WEST
            {unitNZ, unitY}, // EAST
    };
    public static Vector3i[] directionVec = {
            new Vector3i(0, -1, 0),
            new Vector3i(0, 1, 0),
            new Vector3i(0, 0, -1),
            new Vector3i(0, 0, 1),
            new Vector3i(-1, 0, 0),
            new Vector3i(1, 0, 0)
    };
    public double x, y, z;

    public Vector3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3(Vector3d v) {
        this(v.x, v.y, v.z);
    }

    public Vector3(Vector3i v) {
        this(v.getX(), v.getY(), v.getZ());
    }

    public Vector3(Direction f) {
        this(getDirectionVec(f));
    }

    public Vector3(Vector3 v) {
        this(v.x, v.y, v.z);
    }

    public static Vector3 blockCenter(double x, double y, double z) {
        return blockCenter.add(x, y, z);
    }

    public static Vector3 blockCenter(BlockPos pos) {
        return blockCenter.add(pos);
    }

    public static Vector3 sub(double[] u, double[] v) {
        return new Vector3(u[0] - v[0], u[1] - v[1], u[2] - v[2]);
    }

    public static Vector3 unit(Vector3 v) {
        return v.mul(1 / v.length());
    }

    public static Vector3 average(Vector3... va) {
        double x = 0, y = 0, z = 0;
        for (Vector3 v : va) {
            x += v.x;
            y += v.y;
            z += v.z;
        }
        int n = va.length;
        return new Vector3(x / n, y / n, z / n);
    }

    public static Vector3 average(double[]... va) {
        double x = 0, y = 0, z = 0;
        for (double[] v : va) {
            x += v[0];
            y += v[1];
            z += v[2];
        }
        int n = va.length;
        return new Vector3(x / n, y / n, z / n);
    }

    public static Direction facing(double dx, double dy, double dz) {
        double ax = abs(dx), ay = abs(dy), az = abs(dz);
        if (ay >= ax && ay >= az)
            return dy < 0 ? Direction.DOWN : Direction.UP;
        else if (ax >= az)
            return dx < 0 ? Direction.WEST : Direction.EAST;
        else
            return dz < 0 ? Direction.NORTH : Direction.SOUTH;
    }

    public static Vector3[] faceBasis(Direction f) {
        return faceBases[f.ordinal()];
    }

    public static Vector3i getDirectionVec(Direction f) {
        return directionVec[f.ordinal()];
    }

    public Vector3i toVec3i() {
        return new Vector3i(this.x, this.y, this.z);
    }

    public Vector3 add(double x, double y, double z) {
        return new Vector3(this.x + x, this.y + y, this.z + z);
    }

    public Vector3 add(Vector3 v) {
        return this.add(v.x, v.y, v.z);
    }

    public Vector3 add(BlockPos pos) {
        return this.add(pos.getX(), pos.getY(), pos.getZ());
    }

    public Vector3 sub(double x, double y, double z) {
        return new Vector3(this.x - x, this.y - y, this.z - z);
    }

    public Vector3 sub(Vector3 v) {
        return this.sub(v.x, v.y, v.z);
    }

    public Vector3 mul(double c) {
        return new Vector3(c * this.x, c * this.y, c * this.z);
    }

    public double dot(Vector3 v) {
        return this.dot(v.x, v.y, v.z);
    }

    public double dot(double[] v) {
        return this.dot(v[0], v[1], v[2]);
    }

    public double dot(Direction f) {
        Vector3i v = getDirectionVec(f);
        return this.dot(v.getX(), v.getY(), v.getZ());
    }

    public double dot(double vx, double vy, double vz) {
        return this.x * vx + this.y * vy + this.z * vz;
    }

    public Vector3 cross(Vector3 v) {
        return new Vector3(
                this.y * v.z - this.z * v.y,
                this.z * v.x - this.x * v.z,
                this.x * v.y - this.y * v.x);
    }

    public Vector3 min(Vector3 v) {
        return new Vector3(Math.min(this.x, v.x), Math.min(this.y, v.y), Math.min(this.z, v.z));
    }

    public Vector3 max(Vector3 v) {
        return new Vector3(Math.max(this.x, v.x), Math.max(this.y, v.y), Math.max(this.z, v.z));
    }

    public double length() {
        return Math.sqrt(this.x * this.x + this.y * this.y + this.z * this.z);
    }

    public double distance(Vector3 v) {
        double dx = this.x - v.x, dy = this.y - v.y, dz = this.z - v.z;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public int floorX() {
        return (int) Math.floor(this.x);
    }

    public int floorY() {
        return (int) Math.floor(this.y);
    }

    public int floorZ() {
        return (int) Math.floor(this.z);
    }

    // Normals at 45 degrees are biased towards UP or DOWN.
    // In 1.8 this is important for item lighting in inventory to work well.

    public int roundX() {
        return (int) Math.round(this.x);
    }

    public int roundY() {
        return (int) Math.round(this.y);
    }

    public int roundZ() {
        return (int) Math.round(this.z);
    }

    /**
     * Calculates the slope of x in terms of y.
     */
    public double getXYSlope(Vector3 other) {
        return (this.y - other.y) / (this.x - other.x);
    }

    public double getXZSlope(Vector3 other) {
        return (this.z - other.z) / (this.x - other.x);
    }

    /**
     * Calculates the slope of z in terms of y.
     */
    public double getZYSlope(Vector3 other) {
        return (this.y - other.y) / (this.z - other.z);
    }

    public double getZXSlope(Vector3 other) {
        return (this.x - other.x) / (this.z - other.z);
    }

    /**
     * Calculates the slope of y in terms of x.
     */
    public double getYZSlope(Vector3 other) {
        return (this.x - other.x) / (this.y - other.y);
    }

    public double getYXSlope(Vector3 other) {
        return (this.z - other.z) / (this.y - other.y);
    }

    // Workaround for EnumFacing.getDirectionVec being client-side only

    public Direction facing() {
        return facing(this.x, this.y, this.z);
    }

    public BlockPos blockPos() {
        return new BlockPos(this.floorX(), this.floorY(), this.floorZ());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("x", this.x)
                .add("y", this.y)
                .add("z", this.z)
                .toString();
    }

    public double[] toArray() {
        return new double[]{this.x, this.y, this.z};
    }
}
