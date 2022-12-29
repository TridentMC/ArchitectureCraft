package com.tridevmc.architecture.client.render.model.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.tridevmc.architecture.client.render.model.builder.QuadPointDumper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Stores quad info that can be modified with given transforms, tintindices, and face sprites.
 */
public class ArchitectureModelData<T> {

    private static final Direction[] DIRECTIONS_WITH_NULL = new Direction[]{null, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN};
    private final DirectionalQuads quads = new DirectionalQuads();
    private final Map<String, ArchitectureVertex> vertexPool = Maps.newHashMap();
    protected Direction facing = Direction.NORTH;

    /**
     * Stores quads for each cardinal direction as well as general quads that don't fit into a specific direction.
     */
    protected class DirectionalQuads {
        private final List<IBakedQuadProvider<T>> northQuads = Lists.newArrayList();
        private final List<IBakedQuadProvider<T>> southQuads = Lists.newArrayList();
        private final List<IBakedQuadProvider<T>> eastQuads = Lists.newArrayList();
        private final List<IBakedQuadProvider<T>> westQuads = Lists.newArrayList();
        private final List<IBakedQuadProvider<T>> upQuads = Lists.newArrayList();
        private final List<IBakedQuadProvider<T>> downQuads = Lists.newArrayList();
        private final List<IBakedQuadProvider<T>> generalQuads = Lists.newArrayList();
        private final ImmutableList<List<IBakedQuadProvider<T>>> allQuads = ImmutableList.of(this.northQuads, this.southQuads, this.eastQuads, this.westQuads, this.upQuads, this.downQuads, this.generalQuads);

        private List<IBakedQuadProvider<T>> getQuads(@Nullable Direction direction) {
            if (direction == null) {
                return this.generalQuads;
            }
            return switch (direction) {
                case NORTH -> this.northQuads;
                case SOUTH -> this.southQuads;
                case EAST -> this.eastQuads;
                case WEST -> this.westQuads;
                case UP -> this.upQuads;
                case DOWN -> this.downQuads;
            };
        }

        private Stream<IBakedQuadProvider<T>> getAllQuads() {
            return Stream.of(this.northQuads,
                            this.southQuads,
                            this.eastQuads,
                            this.westQuads,
                            this.upQuads,
                            this.downQuads,
                            this.generalQuads)
                    .flatMap(List::stream);
        }

        private void addQuad(@Nullable Direction direction, IBakedQuadProvider<T> quad) {
            if (direction == null) {
                this.generalQuads.add(quad);
            } else {
                switch (direction) {
                    case NORTH -> this.northQuads.add(quad);
                    case SOUTH -> this.southQuads.add(quad);
                    case EAST -> this.eastQuads.add(quad);
                    case WEST -> this.westQuads.add(quad);
                    case UP -> this.upQuads.add(quad);
                    case DOWN -> this.downQuads.add(quad);
                }
            }
        }

        private IBakedQuadProvider<T> getCurrentlyBuildingQuad(T metadata, @Nullable Direction direction) {
            var quads = this.getQuads(direction);
            if (quads.isEmpty() || quads.get(quads.size() - 1).isComplete())
                quads.add(new ArchitectureQuad<>(metadata, direction));
            return quads.get(quads.size() - 1);
        }

        private IBakedQuadProvider<T> getCurrentlyBuildingQuad(T metadata, @Nullable Direction direction, Vector3f normals) {
            var quads = this.getQuads(direction);
            if (quads.isEmpty() || quads.get(quads.size() - 1).isComplete())
                quads.add(new ArchitectureQuad<>(metadata, direction, normals));
            return quads.get(quads.size() - 1);
        }

        private IBakedQuadProvider<T> getCurrentlyBuildingTri(T metadata, @Nullable Direction direction) {
            var quads = this.getQuads(direction);
            if (quads.isEmpty() || quads.get(quads.size() - 1).isComplete())
                quads.add(new ArchitectureTri<>(metadata, direction));
            return quads.get(quads.size() - 1);
        }

        private IBakedQuadProvider<T> getCurrentlyBuildingTri(T metadata, @Nullable Direction direction, Vector3f normals) {
            var quads = this.getQuads(direction);
            if (quads.isEmpty() || quads.get(quads.size() - 1).isComplete())
                quads.add(new ArchitectureTri<>(metadata, direction, normals));
            return quads.get(quads.size() - 1);
        }

        protected int getQuadCount(Direction direction) {
            return this.getQuads(direction).size();
        }

        protected int getNorthQuadCount() {
            return this.northQuads.size();
        }

        protected int getSouthQuadCount() {
            return this.southQuads.size();
        }

        protected int getEastQuadCount() {
            return this.eastQuads.size();
        }

        protected int getWestQuadCount() {
            return this.westQuads.size();
        }

        protected int getUpQuadCount() {
            return this.upQuads.size();
        }

        protected int getDownQuadCount() {
            return this.downQuads.size();
        }

        protected int getGeneralQuadCount() {
            return this.generalQuads.size();
        }
    }

    public ArchitectureModelData() {
    }

    public ArchitectureModelData(BakedModel sourceData) {
        this.loadFromBakedModel(sourceData);
    }

    public ArchitectureModelDataQuads getQuadsFor(IQuadMetadataResolver<T> metadataResolver, Transformation transform) {
        var out = new ArchitectureModelDataQuads(this.quads, transform);
        for (var dir : DIRECTIONS_WITH_NULL) {
            dir = dir != null ? transform.rotateTransform(dir) : null;
            var quads = this.quads.getQuads(dir);
            for (var quad : quads) {
                out.addQuad(dir, quad.bake(transform, dir, metadataResolver));
            }
        }
        return out;
    }

    private Direction rotate(Direction direction, Transformation transform) {
        Vec3i dir = direction.getNormal();
        Vector4f vec = new Vector4f(dir.getX(), dir.getY(), dir.getZ(), 0);
        transform.transformPosition(vec);
        return Direction.getNearest(vec.x(), vec.y(), vec.z());
    }

    public void loadFromBakedModel(BakedModel sourceData) {
        for (int i = -1; i < Direction.values().length; i++) {
            RandomSource rand = RandomSource.create();
            rand.setSeed(42L);
            Direction facing = null;
            if (i != -1) {
                facing = Direction.from3DDataValue(i);
            }
            List<BakedQuad> quads = sourceData.getQuads(Blocks.AIR.defaultBlockState(), facing, rand);

            for (BakedQuad quad : quads) {
                var points = new QuadPointDumper(quad).getPoints();
                for (Vec3 point : points) {
                    this.addQuadInstruction(null,
                            quad.getDirection(),
                            (float) point.x(),
                            (float) point.y(),
                            (float) point.z());
                }
            }
        }
    }

    // TODO: BakedQuadProviders need to build themselves next instead of just piping data one method call at a time.
    public void addQuadInstruction(T metadata, Direction facing, float x, float y, float z) {
        this.addQuadInstruction(metadata, -1, facing, x, y, z);
    }

    public void addQuadInstruction(T metadata, int face, Direction facing, float x, float y, float z) {
        var selectedQuad = this.quads.getCurrentlyBuildingQuad(metadata, facing);
        selectedQuad.setVertex(selectedQuad.getNextVertex(), this.getPooledVertex(face, x, y, z));
    }

    public void addQuadInstruction(T metadata, Direction facing, float x, float y, float z, float u, float v) {
        this.addQuadInstruction(metadata, -1, facing, x, y, z, u, v);
    }

    public void addQuadInstruction(T metadata, int face, Direction facing, float x, float y, float z, float u, float v) {
        var selectedQuad = this.quads.getCurrentlyBuildingQuad(metadata, facing);
        selectedQuad.setVertex(selectedQuad.getNextVertex(), this.getPooledVertex(face, x, y, z, u, v));
    }

    public void addQuadInstruction(T metadata, Direction facing, float x, float y, float z, float nX, float nY, float nZ) {
        this.addQuadInstruction(metadata, -1, facing, x, y, z, nX, nY, nZ);
    }

    public void addQuadInstruction(T metadata, int face, Direction facing, float x, float y, float z, float nX, float nY, float nZ) {
        var selectedQuad = this.quads.getCurrentlyBuildingQuad(metadata, facing, new Vector3f(nX, nY, nZ));
        selectedQuad.setVertex(selectedQuad.getNextVertex(), this.getPooledVertex(face, x, y, z, nX, nY, nZ));
    }

    public void addQuadInstruction(T metadata, Direction facing, float x, float y, float z, float u, float v, float nX, float nY, float nZ) {
        this.addQuadInstruction(metadata, -1, facing, x, y, z, u, v, nX, nY, nZ);
    }

    public void addQuadInstruction(T metadata, int face, Direction facing, float x, float y, float z, float u, float v, float nX, float nY, float nZ) {
        var selectedQuad = this.quads.getCurrentlyBuildingQuad(metadata, facing, new Vector3f(nX, nY, nZ));
        selectedQuad.setVertex(selectedQuad.getNextVertex(), this.getPooledVertex(face, x, y, z, u, v, nX, nY, nZ));
    }

    public void addTriInstruction(T metadata, Direction facing, double x, double y, double z) {
        this.addTriInstruction(metadata, -1, facing, x, y, z);
    }

    public void addTriInstruction(T metadata, int face, Direction facing, double x, double y, double z) {
        this.addTriInstruction(metadata, face, facing, (float) x, (float) y, (float) z);
    }

    public void addTriInstruction(T metadata, Direction facing, float x, float y, float z) {
        this.addTriInstruction(metadata, -1, facing, x, y, z);
    }

    public void addTriInstruction(T metadata, int face, Direction facing, float x, float y, float z) {
        var selectedQuad = this.quads.getCurrentlyBuildingTri(metadata, facing);
        selectedQuad.setVertex(selectedQuad.getNextVertex(), this.getPooledVertex(face, x, y, z));
    }

    public void addTriInstruction(T metadata, Direction facing, double x, double y, double z, double u, double v) {
        this.addTriInstruction(metadata, -1, facing, x, y, z, u, v);
    }

    public void addTriInstruction(T metadata, int face, Direction facing, double x, double y, double z, double u, double v) {
        this.addTriInstruction(metadata, face, facing, (float) x, (float) y, (float) z, (float) u, (float) v);
    }

    public void addTriInstruction(T metadata, Direction facing, float x, float y, float z, float u, float v) {
        this.addTriInstruction(metadata, -1, facing, x, y, z, u, v);
    }

    public void addTriInstruction(T metadata, int face, Direction facing, float x, float y, float z, float u, float v) {
        var selectedQuad = this.quads.getCurrentlyBuildingTri(metadata, facing);

        selectedQuad.setVertex(selectedQuad.getNextVertex(), this.getPooledVertex(face, x, y, z, u, v));
    }

    public void addTriInstruction(T metadata, Direction facing, double x, double y, double z, float nX, float nY, float nZ) {
        this.addTriInstruction(metadata, -1, facing, x, y, z, nX, nY, nZ);
    }

    public void addTriInstruction(T metadata, int face, Direction facing, double x, double y, double z, double nX, double nY, double nZ) {
        this.addTriInstruction(metadata, face, facing, (float) x, (float) y, (float) z, (float) nX, (float) nY, (float) nZ);
    }

    public void addTriInstruction(T metadata, Direction facing, float x, float y, float z, float nX, float nY, float nZ) {
        this.addTriInstruction(metadata, -1, facing, x, y, z, nX, nY, nZ);
    }

    public void addTriInstruction(T metadata, int face, Direction facing, float x, float y, float z, float nX, float nY, float nZ) {
        var selectedQuad = this.quads.getCurrentlyBuildingTri(metadata, facing, new Vector3f(nX, nY, nZ));
        selectedQuad.setVertex(selectedQuad.getNextVertex(), this.getPooledVertex(face, x, y, z, nX, nY, nZ));
    }

    public void addTriInstruction(T metadata, Direction facing, double x, double y, double z, double u, double v, double nX, double nY, double nZ) {
        this.addTriInstruction(metadata, -1, facing, x, y, z, u, v, nX, nY, nZ);
    }

    public void addTriInstruction(T metadata, int face, Direction facing, double x, double y, double z, double u, double v, double nX, double nY, double nZ) {
        this.addTriInstruction(metadata, face, facing, (float) x, (float) y, (float) z, (float) u, (float) v, (float) nX, (float) nY, (float) nZ);
    }

    public void addTriInstruction(T metadata, Direction facing, float x, float y, float z, float u, float v, float nX, float nY, float nZ) {
        this.addTriInstruction(metadata, -1, facing, x, y, z, u, v, nX, nY, nZ);
    }

    public void addTriInstruction(T metadata, int face, Direction facing, float x, float y, float z, float u, float v, float nX, float nY, float nZ) {
        var selectedQuad = this.quads.getCurrentlyBuildingTri(metadata, facing, new Vector3f(nX, nY, nZ));

        selectedQuad.setVertex(selectedQuad.getNextVertex(), this.getPooledVertex(face, x, y, z, u, v, nX, nY, nZ));
    }

    public ArchitectureVertex getPooledVertex(int face, float... data) {
        String vertexIdentity = face + Arrays.toString(data);
        ArchitectureVertex out = this.vertexPool.getOrDefault(vertexIdentity, null);
        if (out == null) {
            if (data.length == 3) {
                out = SmartArchitectureVertex.fromPosition(face, data);
            } else if (data.length == 5) {
                out = SmartArchitectureVertex.fromPositionWithUV(face, Arrays.copyOfRange(data, 0, 3), Arrays.copyOfRange(data, 3, 5));
            } else if (data.length == 6) {
                out = SmartArchitectureVertex.fromPositionWithNormal(face, Arrays.copyOfRange(data, 0, 3), Arrays.copyOfRange(data, 3, 6));
            } else if (data.length == 8) {
                out = new ArchitectureVertex(face, Arrays.copyOfRange(data, 0, 3), Arrays.copyOfRange(data, 3, 5), Arrays.copyOfRange(data, 5, 8));
            }
            this.vertexPool.put(vertexIdentity, out);
        }
        return out;
    }

    protected DirectionalQuads getQuads() {
        return this.quads;
    }

}
