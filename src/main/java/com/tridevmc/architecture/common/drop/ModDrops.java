package com.tridevmc.architecture.common.drop;

/**
 * Registers mod drops and notifies drops of forge events.
 */
public class ModDrops {

/*    private List<IModDrop> registeredDrops = Lists.newArrayList();

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
                Map<String, Object> annotationInfo = asmData.getAnnotationInfo();
                String requiredMod = (String) annotationInfo.get("requiredMod");
                if (Loader.isModLoaded(requiredMod)) {
                    Class<?> dropClass = Class.forName(asmData.getClassName());
                    if (IModDrop.class.isAssignableFrom(dropClass)) {
                        IModDrop drop = (IModDrop) dropClass.newInstance();
                        this.registeredDrops.add(drop);
                    }
                }
            } catch (Exception exception) {
                ArchitectureLog.error("Failed to load drop, caused by {}", exception);
            }
        }
    }*/

}
