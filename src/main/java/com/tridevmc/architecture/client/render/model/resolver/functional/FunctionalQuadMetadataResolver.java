package com.tridevmc.architecture.client.render.model.resolver.functional;

import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An implementation of {@link IQuadMetadataResolver} that resolves the texture and tint index for a quad using two functional interfaces.
 *
 * @param <T> The type of the metadata object.
 */
public class FunctionalQuadMetadataResolver<T> implements IQuadMetadataResolver<T> {

    private final IQuadMetadataTextureResolver<T> textureResolver;
    private final IQuadMetadataTextureResolverWithBlockContext<T> blockTextureResolver;
    private final IQuadMetadataTextureResolverWithItemStackContext<T> itemTextureResolver;

    private final IQuadMetadataTintIndexResolver<T> tintIndexResolver;
    private final IQuadMetadataTintIndexResolverWithBlockContext<T> blockTintIndexResolver;
    private final IQuadMetadataTintIndexResolverWithItemStackContext<T> itemTintIndexResolver;

    private FunctionalQuadMetadataResolver(IQuadMetadataTextureResolver<T> textureResolver, IQuadMetadataTextureResolverWithBlockContext<T> blockTextureResolver, IQuadMetadataTextureResolverWithItemStackContext<T> itemTextureResolver, IQuadMetadataTintIndexResolver<T> tintIndexResolver, IQuadMetadataTintIndexResolverWithBlockContext<T> blockTintIndexResolver, IQuadMetadataTintIndexResolverWithItemStackContext<T> itemTintIndexResolver) {
        this.textureResolver = textureResolver;
        this.blockTextureResolver = blockTextureResolver;
        this.itemTextureResolver = itemTextureResolver;
        this.tintIndexResolver = tintIndexResolver;
        this.blockTintIndexResolver = blockTintIndexResolver;
        this.itemTintIndexResolver = itemTintIndexResolver;
    }

    /**
     * Creates a new {@link FunctionalQuadMetadataResolver} using the given functional interfaces, with the texture resolver being used for both block and item contexts.
     *
     * @param textureResolver   The texture resolver to use for both block and item contexts.
     * @param tintIndexResolver The tint index resolver to use for both block and item contexts.
     * @param <T>               The type of the metadata object.
     * @return A new {@link FunctionalQuadMetadataResolver} using the given functional interfaces.
     */
    public static <T> IQuadMetadataResolver<T> of(IQuadMetadataTextureResolver<T> textureResolver, IQuadMetadataTintIndexResolver<T> tintIndexResolver) {
        return new FunctionalQuadMetadataResolver<>(
                textureResolver,
                (level, pos, state, metadata) -> textureResolver.getTexture(metadata),
                (stack, metadata) -> textureResolver.getTexture(metadata),
                tintIndexResolver,
                (level, pos, state, metadata) -> tintIndexResolver.getTintIndex(metadata),
                (stack, metadata) -> tintIndexResolver.getTintIndex(metadata)
        );
    }

    /**
     * Creates a new {@link FunctionalQuadMetadataResolver} using the given functional interfaces, with the texture resolver being used as a fallback for the ItemStack context.
     *
     * @param textureResolver        The fallback texture resolver to use for the ItemStack context.
     * @param tintIndexResolver      The fallback tint index resolver to use for the ItemStack context.
     * @param blockTextureResolver   The texture resolver to use for the block context.
     * @param blockTintIndexResolver The tint index resolver to use for the block context.
     * @param <T>                    The type of the metadata object.
     * @return A new {@link FunctionalQuadMetadataResolver} using the given functional interfaces.
     */
    public static <T> IQuadMetadataResolver<T> of(IQuadMetadataTextureResolver<T> textureResolver, IQuadMetadataTintIndexResolver<T> tintIndexResolver,
                                                  IQuadMetadataTextureResolverWithBlockContext<T> blockTextureResolver, IQuadMetadataTintIndexResolverWithBlockContext<T> blockTintIndexResolver) {
        return new FunctionalQuadMetadataResolver<>(
                textureResolver,
                blockTextureResolver,
                (stack, metadata) -> textureResolver.getTexture(metadata),
                tintIndexResolver,
                blockTintIndexResolver,
                (stack, metadata) -> tintIndexResolver.getTintIndex(metadata)
        );
    }

    /**
     * Creates a new {@link FunctionalQuadMetadataResolver} using the given functional interfaces, with the texture resolver being used as a fallback for the block context.
     *
     * @param textureResolver       The fallback texture resolver to use for the block context.
     * @param tintIndexResolver     The fallback tint index resolver to use for the block context.
     * @param itemTextureResolver   The texture resolver to use for the ItemStack context.
     * @param itemTintIndexResolver The tint index resolver to use for the ItemStack context.
     * @param <T>                   The type of the metadata object.
     * @return A new {@link FunctionalQuadMetadataResolver} using the given functional interfaces.
     */
    public static <T> IQuadMetadataResolver<T> of(IQuadMetadataTextureResolver<T> textureResolver, IQuadMetadataTintIndexResolver<T> tintIndexResolver,
                                                  IQuadMetadataTextureResolverWithItemStackContext<T> itemTextureResolver, IQuadMetadataTintIndexResolverWithItemStackContext<T> itemTintIndexResolver) {
        return new FunctionalQuadMetadataResolver<>(
                textureResolver,
                (level, pos, state, metadata) -> textureResolver.getTexture(metadata),
                itemTextureResolver,
                tintIndexResolver,
                (level, pos, state, metadata) -> tintIndexResolver.getTintIndex(metadata),
                itemTintIndexResolver
        );
    }

    /**
     * Creates a new {@link FunctionalQuadMetadataResolver} using the given functional interfaces.
     *
     * @param textureResolver        The texture resolver to use as a fallback when no context is available.
     * @param tintIndexResolver      The tint index resolver to use as a fallback when no context is available.
     * @param blockTextureResolver   The texture resolver to use for the block context.
     * @param blockTintIndexResolver The tint index resolver to use for the block context.
     * @param itemTextureResolver    The texture resolver to use for the ItemStack context.
     * @param itemTintIndexResolver  The tint index resolver to use for the ItemStack context.
     * @param <T>                    The type of the metadata object.
     * @return A new {@link FunctionalQuadMetadataResolver} using the given functional interfaces.
     */
    public static <T> IQuadMetadataResolver<T> of(IQuadMetadataTextureResolver<T> textureResolver, IQuadMetadataTintIndexResolver<T> tintIndexResolver,
                                                  IQuadMetadataTextureResolverWithBlockContext<T> blockTextureResolver, IQuadMetadataTintIndexResolverWithBlockContext<T> blockTintIndexResolver,
                                                  IQuadMetadataTextureResolverWithItemStackContext<T> itemTextureResolver, IQuadMetadataTintIndexResolverWithItemStackContext<T> itemTintIndexResolver) {
        return new FunctionalQuadMetadataResolver<>(
                textureResolver,
                blockTextureResolver,
                itemTextureResolver,
                tintIndexResolver,
                blockTintIndexResolver,
                itemTintIndexResolver
        );
    }

    /**
     * Creates a new {@link Builder} for a {@link FunctionalQuadMetadataResolver}.
     *
     * @param <T> The type of the metadata object.
     * @return A new {@link Builder} for a {@link FunctionalQuadMetadataResolver}.
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    @Override
    public TextureAtlasSprite getTexture(LevelAccessor level, BlockPos pos, BlockState state, T metadata) {
        return this.blockTextureResolver.getTexture(level, pos, state, metadata);
    }

    @Override
    public TextureAtlasSprite getTexture(ItemStack stack, T metadata) {
        return this.itemTextureResolver.getTexture(stack, metadata);
    }

    @Override
    public TextureAtlasSprite getTexture(T metadata) {
        return this.textureResolver.getTexture(metadata);
    }

    @Override
    public int getTintIndex(LevelAccessor level, BlockPos pos, BlockState state, T metadata) {
        return this.blockTintIndexResolver.getTintIndex(level, pos, state, metadata);
    }

    @Override
    public int getTintIndex(ItemStack stack, T metadata) {
        return this.itemTintIndexResolver.getTintIndex(stack, metadata);
    }

    @Override
    public int getTintIndex(T metadata) {
        return this.tintIndexResolver.getTintIndex(metadata);
    }

    public static class Builder<T> {
        private IQuadMetadataTextureResolver<T> textureResolver;
        private IQuadMetadataTextureResolverWithBlockContext<T> blockTextureResolver;
        private IQuadMetadataTextureResolverWithItemStackContext<T> itemTextureResolver;
        private IQuadMetadataTintIndexResolver<T> tintIndexResolver;
        private IQuadMetadataTintIndexResolverWithBlockContext<T> blockTintIndexResolver;
        private IQuadMetadataTintIndexResolverWithItemStackContext<T> itemTintIndexResolver;

        private Builder() {
        }

        /**
         * Assigns the given texture resolver to the builder.
         *
         * @param textureResolver The texture resolver to assign.
         * @return The builder.
         */
        public Builder<T> textureResolver(IQuadMetadataTextureResolver<T> textureResolver) {
            this.textureResolver = textureResolver;
            return this;
        }

        /**
         * Assigns the given block texture resolver to the builder.
         *
         * @param blockTextureResolver The block texture resolver to assign.
         * @return The builder.
         */
        public Builder<T> blockTextureResolver(IQuadMetadataTextureResolverWithBlockContext<T> blockTextureResolver) {
            this.blockTextureResolver = blockTextureResolver;
            return this;
        }

        /**
         * Assigns the given item texture resolver to the builder.
         *
         * @param itemTextureResolver The item texture resolver to assign.
         * @return The builder.
         */
        public Builder<T> itemTextureResolver(IQuadMetadataTextureResolverWithItemStackContext<T> itemTextureResolver) {
            this.itemTextureResolver = itemTextureResolver;
            return this;
        }

        /**
         * Assigns the given tint index resolver to the builder.
         *
         * @param tintIndexResolver The tint index resolver to assign.
         * @return The builder.
         */
        public Builder<T> tintIndexResolver(IQuadMetadataTintIndexResolver<T> tintIndexResolver) {
            this.tintIndexResolver = tintIndexResolver;
            return this;
        }

        /**
         * Assigns the given block tint index resolver to the builder.
         *
         * @param blockTintIndexResolver The block tint index resolver to assign.
         * @return The builder.
         */
        public Builder<T> blockTintIndexResolver(IQuadMetadataTintIndexResolverWithBlockContext<T> blockTintIndexResolver) {
            this.blockTintIndexResolver = blockTintIndexResolver;
            return this;
        }

        /**
         * Assigns the given item tint index resolver to the builder.
         *
         * @param itemTintIndexResolver The item tint index resolver to assign.
         * @return The builder.
         */
        public Builder<T> itemTintIndexResolver(IQuadMetadataTintIndexResolverWithItemStackContext<T> itemTintIndexResolver) {
            this.itemTintIndexResolver = itemTintIndexResolver;
            return this;
        }

        /**
         * Builds a new {@link FunctionalQuadMetadataResolver} using the given functional interfaces.
         *
         * @return A new {@link FunctionalQuadMetadataResolver} using the given functional interfaces.
         * @throws IllegalStateException If any of the required functional interfaces are not set.
         */
        public FunctionalQuadMetadataResolver<T> build() {
            if (this.textureResolver == null) {
                throw new IllegalStateException("textureResolver must not be null");
            }
            if (this.tintIndexResolver == null) {
                throw new IllegalStateException("tintIndexResolver must not be null");
            }

            if (this.blockTextureResolver == null) {
                this.blockTextureResolver = (level, pos, state, metadata) -> this.textureResolver.getTexture(metadata);
            }
            if (this.blockTintIndexResolver == null) {
                this.blockTintIndexResolver = (level, pos, state, metadata) -> this.tintIndexResolver.getTintIndex(metadata);
            }
            if (this.itemTextureResolver == null) {
                this.itemTextureResolver = (stack, metadata) -> this.textureResolver.getTexture(metadata);
            }
            if (this.itemTintIndexResolver == null) {
                this.itemTintIndexResolver = (stack, metadata) -> this.tintIndexResolver.getTintIndex(metadata);
            }

            return new FunctionalQuadMetadataResolver<>(
                    this.textureResolver,
                    this.blockTextureResolver,
                    this.itemTextureResolver,
                    this.tintIndexResolver,
                    this.blockTintIndexResolver,
                    this.itemTintIndexResolver
            );
        }
    }
}
