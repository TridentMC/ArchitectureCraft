package com.tridevmc.architecture.common.shape.behaviour;

import com.tridevmc.architecture.client.render.model.OBJSON;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.helpers.Utils;
import com.tridevmc.architecture.common.tile.TileShape;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import java.util.ArrayList;
import java.util.List;

public class ShapeBehaviourModel extends ShapeBehaviour {

    protected String modelName;
    private OBJSON model;

    public ShapeBehaviourModel(String name){
        this(name, null);
    }

    public ShapeBehaviourModel(String name, Object[] profiles) {
        this.modelName = "shape/" + name + ".objson";
        this.profiles = profiles;
    }

    @Override
    public boolean secondaryDefaultsToBase() {
        return true;
    }

    @Override
    public AxisAlignedBB getBounds(TileShape te, IBlockReader world, BlockPos pos, BlockState state,
                                   Entity entity, Trans3 t) {
        return t.t(this.getOBJSONModel().getBounds());
    }

    protected OBJSON getOBJSONModel() {
        if (this.model == null)
            this.model = ArchitectureMod.PROXY.getCachedOBJSON(this.modelName);
        return this.model;
    }

    @Override
    public boolean acceptsCladding() {
        OBJSON model = (OBJSON) this.getOBJSONModel();
        for (OBJSON.Face face : model.faces)
            if (face.texture >= 2)
                return true;
        return false;
    }

    @Override
    public void addCollisionBoxesToList(TileShape te, IBlockReader world, BlockPos pos, BlockState state,
                                        Entity entity, Trans3 t, List list) {
        if (te.shape.occlusionMask == 0)
            this.getOBJSONModel().addBoxesToList(t, list);
        else
            super.addCollisionBoxesToList(te, world, pos, state, entity, t, list);
    }

    @Override
    public double placementOffsetX() {
        List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
        this.getOBJSONModel().addBoxesToList(Trans3.ident, list);
        AxisAlignedBB bounds = Utils.unionOfBoxes(list);
        return 0.5 * (1 - (bounds.maxX - bounds.minX));
    }
}
