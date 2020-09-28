package com.tridevmc.architecture.client.render.model.builder;

import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraftforge.client.model.pipeline.BakedQuadBuilder;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Small utility class for building quads, makes things less painful.
 */
public class QuadBuilder {

    private BakedQuadBuilder builder;
    private VertexFormat format;
    private Optional<TransformationMatrix> transform;

    public QuadBuilder(@Nullable TransformationMatrix transform, Direction side) {
        this.format = DefaultVertexFormats.BLOCK;
        this.builder = new BakedQuadBuilder();
        this.transform = Optional.of(transform);
        this.builder.setQuadOrientation(side);
        this.builder.setApplyDiffuseLighting(true);
        this.builder.setContractUVs(true);
    }

    public void putVertex(float x, float y, float z, float u, float v, float normalX, float normalY, float normalZ) {
        Vector4f vec = new Vector4f();
        Vector3f normals = new Vector3f(normalX, normalY, normalZ);
        for (int e = 0; e < this.format.getElements().size(); e++) {
            switch (this.format.getElements().get(e).getUsage()) {
                case POSITION:
                    if (this.transform.isPresent()) {
                        vec.setX(x);
                        vec.setY(y);
                        vec.setZ(z);
                        vec.setW(1);
                        this.transform.get().transformPosition(vec);
                        this.builder.put(e, vec.getX(), vec.getY(), vec.getZ(), vec.getW());
                    } else {
                        this.builder.put(e, x, y, z, 1);
                    }
                    break;
                case COLOR:
                    this.builder.put(e, 1f, 1f, 1f, 1f);
                    break;
                case UV:
                    if (this.format.getElements().get(e).getIndex() == 0) {
                        this.builder.put(e, u, v, 0f, 1f);
                        break;
                    }
                case NORMAL:
                    if (this.transform.isPresent()) {
                        this.transform.get().transformNormal(normals);
                        this.builder.put(e, normals.getX(), normals.getY(), normals.getZ(), 0F);
                    } else {
                        this.builder.put(e, normalX, normalY, normalZ, 0F);
                    }
                    break;
                default:
                    this.builder.put(e);
                    break;
            }
        }
    }

    public BakedQuad build(TextureAtlasSprite sprite, int tintIndex) {
        this.builder.setTexture(sprite);
        this.builder.setQuadTint(tintIndex);
        return this.builder.build();
    }

}
