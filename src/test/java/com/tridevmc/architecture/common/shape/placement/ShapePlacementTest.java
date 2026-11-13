package com.tridevmc.architecture.common.shape.placement;

import com.tridevmc.architecture.common.block.BlockShape;
import com.tridevmc.architecture.common.block.state.BlockStateArchitecture;
import com.tridevmc.architecture.common.block.state.BlockStateShape;
import com.tridevmc.architecture.common.shape.EnumShape;
import com.tridevmc.architecture.common.shape.ShapeConnections;
import com.tridevmc.architecture.common.shape.ShapeMeshes;
import com.tridevmc.architecture.common.shape.orientation.EnumConnectionState;
import com.tridevmc.architecture.common.shape.orientation.EnumPlacementOffset;
import com.tridevmc.architecture.common.shape.orientation.EnumSpin;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientation;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyAxis;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyFacing;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyHorizontalFacing;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyOffset;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertyRelativeConnection;
import com.tridevmc.architecture.common.shape.orientation.ShapeOrientationPropertySpin;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverBanister;
import com.tridevmc.architecture.common.shape.transformation.ShapeTransformationResolverCladding;
import com.tridevmc.architecture.core.math.IVector3;
import com.tridevmc.architecture.core.physics.AABB;
import com.tridevmc.architecture.core.physics.Ray;
import java.util.Comparator;
import com.tridevmc.architecture.testsupport.MinecraftTestBootstrap;
import com.tridevmc.architecture.testsupport.ShapeScreenshot;

import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StairsShape;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MinecraftTestBootstrap.class)
class ShapePlacementTest {

    @Test
    void everyShapeHasUsableGeometryAndPlacementContracts() throws IOException {
        for (var shape : EnumShape.values()) {
            assertNotNull(shape.getPlacementLogic(), shape.getName());
            assertNotNull(shape.getTransformationResolver(), shape.getName());
            var mesh = ShapeMeshes.getMesh(shape);
            assertFalse(mesh.getFaces().isEmpty(), shape.getName());
            mesh.getFaceStream().forEach(face -> {
                assertTrue(Double.isFinite(face.getNormal().lengthSquared()), shape.getName());
                assertEquals(1, face.getNormal().lengthSquared(), 1e-6, () -> shape.getName()
                        + " mesh=" + mesh.getName() + " normal=" + face.getNormal()
                        + " polygonNormals=" + face.getPolygons().stream().map(polygon -> polygon.getNormal().toString()).toList()
                        + " importer=" + com.tridevmc.architecture.core.model.objson.OBJSON.class.getProtectionDomain().getCodeSource().getLocation());
            });
            mesh.getFaceStream().flatMap(face -> face.getPolygonStream()).forEach(polygon -> {
                assertTrue(Double.isFinite(polygon.getNormal().lengthSquared()), shape.getName());
                assertTrue(polygon.getNormal().lengthSquared() > 0.99, shape.getName());
            });
        }
        var lazyFrame = new LazyFrame();
        lazyFrame.getStateDefinition().getPossibleStates().forEach(BlockState::initCache);
        ShapeScreenshot.write(Path.of("build/reports/shapes/orientations.png"));
    }

    @Test
    void placementAndHammerReachEveryRigidOrientation() {
        var reached = new HashSet<ShapeOrientation>();
        var block = new BlockShape<>(EnumShape.STAIRS);
        var level = mock(Level.class);
        var player = mock(Player.class);
        for (float pitch : new float[]{-90, 0, 90}) {
            when(player.getViewXRot(1.0F)).thenReturn(pitch);
            for (var horizontal : Direction.Plane.HORIZONTAL) {
                when(player.getDirection()).thenReturn(horizontal);
                when(player.getViewYRot(1.0F)).thenReturn(horizontal.get2DDataValue() * 90F);
                for (var hit : Direction.values()) {
                    for (int region = 0; region < 5; region++) {
                        var x = 0.5 + hit.getStepX() * 0.5;
                        var y = 0.5 + hit.getStepY() * 0.5;
                        var z = 0.5 + hit.getStepZ() * 0.5;
                        var offset = region == 1 || region == 3 ? 0.35 : -0.35;
                        if (region != 0) {
                            if (hit.getAxis() == Direction.Axis.X) {
                                if (region < 3) y += offset; else z += offset;
                            } else if (hit.getAxis() == Direction.Axis.Y) {
                                if (region < 3) x += offset; else z += offset;
                            } else {
                                if (region < 3) x += offset; else y += offset;
                            }
                        }
                        var result = new BlockHitResult(new Vec3(x, y, z), hit, BlockPos.ZERO, false);
                        for (var crouching : new boolean[]{false, true}) {
                            when(player.isCrouching()).thenReturn(crouching);
                            reached.add(ShapePlacementLogicStairLike.INSTANCE.getShapeOrientationForPlacement(
                                    block, level, BlockPos.ZERO, player, result));
                        }
                    }
                }
            }
        }
        assertEquals(24, reached.size());
        for (var shape : EnumShape.values()) {
            var shapeBlock = new BlockShape<>(shape);
            var state = shapeBlock.defaultBlockState();
            var start = state;
            var visited = new HashSet<BlockState>();
            for (int i = 0; i < orientationCount(shape); i++) {
                assertTrue(visited.add(state), shape.getName());
                var transform = shapeBlock.getTransformForState((BlockStateShape) state);
                var matrix = transform.matrix();
                var determinant = matrix.m00() * (matrix.m11() * matrix.m22() - matrix.m12() * matrix.m21())
                        - matrix.m01() * (matrix.m10() * matrix.m22() - matrix.m12() * matrix.m20())
                        + matrix.m02() * (matrix.m10() * matrix.m21() - matrix.m11() * matrix.m20());
                assertEquals(1, determinant, 1e-9, shape.getName());
                var center = transform.transformPosImmutable(IVector3.ofImmutable(0.5, 0.5, 0.5));
                assertTrue(Double.isFinite(center.x()) && Double.isFinite(center.y()) && Double.isFinite(center.z()), shape.getName());
                var next = shapeBlock.rotatePlacement(state, false);
                assertEquals(state, shapeBlock.rotatePlacement(next, true), shape.getName());
                state = next;
            }
            assertEquals(start, state, shape.getName());
            if (shape == EnumShape.STAIRS) {
                assertEquals(reached, visited.stream().map(pose -> ((BlockStateShape) pose).getOrientation()).collect(Collectors.toSet()));
            }
        }
    }

    private static int orientationCount(EnumShape shape) {
        return switch (shape) {
            case SPHERE_FULL -> 1;
            case PILLAR, POST, POLE, CYLINDER, WINDOW_FRAME, WINDOW_SMART -> 3;
            case SLAB, CLADDING_SHEET, PILLAR_BASE, DORIC_CAPITAL, CORINTHIAN_CAPITAL,
                    WINDOW_MULLION, WINDOW_MULLION_SMART -> 6;
            case SPHERE_HALF, ARCH_D1, WINDOW_CORNER -> 12;
            case BANISTER_PLAIN_BOTTOM, BANISTER_PLAIN, BANISTER_PLAIN_TOP, BANISTER_PLAIN_END,
                    BANISTER_FANCY_BOTTOM, BANISTER_FANCY, BANISTER_FANCY_TOP, BANISTER_FANCY_END,
                    BALUSTRADE_PLAIN_END -> 72;
            default -> 24;
        };
    }

    @Test
    void windowPartsFollowNeighborsAndDiagonalGapsInEachPlane() {
        var block = new BlockShape<>(EnumShape.WINDOW_FRAME);
        var origin = new BlockPos(-3, 4, -5);
        var blocks = new HashMap<BlockPos, BlockState>();
        var level = level(blocks);
        for (var axis : Direction.Axis.values()) {
            blocks.clear();
            var state = block.defaultBlockState().setValue(ShapeOrientationPropertyAxis.INSTANCE, axis);
            var directions = ShapeConnections.windowDirections(state);
            blocks.put(origin, state);
            blocks.put(origin.relative(directions[1]), state);
            blocks.put(origin.relative(directions[2]), state);
            var lShape = ShapeConnections.update(state, level, origin);
            assertEquals(EnumConnectionState.CONNECTED, lShape.getValue(ShapeOrientationPropertyRelativeConnection.RIGHT));
            assertEquals(EnumConnectionState.DISCONNECTED, lShape.getValue(ShapeConnectionProperties.UP_RIGHT));
            var lMesh = block.getMeshForState((BlockStateShape) lShape);
            assertFalse(lMesh.getParts().containsKey("edge.east"));
            assertTrue(lMesh.getParts().containsKey("corner.inner.up_east"));
            blocks.put(origin.relative(directions[1]).relative(directions[2]), state);
            var square = ShapeConnections.update(lShape, level, origin);
            assertEquals(EnumConnectionState.CONNECTED, square.getValue(ShapeConnectionProperties.UP_RIGHT));
            assertFalse(block.getMeshForState((BlockStateShape) square).getParts().containsKey("corner.inner.up_east"));
            blocks.clear();
            var detached = ShapeConnections.update(square, level, origin);
            assertEquals(EnumConnectionState.DISCONNECTED, detached.getValue(ShapeOrientationPropertyRelativeConnection.RIGHT));
            assertTrue(block.getMeshForState((BlockStateShape) detached).getParts().containsKey("edge.east"));
        }
        var corner = new BlockShape<>(EnumShape.WINDOW_CORNER).defaultBlockState()
                .setValue(ShapeOrientationPropertyAxis.INSTANCE, Direction.Axis.Y)
                .setValue(ShapeOrientationPropertyHorizontalFacing.INSTANCE, Direction.NORTH);
        var cornerBlock = (BlockShape<?>) corner.getBlock();
        var startCorner = corner;
        var cornerBases = new HashSet<List<Direction>>();
        for (int i = 0; i < 12; i++) {
            var transform = cornerBlock.getTransformForState((BlockStateShape) corner);
            assertTrue(cornerBases.add(List.of(transform.transformDirection(Direction.EAST),
                    transform.transformDirection(Direction.UP), transform.transformDirection(Direction.SOUTH))));
            assertTrue(ShapeConnections.windowsConnect(corner, corner, ShapeConnections.direction(corner, Direction.UP)));
            var opposite = corner.setValue(ShapeOrientationPropertyHorizontalFacing.INSTANCE,
                    corner.getValue(ShapeOrientationPropertyHorizontalFacing.INSTANCE).getOpposite());
            assertFalse(ShapeConnections.windowsConnect(corner, opposite, ShapeConnections.direction(corner, Direction.UP)));
            blocks.clear();
            var isolated = ShapeConnections.update(corner, level, origin);
            for (var joinedSide : new Direction[]{Direction.DOWN, Direction.UP}) {
                var pointY = joinedSide == Direction.DOWN ? 1 / 16D : 15 / 16D;
                assertTrue(hasWindowRail(cornerBlock, isolated, 0.5, pointY, 0.75, Direction.EAST));
                assertTrue(hasWindowRail(cornerBlock, isolated, 0.25, pointY, 0.5, Direction.SOUTH));
                blocks.put(origin.relative(ShapeConnections.direction(corner, joinedSide)), corner);
                var joined = ShapeConnections.update(corner, level, origin);
                assertFalse(hasWindowRail(cornerBlock, joined, 0.5, pointY, 0.75, Direction.EAST));
                assertFalse(hasWindowRail(cornerBlock, joined, 0.25, pointY, 0.5, Direction.SOUTH));
                blocks.clear();
            }
            var planeFrame = block.defaultBlockState().setValue(ShapeOrientationPropertyAxis.INSTANCE,
                    ShapeConnections.direction(corner, Direction.EAST).getAxis());
            var vertical = ShapeConnections.direction(corner, Direction.UP);
            var horizontal = ShapeConnections.direction(corner, Direction.SOUTH);
            blocks.clear();
            blocks.put(origin.relative(vertical), corner);
            blocks.put(origin.relative(horizontal), planeFrame);
            blocks.put(origin.relative(vertical).relative(horizontal), planeFrame);
            var filledCorner = ShapeConnections.update(corner, level, origin);
            assertEquals(EnumConnectionState.CONNECTED, filledCorner.getValue(ShapeConnectionProperties.UP_RIGHT));
            blocks.remove(origin.relative(vertical).relative(horizontal));
            assertEquals(EnumConnectionState.DISCONNECTED,
                    ShapeConnections.update(filledCorner, level, origin).getValue(ShapeConnectionProperties.UP_RIGHT));
            var next = cornerBlock.rotatePlacement(corner, false);
            assertEquals(corner, cornerBlock.rotatePlacement(next, true));
            corner = next;
        }
        assertEquals(startCorner, corner);
        corner = startCorner;
        var matchingFrame = block.defaultBlockState().setValue(ShapeOrientationPropertyAxis.INSTANCE, Direction.Axis.X);
        assertTrue(ShapeConnections.windowsConnect(corner, matchingFrame, Direction.SOUTH));
        assertFalse(ShapeConnections.windowsConnect(corner,
                block.defaultBlockState().setValue(ShapeOrientationPropertyAxis.INSTANCE, Direction.Axis.Z), Direction.SOUTH));
    }

    @Test
    void smartShapesAndAttachmentOffsetsRespectTheirLocalAxes() {
        var block = new BlockShape<>(EnumShape.STAIRS_SMART);
        var state = block.defaultBlockState().setValue(ShapeOrientationPropertyFacing.INSTANCE, Direction.WEST)
                .setValue(ShapeOrientationPropertySpin.INSTANCE, EnumSpin.NONE);
        var neighbor = state.setValue(ShapeOrientationPropertyFacing.INSTANCE, Direction.NORTH);
        var blocks = new HashMap<BlockPos, BlockState>();
        var level = level(blocks);
        blocks.put(BlockPos.ZERO.east(), neighbor);
        assertEquals(StairsShape.OUTER_RIGHT, ShapeConnections.update(state, level, BlockPos.ZERO).getValue(ShapeConnectionProperties.STAIRS));
        blocks.clear();
        blocks.put(BlockPos.ZERO.west(), neighbor);
        assertEquals(StairsShape.INNER_RIGHT, ShapeConnections.update(state, level, BlockPos.ZERO).getValue(ShapeConnectionProperties.STAIRS));
        blocks.clear();
        assertEquals(StairsShape.STRAIGHT, ShapeConnections.update(state, level, BlockPos.ZERO).getValue(ShapeConnectionProperties.STAIRS));
        var roof = new BlockShape<>(EnumShape.ROOF_SMART_RIDGE);
        var ridge = new BlockShape<>(EnumShape.ROOF_RIDGE);
        var valley = new BlockShape<>(EnumShape.ROOF_VALLEY);
        var tile = new BlockShape<>(EnumShape.ROOF_TILE);
        for (var facing : Direction.values()) {
            for (var spin : EnumSpin.values()) {
                var roofState = roof.defaultBlockState().setValue(ShapeOrientationPropertyFacing.INSTANCE, facing)
                        .setValue(ShapeOrientationPropertySpin.INSTANCE, spin);
                var ridgeState = ridge.defaultBlockState().setValue(ShapeOrientationPropertyFacing.INSTANCE, facing)
                        .setValue(ShapeOrientationPropertySpin.INSTANCE, spin);
                var up = ShapeConnections.direction(roofState, Direction.UP);
                var side = ShapeConnections.direction(roofState, Direction.WEST);
                blocks.clear();
                blocks.put(BlockPos.ZERO.relative(side), ridgeState);
                var connected = ShapeConnections.update(roofState, level, BlockPos.ZERO);
                assertEquals(EnumConnectionState.CONNECTED, connected.getValue(ShapeOrientationPropertyRelativeConnection.LEFT));
                assertEquals(edgeProfile(roof, connected, BlockPos.ZERO, side, up, BlockPos.ZERO),
                        edgeProfile(ridge, ridgeState, BlockPos.ZERO.relative(side), side, up, BlockPos.ZERO));
                assertFalse(ShapeConnections.roofPortsMatch(roofState,
                        valley.defaultBlockState().setValue(ShapeOrientationPropertyFacing.INSTANCE, facing)
                                .setValue(ShapeOrientationPropertySpin.INSTANCE, spin), side));
                assertFalse(ShapeConnections.roofPortsMatch(roofState,
                        tile.defaultBlockState().setValue(ShapeOrientationPropertyFacing.INSTANCE, facing)
                                .setValue(ShapeOrientationPropertySpin.INSTANCE, spin), side));
                assertFalse(ShapeConnections.roofPortsMatch(roofState, ridgeState,
                        ShapeConnections.direction(roofState, Direction.NORTH)));
            }
        }
        for (var direction : Direction.values()) {
            var cladding = ShapeTransformationResolverCladding.INSTANCE.resolve(ShapeOrientation.forProperties(
                    ShapeOrientationPropertyFacing.of(direction))).transformPosImmutable(IVector3.ofImmutable(0.5, 0.5, 0.5));
            assertEquals(0.5 + direction.getStepX() * 15 / 32D, cladding.x(), 1e-9);
            assertEquals(0.5 + direction.getStepY() * 15 / 32D, cladding.y(), 1e-9);
            assertEquals(0.5 + direction.getStepZ() * 15 / 32D, cladding.z(), 1e-9);
            for (var spin : EnumSpin.values()) {
                var front = ShapeTransformationResolverBanister.INSTANCE.resolve(ShapeOrientation.forProperties(
                        ShapeOrientationPropertyFacing.of(direction), ShapeOrientationPropertySpin.of(spin),
                        ShapeOrientationPropertyOffset.of(EnumPlacementOffset.FRONT))).transformPosImmutable(IVector3.ofImmutable(0.5, 0.5, 0.5));
                var back = ShapeTransformationResolverBanister.INSTANCE.resolve(ShapeOrientation.forProperties(
                        ShapeOrientationPropertyFacing.of(direction), ShapeOrientationPropertySpin.of(spin),
                        ShapeOrientationPropertyOffset.of(EnumPlacementOffset.BACK))).transformPosImmutable(IVector3.ofImmutable(0.5, 0.5, 0.5));
                assertEquals(0.75, back.sub(front).length(), 1e-9);
            }
        }
    }

    private static boolean hasWindowRail(BlockShape<?> block, BlockState state,
            double x, double y, double z, Direction outward) {
        var transform = block.getTransformForState((BlockStateShape) state);
        var ray = new Ray(transform.transformPosImmutable(IVector3.ofImmutable(x, y, z)),
                transform.transformNormalImmutable(IVector3.ofImmutable(outward.getStepX(), outward.getStepY(), outward.getStepZ())));
        var mesh = block.getMeshForState((BlockStateShape) state).transform(transform, false);
        return mesh.getFaceStream().flatMap(face -> face.getPolygonStream())
                .filter(polygon -> polygon.getPolygonData().textureIndex() == 0)
                .map(ray::intersect).filter(Ray.Hit::isValidHit)
                .filter(hit -> hit.distanceTo(ray.origin()) > 1e-8)
                .min(Comparator.comparingDouble(hit -> hit.distanceTo(ray.origin())))
                .map(hit -> hit.poly().getNormal().dot(ray.direction()) > 1e-8).orElse(false);
    }

    private static List<Long> edgeProfile(BlockShape<?> block, BlockState state, BlockPos origin,
            Direction side, Direction up, BlockPos reference) {
        var tangent = Arrays.stream(Direction.values())
                .filter(d -> d.getAxis() != side.getAxis() && d.getAxis() != up.getAxis()).findFirst().orElseThrow();
        var heights = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        var transform = block.getTransformForState((BlockStateShape) state);
        block.getMeshForState((BlockStateShape) state).getFaceStream().flatMap(face -> face.getPolygonStream())
                .flatMap(polygon -> polygon.getVertexStream()).forEach(vertex -> {
                    var point = transform.transformPosImmutable(vertex.getPos()).asMutable()
                            .add(origin.getX(), origin.getY(), origin.getZ());
                    var local = point.asMutable().sub(reference.getX() + 0.5, reference.getY() + 0.5, reference.getZ() + 0.5);
                    var edgeDistance = local.x() * side.getStepX() + local.y() * side.getStepY() + local.z() * side.getStepZ();
                    if (Math.abs(edgeDistance - 0.5) < 1e-9) {
                        var across = local.x() * tangent.getStepX() + local.y() * tangent.getStepY() + local.z() * tangent.getStepZ();
                        var index = (int) Math.round((across + 0.5) * 2);
                        if (index >= 0 && index < heights.length) {
                            var height = point.x() * up.getStepX() + point.y() * up.getStepY() + point.z() * up.getStepZ();
                            heights[index] = Math.max(heights[index], height);
                        }
                    }
                });
        assertTrue(Arrays.stream(heights).allMatch(Double::isFinite));
        return Arrays.stream(heights).mapToObj(height -> Math.round(height * 1000000)).toList();
    }

    private static final class LazyFrame extends BlockShape<LazyFrame> {
        private LazyFrame() {
            super(EnumShape.WINDOW_FRAME);
        }

        @Override
        public CompletableFuture<ImmutableList<AABB>> getBoxesForState(BlockStateArchitecture state) {
            throw new AssertionError("State initialization must not voxelize unused connected variants");
        }
    }

    private static BlockGetter level(Map<BlockPos, BlockState> states) {
        var level = mock(BlockGetter.class);
        when(level.getBlockState(any())).thenAnswer(call -> states.getOrDefault(call.getArgument(0), Blocks.AIR.defaultBlockState()));
        return level;
    }
}
