package com.tridevmc.architecture.client.render.model;

import com.google.common.base.Function;
import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tridevmc.architecture.client.render.model.data.ArchitectureModelData;
import com.tridevmc.architecture.common.ArchitectureLog;
import com.tridevmc.architecture.common.helpers.Vector3;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public abstract class OBJSONModel implements IArchitectureModel {

    private final OBJSON objson;
    private final ArchitectureModelData convertedModelData;
    private final boolean generateUVs;
    private final boolean generateNormals;
    protected ArrayList<Integer>[] textureQuadMap;

    private List<List<Vector3>> knownTris;

    public OBJSONModel(OBJSON objson, boolean generateUVs, boolean generateNormals) {
        this.objson = objson.offset(new Vector3(0.5, 0.5, 0.5));
        this.convertedModelData = new ArchitectureModelData();
        this.generateUVs = generateUVs;
        this.generateNormals = generateNormals;
        this.knownTris = Lists.newArrayList();
        List<Tuple<Integer, OBJSON.Face>> mappedFaces = IntStream.range(0, this.objson.faces.length).mapToObj(i -> new Tuple<>(i, this.objson.faces[i])).collect(Collectors.toList());
        mappedFaces = mappedFaces.stream().sorted(Comparator.comparingInt(o -> o.getB().texture)).collect(Collectors.toList());
        int minTexture = mappedFaces.get(0).getB().texture;
        mappedFaces.forEach(mF -> mF.getB().texture = mF.getB().texture - minTexture);
        ArrayList<ArrayList<Integer>> textureQuads = Lists.newArrayList();

        int quadNumber = 0;
        for (Tuple<Integer, OBJSON.Face> indexedFace : mappedFaces) {
            int faceIndex = indexedFace.getA();
            OBJSON.Face face = indexedFace.getB();
            ArrayList<Integer> quadList = this.addOrGet(textureQuads, face.texture, Lists.newArrayList());
            for (int[] tri : face.triangles) {
                quadNumber = this.splitAndAddTri(this.convertedModelData, quadList, faceIndex, quadNumber, tri, face.vertices);
            }
        }
        this.textureQuadMap = new ArrayList[textureQuads.size()];
        for (int i = 0; i < textureQuads.size(); i++) {
            this.textureQuadMap[i] = textureQuads.get(i);
        }
        this.convertedModelData.resetState();
    }

    private int splitAndAddTri(ArchitectureModelData modelData, ArrayList<Integer> quadList, int face, int quadNumber, int[] tri, double[][] vertices) {
        boolean addNormally = true;
        if (this.generateUVs) {
            Set<Integer> xDimensions = Sets.newHashSet();
            Set<Integer> yDimensions = Sets.newHashSet();
            Set<Integer> zDimensions = Sets.newHashSet();
            Set<Integer>[] dimensions = new Set[]{xDimensions, yDimensions, zDimensions};
            List<TrackedVertex> trackedVertices = Lists.newArrayList();

            List<Vector3> newTriData = Lists.newArrayList();

            for (int i = 0; i < 3; i++) {
                int vertexIndex = tri[i];
                double[] vertex = vertices[vertexIndex];

                trackedVertices.add(new TrackedVertex(i, new Vector3(vertex[0], vertex[1], vertex[2]), new Vector3(vertex[3], vertex[4], vertex[5])));
                newTriData.add(new Vector3(vertex[0], vertex[1], vertex[2]));

                for (int j = 0; j < 3; j++) {
                    double coord = vertex[j];
                    int dimension = (int) coord;
                    if (Math.abs(dimension - coord) > 0) {
                        dimensions[j].add(dimension);
                    }
                }
            }

            boolean triExists = this.knownTris.stream().anyMatch(t -> {
                for (Vector3 newTriDatum : newTriData) {
                    if (!t.contains(newTriDatum)) {
                        return false;
                    }
                }
                return true;
            });

            if (triExists) {
                ArchitectureLog.error("Tri already exists with provided data, \"{}\"", newTriData.toString());
                return quadNumber;
            }

            this.knownTris.add(newTriData);

            boolean needsSplit = Arrays.stream(dimensions).anyMatch(s -> s.size() > 1);
            if (needsSplit) {
                addNormally = false;
                for (int i = 0; i < trackedVertices.size(); i++) {
                    TrackedVertex nextVertex = trackedVertices.get(i == trackedVertices.size() - 1 ? 0 : i + 1);
                    TrackedVertex prevVertex = trackedVertices.get(i == 0 ? trackedVertices.size() - 1 : i - 1);

                    TrackedVertex curVertex = trackedVertices.get(i);
                    curVertex.next = nextVertex;
                    curVertex.previous = prevVertex;
                }

                if (yDimensions.size() > 1) {
                    List<List<TrackedVertex>> newTris = this.recursivelySplit(trackedVertices, xDimensions, yDimensions, zDimensions);

                    for (List<TrackedVertex> triVertices : newTris) {
                        triVertices.forEach(v -> {
                            if (this.generateNormals) {
                                modelData.addTriInstruction(face, null, v.vertex.x, v.vertex.y, v.vertex.z);
                            } else {
                                modelData.addTriInstruction(face, null, v.vertex.x, v.vertex.y, v.vertex.z, v.normal.x, v.normal.y, v.normal.z);
                            }
                        });

                        quadList.add(quadNumber);
                        quadNumber++;
                    }
                }
            }
        }
        if (addNormally) {
            for (int i = 0; i < 3; i++) {
                int vertexIndex = tri[i];
                double[] vertex = vertices[vertexIndex];
                if (this.generateUVs && this.generateNormals) {
                    modelData.addTriInstruction(face, null, vertex[0], vertex[1], vertex[2]);
                } else if (this.generateUVs) {
                    modelData.addTriInstruction(face, null, vertex[0], vertex[1], vertex[2], vertex[3], vertex[4], vertex[5]);
                } else if (this.generateNormals) {
                    modelData.addTriInstruction(face, null, vertex[0], vertex[1], vertex[2], vertex[6] * 16, vertex[7] * 16);
                } else {
                    modelData.addTriInstruction(face, null, vertex[0], vertex[1], vertex[2], vertex[6] * 16, vertex[7] * 16, vertex[3], vertex[4], vertex[5]);
                }
            }
            quadList.add(quadNumber);
            quadNumber++;
        }

        return quadNumber;
    }

    private List<List<TrackedVertex>> recursivelySplit(List<TrackedVertex> trackedVertices, Set<Integer> xDimensions, Set<Integer> yDimensions, Set<Integer> zDimensions) {
        List<List<TrackedVertex>> resultingTris = Lists.newArrayList();

        if (!yDimensions.isEmpty()) {
            List<List<TrackedVertex>> splittableTris = Lists.newArrayList();
            splittableTris.add(trackedVertices);
            while (!splittableTris.isEmpty()) {
                List<List<TrackedVertex>> splitTris = this.splitTriOnY(splittableTris.remove(0));
                for (List<TrackedVertex> vertices : splitTris) {
                    Set<Integer> splitTriYDimensions = Sets.newHashSet();

                    for (TrackedVertex vertex : vertices) {
                        double coord = vertex.vertex.y;
                        int dimension = (int) coord;
                        if (Math.abs(dimension - coord) > 0) {
                            splitTriYDimensions.add(dimension);
                        }
                    }
                    if (splitTriYDimensions.size() > 1) {
                        for (int i = 0; i < vertices.size(); i++) {
                            TrackedVertex nextVertex = vertices.get(i == vertices.size() - 1 ? 0 : i + 1);
                            TrackedVertex prevVertex = vertices.get(i == 0 ? vertices.size() - 1 : i - 1);

                            TrackedVertex curVertex = vertices.get(i);
                            curVertex.next = nextVertex;
                            curVertex.previous = prevVertex;
                        }

                        splittableTris.add(vertices);
                    } else {
                        resultingTris.add(vertices);
                    }
                }
            }
        }

        return resultingTris;
    }

    private List<List<TrackedVertex>> splitTriOnX(List<TrackedVertex> trackedVertices) {
        OptionalInt minDimension = trackedVertices.stream()
                .filter(trackedVertex -> Math.abs((((int) trackedVertex.vertex.x)) - (trackedVertex.vertex.x)) > 0)
                .mapToInt((v) -> (int) v.vertex.x)
                .min();

        if (!minDimension.isPresent()) {
            List<List<TrackedVertex>> out = Lists.newArrayList();
            out.add(trackedVertices);
            return out;
        }

        List<TrackedVertex> coreDimensionVertices = trackedVertices.stream().filter(v -> (int) (v.vertex.x) == minDimension.getAsInt()).collect(Collectors.toList());
        List<TrackedVertex> otherDimensionVertices = trackedVertices.stream().filter(v -> (int) (v.vertex.x) != minDimension.getAsInt()).collect(Collectors.toList());
        int splitPoint = minDimension.getAsInt() + 1; // if for example the dimension is 0, then split the try on the x-level 1
        TrackedVertex loneVertex = coreDimensionVertices.size() == 1 ? coreDimensionVertices.get(0) : otherDimensionVertices.get(0);

        List<List<TrackedVertex>> newTris = Lists.newArrayList();

        TrackedVertex lonePrevSplit = new TrackedVertex(loneVertex.previous.index, loneVertex.calculatePointAtX(loneVertex.previous.vertex, splitPoint), loneVertex.normal);
        TrackedVertex loneNextSplit = new TrackedVertex(loneVertex.next.index, loneVertex.calculatePointAtX(loneVertex.next.vertex, splitPoint), loneVertex.normal);
        newTris.add(Lists.newArrayList(lonePrevSplit, loneVertex, loneNextSplit).stream().sorted(Comparator.comparingInt(o -> o.index)).collect(Collectors.toList()));

        Vector3 lastVertex = loneVertex.previous.vertex;
        newTris.add(Lists.newArrayList(new TrackedVertex(0, lastVertex, loneVertex.normal),
                new TrackedVertex(1, lonePrevSplit.vertex, lonePrevSplit.normal),
                new TrackedVertex(2, loneNextSplit.vertex, loneNextSplit.normal)));

        Vector3 nextVertex = loneVertex.next.vertex;
        newTris.add(Lists.newArrayList(new TrackedVertex(0, nextVertex, loneVertex.next.normal),
                new TrackedVertex(1, lastVertex, loneVertex.previous.normal),
                new TrackedVertex(2, loneNextSplit.vertex, loneNextSplit.normal)));

        return newTris;
    }

    private List<List<TrackedVertex>> splitTriOnY(List<TrackedVertex> trackedVertices) {
        OptionalInt minDimension = trackedVertices.stream()
                .filter(trackedVertex -> Math.abs((((int) trackedVertex.vertex.y)) - (trackedVertex.vertex.y)) > 0)
                .mapToInt((v) -> (int) v.vertex.y)
                .min();

        if (!minDimension.isPresent()) {
            List<List<TrackedVertex>> out = Lists.newArrayList();
            out.add(trackedVertices);
            return out;
        }

        List<TrackedVertex> coreDimensionVertices = trackedVertices.stream().filter(v -> (int) (v.vertex.y) == minDimension.getAsInt()).collect(Collectors.toList());
        List<TrackedVertex> otherDimensionVertices = trackedVertices.stream().filter(v -> (int) (v.vertex.y) != minDimension.getAsInt()).collect(Collectors.toList());
        int splitPoint = minDimension.getAsInt() + 1; // if for example the dimension is 0, then split the try on the y-level 1
        TrackedVertex loneVertex = coreDimensionVertices.size() == 1 ? coreDimensionVertices.get(0) : otherDimensionVertices.get(0);

        List<List<TrackedVertex>> newTris = Lists.newArrayList();

        TrackedVertex lonePrevSplit = new TrackedVertex(loneVertex.previous.index, loneVertex.calculatePointAtY(loneVertex.previous.vertex, splitPoint), loneVertex.normal);
        TrackedVertex loneNextSplit = new TrackedVertex(loneVertex.next.index, loneVertex.calculatePointAtY(loneVertex.next.vertex, splitPoint), loneVertex.normal);
        newTris.add(Lists.newArrayList(lonePrevSplit, loneVertex, loneNextSplit).stream().sorted(Comparator.comparingInt(o -> o.index)).collect(Collectors.toList()));

        Vector3 lastVertex = loneVertex.previous.vertex;
        newTris.add(Lists.newArrayList(new TrackedVertex(0, lastVertex, loneVertex.previous.normal),
                new TrackedVertex(1, lonePrevSplit.vertex, lonePrevSplit.normal),
                new TrackedVertex(2, loneNextSplit.vertex, loneNextSplit.normal)));

        Vector3 nextVertex = loneVertex.next.vertex;
        newTris.add(Lists.newArrayList(new TrackedVertex(0, nextVertex, loneVertex.next.normal),
                new TrackedVertex(1, lastVertex, loneVertex.previous.normal),
                new TrackedVertex(2, loneNextSplit.vertex, loneNextSplit.normal)));

        return newTris;
    }

    private List<List<TrackedVertex>> splitTriOnZ(List<TrackedVertex> trackedVertices) {
        OptionalInt minDimension = trackedVertices.stream()
                .filter(trackedVertex -> Math.abs((((int) trackedVertex.vertex.z)) - (trackedVertex.vertex.z)) > 0)
                .mapToInt((v) -> (int) v.vertex.z)
                .min();

        if (!minDimension.isPresent()) {
            List<List<TrackedVertex>> out = Lists.newArrayList();
            out.add(trackedVertices);
            return out;
        }

        List<TrackedVertex> coreDimensionVertices = trackedVertices.stream().filter(v -> (int) (v.vertex.z) == minDimension.getAsInt()).collect(Collectors.toList());
        List<TrackedVertex> otherDimensionVertices = trackedVertices.stream().filter(v -> (int) (v.vertex.z) != minDimension.getAsInt()).collect(Collectors.toList());
        int splitPoint = minDimension.getAsInt() + 1; // if for example the dimension is 0, then split the try on the z-level 1
        TrackedVertex loneVertex = coreDimensionVertices.size() == 1 ? coreDimensionVertices.get(0) : otherDimensionVertices.get(0);

        List<List<TrackedVertex>> newTris = Lists.newArrayList();

        TrackedVertex lonePrevSplit = new TrackedVertex(loneVertex.previous.index, loneVertex.calculatePointAtZ(loneVertex.previous.vertex, splitPoint), loneVertex.normal);
        TrackedVertex loneNextSplit = new TrackedVertex(loneVertex.next.index, loneVertex.calculatePointAtZ(loneVertex.next.vertex, splitPoint), loneVertex.normal);
        newTris.add(Lists.newArrayList(lonePrevSplit, loneVertex, loneNextSplit).stream().sorted(Comparator.comparingInt(o -> o.index)).collect(Collectors.toList()));

        Vector3 lastVertex = loneVertex.previous.vertex;
        newTris.add(Lists.newArrayList(new TrackedVertex(0, lastVertex, loneVertex.previous.normal),
                new TrackedVertex(1, lonePrevSplit.vertex, lonePrevSplit.normal),
                new TrackedVertex(2, loneNextSplit.vertex, loneNextSplit.normal)));

        Vector3 nextVertex = loneVertex.next.vertex;
        newTris.add(Lists.newArrayList(new TrackedVertex(0, nextVertex, loneVertex.next.normal),
                new TrackedVertex(1, lastVertex, loneVertex.previous.normal),
                new TrackedVertex(2, loneNextSplit.vertex, loneNextSplit.normal)));

        return newTris;
    }

    private class TrackedVertex {
        public TrackedVertex previous, next;
        public final int index;
        public final Vector3 vertex;
        public final Vector3 normal;

        public TrackedVertex(int index, Vector3 vertex, Vector3 normal) {
            this.index = index;
            this.vertex = vertex;
            this.normal = normal;
        }

        public Vector3 calculatePointAtX(Vector3 otherVertex, double targetX) {
            double yM = this.vertex.getYXSlope(otherVertex);
            double zM = this.vertex.getZXSlope(otherVertex);

            double yB = otherVertex.x - (yM * otherVertex.x);
            double zB = otherVertex.x - (zM * otherVertex.z);

            double resultingY = (targetX - yB) / yM;
            double resultingZ = (targetX - zB) / zM;

            resultingY = Double.isNaN(resultingY) ? this.vertex.y : resultingY;
            resultingZ = Double.isNaN(resultingZ) ? this.vertex.z : resultingZ;

            return new Vector3(targetX, resultingY, resultingZ);
        }

        public Vector3 calculatePointAtY(Vector3 otherVertex, double targetY) {
            double xM = this.vertex.getXYSlope(otherVertex);
            double zM = this.vertex.getZYSlope(otherVertex);

            double xB = otherVertex.y - (xM * otherVertex.x);
            double zB = otherVertex.y - (zM * otherVertex.z);

            double resultingX = (targetY - xB) / xM;
            double resultingZ = (targetY - zB) / zM;

            resultingX = Double.isNaN(resultingX) ? this.vertex.x : resultingX;
            resultingZ = Double.isNaN(resultingZ) ? this.vertex.z : resultingZ;

            return new Vector3(resultingX, targetY, resultingZ);
        }

        public Vector3 calculatePointAtZ(Vector3 otherVertex, double targetZ) {
            double xM = this.vertex.getXZSlope(otherVertex);
            double yM = this.vertex.getYZSlope(otherVertex);

            double xB = otherVertex.z - (xM * otherVertex.x);
            double yB = otherVertex.z - (yM * otherVertex.y);

            double resultingX = (targetZ - xB) / xM;
            double resultingY = (targetZ - yB) / yM;

            resultingX = Double.isNaN(resultingX) ? this.vertex.x : resultingX;
            resultingY = Double.isNaN(resultingY) ? this.vertex.y : resultingY;

            return new Vector3(resultingX, resultingY, targetZ);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("index", this.index)
                    .add("vertex", this.vertex)
                    .toString();
        }
    }

    private <T> T addOrGet(ArrayList<T> list, int index, T element) {
        if (index >= list.size()) {
            list.add(index, element);
        }
        return list.get(index);
    }

    @Override
    public ArchitectureModelData.ModelDataQuads getQuads(BlockState state, ILightReader world, BlockPos pos) {
        this.convertedModelData.setState(state);
        TextureAtlasSprite[] textures = this.getTextures(world, pos);
        Integer[] colours = this.getColours(world, pos);
        for (int i = 0; i < this.textureQuadMap.length; i++) {
            ArrayList<Integer> quads = this.textureQuadMap[i];
            TextureAtlasSprite texture = textures[i];
            Integer colour = colours[i];

            for (Integer quad : quads) {
                this.convertedModelData.setFaceData(quad, null, texture, colour);
            }
        }

        return this.convertedModelData.buildModel();
    }

    @Override
    public TextureAtlasSprite getDefaultSprite() {
        return Minecraft.getInstance().getAtlasSpriteGetter(AtlasTexture.LOCATION_BLOCKS_TEXTURE).apply(MissingTextureSprite.getLocation());
    }

    @Override
    public List<BakedQuad> getDefaultModel() {
        return Collections.emptyList();
    }

    public abstract TextureAtlasSprite[] getTextures(ILightReader world, BlockPos pos);

    public abstract Integer[] getColours(ILightReader world, BlockPos pos);
}
