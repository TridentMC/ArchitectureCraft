package com.tridevmc.architecture.common.shape.behaviour;

import com.tridevmc.architecture.client.render.model.objson.LegacyOBJSON;
import com.tridevmc.architecture.common.ArchitectureMod;
import com.tridevmc.architecture.common.block.entity.ShapeBlockEntity;
import com.tridevmc.architecture.core.math.Trans3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ShapeBehaviourModel extends ShapeBehaviour {

    protected String modelName;
    private LegacyOBJSON model;

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

    protected LegacyOBJSON getOBJSONModel() {
        if (this.model == null)
            this.model = ArchitectureMod.PROXY.getCachedOBJSON(this.modelName);
        return this.model;
    }

    public String getModelName() {
        return this.modelName;
    }

    @Override
    public boolean acceptsCladding() {
        LegacyOBJSON model = this.getOBJSONModel();
        for (LegacyOBJSON.Face face : model.getFaces())
            if (face.getTexture() >= 2)
                return true;
        return false;
    }

    @Override
    protected VoxelShape getCollisionBox(ShapeBlockEntity te, BlockGetter world, BlockPos pos, BlockState state, Entity entity, Trans3 t) {
        return t.t(this.getOBJSONModel().getVoxelized());
    }

    @Override
    public double placementOffsetX() {
        VoxelShape shape = this.getOBJSONModel().getShape(Trans3.ident, Shapes.empty());
        AABB bounds = shape.bounds();
        return 0.5 * (1 - (bounds.maxX - bounds.minX));
    }
}
