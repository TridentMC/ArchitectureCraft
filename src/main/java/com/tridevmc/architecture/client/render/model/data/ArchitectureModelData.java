package com.tridevmc.architecture.client.render.model.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import com.tridevmc.architecture.client.render.model.builder.QuadPointDumper;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.stream.IntStream;

/**
 * Stores quad info that can be modified with given transforms, tintindices, and face sprites.
 */
public class ArchitectureModelData {

    private final Map<Direction, List<IBakedQuadProvider>> quads = Maps.newHashMap();
    private final Map<String, ArchitectureVertex> vertexPool = Maps.newHashMap();

    private boolean isLocked = false;
    protected BlockState state;
    protected Direction facing = Direction.NORTH;
    protected Transformation transform = Transformation.identity();
    protected ArrayList<Integer>[] tintIndices = new ArrayList[Direction.values().length + 1];
    protected ArrayList<TextureAtlasSprite>[] faceSprites = new ArrayList[Direction.values().length + 1];

    public ArchitectureModelData() {
        for (int i = 0; i < this.tintIndices.length; i++) {
            this.tintIndices[i] = Lists.newArrayList();
            this.faceSprites[i] = Lists.newArrayList();
        }
    }

    public ArchitectureModelData(BakedModel sourceData) {
        this();
        this.loadFromBakedModel(sourceData);
    }

    public void setFaceData(int quadNumber, Direction side, TextureAtlasSprite sprite, int tintIndex) {
        this.addOrSet(this.faceSprites[side != null ? side.get3DDataValue() : Direction.values().length], quadNumber, sprite);
        this.addOrSet(this.tintIndices[side != null ? side.get3DDataValue() : Direction.values().length], quadNumber, tintIndex);
    }

    private void addOrSet(ArrayList list, int index, Object element) {
        if (index >= list.size()) {
            list.add(index, element);
        } else {
            list.set(index, element);
        }
    }

    public void setTransform(Direction facing, Transformation transform) {
        this.facing = facing;
        this.transform = transform;
    }

    public void setState(BlockState state) {
        this.state = state;
    }

    public ModelDataQuads buildModel() {
        if (!this.isLocked()) {
            this.lock();
        }
        List<BakedQuad> generalQuads = Lists.newArrayList();
        Map<Direction, List<BakedQuad>> faceQuads = Maps.newHashMap();
        IntStream.range(-1, Direction.values().length).forEach((i) -> faceQuads.put(i > -1 ? Direction.from3DDataValue(i) : null, Lists.newArrayList()));

        for (Map.Entry<Direction, List<IBakedQuadProvider>> quadFaceEntry : this.quads.entrySet()) {
            Direction oldFace = quadFaceEntry.getKey();
            Direction newFace = oldFace == null ? null : this.rotate(oldFace, this.transform);
            int faceIndex = newFace == null ? this.faceSprites.length - 1 : newFace.get3DDataValue();
            List<IBakedQuadProvider> quads = quadFaceEntry.getValue();

            ArrayList<TextureAtlasSprite> spritesForFace = this.faceSprites[faceIndex];
            ArrayList<Integer> tintsForFace = this.tintIndices[faceIndex];

            for (int i = 0; i < quads.size(); i++) {
                IBakedQuadProvider quad = quads.get(i);
                BakedQuad builtQuad = quad.bake(this.transform, newFace,
                        spritesForFace.get(i),
                        tintsForFace.get(i));

                LightUtil.setLightData(builtQuad, 1);
                if (builtQuad != null) {
                    generalQuads.add(builtQuad);
                    faceQuads.get(newFace).add(builtQuad);
                }
            }
        }
        this.resetState();
        return new ModelDataQuads(generalQuads, faceQuads);
    }

    private Direction rotate(Direction direction, Transformation transform) {
        Vec3i dir = direction.getNormal();
        Vector4f vec = new Vector4f(dir.getX(), dir.getY(), dir.getZ(), 0);
        transform.transformPosition(vec);
        return Direction.getNearest(vec.x(), vec.y(), vec.z());
    }

    public void resetState() {
        // reset the model data for a new draw request.
        this.state = Blocks.AIR.defaultBlockState();
        this.facing = Direction.NORTH;

        this.transform = Transformation.identity();
        this.tintIndices = new ArrayList[Direction.values().length + 1];
        this.faceSprites = new ArrayList[Direction.values().length + 1];

        for (int i = 0; i < this.tintIndices.length; i++) {
            this.tintIndices[i] = Lists.newArrayList();
            this.faceSprites[i] = Lists.newArrayList();
        }
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
                Vector3f[] points = new QuadPointDumper(quad).getPoints();
                for (Vector3f point : points) {
                    this.addQuadInstruction(quad.getDirection(), point.x(), point.y(), point.z());
                }
            }
        }
    }

    public void addQuadInstruction(Direction facing, float x, float y, float z) {
        this.addQuadInstruction(-1, facing, x, y, z);
    }

    public void addQuadInstruction(int face, Direction facing, float x, float y, float z) {
        if (!this.quads.containsKey(facing))
            this.quads.put(facing, new ArrayList<>());

        List<IBakedQuadProvider> faceQuads = this.quads.get(facing);
        if (faceQuads.isEmpty() || faceQuads.get(faceQuads.size() - 1).isComplete())
            faceQuads.add(new ArchitectureQuad(facing));

        IBakedQuadProvider selectedQuad = faceQuads.get(faceQuads.size() - 1);
        selectedQuad.setVertex(selectedQuad.getNextVertex(), this.getPooledVertex(face, x, y, z));
    }

    public void addQuadInstruction(Direction facing, float x, float y, float z, float u, float v) {
        this.addQuadInstruction(-1, facing, x, y, z, u, v);
    }

    public void addQuadInstruction(int face, Direction facing, float x, float y, float z, float u, float v) {
        if (!this.quads.containsKey(facing))
            this.quads.put(facing, new ArrayList<>());

        List<IBakedQuadProvider> faceQuads = this.quads.get(facing);
        if (faceQuads.isEmpty() || faceQuads.get(faceQuads.size() - 1).isComplete())
            faceQuads.add(new ArchitectureQuad(facing));

        IBakedQuadProvider selectedQuad = faceQuads.get(faceQuads.size() - 1);
        selectedQuad.setVertex(selectedQuad.getNextVertex(), this.getPooledVertex(face, x, y, z, u, v));
    }

    public void addQuadInstruction(Direction facing, float x, float y, float z, float nX, float nY, float nZ) {
        this.addQuadInstruction(-1, facing, x, y, z, nX, nY, nZ);
    }

    public void addQuadInstruction(int face, Direction facing, float x, float y, float z, float nX, float nY, float nZ) {
        if (!this.quads.containsKey(facing))
            this.quads.put(facing, new ArrayList<>());

        List<IBakedQuadProvider> faceQuads = this.quads.get(facing);
        if (faceQuads.isEmpty() || faceQuads.get(faceQuads.size() - 1).isComplete())
            faceQuads.add(new ArchitectureQuad(facing, new Vector3f(nX, nY, nZ)));

        IBakedQuadProvider selectedQuad = faceQuads.get(faceQuads.size() - 1);
        selectedQuad.setVertex(selectedQuad.getNextVertex(), this.getPooledVertex(face, x, y, z, nX, nY, nZ));
    }

    public void addQuadInstruction(Direction facing, float x, float y, float z, float u, float v, float nX, float nY, float nZ) {
        this.addQuadInstruction(-1, facing, x, y, z, u, v, nX, nY, nZ);
    }

    public void addQuadInstruction(int face, Direction facing, float x, float y, float z, float u, float v, float nX, float nY, float nZ) {
        if (!this.quads.containsKey(facing))
            this.quads.put(facing, new ArrayList<>());

        List<IBakedQuadProvider> faceQuads = this.quads.get(facing);
        if (faceQuads.isEmpty() || faceQuads.get(faceQuads.size() - 1).isComplete())
            faceQuads.add(new ArchitectureQuad(facing, new Vector3f(nX, nY, nZ)));

        IBakedQuadProvider selectedQuad = faceQuads.get(faceQuads.size() - 1);
        selectedQuad.setVertex(selectedQuad.getNextVertex(), this.getPooledVertex(face, x, y, z, u, v, nX, nY, nZ));
    }

    public void addTriInstruction(Direction facing, double x, double y, double z) {
        this.addTriInstruction(-1, facing, x, y, z);
    }

    public void addTriInstruction(int face, Direction facing, double x, double y, double z) {
        this.addTriInstruction(face, facing, (float) x, (float) y, (float) z);
    }

    public void addTriInstruction(Direction facing, float x, float y, float z) {
        this.addTriInstruction(-1, facing, x, y, z);
    }

    public void addTriInstruction(int face, Direction facing, float x, float y, float z) {
        if (!this.quads.containsKey(facing))
            this.quads.put(facing, new ArrayList<>());

        List<IBakedQuadProvider> faceQuads = this.quads.get(facing);
        if (faceQuads.isEmpty() || faceQuads.get(faceQuads.size() - 1).isComplete())
            faceQuads.add(new ArchitectureTri(facing));

        IBakedQuadProvider selectedQuad = faceQuads.get(faceQuads.size() - 1);

        selectedQuad.setVertex(selectedQuad.getNextVertex(), this.getPooledVertex(face, x, y, z));
    }

    public void addTriInstruction(Direction facing, double x, double y, double z, double u, double v) {
        this.addTriInstruction(-1, facing, x, y, z, u, v);
    }

    public void addTriInstruction(int face, Direction facing, double x, double y, double z, double u, double v) {
        this.addTriInstruction(face, facing, (float) x, (float) y, (float) z, (float) u, (float) v);
    }

    public void addTriInstruction(Direction facing, float x, float y, float z, float u, float v) {
        this.addTriInstruction(-1, facing, x, y, z, u, v);
    }

    public void addTriInstruction(int face, Direction facing, float x, float y, float z, float u, float v) {
        if (!this.quads.containsKey(facing))
            this.quads.put(facing, new ArrayList<>());

        List<IBakedQuadProvider> faceQuads = this.quads.get(facing);
        if (faceQuads.isEmpty() || faceQuads.get(faceQuads.size() - 1).isComplete())
            faceQuads.add(new ArchitectureTri(facing));

        IBakedQuadProvider selectedQuad = faceQuads.get(faceQuads.size() - 1);

        selectedQuad.setVertex(selectedQuad.getNextVertex(), this.getPooledVertex(face, x, y, z, u, v));
    }

    public void addTriInstruction(Direction facing, double x, double y, double z, float nX, float nY, float nZ) {
        this.addTriInstruction(-1, facing, x, y, z, nX, nY, nZ);
    }

    public void addTriInstruction(int face, Direction facing, double x, double y, double z, double nX, double nY, double nZ) {
        this.addTriInstruction(face, facing, (float) x, (float) y, (float) z, (float) nX, (float) nY, (float) nZ);
    }

    public void addTriInstruction(Direction facing, float x, float y, float z, float nX, float nY, float nZ) {
        this.addTriInstruction(-1, facing, x, y, z, nX, nY, nZ);
    }

    public void addTriInstruction(int face, Direction facing, float x, float y, float z, float nX, float nY, float nZ) {
        if (!this.quads.containsKey(facing))
            this.quads.put(facing, new ArrayList<>());

        List<IBakedQuadProvider> faceQuads = this.quads.get(facing);
        if (faceQuads.isEmpty() || faceQuads.get(faceQuads.size() - 1).isComplete())
            faceQuads.add(new ArchitectureTri(facing));

        IBakedQuadProvider selectedQuad = faceQuads.get(faceQuads.size() - 1);

        selectedQuad.setVertex(selectedQuad.getNextVertex(), this.getPooledVertex(face, x, y, z, nX, nY, nZ));
    }

    public void addTriInstruction(Direction facing, double x, double y, double z, double u, double v, double nX, double nY, double nZ) {
        this.addTriInstruction(-1, facing, x, y, z, u, v, nX, nY, nZ);
    }

    public void addTriInstruction(int face, Direction facing, double x, double y, double z, double u, double v, double nX, double nY, double nZ) {
        this.addTriInstruction(face, facing, (float) x, (float) y, (float) z, (float) u, (float) v, (float) nX, (float) nY, (float) nZ);
    }

    public void addTriInstruction(Direction facing, float x, float y, float z, float u, float v, float nX, float nY, float nZ) {
        this.addTriInstruction(-1, facing, x, y, z, u, v, nX, nY, nZ);
    }

    public void addTriInstruction(int face, Direction facing, float x, float y, float z, float u, float v, float nX, float nY, float nZ) {
        if (!this.quads.containsKey(facing))
            this.quads.put(facing, new ArrayList<>());

        List<IBakedQuadProvider> faceQuads = this.quads.get(facing);
        if (faceQuads.isEmpty() || faceQuads.get(faceQuads.size() - 1).isComplete())
            faceQuads.add(new ArchitectureTri(facing, new Vector3f(nX, nY, nZ)));

        IBakedQuadProvider selectedQuad = faceQuads.get(faceQuads.size() - 1);

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

    public void lock() {
        this.isLocked = true;
        this.vertexPool.values().stream().filter(ArchitectureVertex::assignNormals).forEach(v -> v.setNormals(new Vector3f(0, 0, 0)));
        this.quads.values().forEach(qL -> qL.forEach((IBakedQuadProvider::assignNormals)));
        this.vertexPool.values().stream().filter(ArchitectureVertex::assignNormals).forEach(v -> {
            Vector3f normals = v.getNormals();
            normals.normalize();
            v.setNormals(normals);
        });
    }

    public boolean isLocked() {
        return this.isLocked;
    }

    public class ModelDataQuads {
        private final List<BakedQuad> generalQuads;
        private final Map<Direction, List<BakedQuad>> faceQuads;

        public ModelDataQuads(List<BakedQuad> generalQuads, Map<Direction, List<BakedQuad>> faceQuads) {
            this.generalQuads = generalQuads;
            this.faceQuads = faceQuads;
        }

        public List<BakedQuad> getGeneralQuads() {
            return this.generalQuads;
        }

        public Map<Direction, List<BakedQuad>> getFaceQuads() {
            return this.faceQuads;
        }
    }
}
