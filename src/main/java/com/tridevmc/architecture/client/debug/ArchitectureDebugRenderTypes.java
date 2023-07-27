package com.tridevmc.architecture.client.debug;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

import java.util.OptionalDouble;

public class ArchitectureDebugRenderTypes extends RenderStateShard {
    public static RenderType ARCHITECTURE_DEBUG_LINE = RenderType.create(
            "architecture_line_type",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.DEBUG_LINES,
            256,
            false,
            false,
            RenderType.CompositeState.builder()
                    .setLineState(new LineStateShard(OptionalDouble.empty()))
                    .setTransparencyState(NO_TRANSPARENCY)
                    .setWriteMaskState(COLOR_WRITE)
                    .setCullState(NO_CULL)
                    .setDepthTestState(NO_DEPTH_TEST)
                    .setShaderState(POSITION_COLOR_SHADER)
                    .setOverlayState(OVERLAY)
                    .setOutputState(PARTICLES_TARGET)
                    .createCompositeState(false)
    );

    public ArchitectureDebugRenderTypes(String pName, Runnable pSetupState, Runnable pClearState) {
        super(pName, pSetupState, pClearState);
    }

    private static class DisableDepthTest extends RenderStateShard.DepthTestStateShard {
        public DisableDepthTest(String name) {
            super("disable_depth_test", 519);
            this.setupState = () -> {
                RenderSystem.disableDepthTest();
            };

        }
    }


}
