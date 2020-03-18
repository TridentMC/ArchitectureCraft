package com.tridevmc.architecture.client.render.model.data;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tridevmc.architecture.client.render.model.builder.QuadPointDumper;
import com.tridevmc.architecture.client.render.model.data.ArchitectureQuad;
import com.tridevmc.architecture.client.render.model.data.IBakedQuadProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.TransformationMatrix;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.Vector4f;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3i;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Stores quad info that can be modified with given transforms, tintindices, and face sprites.
 */
public class ArchitectureModelData {

    private final Map<Direction, List<IBakedQuadProvider>> quads = Maps.newHashMap();

    protected BlockState state;
    protected Direction facing = Direction.NORTH;
    protected TransformationMatrix transform = TransformationMatrix.identity();
    protected ArrayList<Integer>[] tintIndices = new ArrayList[Direction.values().length + 1];
    protected ArrayList<TextureAtlasSprite>[] faceSprites = new ArrayList[Direction.values().length + 1];

    public ArchitectureModelData() {
        for (int i = 0; i < this.tintIndices.length; i++) {
            this.tintIndices[i] = Lists.newArrayList();
            this.faceSprites[i] = Lists.newArrayList();
        }
    }

    public ArchitectureModelData(IBakedModel sourceData) {
        this();
        this.loadFromBakedModel(sourceData);
    }

    public void setFaceData(int quadNumber, Direction side, TextureAtlasSprite sprite, int tintIndex) {
        this.addOrSet(this.faceSprites[side != null ? side.getIndex() : Direction.values().length], quadNumber, sprite);
        this.addOrSet(this.tintIndices[side != null ? side.getIndex() : Direction.values().length], quadNumber, tintIndex);
    }

    private void addOrSet(ArrayList list, int index, Object element) {
        if (index >= list.size()) {
            list.add(index, element);
        } else {
            list.set(index, element);
        }
    }

    public void setTransform(Direction facing, TransformationMatrix transform) {
        this.facing = facing;
        this.transform = transform;
    }

    public void setState(BlockState state) {
        this.state = state;
    }

    public ModelDataQuads buildModel() {
        List<BakedQuad> generalQuads = Lists.newArrayList();
        Map<Direction, List<BakedQuad>> faceQuads = Maps.newHashMap();
        IntStream.range(-1, Direction.values().length).forEach((i) -> faceQuads.put(i > -1 ? Direction.byIndex(i) : null, Lists.newArrayList()));

        for (Map.Entry<Direction, List<IBakedQuadProvider>> quadFaceEntry : this.quads.entrySet()) {
            Direction oldFace = quadFaceEntry.getKey();
            Direction newFace = oldFace == null ? null : this.rotate(oldFace, this.transform);
            int faceIndex = newFace == null ? this.faceSprites.length - 1 : newFace.getIndex();
            List<IBakedQuadProvider> quads = quadFaceEntry.getValue();

            ArrayList<TextureAtlasSprite> spritesForFace = this.faceSprites[faceIndex];
            ArrayList<Integer> tintsForFace = this.tintIndices[faceIndex];

            for (int i = 0; i < quads.size(); i++) {
                IBakedQuadProvider quad = quads.get(i);
                BakedQuad builtQuad = quad.bake(this.transform, newFace,
                        spritesForFace.get(i),
                        tintsForFace.get(i));
                generalQuads.add(builtQuad);
                faceQuads.get(newFace).add(builtQuad);
            }
        }
        this.setup();
        return new ModelDataQuads(generalQuads, faceQuads);
    }

    private Direction rotate(Direction direction, TransformationMatrix transform) {
        Vec3i dir = direction.getDirectionVec();
        Vector4f vec = new Vector4f(dir.getX(), dir.getY(), dir.getZ(), 0);
        transform.transformPosition(vec);
        return Direction.getFacingFromVector(vec.getX(), vec.getY(), vec.getZ());
    }

    protected void setup() {
        // reset the model data for a new draw request.
        this.state = Blocks.AIR.getDefaultState();
        this.facing = Direction.NORTH;

        this.transform = new TransformationMatrix(Matrix4f.makeTranslate(0.5F, 0.5F, 0.5F));
        this.tintIndices = new ArrayList[Direction.values().length + 1];
        this.faceSprites = new ArrayList[Direction.values().length + 1];

        for (int i = 0; i < this.tintIndices.length; i++) {
            this.tintIndices[i] = Lists.newArrayList();
            this.faceSprites[i] = Lists.newArrayList();
        }
    }

    public void loadFromBakedModel(IBakedModel sourceData) {
        for (int i = -1; i < Direction.values().length; i++) {
            Random rand = new Random();
            rand.setSeed(42L);
            Direction facing = null;
            if (i != -1) {
                facing = Direction.byIndex(i);
            }
            List<BakedQuad> quads = sourceData.getQuads(Blocks.AIR.getDefaultState(), facing, rand);

            for (BakedQuad quad : quads) {
                Vector3f[] points = new QuadPointDumper(quad).getPoints();
                for (Vector3f point : points) {
                    this.addQuadInstruction(quad.getFace(), point.getX(), point.getY(), point.getZ());
                }
            }
        }
    }

    public void addQuadInstruction(Direction facing, float x, float y, float z) {
        float[] data = new float[]{x, y, z};

        if (!this.quads.containsKey(facing))
            this.quads.put(facing, new ArrayList<>());

        List<IBakedQuadProvider> faceQuads = this.quads.get(facing);
        if (faceQuads.isEmpty() || faceQuads.get(faceQuads.size() - 1).isComplete())
            faceQuads.add(new ArchitectureQuad(facing));

        IBakedQuadProvider selectedQuad = faceQuads.get(faceQuads.size() - 1);
        selectedQuad.setVertex(selectedQuad.getNextVertex(), data);
    }

    public void addQuadInstruction(Direction facing, float x, float y, float z, float u, float v, float nX, float nY, float nZ) {
        float[] data = new float[]{x, y, z};
        float[] uvs = new float[]{u, v};

        if (!this.quads.containsKey(facing))
            this.quads.put(facing, new ArrayList<>());

        List<IBakedQuadProvider> faceQuads = this.quads.get(facing);
        if (faceQuads.isEmpty() || faceQuads.get(faceQuads.size() - 1).isComplete())
            faceQuads.add(new ArchitectureQuad(facing, new Vector3f(nX, nY, nZ)));

        IBakedQuadProvider selectedQuad = faceQuads.get(faceQuads.size() - 1);
        selectedQuad.setVertex(selectedQuad.getNextVertex(), data, uvs);
    }

    public void addTriInstruction(Direction facing, double x, double y, double z) {
        this.addTriInstruction(facing, (float) x, (float) y, (float) z);
    }

    public void addTriInstruction(Direction facing, float x, float y, float z) {
        float[] data = new float[]{x, y, z};

        if (!this.quads.containsKey(facing))
            this.quads.put(facing, new ArrayList<>());

        List<IBakedQuadProvider> faceQuads = this.quads.get(facing);
        if (faceQuads.isEmpty() || faceQuads.get(faceQuads.size() - 1).isComplete())
            faceQuads.add(new ArchitectureQuad(facing));

        IBakedQuadProvider selectedQuad = faceQuads.get(faceQuads.size() - 1);

        // If this is the first vertex being added to the quad then add it twice so the tri creates a full quad.
        if (selectedQuad.getNextVertex() == 0)
            selectedQuad.setVertex(selectedQuad.getNextVertex(), data);
        selectedQuad.setVertex(selectedQuad.getNextVertex(), data);
    }

    public void addTriInstruction(Direction facing, double x, double y, double z, double u, double v, double nX, double nY, double nZ) {
        this.addTriInstruction(facing, (float) x, (float) y, (float) z, (float) u, (float) v, (float) nX, (float) nY, (float) nZ);
    }

    public void addTriInstruction(Direction facing, float x, float y, float z, float u, float v, float nX, float nY, float nZ) {
        float[] data = new float[]{x, y, z};
        float[] uvs = new float[]{u, v};

        if (!this.quads.containsKey(facing))
            this.quads.put(facing, new ArrayList<>());

        List<IBakedQuadProvider> faceQuads = this.quads.get(facing);
        if (faceQuads.isEmpty() || faceQuads.get(faceQuads.size() - 1).isComplete())
            faceQuads.add(new ArchitectureQuad(facing, new Vector3f(nX, nY, nZ)));

        IBakedQuadProvider selectedQuad = faceQuads.get(faceQuads.size() - 1);

        // If this is the first vertex being added to the quad then add it twice so the tri creates a full quad.
        if (selectedQuad.getNextVertex() == 0)
            selectedQuad.setVertex(selectedQuad.getNextVertex(), data, uvs);
        selectedQuad.setVertex(selectedQuad.getNextVertex(), data, uvs);
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
