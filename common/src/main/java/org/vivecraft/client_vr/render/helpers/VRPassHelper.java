package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.shaders.ProgramManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.lwjgl.opengl.GL13C;
import org.vivecraft.client.Xplat;
import org.vivecraft.client.extensions.RenderTargetExtension;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.VRShaders;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.mod_compat_vr.iris.IrisHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

public class VRPassHelper {

    private static float fovReduction = 1.0F;

    public static void renderSingleView(RenderPass eye, float partialTicks, long nanoTime, boolean renderWorld) {
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
        RenderSystem.clear(16384, Minecraft.ON_OSX);
        RenderSystem.enableDepthTest();
        VRState.mc.getProfiler().push("updateCameraAndRender");
        VRState.mc.gameRenderer.render(partialTicks, nanoTime, renderWorld);
        VRState.mc.getProfiler().pop();
        checkGLError("post game render " + eye.name());

        if (ClientDataHolderVR.currentPass == RenderPass.LEFT || ClientDataHolderVR.currentPass == RenderPass.RIGHT) {
            VRState.mc.getProfiler().push("postProcessEye");
            RenderTarget rendertarget = VRState.mc.getMainRenderTarget();

            if (ClientDataHolderVR.vrSettings.useFsaa) {
                RenderSystem.clearColor(RenderSystem.getShaderFogColor()[0], RenderSystem.getShaderFogColor()[1], RenderSystem.getShaderFogColor()[2], RenderSystem.getShaderFogColor()[3]);
                if (eye == RenderPass.LEFT) {
                    ClientDataHolderVR.vrRenderer.framebufferEye0.bindWrite(true);
                } else {
                    ClientDataHolderVR.vrRenderer.framebufferEye1.bindWrite(true);
                }
                RenderSystem.clear(16384, Minecraft.ON_OSX);
                VRState.mc.getProfiler().push("fsaa");
                ClientDataHolderVR.vrRenderer.doFSAA(false);
                rendertarget = ClientDataHolderVR.vrRenderer.fsaaLastPassResultFBO;
                checkGLError("fsaa " + eye.name());
                VRState.mc.getProfiler().pop();
            }

            if (eye == RenderPass.LEFT) {
                ClientDataHolderVR.vrRenderer.framebufferEye0.bindWrite(true);
            } else {
                ClientDataHolderVR.vrRenderer.framebufferEye1.bindWrite(true);
            }

            if (ClientDataHolderVR.vrSettings.useFOVReduction
                && ClientDataHolderVR.vrPlayer.getFreeMove()) {
                if (VRState.mc.player != null && (Math.abs(VRState.mc.player.zza) > 0.0F || Math.abs(VRState.mc.player.xxa) > 0.0F)) {
                    fovReduction = fovReduction - 0.05F;

                    if (fovReduction < ClientDataHolderVR.vrSettings.fovReductionMin) {
                        fovReduction = ClientDataHolderVR.vrSettings.fovReductionMin;
                    }
                } else {
                    fovReduction = fovReduction + 0.01F;

                    if (fovReduction > 0.8F) {
                        fovReduction = 0.8F;
                    }
                }
            } else {
                fovReduction = 1.0F;
            }

            VRShaders._FOVReduction_OffsetUniform.set(ClientDataHolderVR.vrSettings.fovRedutioncOffset);
            float red = 0.0F;
            float black = 0.0F;
            float blue = 0.0F;
            float time = (float) Util.getMillis() / 1000.0F;

            if (VRState.mc.player != null && VRState.mc.level != null) {
                if (((GameRendererExtension) VRState.mc.gameRenderer)
                    .vivecraft$wasInWater() != ((GameRendererExtension) VRState.mc.gameRenderer).vivecraft$isInWater()) {
                    ClientDataHolderVR.watereffect = 2.3F;
                } else {
                    if (((GameRendererExtension) VRState.mc.gameRenderer).vivecraft$isInWater()) {
                        ClientDataHolderVR.watereffect -= 0.008333334F;
                    } else {
                        ClientDataHolderVR.watereffect -= 0.016666668F;
                    }

                    if (ClientDataHolderVR.watereffect < 0.0F) {
                        ClientDataHolderVR.watereffect = 0.0F;
                    }
                }

                ((GameRendererExtension) VRState.mc.gameRenderer)
                    .vivecraft$setWasInWater(((GameRendererExtension) VRState.mc.gameRenderer).vivecraft$isInWater());

                if (Xplat
                    .isModLoaded("iris") || Xplat.isModLoaded("oculus")) {
                    if (!IrisHelper.hasWaterEffect()) {
                        ClientDataHolderVR.watereffect = 0.0F;
                    }
                }

                if (((GameRendererExtension) VRState.mc.gameRenderer).vivecraft$isInPortal()) {
                    ClientDataHolderVR.portaleffect = 1.0F;
                } else {
                    ClientDataHolderVR.portaleffect -= 0.016666668F;

                    if (ClientDataHolderVR.portaleffect < 0.0F) {
                        ClientDataHolderVR.portaleffect = 0.0F;
                    }
                }

                ItemStack itemstack = VRState.mc.player.getInventory().getArmor(3);

                if (itemstack.getItem() == Blocks.CARVED_PUMPKIN.asItem()
                    && (!itemstack.hasTag() || itemstack.getTag().getInt("CustomModelData") == 0)) {
                    ClientDataHolderVR.pumpkineffect = 1.0F;
                } else {
                    ClientDataHolderVR.pumpkineffect = 0.0F;
                }

                float hurtTimer = (float) VRState.mc.player.hurtTime - partialTicks;
                float healthPercent = 1.0F - VRState.mc.player.getHealth() / VRState.mc.player.getMaxHealth();
                healthPercent = (healthPercent - 0.5F) * 0.75F;

                if (hurtTimer > 0.0F) { // hurt flash
                    hurtTimer = hurtTimer / (float) VRState.mc.player.hurtDuration;
                    hurtTimer = healthPercent + Mth.sin(hurtTimer * hurtTimer * hurtTimer * hurtTimer * (float) Math.PI) * 0.5F;
                    red = hurtTimer;
                } else if (ClientDataHolderVR.vrSettings.low_health_indicator) { // red due to low health
                    red = healthPercent * Mth.abs(Mth.sin((2.5F * time) / (1.0F - healthPercent + 0.1F)));

                    if (VRState.mc.player.isCreative()) {
                        red = 0.0F;
                    }
                }

                float freeze = VRState.mc.player.getPercentFrozen();
                if (freeze > 0) {
                    blue = red;
                    blue = Math.max(freeze / 2, blue);
                    red = 0;
                }

                if (VRState.mc.player.isSleeping()) {
                    black = 0.5F + 0.3F * VRState.mc.player.getSleepTimer() * 0.01F;
                }

                if (ClientDataHolderVR.vr.isWalkingAbout && black < 0.8F) {
                    black = 0.5F;
                }
            } else {
                ClientDataHolderVR.watereffect = 0.0F;
                ClientDataHolderVR.portaleffect = 0.0F;
                ClientDataHolderVR.pumpkineffect = 0.0F;
            }

            if (ClientDataHolderVR.pumpkineffect > 0.0F) {
                VRShaders._FOVReduction_RadiusUniform.set(0.3F);
                VRShaders._FOVReduction_BorderUniform.set(0.0F);
            } else {
                VRShaders._FOVReduction_RadiusUniform.set(fovReduction);
                VRShaders._FOVReduction_BorderUniform.set(0.06F);
            }

            VRShaders._Overlay_HealthAlpha.set(red);
            VRShaders._Overlay_FreezeAlpha.set(blue);
            VRShaders._Overlay_BlackAlpha.set(black);
            VRShaders._Overlay_time.set(time);
            VRShaders._Overlay_waterAmplitude.set(ClientDataHolderVR.watereffect);
            VRShaders._Overlay_portalAmplitutde.set(ClientDataHolderVR.portaleffect);
            VRShaders._Overlay_pumpkinAmplitutde.set(ClientDataHolderVR.pumpkineffect);

            VRShaders._Overlay_eye.set(ClientDataHolderVR.currentPass == RenderPass.LEFT ? 1 : -1);
            ((RenderTargetExtension) rendertarget).vivecraft$blitFovReduction(VRShaders.fovReductionShader, ClientDataHolderVR.vrRenderer.framebufferEye0.viewWidth, ClientDataHolderVR.vrRenderer.framebufferEye0.viewHeight);
            ProgramManager.glUseProgram(0);
            checkGLError("post overlay" + eye);
            VRState.mc.getProfiler().pop();
        }

        if (ClientDataHolderVR.currentPass == RenderPass.CAMERA) {
            VRState.mc.getProfiler().push("cameraCopy");
            ClientDataHolderVR.vrRenderer.cameraFramebuffer.bindWrite(true);
            RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 1.0F);
            RenderSystem.clear(16640, Minecraft.ON_OSX);
            ((RenderTargetExtension) ClientDataHolderVR.vrRenderer.cameraRenderFramebuffer).vivecraft$blitToScreen(0,
                ClientDataHolderVR.vrRenderer.cameraFramebuffer.viewWidth,
                ClientDataHolderVR.vrRenderer.cameraFramebuffer.viewHeight, 0, true, 0.0F, 0.0F, false);
            VRState.mc.getProfiler().pop();
        }

        if (ClientDataHolderVR.currentPass == RenderPass.THIRD
            && ClientDataHolderVR.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY
            && OptifineHelper.isOptifineLoaded()
            && renderWorld && VRState.mc.level != null
            && OptifineHelper.isShaderActive()
            && OptifineHelper.bindShaderFramebuffer()) {
            // copy optifine depth buffer, since we need it for the mixed reality split
            RenderSystem.activeTexture(GL13C.GL_TEXTURE0);
            RenderSystem.bindTexture(ClientDataHolderVR.vrRenderer.framebufferMR.getDepthTextureId());
            checkGLError("pre copy depth");
            GlStateManager._glCopyTexSubImage2D(GL13C.GL_TEXTURE_2D, 0, 0, 0, 0, 0, ClientDataHolderVR.vrRenderer.framebufferMR.width, ClientDataHolderVR.vrRenderer.framebufferMR.height);
            checkGLError("post copy depth");
            // rebind the original buffer
            ClientDataHolderVR.vrRenderer.framebufferMR.bindWrite(false);
        }
    }

    private static void checkGLError(String string) {
        int i = GlStateManager._getError();
        if (i != 0) {
            System.err.println(string + ": " + i);
        }
    }
}
