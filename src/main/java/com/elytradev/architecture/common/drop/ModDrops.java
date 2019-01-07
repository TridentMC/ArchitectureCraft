package com.elytradev.architecture.common.drop;

import com.elytradev.architecture.common.ArchitectureLog;
import com.google.common.collect.Lists;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.util.List;
import java.util.Map;

/**
 * Registers mod drops and notifies drops of forge events.
 */
public class ModDrops {

    private List<IModDrop> registeredDrops = Lists.newArrayList();

    public void preInit(FMLPreInitializationEvent e) {
        this.loadDrops(e.getAsmData());

        this.registeredDrops.forEach((d) -> d.preInit(e));
    }

    public void init(FMLInitializationEvent e) {
        this.registeredDrops.forEach((d) -> d.init(e));
    }

    public void postInit(FMLPostInitializationEvent e) {
        this.registeredDrops.forEach((d) -> d.postInit(e));
    }

    private void loadDrops(ASMDataTable dataTable) {
        String annotationName = RegisterDrop.class.getName();
        Set<ASMDataTable.ASMData> asmDataSet = dataTable.getAll(annotationName);

        for (ASMDataTable.ASMData asmData : asmDataSet) {
            try {
                Class<?> dropClass = Class.forName(asmData.getClassName());
                if (IModDrop.class.isAssignableFrom(dropClass)) {
                    Map<String, Object> annotationInfo = asmData.getAnnotationInfo();
                    String requiredMod = (String) annotationInfo.get("requiredMod");
                    if (Loader.isModLoaded(requiredMod)) {
                        IModDrop drop = (IModDrop) dropClass.newInstance();
                        registeredDrops.add(drop);
                    }
                }
            } catch (Exception exception) {
                ArchitectureLog.error("Failed to load drop, caused by {}", exception);
            }
        }
    }

}
