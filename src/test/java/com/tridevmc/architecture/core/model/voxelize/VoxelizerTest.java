package com.tridevmc.architecture.core.model.voxelize;

import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.ShapeMeshes;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFacing;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverCladding;
import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.physics.AABB;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class VoxelizerTest {
    @Test
    void fractionalSheetCollisionMatchesEveryAttachmentAndNegativeCoordinates() {
        var mesh = ShapeMeshes.getMesh(EnumShape.CLADDING_SHEET);
        var voxels = new Voxelizer(mesh, 16).voxelizeNow();
        assertFalse(voxels.isEmpty());
        assertBounds(mesh.getBounds(), enclosing(voxels));
        for (var direction : Direction.values()) {
            var transform = ShapeTransformationResolverCladding.INSTANCE.resolve(ShapeOrientation.forProperties(
                    ShapeOrientationPropertyFacing.of(direction)));
            assertBounds(transform.transformAABB(mesh.getBounds()),
                    enclosing(voxels.stream().map(transform::transformAABB).toList()));
        }
        var translated = mesh.transform(ITrans3.ofTranslationImmutable(-2, -3, -4));
        assertBounds(translated.getBounds(), enclosing(new Voxelizer(translated, 16).voxelizeNow()));
    }

    private static AABB enclosing(List<AABB> boxes) {
        return new AABB(boxes.stream().mapToDouble(AABB::minX).min().orElseThrow(),
                boxes.stream().mapToDouble(AABB::minY).min().orElseThrow(),
                boxes.stream().mapToDouble(AABB::minZ).min().orElseThrow(),
                boxes.stream().mapToDouble(AABB::maxX).max().orElseThrow(),
                boxes.stream().mapToDouble(AABB::maxY).max().orElseThrow(),
                boxes.stream().mapToDouble(AABB::maxZ).max().orElseThrow());
    }

    private static void assertBounds(AABB expected, AABB actual) {
        assertEquals(expected.minX(), actual.minX(), 1e-9);
        assertEquals(expected.minY(), actual.minY(), 1e-9);
        assertEquals(expected.minZ(), actual.minZ(), 1e-9);
        assertEquals(expected.maxX(), actual.maxX(), 1e-9);
        assertEquals(expected.maxY(), actual.maxY(), 1e-9);
        assertEquals(expected.maxZ(), actual.maxZ(), 1e-9);
    }
}
