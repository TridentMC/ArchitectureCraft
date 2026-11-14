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

package com.tridevmc.architecture.client.proxy;

import com.tridevmc.architecture.client.debug.ArchitectureDebugEventListeners;
import com.tridevmc.architecture.client.render.model.geometry.ArchitectureBlockModel;
import com.tridevmc.architecture.client.render.model.geometry.ArchitectureItemModel;
import com.tridevmc.architecture.client.render.model.impl.BakedModelSawbench;
import com.tridevmc.architecture.client.render.model.impl.ModelResolverSawbench;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.item.ItemCladding;
import com.tridevmc.architecture.common.item.ItemShape;
import com.tridevmc.architecture.common.proxy.CommonProxy;
import com.tridevmc.architecture.common.ui.ArchitectureUIHooks;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterBlockStateModels;
import net.neoforged.neoforge.client.event.RegisterItemModelsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void setup(FMLCommonSetupEvent e) {
        super.setup(e);
        NeoForge.EVENT_BUS.addListener(this::onItemTooltip);
        if (!FMLEnvironment.isProduction())
            NeoForge.EVENT_BUS.register(ArchitectureDebugEventListeners.class);
    }

    private void onItemTooltip(ItemTooltipEvent event) {
        var stack = event.getItemStack();
        var lines = event.getToolTip();
        if (lines.isEmpty()) {
            return;
        }
        if (stack.getItem() instanceof ItemShape
                && stack.get(ArchitectureMod.CONTENT.componentMaterial) != null) {
            var shape = ItemShape.getShapeFromStack(stack);
            if (shape != null) {
                lines.set(0, Component.translatable(shape.getLocalizationKey()));
            } else {
                lines.set(0, Component.literal(lines.getFirst().getString() + " (" + -1 + ")"));
            }
        } else if (stack.getItem() instanceof ItemCladding) {
            lines.set(0, stack.getComponents().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY));
        }
    }

    @SubscribeEvent
    public void onBlockModels(RegisterBlockStateModels event) {
        event.registerModel(Identifier.fromNamespaceAndPath(ArchitectureMod.MOD_ID, "mesh"),
                ArchitectureBlockModel.CODEC);
    }

    @SubscribeEvent
    public void onItemModels(RegisterItemModelsEvent event) {
        event.register(Identifier.fromNamespaceAndPath(ArchitectureMod.MOD_ID, "mesh"),
                ArchitectureItemModel.CODEC);
    }

    @SubscribeEvent
    public void onModelsBaked(ModelEvent.BakingCompleted event) {
        ModelResolverSawbench.clearCache();
    }

    @SubscribeEvent
    public void onMenuScreenRegisterEvent(RegisterMenuScreensEvent e) {
        ArchitectureUIHooks.register(e);
    }

    @Override
    public void registerHandlers() {
        super.registerHandlers();
    }

}
