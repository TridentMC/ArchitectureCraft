package com.tridevmc.architecture.common.shape.behaviour;

import com.tridevmc.architecture.client.render.model.OBJSON;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.helpers.Trans3;
import com.tridevmc.architecture.common.tile.TileShape;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

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
    protected VoxelShape getCollisionBox(TileShape te, Level world, BlockPos pos, BlockState state, Entity entity, Trans3 t) {
        return this.getOBJSONModel().getVoxelized();
    }

    @Override
    public double placementOffsetX() {
        VoxelShape shape = this.getOBJSONModel().getShape(Trans3.ident, Shapes.empty());
        AABB bounds = shape.bounds();
        return 0.5 * (1 - (bounds.maxX - bounds.minX));
    }
}
