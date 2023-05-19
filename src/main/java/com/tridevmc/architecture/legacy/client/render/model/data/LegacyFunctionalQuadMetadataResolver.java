package com.tridevmc.architecture.legacy.client.render.model.data;

import com.tridevmc.architecture.client.render.model.resolver.HashedQuadMetadataTextureResolver;
import com.tridevmc.architecture.client.render.model.resolver.HashedQuadMetadataTintIndexResolver;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataTintIndexResolver;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataTextureResolver;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.util.Objects;

/**
 * An implementation of {@link IQuadMetadataResolver} that resolves the texture and tint index for a quad using two functional interfaces.
 * <p>
 * Please note that by default this class attempts to generate a hash code based on the texture and tint index resolvers.
 * <p>
 * If you are creating lambda expressions on the fly then this means that the hash code will be different every time,
 * in which case it would be better to implement your own {@link IQuadMetadataResolver}
 * or use {@link HashedQuadMetadataTextureResolver} and {@link HashedQuadMetadataTintIndexResolver} instead of the default functional interfaces.
 *
 * @param <T> The type of the metadata object.
 */
@Deprecated
public class LegacyFunctionalQuadMetadataResolver<T> implements IQuadMetadataResolver<T> {

    private final IQuadMetadataTextureResolver<T> textureResolver;
    private final IQuadMetadataTintIndexResolver<T> colourResolver;

    private LegacyFunctionalQuadMetadataResolver(IQuadMetadataTextureResolver<T> textureResolver, IQuadMetadataTintIndexResolver<T> colourResolver) {
        this.textureResolver = textureResolver;
        this.colourResolver = colourResolver;
    }

    /**
     * Creates a new instance of {@link LegacyFunctionalQuadMetadataResolver} using the given texture and colour resolvers.
     *
     * @param textureResolver The texture resolver.
     * @param colourResolver  The colour resolver.
     * @param <T>             The type of the metadata object.
     * @return The new instance.
     */
    public static <T> LegacyFunctionalQuadMetadataResolver<T> of(IQuadMetadataTextureResolver<T> textureResolver, IQuadMetadataTintIndexResolver<T> colourResolver) {
        return new LegacyFunctionalQuadMetadataResolver<>(textureResolver, colourResolver);
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
        LegacyFunctionalQuadMetadataResolver<?> that = (LegacyFunctionalQuadMetadataResolver<?>) o;
        return Objects.equals(this.textureResolver, that.textureResolver) && Objects.equals(this.colourResolver, that.colourResolver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.textureResolver, this.colourResolver);
    }

}
