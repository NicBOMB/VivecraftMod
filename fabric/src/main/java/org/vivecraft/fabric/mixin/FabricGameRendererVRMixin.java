package org.vivecraft.fabric.mixin;

import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_xr.render_pass.RenderPassType;

import org.joml.Quaternionf;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.GameRenderer;

import static org.vivecraft.client_vr.VRState.dh;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(GameRenderer.class)
public class FabricGameRendererVRMixin {

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V ", ordinal = 2), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void removeMulposeX(PoseStack s, Quaternionf quaternion) {
        if (RenderPassType.isVanilla()) {
            s.mulPose(quaternion);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V ", ordinal = 3), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void removeMulposeY(PoseStack s, Quaternionf quaternion) {
        if (RenderPassType.isVanilla()) {
            s.mulPose(quaternion);
        } else {
            ((GameRendererExtension) this).applyVRModelView(dh.currentPass, s);
        }
    }

    //optifabric only
    @Redirect(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;mulPose(Lorg/joml/Quaternionf;)V ", ordinal = 4), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V", expect = 0)
    public void removeMulposeY2(PoseStack s, Quaternionf quaternion) {
        if (RenderPassType.isVanilla()) {
            s.mulPose(quaternion);
        }
    }
}
