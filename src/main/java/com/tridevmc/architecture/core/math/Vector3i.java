package com.tridevmc.architecture.core.math;

public record Vector3i(int x, int y, int z) {

    public Vector3i add(Vector3i other) {
        return new Vector3i(this.x + other.x, this.y + other.y, this.z + other.z);
    }

    public Vector3i sub(Vector3i other) {
        return new Vector3i(this.x - other.x, this.y - other.y, this.z - other.z);
    }

    public Vector3i cross(Vector3i other) {
        return new Vector3i(this.x * other.x, this.y * other.y, this.z * other.z);
    }

    public Vector3i mul(int scalar) {
        return new Vector3i(this.x * scalar, this.y * scalar, this.z * scalar);
    }

    public Vector3i div(int scalar) {
        return new Vector3i(this.x / scalar, this.y / scalar, this.z / scalar);
    }

    public Vector3i negate() {
        return new Vector3i(-this.x, -this.y, -this.z);
    }

    public Vector3i abs() {
        return new Vector3i(Math.abs(this.x), Math.abs(this.y), Math.abs(this.z));
    }

    public LegacyVector3 toVector3() {
        return new LegacyVector3(this.x, this.y, this.z);
    }

}
