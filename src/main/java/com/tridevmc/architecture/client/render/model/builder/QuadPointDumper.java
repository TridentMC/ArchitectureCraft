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

    public ImmutableList<Vec3> getPoints() {
        return this.immutablePoints;
    }

    private class DumpingVertexConsumer implements VertexConsumer {

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            if (QuadPointDumper.this.points.size() == 4) {
                QuadPointDumper.this.immutablePoints = ImmutableList.copyOf(QuadPointDumper.this.points);
            }

            QuadPointDumper.this.points.add(new Vec3(x, y, z));
            return this;
        }

        @Override
        public VertexConsumer setColor(int i, int i1, int i2, int i3) {
            return this;
        }

        @Override
        public VertexConsumer setUv(float v, float v1) {
            return this;
        }

        @Override
        public VertexConsumer setUv1(int i, int i1) {
            return this;
        }

        @Override
        public VertexConsumer setUv2(int i, int i1) {
            return this;
        }

        @Override
        public VertexConsumer setNormal(float v, float v1, float v2) {
            return this;
        }
    }

}