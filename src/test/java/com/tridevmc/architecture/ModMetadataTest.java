package com.tridevmc.architecture;

import org.junit.jupiter.api.Test;

import net.neoforged.fml.loading.moddiscovery.ModInfo;
import net.neoforged.neoforgespi.language.IConfigurable;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ModMetadataTest {
    @Test
    void generatedVersionIsAcceptedByNeoForge() throws Exception {
        String version;
        try (var metadata = getClass().getResourceAsStream("/META-INF/neoforge.mods.toml")) {
            assertNotNull(metadata);
            version = new String(metadata.readAllBytes(), StandardCharsets.UTF_8).lines()
                    .filter(line -> line.startsWith("version = "))
                    .map(line -> line.split("\"")[1]).findFirst().orElseThrow();
        }
        var config = mock(IConfigurable.class);
        when(config.getConfigElement("modId")).thenReturn(Optional.of("architecturecraft"));
        when(config.getConfigElement("version")).thenReturn(Optional.of(version));
        assertEquals(version, new ModInfo(null, config).getVersion().toString());
    }
}
