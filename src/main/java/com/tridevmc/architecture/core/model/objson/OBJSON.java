package com.tridevmc.architecture.core.model.objson;

import com.tridevmc.architecture.core.math.ITrans3;
import com.tridevmc.architecture.core.model.Voxelizer;
import com.tridevmc.architecture.core.model.mesh.IMesh;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import com.tridevmc.architecture.core.physics.AABB;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record OBJSON(OBJSONData data, IMesh<String, PolygonData> mesh, Voxelizer voxelizer) {

    public OBJSON(OBJSONData data, IMesh<String, PolygonData> mesh, int blockResolution) {
        this(data, mesh, new Voxelizer(mesh, blockResolution));
    }

    public OBJSON(OBJSONData data, IMesh<String, PolygonData> mesh) {
        this(data, mesh, 16);
    }

    public OBJSON(OBJSONData data, @NotNull ITrans3 trans, int blockResolution) {
        this(data, new OBJSONMesh(data).transform(trans), blockResolution);
    }

    public OBJSON(OBJSONData data, @NotNull ITrans3 trans) {
        this(data, trans, 16);
    }

    public static OBJSON fromResource(ResourceLocation location) {
        return OBJSON.fromResource(location, ITrans3.BLOCK_CENTER);
    }

    public static OBJSON fromResource(ResourceLocation location, ITrans3 trans) {
        return OBJSON.fromResource(location, trans, 16);
    }

    public static OBJSON fromResource(ResourceLocation location, ITrans3 trans, int blockResolution) {
        return new OBJSON(OBJSONData.fromResource(location), trans, blockResolution);
    }

    public String name() {
        return this.data.name();
    }

    public List<AABB> voxelize() {
        return this.voxelizer.voxelize();
    }

}
