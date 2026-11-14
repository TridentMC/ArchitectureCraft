package com.tridevmc.architecture.common.shape.orientation;

import com.tridevmc.architecture.common.shape.placement.ShapePlacementLogicWindowCorner;
import com.tridevmc.architecture.common.shape.placement.ShapePlacementLogicWindowFrame;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverPointedWithSpin;
import com.tridevmc.architecture.core.math.IVector3;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShapeOrientationTest {

    @Test
    void cacheRetainsEveryPropertyInEveryArgumentOrder() {
        var facing = ShapeOrientationPropertyFacing.of(Direction.SOUTH);
        var flip = ShapeOrientationPropertyFlip.of(EnumFlip.FLIPPED);
        var turn = ShapeOrientationPropertyTurn.of(EnumTurn.TURNED);
        ShapeOrientationProperty.Value<?>[] values = {facing, flip, turn};
        var expected = ShapeOrientation.forProperties(values);
        for (int first = 0; first < 3; first++) {
            for (int second = 0; second < 3; second++) {
                if (first == second) {
                    continue;
                }
                int third = 3 - first - second;
                var orientation = ShapeOrientation.forProperties(values[first], values[second], values[third]);
                assertSame(expected, orientation);
                assertSame(facing, orientation.getValue(ShapeOrientationPropertyFacing.INSTANCE));
                assertSame(flip, orientation.getValue(ShapeOrientationPropertyFlip.INSTANCE));
                assertSame(turn, orientation.getValue(ShapeOrientationPropertyTurn.INSTANCE));
            }
        }
    }

    @Test
    void orientationOwnsItsPropertiesEvenWhenSharedByTheCache() {
        var facing = ShapeOrientationPropertyFacing.of(Direction.WEST);
        var spin = ShapeOrientationPropertySpin.of(EnumSpin.HALF);
        ShapeOrientationProperty.Value<?>[] input = {facing, spin};
        var direct = new ShapeOrientation(input);
        input[0] = ShapeOrientationPropertyFacing.of(Direction.EAST);
        assertSame(facing, direct.getValue(ShapeOrientationPropertyFacing.INSTANCE));

        var cached = ShapeOrientation.forProperties(facing, spin);
        var exposed = cached.properties();
        exposed[0] = ShapeOrientationPropertyFacing.of(Direction.UP);
        assertSame(cached, ShapeOrientation.forProperties(facing, spin));
        assertSame(facing, cached.getValue(ShapeOrientationPropertyFacing.INSTANCE));
        assertSame(spin, cached.getValue(ShapeOrientationPropertySpin.INSTANCE));
    }

    @Test
    void everyFacingAndSpinIsAUniqueProperRotationAroundTheBlockCenter() {
        var bases = new HashSet<List<Direction>>();
        for (Direction facing : Direction.values()) {
            for (EnumSpin spin : EnumSpin.values()) {
                var orientation = ShapeOrientation.forProperties(
                        ShapeOrientationPropertyFacing.of(facing), ShapeOrientationPropertySpin.of(spin));
                var transform = ShapeTransformationResolverPointedWithSpin.INSTANCE.resolve(orientation);
                var right = transform.transformDirection(Direction.EAST);
                var top = transform.transformDirection(Direction.UP);
                var back = transform.transformDirection(Direction.SOUTH);
                assertSame(facing, transform.transformDirection(Direction.WEST));
                assertEquals(1, determinant(right, top, back));
                assertTrue(bases.add(List.of(right, top, back)));
                var center = transform.transformPosImmutable(IVector3.ofImmutable(0.5, 0.5, 0.5));
                assertEquals(0.5, center.x(), 1.0E-9);
                assertEquals(0.5, center.y(), 1.0E-9);
                assertEquals(0.5, center.z(), 1.0E-9);
                for (int corner = 0; corner < 8; corner++) {
                    var point = transform.transformPosImmutable(IVector3.ofImmutable(
                            corner & 1, (corner >> 1) & 1, (corner >> 2) & 1));
                    assertTrue(point.x() >= -1.0E-9 && point.x() <= 1 + 1.0E-9);
                    assertTrue(point.y() >= -1.0E-9 && point.y() <= 1 + 1.0E-9);
                    assertTrue(point.z() >= -1.0E-9 && point.z() <= 1 + 1.0E-9);
                }
            }
        }
        assertEquals(24, bases.size());
    }

    @Test
    void relativeDirectionsRotateTheWholeBasisWithoutReflection() {
        for (Direction facing : Direction.values()) {
            assertSame(facing, EnumRelativeDirection.FRONT.toAbsoluteDirection(facing));
            assertSame(facing.getOpposite(), EnumRelativeDirection.BACK.toAbsoluteDirection(facing));
            var right = EnumRelativeDirection.RIGHT.toAbsoluteDirection(facing);
            var top = EnumRelativeDirection.TOP.toAbsoluteDirection(facing);
            var back = EnumRelativeDirection.BACK.toAbsoluteDirection(facing);
            assertEquals(1, determinant(right, top, back));
            assertSame(right.getOpposite(), EnumRelativeDirection.LEFT.toAbsoluteDirection(facing));
            assertSame(top.getOpposite(), EnumRelativeDirection.BOTTOM.toAbsoluteDirection(facing));
            if (facing.getAxis().isHorizontal()) {
                assertSame(Direction.UP, top);
            }
        }
    }

    @Test
    void windowOrientationTablesInitializeWithTheirDeclaredProperties() {
        var frame = assertDoesNotThrow(() -> new ShapePlacementLogicWindowFrame<>());
        assertEquals(5, frame.getProperties().size());
        var corner = assertDoesNotThrow(() -> new ShapePlacementLogicWindowCorner<>());
        assertEquals(6, corner.getProperties().size());
        assertEquals(192, ShapePlacementLogicWindowCorner.CACHE.size());
        for (var orientation : ShapePlacementLogicWindowCorner.CACHE) {
            assertEquals(6, orientation.properties().length);
            assertTrue(orientation.getValue(ShapeOrientationPropertyHorizontalFacing.INSTANCE).value().getAxis().isHorizontal());
        }
    }

    private static int determinant(Direction right, Direction top, Direction back) {
        return right.getStepX() * (top.getStepY() * back.getStepZ() - top.getStepZ() * back.getStepY())
                - right.getStepY() * (top.getStepX() * back.getStepZ() - top.getStepZ() * back.getStepX())
                + right.getStepZ() * (top.getStepX() * back.getStepY() - top.getStepY() * back.getStepX());
    }
}
