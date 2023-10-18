package org.vivecraft.client_xr.render_pass;

import com.mojang.blaze3d.pipeline.MainTarget;
import net.minecraft.client.renderer.PostChain;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.mixin.client.MinecraftAccessor;
import org.vivecraft.mixin.client.renderer.GameRendererAccessor;

public class RenderPassManager {
    public static RenderPassManager INSTANCE;
    public final MainTarget vanillaRenderTarget;
    public PostChain vanillaOutlineChain;
    public PostChain vanillaPostEffect;
    public PostChain vanillaTransparencyChain;
    public static RenderPassType renderPassType = RenderPassType.VANILLA;
    public static WorldRenderPass wrp;

    public RenderPassManager(MainTarget vanillaRenderTarget) {
        this.vanillaRenderTarget = vanillaRenderTarget;
    }

    public static void setWorldRenderPass(WorldRenderPass wrp) {
        RenderPassManager.wrp = wrp;
        renderPassType = RenderPassType.WORLD_ONLY;
        ((MinecraftAccessor) VRState.mc).setMainRenderTarget(wrp.target);
        if (VRState.mc.gameRenderer != null) {
            ((GameRendererAccessor) VRState.mc.gameRenderer).setPostEffect(wrp.postEffect);
        }
    }

    public static void setGUIRenderPass() {
        ClientDataHolderVR.currentPass = RenderPass.GUI;
        RenderPassManager.wrp = null;
        renderPassType = RenderPassType.GUI_ONLY;
        ((MinecraftAccessor) VRState.mc).setMainRenderTarget(GuiHandler.guiFramebuffer);
    }

    public static void setVanillaRenderPass() {
        ClientDataHolderVR.currentPass = null;
        RenderPassManager.wrp = null;
        renderPassType = RenderPassType.VANILLA;
        ((MinecraftAccessor) VRState.mc).setMainRenderTarget(INSTANCE.vanillaRenderTarget);
        if (VRState.mc.gameRenderer != null) {
            ((GameRendererAccessor) VRState.mc.gameRenderer).setPostEffect(INSTANCE.vanillaPostEffect);
        }
    }
}
