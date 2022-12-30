package com.tridevmc.architecture.legacy.math;

@Deprecated
public record LegacyVector3i(int x, int y, int z) {

    public LegacyVector3i add(LegacyVector3i other) {
        return new LegacyVector3i(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public LegacyVector3i sub(LegacyVector3i other) {
        return new LegacyVector3i(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public LegacyVector3i cross(LegacyVector3i other) {
        return new LegacyVector3i(this.x * other.x, this.y * other.y, this.z * other.z);
    }

    public LegacyVector3i mul(int scalar) {
        return new LegacyVector3i(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public LegacyVector3i div(int scalar) {
        return new LegacyVector3i(this.x / scalar, this.y / scalar, this.z / scalar);
    }

    public LegacyVector3i negate() {
        return new LegacyVector3i(-this.x, -this.y, -this.z);
    }

    public LegacyVector3i abs() {
        return new LegacyVector3i(Math.abs(this.x), Math.abs(this.y), Math.abs(this.z));
    }

    public LegacyVector3 toVector3() {
        return new LegacyVector3(this.x, this.y, this.z);
    }

}
