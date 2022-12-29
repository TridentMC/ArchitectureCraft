package com.tridevmc.architecture.core.math;

/**
 * An immutable transform matrix, provides methods for transforming vectors, normals, and UVs.
 * <p>
 * As this is an immutable class, you'll need to use the {@link Builder} to create a new instance
 * and the {@link Transform#mutate()} method to retrieve a builder you can use to modify the transform.
 *
 * @param matrix The matrix that makes up this transform.
 */
public record Transform(IMatrix4 matrix) {

    public static final Transform IDENTITY = new Transform(IMatrix4.IDENTITY);

    public static final Transform BLOCK_CENTER = new Transform(IVector3.BLOCK_CENTER, IVector3.ZERO, IVector3.ONE);

    public static final Transform[][] SIDE_TURN_ROTATIONS = new Transform[6][4];

    static {
        for (var i = 0; i < 6; i++) {
            for (var j = 0; j < 4; j++) {
                SIDE_TURN_ROTATIONS[i][j] = new Transform(IMatrix4.getSideTurnRotation(i, j));
            }
        }
    }

    public Transform(IMatrix4 matrix) {
        this.matrix = matrix.asImmutable();
    }

    public Transform() {
        this(Matrix4.IDENTITY);
    }

    public Transform(IVector3 pos, IVector3 rot, IVector3 scale) {
        this(IVector3.ZERO, pos, rot, scale);
    }

    public Transform(IVector3 origin, IVector3 pos, IVector3 rot, IVector3 scale) {
        this(
                IMatrix4Mutable.ofIdentity()
                        .translate(pos)
                        .rotateXYZ(rot, origin)
                        .scale(scale, origin)
        );
    }

    public Transform(IVector3 pos, IVector3 rot) {
        this(pos, rot, IVector3.ONE);
    }

    public Transform(IVector3 pos) {
        this(pos, IVector3.ZERO, IVector3.ONE);
    }

    public Transform(IVector3 pos, IVector3 rot, double scale) {
        this(pos, rot, IVector3.ofImmutable(scale, scale, scale));
    }

    public Transform(IVector3 pos, double scale) {
        this(pos, IVector3.ZERO, IVector3.ofImmutable(scale, scale, scale));
    }

    public Transform(double scale) {
        this(IVector3.ZERO, IVector3.ZERO, IVector3.ofImmutable(scale, scale, scale));
    }

    public IVector3 transformPosition(IVector3 vec) {
        // The matrix itself doesn't have methods for transforming positions it's just for storing data.
        // So we need to transform it here ourselves.
        return IVector3.ofImmutable(
                this.matrix.m00() * vec.x() + this.matrix.m01() * vec.y() + this.matrix.m02() * vec.z() + this.matrix.m03(),
                this.matrix.m10() * vec.x() + this.matrix.m11() * vec.y() + this.matrix.m12() * vec.z() + this.matrix.m13(),
                this.matrix.m20() * vec.x() + this.matrix.m21() * vec.y() + this.matrix.m22() * vec.z() + this.matrix.m23()
        );
    }

    public IVector3 transformNormal(IVector3 vec) {
        // The matrix itself doesn't have methods for transforming normals it's just for storing data.
        // So we need to transform it here ourselves.
        return IVector3.ofImmutable(
                this.matrix.m00() * vec.x() + this.matrix.m01() * vec.y() + this.matrix.m02() * vec.z(),
                this.matrix.m10() * vec.x() + this.matrix.m11() * vec.y() + this.matrix.m12() * vec.z(),
                this.matrix.m20() * vec.x() + this.matrix.m21() * vec.y() + this.matrix.m22() * vec.z()
        );
    }

    public Vector2 transformUV(Vector2 vec) {
        // The matrix itself doesn't have methods for transforming UVs it's just for storing data.
        // So we need to transform it here ourselves.
        return new Vector2(
                this.matrix.m00() * vec.x() + this.matrix.m01() * vec.y() + this.matrix.m03(),
                this.matrix.m10() * vec.x() + this.matrix.m11() * vec.y() + this.matrix.m13()
        );
    }


}
