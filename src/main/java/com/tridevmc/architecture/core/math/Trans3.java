package com.tridevmc.architecture.core.math;

record Trans3(IMatrix4Immutable matrix) implements ITrans3Immutable {
    record Mutable(IMatrix4Mutable matrix) implements ITrans3Mutable {

    }
}
