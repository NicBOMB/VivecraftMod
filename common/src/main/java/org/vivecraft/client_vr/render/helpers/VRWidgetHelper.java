package org.vivecraft.client_vr.render.helpers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Vector3f;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.gameplay.trackers.CameraTracker;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.function.Function;

public class VRWidgetHelper {
    private static final RandomSource random = RandomSource.create();
    public static boolean debug = false;

    public static void renderVRThirdPersonCamWidget() {

        if (ClientDataHolderVR.vrSettings.mixedRealityRenderCameraModel) {
            if ((ClientDataHolderVR.currentPass == RenderPass.LEFT || ClientDataHolderVR.currentPass == RenderPass.RIGHT) && (ClientDataHolderVR.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY || ClientDataHolderVR.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON)) {
                float f = 0.35F;

                if (ClientDataHolderVR.interactTracker.isInCamera() && !VRHotkeys.isMovingThirdPersonCam()) {
                    f *= 1.03F;
                }

                renderVRCameraWidget(-0.748F, -0.438F, -0.06F, f, RenderPass.THIRD, ClientDataHolderVR.thirdPersonCameraModel, ClientDataHolderVR.thirdPersonCameraDisplayModel, () ->
                {
                    ClientDataHolderVR.vrRenderer.framebufferMR.bindRead();
                    RenderSystem.setShaderTexture(0, ClientDataHolderVR.vrRenderer.framebufferMR.getColorTextureId());
                }, (face) ->
                {
                    if (face == Direction.NORTH) {
                        return DisplayFace.MIRROR;
                    } else {
                        return face == Direction.SOUTH ? DisplayFace.NORMAL : DisplayFace.NONE;
                    }
                });
            }
        }
    }

    public static void renderVRHandheldCameraWidget() {
        if (ClientDataHolderVR.currentPass != RenderPass.CAMERA && ClientDataHolderVR.cameraTracker.isVisible()) {
            float f = 0.25F;

            if (ClientDataHolderVR.interactTracker.isInHandheldCamera() && !ClientDataHolderVR.cameraTracker.isMoving()) {
                f *= 1.03F;
            }

            renderVRCameraWidget(-0.5F, -0.25F, -0.22F, f, RenderPass.CAMERA, CameraTracker.cameraModel, CameraTracker.cameraDisplayModel, () ->
            {
                if (VREffectsHelper.getNearOpaqueBlock(ClientDataHolderVR.vrPlayer.vrdata_world_render.getEye(RenderPass.CAMERA).getPosition(), ((GameRendererExtension) VRState.mc.gameRenderer).vivecraft$getMinClipDistance()) == null) {
                    ClientDataHolderVR.vrRenderer.cameraFramebuffer.bindRead();
                    RenderSystem.setShaderTexture(0, ClientDataHolderVR.vrRenderer.cameraFramebuffer.getColorTextureId());
                } else {
                    RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/black.png"));
                }
            }, (face) ->
            {
                return face == Direction.SOUTH ? DisplayFace.NORMAL : DisplayFace.NONE;
            });
        }
    }

    public static void renderVRCameraWidget(float offsetX, float offsetY, float offsetZ, float scale, RenderPass renderPass, ModelResourceLocation model, ModelResourceLocation displayModel, Runnable displayBindFunc, Function<Direction, DisplayFace> displayFaceFunc) {
        PoseStack poseStack = RenderSystem.getModelViewStack();
        poseStack.pushPose();
        poseStack.setIdentity();
        RenderHelper.applyVRModelView(ClientDataHolderVR.currentPass, poseStack);

        Vec3 widgetPosition = ClientDataHolderVR.vrPlayer.vrdata_world_render.getEye(renderPass).getPosition();
        Vec3 eye = RenderHelper.getSmoothCameraPosition(ClientDataHolderVR.currentPass, ClientDataHolderVR.vrPlayer.vrdata_world_render);
        Vec3 widgetOffset = widgetPosition.subtract(eye);

        poseStack.translate(widgetOffset.x, widgetOffset.y, widgetOffset.z);
        poseStack.mulPoseMatrix(ClientDataHolderVR.vrPlayer.vrdata_world_render.getEye(renderPass).getMatrix().toMCMatrix());
        scale = scale * ClientDataHolderVR.vrPlayer.vrdata_world_render.worldScale;
        poseStack.scale(scale, scale, scale);

        if (debug) {
            MethodHolder.rotateDeg(poseStack, 180.0F, 0.0F, 1.0F, 0.0F);
            RenderHelper.renderDebugAxes(0, 0, 0, 0.08F);
            MethodHolder.rotateDeg(poseStack, 180.0F, 0.0F, 1.0F, 0.0F);
        }

        poseStack.translate(offsetX, offsetY, offsetZ);
        RenderSystem.applyModelViewMatrix();

        BlockPos blockpos = BlockPos.containing(ClientDataHolderVR.vrPlayer.vrdata_world_render.getEye(renderPass).getPosition());
        int i = Utils.getCombinedLightWithMin(VRState.mc.level, blockpos, 0);

        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        if (VRState.mc.level != null) {
            RenderSystem.setShader(GameRenderer::getRendertypeEntityCutoutNoCullShader);
        } else {
            RenderSystem.setShader(GameRenderer::getPositionTexColorNormalShader);
        }
        VRState.mc.gameRenderer.lightTexture().turnOnLightLayer();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);

        PoseStack poseStack2 = new PoseStack();
        RenderHelper.applyVRModelView(ClientDataHolderVR.currentPass, poseStack2);
        poseStack2.last().pose().identity();
        poseStack2.last().normal().mul(new Matrix3f(ClientDataHolderVR.vrPlayer.vrdata_world_render.getEye(renderPass).getMatrix().toMCMatrix()));

        VRState.mc.getBlockRenderer().getModelRenderer().renderModel(poseStack2.last(), bufferbuilder, null, VRState.mc.getModelManager().getModel(model), 1.0F, 1.0F, 1.0F, i, OverlayTexture.NO_OVERLAY);
        tesselator.end();

        RenderSystem.disableBlend();
        displayBindFunc.run();
        RenderSystem.setShader(GameRenderer::getRendertypeEntitySolidShader);

        BufferBuilder bufferbuilder1 = tesselator.getBuilder();
        bufferbuilder1.begin(Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);

        for (BakedQuad bakedquad : VRState.mc.getModelManager().getModel(displayModel).getQuads(null, null, random)) {
            if (displayFaceFunc.apply(bakedquad.getDirection()) != DisplayFace.NONE && bakedquad.getSprite().contents().name().equals(new ResourceLocation("vivecraft:transparent"))) {
                int[] vertexList = bakedquad.getVertices();
                boolean flag = displayFaceFunc.apply(bakedquad.getDirection()) == DisplayFace.MIRROR;
                // make normals point up, so they are always bright
                // TODO: might break with shaders?
                Vector3f normal = poseStack.last().normal().transform(new Vector3f(0.0F, 1.0F, 0.0F));
                int j = LightTexture.pack(15, 15);
                int step = vertexList.length / 4;
                bufferbuilder1.vertex(
                        Float.intBitsToFloat(vertexList[0]),
                        Float.intBitsToFloat(vertexList[1]),
                        Float.intBitsToFloat(vertexList[2]))
                    .color(1.0F, 1.0F, 1.0F, 1.0F)
                    .uv(flag ? 1.0F : 0.0F, 1.0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(j)
                    .normal(normal.x, normal.y, normal.z)
                    .endVertex();
                bufferbuilder1.vertex(
                        Float.intBitsToFloat(vertexList[step]),
                        Float.intBitsToFloat(vertexList[step + 1]),
                        Float.intBitsToFloat(vertexList[step + 2]))
                    .color(1.0F, 1.0F, 1.0F, 1.0F)
                    .uv(flag ? 1.0F : 0.0F, 0.0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(j)
                    .normal(normal.x, normal.y, normal.z).endVertex();
                bufferbuilder1.vertex(
                        Float.intBitsToFloat(vertexList[step * 2]),
                        Float.intBitsToFloat(vertexList[step * 2 + 1]),
                        Float.intBitsToFloat(vertexList[step * 2 + 2]))
                    .color(1.0F, 1.0F, 1.0F, 1.0F)
                    .uv(flag ? 0.0F : 1.0F, 0.0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(j)
                    .normal(normal.x, normal.y, normal.z).endVertex();
                bufferbuilder1.vertex(
                        Float.intBitsToFloat(vertexList[step * 3]),
                        Float.intBitsToFloat(vertexList[step * 3 + 1]),
                        Float.intBitsToFloat(vertexList[step * 3 + 2]))
                    .color(1.0F, 1.0F, 1.0F, 1.0F)
                    .uv(flag ? 0.0F : 1.0F, 1.0F)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(j)
                    .normal(normal.x, normal.y, normal.z).endVertex();
            }
        }

        tesselator.end();
        VRState.mc.gameRenderer.lightTexture().turnOffLightLayer();
        RenderSystem.enableBlend();
        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public enum DisplayFace {
        NONE,
        NORMAL,
        MIRROR
    }
}
