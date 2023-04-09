package com.tridevmc.architecture.legacy.client.render.model.data;

import com.tridevmc.architecture.client.render.model.data.IQuadMetadataTintIndexResolver;
import com.tridevmc.architecture.client.render.model.data.IQuadMetadataResolver;
import com.tridevmc.architecture.client.render.model.data.IQuadMetadataTextureResolver;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.Objects;

/**
 * An implementation of {@link IQuadMetadataResolver} that resolves the texture and tint index for a quad using two functional interfaces.
 * <p>
 * Please note that by default this class attempts to generate a hash code based on the texture and tint index resolvers.
 * <p>
 * If you are creating lambda expressions on the fly then this means that the hash code will be different every time,
 * in which case it would be better to implement your own {@link IQuadMetadataResolver}
 * or use {@link com.tridevmc.architecture.client.render.model.data.HashedQuadMetadataTextureResolver} and {@link com.tridevmc.architecture.client.render.model.data.HashedQuadMetadataTintIndexResolver} instead of the default functional interfaces.
 *
 * @param <T> The type of the metadata object.
 */
public class FunctionalQuadMetadataResolver<T> implements IQuadMetadataResolver<T> {

    private final IQuadMetadataTextureResolver<T> textureResolver;
    private final IQuadMetadataTintIndexResolver<T> colourResolver;

    private FunctionalQuadMetadataResolver(IQuadMetadataTextureResolver<T> textureResolver, IQuadMetadataTintIndexResolver<T> colourResolver) {
        this.textureResolver = textureResolver;
        this.colourResolver = colourResolver;
    }

    /**
     * Creates a new instance of {@link FunctionalQuadMetadataResolver} using the given texture and colour resolvers.
     *
     * @param textureResolver The texture resolver.
     * @param colourResolver  The colour resolver.
     * @param <T>             The type of the metadata object.
     * @return The new instance.
     */
    public static <T> FunctionalQuadMetadataResolver<T> of(IQuadMetadataTextureResolver<T> textureResolver, IQuadMetadataTintIndexResolver<T> colourResolver) {
        return new FunctionalQuadMetadataResolver<>(textureResolver, colourResolver);
    }

    @Override
    public TextureAtlasSprite getTexture(T metadata) {
        return this.textureResolver.getTexture(metadata);
    }

    @Override
    public int getTintIndex(T metadata) {
        return this.colourResolver.getTintIndex(metadata);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        FunctionalQuadMetadataResolver<?> that = (FunctionalQuadMetadataResolver<?>) o;
        return Objects.equals(this.textureResolver, that.textureResolver) && Objects.equals(this.colourResolver, that.colourResolver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.textureResolver, this.colourResolver);
    }

}
