package org.vivecraft.mixin.client_vr.renderer;


import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import com.mojang.math.Axis;
import net.minecraft.client.multiplayer.ClientLevel;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Triple;
import org.lwjgl.opengl.GL11C;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.mod_compat_vr.ShadersHelper;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client.Xevents;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_xr.render_pass.RenderPassType;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.extensions.ItemInHandRendererExtension;
import org.vivecraft.client_vr.extensions.LevelRendererExtension;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client.extensions.RenderTargetExtension;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.gameplay.trackers.BowTracker;
import org.vivecraft.client_vr.gameplay.trackers.TelescopeTracker;
import org.vivecraft.mixin.client.blaze3d.RenderSystemAccessor;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.render.XRCamera;
import org.vivecraft.client_vr.render.VRWidgetHelper;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client.utils.Utils;

import java.nio.file.Path;

@Mixin(GameRenderer.class)
public abstract class GameRendererVRMixin
        implements ResourceManagerReloadListener, AutoCloseable, GameRendererExtension {

    private static final ClientDataHolderVR DATA_HOLDER = ClientDataHolderVR.getInstance();
    @Unique
    public float minClipDistance = 0.02F;
    @Unique
    public Vec3 crossVec;
    @Unique
    public Matrix4f thirdPassProjectionMatrix = new Matrix4f();
    @Unique
    public boolean menuWorldFastTime;
    @Unique
    public boolean inwater;
    @Unique
    public boolean wasinwater;
    @Unique
    public boolean inportal;
    @Unique
    public boolean onfire;
    @Unique
    public float inBlock = 0.0F;
    @Unique
    public double rveX;
    @Unique
    public double rveY;
    @Unique
    public double rveZ;
    @Unique
    public double rvelastX;
    @Unique
    public double rvelastY;
    @Unique
    public double rvelastZ;
    @Unique
    public double rveprevX;
    @Unique
    public double rveprevY;
    @Unique
    public double rveprevZ;
    @Unique
    public float rveyaw;
    @Unique
    public float rvepitch;
    @Unique
    private float rvelastyaw;
    @Unique
    private float rvelastpitch;
    @Unique
    private float rveHeight;
    @Unique
    private boolean cached;
    @Unique
    private int polyblendsrca;
    @Unique
    private int polyblenddsta;
    @Unique
    private int polyblendsrcrgb;
    @Unique
    private int polyblenddstrgb;
    // private net.optifine.shaders.Program prog;
    @Unique
    private boolean polyblend;
//    @Unique
//    private boolean polytex;
//    @Unique
//    private boolean polylight;
    @Unique
    private boolean polycull;
    @Unique
    private Vec3i tpUnlimitedColor = new Vec3i(-83, -40, -26);
    @Unique
    private Vec3i tpLimitedColor = new Vec3i(-51, -87, -51);
    @Unique
    private Vec3i tpInvalidColor = new Vec3i(83, 83, 83);

    @Unique // TODO added by optifine...
    private float clipDistance = 128.0F;

    @Unique
    private PoseStack stack;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    private float renderDistance;

    @Shadow
    @Final
    private LightTexture lightTexture;
    @Shadow
    private float zoom;
    @Shadow
    private float zoomX;
    @Shadow
    private float zoomY;
    @Shadow
    private float fov;

    @Shadow
    private float oldFov;
    @Shadow
    @Final
    private RenderBuffers renderBuffers;
    @Shadow
    @Final
    public ItemInHandRenderer itemInHandRenderer;
    @Shadow
    private int tick;
    @Shadow
    private boolean renderHand;

    @Shadow
    public abstract Matrix4f getProjectionMatrix(double fov);

    @Shadow
    protected abstract double getFov(Camera mainCamera2, float partialTicks, boolean b);

    @Shadow
    public abstract void resetProjectionMatrix(Matrix4f projectionMatrix);

    @Shadow
    protected abstract void renderItemActivationAnimation(int i, int j, float par1);

    @Shadow
    public abstract void pick(float f);

    @Shadow
    private boolean effectActive;

    @Shadow
    private long lastActiveTime;

    @Shadow
    public abstract OverlayTexture overlayTexture();

    @Shadow
    @Final
    private Camera mainCamera;

    @Override
    public double getRveY() {
        return rveY;
    }

    @Override
    public float inBlock() {
        return inBlock;
    }

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/client/Camera"))
    Camera replaceCamera() {
        return new XRCamera();
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;level:Lnet/minecraft/client/multiplayer/ClientLevel;"), method = "pick")
    public ClientLevel appendCheck(Minecraft instance) {
        if (!VRState.vrRunning) {
            return instance.level;
        }
        return DATA_HOLDER.vrPlayer.vrdata_world_render == null ? null : instance.level;
    }

    @ModifyVariable(at = @At("STORE"), method = "pick(F)V", ordinal = 0)
    public Vec3 rayTrace(Vec3 original) {
        if (!VRState.vrRunning) {
            return original;
        }
        VRData vrworld = DATA_HOLDER.vrPlayer.getVRDataWorld();
        this.minecraft.hitResult = DATA_HOLDER.vrPlayer.rayTraceBlocksVR(
            vrworld,
            0,
            this.minecraft.gameMode.getPickRange(),
            false
        );
        this.crossVec = DATA_HOLDER.vrPlayer.AimedPointAtDistance(
            vrworld,
            0,
            this.minecraft.gameMode.getPickRange()
        );
        return vrworld.getController(0).getPosition();
    }

    @ModifyVariable(at = @At("STORE"), method = "pick(F)V", ordinal = 1)
    public Vec3 vrVec31(Vec3 original) {
        if (!VRState.vrRunning) {
            return original;
        }
        return DATA_HOLDER.vrPlayer.getVRDataWorld().getController(0).getDirection();
    }

    //TODO Vivecraft add riding check in case your hand is somewhere inappropriate

    @Inject(at = @At("HEAD"), method = "tickFov", cancellable = true)
    public void noFOVchangeInVR(CallbackInfo ci){
        if (!RenderPassType.isVanilla()) {
            this.oldFov = this.fov = 1;
            ci.cancel();
        }
    }

    @Inject(at = @At("HEAD"), method = "getFov(Lnet/minecraft/client/Camera;FZ)D", cancellable = true)
    public void fov(Camera camera, float f, boolean bl, CallbackInfoReturnable<Double> info) {
        if (this.minecraft.level == null || isInMenuRoom()) { // Vivecraft: using this on the main menu
            info.setReturnValue(Double.valueOf(this.minecraft.options.fov().get()));
        }
    }

    @Inject(at = @At("HEAD"), method = "getProjectionMatrix(D)Lorg/joml/Matrix4f;", cancellable = true)
    public void projection(double d, CallbackInfoReturnable<Matrix4f> info) {
        PoseStack posestack = new PoseStack();
        setupClipPlanes();
        switch (DATA_HOLDER.currentPass != null ? DATA_HOLDER.currentPass : RenderPass.CENTER){
            case LEFT -> posestack.mulPoseMatrix(DATA_HOLDER.vrRenderer.eyeproj[0]);
            case RIGHT -> posestack.mulPoseMatrix(DATA_HOLDER.vrRenderer.eyeproj[1]);
            case THIRD -> {
                if (DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY) {
                    posestack.mulPoseMatrix(
                        new Matrix4f().setPerspective(
                            DATA_HOLDER.vrSettings.mixedRealityFov * 0.01745329238474369F,
                            DATA_HOLDER.vrSettings.mixedRealityAspectRatio,
                            this.minClipDistance,
                            this.clipDistance
                        )
                    );
                } else {
                    posestack.mulPoseMatrix(
                        new Matrix4f().setPerspective(
                            DATA_HOLDER.vrSettings.mixedRealityFov * 0.01745329238474369F,
                            (float) this.minecraft.getWindow().getScreenWidth()
                                / (float) this.minecraft.getWindow().getScreenHeight(),
                            this.minClipDistance,
                            this.clipDistance
                        )
                    );
                }
                this.thirdPassProjectionMatrix = new Matrix4f(posestack.last().pose());
            }
            case CAMERA -> posestack.mulPoseMatrix(
                new Matrix4f().setPerspective(
                    DATA_HOLDER.vrSettings.handCameraFov * 0.01745329238474369F,
                    (float) DATA_HOLDER.vrRenderer.cameraFramebuffer.viewWidth
                        / (float) DATA_HOLDER.vrRenderer.cameraFramebuffer.viewHeight,
                    this.minClipDistance,
                    this.clipDistance
                )
            );
            case SCOPEL, SCOPER -> posestack.mulPoseMatrix(
                new Matrix4f().setPerspective(
                    70f / 8f * 0.01745329238474369F,
                    1,
                    0.05F,
                    this.clipDistance
                )
            );
            default -> {
                if (this.zoom != 1) {
                    posestack.translate(this.zoomX, -this.zoomY, 0);
                    posestack.scale(this.zoom, this.zoom, 1);
                }
                posestack.mulPoseMatrix(
                    new Matrix4f().setPerspective(
                        (float) d * 0.01745329238474369F,
                        (float) this.minecraft.getWindow().getScreenWidth()
                            / (float) this.minecraft.getWindow().getScreenHeight(),
                        0.05F,
                        this.clipDistance
                    )
                );
            }
        }
        info.setReturnValue(posestack.last().pose());
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isWindowActive()Z"), method = "render")
    public boolean focus(Minecraft instance) {
        return VRState.vrRunning || instance.isWindowActive();
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;pauseGame(Z)V"), method = "render")
    public void pause(Minecraft instance, boolean bl) {
        if (!VRState.vrRunning || DATA_HOLDER.currentPass == RenderPass.LEFT) {
            instance.pauseGame(bl);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/Util;getMillis()J"), method = "render")
    public long active() {
        if (!VRState.vrRunning || DATA_HOLDER.currentPass == RenderPass.LEFT) {
            return Util.getMillis();
        } else {
            return this.lastActiveTime;
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;viewport(IIII)V", shift = Shift.AFTER), method = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V")
    public void matrix(float partialTicks, long nanoTime, boolean renderWorldIn, CallbackInfo info) {
        this.resetProjectionMatrix(this.getProjectionMatrix(minecraft.options.fov().get()));
        RenderSystem.getModelViewStack().setIdentity();
        RenderSystem.applyModelViewMatrix();
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V"), method = "render")
    public PoseStack newStack(PoseStack poseStack) {
        this.stack = poseStack;
        return poseStack;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V", shift = Shift.AFTER), method = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V")
    public void renderoverlay(float f, long l, boolean bl, CallbackInfo ci) {
        switch (DATA_HOLDER.currentPass != null ? DATA_HOLDER.currentPass : RenderPass.CENTER){
            case THIRD, CAMERA -> {}
            default -> {
                if (VRState.vrRunning){
                    this.renderFaceOverlay(f, this.stack);
                }
            }
        };
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;effectActive:Z"), method = "render")
    public boolean effect(GameRenderer instance) {
        return this.effectActive && DATA_HOLDER.currentPass != RenderPass.THIRD;
    }

    @Inject(at = @At("HEAD"), method = "takeAutoScreenshot", cancellable = true)
    public void noScreenshotInMenu(Path path, CallbackInfo ci) {
        if (VRState.vrRunning && isInMenuRoom()) {
            ci.cancel();
        }
    }

    @Unique
    private boolean shouldDrawScreen = false;
    @Unique
    private boolean shouldDrawGui = false;

    @Override
    public void setShouldDrawScreen(boolean shouldDrawScreen) {
        this.shouldDrawScreen = shouldDrawScreen;
    }

    @Override
    public void setShouldDrawGui(boolean shouldDrawGui) {
        this.shouldDrawGui = shouldDrawGui;
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;", shift = Shift.BEFORE, ordinal = 6), method = "Lnet/minecraft/client/renderer/GameRenderer;render(FJZ)V", cancellable = true)
    public void mainMenu(float partialTicks, long nanoTime, boolean renderWorldIn, CallbackInfo info) {
        if (RenderPassType.isVanilla()) {
            return;
        }

        if (!renderWorldIn && shouldDrawScreen) {
            shouldDrawScreen = false;
            return;
        }
        if (!renderWorldIn || this.minecraft.level == null) {
            this.minecraft.getProfiler().push("MainMenu");
            GL11C.glDisable(GL11C.GL_STENCIL_TEST);

            PoseStack pMatrixStack = new PoseStack();
            applyVRModelView(DATA_HOLDER.currentPass, pMatrixStack);
            this.renderGuiLayer(partialTicks, true, pMatrixStack);

            if (KeyboardHandler.Showing) {
                if (DATA_HOLDER.vrSettings.physicalKeyboard) {
                    this.renderPhysicalKeyboard(partialTicks, pMatrixStack);
                } else {
                    this.render2D(
                        partialTicks,
                        KeyboardHandler.Framebuffer,
                        KeyboardHandler.Pos_room,
                        KeyboardHandler.Rotation_room,
                        DATA_HOLDER.vrSettings.menuAlwaysFollowFace && isInMenuRoom(),
                        pMatrixStack
                    );
                }
            }

            if ((DATA_HOLDER.currentPass != RenderPass.THIRD
                || DATA_HOLDER.vrSettings.mixedRealityRenderHands)
                && DATA_HOLDER.currentPass != RenderPass.CAMERA
            ) {
                this.renderVRHands(
                    partialTicks,
                    true,
                    true,
                    true,
                    true,
                    pMatrixStack
                );
            }
        }
        this.minecraft.getProfiler().pop();
        info.cancel();
    }

    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;getWindow()Lcom/mojang/blaze3d/platform/Window;", shift = Shift.AFTER, ordinal = 6), method = "render(FJZ)V", ordinal = 0, argsOnly = true)
    private boolean renderGui(boolean doRender) {
        if (RenderPassType.isVanilla()) {
            return doRender;
        }
        return shouldDrawGui;
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;renderItemActivationAnimation(IIF)V"), method = "render(FJZ)V")
    private void noItemActivationAnimationOnGUI(GameRenderer instance, int i, int j, float f) {
        if (RenderPassType.isVanilla()) {
            renderItemActivationAnimation(i, j, f);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;render(Lcom/mojang/blaze3d/vertex/PoseStack;F)V"), method = "render(FJZ)V")
    private void noGUIwithViewOnly(Gui instance, PoseStack poseStack, float f) {
        if (RenderPassType.isVanilla() || !ClientDataHolderVR.viewonly) {
            instance.render(poseStack, f);
        }
    }

    @Inject(at = @At("HEAD"), method = "renderConfusionOverlay", cancellable = true)
    private void noConfusionOverlayOnGUI(float f, CallbackInfo ci) {
        if (DATA_HOLDER.currentPass == RenderPass.GUI) {
            ci.cancel();
        }
    }

    @Redirect(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;translate(FFF)V"))
    private void noTranslateItem(PoseStack poseStack, float x, float y, float z) {
        if (RenderPassType.isVanilla()) {
            poseStack.translate(x, y, z);
        }
    }

    @Redirect(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"))
    private void noScaleItem(PoseStack poseStack, float x, float y, float z) {
        if (RenderPassType.isVanilla()) {
            poseStack.scale(x, y, z);
        }
    }

    @Inject(method = "renderItemActivationAnimation", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;scale(FFF)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void transformItem(int i, int j, float f, CallbackInfo ci, int k, float g, float h, float l, float m, float n, float o, float p, PoseStack posestack) {
        if (!RenderPassType.isVanilla()) {
            float sinN = Mth.sin(n) * 0.5F;
            posestack.translate(0, 0, sinN - 1.0);
            if (DATA_HOLDER.currentPass == RenderPass.THIRD) {
                sinN *= DATA_HOLDER.vrSettings.mixedRealityFov / 70.0;
            }
            applyVRModelView(DATA_HOLDER.currentPass, posestack);
            applystereo(DATA_HOLDER.currentPass, posestack);
            posestack.scale(sinN, sinN, sinN);
            VRData vrworld = DATA_HOLDER.vrPlayer.getVRDataWorld();
            posestack.mulPose(Axis.YP.rotationDegrees(-vrworld.getEye(DATA_HOLDER.currentPass).getYaw()));
            posestack.mulPose(Axis.XP.rotationDegrees(-vrworld.getEye(DATA_HOLDER.currentPass).getPitch()));
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;pick(F)V"), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void renderpick(GameRenderer g, float pPartialTicks) {
        if (RenderPassType.isVanilla()) {
            g.pick(pPartialTicks);
            return;
        }

        if (DATA_HOLDER.currentPass == RenderPass.LEFT) {
            this.pick(pPartialTicks);

            if (this.minecraft.hitResult != null && this.minecraft.hitResult.getType() != HitResult.Type.MISS) {
                this.crossVec = this.minecraft.hitResult.getLocation();
            }

            if (this.minecraft.screen == null) {
                DATA_HOLDER.teleportTracker.updateTeleportDestinations(
                    (GameRenderer) (Object) this,
                    this.minecraft,
                    this.minecraft.player
                );
            }
        }

        this.cacheRVEPos((LivingEntity) this.minecraft.getCameraEntity());
        this.setupRVE();
        this.setupOverlayStatus(pPartialTicks);
    }

    @Inject(at = @At("HEAD"), method = "bobHurt", cancellable = true)
    public void removeBobHurt(PoseStack poseStack, float f, CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }

    @Inject(method = "bobView", at = @At("HEAD"), cancellable = true)
    void cancelBobView(PoseStack matrixStack, float f, CallbackInfo ci) {
        if (!RenderPassType.isVanilla()) {
            ci.cancel();
        }
    }

    @ModifyVariable(at = @At(value = "STORE"), method = "renderLevel")
    public int reduceNauseaSpeed(int oldVal) {
        if (!RenderPassType.isVanilla()) {
            return oldVal / 5;
        } else {
            return oldVal;
        }
    }

    @ModifyVariable(at = @At(value = "STORE", ordinal = 1), ordinal = 3, method = "renderLevel")
    public float reduceNauseaAffect(float oldVal) {
        if (!RenderPassType.isVanilla()) {
            // scales down the effect from (1,0.65) to (1,0.9)
            return 1f - (1f - oldVal) * 0.25f;
        } else {
            return oldVal;
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiling/ProfilerFiller;popPush(Ljava/lang/String;)V", ordinal = 1), method = "renderLevel")
    public void noHandProfiler(ProfilerFiller instance, String s) {
        GL11C.glDisable(GL11C.GL_STENCIL_TEST);
        this.minecraft.getProfiler().popPush("ShadersEnd"); //TODO needed?
    }

    @Redirect(at = @At(value = "FIELD", target = "Lnet/minecraft/client/renderer/GameRenderer;renderHand:Z"), method = "renderLevel")
    public boolean noHandsVR(GameRenderer instance) {
        return RenderPassType.isVanilla() && renderHand;
    }

    @Inject(at = @At(value = "TAIL", shift = Shift.BEFORE), method = "renderLevel(FJLcom/mojang/blaze3d/vertex/PoseStack;)V")
    public void restoreVE(float f, long j, PoseStack p, CallbackInfo i) {
        if (RenderPassType.isVanilla()) {
            return;
        }
        this.restoreRVEPos((LivingEntity) this.minecraft.getCameraEntity());
    }

    private void setupOverlayStatus(float partialTicks) {
        this.inBlock = 0.0F;
        this.inwater = false;
        this.onfire = false;

        if (!this.minecraft.player.isSpectator() && !this.isInMenuRoom() && this.minecraft.player.isAlive()) {
            Vec3 pos = DATA_HOLDER.vrPlayer.getVRDataWorld().getEye(DATA_HOLDER.currentPass).getPosition();
            Triple<Float, BlockState, BlockPos> triple = ((ItemInHandRendererExtension) this.itemInHandRenderer).getNearOpaqueBlock(pos, (double) this.minClipDistance);

            if (triple != null && !Xevents.renderBlockOverlay(this.minecraft.player, new PoseStack(), triple.getMiddle(), triple.getRight())) {
                this.inBlock = triple.getLeft();
            } else {
                this.inBlock = 0.0F;
            }

            this.inwater = this.minecraft.player.isEyeInFluid(FluidTags.WATER) && !Xevents.renderWaterOverlay(this.minecraft.player, new PoseStack());
            this.onfire = DATA_HOLDER.currentPass != RenderPass.THIRD
                    && DATA_HOLDER.currentPass != RenderPass.CAMERA && this.minecraft.player.isOnFire() && !Xevents.renderFireOverlay(this.minecraft.player, new PoseStack());
        }
    }

    @Override
    public void setupRVE() {
        if (this.cached) {
            VRData.VRDevicePose data = DATA_HOLDER.vrPlayer.getVRDataWorld().getEye(DATA_HOLDER.currentPass);
            Vec3 f = data.getPosition();
            LivingEntity rve = (LivingEntity) this.minecraft.getCameraEntity();
            rve.setPosRaw(f.x, f.y, f.z);
            rve.xOld = f.x;
            rve.yOld = f.y;
            rve.zOld = f.z;
            rve.xo = f.x;
            rve.yo = f.y;
            rve.zo = f.z;
            rve.setXRot(-data.getPitch());
            rve.xRotO = rve.getXRot();
            rve.setYRot(data.getYaw());
            rve.yHeadRot = rve.getYRot();
            rve.yHeadRotO = rve.getYRot();
            rve.eyeHeight = 0;
        }
    }

    @Override
    public void cacheRVEPos(LivingEntity e) {
        if (this.minecraft.getCameraEntity() != null) {
            if (!this.cached) {
                this.rveX = e.getX();
                this.rveY = e.getY();
                this.rveZ = e.getZ();
                this.rvelastX = e.xOld;
                this.rvelastY = e.yOld;
                this.rvelastZ = e.zOld;
                this.rveprevX = e.xo;
                this.rveprevY = e.yo;
                this.rveprevZ = e.zo;
                this.rveyaw = e.yHeadRot;
                this.rvepitch = e.getXRot();
                this.rvelastyaw = e.yHeadRotO;
                this.rvelastpitch = e.xRotO;
                this.rveHeight = e.getEyeHeight();
                this.cached = true;
            }
        }
    }

    void renderMainMenuHand(int c, float partialTicks, boolean depthAlways, PoseStack poseStack) {
        this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(mainCamera, partialTicks, false)));
        poseStack.pushPose();
        poseStack.setIdentity();
        RenderSystem.enableDepthTest();
        RenderSystem.defaultBlendFunc();
        applyVRModelView(DATA_HOLDER.currentPass, poseStack);
        SetupRenderingAtController(c, poseStack);

        if (this.minecraft.getOverlay() == null) {
            RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
        }

        Tesselator tes = Tesselator.getInstance();

        if (depthAlways && c == 0) {
            RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        } else {
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        }

        Vec3i color = new Vec3i(64, 64, 64);
        byte alpha = -1;
        Vec3 start = new Vec3(0.0D, 0.0D, 0.0D);
        VRData vrworld = DATA_HOLDER.vrPlayer.getVRDataWorld();
        Vec3 dir = vrworld.getController(c).getDirection();
        Vec3 up = vrworld.getController(c).getCustomVector(new Vec3(0.0D, 1.0D, 0.0D));
        up = new Vec3(0.0D, 1.0D, 0.0D);
        dir = new Vec3(0.0D, 0.0D, -1.0D);
        Vec3 end = new Vec3(start.x - dir.x * 0.18D, start.y - dir.y * 0.18D, start.z - dir.z * 0.18D);

        if (this.minecraft.level != null) {
            float light = (float) this.minecraft.level.getMaxLocalRawBrightness(
                    BlockPos.containing(vrworld.hmd.getPosition()));

            int minLight = ShadersHelper.ShaderLight();

            if (light < (float) minLight) {
                light = (float) minLight;
            }

            float lightpercent = light / (float) this.minecraft.level.getMaxLightLevel();
            color = new Vec3i(Mth.floor(color.getX() * lightpercent), Mth.floor(color.getY() * lightpercent),
                    Mth.floor(color.getZ() * lightpercent));
        }
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        tes.getBuilder().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        this.renderBox(tes, start, end, -0.02F, 0.02F, -0.0125F, 0.0125F, up, color, alpha, poseStack);
        BufferUploader.drawWithShader(tes.getBuilder().end());
        poseStack.popPose();
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
    }

    private void renderVRHands(float partialTicks, boolean renderright, boolean renderleft, boolean menuhandright,
                               boolean menuhandleft, PoseStack poseStack) {
        this.minecraft.getProfiler().push("hands");
        // backup projection matrix, not doing that breaks sodium water on 1.19.3
        RenderSystem.backupProjectionMatrix();

        if (renderright) {
            this.minecraft.getItemRenderer();
            ClientDataHolderVR.ismainhand = true;

            if (menuhandright) {
                this.renderMainMenuHand(0, partialTicks, false, poseStack);
            } else {
                this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, partialTicks, true)));
                PoseStack posestack = new PoseStack();
                posestack.last().pose().identity();
                this.applyVRModelView(DATA_HOLDER.currentPass, posestack);
                this.renderVRHand_Main(posestack, partialTicks);
            }

            this.minecraft.getItemRenderer();
            ClientDataHolderVR.ismainhand = false;
        }

        if (renderleft) {
            if (menuhandleft) {
                this.renderMainMenuHand(1, partialTicks, false, poseStack);
            } else {
                this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, partialTicks, true)));
                PoseStack posestack = new PoseStack();
                posestack.last().pose().identity();
                this.applyVRModelView(DATA_HOLDER.currentPass, posestack);
                this.renderVRHand_Offhand(partialTicks, true, posestack);
            }
        }

        RenderSystem.restoreProjectionMatrix();
        this.minecraft.getProfiler().pop();
    }

    @Override
    public boolean isInWater() {
        return inwater;
    }

    @Override
    public boolean isInMenuRoom() {
        return this.minecraft.level == null ||
                this.minecraft.screen instanceof WinScreen ||
                this.minecraft.screen instanceof ReceivingLevelScreen ||
                this.minecraft.screen instanceof ProgressScreen ||
                this.minecraft.screen instanceof GenericDirtMessageScreen ||
                DATA_HOLDER.integratedServerLaunchInProgress ||
                this.minecraft.getOverlay() != null;
    }

    @Override
    public boolean willBeInMenuRoom(Screen newScreen) {
        return this.minecraft.level == null ||
                newScreen instanceof WinScreen ||
                newScreen instanceof ReceivingLevelScreen ||
                newScreen instanceof ProgressScreen ||
                newScreen instanceof GenericDirtMessageScreen ||
                DATA_HOLDER.integratedServerLaunchInProgress ||
                this.minecraft.getOverlay() != null;
    }

    @Override
    public Vec3 getControllerRenderPos(int c) {
        VRData vrworld = DATA_HOLDER.vrPlayer.getVRDataWorld();
        if (DATA_HOLDER.vrSettings.seated) {
            Vec3 out;
            Vec3 dir = vrworld.hmd.getDirection();

            if (this.minecraft.getCameraEntity() != null && this.minecraft.level != null) {
                dir = dir.yRot((float) Math.toRadians(c == 0 ? -35.0D : 35.0D));
                dir = new Vec3(dir.x, 0.0D, dir.z);
                dir = dir.normalize();
                RenderPass renderpass = RenderPass.CENTER;
                out = vrworld.getEye(renderpass).getPosition().add(
                    dir.x * 0.3D * (double) vrworld.worldScale,
                    -0.4D * (double) vrworld.worldScale,
                    dir.z * 0.3D * (double) vrworld.worldScale
                );

                if (TelescopeTracker.isTelescope(minecraft.player.getUseItem())) {
                    if (c == 0 && minecraft.player.getUsedItemHand() == InteractionHand.MAIN_HAND)
                        out = vrworld.eye0.getPosition().add(
                            vrworld.hmd.getDirection().scale(0.2 * vrworld.worldScale)
                        );
                    if (c == 1 && minecraft.player.getUsedItemHand() == InteractionHand.OFF_HAND)
                        out = vrworld.eye1.getPosition().add(
                            vrworld.hmd.getDirection().scale(0.2 * vrworld.worldScale)
                        );
                }

            } else {
                dir = dir.yRot((float) Math.toRadians(c == 0 ? -35.0D : 35.0D));
                dir = new Vec3(dir.x, 0.0D, dir.z);
                dir = dir.normalize();
                out = vrworld.hmd.getPosition().add(dir.x * 0.3D, -0.4D, dir.z * 0.3D);
            }

            return out;
        } else {
            return vrworld.getController(c).getPosition();
        }
    }

    @Override
    public Vec3 getCrossVec() {
        return crossVec;
    }

    @Override
    public void setMenuWorldFastTime(boolean b) {
        this.menuWorldFastTime = b;
    }

    @Override
    public void setupClipPlanes() {
        this.renderDistance = (float) (this.minecraft.options.getEffectiveRenderDistance() * 16);

//		if (Config.isFogOn()) { TODO
//			this.renderDistance *= 0.95F;
//		}

        this.clipDistance = this.renderDistance + 1024.0F;

    }

    @Override
    public float getMinClipDistance() {
        return this.minClipDistance;
    }

    @Override
    public float getClipDistance() {
        return this.clipDistance;
    }

    @Override
    public void applyVRModelView(RenderPass currentPass, PoseStack poseStack) {
        Matrix4f modelView = (DATA_HOLDER.vrPlayer.getVRDataWorld()
            .getEye(currentPass)
            .getMatrix()
            .transposed()
            .toMCMatrix()
        );
        poseStack.last().pose().mul(modelView);
        poseStack.last().normal().mul(new Matrix3f(modelView));
    }

    @Override
    public void renderDebugAxes(int r, int g, int b, float radius) {
        this.setupPolyRendering(true);
        RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
        this.renderCircle(new Vec3(0.0D, 0.0D, 0.0D), radius, 32, r, g, b, 255, 0);
        this.renderCircle(new Vec3(0.0D, 0.01D, 0.0D), radius * 0.75F, 32, r, g, b, 255, 0);
        this.renderCircle(new Vec3(0.0D, 0.02D, 0.0D), radius * 0.25F, 32, r, g, b, 255, 0);
        this.renderCircle(new Vec3(0.0D, 0.0D, 0.15D), radius * 0.5F, 32, r, g, b, 255, 2);
        this.setupPolyRendering(false);
    }

    public void renderCircle(Vec3 pos, float radius, int edges, int r, int g, int b, int a, int side) {
        Tesselator tes = Tesselator.getInstance();
        tes.getBuilder().begin(Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);
        tes.getBuilder().vertex(pos.x, pos.y, pos.z).color(r, g, b, a).endVertex();

        for (int i = 0; i < edges + 1; i++) {
            float startAngle = (float) i / (float) edges * (float) Math.PI * 2.0F;

            if (side == 0 || side == 1) { // y
                float x = (float) pos.x + (float) Math.cos(startAngle) * radius;
                float y = (float) pos.y;
                float z = (float) pos.z + (float) Math.sin(startAngle) * radius;
                tes.getBuilder().vertex(x, y, z).color(r, g, b, a).endVertex();
            } else if (side == 2 || side == 3) { // z
                float x = (float) pos.x + (float) Math.cos(startAngle) * radius;
                float y = (float) pos.y + (float) Math.sin(startAngle) * radius;
                float z = (float) pos.z;
                tes.getBuilder().vertex(x, y, z).color(r, g, b, a).endVertex();
            } else if (side == 4 || side == 5) { // x
                float x = (float) pos.x;
                float y = (float) pos.y + (float) Math.cos(startAngle) * radius;
                float z = (float) pos.z + (float) Math.sin(startAngle) * radius;
                tes.getBuilder().vertex(x, y, z).color(r, g, b, a).endVertex();
            }
        }

        tes.end();
    }

    private void setupPolyRendering(boolean enable) {
		// boolean shadersMod = false; // Config.isShaders(); TODO
        // boolean shadersModShadowPass = false;

        if (enable) {
            this.polyblendsrca = GlStateManager.BLEND.srcAlpha;
            this.polyblenddsta = GlStateManager.BLEND.dstAlpha;
            this.polyblendsrcrgb = GlStateManager.BLEND.srcRgb;
            this.polyblenddstrgb = GlStateManager.BLEND.dstRgb;
            this.polyblend = GL11C.glIsEnabled(GL11C.GL_BLEND);
            // this.polytex = GL11C.glIsEnabled(GL11C.GL_TEXTURE_2D);
            // this.polylight = false;
            this.polycull = GL11C.glIsEnabled(GL11C.GL_CULL_FACE);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            // GlStateManager._disableLighting();
            RenderSystem.disableCull();

            // if (shadersMod) {
            //     this.prog = Shaders.activeProgram; TODO
            //     Shaders.useProgram(Shaders.ProgramTexturedLit);
            // }
        } else {
            RenderSystem.blendFuncSeparate(this.polyblendsrcrgb, this.polyblenddstrgb, this.polyblendsrca,
                    this.polyblenddsta);

            if (!this.polyblend) { RenderSystem.disableBlend(); }

            // if (this.polytex) {}

            // if (this.polylight) { GlStateManager._enableLighting(); }

            if (this.polycull) { RenderSystem.enableCull(); }

//			if (shadersMod && this.polytex) {
//				Shaders.useProgram(this.prog); TODO
//			}
        }
    }

    @Override
    public void drawScreen(float par1, Screen screen, PoseStack poseStack) {
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.setIdentity();
        posestack.translate(0.0D, 0.0D, -2000.0D);
        RenderSystem.applyModelViewMatrix();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE);
        screen.render(poseStack, 0, 0, par1);
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE);
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();

        this.minecraft.getMainRenderTarget().bindRead();
        ((RenderTargetExtension) this.minecraft.getMainRenderTarget()).genMipMaps();
        this.minecraft.getMainRenderTarget().unbindRead();
    }

    @Override
    public boolean wasInWater() {
        return wasinwater;
    }

    @Override
    public void setWasInWater(boolean b) {
        this.wasinwater = b;
    }

    @Override
    public boolean isInPortal() {
        return this.inportal;
    }

    @Override
    public Matrix4f getThirdPassProjectionMatrix() {
        return thirdPassProjectionMatrix;
    }

    private void renderVRHand_Main(PoseStack posestack, float partialTicks) {
        posestack.pushPose();
        this.SetupRenderingAtController(0, posestack);
        ItemStack item = this.minecraft.player.getMainHandItem();
        ItemStack override = null; // this.minecraft.physicalGuiManager.getHeldItemOverride();

        if (override != null) {
            item = override;
        }

        if (DATA_HOLDER.climbTracker.isClimbeyClimb() && item.getItem() != Items.SHEARS) {
            item = override == null ? this.minecraft.player.getOffhandItem() : override;
        }

        if (BowTracker.isHoldingBow(this.minecraft.player, InteractionHand.MAIN_HAND)) {
            // do ammo override
            int c = 0;

            if (DATA_HOLDER.vrSettings.reverseShootingEye) {
                c = 1;
            }

            ItemStack ammo = this.minecraft.player.getProjectile(this.minecraft.player.getMainHandItem());

            if (ammo != ItemStack.EMPTY && !DATA_HOLDER.bowTracker.isNotched()) {
                item = ammo;
            } else {
                item = ItemStack.EMPTY;
            }
        } else if (BowTracker.isHoldingBow(this.minecraft.player, InteractionHand.OFF_HAND)
                && DATA_HOLDER.bowTracker.isNotched()) {
            int c = 0;

            if (DATA_HOLDER.vrSettings.reverseShootingEye) {
                c = 1;
            }

            item = ItemStack.EMPTY;
        }

        boolean translucent = false;

//		if (Config.isShaders()) { TODO
//			Shaders.beginHand(posestack, translucent);
//		} else {
        posestack.pushPose();
//		}

        this.lightTexture.turnOnLightLayer();
        MultiBufferSource.BufferSource buffer = this.renderBuffers.bufferSource();
        this.itemInHandRenderer.renderArmWithItem(
            this.minecraft.player,
            partialTicks,
            0.0F,
            InteractionHand.MAIN_HAND,
            this.minecraft.player.getAttackAnim(partialTicks),
            item,
            0.0F,
            posestack,
            buffer,
            this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, partialTicks)
        );
        buffer.endBatch();
        this.lightTexture.turnOffLightLayer();

//		if (Config.isShaders()) { TODO
//			Shaders.endHand(posestack);
//		} else {
        posestack.popPose();
//		}

        posestack.popPose();
    }

    }

    private void renderVRHand_Offhand(float partialTicks, boolean renderTeleport, PoseStack posestack) {
        // boolean shadersMod = Config.isShaders();TODO
        boolean shadersMod = false;
        boolean shadersModShadowPass = false;

//		if (shadersMod) {
//			shadersModShadowPass = Shaders.isShadowPass;
//		}

        posestack.pushPose();
        this.SetupRenderingAtController(1, posestack);
        ItemStack item = this.minecraft.player.getOffhandItem();
        ItemStack override = null;// this.minecraft.physicalGuiManager.getOffhandOverride();

        if (override != null) {
            item = override;
        }

        if (DATA_HOLDER.climbTracker.isClimbeyClimb()
                && (item == null || item.getItem() != Items.SHEARS)) {
            item = this.minecraft.player.getMainHandItem();
        }

        if (BowTracker.isHoldingBow(this.minecraft.player, InteractionHand.MAIN_HAND)) {
            int c = 1;

            if (DATA_HOLDER.vrSettings.reverseShootingEye) {
                c = 0;
            }

            item = this.minecraft.player.getMainHandItem();
        }

        boolean translucent = false;

//		if (Config.isShaders()) { TODO
//			Shaders.beginHand(posestack, translucent);
//		} else {
        posestack.pushPose();
//		}

        this.lightTexture.turnOnLightLayer();
        MultiBufferSource.BufferSource buffer = this.renderBuffers.bufferSource();
        this.itemInHandRenderer.renderArmWithItem(
            this.minecraft.player,
            partialTicks,
            0,
            InteractionHand.OFF_HAND,
            this.minecraft.player.getAttackAnim(partialTicks),
            item,
            0,
            posestack,
            buffer,
            this.minecraft.getEntityRenderDispatcher().getPackedLightCoords(this.minecraft.player, partialTicks)
        );
        buffer.endBatch();
        this.lightTexture.turnOffLightLayer();

//		if (Config.isShaders()) { TODO
//			Shaders.endHand(posestack);
//		} else {
        posestack.popPose();
//		}

        posestack.popPose();

        if (renderTeleport) {
            posestack.pushPose();
            posestack.setIdentity();
            this.applyVRModelView(DATA_HOLDER.currentPass, posestack);
//			net.optifine.shaders.Program program = Shaders.activeProgram; TODO

//			if (Config.isShaders()) {
//				Shaders.useProgram(Shaders.ProgramTexturedLit);
//			}

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
            );

            if (
                ClientNetworking.isLimitedSurvivalTeleport()
                && !DATA_HOLDER.vrPlayer.getFreeMove()
                && this.minecraft.gameMode.hasMissTime()
                && DATA_HOLDER.teleportTracker.vrMovementStyle.arcAiming
                && !DATA_HOLDER.bowTracker.isActive(this.minecraft.player)
            ) {
                posestack.pushPose();
                this.SetupRenderingAtController(1, posestack);
                Vec3 start = new Vec3(0, 0.005D, 0.03D);
                float max = 0.03F;
                float r;

                if (DATA_HOLDER.teleportTracker.isAiming()) {
                    r = 2 * (float) ((double) DATA_HOLDER.teleportTracker.getTeleportEnergy()
                            - 4 * DATA_HOLDER.teleportTracker.movementTeleportDistance) / 100 * max;
                } else {
                    r = 2 * DATA_HOLDER.teleportTracker.getTeleportEnergy() / 100 * max;
                }

                if (r < 0) {
                    r = 0;
                }
                RenderSystem.setShader(GameRenderer::getPositionColorShader);
                RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
                this.renderFlatQuad(start.add(0, 0.05001D, 0), r, r, 0, this.tpLimitedColor.getX(),
                        this.tpLimitedColor.getY(), this.tpLimitedColor.getZ(), 128, posestack);
                this.renderFlatQuad(start.add(0, 0.05D, 0), max, max, 0, this.tpLimitedColor.getX(),
                        this.tpLimitedColor.getY(), this.tpLimitedColor.getZ(), 50, posestack);
                posestack.popPose();
            }

            if (DATA_HOLDER.teleportTracker.isAiming()) {
                RenderSystem.enableDepthTest();

                if (DATA_HOLDER.teleportTracker.vrMovementStyle.arcAiming) {
                    this.renderTeleportArc(DATA_HOLDER.vrPlayer, posestack);
                }

            }

            RenderSystem.defaultBlendFunc();

//			if (Config.isShaders()) {
//				Shaders.useProgram(program);
//			}

            posestack.popPose();
        }
    }

    void render2D(float par1, RenderTarget framebuffer, Vec3 pos, org.vivecraft.common.utils.math.Matrix4f rot,
                  boolean depthAlways, PoseStack poseStack) {
        if (!DATA_HOLDER.bowTracker.isDrawing) {
            boolean inMenuRoom = this.isInMenuRoom();
            this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, par1, true)));
            poseStack.pushPose();
            poseStack.setIdentity();
            this.applyVRModelView(DATA_HOLDER.currentPass, poseStack);
            VRData vrworld = DATA_HOLDER.vrPlayer.getVRDataWorld();
            Vec3 eyepos = vrworld.getEye(DATA_HOLDER.currentPass).getPosition();
            Vec3 vec31 = new Vec3(0.0D, 0.0D, 0.0D);
            Vec3 guipos = VRPlayer.room_to_world_pos(pos, vrworld);
            org.vivecraft.common.utils.math.Matrix4f worldrot = org.vivecraft.common.utils.math.Matrix4f.rotationY(vrworld.rotation_radians);
            org.vivecraft.common.utils.math.Matrix4f guirot = org.vivecraft.common.utils.math.Matrix4f.multiply(worldrot, rot);

            poseStack.translate((float) (guipos.x - eyepos.x), (float) (guipos.y - eyepos.y), (float) (guipos.z - eyepos.z));
            poseStack.mulPoseMatrix(guirot.toMCMatrix());
            poseStack.translate((float) vec31.x, (float) vec31.y, (float) vec31.z);
            float scaleFactor = GuiHandler.guiScale * vrworld.worldScale;
            poseStack.scale(scaleFactor, scaleFactor, scaleFactor);

            framebuffer.bindRead();
            RenderSystem.disableCull();
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, framebuffer.getColorTextureId());

            float[] color = new float[]{1, 1, 1, 1};
            if (!inMenuRoom) {
                if (this.minecraft.screen == null) {
                    color[3] = DATA_HOLDER.vrSettings.hudOpacity;
                }

                if (this.minecraft.player != null && this.minecraft.player.isShiftKeyDown()) {
                    color[3] *= 0.75F;
                }

                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
                        GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE_MINUS_DST_ALPHA,
                        GlStateManager.DestFactor.ONE);
            } else {
                RenderSystem.disableBlend();
            }

            if (depthAlways) {
                RenderSystem.depthFunc(GL11C.GL_ALWAYS);
            } else {
                RenderSystem.depthFunc(GL11C.GL_LEQUAL);
            }

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();


            if (this.minecraft.level != null) {
                if (((ItemInHandRendererExtension) this.itemInHandRenderer).isInsideOpaqueBlock(eyepos)) {
                    eyepos = vrworld.hmd.getPosition();
                }

                int i = ShadersHelper.ShaderLight();
                int j = Utils.getCombinedLightWithMin(this.minecraft.level, BlockPos.containing(eyepos), i);
                this.drawSizedQuadWithLightmap((float) this.minecraft.getWindow().getGuiScaledWidth(),
                        (float) this.minecraft.getWindow().getGuiScaledHeight(), 1.5F, j, color,
                        poseStack.last().pose());
            } else {
                this.drawSizedQuad((float) this.minecraft.getWindow().getGuiScaledWidth(),
                        (float) this.minecraft.getWindow().getGuiScaledHeight(), 1.5F, color, poseStack.last().pose());
            }

            RenderSystem.defaultBlendFunc();
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
            RenderSystem.enableCull();

            poseStack.popPose();
        }
    }

    void renderPhysicalKeyboard(float partialTicks, PoseStack poseStack) {
        if (!DATA_HOLDER.bowTracker.isDrawing) {
            this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, partialTicks, true)));
            poseStack.pushPose();
            poseStack.setIdentity();
            // RenderSystem.enableRescaleNormal();
            // Lighting.setupFor3DItems();

            this.minecraft.getProfiler().push("applyPhysicalKeyboardModelView");
            VRData vrworld = DATA_HOLDER.vrPlayer.getVRDataWorld();
            Vec3 eyepos = vrworld.getEye(DATA_HOLDER.currentPass).getPosition();
            Vec3 guipos = VRPlayer.room_to_world_pos(KeyboardHandler.Pos_room, vrworld);
            org.vivecraft.common.utils.math.Matrix4f worldrot = org.vivecraft.common.utils.math.Matrix4f.rotationY(vrworld.rotation_radians);
            org.vivecraft.common.utils.math.Matrix4f guirot = org.vivecraft.common.utils.math.Matrix4f.multiply(worldrot, KeyboardHandler.Rotation_room);
            poseStack.mulPoseMatrix(vrworld.getEye(DATA_HOLDER.currentPass).getMatrix().transposed().toMCMatrix());
            poseStack.translate((float) (guipos.x - eyepos.x), (float) (guipos.y - eyepos.y), (float) (guipos.z - eyepos.z));
            // GlStateManager._multMatrix(guirot.transposed().toFloatBuffer());
            poseStack.mulPoseMatrix(guirot.toMCMatrix());
            poseStack.scale(vrworld.worldScale, vrworld.worldScale, vrworld.worldScale);
            this.minecraft.getProfiler().pop();

            KeyboardHandler.physicalKeyboard.render(poseStack);
            // Lighting.turnOff();
            // RenderSystem.disableRescaleNormal();
            poseStack.popPose();
            RenderSystem.applyModelViewMatrix();
        }
    }

    private void renderGuiLayer(float par1, boolean depthAlways, PoseStack pMatrix) {
        if (!DATA_HOLDER.bowTracker.isDrawing && !RadialHandler.isShowing() && (this.minecraft.screen != null || !this.minecraft.options.hideGui)) {
            minecraft.getProfiler().push("GuiLayer");
            // cache fog distance
            float fogStart = RenderSystem.getShaderFogStart();

            // remove nausea effect from projection matrix, for vanilla, nd posestack for iris
            this.resetProjectionMatrix(this.getProjectionMatrix(this.getFov(this.mainCamera, par1, true)));
            pMatrix.pushPose();
            pMatrix.setIdentity();
            this.applyVRModelView(DATA_HOLDER.currentPass, pMatrix);

            boolean inMenuRoom = this.isInMenuRoom();

            // render the screen always on top in the menu room to prevent z fighting
            depthAlways |= inMenuRoom;

            PoseStack poseStack = RenderSystem.getModelViewStack();
            poseStack.pushPose();
            poseStack.setIdentity();
            RenderSystem.applyModelViewMatrix();

            if (inMenuRoom) {
                pMatrix.pushPose();
                VRData vrworld = DATA_HOLDER.vrPlayer.getVRDataWorld();
                Vec3 eye = vrworld.getEye(DATA_HOLDER.currentPass).getPosition();
                pMatrix.translate((vrworld.origin.x - eye.x), (vrworld.origin.y - eye.y), (vrworld.origin.z - eye.z));

                // remove world rotation or the room doesn't align with the screen
                pMatrix.mulPose(Axis.YN.rotation(-vrworld.rotation_radians));

                //System.out.println(eye + " eye");
                //System.out.println(vrworld.origin + " world");

//						if (DATA_HOLDER.menuWorldRenderer != null
//								&& DATA_HOLDER.menuWorldRenderer.isReady()) {
//							try {
//								//this.renderTechjarsAwesomeMainMenuRoom();
//							} catch (Exception exception) {
//								System.out.println("Error rendering main menu world, unloading to prevent more errors");
//								exception.printStackTrace();
//								DATA_HOLDER.menuWorldRenderer.destroy();
//							}
//						} else {
                this.renderJrbuddasAwesomeMainMenuRoomNew(pMatrix);
//						}
                pMatrix.popPose();
            }

            Vec3 guipos = GuiHandler.applyGUIModelView(DATA_HOLDER.currentPass, pMatrix);
            GuiHandler.guiFramebuffer.bindRead();
            RenderSystem.disableCull();
            RenderSystem.setShaderTexture(0, GuiHandler.guiFramebuffer.getColorTextureId());

            float[] color = new float[]{1.0F, 1.0F, 1.0F, 1.0F};
            if (!inMenuRoom) {
                if (this.minecraft.screen == null) {
                    color[3] = DATA_HOLDER.vrSettings.hudOpacity;
                } else {
                    // disable fog for menus
                    RenderSystem.setShaderFogStart(Float.MAX_VALUE);
                }

                if (this.minecraft.player != null && this.minecraft.player.isShiftKeyDown()) {
                    color[3] *= 0.75F;
                }

                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                    GlStateManager.SourceFactor.ONE_MINUS_DST_ALPHA,
                    GlStateManager.DestFactor.ONE
                );
                if (DATA_HOLDER.vrSettings.shaderGUIRender == VRSettings.ShaderGUIRender.BEFORE_TRANSLUCENT_SOLID && ShadersHelper.isShaderActive()) {
                    RenderSystem.disableBlend();
                }
            } else {
                RenderSystem.disableBlend();
            }

            if (depthAlways) {
                RenderSystem.depthFunc(GL11C.GL_ALWAYS);
            } else {
                RenderSystem.depthFunc(GL11C.GL_LEQUAL);
            }

            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();

            // RenderSystem.disableLighting();

            if (this.minecraft.level != null) {
                if (((ItemInHandRendererExtension) this.itemInHandRenderer).isInsideOpaqueBlock(guipos)) {
                    guipos = DATA_HOLDER.vrPlayer.getVRDataWorld().hmd.getPosition();
                }

                int minLight = ShadersHelper.ShaderLight();
                int i = Utils.getCombinedLightWithMin(this.minecraft.level, BlockPos.containing(guipos), minLight);
                this.drawSizedQuadWithLightmap((float) this.minecraft.getWindow().getGuiScaledWidth(),
                        (float) this.minecraft.getWindow().getGuiScaledHeight(), 1.5F, i, color,
                        pMatrix.last().pose());
            } else {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                this.drawSizedQuad((float) this.minecraft.getWindow().getGuiScaledWidth(),
                        (float) this.minecraft.getWindow().getGuiScaledHeight(), 1.5F, color,
                        pMatrix.last().pose());
            }

            // RenderSystem.blendColor(1.0F, 1.0F, 1.0F, 1.0F);
            // reset fog
            RenderSystem.setShaderFogStart(fogStart);
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
            RenderSystem.enableDepthTest();
            // RenderSystem.defaultAlphaFunc();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableCull();
            pMatrix.popPose();

            poseStack.popPose();
            RenderSystem.applyModelViewMatrix();
            minecraft.getProfiler().pop();
        }
    }

    public void SetupRenderingAtController(int controller, PoseStack matrix) {
        VRData vrworld = DATA_HOLDER.vrPlayer.getVRDataWorld();
        Vec3 aimSource = this.getControllerRenderPos(controller).subtract(
            vrworld.getEye(DATA_HOLDER.currentPass).getPosition()
        );

        // move from head to hand origin
        matrix.translate((float) aimSource.x, (float) aimSource.y, (float) aimSource.z);

        float sc = vrworld.worldScale;
        if (minecraft.level != null && TelescopeTracker.isTelescope(minecraft.player.getUseItem())) {
            matrix.mulPoseMatrix(vrworld.hmd.getMatrix().inverted().transposed().toMCMatrix());
            MethodHolder.rotateDegXp(matrix, 90);
            matrix.translate(controller == 0 ? 0.075 * sc : -0.075 * sc, -0.025 * sc, 0.0325 * sc);
        } else {
            matrix.mulPoseMatrix(vrworld.getController(controller).getMatrix().inverted().transposed().toMCMatrix());
        }

        matrix.scale(sc, sc, sc);

    }

    public void renderFlatQuad(Vec3 pos, float width, float height, float yaw, int r, int g, int b, int a,
                               PoseStack poseStack) {
        Tesselator tes = Tesselator.getInstance();
        tes.getBuilder().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);

        Vec3 lr = new Vec3(-width / 2, 0, height / 2).yRot((float) Math.toRadians(-yaw));
        Vec3 ls = new Vec3(-width / 2, 0, -height / 2).yRot((float) Math.toRadians(-yaw));
        Vec3 lt = new Vec3(width / 2, 0, -height / 2).yRot((float) Math.toRadians(-yaw));
        Vec3 lu = new Vec3(width / 2, 0, height / 2).yRot((float) Math.toRadians(-yaw));

        Matrix4f mat = poseStack.last().pose();
        tes.getBuilder().vertex(
            mat,
            (float) (pos.x + lr.x),
            (float) pos.y,
            (float) (pos.z + lr.z)
        ).color(r, g, b, a).normal(0, 1, 0).endVertex();
        tes.getBuilder().vertex(
            mat,
            (float) (pos.x + ls.x),
            (float) pos.y,
            (float) (pos.z + ls.z)
        ).color(r, g, b, a).normal(0, 1, 0).endVertex();
        tes.getBuilder().vertex(
            mat,
            (float) (pos.x + lt.x),
            (float) pos.y,
            (float) (pos.z + lt.z)
        ).color(r, g, b, a).normal(0, 1, 0).endVertex();
        tes.getBuilder().vertex(
            mat,
            (float) (pos.x + lu.x),
            (float) pos.y,
            (float) (pos.z + lu.z)
        ).color(r, g, b, a).normal(0, 1, 0).endVertex();

        tes.end();

    }

    private void renderBox(Tesselator tes, Vec3 start, Vec3 end, float minX, float maxX, float minY, float maxY,
                           Vec3 up, Vec3i color, byte alpha, PoseStack poseStack) {
        Vec3 forward = start.subtract(end).normalize();
        Vec3 right = forward.cross(up);
        up = right.cross(forward);
        Vec3 left = new Vec3(right.x * (double) minX, right.y * (double) minX, right.z * (double) minX);
        right = right.scale((double) maxX);
        Vec3 down = new Vec3(up.x * (double) minY, up.y * (double) minY, up.z * (double) minY);
        up = up.scale((double) maxY);
        org.vivecraft.common.utils.lwjgl.Vector3f forwardNormal = Utils.convertToVector3f(forward);
        org.vivecraft.common.utils.lwjgl.Vector3f upNormal = Utils.convertToVector3f(up.normalize());
        org.vivecraft.common.utils.lwjgl.Vector3f rightNormal = Utils.convertToVector3f(right.normalize());
        Vec3 backRightBottom = start.add(right.x + down.x, right.y + down.y, right.z + down.z);
        Vec3 backRightTop = start.add(right.x + up.x, right.y + up.y, right.z + up.z);
        Vec3 backLeftBottom = start.add(left.x + down.x, left.y + down.y, left.z + down.z);
        Vec3 backLeftTop = start.add(left.x + up.x, left.y + up.y, left.z + up.z);
        Vec3 frontRightBottom = end.add(right.x + down.x, right.y + down.y, right.z + down.z);
        Vec3 frontRightTop = end.add(right.x + up.x, right.y + up.y, right.z + up.z);
        Vec3 frontLeftBottom = end.add(left.x + down.x, left.y + down.y, left.z + down.z);
        Vec3 frontLeftTop = end.add(left.x + up.x, left.y + up.y, left.z + up.z);
        BufferBuilder b = tes.getBuilder();
        Matrix4f mat = poseStack.last().pose();
        b.vertex(mat, (float) backRightBottom.x, (float) backRightBottom.y, (float) backRightBottom.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(forwardNormal.x, forwardNormal.y, forwardNormal.z)
            .endVertex();
        b.vertex(mat, (float) backLeftBottom.x, (float) backLeftBottom.y, (float) backLeftBottom.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(forwardNormal.x, forwardNormal.y, forwardNormal.z)
            .endVertex();
        b.vertex(mat, (float) backLeftTop.x, (float) backLeftTop.y, (float) backLeftTop.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(forwardNormal.x, forwardNormal.y, forwardNormal.z)
            .endVertex();
        b.vertex(mat, (float) backRightTop.x, (float) backRightTop.y, (float) backRightTop.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(forwardNormal.x, forwardNormal.y, forwardNormal.z)
            .endVertex();
        b.vertex(mat, (float) frontLeftBottom.x, (float) frontLeftBottom.y, (float) frontLeftBottom.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(-forwardNormal.x, -forwardNormal.y, -forwardNormal.z)
            .endVertex();
        b.vertex(mat, (float) frontRightBottom.x, (float) frontRightBottom.y, (float) frontRightBottom.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(-forwardNormal.x, -forwardNormal.y, -forwardNormal.z)
            .endVertex();
        b.vertex(mat, (float) frontRightTop.x, (float) frontRightTop.y, (float) frontRightTop.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(-forwardNormal.x, -forwardNormal.y, -forwardNormal.z)
            .endVertex();
        b.vertex(mat, (float) frontLeftTop.x, (float) frontLeftTop.y, (float) frontLeftTop.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(-forwardNormal.x, -forwardNormal.y, -forwardNormal.z)
            .endVertex();
        b.vertex(mat, (float) frontRightBottom.x, (float) frontRightBottom.y, (float) frontRightBottom.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(rightNormal.x, rightNormal.y, rightNormal.z)
            .endVertex();
        b.vertex(mat, (float) backRightBottom.x, (float) backRightBottom.y, (float) backRightBottom.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(rightNormal.x, rightNormal.y, rightNormal.z)
            .endVertex();
        b.vertex(mat, (float) backRightTop.x, (float) backRightTop.y, (float) backRightTop.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(rightNormal.x, rightNormal.y, rightNormal.z)
            .endVertex();
        b.vertex(mat, (float) frontRightTop.x, (float) frontRightTop.y, (float) frontRightTop.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(rightNormal.x, rightNormal.y, rightNormal.z)
            .endVertex();
        b.vertex(mat, (float) backLeftBottom.x, (float) backLeftBottom.y, (float) backLeftBottom.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(-rightNormal.x, -rightNormal.y, -rightNormal.z)
            .endVertex();
        b.vertex(mat, (float) frontLeftBottom.x, (float) frontLeftBottom.y, (float) frontLeftBottom.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(-rightNormal.x, -rightNormal.y, -rightNormal.z)
            .endVertex();
        b.vertex(mat, (float) frontLeftTop.x, (float) frontLeftTop.y, (float) frontLeftTop.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(-rightNormal.x, -rightNormal.y, -rightNormal.z)
            .endVertex();
        b.vertex(mat, (float) backLeftTop.x, (float) backLeftTop.y, (float) backLeftTop.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(-rightNormal.x, -rightNormal.y, -rightNormal.z)
            .endVertex();
        b.vertex(mat, (float) backLeftTop.x, (float) backLeftTop.y, (float) backLeftTop.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(upNormal.x, upNormal.y, upNormal.z)
            .endVertex();
        b.vertex(mat, (float) frontLeftTop.x, (float) frontLeftTop.y, (float) frontLeftTop.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(upNormal.x, upNormal.y, upNormal.z)
            .endVertex();
        b.vertex(mat, (float) frontRightTop.x, (float) frontRightTop.y, (float) frontRightTop.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(upNormal.x, upNormal.y, upNormal.z)
            .endVertex();
        b.vertex(mat, (float) backRightTop.x, (float) backRightTop.y, (float) backRightTop.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(upNormal.x, upNormal.y, upNormal.z)
            .endVertex();
        b.vertex(mat, (float) frontLeftBottom.x, (float) frontLeftBottom.y, (float) frontLeftBottom.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(-upNormal.x, -upNormal.y, -upNormal.z)
            .endVertex();
        b.vertex(mat, (float) backLeftBottom.x, (float) backLeftBottom.y, (float) backLeftBottom.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(-upNormal.x, -upNormal.y, -upNormal.z)
            .endVertex();
        b.vertex(mat, (float) backRightBottom.x, (float) backRightBottom.y, (float) backRightBottom.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(-upNormal.x, -upNormal.y, -upNormal.z)
            .endVertex();
        b.vertex(mat, (float) frontRightBottom.x, (float) frontRightBottom.y, (float) frontRightBottom.z)
            .color(color.getX(), color.getY(), color.getZ(), alpha)
            .normal(-upNormal.x, -upNormal.y, -upNormal.z)
            .endVertex();
    }

    private void renderJrbuddasAwesomeMainMenuRoomNew(PoseStack pMatrixStack) {
        int repeat = 4;
        float height = 2.5F;
        float oversize = 1.3F;
        Vector2f area = DATA_HOLDER.vr.getPlayAreaSize();
        if (area == null)
            area = new Vector2f(2, 2);

        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        RenderSystem.depthMask(true); //TODO temp fix
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, Screen.BACKGROUND_LOCATION);
        RenderSystem.setShaderColor(1, 1, 1, 1);
        pMatrixStack.pushPose();
        float width = area.x + oversize;
        float length = area.y + oversize;
        pMatrixStack.translate(-width / 2, 0, -length / 2);

        Matrix4f matrix4f = pMatrixStack.last().pose();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);

        float r, g, b, a;
        r = g = b = 0.8f;
        a = 1.0f;

        bufferbuilder.vertex(matrix4f, 0, 0, 0).uv(0, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();
        bufferbuilder.vertex(matrix4f, 0, 0, length).uv(0, repeat * length).color(r, g, b, a).normal(0, 1, 0).endVertex();
        bufferbuilder.vertex(matrix4f, width, 0, length).uv(repeat * width, repeat * length).color(r, g, b, a).normal(0, 1, 0).endVertex();
        bufferbuilder.vertex(matrix4f, width, 0, 0).uv(repeat * width, 0).color(r, g, b, a).normal(0, 1, 0).endVertex();

        bufferbuilder.vertex(matrix4f, 0, height, length).uv(0, 0).color(r, g, b, a).normal(0, -1, 0).endVertex();
        bufferbuilder.vertex(matrix4f, 0, height, 0).uv(0, repeat * length).color(r, g, b, a).normal(0, -1, 0).endVertex();
        bufferbuilder.vertex(matrix4f, width, height, 0).uv(repeat * width, repeat * length).color(r, g, b, a).normal(0, -1, 0).endVertex();
        bufferbuilder.vertex(matrix4f, width, height, length).uv(repeat * width, 0).color(r, g, b, a).normal(0, -1, 0).endVertex();

        bufferbuilder.vertex(matrix4f, 0, 0, 0).uv(0, 0).color(r, g, b, a).normal(1, 0, 0).endVertex();
        bufferbuilder.vertex(matrix4f, 0, height, 0).uv(0, repeat * height).color(r, g, b, a).normal(1, 0, 0).endVertex();
        bufferbuilder.vertex(matrix4f, 0, height, length).uv(repeat * length, repeat * height).color(r, g, b, a).normal(1, 0, 0).endVertex();
        bufferbuilder.vertex(matrix4f, 0, 0, length).uv(repeat * length, 0).color(r, g, b, a).normal(1, 0, 0).endVertex();

        bufferbuilder.vertex(matrix4f, width, 0, 0).uv(0, 0).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        bufferbuilder.vertex(matrix4f, width, 0, length).uv(repeat * length, 0).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        bufferbuilder.vertex(matrix4f, width, height, length).uv(repeat * length, repeat * height).color(r, g, b, a).normal(-1, 0, 0).endVertex();
        bufferbuilder.vertex(matrix4f, width, height, 0).uv(0, repeat * height).color(r, g, b, a).normal(-1, 0, 0).endVertex();

        bufferbuilder.vertex(matrix4f, 0, 0, 0).uv(0, 0).color(r, g, b, a).normal(0, 0, 1).endVertex();
        bufferbuilder.vertex(matrix4f, width, 0, 0).uv(repeat * width, 0).color(r, g, b, a).normal(0, 0, 1).endVertex();
        bufferbuilder.vertex(matrix4f, width, height, 0).uv(repeat * width, repeat * height).color(r, g, b, a).normal(0, 0, 1).endVertex();
        bufferbuilder.vertex(matrix4f, 0, height, 0).uv(0, repeat * height).color(r, g, b, a).normal(0, 0, 1).endVertex();

        bufferbuilder.vertex(matrix4f, 0, 0, length).uv(0, 0).color(r, g, b, a).normal(0, 0, -1).endVertex();
        bufferbuilder.vertex(matrix4f, 0, height, length).uv(0, repeat * height).color(r, g, b, a).normal(0, 0, -1).endVertex();
        bufferbuilder.vertex(matrix4f, width, height, length).uv(repeat * width, repeat * height).color(r, g, b, a).normal(0, 0, -1).endVertex();
        bufferbuilder.vertex(matrix4f, width, 0, length).uv(repeat * width, 0).color(r, g, b, a).normal(0, 0, -1).endVertex();

        BufferUploader.drawWithShader(bufferbuilder.end());
        pMatrixStack.popPose();

    }

    public void renderVRFabulous(float partialTicks, LevelRenderer worldrendererin, boolean menuHandRight,
                                 boolean menuHandLeft, PoseStack posestack) {
        if (DATA_HOLDER.currentPass == RenderPass.SCOPEL || DATA_HOLDER.currentPass == RenderPass.SCOPER)
            return;
        this.minecraft.getProfiler().popPush("VR");
        this.renderCrosshairAtDepth(!DATA_HOLDER.vrSettings.useCrosshairOcclusion, posestack);
        this.minecraft.getMainRenderTarget().unbindWrite();
        ((LevelRendererExtension) worldrendererin).getAlphaSortVROccludedFramebuffer().clear(Minecraft.ON_OSX);
        ((LevelRendererExtension) worldrendererin).getAlphaSortVROccludedFramebuffer().copyDepthFrom(this.minecraft.getMainRenderTarget());
        ((LevelRendererExtension) worldrendererin).getAlphaSortVROccludedFramebuffer().bindWrite(true);

        if (this.shouldOccludeGui()) {
            this.renderGuiLayer(partialTicks, false, posestack);
            this.renderVrShadow(partialTicks, false, posestack);

            if (KeyboardHandler.Showing) {
                if (DATA_HOLDER.vrSettings.physicalKeyboard) {
                    this.renderPhysicalKeyboard(partialTicks, posestack);
                } else {
                    this.render2D(partialTicks, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
                            KeyboardHandler.Rotation_room, false, posestack);
                }
            }

            if (RadialHandler.isShowing()) {
                this.render2D(partialTicks, RadialHandler.Framebuffer, RadialHandler.Pos_room,
                        RadialHandler.Rotation_room, false, posestack);
            }
        }

        ((LevelRendererExtension) worldrendererin).getAlphaSortVRUnoccludedFramebuffer().clear(Minecraft.ON_OSX);
        ((LevelRendererExtension) worldrendererin).getAlphaSortVRUnoccludedFramebuffer().bindWrite(true);

        if (!this.shouldOccludeGui()) {
            this.renderGuiLayer(partialTicks, false, posestack);
            this.renderVrShadow(partialTicks, false, posestack);

            if (KeyboardHandler.Showing) {
                if (DATA_HOLDER.vrSettings.physicalKeyboard) {
                    this.renderPhysicalKeyboard(partialTicks, posestack);
                } else {
                    this.render2D(partialTicks, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
                            KeyboardHandler.Rotation_room, false, posestack);
                }
            }

            if (RadialHandler.isShowing()) {
                this.render2D(partialTicks, RadialHandler.Framebuffer, RadialHandler.Pos_room,
                        RadialHandler.Rotation_room, false, posestack);
            }
        }

        this.renderVRSelfEffects(partialTicks);
        VRWidgetHelper.renderVRThirdPersonCamWidget();
        VRWidgetHelper.renderVRHandheldCameraWidget();
        boolean should = this.shouldRenderHands();
        this.renderVRHands(partialTicks, should && menuHandRight, should && menuHandLeft, true, true, posestack);
        ((LevelRendererExtension) worldrendererin).getAlphaSortVRHandsFramebuffer().clear(Minecraft.ON_OSX);
        ((LevelRendererExtension) worldrendererin).getAlphaSortVRHandsFramebuffer().copyDepthFrom(this.minecraft.getMainRenderTarget());
        ((LevelRendererExtension) worldrendererin).getAlphaSortVRHandsFramebuffer().bindWrite(true);
        this.renderVRHands(partialTicks, should && !menuHandRight, should && !menuHandLeft, false, false, posestack);
        RenderSystem.defaultBlendFunc();
        // RenderSystem.defaultAlphaFunc();
        RenderSystem.setShaderColor(1, 1, 1, 1);
        // Lighting.turnBackOn();
        // Lighting.turnOff();
        this.minecraft.getMainRenderTarget().bindWrite(true);
    }

    @Override
    public void renderVrFast(float partialTicks, boolean secondpass, boolean menuright, boolean menuleft,
                             PoseStack posestack) {
        if (DATA_HOLDER.currentPass == RenderPass.SCOPEL || DATA_HOLDER.currentPass == RenderPass.SCOPER)
            return;
        this.minecraft.getProfiler().popPush("VR");
        this.lightTexture.turnOffLightLayer();

        if (secondpass) {
            this.renderVrShadow(partialTicks, !this.shouldOccludeGui(), posestack);
        }

        if (!secondpass) {
            this.renderCrosshairAtDepth(!DATA_HOLDER.vrSettings.useCrosshairOcclusion, posestack);
        }

        if (!secondpass) {
            VRWidgetHelper.renderVRThirdPersonCamWidget();
        }

        if (!secondpass) {
            VRWidgetHelper.renderVRHandheldCameraWidget();
        }

        if (secondpass && (Minecraft.getInstance().screen != null || !KeyboardHandler.Showing)) {
            this.renderGuiLayer(partialTicks, !this.shouldOccludeGui(), posestack);
        }

        if (secondpass && KeyboardHandler.Showing) {
            if (DATA_HOLDER.vrSettings.physicalKeyboard) {
                this.renderPhysicalKeyboard(partialTicks, posestack);
            } else {
                this.render2D(partialTicks, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
                        KeyboardHandler.Rotation_room, !this.shouldOccludeGui(), posestack);
            }
        }

        if (secondpass && RadialHandler.isShowing()) {
            this.render2D(partialTicks, RadialHandler.Framebuffer, RadialHandler.Pos_room, RadialHandler.Rotation_room,
                    !this.shouldOccludeGui(), posestack);
        }
        // render hands in second pass when gui is open
        boolean renderHandsSecond = RadialHandler.isShowing() || KeyboardHandler.Showing || Minecraft.getInstance().screen != null;
        if (secondpass == renderHandsSecond) {
            // should render hands in second pass if menus are open, else in the first pass
            // only render the hands only once
            this.renderVRHands(partialTicks, this.shouldRenderHands(), this.shouldRenderHands(), menuright, menuleft,
                    posestack);
        }
        this.renderVRSelfEffects(partialTicks);
    }

    public void drawSizedQuad(float displayWidth, float displayHeight, float size, float[] color) {
        float aspect = displayHeight / displayWidth;
        BufferBuilder b = Tesselator.getInstance().getBuilder();
        b.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        b.vertex(-size / 2, -(size * aspect) / 2, 0)
            .uv(0, 0)
            .color(color[0], color[1], color[2], color[3])
            .normal(0, 0, 1)
            .endVertex()
        ;
        b.vertex(size / 2, -(size * aspect) / 2, 0)
            .uv(1, 0)
            .color(color[0], color[1], color[2], color[3])
            .normal(0, 0, 1)
            .endVertex()
        ;
        b.vertex(size / 2, (size * aspect) / 2, 0)
            .uv(1, 1)
            .color(color[0], color[1], color[2], color[3])
            .normal(0, 0, 1)
            .endVertex()
        ;
        b.vertex(-size / 2, (size * aspect) / 2, 0)
            .uv(0, 1)
            .color(color[0], color[1], color[2], color[3])
            .normal(0, 0, 1)
            .endVertex()
        ;
        BufferUploader.drawWithShader(b.end());
    }

    public void drawSizedQuad(float displayWidth, float displayHeight, float size, float[] color, Matrix4f pMatrix) {
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        float aspect = displayHeight / displayWidth;
        BufferBuilder b = Tesselator.getInstance().getBuilder();
        b.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR_NORMAL);
        b.vertex(pMatrix, -size / 2, -(size * aspect) / 2, 0)
            .uv(0, 0)
            .color(color[0], color[1], color[2], color[3])
            .normal(0, 0, 1)
            .endVertex()
        ;
        b.vertex(pMatrix, size / 2, -(size * aspect) / 2, 0)
            .uv(1, 0)
            .color(color[0], color[1], color[2], color[3])
            .normal(0, 0, 1)
            .endVertex()
        ;
        b.vertex(pMatrix, size / 2, (size * aspect) / 2, 0)
            .uv(1, 1)
            .color(color[0], color[1], color[2], color[3])
            .normal(0, 0, 1)
            .endVertex()
        ;
        b.vertex(pMatrix, -size / 2, (size * aspect) / 2, 0)
            .uv(0, 1)
            .color(color[0], color[1], color[2], color[3])
            .normal(0, 0, 1)
            .endVertex()
        ;
        BufferUploader.drawWithShader(b.end());
    }

    public void drawSizedQuadSolid(float displayWidth, float displayHeight, float size, float[] color, Matrix4f pMatrix) {
        RenderSystem.setShader(GameRenderer::getRendertypeEntitySolidShader);
        this.lightTexture.turnOnLightLayer();
        this.overlayTexture().setupOverlayColor();
        float aspect = displayHeight / displayWidth;
        BufferBuilder b = Tesselator.getInstance().getBuilder();
        b.begin(Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);
        int light = LightTexture.pack(15, 15);

        // store old lights
        Vector3f light0Old = RenderSystemAccessor.getShaderLightDirections()[0];
        Vector3f light1Old = RenderSystemAccessor.getShaderLightDirections()[1];

        // set lights to front
        RenderSystem.setShaderLights(new Vector3f(0, 0, 1), new Vector3f(0, 0, 1));
        RenderSystem.setupShaderLights(RenderSystem.getShader());

        b.vertex(pMatrix, -size / 2, -(size * aspect) / 2, 0)
            .color(color[0], color[1], color[2], color[3])
            .uv(0, 0)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(light)
            .normal(0, 0, 1)
            .endVertex()
        ;
        b.vertex(pMatrix, size / 2, -(size * aspect) / 2, 0)
            .color(color[0], color[1], color[2], color[3])
            .uv(1, 0)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(light)
            .normal(0, 0, 1)
            .endVertex()
        ;
        b.vertex(pMatrix, size / 2, (size * aspect) / 2, 0)
            .color(color[0], color[1], color[2], color[3])
            .uv(1, 1)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(light)
            .normal(0, 0, 1)
            .endVertex()
        ;
        b.vertex(pMatrix, -size / 2, (size * aspect) / 2, 0)
            .color(color[0], color[1], color[2], color[3])
            .uv(0, 1)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(light)
            .normal(0, 0, 1)
            .endVertex()
        ;
        BufferUploader.drawWithShader(b.end());
        this.lightTexture.turnOffLightLayer();

        // reset lights
        if (light0Old != null && light1Old != null) {
            RenderSystem.setShaderLights(light0Old, light1Old);
            RenderSystem.setupShaderLights(RenderSystem.getShader());
        }
    }


    public void drawSizedQuad(float displayWidth, float displayHeight, float size) {
        this.drawSizedQuad(displayWidth, displayHeight, size, new float[]{1, 1, 1, 1});
    }

    public void drawSizedQuadWithLightmap(float displayWidth, float displayHeight, float size, int lighti,
                                          float[] color, Matrix4f pMatrix) {
        RenderSystem.setShader(GameRenderer::getRendertypeEntityCutoutNoCullShader);
        float aspect = displayHeight / displayWidth;
        this.lightTexture.turnOnLightLayer();
        this.overlayTexture().setupOverlayColor();
        BufferBuilder b = Tesselator.getInstance().getBuilder();
        b.begin(Mode.QUADS, DefaultVertexFormat.NEW_ENTITY);

        // store old lights
        Vector3f light0Old = RenderSystemAccessor.getShaderLightDirections()[0];
        Vector3f light1Old = RenderSystemAccessor.getShaderLightDirections()[1];

        // set lights to front
        RenderSystem.setShaderLights(new Vector3f(0, 0, 1), new Vector3f(0, 0, 1));
        RenderSystem.setupShaderLights(RenderSystem.getShader());

        b.vertex(pMatrix, -size / 2, -(size * aspect) / 2, 0)
            .color(color[0], color[1], color[2], color[3])
            .uv(0, 0)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(lighti)
            .normal(0, 0, 1)
            .endVertex()
        ;
        b.vertex(pMatrix, size / 2, -(size * aspect) / 2, 0)
            .color(color[0], color[1], color[2], color[3])
            .uv(1, 0)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(lighti)
            .normal(0, 0, 1)
            .endVertex()
        ;
        b.vertex(pMatrix, size / 2, (size * aspect) / 2, 0)
            .color(color[0], color[1], color[2], color[3])
            .uv(1, 1)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(lighti)
            .normal(0, 0, 1)
            .endVertex()
        ;
        b.vertex(pMatrix, -size / 2, (size * aspect) / 2, 0)
            .color(color[0], color[1], color[2], color[3])
            .uv(0, 1)
            .overlayCoords(OverlayTexture.NO_OVERLAY)
            .uv2(lighti)
            .normal(0, 0, 1)
            .endVertex()
        ;
        BufferUploader.drawWithShader(b.end());

        this.lightTexture.turnOffLightLayer();

        // reset lights
        if (light0Old != null && light1Old != null) {
            RenderSystem.setShaderLights(light0Old, light1Old);
            RenderSystem.setupShaderLights(RenderSystem.getShader());
        }
    }

    public void drawSizedQuadWithLightmap(float displayWidth, float displayHeight, float size, int lighti,
                                          Matrix4f pMatrix) {
        this.drawSizedQuadWithLightmap(displayWidth, displayHeight, size, lighti, new float[]{1, 1, 1, 1}, pMatrix);
    }

    private void renderTeleportArc(VRPlayer vrPlayer, PoseStack poseStack) {
        if (DATA_HOLDER.teleportTracker.vrMovementStyle.showBeam
                && DATA_HOLDER.teleportTracker.isAiming()
                && DATA_HOLDER.teleportTracker.movementTeleportArcSteps > 1) {
            this.minecraft.getProfiler().push("teleportArc");
            // boolean isShader = Config.isShaders();
            boolean isShader = false;
            RenderSystem.enableCull();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
            Tesselator tesselator = Tesselator.getInstance();
            tesselator.getBuilder().begin(Mode.QUADS, DefaultVertexFormat.POSITION_COLOR_NORMAL);
            double VOffset = DATA_HOLDER.teleportTracker.lastTeleportArcDisplayOffset;
            Vec3 dest = DATA_HOLDER.teleportTracker.getDestination();
            boolean validLocation = dest.x != 0.0D || dest.y != 0.0D || dest.z != 0.0D;
            byte alpha = -1;
            Vec3i color;

            if (!validLocation) {
                color = new Vec3i(83, 75, 83);
                alpha = -128;
            } else {
                if (ClientNetworking.isLimitedSurvivalTeleport() && !this.minecraft.player.getAbilities().mayfly) {
                    color = this.tpLimitedColor;
                } else {
                    color = this.tpUnlimitedColor;
                }

                VOffset = DATA_HOLDER.vrRenderer.getCurrentTimeSecs()
                        * (double) DATA_HOLDER.teleportTracker.vrMovementStyle.textureScrollSpeed * 0.6D;
                DATA_HOLDER.teleportTracker.lastTeleportArcDisplayOffset = VOffset;
            }

            float segmentHalfWidth = DATA_HOLDER.teleportTracker.vrMovementStyle.beamHalfWidth * 0.15F;
            int i = DATA_HOLDER.teleportTracker.movementTeleportArcSteps - 1;

            if (DATA_HOLDER.teleportTracker.vrMovementStyle.beamGrow) {
                i = (int) ((double) i * DATA_HOLDER.teleportTracker.movementTeleportProgress);
            }

            double segmentProgress = 1.0D / (double) i;
            Vec3 up = new Vec3(0.0D, 1.0D, 0.0D);

            for (int j = 0; j < i; ++j) {
                double progress = (double) j / (double) i + VOffset * segmentProgress;
                int k = Mth.floor(progress);
                progress = progress - (double) ((float) k);
                Vec3 start = DATA_HOLDER.teleportTracker
                        .getInterpolatedArcPosition((float) (progress - segmentProgress * (double) 0.4F))
                        .subtract(this.minecraft.getCameraEntity().position());
                Vec3 end = DATA_HOLDER.teleportTracker.getInterpolatedArcPosition((float) progress)
                        .subtract(this.minecraft.getCameraEntity().position());
                float shift = (float) progress * 2.0F;
                this.renderBox(tesselator, start, end, -segmentHalfWidth, segmentHalfWidth, (-1.0F + shift) * segmentHalfWidth, (1.0F + shift) * segmentHalfWidth, up, color, alpha,
                        poseStack);
            }

            tesselator.end();
            RenderSystem.disableCull();

            if (validLocation && DATA_HOLDER.teleportTracker.movementTeleportProgress >= 1) {
                Vec3 circlePos = (new Vec3(dest.x, dest.y, dest.z)).subtract(this.minecraft.getCameraEntity().position());
                int side = 1;
                float o = 0.01F;
                double x = 0;
                double y = 0;
                double z = 0;

                if (side == 0) y -= o;
                if (side == 1) y += o;
                if (side == 2) z -= o;
                if (side == 3) z += o;
                if (side == 4) x -= o;
                if (side == 5) x += o;

                this.renderFlatQuad(circlePos.add(x, y, z), 0.6F, 0.6F, 0, (int) ((double) color.getX() * 1.03D),
                        (int) ((double) color.getY() * 1.03D), (int) ((double) color.getZ() * 1.03D), 64, poseStack);

                if (side == 0) y -= o;
                if (side == 1) y += o;
                if (side == 2) z -= o;
                if (side == 3) z += o;
                if (side == 4) x -= o;
                if (side == 5) x += o;

                this.renderFlatQuad(circlePos.add(x, y, z), 0.4F, 0.4F, 0, (int) ((double) color.getX() * 1.04D),
                        (int) ((double) color.getY() * 1.04D), (int) ((double) color.getZ() * 1.04D), 64, poseStack);

                if (side == 0) y -= o;
                if (side == 1) y += o;
                if (side == 2) z -= o;
                if (side == 3) z += o;
                if (side == 4) x -= o;
                if (side == 5) x += o;

                this.renderFlatQuad(circlePos.add(x, y, z), 0.2F, 0.2F, 0, (int) ((double) color.getX() * 1.05D),
                        (int) ((double) color.getY() * 1.05D), (int) ((double) color.getZ() * 1.05D), 64, poseStack);
            }

            this.minecraft.getProfiler().pop();
            RenderSystem.enableCull();
        }
    }

    @Override
    public void drawEyeStencil(boolean shaders) {
        switch (DATA_HOLDER.currentPass != null ? DATA_HOLDER.currentPass : RenderPass.CENTER){
            case LEFT, RIGHT -> {
                if (DATA_HOLDER.vrSettings.vrUseStencil) {
                    DATA_HOLDER.vrRenderer.doStencil(false);
//				net.optifine.shaders.Program program = Shaders.activeProgram;
//
//				if (shaders && Shaders.dfb != null) {
//					Shaders.dfb.bindFramebuffer();
//					Shaders.useProgram(Shaders.ProgramNone);
//
//					for (int i = 0; i < Shaders.usedDepthBuffers; ++i) {
//						GlStateManager._bindTexture(Shaders.dfb.depthTextures.get(i));
//						this.minecraft.vrRenderer.doStencil(false);
//					}
//
//					Shaders.useProgram(program);
                } else {
                    GL11C.glDisable(GL11C.GL_STENCIL_TEST);
                }
            }
            default -> {
                // No stencil for telescope
                // DATA_HOLDER.vrRenderer.doStencil(true);
            }
        }
    }

    private void renderFaceOverlay(float par1, PoseStack pMatrix) {
//		boolean shadersMod = Config.isShaders();
        boolean shadersMod = false;

//		if (shadersMod) { TODO
//			Shaders.beginFPOverlay();
//		}

        if (this.inBlock > 0.0F) {
            this.renderFaceInBlock();
            this.renderGuiLayer(par1, true, pMatrix);

            if (KeyboardHandler.Showing) {
                if (DATA_HOLDER.vrSettings.physicalKeyboard) {
                    this.renderPhysicalKeyboard(par1, pMatrix);
                } else {
                    this.render2D(par1, KeyboardHandler.Framebuffer, KeyboardHandler.Pos_room,
                            KeyboardHandler.Rotation_room, true, pMatrix);
                }
            }

            if (RadialHandler.isShowing()) {
                this.render2D(par1, RadialHandler.Framebuffer, RadialHandler.Pos_room, RadialHandler.Rotation_room,
                        true, pMatrix);
            }

            if (this.inBlock >= 1.0F) {
                this.renderVRHands(par1, true, true, true, true, pMatrix);
            }
        }

//		if (shadersMod) { TODO
//			Shaders.endFPOverlay();
//		}
    }

    private void renderFaceInBlock() {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferbuilder = tesselator.getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderColor(0, 0, 0, ((GameRendererExtension) this.minecraft.gameRenderer).inBlock());

        // orthographic matrix, (-1, -1) to (1, 1), near = 0.0, far 2.0
        Matrix4f mat = new Matrix4f();
        mat.m00(1.0F);
        mat.m11(1.0F);
        mat.m22(-1.0F);
        mat.m33(1.0F);
        mat.m32(-1.0F);

        RenderSystem.depthFunc(GL11C.GL_ALWAYS);
        RenderSystem.depthMask(true);
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferbuilder.vertex(mat, -1.5F, -1.5F, 0.0F).endVertex();
        bufferbuilder.vertex(mat, 1.5F, -1.5F, 0.0F).endVertex();
        bufferbuilder.vertex(mat, 1.5F, 1.5F, 0.0F).endVertex();
        bufferbuilder.vertex(mat, -1.5F, 1.5F, 0.0F).endVertex();
        tesselator.end();
        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    public boolean shouldRenderCrosshair() {
        return (
            !ClientDataHolderVR.viewonly &&
            this.minecraft.level != null &&
            this.minecraft.screen == null &&
            !switch (DATA_HOLDER.vrSettings.renderInGameCrosshairMode) {
                case ALWAYS -> false;
                case WITH_HUD -> this.minecraft.options.hideGui;
                default -> true;
            } &&
            !switch (DATA_HOLDER.currentPass != null ? DATA_HOLDER.currentPass : RenderPass.CENTER){
                case THIRD, SCOPEL, SCOPER, CAMERA -> true;
                default -> false;
            } &&
            !KeyboardHandler.Showing &&
            !RadialHandler.isUsingController(ControllerType.RIGHT) &&
            !DATA_HOLDER.bowTracker.isNotched() &&
            (
                !DATA_HOLDER.vr.getInputAction(VivecraftVRMod.INSTANCE.keyVRInteract)
                    .isEnabledRaw(ControllerType.RIGHT)
                && !VivecraftVRMod.INSTANCE.keyVRInteract.isDown(ControllerType.RIGHT)
            ) && (
                !DATA_HOLDER.vr.getInputAction(VivecraftVRMod.INSTANCE.keyClimbeyGrab)
                    .isEnabledRaw(ControllerType.RIGHT)
                && !VivecraftVRMod.INSTANCE.keyClimbeyGrab.isDown(ControllerType.RIGHT)
            ) &&
            !DATA_HOLDER.teleportTracker.isAiming() &&
            !DATA_HOLDER.climbTracker.isGrabbingLadder(0) &&
            !(DATA_HOLDER.vrPlayer.worldScale > 15)
        );
    }

    private void renderCrosshairAtDepth(boolean depthAlways, PoseStack poseStack) {
        if (this.shouldRenderCrosshair()) {
            this.minecraft.getProfiler().push("crosshair");
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            Vec3 crosshairRenderPos = this.crossVec;
            VRData vrworld = DATA_HOLDER.vrPlayer.getVRDataWorld();
            Vec3 aim = crosshairRenderPos.subtract(vrworld.getController(0).getPosition());
            float crossDepth = (float) aim.length();
            float scale = (float) ((double) (0.125F * DATA_HOLDER.vrSettings.crosshairScale)
                    * Math.sqrt(vrworld.worldScale));
            crosshairRenderPos = crosshairRenderPos.add(aim.normalize().scale(-0.01D));
            poseStack.pushPose();
            poseStack.setIdentity();
            applyVRModelView(DATA_HOLDER.currentPass, poseStack);

            Vec3 translate = crosshairRenderPos.subtract(this.minecraft.getCameraEntity().position());
            poseStack.translate(translate.x, translate.y, translate.z);

            if (this.minecraft.hitResult != null && this.minecraft.hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult hit = (BlockHitResult) this.minecraft.hitResult;

                if (hit.getDirection() == Direction.DOWN) {
                    MethodHolder.rotateDeg(poseStack, vrworld.getController(0).getYaw(), 0.0F, 1.0F, 0.0F);
                    MethodHolder.rotateDeg(poseStack, -90.0F, 1.0F, 0.0F, 0.0F);
                } else if (hit.getDirection() == Direction.EAST) {
                    MethodHolder.rotateDeg(poseStack, 90.0F, 0.0F, 1.0F, 0.0F);
                } else if (hit.getDirection() != Direction.NORTH
                        && hit.getDirection() != Direction.SOUTH) {
                    if (hit.getDirection() == Direction.UP) {
                        MethodHolder.rotateDeg(poseStack, -vrworld.getController(0).getYaw(), 0.0F, 1.0F, 0.0F);
                        MethodHolder.rotateDeg(poseStack, -90.0F, 1.0F, 0.0F, 0.0F);
                    } else if (hit.getDirection() == Direction.WEST) {
                        MethodHolder.rotateDeg(poseStack, 90.0F, 0.0F, 1.0F, 0.0F);
                    }
                }
            } else {
                MethodHolder.rotateDeg(poseStack, -vrworld.getController(0).getYaw(), 0.0F, 1.0F, 0.0F);
                MethodHolder.rotateDeg(poseStack, -vrworld.getController(0).getPitch(), 1.0F, 0.0F, 0.0F);
            }

            if (DATA_HOLDER.vrSettings.crosshairScalesWithDistance) {
                float depthscale = 0.3F + 0.2F * crossDepth;
                scale *= depthscale;
            }

            this.lightTexture.turnOnLightLayer();
            poseStack.scale(scale, scale, scale);
            RenderSystem.depthMask(true);
            RenderSystem.enableDepthTest();
            // RenderSystem.disableLighting();
            RenderSystem.disableCull();

            if (depthAlways) {
                RenderSystem.depthFunc(GL11C.GL_ALWAYS);
            } else {
                RenderSystem.depthFunc(GL11C.GL_LEQUAL);
            }

            // boolean shadersMod = Config.isShaders();
            boolean shadersMod = false;
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
                    GlStateManager.DestFactor.ZERO, GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            int light = LevelRenderer.getLightColor(this.minecraft.level, BlockPos.containing(crosshairRenderPos));
            float brightness = 1.0F;

            if (this.minecraft.hitResult == null || this.minecraft.hitResult.getType() == HitResult.Type.MISS) {
                brightness = 0.5F;
            }

            RenderSystem.setShaderTexture(0, Screen.GUI_ICONS_LOCATION);
            float f3 = 0.00390625F;
            float f4 = 0.00390625F;

            BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();

            RenderSystem.setShader(GameRenderer::getRendertypeCutoutShader);
            bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.BLOCK);

            bufferbuilder.vertex(poseStack.last().pose(), -1, 1, 0)
                .color(brightness, brightness, brightness, 1)
                .uv(0, 15 * f4)
                .uv2(light)
                .normal(0, 0, 1)
                .endVertex()
            ;
            bufferbuilder.vertex(poseStack.last().pose(), 1, 1, 0)
                .color(brightness, brightness, brightness, 1)
                .uv(15 * f3, 15 * f4)
                .uv2(light)
                .normal(0, 0, 1)
                .endVertex()
            ;
            bufferbuilder.vertex(poseStack.last().pose(), 1, -1, 0)
                .color(brightness, brightness, brightness, 1)
                .uv(15 * f3, 0)
                .uv2(light)
                .normal(0, 0, 1)
                .endVertex()
            ;
            bufferbuilder.vertex(poseStack.last().pose(), -1, -1, 0)
                .color(brightness, brightness, brightness, 1)
                .uv(0, 0)
                .uv2(light)
                .normal(0, 0, 1)
                .endVertex()
            ;

            BufferUploader.drawWithShader(bufferbuilder.end());

            RenderSystem.defaultBlendFunc();
            RenderSystem.disableBlend();
            RenderSystem.enableCull();
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
            poseStack.popPose();
            this.minecraft.getProfiler().pop();
        }
    }

    public boolean shouldOccludeGui() {
        return switch (DATA_HOLDER.currentPass != null ? DATA_HOLDER.currentPass : RenderPass.CENTER){
            case THIRD, CAMERA -> true;
            default -> (
                !this.isInMenuRoom() && this.minecraft.screen == null && !KeyboardHandler.Showing
                && !RadialHandler.isShowing() && DATA_HOLDER.vrSettings.hudOcclusion
                && !((ItemInHandRendererExtension) this.itemInHandRenderer).isInsideOpaqueBlock(
                    DATA_HOLDER.vrPlayer.getVRDataWorld().getEye(DATA_HOLDER.currentPass).getPosition()
                )
            );
        };
    }

    private void renderVrShadow(float par1, boolean depthAlways, PoseStack poseStack) {
        switch (DATA_HOLDER.currentPass != null ? DATA_HOLDER.currentPass : RenderPass.CENTER){
            case THIRD, CAMERA -> {}
            default -> {
                if (this.minecraft.player.isAlive() &&
                    !(((PlayerExtension) this.minecraft.player).getRoomYOffsetFromPose() < 0.0D) &&
                    this.minecraft.player.getVehicle() == null
                ) {
                    this.minecraft.getProfiler().push("vr shadow");
                    AABB aabb = this.minecraft.player.getBoundingBox();

                    if (DATA_HOLDER.vrSettings.vrShowBlueCircleBuddy && aabb != null) {

                        poseStack.pushPose();
                        poseStack.setIdentity();
                        RenderSystem.disableCull();
                        this.applyVRModelView(DATA_HOLDER.currentPass, poseStack);
                        Vec3 o = DATA_HOLDER.vrPlayer.getVRDataWorld()
                            .getEye(DATA_HOLDER.currentPass).getPosition();
                        LocalPlayer player = this.minecraft.player;
                        Vec3 interpolatedPlayerPos = new Vec3(
                            this.rvelastX + (this.rveX - this.rvelastX) * (double) par1,
                            this.rvelastY + (this.rveY - this.rvelastY) * (double) par1,
                            this.rvelastZ + (this.rveZ - this.rvelastZ) * (double) par1
                        );
                        Vec3 pos = interpolatedPlayerPos.subtract(o).add(0.0D, 0.005D, 0.0D);
                        this.setupPolyRendering(true);
                        RenderSystem.enableDepthTest();

                        if (depthAlways) {
                            RenderSystem.depthFunc(GL11C.GL_ALWAYS);
                        } else {
                            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
                        }

                        RenderSystem.setShader(GameRenderer::getPositionColorShader);
                        RenderSystem.setShaderTexture(0, new ResourceLocation("vivecraft:textures/white.png"));
                        this.renderFlatQuad(pos, (float) (aabb.maxX - aabb.minX), (float) (aabb.maxZ - aabb.minZ),
                            0.0F, 0, 0, 0, 64, poseStack);
                        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
                        this.setupPolyRendering(false);
                        poseStack.popPose();
                        RenderSystem.enableCull();
                    }
                    this.minecraft.getProfiler().pop();
                }
            }
        }
    }

    public boolean shouldRenderHands() {
        return switch (DATA_HOLDER.currentPass != null ? DATA_HOLDER.currentPass : RenderPass.CENTER){
            case THIRD -> DATA_HOLDER.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY;
            case CAMERA -> false;
            default -> !ClientDataHolderVR.viewonly;
        };
    }

    private void renderVRSelfEffects(float par1) {
        switch (DATA_HOLDER.currentPass != null ? DATA_HOLDER.currentPass : RenderPass.CENTER){
            case THIRD, CAMERA -> this.renderItemActivationAnimation(0, 0, par1);
            default -> {
                if (this.onfire) {
                    this.renderFireInFirstPerson();
                }
            }
        }
    }

    private void renderFireInFirstPerson() {
        PoseStack posestack = new PoseStack();
        this.applyVRModelView(DATA_HOLDER.currentPass, posestack);
        this.applystereo(DATA_HOLDER.currentPass, posestack);
        BufferBuilder bufferbuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.depthFunc(GL11C.GL_ALWAYS);

        if (DATA_HOLDER.currentPass == RenderPass.THIRD || DATA_HOLDER.currentPass == RenderPass.CAMERA) {
            RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        TextureAtlasSprite textureatlassprite = ModelBakery.FIRE_1.sprite();
        RenderSystem.enableDepthTest();

//		if (SmartAnimations.isActive()) { TODO
//			SmartAnimations.spriteRendered(textureatlassprite);
//		}

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, textureatlassprite.atlasLocation());
        VRData vrworld = DATA_HOLDER.vrPlayer.getVRDataWorld();
        float f = textureatlassprite.getU0();
        float f1 = textureatlassprite.getU1();
        float f2 = (f + f1) / 2.0F;
        float f3 = textureatlassprite.getV0();
        float f4 = textureatlassprite.getV1();
        float f5 = (f3 + f4) / 2.0F;
        float f6 = textureatlassprite.uvShrinkRatio();
        float f7 = Mth.lerp(f6, f, f2);
        float f8 = Mth.lerp(f6, f1, f2);
        float f9 = Mth.lerp(f6, f3, f5);
        float f10 = Mth.lerp(f6, f4, f5);
        float f11 = 1.0F;

        float a = 0.3F;
        float b = (float) (vrworld.getHeadPivot().y - ((GameRendererExtension) this.minecraft.gameRenderer).getRveY());

        for (int i = 0; i < 4; ++i) {
            posestack.pushPose();
            posestack.mulPose(Axis.YP.rotationDegrees((float) i * 90.0F - vrworld.getBodyYaw()));
            posestack.translate(0, -b, 0);
            Matrix4f matrix4f = posestack.last().pose();
            bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
            bufferbuilder.vertex(matrix4f, -a, 0, -a).uv(f8, f10).color(1, 1, 1, 0.9F).endVertex();
            bufferbuilder.vertex(matrix4f, a, 0, -a).uv(f7, f10).color(1, 1, 1, 0.9F).endVertex();
            bufferbuilder.vertex(matrix4f, a, b, -a).uv(f7, f9).color(1, 1, 1, 0.9F).endVertex();
            bufferbuilder.vertex(matrix4f, -a, b, -a).uv(f8, f9).color(1, 1, 1, 0.9F).endVertex();
            BufferUploader.drawWithShader(bufferbuilder.end());

            posestack.popPose();
        }

        RenderSystem.depthFunc(GL11C.GL_LEQUAL);
        RenderSystem.disableBlend();
    }

    public void applystereo(RenderPass currentPass, PoseStack matrix) {
        if (currentPass == RenderPass.LEFT || currentPass == RenderPass.RIGHT) {
            VRData vrworld = DATA_HOLDER.vrPlayer.getVRDataWorld();
            Vec3 eye = vrworld.getEye(currentPass).getPosition().subtract(vrworld.getEye(RenderPass.CENTER).getPosition());
            matrix.translate((float) -eye.x, (float) -eye.y, (float) -eye.z);
        }
    }

    @Override
    public void restoreRVEPos(LivingEntity e) {
        if (e != null) {
            e.setPosRaw(this.rveX, this.rveY, this.rveZ);
            e.xOld = this.rvelastX;
            e.yOld = this.rvelastY;
            e.zOld = this.rvelastZ;
            e.xo = this.rveprevX;
            e.yo = this.rveprevY;
            e.zo = this.rveprevZ;
            e.setYRot(this.rveyaw);
            e.setXRot(this.rvepitch);
            e.yRotO = this.rvelastyaw;
            e.xRotO = this.rvelastpitch;
            e.yHeadRot = this.rveyaw;
            e.yHeadRotO = this.rvelastyaw;
            e.eyeHeight = this.rveHeight;
            this.cached = false;
        }
    }

    @Override
    public void DrawScopeFB(PoseStack matrixStackIn, int i) {
        if (DATA_HOLDER.currentPass != RenderPass.SCOPEL && DATA_HOLDER.currentPass != RenderPass.SCOPER) {
            //this.lightTexture.turnOffLightLayer();
            matrixStackIn.pushPose();
            RenderSystem.enableDepthTest();

            if (i == 0) {
                DATA_HOLDER.vrRenderer.telescopeFramebufferR.bindRead();
                RenderSystem.setShaderTexture(0, DATA_HOLDER.vrRenderer.telescopeFramebufferR.getColorTextureId());
            } else {
                DATA_HOLDER.vrRenderer.telescopeFramebufferL.bindRead();
                RenderSystem.setShaderTexture(0, DATA_HOLDER.vrRenderer.telescopeFramebufferL.getColorTextureId());
            }

            float scale = 0.0785F;
            //actual framebuffer
            float alpha = TelescopeTracker.viewPercent(i);
            // this.drawSizedQuad(720.0F, 720.0F, scale, new float[]{alpha, alpha, alpha, 1}, matrixStackIn.last().pose());
            this.drawSizedQuadSolid(720, 720, scale, new float[]{alpha, alpha, alpha, 1}, matrixStackIn.last().pose());

            RenderSystem.setShaderTexture(0, new ResourceLocation("textures/misc/spyglass_scope.png"));
            RenderSystem.enableBlend();
            matrixStackIn.translate(0.0D, 0.0D, 0.00001D);
            int light = LevelRenderer.getLightColor(this.minecraft.level, BlockPos.containing(DATA_HOLDER.vrPlayer.getVRDataWorld().getController(i).getPosition()));
            this.drawSizedQuadWithLightmap(720, 720, scale, light, matrixStackIn.last().pose());

            matrixStackIn.popPose();
            this.lightTexture.turnOnLightLayer();
        }
    }
}