package com.tridevmc.architecture.client.render.model;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tridevmc.architecture.client.render.model.data.ArchitectureModelData;
import com.tridevmc.architecture.common.helpers.Vector3;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.MissingTextureSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ILightReader;

import java.util.*;
import java.util.stream.Collectors;

public abstract class OBJSONModel implements IArchitectureModel {

    private final OBJSON objson;
    private final ArchitectureModelData convertedModelData;
    protected ArrayList<Integer>[] textureQuadMap;

    public OBJSONModel(OBJSON objson) {
        this.objson = objson;
        this.convertedModelData = new ArchitectureModelData();
        ArrayList<ArrayList<Integer>> textureQuads = Lists.newArrayList();

        int quadNumber = 0;
        for (OBJSON.Face face : Arrays.stream(this.objson.faces).sorted(Comparator.comparingInt(o -> o.texture)).collect(Collectors.toList())) {
            ArrayList<Integer> quadList = this.addOrGet(textureQuads, face.texture, Lists.newArrayList());
            for (int[] tri : face.triangles) {
                quadNumber = this.splitAndAddTri(convertedModelData, quadList, quadNumber, tri, face.vertices);
            }
        }
        this.textureQuadMap = new ArrayList[textureQuads.size()];
        for (int i = 0; i < textureQuads.size(); i++) {
            this.textureQuadMap[i] = textureQuads.get(i);
        }
    }

    private int splitAndAddTri(ArchitectureModelData modelData, ArrayList<Integer> quadList, int quadNumber, int[] tri, double[][] vertices) {
        Set<Integer> xDimensions = Sets.newHashSet();
        Set<Integer> yDimensions = Sets.newHashSet();
        Set<Integer> zDimensions = Sets.newHashSet();
        Set<Integer>[] dimensions = new Set[]{xDimensions, yDimensions, zDimensions};
        List<TrackedVertex> trackedVertices = Lists.newArrayList();

        for (int i = 0; i < 3; i++) {
            int vertexIndex = tri[i];
            double[] vertex = vertices[vertexIndex];

            trackedVertices.add(new TrackedVertex(i, new Vector3(vertex[0] + 0.5, vertex[1] + 0.5, vertex[2] + 0.5)));

            for (int j = 0; j < 3; j++) {
                double coord = vertex[j] + 0.5;
                int dimension = (int) coord;
                if (Math.abs(dimension - coord) > 0) {
                    dimensions[j].add(dimension);
                }
            }
        }

        boolean needsSplit = Arrays.stream(dimensions).anyMatch(s -> s.size() > 1);
        if (needsSplit) {
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
                    triVertices.forEach(v -> modelData.addTriInstruction(null, v.vertex.x - 0.5, v.vertex.y - 0.5, v.vertex.z - 0.5));

                    quadList.add(quadNumber);
                    quadNumber++;
                }
            }
        } else {
            for (int i = 0; i < 3; i++) {
                int vertexIndex = tri[i];
                double[] vertex = vertices[vertexIndex];
                modelData.addTriInstruction(null, vertex[0], vertex[1], vertex[2]);
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
        List<TrackedVertex> newVertices = Lists.newArrayList();

        List<TrackedVertex> targetVertices = coreDimensionVertices.size() > otherDimensionVertices.size() ? coreDimensionVertices : otherDimensionVertices;
        TrackedVertex loneVertex = coreDimensionVertices.size() == 1 ? coreDimensionVertices.get(0) : otherDimensionVertices.get(0);

        List<List<TrackedVertex>> newTris = Lists.newArrayList();

        TrackedVertex lonePrevSplit = new TrackedVertex(loneVertex.previous.index, loneVertex.calculatePointAtY(loneVertex.previous.vertex, splitPoint));
        TrackedVertex loneNextSplit = new TrackedVertex(loneVertex.next.index, loneVertex.calculatePointAtY(loneVertex.next.vertex, splitPoint));
        newTris.add(Lists.newArrayList(lonePrevSplit, loneVertex, loneNextSplit).stream().sorted(Comparator.comparingInt(o -> o.index)).collect(Collectors.toList()));

        Vector3 lastVertex = loneVertex.previous.vertex;
        newTris.add(Lists.newArrayList(new TrackedVertex(0, lastVertex),
                new TrackedVertex(1, lonePrevSplit.vertex),
                new TrackedVertex(2, loneNextSplit.vertex)));

        Vector3 nextVertex = loneVertex.next.vertex;
        newTris.add(Lists.newArrayList(new TrackedVertex(0, nextVertex),
                new TrackedVertex(1, lastVertex),
                new TrackedVertex(2, loneNextSplit.vertex)));

        return newTris;
    }

    private class TrackedVertex {
        public TrackedVertex previous, next;
        public final int index;
        public final Vector3 vertex;

        public TrackedVertex(int index, Vector3 vertex) {
            this.index = index;
            this.vertex = vertex;
        }

        public Vector3 calculatePointAtX(Vector3 otherVertex, double targetX) {
            // First calculate the slope - m = y2-y1 / x2-x1
            double ySlope = (otherVertex.x - this.vertex.x) / (otherVertex.y - this.vertex.y);
            double zSlope = (otherVertex.x - this.vertex.x) / (otherVertex.z - this.vertex.z);

            // Welcome back to high school math everyone! x = y/m
            double resultingY = targetX / ySlope;
            double resultingZ = targetX / zSlope;

            return new Vector3(targetX, resultingY, resultingZ);
        }

        public Vector3 calculatePointAtY(Vector3 otherVertex, double targetY) {
            double xM = this.vertex.getXSlope(otherVertex);
            double zM = this.vertex.getZSlope(otherVertex);

            double xB = otherVertex.y - (xM * otherVertex.x);
            double zB = otherVertex.y - (zM * otherVertex.z);

            double resultingX = (targetY - xB) / xM;
            double resultingZ = (targetY - zB) / zM;

            resultingX = Double.isNaN(resultingX) ? this.vertex.x : resultingX;
            resultingZ = Double.isNaN(resultingZ) ? this.vertex.z : resultingZ;

            return new Vector3(resultingX, targetY, resultingZ);
        }

        public Vector3 calculatePointAtZ(Vector3 otherVertex, double targetZ) {
            // First calculate the slope - m = y2-y1 / x2-x1
            double xSlope = (otherVertex.z - this.vertex.z) / (otherVertex.x - this.vertex.x);
            double ySlope = (otherVertex.z - this.vertex.z) / (otherVertex.y - this.vertex.y);

            // Welcome back to high school math everyone! x = y/m
            double resultingX = targetZ / xSlope;
            double resultingY = targetZ / ySlope;

            return new Vector3(resultingX, resultingY, targetZ);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this)
                    .add("index", index)
                    .add("vertex", vertex)
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
