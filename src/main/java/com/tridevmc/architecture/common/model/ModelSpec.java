/*
 * MIT License
 *
 * Copyright (c) 2017 Benjamin K
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.tridevmc.architecture.common.model;

import com.tridevmc.architecture.legacy.math.LegacyVector3;

import java.util.Arrays;
import java.util.Objects;

public class ModelSpec {
    public String modelName;
    public String[] textureNames;
    public LegacyVector3 origin;

    public ModelSpec(String model, String... textures) {
        this(model, LegacyVector3.BLOCK_CENTER, textures);
    }

    public ModelSpec(String model, LegacyVector3 origin, String... textures) {
        this.modelName = model;
        this.textureNames = textures;
        this.origin = origin;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;

        ModelSpec modelSpec = (ModelSpec) o;

        if (!Objects.equals(this.modelName, modelSpec.modelName))
            return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        if (!Arrays.equals(this.textureNames, modelSpec.textureNames)) return false;
        return Objects.equals(this.origin, modelSpec.origin);
    }

    @Override
    public int hashCode() {
        int result = this.modelName != null ? this.modelName.hashCode() : 0;
        result = 31 * result + Arrays.hashCode(this.textureNames);
        result = 31 * result + (this.origin != null ? this.origin.hashCode() : 0);
        return result;
    }
}
