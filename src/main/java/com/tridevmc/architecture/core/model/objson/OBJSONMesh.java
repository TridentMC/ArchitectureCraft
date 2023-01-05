package com.tridevmc.architecture.core.model.objson;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.tridevmc.architecture.core.model.mesh.IFace;
import com.tridevmc.architecture.core.model.mesh.Mesh;
import com.tridevmc.architecture.core.model.mesh.PolygonData;
import org.jetbrains.annotations.NotNull;

public class OBJSONMesh extends Mesh<String, PolygonData> {
    public OBJSONMesh(@NotNull OBJSONData data) {
        super(ImmutableMap.of(), ImmutableList.of());
    }
}
