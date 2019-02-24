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

package com.elytradev.architecture.common.network;

import com.elytradev.architecture.common.tile.TileSawbench;
import com.tridevmc.compound.network.marshallers.SetMarshaller;
import com.tridevmc.compound.network.message.Message;
import com.tridevmc.compound.network.message.RegisteredMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@RegisteredMessage(networkChannel = "architecturecraft:network", destination = Dist.DEDICATED_SERVER)
public class SelectShapeMessage extends Message {

    public BlockPos sawPos;
    @SetMarshaller(marshallerId = "int")
    public int page, slot, dim;

    public SelectShapeMessage(TileSawbench sawbench, int page, int slot) {
        super();
        this.sawPos = sawbench.getPos();
        this.dim = sawbench.getWorld().getDimension().getType().getId();
        this.page = page;
        this.slot = slot;
    }

    @Override
    public void handle(EntityPlayer player) {
        World world = DimensionManager.getWorld(ServerLifecycleHooks.getCurrentServer(), DimensionType.getById(dim), false, false);
        if (world == null)
            return;
        if (world.isBlockLoaded(sawPos)) {
            TileEntity tile = world.getTileEntity(sawPos);
            if (tile instanceof TileSawbench) {
                TileSawbench sawbench = (TileSawbench) tile;
                sawbench.setSelectedShape(page, slot);
            }
        }
    }
}
