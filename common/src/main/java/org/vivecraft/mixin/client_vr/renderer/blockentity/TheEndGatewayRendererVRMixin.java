package org.vivecraft.mixin.client_vr.renderer.blockentity;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.RenderStateShard.MultiTextureStateShard;
import net.minecraft.client.renderer.RenderStateShard.ShaderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_xr.render_pass.RenderPassType;

@Mixin(net.minecraft.client.renderer.blockentity.TheEndGatewayRenderer.class)
public class TheEndGatewayRendererVRMixin {
    @Unique
    private static final RenderType vivecraft$END_GATEWAY_VR = RenderType.create(
        "end_portal",
        DefaultVertexFormat.POSITION,
        Mode.QUADS,
        256,
        false,
        false,
        CompositeState.builder()
            .setShaderState(new ShaderStateShard(VRShaders::getRendertypeEndGatewayShaderVR))
            .setTextureState(MultiTextureStateShard.builder()
                .add(TheEndPortalRenderer.END_SKY_LOCATION, false, false)
                .add(TheEndPortalRenderer.END_PORTAL_LOCATION, false, false)
                .build()
            )
            .createCompositeState(false)
    );

    @Inject(at = @At("HEAD"), method = "renderType", cancellable = true)
    private void vivecraft$differentShaderInVR(CallbackInfoReturnable<RenderType> cir) {
        if (!RenderPassType.isVanilla()) {
            cir.setReturnValue(vivecraft$END_GATEWAY_VR);
        }
    }
}
