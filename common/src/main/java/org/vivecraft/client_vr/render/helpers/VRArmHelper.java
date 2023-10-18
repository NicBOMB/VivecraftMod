package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11C;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.gameplay.trackers.BowTracker;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.mod_compat_vr.ShadersHelper;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

public class VRArmHelper {

    private static final Vec3i tpUnlimitedColor = new Vec3i(173, 216, 230);
    private static final Vec3i tpLimitedColor = new Vec3i(205, 169, 205);
    private static final Vec3i tpInvalidColor = new Vec3i(83, 83, 83);

    public static boolean shouldRenderHands() {
        if (ClientDataHolderVR.viewonly) {
            return false;
        } else if (ClientDataHolderVR.currentPass == RenderPass.THIRD) {
            return ClientDataHolderVR.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY;
        } else {
            return ClientDataHolderVR.currentPass != RenderPass.CAMERA;
        }
    }

    public static void renderVRHands(float partialTicks, boolean renderRight, boolean renderLeft, boolean menuHandRight,
        boolean menuHandLeft, PoseStack poseStack) {
        VRState.mc.getProfiler().push("hands");
        // backup projection matrix, not doing that breaks sodium water on 1.19.3
        RenderSystem.backupProjectionMatrix();

        if (renderRight) {
            ClientDataHolderVR.ismainhand = true;

            if (menuHandRight) {
                renderMainMenuHand(0, partialTicks, false, poseStack);
            } else {
                ((GameRendererExtension) VRState.mc.gameRenderer).vivecraft$resetProjectionMatrix(partialTicks);
                PoseStack newPoseStack = new PoseStack();
                newPoseStack.last().pose().identity();
                RenderHelper.applyVRModelView(ClientDataHolderVR.currentPass, newPoseStack);
                renderVRHand_Main(newPoseStack, partialTicks);
            }

            ClientDataHolderVR.ismainhand = false;
        }

        if (renderLeft) {
            if (menuHandLeft) {
                renderMainMenuHand(1, partialTicks, false, poseStack);
            } else {
                ((GameRendererExtension) VRState.mc.gameRenderer).vivecraft$resetProjectionMatrix(partialTicks);
                PoseStack newPoseStack = new PoseStack();
                newPoseStack.last().pose().identity();
                RenderHelper.applyVRModelView(ClientDataHolderVR.currentPass, newPoseStack);
                renderVRHand_Offhand(partialTicks, true, newPoseStack);
            }
        }

        RenderSystem.restoreProjectionMatrix();
        VRState.mc.getProfiler().pop();
    }

    public static void renderMainMenuHand(int c, float partialTicks, boolean depthAlways, PoseStack poseStack) {
        ((GameRendererExtension) VRState.mc.gameRenderer).vivecraft$resetProjectionMatrix(partialTicks);
        poseStack.pushPose();
        poseStack.setIdentity();
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderHelper.applyVRModelView(ClientDataHolderVR.currentPass, poseStack);
        RenderHelper.setupRenderingAtController(c, poseStack);

        if (VRState.mc.getOverlay() == null) {
            VRState.mc.getTextureManager().bindForSetup(new ResourceLocation("vivecraft:textures/white.png"));
            RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
        }

        Tesselator tesselator = Tesselator.getInstance();

        if (depthAlways && c == 0) {
            RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        } else {
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        }

        Vec3i color = new Vec3i(64, 64, 64);
        byte alpha = (byte) 255;

        Vec3 dir = new Vec3(0.0D, 0.0D, -1.0D);

        Vec3 start = new Vec3(0.0D, 0.0D, 0.0D);
        Vec3 end = new Vec3(start.x - dir.x * 0.18D, start.y - dir.y * 0.18D, start.z - dir.z * 0.18D);

        if (VRState.mc.level != null) {
            float light = (float) VRState.mc.level.getMaxLocalRawBrightness(
                BlockPos.containing(ClientDataHolderVR.vrPlayer.vrdata_world_render.hmd.getPosition()));

            int minLight = ShadersHelper.ShaderLight();

            if (light < (float) minLight) {
                light = (float) minLight;
            }

            float lightPercent = light / (float) VRState.mc.level.getMaxLightLevel();
            color = new Vec3i(Mth.floor(color.getX() * lightPercent), Mth.floor(color.getY() * lightPercent),
                Mth.floor(color.getZ() * lightPercent));
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        tesselator.getBuilder().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        RenderHelper.renderBox(tesselator, start, end, -0.02F, 0.02F, -0.0125F, 0.0125F, color, alpha, poseStack);
        BufferUploader.drawWithShader(tesselator.getBuilder().end());
        poseStack.popPose();
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
    }

    public static void renderVRHand_Main(PoseStack poseStack, float partialTicks) {
        poseStack.pushPose();
        RenderHelper.setupRenderingAtController(0, poseStack);
        ItemStack item = VRState.mc.player.getMainHandItem();
        ItemStack override = null; // this.minecraft.physicalGuiManager.getHeldItemOverride();

        if (override != null) {
            item = override;
        }

        if (ClientDataHolderVR.climbTracker.isClimbeyClimb() && item.getItem() != Items.SHEARS) {
            item = override == null ? VRState.mc.player.getOffhandItem() : override;
        }

        if (BowTracker.isHoldingBow(VRState.mc.player, InteractionHand.MAIN_HAND)) {
            //do ammo override
            int c = 0;

            if (ClientDataHolderVR.vrSettings.reverseShootingEye) {
                c = 1;
            }

            ItemStack ammo = VRState.mc.player.getProjectile(VRState.mc.player.getMainHandItem());

            if (ammo != ItemStack.EMPTY && !ClientDataHolderVR.bowTracker.isNotched()) {
                //render the arrow in right, left hand will check for and render bow.
                item = ammo;
            } else {
                item = ItemStack.EMPTY;
            }
        } else if (BowTracker.isHoldingBow(VRState.mc.player, InteractionHand.OFF_HAND)
            && ClientDataHolderVR.bowTracker.isNotched()) {
            int c = 0;

            if (ClientDataHolderVR.vrSettings.reverseShootingEye) {
                c = 1;
            }

            item = ItemStack.EMPTY;
        }

        if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
            OptifineHelper.beginEntities();
        }
        poseStack.pushPose();

        VRState.mc.gameRenderer.lightTexture().turnOnLightLayer();
        MultiBufferSource.BufferSource bufferSource = VRState.mc.renderBuffers().bufferSource();
        VRState.mc.gameRenderer.itemInHandRenderer.renderArmWithItem(VRState.mc.player, partialTicks,
            0.0F, InteractionHand.MAIN_HAND, VRState.mc.player.getAttackAnim(partialTicks), item, 0.0F,
            poseStack, bufferSource,
            VRState.mc.getEntityRenderDispatcher().getPackedLightCoords(VRState.mc.player, partialTicks));
        bufferSource.endBatch();
        VRState.mc.gameRenderer.lightTexture().turnOffLightLayer();

        if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
            OptifineHelper.endEntities();
        }
        poseStack.popPose();

        poseStack.popPose();
    }

    public static void renderVRHand_Offhand(float partialTicks, boolean renderTeleport, PoseStack poseStack) {
        poseStack.pushPose();
        RenderHelper.setupRenderingAtController(1, poseStack);
        ItemStack item = VRState.mc.player.getOffhandItem();
        ItemStack override = null;// this.minecraft.physicalGuiManager.getOffhandOverride();

        if (override != null) {
            item = override;
        }

        if (ClientDataHolderVR.climbTracker.isClimbeyClimb()
            && (item == null || item.getItem() != Items.SHEARS)) {
            item = VRState.mc.player.getMainHandItem();
        }

        if (BowTracker.isHoldingBow(VRState.mc.player, InteractionHand.MAIN_HAND)) {
            int c = 1;

            if (ClientDataHolderVR.vrSettings.reverseShootingEye) {
                c = 0;
            }

            item = VRState.mc.player.getMainHandItem();
        }

        if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
            OptifineHelper.beginEntities();
        }
        poseStack.pushPose();

        VRState.mc.gameRenderer.lightTexture().turnOnLightLayer();
        MultiBufferSource.BufferSource bufferSource = VRState.mc.renderBuffers().bufferSource();
        VRState.mc.gameRenderer.itemInHandRenderer.renderArmWithItem(VRState.mc.player, partialTicks,
            0.0F, InteractionHand.OFF_HAND, VRState.mc.player.getAttackAnim(partialTicks), item, 0.0F,
            poseStack, bufferSource,
            VRState.mc.getEntityRenderDispatcher().getPackedLightCoords(VRState.mc.player, partialTicks));
        bufferSource.endBatch();
        VRState.mc.gameRenderer.lightTexture().turnOffLightLayer();

        if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isShaderActive()) {
            OptifineHelper.endEntities();
        }
        poseStack.popPose();

        poseStack.popPose();

        if (renderTeleport) {
            poseStack.pushPose();
            poseStack.setIdentity();
            RenderHelper.applyVRModelView(ClientDataHolderVR.currentPass, poseStack);
//			net.optifine.shaders.Program program = Shaders.activeProgram; TODO

//			if (Config.isShaders()) {
//				Shaders.useProgram(Shaders.ProgramTexturedLit);
//			}

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

            //	TP energy
            if (ClientNetworking.isLimitedSurvivalTeleport() && !ClientDataHolderVR.vrPlayer.getFreeMove()
                && VRState.mc.gameMode.hasMissTime()
                && ClientDataHolderVR.teleportTracker.vrMovementStyle.arcAiming
                && !ClientDataHolderVR.bowTracker.isActive(VRState.mc.player)) {
                poseStack.pushPose();
                RenderHelper.setupRenderingAtController(1, poseStack);
                Vec3 start = new Vec3(0.0D, 0.005D, 0.03D);
                float max = 0.03F;
                float r;

                if (ClientDataHolderVR.teleportTracker.isAiming()) {
                    r = 2.0F * (float) ((double) ClientDataHolderVR.teleportTracker.getTeleportEnergy()
                        - 4.0D * ClientDataHolderVR.teleportTracker.movementTeleportDistance) / 100.0F * max;
                } else {
                    r = 2.0F * ClientDataHolderVR.teleportTracker.getTeleportEnergy() / 100.0F * max;
                }

                if (r < 0.0F) {
                    r = 0.0F;
                }
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                VRState.mc.getTextureManager().bindForSetup(new ResourceLocation("vivecraft:textures/white.png"));
                RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
                RenderHelper.renderFlatQuad(start.add(0.0D, 0.05001D, 0.0D), r, r, 0.0F, tpLimitedColor.getX(),
                    tpLimitedColor.getY(), tpLimitedColor.getZ(), 128, poseStack);
                RenderHelper.renderFlatQuad(start.add(0.0D, 0.05D, 0.0D), max, max, 0.0F, tpLimitedColor.getX(),
                    tpLimitedColor.getY(), tpLimitedColor.getZ(), 50, poseStack);
                poseStack.popPose();
            }

            if (ClientDataHolderVR.teleportTracker.isAiming()) {
                RenderSystem.enableDepthTest();

                if (ClientDataHolderVR.teleportTracker.vrMovementStyle.arcAiming) {
                    renderTeleportArc(poseStack);
                }
            }

            RenderSystem.defaultBlendFunc();

//			if (Config.isShaders()) {
//				Shaders.useProgram(program);
//			}

            poseStack.popPose();
        }
    }

    public static void renderTeleportArc(PoseStack poseStack) {
        if (ClientDataHolderVR.teleportTracker.vrMovementStyle.showBeam
            && ClientDataHolderVR.teleportTracker.isAiming()
            && ClientDataHolderVR.teleportTracker.movementTeleportArcSteps > 1) {
            VRState.mc.getProfiler().push("teleportArc");

            RenderSystem.enableCull();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);

            // to make shaders work
            VRState.mc.getTextureManager().bindForSetup(new ResourceLocation("vivecraft:textures/white.png"));
            RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));

            Tesselator tesselator = Tesselator.getInstance();
            tesselator.getBuilder().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);

            double VOffset = ClientDataHolderVR.teleportTracker.lastTeleportArcDisplayOffset;
            Vec3 dest = ClientDataHolderVR.teleportTracker.getDestination();
            boolean validLocation = dest.x != 0.0D || dest.y != 0.0D || dest.z != 0.0D;

            byte alpha = -1;
            Vec3i color;

            if (!validLocation) {
                color = tpInvalidColor;
                alpha = -128;
            } else {
                if (ClientNetworking.isLimitedSurvivalTeleport() && !VRState.mc.player.getAbilities().mayfly) {
                    color = tpLimitedColor;
                } else {
                    color = tpUnlimitedColor;
                }

                VOffset = ClientDataHolderVR.vrRenderer.getCurrentTimeSecs()
                    * (double) ClientDataHolderVR.teleportTracker.vrMovementStyle.textureScrollSpeed * 0.6D;
                ClientDataHolderVR.teleportTracker.lastTeleportArcDisplayOffset = VOffset;
            }

            float segmentHalfWidth = ClientDataHolderVR.teleportTracker.vrMovementStyle.beamHalfWidth * 0.15F;
            int segments = ClientDataHolderVR.teleportTracker.movementTeleportArcSteps - 1;

            if (ClientDataHolderVR.teleportTracker.vrMovementStyle.beamGrow) {
                segments = (int) (segments * ClientDataHolderVR.teleportTracker.movementTeleportProgress);
            }

            double segmentProgress = 1.0D / (double) segments;

            Vec3 cameraPosition = RenderHelper.getSmoothCameraPosition(ClientDataHolderVR.currentPass, ClientDataHolderVR.vrPlayer.getVRDataWorld());
            // arc
            for (int i = 0; i < segments; ++i) {
                double progress = (double) i / (double) segments + VOffset * segmentProgress;
                int progressBase = Mth.floor(progress);
                progress -= progressBase;

                Vec3 start = ClientDataHolderVR.teleportTracker
                    .getInterpolatedArcPosition((float) (progress - segmentProgress * (double) 0.4F))
                    .subtract(cameraPosition);

                Vec3 end = ClientDataHolderVR.teleportTracker.getInterpolatedArcPosition((float) progress)
                    .subtract(cameraPosition);
                float shift = (float) progress * 2.0F;
                RenderHelper.renderBox(tesselator, start, end, -segmentHalfWidth, segmentHalfWidth, (-1.0F + shift) * segmentHalfWidth, (1.0F + shift) * segmentHalfWidth, color, alpha, poseStack);
            }

            tesselator.end();

            // hit indicator
            if (validLocation && ClientDataHolderVR.teleportTracker.movementTeleportProgress >= 1.0D) {
                RenderSystem.disableCull();
                Vec3 vec34 = (new Vec3(dest.x, dest.y, dest.z)).subtract(cameraPosition);
                float offset = 0.01F;
                double x = 0.0D;
                double y = 0.0D;
                double z = 0.0D;

                y += offset;

                RenderHelper.renderFlatQuad(vec34.add(x, y, z), 0.6F, 0.6F, 0.0F, (int) (color.getX() * 1.03D),
                    (int) (color.getY() * 1.03D), (int) (color.getZ() * 1.03D), 64, poseStack);

                y += offset;

                RenderHelper.renderFlatQuad(vec34.add(x, y, z), 0.4F, 0.4F, 0.0F, (int) (color.getX() * 1.04D),
                    (int) (color.getY() * 1.04D), (int) (color.getZ() * 1.04D), 64, poseStack);

                y += offset;

                RenderHelper.renderFlatQuad(vec34.add(x, y, z), 0.2F, 0.2F, 0.0F, (int) (color.getX() * 1.05D),
                    (int) (color.getY() * 1.05D), (int) (color.getZ() * 1.05D), 64, poseStack);
                RenderSystem.enableCull();
            }

            VRState.mc.getProfiler().pop();
        }
    }
}
