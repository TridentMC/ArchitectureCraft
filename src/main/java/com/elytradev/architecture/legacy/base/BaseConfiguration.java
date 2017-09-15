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

package com.elytradev.architecture.legacy.base;

import com.elytradev.architecture.common.utils.StringUtils;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import java.io.File;

public class BaseConfiguration extends Configuration {

    public boolean extended = false;
    int nextVillagerID = 100;

    public BaseConfiguration(File file) {
        super(file);
    }

    public boolean getBoolean(String category, String key, boolean defaultValue) {
        return get(category, key, defaultValue).getBoolean(defaultValue);
    }

    public int getInteger(String category, String key, int defaultValue) {
        return get(category, key, defaultValue).getInt(defaultValue);
    }

    public double getDouble(String category, String key, double defaultValue) {
        return get(category, key, defaultValue).getDouble(defaultValue);
    }

    public String getString(String category, String key, String defaultValue) {
        return get(category, key, defaultValue).getString();
    }

    public String[] getStringList(String category, String key, String... defaultValueList) {
        String defaultValue = StringUtils.join(",", defaultValueList);
        String value = getString(category, key, defaultValue);
        return StringUtils.split(",", value);
    }

//     public int getVillager(String key) {
//         VillagerRegistry reg = VillagerRegistry.instance();
//         Property prop = get("villagers", key, -1);
//         int id = prop.getInt();
//         if (id == -1) {
//             id = allocateVillagerId(reg);
//             prop.set(id);
//         }
//         reg.registerVillagerId(id);
//         return id;
//     }
//     
//     int allocateVillagerId(VillagerRegistry reg) {
//         Collection<Integer> inUse = VillagerRegistry.getRegisteredVillagers();
//         for (;;) {
//             int id = nextVillagerID++;
//             if (!inUse.contains(id))
//                 return id;
//         }
//     }

    @Override
    public Property get(String category, String key, String defaultValue, String comment, Property.Type type) {
        if (!hasKey(category, key))
            extended = true;
        return super.get(category, key, defaultValue, comment, type);
    }

}
