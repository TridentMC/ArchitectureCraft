package com.tridevmc.architecture.common.shape.behaviour;

import com.tridevmc.architecture.client.render.model.OBJSON;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.tile.TileShape;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;

public class ShapeBehaviourModel extends ShapeBehaviour {

    protected String modelName;
    private OBJSON model;

    public ShapeBehaviourModel(String name) {
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

    protected OBJSON getOBJSONModel() {
        if (this.model == null)
            this.model = ArchitectureMod.PROXY.getCachedOBJSON(this.modelName);
        return this.model;
    }

    public String getModelName() {
        return this.modelName;
    }

    @Override
    public boolean acceptsCladding() {
        OBJSON model = this.getOBJSONModel();
        for (OBJSON.Face face : model.getFaces())
            if (face.getTexture() >= 2)
                return true;
        return false;
    }

    @Override
    protected VoxelShape getCollisionBox(TileShape te, IBlockReader world, BlockPos pos, BlockState state, Entity entity, Trans3 t) {
        return this.getOBJSONModel().getVoxelized();
    }

    @Override
    public double placementOffsetX() {
        VoxelShape shape = this.getOBJSONModel().getShape(Trans3.ident, VoxelShapes.empty());
        AxisAlignedBB bounds = shape.getBoundingBox();
        return 0.5 * (1 - (bounds.maxX - bounds.minX));
    }
}
