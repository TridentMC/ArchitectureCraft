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
import com.elytradev.concrete.network.Message;
import com.elytradev.concrete.network.NetworkContext;
import com.elytradev.concrete.network.annotation.field.MarshalledAs;
import com.elytradev.concrete.network.annotation.type.ReceivedOn;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.DimensionManager;

@ReceivedOn(Dist.SERVER)
public class SelectShapeMessage extends Message {

    public BlockPos sawPos;
    @MarshalledAs("int")
    public int page, slot, dim;

    public SelectShapeMessage(TileSawbench sawbench, int page, int slot) {
        super(ArchitectureNetworking.NETWORK);
        this.sawPos = sawbench.getPos();
        this.dim = sawbench.getWorld().getDimension().getId();
        this.page = page;
        this.slot = slot;
    }

    public SelectShapeMessage(NetworkContext ctx) {
        super(ctx);
    }

    @Override
    protected void handle(EntityPlayer player) {
        World world = DimensionManager.getWorld(dim);
        if (world.isBlockLoaded(sawPos)) {
            TileEntity tile = world.getTileEntity(sawPos);
            if (tile instanceof TileSawbench) {
                TileSawbench sawbench = (TileSawbench) tile;
                sawbench.setSelectedShape(page, slot);
            }
        }
    }
}
