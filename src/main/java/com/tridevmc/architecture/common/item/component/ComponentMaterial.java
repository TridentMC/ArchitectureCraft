package com.tridevmc.architecture.common.item.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a set of materials for an item, consisting of a base and secondary block state.
 *
 * @param base      The base block state.
 * @param secondary The secondary block state.
 */
public record ComponentMaterial(@NotNull BlockState base, @NotNull BlockState secondary) {
    public static final Codec<ComponentMaterial> CODEC = RecordCodecBuilder.create((instance) -> instance.group(
            BlockState.CODEC.fieldOf("base").forGetter(ComponentMaterial::base),
            BlockState.CODEC.fieldOf("secondary").forGetter(ComponentMaterial::secondary)
    ).apply(instance, ComponentMaterial::new));

    public static final StreamCodec<ByteBuf, ComponentMaterial> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(BlockState.CODEC), ComponentMaterial::base,
            ByteBufCodecs.fromCodec(BlockState.CODEC), ComponentMaterial::secondary,
            ComponentMaterial::new
    );
    public static final ComponentMaterial DEFAULT = new ComponentMaterial(Blocks.OAK_PLANKS.defaultBlockState(), Blocks.AIR.defaultBlockState());

    public ComponentMaterial(BlockState base) {
        this(base, Blocks.AIR.defaultBlockState());
    }

    public boolean hasBase() {
        return !this.base().isAir();
    }

    public boolean hasSecondary() {
        return !this.secondary().isAir();
    }

    public BlockState safeBase() {
        return this.hasBase() ? this.base() : Blocks.OAK_PLANKS.defaultBlockState();
    }

    public BlockState safeSecondary() {
        return this.hasSecondary() ? this.secondary() : this.safeBase();
    }
}
