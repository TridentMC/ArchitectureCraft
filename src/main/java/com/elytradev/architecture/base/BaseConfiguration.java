//------------------------------------------------------
//
//   Greg's Mod Base for 1.10 - Configuration
//
//------------------------------------------------------

package com.elytradev.architecture.base;

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
        String defaultValue = BaseStringUtils.join(",", defaultValueList);
        String value = getString(category, key, defaultValue);
        return BaseStringUtils.split(",", value);
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
