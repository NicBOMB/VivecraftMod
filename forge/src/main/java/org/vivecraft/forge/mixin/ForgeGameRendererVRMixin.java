package org.vivecraft.forge.mixin;

import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_xr.render_pass.RenderPassType;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;

import static org.vivecraft.client_vr.VRState.dh;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class ForgeGameRendererVRMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setAnglesInternal(FF)V", remap = false), method = "renderLevel")
    public void forgeInternal(Camera camera, float yaw, float pitch) {
        if (RenderPassType.isVanilla() || dh.currentPass != RenderPass.LEFT && dh.currentPass != RenderPass.RIGHT) {
            camera.setAnglesInternal(yaw, pitch);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V", ordinal = 2), method = "renderLevel")
    public void forgeMulposZ(PoseStack poseStack, Quaternionf quaternion) {
        if (RenderPassType.isVanilla() || dh.currentPass != RenderPass.LEFT && dh.currentPass != RenderPass.RIGHT) {
            poseStack.mulPose(quaternion);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V ", ordinal = 3), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void removeMulposeX(PoseStack s, Quaternionf quaternion) {
        if (RenderPassType.isVanilla()) {
            s.mulPose(quaternion);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V ", ordinal = 4), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void removeMulposeY(PoseStack s, Quaternionf quaternion) {
        if (RenderPassType.isVanilla()) {
            s.mulPose(quaternion);
        } else {
            ((GameRendererExtension) this).applyVRModelView(dh.currentPass, s);
        }
    }

}
