package com.tridevmc.architecture.client.render.model.builder;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.model.pipeline.LightUtil;

public class QuadPointDumper {

    private Vector3f[] points = new Vector3f[4];
    private int currentPoint = 0;
    private VertexFormat formatTo;

    public QuadPointDumper(BakedQuad quad) {
        float[] data = new float[4];
        VertexFormat formatFrom = DefaultVertexFormats.BLOCK;
        this.formatTo = DefaultVertexFormats.BLOCK;
        int countFrom = formatFrom.getElements().size();
        int countTo = this.formatTo.getElements().size();
        int[] eMap = LightUtil.mapFormats(formatFrom, this.formatTo);
        for (int v = 0; v < 4; v++) {
            for (int e = 0; e < countFrom; e++) {
                if (eMap[e] != countTo) {
                    this.unpack(quad.getVertexData(), data, this.formatTo, v, eMap[e]);
                    this.put(e, data);
                } else {
                    this.put(e);
                }
            }
        }
    }

    private void unpack(int[] from, float[] to, VertexFormat formatFrom, int v, int e) {
        int length = Math.min(4, to.length);
        VertexFormatElement element = formatFrom.getElements().get(e);
        int vertexStart = v * formatFrom.getSize() + formatFrom.getOffset(e);
        int count = element.getElementCount();
        VertexFormatElement.Type type = element.getType();
        int size = type.getSize();
        int mask = (256 << (8 * (size - 1))) - 1;
        for (int i = 0; i < length; i++) {
            if (i < count) {
                int pos = vertexStart + size * i;
                int index = pos >> 2;
                int offset = pos & 3;
                int bits = from[index];
                bits = bits >>> (offset * 8);
                if ((pos + size - 1) / 4 != index) {
                    bits |= from[index + 1] << ((4 - offset) * 8);
                }
                bits &= mask;
                if (type == VertexFormatElement.Type.FLOAT) {
                    to[i] = Float.intBitsToFloat(bits);
                } else if (type == VertexFormatElement.Type.UBYTE || type == VertexFormatElement.Type.USHORT) {
                    to[i] = (float) bits / mask;
                } else if (type == VertexFormatElement.Type.UINT) {
                    to[i] = (float) ((double) (bits & 0xFFFFFFFFL) / 0xFFFFFFFFL);
                } else if (type == VertexFormatElement.Type.BYTE) {
                    to[i] = ((float) (byte) bits) / (mask >> 1);
                } else if (type == VertexFormatElement.Type.SHORT) {
                    to[i] = ((float) (short) bits) / (mask >> 1);
                } else if (type == VertexFormatElement.Type.INT) {
                    to[i] = (float) ((double) (bits & 0xFFFFFFFFL) / (0xFFFFFFFFL >> 1));
                }
            } else {
                to[i] = 0;
            }
        }
    }

    public void put(int element, float... data) {
        VertexFormatElement elementType = this.formatTo.getElements().get(element);
        if (elementType != DefaultVertexFormats.POSITION_3F)
            return;

        if (this.currentPoint == 4) {
            return;
        }

        this.points[this.currentPoint] = new Vector3f(data[0], data[1], data[2]);
        this.currentPoint++;
    }

    public Vector3f[] getPoints() {
        return this.points;
    }
}