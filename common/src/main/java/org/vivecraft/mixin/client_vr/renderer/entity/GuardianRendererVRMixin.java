package org.vivecraft.mixin.client_vr.renderer.entity;

import net.minecraft.client.renderer.entity.GuardianRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_xr.render_pass.RenderPassType;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

@Mixin(GuardianRenderer.class)
public abstract class GuardianRendererVRMixin {

    @Shadow
    protected abstract Vec3 getPosition(LivingEntity livingEntity, double d, float f);

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/GuardianRenderer;getPosition(Lnet/minecraft/world/entity/LivingEntity;DF)Lnet/minecraft/world/phys/Vec3;"), method = "render(Lnet/minecraft/world/entity/monster/Guardian;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V")
    public Vec3 vivecraft$changeEye(GuardianRenderer instance, LivingEntity livingEntity, double d, float f) {
        if (!RenderPassType.isVanilla() && livingEntity == mc.getCameraEntity()) {
            return dh.vrPlayer.getVRDataWorld().getEye(RenderPass.CENTER).getPosition().subtract(0.0D, 0.3D * (double) dh.vrPlayer.worldScale, 0.0D);
        }
        return this.getPosition(livingEntity, d, f);
    }
}
