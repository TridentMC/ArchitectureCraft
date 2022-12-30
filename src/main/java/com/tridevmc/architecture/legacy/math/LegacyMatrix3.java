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
import com.tridevmc.architecture.core.ArchitectureLog;

import java.util.Arrays;

@Deprecated
public class LegacyMatrix3 {

    public static LegacyMatrix3 ident = new LegacyMatrix3();

    public static LegacyMatrix3[] turnRotations = {
            rotY(0), rotY(90), rotY(180), rotY(270)
    };

    public static LegacyMatrix3[] sideRotations = {
            /*0, -Y, DOWN */ ident,
            /*1, +Y, UP   */ rotX(180),
            /*2, -Z, NORTH*/ rotX(90),
            /*3, +Z, SOUTH*/ rotX(-90).mul(rotY(180)),
            /*4, -X, WEST */ rotZ(-90).mul(rotY(90)),
            /*5, +X, EAST */ rotZ(90).mul(rotY(-90))
    };

    public static LegacyMatrix3[][] sideTurnRotations = new LegacyMatrix3[6][4];

    static {
        for (int side = 0; side < 6; side++)
            for (int turn = 0; turn < 4; turn++)
                sideTurnRotations[side][turn] = sideRotations[side].mul(turnRotations[turn]);
    }

    public double[][] m = new double[][]{
            {1, 0, 0},
            {0, 1, 0},
            {0, 0, 1}
    };

    public static LegacyMatrix3 rotX(double deg) {
        return rot(deg, 1, 2);
    }

    public static LegacyMatrix3 rotY(double deg) {
        return rot(deg, 2, 0);
    }

    public static LegacyMatrix3 rotZ(double deg) {
        return rot(deg, 0, 1);
    }

    static LegacyMatrix3 rot(double deg, int i, int j) {
        double a = Math.toRadians(deg);
        double s = Math.sin(a);
        double c = Math.cos(a);
        LegacyMatrix3 r = new LegacyMatrix3();
        r.m[i][i] = c;
        r.m[i][j] = -s;
        r.m[j][i] = s;
        r.m[j][j] = c;
        return r;
    }

    public LegacyMatrix3 mul(LegacyMatrix3 n) {
        LegacyMatrix3 r = new LegacyMatrix3();
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                r.m[i][j] = this.m[i][0] * n.m[0][j] + this.m[i][1] * n.m[1][j] + this.m[i][2] * n.m[2][j];
        return r;
    }

    public LegacyVector3 mul(double x, double y, double z) {
        return new LegacyVector3(
                x * this.m[0][0] + y * this.m[0][1] + z * this.m[0][2],
                x * this.m[1][0] + y * this.m[1][1] + z * this.m[1][2],
                x * this.m[2][0] + y * this.m[2][1] + z * this.m[2][2]
        );
    }

    public LegacyVector3 imul(double x, double y, double z) {
        //  Multiply by inverse, assuming an orthonormal matrix
        return new LegacyVector3(
                x * this.m[0][0] + y * this.m[1][0] + z * this.m[2][0],
                x * this.m[0][1] + y * this.m[1][1] + z * this.m[2][1],
                x * this.m[0][2] + y * this.m[1][2] + z * this.m[2][2]
        );
    }

    public LegacyVector3 mul(LegacyVector3 v) {
        return this.mul(v.x(), v.y(), v.z());
    }

    public LegacyVector3 imul(LegacyVector3 v) {
        return this.imul(v.x(), v.y(), v.z());
    }

    public void dump() {
        for (int i = 0; i < 3; i++)
            ArchitectureLog.info("[%6.3f %6.3f %6.3f]\n", this.m[i][0], this.m[i][1], this.m[i][2]);
    }

    public boolean isIdent() {
        return Arrays.equals(this.m[0], LegacyMatrix3.ident.m[0]) &&
                Arrays.equals(this.m[1], LegacyMatrix3.ident.m[1]) &&
                Arrays.equals(this.m[2], LegacyMatrix3.ident.m[2]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LegacyMatrix3 matrix3)) return false;
        return Arrays.equals(this.m[0], matrix3.m[0]) &&
                Arrays.equals(this.m[1], matrix3.m[1]) &&
                Arrays.equals(this.m[2], matrix3.m[2]);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(Arrays.stream(this.m).flatMapToDouble(Arrays::stream).toArray());
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("m", this.m)
                .toString();
    }
}
