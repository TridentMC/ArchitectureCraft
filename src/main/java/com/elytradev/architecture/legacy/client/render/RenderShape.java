//-----------------------------------------------------------------
//
//   ArchitectureCraft - Base class for special shape renderers
//
//-----------------------------------------------------------------

package com.elytradev.architecture.legacy.client.render;

import com.elytradev.architecture.client.render.model.IModel;
import com.elytradev.architecture.client.render.target.RenderTargetBase;
import com.elytradev.architecture.client.render.texture.ITexture;
import com.elytradev.architecture.legacy.common.ArchitectureCraft;
import com.elytradev.architecture.legacy.common.helpers.Trans3;
import com.elytradev.architecture.common.tile.TileShape;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public abstract class RenderShape {

    protected IBlockAccess blockWorld;
    protected BlockPos blockPos;
    protected TileShape te;
    protected ITexture[] textures;
    protected Trans3 t;
    protected RenderTargetBase target;

    public RenderShape(TileShape te, ITexture[] textures, Trans3 t, RenderTargetBase target) {
        this.te = te;
        this.blockWorld = te.getWorld();
        this.blockPos = te.getPos();
        this.textures = textures;
        this.t = t;
        this.target = target;
    }

    protected abstract void render();

    protected IModel getModel(String name) {
        return ArchitectureCraft.mod.client.getModel(name);
    }

//	protected TileEntity getTileEntityInGlobalDir(EnumFacing gdir) {
//		if (blockWorld != null)
//			return blockWorld.getTileEntity(blockPos.offset(gdir));
//		else
//			return null;
//	}
//
//	protected ShapeTE getShapeTEInGlobalDir(EnumFacing gdir) {
//		TileEntity te = getTileEntityInGlobalDir(gdir);
//		if (te instanceof ShapeTE)
//			return (ShapeTE)te;
//		else
//			return null;
//	}

}
