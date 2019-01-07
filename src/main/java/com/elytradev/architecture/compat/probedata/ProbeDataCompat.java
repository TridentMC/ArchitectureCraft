/*
 * MIT License
 *
 * Copyright (c) 2017 Benjamin K
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.elytradev.architecture.compat.probedata;

import com.elytradev.architecture.common.ArchitectureMod;
import com.elytradev.architecture.common.tile.TileShape;
import com.elytradev.probe.api.IProbeData;
import com.elytradev.probe.api.IProbeDataProvider;
import com.elytradev.probe.api.impl.ProbeData;
import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.OptionalCapabilityInstance;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ProbeDataCompat {
    public static Capability<?> PROBE_CAPABILITY;

    @CapabilityInject(IProbeDataProvider.class)
    public static void onProbeDataInjected(Capability<?> capability) {
        PROBE_CAPABILITY = capability;
        MinecraftForge.EVENT_BUS.register(ProbeDataCompat.class);
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<TileEntity> e) {
        if (PROBE_CAPABILITY != null && e.getObject() instanceof TileShape) {
            e.addCapability(new ResourceLocation(ArchitectureMod.MOD_ID, "ShapeProbe"),
                    new ICapabilityProvider() {
                        TileShape shape = (TileShape) e.getObject();
                        IProbeDataProvider probeDataProvider = data -> {
                            genData(shape.baseBlockState,
                                    "tooltip.architecturecraft.basematerial",
                                    data);
                            genData(shape.secondaryBlockState,
                                    "tooltip.architecturecraft.secondarymaterial",
                                    data);
                        };

                        public void genData(IBlockState state, String key, List<IProbeData> data) {
                            if (state != null &&
                                    !(state.getBlock() instanceof BlockAir)) {
                                ProbeData probeData = new ProbeData();
                                probeData = probeData.withLabel(new TextComponentTranslation(
                                        key,
                                        state.getBlock().getNameTextComponent()));
                                Item item = Item.getItemFromBlock(state.getBlock());
                                if (item != null) {
                                    ItemStack stack = new ItemStack(item);
                                    //stack.setDamage(state.getBlock().getItemDropped(state)); TODO: Not sure what to do here now that item damage is gone. It's gonna take me a bit of time to adjust my thinking...
                                    probeData = probeData.withInventory(ImmutableList.of(stack));
                                }
                                data.add(probeData);
                            }
                        }

                        @Nonnull
                        public <T> OptionalCapabilityInstance<T> getCapability(@Nonnull final Capability<T> cap, final @Nullable EnumFacing side) {
                            if (cap == PROBE_CAPABILITY) {
                                return OptionalCapabilityInstance.of(() -> (T) probeDataProvider);
                            }
                            return OptionalCapabilityInstance.empty();
                        }
                    });
        }
    }
}
