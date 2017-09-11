package com.elytradev.architecture.common.render;

import com.elytradev.architecture.legacy.common.helpers.Vector3;

public class ModelSpec {
    public String modelName;
    public String[] textureNames;
    public Vector3 origin;

    public ModelSpec(String model, String... textures) {
        this(model, Vector3.zero, textures);
    }

    public ModelSpec(String model, Vector3 origin, String... textures) {
        modelName = model;
        textureNames = textures;
        this.origin = origin;
    }
}
