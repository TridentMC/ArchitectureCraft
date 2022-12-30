package com.tridevmc.architecture.core.model.objson;

import com.tridevmc.architecture.core.model.Voxelizer;
import com.tridevmc.architecture.core.physics.AABB;

import java.util.List;

public record OBJSON(OBJSONData data, OBJSONMesh mesh, Voxelizer voxelizer) {

 /*   public OBJSON(OBJSONData data, OBJSONMesh mesh, int blockResolution) {
        this(data, mesh, new Voxelizer(mesh, blockResolution));
    }

    public OBJSON(OBJSONData data, OBJSONMesh mesh) {
        this(data, mesh, 16);
    }

    public OBJSON(OBJSONData data, @NotNull Transform trans) {
        this(data, new OBJSONMesh(data).transform(trans));
    }

    public static OBJSON fromResource(ResourceLocation location) {
        return OBJSON.fromResource(location, Trans3.blockCenter);
    }

    public static OBJSON fromResource(ResourceLocation location, Trans3 trans) {
        return OBJSON.fromResource(location, trans, 16);
    }

    public static OBJSON fromResource(ResourceLocation location, Trans3 trans, int blockResolution) {
        return new OBJSON()
    }*/

    public String name() {
        return this.data.name();
    }

    public List<AABB> voxelize() {
        return this.voxelizer.voxelize();
    }

}
