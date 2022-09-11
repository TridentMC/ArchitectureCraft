package com.tridevmc.architecture.client.render.model.builder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class QuadPointDumper {

    private final List<Vec3> points = Lists.newArrayListWithCapacity(4);
    private ImmutableList<Vec3> immutablePoints;

    public QuadPointDumper(BakedQuad quad) {
        var consumer = new DumpingVertexConsumer();
        var poseStack = new PoseStack();
        consumer.putBulkData(poseStack.last(), quad, 1F, 1F, 1F, 1F, 1, 1, true);
        if (this.immutablePoints.size() < 4) {
            throw new IllegalArgumentException("QuadPointDumper was given a quad with less than 4 points!");
        }
    }

    private class DumpingVertexConsumer implements VertexConsumer {
        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            QuadPointDumper.this.points.add(new Vec3(x, y, z));
            return this;
        }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) {
            return this;
        }

        @Override
        public VertexConsumer overlayCoords(int u1, int v1) {
            return this;
        }

        @Override
        public VertexConsumer uv2(int u2, int v2) {
            return this;
        }

        @Override
        public VertexConsumer normal(float nX, float nY, float nZ) {
            return this;
        }

        @Override
        public void endVertex() {
            if (QuadPointDumper.this.points.size() == 4) {
                QuadPointDumper.this.immutablePoints = ImmutableList.copyOf(QuadPointDumper.this.points);
            }
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
        }

        @Override
        public void unsetDefaultColor() {
        }
    }

    public ImmutableList<Vec3> getPoints() {
        return this.immutablePoints;
    }
}