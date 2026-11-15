package com.tridevmc.architecture.client.render.model.resolver.functional;

import com.tridevmc.architecture.client.render.model.resolver.IQuadMetadataResolver;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An implementation of {@link IQuadMetadataResolver} that resolves the material and tint index for a quad using two functional interfaces.
 *
 * @param <T> The type of the metadata object.
 */
public class FunctionalQuadMetadataResolver<T> implements IQuadMetadataResolver<T> {

    private final IQuadMetadataMaterialResolver<T> materialResolver;
    private final IQuadMetadataMaterialResolverWithBlockContext<T> blockMaterialResolver;
    private final IQuadMetadataMaterialResolverWithItemStackContext<T> itemMaterialResolver;

    private final IQuadMetadataTintIndexResolver<T> tintIndexResolver;
    private final IQuadMetadataTintIndexResolverWithBlockContext<T> blockTintIndexResolver;
    private final IQuadMetadataTintIndexResolverWithItemStackContext<T> itemTintIndexResolver;

    private FunctionalQuadMetadataResolver(IQuadMetadataMaterialResolver<T> materialResolver, IQuadMetadataMaterialResolverWithBlockContext<T> blockMaterialResolver, IQuadMetadataMaterialResolverWithItemStackContext<T> itemMaterialResolver, IQuadMetadataTintIndexResolver<T> tintIndexResolver, IQuadMetadataTintIndexResolverWithBlockContext<T> blockTintIndexResolver, IQuadMetadataTintIndexResolverWithItemStackContext<T> itemTintIndexResolver) {
        this.materialResolver = materialResolver;
        this.blockMaterialResolver = blockMaterialResolver;
        this.itemMaterialResolver = itemMaterialResolver;
        this.tintIndexResolver = tintIndexResolver;
        this.blockTintIndexResolver = blockTintIndexResolver;
        this.itemTintIndexResolver = itemTintIndexResolver;
    }

    /**
     * Creates a new {@link FunctionalQuadMetadataResolver} using the given functional interfaces, with the material resolver being used for both block and item contexts.
     *
     * @param materialResolver   The material resolver to use for both block and item contexts.
     * @param tintIndexResolver The tint index resolver to use for both block and item contexts.
     * @param <T>               The type of the metadata object.
     * @return A new {@link FunctionalQuadMetadataResolver} using the given functional interfaces.
     */
    public static <T> IQuadMetadataResolver<T> of(IQuadMetadataMaterialResolver<T> materialResolver, IQuadMetadataTintIndexResolver<T> tintIndexResolver) {
        return new FunctionalQuadMetadataResolver<>(
                materialResolver,
                (level, pos, state, metadata) -> materialResolver.getMaterial(metadata),
                (stack, metadata) -> materialResolver.getMaterial(metadata),
                tintIndexResolver,
                (level, pos, state, metadata) -> tintIndexResolver.getTintIndex(metadata),
                (stack, metadata) -> tintIndexResolver.getTintIndex(metadata)
        );
    }

    /**
     * Creates a new {@link FunctionalQuadMetadataResolver} using the given functional interfaces, with the material resolver being used as a fallback for the ItemStack context.
     *
     * @param materialResolver        The fallback material resolver to use for the ItemStack context.
     * @param tintIndexResolver      The fallback tint index resolver to use for the ItemStack context.
     * @param blockMaterialResolver   The material resolver to use for the block context.
     * @param blockTintIndexResolver The tint index resolver to use for the block context.
     * @param <T>                    The type of the metadata object.
     * @return A new {@link FunctionalQuadMetadataResolver} using the given functional interfaces.
     */
    public static <T> IQuadMetadataResolver<T> of(IQuadMetadataMaterialResolver<T> materialResolver, IQuadMetadataTintIndexResolver<T> tintIndexResolver,
                                                  IQuadMetadataMaterialResolverWithBlockContext<T> blockMaterialResolver, IQuadMetadataTintIndexResolverWithBlockContext<T> blockTintIndexResolver) {
        return new FunctionalQuadMetadataResolver<>(
                materialResolver,
                blockMaterialResolver,
                (stack, metadata) -> materialResolver.getMaterial(metadata),
                tintIndexResolver,
                blockTintIndexResolver,
                (stack, metadata) -> tintIndexResolver.getTintIndex(metadata)
        );
    }

    /**
     * Creates a new {@link FunctionalQuadMetadataResolver} using the given functional interfaces, with the material resolver being used as a fallback for the block context.
     *
     * @param materialResolver       The fallback material resolver to use for the block context.
     * @param tintIndexResolver     The fallback tint index resolver to use for the block context.
     * @param itemMaterialResolver   The material resolver to use for the ItemStack context.
     * @param itemTintIndexResolver The tint index resolver to use for the ItemStack context.
     * @param <T>                   The type of the metadata object.
     * @return A new {@link FunctionalQuadMetadataResolver} using the given functional interfaces.
     */
    public static <T> IQuadMetadataResolver<T> of(IQuadMetadataMaterialResolver<T> materialResolver, IQuadMetadataTintIndexResolver<T> tintIndexResolver,
                                                  IQuadMetadataMaterialResolverWithItemStackContext<T> itemMaterialResolver, IQuadMetadataTintIndexResolverWithItemStackContext<T> itemTintIndexResolver) {
        return new FunctionalQuadMetadataResolver<>(
                materialResolver,
                (level, pos, state, metadata) -> materialResolver.getMaterial(metadata),
                itemMaterialResolver,
                tintIndexResolver,
                (level, pos, state, metadata) -> tintIndexResolver.getTintIndex(metadata),
                itemTintIndexResolver
        );
    }

    /**
     * Creates a new {@link FunctionalQuadMetadataResolver} using the given functional interfaces.
     *
     * @param materialResolver        The material resolver to use as a fallback when no context is available.
     * @param tintIndexResolver      The tint index resolver to use as a fallback when no context is available.
     * @param blockMaterialResolver   The material resolver to use for the block context.
     * @param blockTintIndexResolver The tint index resolver to use for the block context.
     * @param itemMaterialResolver    The material resolver to use for the ItemStack context.
     * @param itemTintIndexResolver  The tint index resolver to use for the ItemStack context.
     * @param <T>                    The type of the metadata object.
     * @return A new {@link FunctionalQuadMetadataResolver} using the given functional interfaces.
     */
    public static <T> IQuadMetadataResolver<T> of(IQuadMetadataMaterialResolver<T> materialResolver, IQuadMetadataTintIndexResolver<T> tintIndexResolver,
                                                  IQuadMetadataMaterialResolverWithBlockContext<T> blockMaterialResolver, IQuadMetadataTintIndexResolverWithBlockContext<T> blockTintIndexResolver,
                                                  IQuadMetadataMaterialResolverWithItemStackContext<T> itemMaterialResolver, IQuadMetadataTintIndexResolverWithItemStackContext<T> itemTintIndexResolver) {
        return new FunctionalQuadMetadataResolver<>(
                materialResolver,
                blockMaterialResolver,
                itemMaterialResolver,
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
    public Material.Baked getMaterial(BlockGetter level, BlockPos pos, BlockState state, T metadata) {
        return this.blockMaterialResolver.getMaterial(level, pos, state, metadata);
    }

    @Override
    public Material.Baked getMaterial(ItemStack stack, T metadata) {
        return this.itemMaterialResolver.getMaterial(stack, metadata);
    }

    @Override
    public Material.Baked getMaterial(T metadata) {
        return this.materialResolver.getMaterial(metadata);
    }

    @Override
    public int getTintIndex(BlockGetter level, BlockPos pos, BlockState state, T metadata) {
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
        private IQuadMetadataMaterialResolver<T> materialResolver;
        private IQuadMetadataMaterialResolverWithBlockContext<T> blockMaterialResolver;
        private IQuadMetadataMaterialResolverWithItemStackContext<T> itemMaterialResolver;
        private IQuadMetadataTintIndexResolver<T> tintIndexResolver;
        private IQuadMetadataTintIndexResolverWithBlockContext<T> blockTintIndexResolver;
        private IQuadMetadataTintIndexResolverWithItemStackContext<T> itemTintIndexResolver;

        private Builder() {
        }

        /**
         * Assigns the given material resolver to the builder.
         *
         * @param materialResolver The material resolver to assign.
         * @return The builder.
         */
        public Builder<T> materialResolver(IQuadMetadataMaterialResolver<T> materialResolver) {
            this.materialResolver = materialResolver;
            return this;
        }

        /**
         * Assigns the given block material resolver to the builder.
         *
         * @param blockMaterialResolver The block material resolver to assign.
         * @return The builder.
         */
        public Builder<T> blockMaterialResolver(IQuadMetadataMaterialResolverWithBlockContext<T> blockMaterialResolver) {
            this.blockMaterialResolver = blockMaterialResolver;
            return this;
        }

        /**
         * Assigns the given item material resolver to the builder.
         *
         * @param itemMaterialResolver The item material resolver to assign.
         * @return The builder.
         */
        public Builder<T> itemMaterialResolver(IQuadMetadataMaterialResolverWithItemStackContext<T> itemMaterialResolver) {
            this.itemMaterialResolver = itemMaterialResolver;
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
            if (this.materialResolver == null) {
                throw new IllegalStateException("materialResolver must not be null");
            }
            if (this.tintIndexResolver == null) {
                throw new IllegalStateException("tintIndexResolver must not be null");
            }

            if (this.blockMaterialResolver == null) {
                this.blockMaterialResolver = (level, pos, state, metadata) -> this.materialResolver.getMaterial(metadata);
            }
            if (this.blockTintIndexResolver == null) {
                this.blockTintIndexResolver = (level, pos, state, metadata) -> this.tintIndexResolver.getTintIndex(metadata);
            }
            if (this.itemMaterialResolver == null) {
                this.itemMaterialResolver = (stack, metadata) -> this.materialResolver.getMaterial(metadata);
            }
            if (this.itemTintIndexResolver == null) {
                this.itemTintIndexResolver = (stack, metadata) -> this.tintIndexResolver.getTintIndex(metadata);
            }

            return new FunctionalQuadMetadataResolver<>(
                    this.materialResolver,
                    this.blockMaterialResolver,
                    this.itemMaterialResolver,
                    this.tintIndexResolver,
                    this.blockTintIndexResolver,
                    this.itemTintIndexResolver
            );
        }
    }
}
