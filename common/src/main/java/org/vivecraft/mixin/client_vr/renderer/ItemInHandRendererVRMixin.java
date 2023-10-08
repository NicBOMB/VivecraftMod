package org.vivecraft.mixin.client_vr.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.extensions.EntityRenderDispatcherVRExtension;
import org.vivecraft.client_vr.extensions.ItemInHandRendererExtension;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.VRArmRenderer;
import org.vivecraft.client_vr.render.VRFirstPersonArmSwing;
import org.vivecraft.client_vr.render.VivecraftItemRendering;
import org.vivecraft.client_vr.render.VivecraftItemRendering.VivecraftItemTransformType;
import org.vivecraft.client_vr.render.helpers.VREffectsHelper;

import static org.joml.Math.*;
import static org.vivecraft.client_vr.VRState.*;

@Mixin(value = ItemInHandRenderer.class, priority = 999)
public abstract class ItemInHandRendererVRMixin implements ItemInHandRendererExtension {

    @Unique
    private VRFirstPersonArmSwing vivecraft$swingType = VRFirstPersonArmSwing.Attack;
    @Final
    @Shadow
    private EntityRenderDispatcher entityRenderDispatcher;
    @Final
    @Shadow
    private ItemRenderer itemRenderer;
    @Shadow
    private float oMainHandHeight;
    @Shadow
    private float mainHandHeight;
    @Shadow
    private float oOffHandHeight;
    @Shadow
    private float offHandHeight;

    @Inject(at = @At("HEAD"), method = "renderPlayerArm", cancellable = true)
    public void vivecraft$overrideArm(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f, float g, HumanoidArm humanoidArm, CallbackInfo ci) {
        if (!vrRunning) {
            return;
        }
        this.vivecraft$vrPlayerArm(poseStack, multiBufferSource, i, f, g, humanoidArm);
        ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "renderArmWithItem", cancellable = true)
    public void vivecraft$overrideArmItem(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float h, ItemStack itemStack, float i, PoseStack poseStack, MultiBufferSource multiBufferSource, int j, CallbackInfo ci) {
        if (!vrRunning) {
            return;
        }
        this.vivecraft$vrRenderArmWithItem(abstractClientPlayer, f, g, interactionHand, h, itemStack, i, poseStack, multiBufferSource, j);
        ci.cancel();
    }

    @Unique
    private void vivecraft$vrRenderArmWithItem(AbstractClientPlayer pPlayer, float pPartialTicks, float pPitch, InteractionHand pHand, float pSwingProgress, ItemStack pStack, float pEquippedProgress, PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight) {
        boolean mainHand = pHand == InteractionHand.MAIN_HAND;
        HumanoidArm humanoidarm = mainHand ? pPlayer.getMainArm() : pPlayer.getMainArm().getOpposite();
        pEquippedProgress = this.vivecraft$getEquipProgress(pHand, pPartialTicks);
        pMatrixStack.pushPose();

        if (!(dh.bowTracker.isActive(pHand)) && !pPlayer.isInvisible() && switch (dh.currentPass) {
            case THIRD -> {
                yield dh.vrSettings.mixedRealityRenderHands;
            }
            case CAMERA -> {
                yield false;
            }
            case SCOPEL -> {
                yield !(TelescopeTracker.isTelescope(pStack) && !mainHand);
            }
            case SCOPER -> {
                yield !(TelescopeTracker.isTelescope(pStack) && mainHand);
            }
            default -> {
                yield true;
            }
        }
        ) {
            this.renderPlayerArm(pMatrixStack, pBuffer, pCombinedLight, pEquippedProgress, pSwingProgress, humanoidarm);
        }

        if (!pStack.isEmpty()) {
            pMatrixStack.pushPose();

            if (pPlayer.swingingArm == pHand) {
                this.vivecraft$transformFirstPersonVR(pMatrixStack, humanoidarm, pSwingProgress);
            }

            VivecraftItemTransformType rendertype = VivecraftItemRendering.getTransformType(pStack, pPlayer, this.itemRenderer);

            boolean useLeftHandModelinLeftHand = false;

            ItemDisplayContext itemDisplayContext;
            if (dh.vrSettings.thirdPersonItems) {
                useLeftHandModelinLeftHand = true; //test
                VivecraftItemRendering.applyThirdPersonItemTransforms(pMatrixStack, rendertype, mainHand, pPlayer, pEquippedProgress, pPartialTicks, pStack, pHand);
                itemDisplayContext = mainHand ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : (useLeftHandModelinLeftHand ? ItemDisplayContext.THIRD_PERSON_LEFT_HAND : ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
            } else {
                VivecraftItemRendering.applyFirstPersonItemTransforms(pMatrixStack, rendertype, mainHand, pPlayer, pEquippedProgress, pPartialTicks, pStack, pHand);
                itemDisplayContext = mainHand ? ItemDisplayContext.FIRST_PERSON_RIGHT_HAND : (useLeftHandModelinLeftHand ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
            }

            dh.isfphand = true;

            switch (rendertype) {
                case Map -> {
                    RenderSystem.disableCull();
                    this.renderMap(pMatrixStack, pBuffer, pCombinedLight, pStack);
                }
                case Telescope -> {
                    if (dh.currentPass != RenderPass.SCOPEL && dh.currentPass != RenderPass.SCOPER) {
                        pMatrixStack.pushPose();
                        pMatrixStack.scale(0.625F, 0.625F, 0.625F);
                        pMatrixStack.last().pose().translate(mainHand ? -0.53F : -0.47F, -0.5F, -0.6F);
                        //pMatrixStack.last().pose().rotateX((float) PI);
                        //pMatrixStack.last().normal().rotateX((float) PI);
                        mc.getBlockRenderer().getModelRenderer().renderModel(pMatrixStack.last(), pBuffer.getBuffer(Sheets.solidBlockSheet()), null, mc.getModelManager().getModel(TelescopeTracker.scopeModel), 0.5F, 0.5F, 1.0F, pCombinedLight, OverlayTexture.NO_OVERLAY);
                        pMatrixStack.popPose();
                    }

                    float ang1 = (float) PI / 2.0F;
                    float ang2 = (float) PI;

                    pMatrixStack.pushPose();
                    pMatrixStack.last().pose()
                        .translate(mainHand ? -0.01875F : 0.01875F, 0.215F, -0.0626F)
                        .rotateXYZ(ang1, ang2, ang2);
                    pMatrixStack.last().normal().rotateXYZ(ang1, ang2, ang2);
                    VREffectsHelper.drawScopeFB(pMatrixStack, pHand == InteractionHand.MAIN_HAND ? 0 : 1);
                    pMatrixStack.popPose();
                }
                default -> {
                    this.renderItem(pPlayer, pStack, itemDisplayContext, !mainHand && useLeftHandModelinLeftHand, pMatrixStack, pBuffer, pCombinedLight);
                }
            }

            dh.isfphand = false;
            pMatrixStack.popPose();
        }

        pMatrixStack.popPose();
    }

    @Shadow
    public abstract void renderItem(LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext itemDisplayContext, boolean bl, PoseStack poseStack, MultiBufferSource multiBufferSource, int i);

    @Shadow
    protected abstract void renderMap(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, ItemStack pStack);

    @Shadow
    protected abstract void renderPlayerArm(PoseStack pMatrixStack, MultiBufferSource pBuffer, int pCombinedLight, float pEquippedProgress, float pSwingProgress, HumanoidArm humanoidarm);

    @Unique
    private float vivecraft$getEquipProgress(InteractionHand hand, float partialTicks) {
        return hand == InteractionHand.MAIN_HAND ? 1.0F - (this.oMainHandHeight + (this.mainHandHeight - this.oMainHandHeight) * partialTicks) : 1.0F - (this.oOffHandHeight + (this.offHandHeight - this.oOffHandHeight) * partialTicks);
    }

    @Unique
    private void vivecraft$vrPlayerArm(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, float f, float g, HumanoidArm humanoidArm) {
        boolean flag = humanoidArm != HumanoidArm.LEFT;
        float h = flag ? 1.0F : -1.0F;
        AbstractClientPlayer abstractclientplayer = mc.player;
        RenderSystem.setShaderTexture(0, abstractclientplayer.getSkin().texture());
        VRArmRenderer vrarmrenderer = ((EntityRenderDispatcherVRExtension) this.entityRenderDispatcher).vivecraft$getArmSkinMap().get(abstractclientplayer.getSkin().model().id());
        poseStack.pushPose();

        if (abstractclientplayer.swingingArm == InteractionHand.MAIN_HAND && flag) {
            this.vivecraft$transformFirstPersonVR(poseStack, humanoidArm, g);
        }

        if (abstractclientplayer.swingingArm == InteractionHand.OFF_HAND && !flag) {
            this.vivecraft$transformFirstPersonVR(poseStack, humanoidArm, g);
        }

        poseStack.scale(0.4f, 0.4F, 0.4F);
        boolean slim = "slim".equals(abstractclientplayer.getSkin().model().id());

            /*
             x offset: (arm x origin + arm x offset + arm x dimension * 0.5) / 16
             z offset: (arm y origin + arm y offset + arm y dimension) / 16
             slim
             x offset: (5 + -1 + 3*0.5) / 16 = 0.34375
             z offset: (-2 + 2.5 + 12) / 16 = 0.78125
             regular
             x offset: (5 - 1 + 4*0.5) / 16 = 0.375
             z offset: (-2 + 2 + 12) / 16 = 0.75
            */
        float ang1 = (float) PI / -2.0F;
        float ang2 = (float) PI;

        poseStack.last().pose()
            .translate((slim ? -0.34375F : -0.375F) * h, 0.0F, slim ? 0.78125F : 0.75F)
            .rotateX(ang1)
            .rotateY(ang2);
        poseStack.last().normal()
            .rotateX(ang1)
            .rotateY(ang2);

        if (flag) {
            vrarmrenderer.renderRightHand(poseStack, multiBufferSource, i, abstractclientplayer);
        } else {
            vrarmrenderer.renderLeftHand(poseStack, multiBufferSource, i, abstractclientplayer);
        }
        poseStack.popPose();
    }

    @Override
    @Unique
    public void vivecraft$setSwingType(VRFirstPersonArmSwing interact) {
        this.vivecraft$swingType = interact;
    }

    @Unique
    private void vivecraft$transformFirstPersonVR(PoseStack matrixStackIn, HumanoidArm hand, float swingProgress) {
        if (swingProgress != 0.0F) {
            switch (this.vivecraft$swingType) {
                case Attack -> {
                    float f2 = sin((swingProgress * 3.0F) * (float) PI);
                    if (swingProgress > 0.5F) {
                        f2 = sin((swingProgress * (float) PI + (float) PI));
                    }
                    f2 *= 30.0F;
                    f2 = toRadians(f2);
                    matrixStackIn.last().pose()
                        .translate(0.0F, 0.0F, 0.2F)
                        .rotateX(f2)
                        .translate(0.0F, 0.0F, -0.2F);
                    matrixStackIn.last().normal().rotateX(f2);
                }
                case Interact -> {
                    float f1 = sin((swingProgress * 3.0F) * (float) PI);
                    if (swingProgress > 0.5F) {
                        f1 = sin(swingProgress * (float) PI + (float) PI);
                    }
                    f1 *= hand == HumanoidArm.RIGHT ? -45.0F : 45.0F;
                    f1 = toRadians(f1);
                    matrixStackIn.last().pose().rotateZ(f1);
                    matrixStackIn.last().normal().rotateZ(f1);
                }
                case Use -> {
                    float f = sin((swingProgress * 2.0F) * (float) PI);
                    if (swingProgress > 0.25F) {
                        f = sin((swingProgress / 2.0F) * (float) PI + (float) PI);
                    }
                    matrixStackIn.last().pose().translate(0.0F, 0.0F, -(1.0F + f) * 0.1F);
                }
            }
        }
    }
}
