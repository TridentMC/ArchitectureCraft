package com.elytradev.architecture.common.render;

import com.elytradev.architecture.legacy.common.helpers.Vector3;

import java.util.Arrays;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModelSpec modelSpec = (ModelSpec) o;

        if (modelName != null ? !modelName.equals(modelSpec.modelName) : modelSpec.modelName != null) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(textureNames, modelSpec.textureNames)) return false;
        return origin != null ? origin.equals(modelSpec.origin) : modelSpec.origin == null;
    }

    @Override
    public int hashCode() {
        int result = modelName != null ? modelName.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(textureNames);
        result = 31 * result + (origin != null ? origin.hashCode() : 0);
        return result;
    }
}
