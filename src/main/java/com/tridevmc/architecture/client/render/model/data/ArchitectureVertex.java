package com.tridevmc.architecture.client.render.model.data;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.mojang.math.Transformation;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;


import java.util.Arrays;
import java.util.Optional;

/**
 * Stores information about a vertex that can be piped into a vertex consumer, assumes all data provided is valid.
 */
public class ArchitectureVertex {

    private final int face;
    private final float[] data;
    private final float[] uvs;
    private float[] normals;

    public ArchitectureVertex(int face, float[] data, float[] uvs, float[] normals) {
        this.face = face;
        this.data = data;
        this.uvs = uvs;
        this.normals = normals;
    }

    public boolean assignNormals() {
        return false;
    }

    public void setNormals(Vector3f normals) {
        this.normals = new float[]{normals.x(), normals.y(), normals.z()};
    }

    public Vector3f getNormals() {
        return new Vector3f(this.normals);
    }

    public Vector3f getPosition() {
        return new Vector3f(this.data);
    }

    public Vector3f getPosition(Transformation transform) {
        Vector3f position = this.getPosition();
        Vector4f transformedPosition = new Vector4f(position.x(), position.y(), position.z(), 1);
        transform.transformPosition(transformedPosition);
        return new Vector3f(transformedPosition.x(), transformedPosition.y(), transformedPosition.z());
    }

    protected Direction rotate(Direction direction, Transformation transform) {
        Vec3i dir = direction == null ? new Vec3i(0, 0, 0) : direction.getNormal();
        Vector4f vec = new Vector4f(dir.getX(), dir.getY(), dir.getZ(), 0);
        transform.transformPosition(vec);
        return Direction.getNearest(vec.x(), vec.y(), vec.z());
    }

    public float[] getUVs(IBakedQuadProvider quadProvider, Transformation transform) {
        return this.uvs;
    }

    public float getX() {
        return this.data[0];
    }

    public float getY() {
        return this.data[1];
    }

    public float getZ() {
        return this.data[2];
    }

    public float getNormalX() {
        return this.getNormals().x();
    }

    public float getNormalY() {
        return this.getNormals().y();
    }

    public float getNormalZ() {
        return this.getNormals().z();
    }

    public void pipe(VertexConsumer consumer, IBakedQuadProvider bakedQuadProvider, TextureAtlasSprite sprite, Optional<Transformation> transform) {
        Vector4f pos = new Vector4f(this.getPosition());
        Vector3f normals = this.getNormals();
        float[] uvs = this.getUVs(bakedQuadProvider, transform.orElse(Transformation.identity()));
        transform.ifPresent((t) -> {
            t.transformPosition(pos);
            t.transformNormal(normals);
        });
        VertexFormat format = consumer.getVertexFormat();
        ImmutableList<VertexFormatElement> elements = format.getElements();
        for (int eI = 0; eI < elements.size(); eI++) {
            VertexFormatElement element = elements.get(eI);
            switch (element.getUsage()) {
                case POSITION:
                    consumer.put(eI, pos.getX(), pos.getY(), pos.getZ(), pos.getW());
                    break;
                case NORMAL:
                    consumer.put(eI, normals.getX(), normals.getY(), normals.getZ(), 0F);
                    break;
                case COLOR:
                    consumer.put(eI, 1F, 1F, 1F, 1F);
                    break;
                case UV:
                    if (element.getIndex() == 0) {
                        float u = sprite.getInterpolatedU(uvs[0]), v = sprite.getInterpolatedV(uvs[1]);
                        consumer.put(eI, u, v);
                    } else if (element.getIndex() == 2) {
                        consumer.put(eI, 0, 0, 1, 1);
                    } else {
                        consumer.put(eI);
                    }
                    break;
                default:
                    consumer.put(eI);
                    break;
            }
        }
    }

    public int getFace() {
        return this.face;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ArchitectureVertex) {
            return Arrays.equals(((ArchitectureVertex) obj).data, this.data);
        }

        return super.equals(obj);
    }
}
