package com.tridevmc.architecture.legacy.client.render.model.data;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import com.tridevmc.architecture.client.render.model.piped.IPipedBakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.Arrays;
import java.util.Optional;

/**
 * Stores information about a vertex that can be piped into a vertex consumer, assumes all data provided is valid.
 */
@Deprecated
public class LegacyArchitectureVertex {

    private final int face;
    private final float[] data;
    private final float[] uvs;
    private float[] normals;

    public LegacyArchitectureVertex(int face, float[] data, float[] uvs, float[] normals) {
        this.face = face;
        this.data = data;
        this.uvs = uvs;
        this.normals = normals;
    }

    public boolean assignNormals() {
        return false;
    }

    public Vector3f getNormals() {
        return new Vector3f(this.normals);
    }

    public void setNormals(Vector3f normals) {
        this.normals = new float[]{normals.x(), normals.y(), normals.z()};
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

    public float[] getUVs(IPipedBakedQuad quadProvider, Transformation transform) {
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

    public void pipe(VertexConsumer consumer, IPipedBakedQuad<?, ?, ?> bakedQuadProvider,
                     Optional<Transformation> transform, TextureAtlasSprite sprite,
                     int colour) {
        var p = this.getPosition();
        Vector4f pos = new Vector4f(p.x(), p.y(), p.z(), 1);
        Vector3f normals = this.getNormals();
        float[] uvs = this.getUVs(bakedQuadProvider, transform.orElse(Transformation.identity()));
        transform.ifPresent((t) -> {
            t.transformPosition(pos);
            t.transformNormal(normals);
        });
        // TODO: UV2 seems to be lighting related, need to look at this closer as lighting is the biggest blocker atm
        consumer.vertex(pos.x(), pos.y(), pos.z())
                .color(colour)
                .normal(normals.x(), normals.y(), normals.z())
                .uv(sprite.getU(uvs[0]), sprite.getV(uvs[1]))
                .uv2(1, 0)
                .overlayCoords(1, 0)
                .endVertex();
    }

    public int getFace() {
        return this.face;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LegacyArchitectureVertex) {
            return Arrays.equals(((LegacyArchitectureVertex) obj).data, this.data);
        }

        return super.equals(obj);
    }

}
