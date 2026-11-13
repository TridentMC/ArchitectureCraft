package com.tridevmc.architecture.testsupport;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.MockedStatic;

import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.server.Bootstrap;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.registries.GameData;

import java.util.List;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public final class MinecraftTestBootstrap implements BeforeAllCallback {
    private static final MockedStatic<FMLEnvironment> ENVIRONMENT = mockStatic(FMLEnvironment.class);

    private static final MockedStatic<FMLLoader> LOADER = mockStatic(FMLLoader.class);
    private static final HolderLookup.Provider REGISTRIES;

    static {
        ENVIRONMENT.when(FMLEnvironment::isProduction).thenReturn(true);
        // A plain JVM has no discovered mods; NeoForge still asks the loader for feature flags.
        var loader = mock(FMLLoader.class, RETURNS_DEEP_STUBS);
        LOADER.when(FMLLoader::getCurrent).thenReturn(loader);
        when(loader.getLoadingModList().getModFiles()).thenReturn(List.of());
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        REGISTRIES = VanillaRegistries.createWorldLookup();
        bindDefaultComponents();
        GameData.unfreezeData();
    }

    public static void initialize() {
    }

    public static void bindDefaultComponents() {
        BuiltInRegistries.DATA_COMPONENT_INITIALIZERS.build(REGISTRIES)
                .forEach(components -> components.apply());
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL).getOrComputeIfAbsent(
                MinecraftTestBootstrap.class, key -> (ExtensionContext.Store.CloseableResource) () -> {
                    LOADER.close();
                    ENVIRONMENT.close();
                });
    }
}
