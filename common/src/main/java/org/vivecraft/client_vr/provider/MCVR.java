package org.vivecraft.client_vr.provider;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.*;
import org.vivecraft.client_vr.extensions.GuiExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;
import org.vivecraft.client_vr.provider.openvr_lwjgl.control.VRInputActionSet;
import org.vivecraft.client_vr.provider.openvr_lwjgl.control.VivecraftMovementInput;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.common.utils.lwjgl.Matrix4f;
import org.vivecraft.common.utils.lwjgl.Vector3f;
import org.vivecraft.common.utils.math.Quaternion;
import org.vivecraft.common.utils.math.Vector3;
import org.vivecraft.mixin.client.MinecraftAccessor;
import org.vivecraft.mod_compat_vr.ShadersHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class MCVR {
    protected static MCVR me;
    protected org.vivecraft.common.utils.math.Matrix4f hmdPose = new org.vivecraft.common.utils.math.Matrix4f();
    public org.vivecraft.common.utils.math.Matrix4f hmdRotation = new org.vivecraft.common.utils.math.Matrix4f();
    public HardwareType detectedHardware = HardwareType.VIVE;
    protected org.vivecraft.common.utils.math.Matrix4f hmdPoseLeftEye = new org.vivecraft.common.utils.math.Matrix4f();
    protected org.vivecraft.common.utils.math.Matrix4f hmdPoseRightEye = new org.vivecraft.common.utils.math.Matrix4f();
    public Vec3History hmdHistory = new Vec3History();
    public Vec3History hmdPivotHistory = new Vec3History();
    public QuaternionfHistory hmdRotHistory = new QuaternionfHistory();
    protected boolean headIsTracking;
    protected org.vivecraft.common.utils.math.Matrix4f[] controllerPose = new org.vivecraft.common.utils.math.Matrix4f[3];
    protected org.vivecraft.common.utils.math.Matrix4f[] controllerRotation = new org.vivecraft.common.utils.math.Matrix4f[3];
    protected boolean[] controllerTracking = new boolean[3];
    protected org.vivecraft.common.utils.math.Matrix4f[] handRotation = new org.vivecraft.common.utils.math.Matrix4f[3];
    public Vec3History[] controllerHistory = new Vec3History[]{new Vec3History(), new Vec3History()};
    public Vec3History[] controllerForwardHistory = new Vec3History[]{new Vec3History(), new Vec3History()};
    public Vec3History[] controllerUpHistory = new Vec3History[]{new Vec3History(), new Vec3History()};
    protected double gunAngle = 0.0D;
    protected boolean gunStyle;
    public boolean initialized;
    public String initStatus;
    public boolean initSuccess;
    protected org.vivecraft.common.utils.math.Matrix4f[] poseMatrices;
    protected Vec3[] deviceVelocity;
    protected Vec3[] aimSource = new Vec3[3];
    public Vector3 forward = new Vector3(0.0F, 0.0F, -1.0F);
    public Vector3 up = new Vector3(0.0F, 1.0F, 0.0F);
    public int hmdAvgLength = 90;
    public LinkedList<Vec3> hmdPosSamples = new LinkedList<>();
    public LinkedList<Float> hmdYawSamples = new LinkedList<>();
    protected float hmdYawTotal;
    protected float hmdYawLast;
    protected boolean trigger;
    public boolean mrMovingCamActive;
    public Vec3 mrControllerPos = Vec3.ZERO;
    public float mrControllerPitch;
    public float mrControllerYaw;
    public float mrControllerRoll;
    protected HapticScheduler hapticScheduler;
    public float seatedRot;
    public float aimPitch = 0.0F;
    protected final org.vivecraft.common.utils.math.Matrix4f Neutral_HMD = new org.vivecraft.common.utils.math.Matrix4f(1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.0F, 1.62F, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F);
    protected final org.vivecraft.common.utils.math.Matrix4f TPose_Left = new org.vivecraft.common.utils.math.Matrix4f(1.0F, 0.0F, 0.0F, 0.25F, 0.0F, 1.0F, 0.0F, 1.62F, 0.0F, 0.0F, 1.0F, 0.25F, 0.0F, 0.0F, 0.0F, 1.0F);
    protected final org.vivecraft.common.utils.math.Matrix4f TPose_Right = new org.vivecraft.common.utils.math.Matrix4f(1.0F, 0.0F, 0.0F, 0.75F, 0.0F, 1.0F, 0.0F, 1.62F, 0.0F, 0.0F, 1.0F, 0.75F, 0.0F, 0.0F, 0.0F, 1.0F);
    protected boolean TPose = false;
    public boolean hudPopup = true;
    protected int moveModeSwitchCount = 0;
    public boolean isWalkingAbout;
    protected boolean isFreeRotate;
    protected ControllerType walkaboutController;
    protected ControllerType freeRotateController;
    protected float walkaboutYawStart;
    protected float hmdForwardYaw = 180;
    public boolean ignorePressesNextFrame = false;
    protected int quickTorchPreviousSlot;
    protected Map<String, VRInputAction> inputActions = new HashMap<>();
    protected Map<String, VRInputAction> inputActionsByKeyBinding = new HashMap<>();

    public MCVR() {
        me = this;

        for (int i = 0; i < 3; ++i) {
            this.aimSource[i] = new Vec3(0.0D, 0.0D, 0.0D);
            this.controllerPose[i] = new org.vivecraft.common.utils.math.Matrix4f();
            this.controllerRotation[i] = new org.vivecraft.common.utils.math.Matrix4f();
            this.handRotation[i] = new org.vivecraft.common.utils.math.Matrix4f();
        }
    }

    public static MCVR get() {
        return me;
    }

    public abstract String getName();

    public abstract String getID();

    public abstract void processInputs();

    public abstract void destroy();

    public double getGunAngle() {
        return this.gunAngle;
    }

    public org.vivecraft.common.utils.math.Matrix4f getAimRotation(int controller) {
        return this.controllerRotation[controller];
    }

    public Vec3 getAimSource(int controller) {
        Vec3 vec3 = new Vec3(this.aimSource[controller].x, this.aimSource[controller].y, this.aimSource[controller].z);

        if (!ClientDataHolderVR.vrSettings.seated && ClientDataHolderVR.vrSettings.allowStandingOriginOffset) {
            if (ClientDataHolderVR.vr.isHMDTracking()) {
                vec3 = vec3.add(ClientDataHolderVR.vrSettings.originOffset.getX(), ClientDataHolderVR.vrSettings.originOffset.getY(), ClientDataHolderVR.vrSettings.originOffset.getZ());
            }
        }

        return vec3;
    }

    public Vec3 getAimVector(int controller) {
        Vector3 vector3 = this.controllerRotation[controller].transform(this.forward);
        return vector3.toVector3d();
    }

    public void triggerHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude) {
        this.triggerHapticPulse(controller, durationSeconds, frequency, amplitude, 0.0F);
    }

    public void triggerHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude, float delaySeconds) {
        if (!ClientDataHolderVR.vrSettings.seated) {
            if (ClientDataHolderVR.vrSettings.reverseHands) {
                if (controller == ControllerType.RIGHT) {
                    controller = ControllerType.LEFT;
                } else {
                    controller = ControllerType.RIGHT;
                }
            }

            this.hapticScheduler.queueHapticPulse(controller, durationSeconds, frequency, amplitude, delaySeconds);
        }
    }

    @Deprecated
    public void triggerHapticPulse(ControllerType controller, int strength) {
        if (strength >= 1) {
            this.triggerHapticPulse(controller, (float) strength / 1000000.0F, 160.0F, 1.0F);
        }
    }

    @Deprecated
    public void triggerHapticPulse(int controller, int strength) {
        if (controller >= 0 && controller < ControllerType.values().length) {
            this.triggerHapticPulse(ControllerType.values()[controller], strength);
        }
    }

    public org.vivecraft.common.utils.math.Matrix4f getHandRotation(int controller) {
        return this.handRotation[controller];
    }

    public Vec3 getHandVector(int controller) {
        Vector3 vector3 = new Vector3(0.0F, 0.0F, -1.0F);
        org.vivecraft.common.utils.math.Matrix4f matrix4f = this.handRotation[controller];
        Vector3 vector31 = matrix4f.transform(vector3);
        return vector31.toVector3d();
    }

    public Vec3 getCenterEyePosition() {
        Vector3 vector3 = Utils.convertMatrix4ftoTranslationVector(this.hmdPose);

        if (ClientDataHolderVR.vrSettings.seated || ClientDataHolderVR.vrSettings.allowStandingOriginOffset) {
            if (ClientDataHolderVR.vr.isHMDTracking()) {
                vector3 = vector3.add(ClientDataHolderVR.vrSettings.originOffset);
            }
        }

        return vector3.toVector3d();
    }

    public Vec3 getEyePosition(RenderPass eye) {
        org.vivecraft.common.utils.math.Matrix4f matrix4f = this.hmdPoseRightEye;

        if (eye == RenderPass.LEFT) {
            matrix4f = this.hmdPoseLeftEye;
        } else if (eye == RenderPass.RIGHT) {
            matrix4f = this.hmdPoseRightEye;
        } else {
            matrix4f = null;
        }

        if (matrix4f == null) {
            org.vivecraft.common.utils.math.Matrix4f matrix4f2 = this.hmdPose;
            Vector3 vector31 = Utils.convertMatrix4ftoTranslationVector(matrix4f2);

            if (ClientDataHolderVR.vrSettings.seated || ClientDataHolderVR.vrSettings.allowStandingOriginOffset) {
                if (ClientDataHolderVR.vr.isHMDTracking()) {
                    vector31 = vector31.add(ClientDataHolderVR.vrSettings.originOffset);
                }
            }

            return vector31.toVector3d();
        } else {
            org.vivecraft.common.utils.math.Matrix4f matrix4f1 = org.vivecraft.common.utils.math.Matrix4f.multiply(this.hmdPose, matrix4f);
            Vector3 vector3 = Utils.convertMatrix4ftoTranslationVector(matrix4f1);

            if (ClientDataHolderVR.vrSettings.seated || ClientDataHolderVR.vrSettings.allowStandingOriginOffset) {
                if (ClientDataHolderVR.vr.isHMDTracking()) {
                    vector3 = vector3.add(ClientDataHolderVR.vrSettings.originOffset);
                }
            }

            return vector3.toVector3d();
        }
    }

    public HardwareType getHardwareType() {
        return ClientDataHolderVR.vrSettings.forceHardwareDetection > 0 ? HardwareType.values()[ClientDataHolderVR.vrSettings.forceHardwareDetection - 1] : this.detectedHardware;
    }

    public Vec3 getHmdVector() {
        Vector3 vector3 = this.hmdRotation.transform(this.forward);
        return vector3.toVector3d();
    }

    public org.vivecraft.common.utils.math.Matrix4f getEyeRotation(RenderPass eye) {
        org.vivecraft.common.utils.math.Matrix4f matrix4f;

        if (eye == RenderPass.LEFT) {
            matrix4f = this.hmdPoseLeftEye;
        } else if (eye == RenderPass.RIGHT) {
            matrix4f = this.hmdPoseRightEye;
        } else {
            matrix4f = null;
        }

        if (matrix4f != null) {
            org.vivecraft.common.utils.math.Matrix4f matrix4f1 = new org.vivecraft.common.utils.math.Matrix4f();
            matrix4f1.M[0][0] = matrix4f.M[0][0];
            matrix4f1.M[0][1] = matrix4f.M[0][1];
            matrix4f1.M[0][2] = matrix4f.M[0][2];
            matrix4f1.M[0][3] = 0.0F;
            matrix4f1.M[1][0] = matrix4f.M[1][0];
            matrix4f1.M[1][1] = matrix4f.M[1][1];
            matrix4f1.M[1][2] = matrix4f.M[1][2];
            matrix4f1.M[1][3] = 0.0F;
            matrix4f1.M[2][0] = matrix4f.M[2][0];
            matrix4f1.M[2][1] = matrix4f.M[2][1];
            matrix4f1.M[2][2] = matrix4f.M[2][2];
            matrix4f1.M[2][3] = 0.0F;
            matrix4f1.M[3][0] = 0.0F;
            matrix4f1.M[3][1] = 0.0F;
            matrix4f1.M[3][2] = 0.0F;
            matrix4f1.M[3][3] = 1.0F;
            return org.vivecraft.common.utils.math.Matrix4f.multiply(this.hmdRotation, matrix4f1);
        } else {
            return this.hmdRotation;
        }
    }

    public VRInputAction getInputAction(String keyBindingDesc) {
        return this.inputActionsByKeyBinding.get(keyBindingDesc);
    }

    public VRInputAction getInputActionByName(String name) {
        return this.inputActions.get(name);
    }

    public Collection<VRInputAction> getInputActions() {
        return Collections.unmodifiableCollection(this.inputActions.values());
    }

    public VRInputAction getInputAction(KeyMapping keyBinding) {
        return this.getInputAction(keyBinding.getName());
    }

    public Collection<VRInputAction> getInputActionsInSet(VRInputActionSet set) {
        return Collections.unmodifiableCollection(this.inputActions.values().stream().filter((action) ->
        {
            return action.actionSet == set;
        }).collect(Collectors.toList()));
    }

    public boolean isControllerTracking(ControllerType controller) {
        return this.isControllerTracking(controller.ordinal());
    }

    public boolean isControllerTracking(int controller) {
        return this.controllerTracking[controller];
    }

    public void resetPosition() {
        Vec3 vec3 = this.getCenterEyePosition().scale(-1.0D).add(ClientDataHolderVR.vrSettings.originOffset.getX(), ClientDataHolderVR.vrSettings.originOffset.getY(), ClientDataHolderVR.vrSettings.originOffset.getZ());
        ClientDataHolderVR.vrSettings.originOffset = new Vector3((float) vec3.x, (float) vec3.y + 1.62F, (float) vec3.z);
    }

    public void clearOffset() {
        ClientDataHolderVR.vrSettings.originOffset = new Vector3(0.0F, 0.0F, 0.0F);
    }

    public boolean isHMDTracking() {
        return this.headIsTracking;
    }

    protected void processHotbar() {
        int previousSlot = ClientDataHolderVR.interactTracker.hotbar;
        ClientDataHolderVR.interactTracker.hotbar = -1;
        if (VRState.mc.player == null) {
            return;
        }
        if (VRState.mc.player.getInventory() == null) {
            return;
        }

        if (ClientDataHolderVR.climbTracker.isGrabbingLadder() &&
            ClientDataHolderVR.climbTracker.isClaws(VRState.mc.player.getMainHandItem())) {
            return;
        }
        if (!ClientDataHolderVR.interactTracker.isActive(VRState.mc.player)) {
            return;
        }

        Vec3 main = this.getAimSource(0);
        Vec3 off = this.getAimSource(1);
        Vec3 barStartos = null, barEndos = null;

        int i = 1;
        if (ClientDataHolderVR.vrSettings.reverseHands) {
            i = -1;
        }

        if (ClientDataHolderVR.vrSettings.vrHudLockMode == VRSettings.HUDLock.WRIST) {
            barStartos = this.getAimRotation(1).transform(new Vector3((float) i * 0.02F, 0.05F, 0.26F)).toVector3d();
            barEndos = this.getAimRotation(1).transform(new Vector3((float) i * 0.02F, 0.05F, 0.01F)).toVector3d();
        } else if (ClientDataHolderVR.vrSettings.vrHudLockMode == VRSettings.HUDLock.HAND) {
            barStartos = this.getAimRotation(1).transform(new Vector3((float) i * -0.18F, 0.08F, -0.01F)).toVector3d();
            barEndos = this.getAimRotation(1).transform(new Vector3((float) i * 0.19F, 0.04F, -0.08F)).toVector3d();
        } else {
            return; //how did u get here
        }


        Vec3 barStart = off.add(barStartos.x, barStartos.y, barStartos.z);
        Vec3 barEnd = off.add(barEndos.x, barEndos.y, barEndos.z);

        Vec3 u = barStart.subtract(barEnd);
        Vec3 pq = barStart.subtract(main);
        float dist = (float) (pq.cross(u).length() / u.length());

        if (dist > 0.06) {
            return;
        }

        float fact = (float) (pq.dot(u) / (u.x * u.x + u.y * u.y + u.z * u.z));

        if (fact < -1) {
            return;
        }

        Vec3 w2 = u.scale(fact).subtract(pq);

        Vec3 point = main.subtract(w2);
        float linelen = (float) u.length();
        float ilen = (float) barStart.subtract(point).length();
        if (fact < 0) {
            ilen *= -1;
        }
        float pos = ilen / linelen * 9;

        if (ClientDataHolderVR.vrSettings.reverseHands) {
            pos = 9 - pos;
        }

        int box = (int) Math.floor(pos);

        if (box > 8) {
            return;
        }
        if (box < 0) {
            if (pos <= -0.5 && pos >= -1.5) //TODO fix reversed hands situation.
            {
                box = 9;
            } else {
                return;
            }
        }
        //all that maths for this.
        ClientDataHolderVR.interactTracker.hotbar = box;
        if (previousSlot != ClientDataHolderVR.interactTracker.hotbar) {
            triggerHapticPulse(0, 750);
        }
    }

    protected KeyMapping findKeyBinding(String name) {
        return Stream.concat(Arrays.stream(VRState.mc.options.keyMappings), VivecraftVRMod.hiddenKeyBindingSet.stream()).filter((kb) ->
        {
            return name.equals(kb.getName());
        }).findFirst().orElse(null);
    }

    protected void hmdSampling() {
        if (this.hmdPosSamples.size() == this.hmdAvgLength) {
            this.hmdPosSamples.removeFirst();
        }

        if (this.hmdYawSamples.size() == this.hmdAvgLength) {
            this.hmdYawSamples.removeFirst();
        }

        float f = ClientDataHolderVR.vrPlayer.vrdata_room_pre.hmd.getYaw();

        if (f < 0.0F) {
            f += 360.0F;
        }

        this.hmdYawTotal += Utils.angleDiff(f, this.hmdYawLast);
        this.hmdYawLast = f;

        if (Math.abs(Utils.angleNormalize(this.hmdYawTotal) - this.hmdYawLast) > 1.0F || this.hmdYawTotal > 100000.0F) {
            this.hmdYawTotal = this.hmdYawLast;
            System.out.println("HMD yaw desync/overflow corrected");
        }

        this.hmdPosSamples.add(ClientDataHolderVR.vrPlayer.vrdata_room_pre.hmd.getPosition());
        float f1 = 0.0F;

        if (this.hmdYawSamples.size() > 0) {
            for (float f2 : this.hmdYawSamples) {
                f1 += f2;
            }

            f1 /= (float) this.hmdYawSamples.size();
        }

        if (Math.abs(this.hmdYawTotal - f1) > 20.0F) {
            this.trigger = true;
        }

        if (Math.abs(this.hmdYawTotal - f1) < 1.0F) {
            this.trigger = false;
        }

        if (this.trigger || this.hmdYawSamples.isEmpty()) {
            this.hmdYawSamples.add(this.hmdYawTotal);
        }
    }

    protected void updateAim() {
        RenderPassManager.setGUIRenderPass();


        if (VRState.mc != null) {
            this.hmdRotation.M[0][0] = this.hmdPose.M[0][0];
            this.hmdRotation.M[0][1] = this.hmdPose.M[0][1];
            this.hmdRotation.M[0][2] = this.hmdPose.M[0][2];
            this.hmdRotation.M[0][3] = 0.0F;
            this.hmdRotation.M[1][0] = this.hmdPose.M[1][0];
            this.hmdRotation.M[1][1] = this.hmdPose.M[1][1];
            this.hmdRotation.M[1][2] = this.hmdPose.M[1][2];
            this.hmdRotation.M[1][3] = 0.0F;
            this.hmdRotation.M[2][0] = this.hmdPose.M[2][0];
            this.hmdRotation.M[2][1] = this.hmdPose.M[2][1];
            this.hmdRotation.M[2][2] = this.hmdPose.M[2][2];
            this.hmdRotation.M[2][3] = 0.0F;
            this.hmdRotation.M[3][0] = 0.0F;
            this.hmdRotation.M[3][1] = 0.0F;
            this.hmdRotation.M[3][2] = 0.0F;
            this.hmdRotation.M[3][3] = 1.0F;
            Vec3 vec3 = this.getCenterEyePosition();
            this.hmdHistory.add(vec3);
            Vector3 vector3 = this.hmdRotation.transform(new Vector3(0.0F, -0.1F, 0.1F));
            this.hmdPivotHistory.add(new Vec3((double) vector3.getX() + vec3.x, (double) vector3.getY() + vec3.y, (double) vector3.getZ() + vec3.z));
            hmdRotHistory.add(new Quaternionf().setFromNormalized(hmdRotation.transposed().toMCMatrix().rotateY((float) -Math.toRadians(ClientDataHolderVR.vrSettings.worldRotation))));

            if (ClientDataHolderVR.vrSettings.seated) {
                this.controllerPose[0] = this.hmdPose.inverted().inverted();
                this.controllerPose[1] = this.hmdPose.inverted().inverted();
            }

            org.vivecraft.common.utils.math.Matrix4f[] amatrix4f = new org.vivecraft.common.utils.math.Matrix4f[]{new org.vivecraft.common.utils.math.Matrix4f(), new org.vivecraft.common.utils.math.Matrix4f()};
            org.vivecraft.common.utils.math.Matrix4f[] amatrix4f1 = new org.vivecraft.common.utils.math.Matrix4f[]{new org.vivecraft.common.utils.math.Matrix4f(), new org.vivecraft.common.utils.math.Matrix4f()};

            if (ClientDataHolderVR.vrSettings.seated) {
                amatrix4f1[0] = this.controllerPose[0];
            } else {
                amatrix4f1[0] = org.vivecraft.common.utils.math.Matrix4f.multiply(this.controllerPose[0], this.getControllerComponentTransform(0, "handgrip"));
            }

            this.handRotation[0].M[0][0] = amatrix4f1[0].M[0][0];
            this.handRotation[0].M[0][1] = amatrix4f1[0].M[0][1];
            this.handRotation[0].M[0][2] = amatrix4f1[0].M[0][2];
            this.handRotation[0].M[0][3] = 0.0F;
            this.handRotation[0].M[1][0] = amatrix4f1[0].M[1][0];
            this.handRotation[0].M[1][1] = amatrix4f1[0].M[1][1];
            this.handRotation[0].M[1][2] = amatrix4f1[0].M[1][2];
            this.handRotation[0].M[1][3] = 0.0F;
            this.handRotation[0].M[2][0] = amatrix4f1[0].M[2][0];
            this.handRotation[0].M[2][1] = amatrix4f1[0].M[2][1];
            this.handRotation[0].M[2][2] = amatrix4f1[0].M[2][2];
            this.handRotation[0].M[2][3] = 0.0F;
            this.handRotation[0].M[3][0] = 0.0F;
            this.handRotation[0].M[3][1] = 0.0F;
            this.handRotation[0].M[3][2] = 0.0F;
            this.handRotation[0].M[3][3] = 1.0F;

            if (ClientDataHolderVR.vrSettings.seated) {
                amatrix4f[0] = this.controllerPose[0];
            } else {
                amatrix4f[0] = org.vivecraft.common.utils.math.Matrix4f.multiply(this.controllerPose[0], this.getControllerComponentTransform(0, "tip"));
            }

            Vector3 vector31 = Utils.convertMatrix4ftoTranslationVector(amatrix4f[0]);
            this.aimSource[0] = vector31.toVector3d();
            this.controllerHistory[0].add(this.getAimSource(0));
            this.controllerRotation[0].M[0][0] = amatrix4f[0].M[0][0];
            this.controllerRotation[0].M[0][1] = amatrix4f[0].M[0][1];
            this.controllerRotation[0].M[0][2] = amatrix4f[0].M[0][2];
            this.controllerRotation[0].M[0][3] = 0.0F;
            this.controllerRotation[0].M[1][0] = amatrix4f[0].M[1][0];
            this.controllerRotation[0].M[1][1] = amatrix4f[0].M[1][1];
            this.controllerRotation[0].M[1][2] = amatrix4f[0].M[1][2];
            this.controllerRotation[0].M[1][3] = 0.0F;
            this.controllerRotation[0].M[2][0] = amatrix4f[0].M[2][0];
            this.controllerRotation[0].M[2][1] = amatrix4f[0].M[2][1];
            this.controllerRotation[0].M[2][2] = amatrix4f[0].M[2][2];
            this.controllerRotation[0].M[2][3] = 0.0F;
            this.controllerRotation[0].M[3][0] = 0.0F;
            this.controllerRotation[0].M[3][1] = 0.0F;
            this.controllerRotation[0].M[3][2] = 0.0F;
            this.controllerRotation[0].M[3][3] = 1.0F;
            Vec3 vec31 = this.getHmdVector();

            if (ClientDataHolderVR.vrSettings.seated && VRState.mc.screen == null) {
                Matrix4f matrix4f = new Matrix4f();
                float f = 110.0F;
                float f1 = 180.0F;
                double d0 = VRState.mc.mouseHandler.xpos() / (double) VRState.mc.getWindow().getScreenWidth() * (double) f - (double) (f / 2.0F);
                int i = VRState.mc.getWindow().getScreenHeight();

                if (i % 2 != 0) {
                    --i;
                }

                double d1 = -VRState.mc.mouseHandler.ypos() / (double) i * (double) f1 + (double) (f1 / 2.0F);
                double d2 = -d1;

                if (VRState.mc.isWindowActive()) {
                    float f2 = ClientDataHolderVR.vrSettings.keyholeX;
                    float f3 = 20.0F * ClientDataHolderVR.vrSettings.xSensitivity;
                    int j = (int) ((double) (-f2 + f / 2.0F) * (double) VRState.mc.getWindow().getScreenWidth() / (double) f) + 1;
                    int k = (int) ((double) (f2 + f / 2.0F) * (double) VRState.mc.getWindow().getScreenWidth() / (double) f) - 1;
                    float f4 = ((float) Math.abs(d0) - f2) / (f / 2.0F - f2);
                    double d3 = VRState.mc.mouseHandler.xpos();

                    if (d0 < (double) (-f2)) {
                        this.seatedRot += f3 * f4;
                        this.seatedRot %= 360.0F;
                        this.hmdForwardYaw = (float) Math.toDegrees(Math.atan2(vec31.x, vec31.z));
                        d3 = j;
                        d0 = -f2;
                    } else if (d0 > (double) f2) {
                        this.seatedRot -= f3 * f4;
                        this.seatedRot %= 360.0F;
                        this.hmdForwardYaw = (float) Math.toDegrees(Math.atan2(vec31.x, vec31.z));
                        d3 = k;
                        d0 = f2;
                    }

                    double d4 = 0.5D * (double) ClientDataHolderVR.vrSettings.ySensitivity;
                    d2 = (double) this.aimPitch + d1 * d4;
                    d2 = Mth.clamp(d2, -89.9D, 89.9D);
                    InputSimulator.setMousePos(d3, i / 2);
                    GLFW.glfwSetCursorPos(VRState.mc.getWindow().getWindow(), d3, i / 2);
                    matrix4f.rotate((float) Math.toRadians(-d2), new Vector3f(1.0F, 0.0F, 0.0F));
                    matrix4f.rotate((float) Math.toRadians(-180.0D + d0 - (double) this.hmdForwardYaw), new Vector3f(0.0F, 1.0F, 0.0F));
                }

                this.controllerRotation[0].M[0][0] = matrix4f.m00;
                this.controllerRotation[0].M[0][1] = matrix4f.m01;
                this.controllerRotation[0].M[0][2] = matrix4f.m02;
                this.controllerRotation[0].M[1][0] = matrix4f.m10;
                this.controllerRotation[0].M[1][1] = matrix4f.m11;
                this.controllerRotation[0].M[1][2] = matrix4f.m12;
                this.controllerRotation[0].M[2][0] = matrix4f.m20;
                this.controllerRotation[0].M[2][1] = matrix4f.m21;
                this.controllerRotation[0].M[2][2] = matrix4f.m22;

                this.handRotation[0].M[0][0] = matrix4f.m00;
                this.handRotation[0].M[0][1] = matrix4f.m01;
                this.handRotation[0].M[0][2] = matrix4f.m02;
                this.handRotation[0].M[1][0] = matrix4f.m10;
                this.handRotation[0].M[1][1] = matrix4f.m11;
                this.handRotation[0].M[1][2] = matrix4f.m12;
                this.handRotation[0].M[2][0] = matrix4f.m20;
                this.handRotation[0].M[2][1] = matrix4f.m21;
                this.handRotation[0].M[2][2] = matrix4f.m22;
            }

            Vec3 vec32 = this.getAimVector(0);
            this.aimPitch = (float) Math.toDegrees(Math.asin(vec32.y / vec32.length()));
            this.controllerForwardHistory[0].add(vec32);
            Vec3 vec33 = this.controllerRotation[0].transform(this.up).toVector3d();
            this.controllerUpHistory[0].add(vec33);

            if (ClientDataHolderVR.vrSettings.seated) {
                amatrix4f1[1] = this.controllerPose[1];
            } else {
                amatrix4f1[1] = org.vivecraft.common.utils.math.Matrix4f.multiply(this.controllerPose[1], this.getControllerComponentTransform(1, "handgrip"));
            }

            this.handRotation[1].M[0][0] = amatrix4f1[1].M[0][0];
            this.handRotation[1].M[0][1] = amatrix4f1[1].M[0][1];
            this.handRotation[1].M[0][2] = amatrix4f1[1].M[0][2];
            this.handRotation[1].M[0][3] = 0.0F;
            this.handRotation[1].M[1][0] = amatrix4f1[1].M[1][0];
            this.handRotation[1].M[1][1] = amatrix4f1[1].M[1][1];
            this.handRotation[1].M[1][2] = amatrix4f1[1].M[1][2];
            this.handRotation[1].M[1][3] = 0.0F;
            this.handRotation[1].M[2][0] = amatrix4f1[1].M[2][0];
            this.handRotation[1].M[2][1] = amatrix4f1[1].M[2][1];
            this.handRotation[1].M[2][2] = amatrix4f1[1].M[2][2];
            this.handRotation[1].M[2][3] = 0.0F;
            this.handRotation[1].M[3][0] = 0.0F;
            this.handRotation[1].M[3][1] = 0.0F;
            this.handRotation[1].M[3][2] = 0.0F;
            this.handRotation[1].M[3][3] = 1.0F;

            if (ClientDataHolderVR.vrSettings.seated) {
                amatrix4f[1] = this.controllerPose[1];
            } else {
                amatrix4f[1] = org.vivecraft.common.utils.math.Matrix4f.multiply(this.controllerPose[1], this.getControllerComponentTransform(1, "tip"));
            }

            vector31 = Utils.convertMatrix4ftoTranslationVector(amatrix4f[1]);
            this.aimSource[1] = vector31.toVector3d();
            this.controllerHistory[1].add(this.getAimSource(1));
            this.controllerRotation[1].M[0][0] = amatrix4f[1].M[0][0];
            this.controllerRotation[1].M[0][1] = amatrix4f[1].M[0][1];
            this.controllerRotation[1].M[0][2] = amatrix4f[1].M[0][2];
            this.controllerRotation[1].M[0][3] = 0.0F;
            this.controllerRotation[1].M[1][0] = amatrix4f[1].M[1][0];
            this.controllerRotation[1].M[1][1] = amatrix4f[1].M[1][1];
            this.controllerRotation[1].M[1][2] = amatrix4f[1].M[1][2];
            this.controllerRotation[1].M[1][3] = 0.0F;
            this.controllerRotation[1].M[2][0] = amatrix4f[1].M[2][0];
            this.controllerRotation[1].M[2][1] = amatrix4f[1].M[2][1];
            this.controllerRotation[1].M[2][2] = amatrix4f[1].M[2][2];
            this.controllerRotation[1].M[2][3] = 0.0F;
            this.controllerRotation[1].M[3][0] = 0.0F;
            this.controllerRotation[1].M[3][1] = 0.0F;
            this.controllerRotation[1].M[3][2] = 0.0F;
            this.controllerRotation[1].M[3][3] = 1.0F;
            vec31 = this.getAimVector(1);
            this.controllerForwardHistory[1].add(vec31);
            vec32 = this.controllerRotation[1].transform(this.up).toVector3d();
            this.controllerUpHistory[1].add(vec32);

            if (ClientDataHolderVR.vrSettings.seated) {
                this.aimSource[1] = this.getCenterEyePosition();
                this.aimSource[0] = this.getCenterEyePosition();
            }

            boolean flag = false;

            if (flag) {
                this.controllerPose[2] = this.controllerPose[0];
            }

            this.controllerRotation[2].M[0][0] = this.controllerPose[2].M[0][0];
            this.controllerRotation[2].M[0][1] = this.controllerPose[2].M[0][1];
            this.controllerRotation[2].M[0][2] = this.controllerPose[2].M[0][2];
            this.controllerRotation[2].M[0][3] = 0.0F;
            this.controllerRotation[2].M[1][0] = this.controllerPose[2].M[1][0];
            this.controllerRotation[2].M[1][1] = this.controllerPose[2].M[1][1];
            this.controllerRotation[2].M[1][2] = this.controllerPose[2].M[1][2];
            this.controllerRotation[2].M[1][3] = 0.0F;
            this.controllerRotation[2].M[2][0] = this.controllerPose[2].M[2][0];
            this.controllerRotation[2].M[2][1] = this.controllerPose[2].M[2][1];
            this.controllerRotation[2].M[2][2] = this.controllerPose[2].M[2][2];
            this.controllerRotation[2].M[2][3] = 0.0F;
            this.controllerRotation[2].M[3][0] = 0.0F;
            this.controllerRotation[2].M[3][1] = 0.0F;
            this.controllerRotation[2].M[3][2] = 0.0F;
            this.controllerRotation[2].M[3][3] = 1.0F;

            if ((!this.hasThirdController() || ClientDataHolderVR.vrSettings.displayMirrorMode != VRSettings.MirrorMode.MIXED_REALITY && ClientDataHolderVR.vrSettings.displayMirrorMode != VRSettings.MirrorMode.THIRD_PERSON) && !flag) {
                this.mrMovingCamActive = false;
                this.aimSource[2] = new Vec3(ClientDataHolderVR.vrSettings.vrFixedCamposX, ClientDataHolderVR.vrSettings.vrFixedCamposY, ClientDataHolderVR.vrSettings.vrFixedCamposZ);
            } else {
                this.mrMovingCamActive = true;
                Vector3 vector32 = Utils.convertMatrix4ftoTranslationVector(this.controllerPose[2]);
                this.aimSource[2] = vector32.toVector3d();
            }
        }
    }

    public void processBindings() {
        if (!this.inputActions.isEmpty()) {
            boolean flag = VRState.mc.level != null && VRState.mc.player != null && VRState.mc.player.isSleeping();
            boolean flag1 = VRState.mc.screen != null;
            boolean flag2 = VivecraftVRMod.keyToggleMovement.consumeClick();

            if (!VRState.mc.options.keyPickItem.isDown() && !flag2) {
                this.moveModeSwitchCount = 0;
            } else if (++this.moveModeSwitchCount == 80 || flag2) {
                if (ClientDataHolderVR.vrSettings.seated) {
                    ClientDataHolderVR.vrSettings.seatedFreeMove = !ClientDataHolderVR.vrSettings.seatedFreeMove;
                    VRState.mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.movementmodeswitch", ClientDataHolderVR.vrSettings.seatedFreeMove ? Component.translatable("vivecraft.options.freemove") : Component.translatable("vivecraft.options.teleport")));
                } else if (ClientDataHolderVR.vrPlayer.isTeleportSupported()) {
                    ClientDataHolderVR.vrSettings.forceStandingFreeMove = !ClientDataHolderVR.vrSettings.forceStandingFreeMove;
                    VRState.mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.movementmodeswitch", ClientDataHolderVR.vrSettings.seatedFreeMove ? Component.translatable("vivecraft.options.freemove") : Component.translatable("vivecraft.options.teleport")));
                } else if (ClientDataHolderVR.vrPlayer.isTeleportOverridden()) {
                    ClientDataHolderVR.vrPlayer.setTeleportOverride(false);
                    VRState.mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.teleportdisabled"));
                } else {
                    ClientDataHolderVR.vrPlayer.setTeleportOverride(true);
                    VRState.mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.teleportenabled"));
                }
            }

            Vec3 vec3 = this.getAimVector(0);
            Vec3 vec31 = this.getAimVector(1);
            float f = (float) Math.toDegrees(Math.atan2(-vec3.x, vec3.z));
            float f1 = (float) Math.toDegrees(Math.atan2(-vec31.x, vec31.z));

            if (!flag1) {
                if (VivecraftVRMod.keyWalkabout.isDown()) {
                    float f2 = f;
                    ControllerType controllertype = this.findActiveBindingControllerType(VivecraftVRMod.keyWalkabout);

                    if (controllertype != null && controllertype == ControllerType.LEFT) {
                        f2 = f1;
                    }

                    if (!this.isWalkingAbout) {
                        this.isWalkingAbout = true;
                        this.walkaboutYawStart = ClientDataHolderVR.vrSettings.worldRotation - f2;
                    } else {
                        ClientDataHolderVR.vrSettings.worldRotation = this.walkaboutYawStart + f2;
                        ClientDataHolderVR.vrSettings.worldRotation %= 360.0F;
                    }
                } else {
                    this.isWalkingAbout = false;
                }

                if (VivecraftVRMod.keyRotateFree.isDown()) {
                    float f3 = f;
                    ControllerType controllertype5 = this.findActiveBindingControllerType(VivecraftVRMod.keyRotateFree);

                    if (controllertype5 != null && controllertype5 == ControllerType.LEFT) {
                        f3 = f1;
                    }

                    if (!this.isFreeRotate) {
                        this.isFreeRotate = true;
                        this.walkaboutYawStart = ClientDataHolderVR.vrSettings.worldRotation + f3;
                    } else {
                        ClientDataHolderVR.vrSettings.worldRotation = this.walkaboutYawStart - f3;
                    }
                } else {
                    this.isFreeRotate = false;
                }
            }

            if (VivecraftVRMod.keyHotbarNext.consumeClick()) {
                this.changeHotbar(-1);
                this.triggerBindingHapticPulse(VivecraftVRMod.keyHotbarNext, 250);
            }

            if (VivecraftVRMod.keyHotbarPrev.consumeClick()) {
                this.changeHotbar(1);
                this.triggerBindingHapticPulse(VivecraftVRMod.keyHotbarPrev, 250);
            }

            if (VivecraftVRMod.keyQuickTorch.consumeClick() && VRState.mc.player != null) {
                for (int j = 0; j < 9; ++j) {
                    ItemStack itemstack = VRState.mc.player.getInventory().getItem(j);

                    if (itemstack.getItem() instanceof BlockItem && ((BlockItem) itemstack.getItem()).getBlock() instanceof TorchBlock && VRState.mc.screen == null) {
                        this.quickTorchPreviousSlot = VRState.mc.player.getInventory().selected;
                        VRState.mc.player.getInventory().selected = j;
                        ((MinecraftAccessor) VRState.mc).callStartUseItem();
                        VRState.mc.player.getInventory().selected = this.quickTorchPreviousSlot;
                        this.quickTorchPreviousSlot = -1;
                        break;
                    }
                }
            }

            if (flag1 && !flag && VRState.mc.options.keyUp.isDown() && !(VRState.mc.screen instanceof WinScreen) && VRState.mc.player != null) {
                VRState.mc.player.closeContainer();
            }

            if (VRState.mc.screen instanceof AbstractContainerScreen && VRState.mc.options.keyInventory.consumeClick() && VRState.mc.player != null) {
                VRState.mc.player.closeContainer();
            }

            if (VRState.mc.screen instanceof ChatScreen && VRState.mc.options.keyChat.consumeClick()) {
                VRState.mc.setScreen(null);
            }

            if (ClientDataHolderVR.vrSettings.worldRotationIncrement == 0.0F) {
                float f4 = this.getInputAction(VivecraftVRMod.keyRotateAxis).getAxis2DUseTracked().getX();

                if (f4 == 0.0F) {
                    f4 = this.getInputAction(VivecraftVRMod.keyFreeMoveRotate).getAxis2DUseTracked().getX();
                }

                if (f4 != 0.0F) {
                    float f8 = 10.0F * f4;
                    ClientDataHolderVR.vrSettings.worldRotation -= f8;
                    ClientDataHolderVR.vrSettings.worldRotation %= 360.0F;
                }
            } else if (VivecraftVRMod.keyRotateAxis.consumeClick() || VivecraftVRMod.keyFreeMoveRotate.consumeClick()) {
                float f5 = this.getInputAction(VivecraftVRMod.keyRotateAxis).getAxis2D(false).getX();

                if (f5 == 0.0F) {
                    f5 = this.getInputAction(VivecraftVRMod.keyFreeMoveRotate).getAxis2D(false).getX();
                }

                if (Math.abs(f5) > 0.5F) {
                    ClientDataHolderVR.vrSettings.worldRotation -= ClientDataHolderVR.vrSettings.worldRotationIncrement * Math.signum(f5);
                    ClientDataHolderVR.vrSettings.worldRotation %= 360.0F;
                }
            }

            if (ClientDataHolderVR.vrSettings.worldRotationIncrement == 0.0F) {
                float f6 = VivecraftMovementInput.getMovementAxisValue(VivecraftVRMod.keyRotateLeft);

                if (f6 > 0.0F) {
                    float f9 = 5.0F;

                    if (f6 > 0.0F) {
                        f9 = 10.0F * f6;
                    }

                    ClientDataHolderVR.vrSettings.worldRotation += f9;
                    ClientDataHolderVR.vrSettings.worldRotation %= 360.0F;
                }
            } else if (VivecraftVRMod.keyRotateLeft.consumeClick()) {
                ClientDataHolderVR.vrSettings.worldRotation += ClientDataHolderVR.vrSettings.worldRotationIncrement;
                ClientDataHolderVR.vrSettings.worldRotation %= 360.0F;
            }

            if (ClientDataHolderVR.vrSettings.worldRotationIncrement == 0.0F) {
                float f7 = VivecraftMovementInput.getMovementAxisValue(VivecraftVRMod.keyRotateRight);

                if (f7 > 0.0F) {
                    float f10 = 5.0F;

                    if (f7 > 0.0F) {
                        f10 = 10.0F * f7;
                    }

                    ClientDataHolderVR.vrSettings.worldRotation -= f10;
                    ClientDataHolderVR.vrSettings.worldRotation %= 360.0F;
                }
            } else if (VivecraftVRMod.keyRotateRight.consumeClick()) {
                ClientDataHolderVR.vrSettings.worldRotation -= ClientDataHolderVR.vrSettings.worldRotationIncrement;
                ClientDataHolderVR.vrSettings.worldRotation %= 360.0F;
            }

            this.seatedRot = ClientDataHolderVR.vrSettings.worldRotation;

            if (VivecraftVRMod.keyRadialMenu.consumeClick() && !flag1) {
                ControllerType controllertype1 = this.findActiveBindingControllerType(VivecraftVRMod.keyRadialMenu);

                if (controllertype1 != null) {
                    RadialHandler.setOverlayShowing(!RadialHandler.isShowing(), controllertype1);
                }
            }

            if (VivecraftVRMod.keySwapMirrorView.consumeClick()) {
                if (ClientDataHolderVR.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON) {
                    ClientDataHolderVR.vrSettings.displayMirrorMode = VRSettings.MirrorMode.FIRST_PERSON;
                } else if (ClientDataHolderVR.vrSettings.displayMirrorMode == VRSettings.MirrorMode.FIRST_PERSON) {
                    ClientDataHolderVR.vrSettings.displayMirrorMode = VRSettings.MirrorMode.THIRD_PERSON;
                }

                if (!ShadersHelper.isShaderActive()) {
                    ClientDataHolderVR.vrRenderer.reinitFrameBuffers("Mirror Setting Changed");
                } else {
                    // in case if the last third person mirror was mixed reality
                    ClientDataHolderVR.vrRenderer.resizeFrameBuffers("Mirror Setting Changed");
                }
            }

            if (VivecraftVRMod.keyToggleKeyboard.consumeClick()) {
                KeyboardHandler.setOverlayShowing(!KeyboardHandler.Showing);
            }

            if (VivecraftVRMod.keyMoveThirdPersonCam.consumeClick() && !ClientDataHolderVR.kiosk && !ClientDataHolderVR.vrSettings.seated && (ClientDataHolderVR.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY || ClientDataHolderVR.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON)) {
                ControllerType controllertype2 = this.findActiveBindingControllerType(VivecraftVRMod.keyMoveThirdPersonCam);

                if (controllertype2 != null) {
                    VRHotkeys.startMovingThirdPersonCam(controllertype2.ordinal(), VRHotkeys.Triggerer.BINDING);
                }
            }

            if (!VivecraftVRMod.keyMoveThirdPersonCam.isDown() && VRHotkeys.isMovingThirdPersonCam() && VRHotkeys.getMovingThirdPersonCamTriggerer() == VRHotkeys.Triggerer.BINDING) {
                VRHotkeys.stopMovingThirdPersonCam();
                ClientDataHolderVR.vrSettings.saveOptions();
            }

            if (VRHotkeys.isMovingThirdPersonCam() && VRHotkeys.getMovingThirdPersonCamTriggerer() == VRHotkeys.Triggerer.MENUBUTTON && VivecraftVRMod.keyMenuButton.consumeClick()) {
                VRHotkeys.stopMovingThirdPersonCam();
                ClientDataHolderVR.vrSettings.saveOptions();
            }

            if (KeyboardHandler.Showing && VRState.mc.screen == null && VivecraftVRMod.keyMenuButton.consumeClick()) {
                KeyboardHandler.setOverlayShowing(false);
            }

            if (RadialHandler.isShowing() && VivecraftVRMod.keyMenuButton.consumeClick()) {
                RadialHandler.setOverlayShowing(false, null);
            }

            if (VivecraftVRMod.keyMenuButton.consumeClick()) {
                if (!flag1) {
                    if (!ClientDataHolderVR.kiosk) {
                        VRState.mc.pauseGame(false);
                    }
                } else {
                    InputSimulator.pressKey(256);
                    InputSimulator.releaseKey(256);
                }

                KeyboardHandler.setOverlayShowing(false);
            }

            if (VivecraftVRMod.keyTogglePlayerList.consumeClick()) {
                ((GuiExtension) VRState.mc.gui).vivecraft$setShowPlayerList(!((GuiExtension) VRState.mc.gui).vivecraft$getShowPlayerList());
            }

            if (VivecraftVRMod.keyToggleHandheldCam.consumeClick() && VRState.mc.player != null) {
                ClientDataHolderVR.cameraTracker.toggleVisibility();

                if (ClientDataHolderVR.cameraTracker.isVisible()) {
                    ControllerType controllertype3 = this.findActiveBindingControllerType(VivecraftVRMod.keyToggleHandheldCam);

                    if (controllertype3 == null) {
                        controllertype3 = ControllerType.RIGHT;
                    }

                    VRData.VRDevicePose vrdata$vrdevicepose = ClientDataHolderVR.vrPlayer.vrdata_world_pre.getController(controllertype3.ordinal());
                    ClientDataHolderVR.cameraTracker.setPosition(vrdata$vrdevicepose.getPosition());
                    ClientDataHolderVR.cameraTracker.setRotation(new Quaternion(vrdata$vrdevicepose.getMatrix().transposed()));
                }
            }

            if (VivecraftVRMod.keyQuickHandheldCam.consumeClick() && VRState.mc.player != null) {
                if (!ClientDataHolderVR.cameraTracker.isVisible()) {
                    ClientDataHolderVR.cameraTracker.toggleVisibility();
                }

                ControllerType controllertype4 = this.findActiveBindingControllerType(VivecraftVRMod.keyQuickHandheldCam);

                if (controllertype4 == null) {
                    controllertype4 = ControllerType.RIGHT;
                }

                VRData.VRDevicePose vrdata$vrdevicepose1 = ClientDataHolderVR.vrPlayer.vrdata_world_pre.getController(controllertype4.ordinal());
                ClientDataHolderVR.cameraTracker.setPosition(vrdata$vrdevicepose1.getPosition());
                ClientDataHolderVR.cameraTracker.setRotation(new Quaternion(vrdata$vrdevicepose1.getMatrix().transposed()));
                ClientDataHolderVR.cameraTracker.startMoving(controllertype4.ordinal(), true);
            }

            if (!VivecraftVRMod.keyQuickHandheldCam.isDown() && ClientDataHolderVR.cameraTracker.isMoving() && ClientDataHolderVR.cameraTracker.isQuickMode() && VRState.mc.player != null) {
                ClientDataHolderVR.cameraTracker.stopMoving();
                ClientDataHolderVR.grabScreenShot = true;
            }

            GuiHandler.processBindingsGui();
            RadialHandler.processBindings();
            KeyboardHandler.processBindings();
            ClientDataHolderVR.interactTracker.processBindings();
        }
    }

    public void populateInputActions() {
        Map<String, ActionParams> map = this.getSpecialActionParams();

        // iterate over all minecraft keys, and our hidden keys
        for (KeyMapping keymapping : Stream.concat(Arrays.stream(VRState.mc.options.keyMappings), VivecraftVRMod.hiddenKeyBindingSet.stream()).toList()) {
            ActionParams actionparams = map.getOrDefault(keymapping.getName(), new ActionParams("optional", "boolean", null));
            VRInputAction vrinputaction = new VRInputAction(keymapping, actionparams.requirement, actionparams.type, actionparams.actionSetOverride);
            this.inputActions.put(vrinputaction.name, vrinputaction);
        }

        for (VRInputAction vrinputaction1 : this.inputActions.values()) {
            this.inputActionsByKeyBinding.put(vrinputaction1.keyBinding.getName(), vrinputaction1);
        }

        this.getInputAction(VivecraftVRMod.keyVRInteract).setPriority(5).setEnabled(false);
        this.getInputAction(VivecraftVRMod.keyClimbeyGrab).setPriority(10).setEnabled(false);
        this.getInputAction(VivecraftVRMod.keyClimbeyJump).setEnabled(false);
        this.getInputAction(GuiHandler.keyKeyboardClick).setPriority(50);
        this.getInputAction(GuiHandler.keyKeyboardShift).setPriority(50);
    }

    public Map<String, ActionParams> getSpecialActionParams() {
        Map<String, ActionParams> map = new HashMap<>();
        this.addActionParams(map, VRState.mc.options.keyUp, "optional", "vector1", null);
        this.addActionParams(map, VRState.mc.options.keyDown, "optional", "vector1", null);
        this.addActionParams(map, VRState.mc.options.keyLeft, "optional", "vector1", null);
        this.addActionParams(map, VRState.mc.options.keyRight, "optional", "vector1", null);
        this.addActionParams(map, VRState.mc.options.keyInventory, "suggested", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, VRState.mc.options.keyAttack, "suggested", "boolean", null);
        this.addActionParams(map, VRState.mc.options.keyUse, "suggested", "boolean", null);
        this.addActionParams(map, VRState.mc.options.keyChat, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, VivecraftVRMod.keyHotbarScroll, "optional", "vector2", null);
        this.addActionParams(map, VivecraftVRMod.keyHotbarSwipeX, "optional", "vector2", null);
        this.addActionParams(map, VivecraftVRMod.keyHotbarSwipeY, "optional", "vector2", null);
        this.addActionParams(map, VivecraftVRMod.keyMenuButton, "suggested", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, VivecraftVRMod.keyTeleportFallback, "suggested", "vector1", null);
        this.addActionParams(map, VivecraftVRMod.keyFreeMoveRotate, "optional", "vector2", null);
        this.addActionParams(map, VivecraftVRMod.keyFreeMoveStrafe, "optional", "vector2", null);
        this.addActionParams(map, VivecraftVRMod.keyRotateLeft, "optional", "vector1", null);
        this.addActionParams(map, VivecraftVRMod.keyRotateRight, "optional", "vector1", null);
        this.addActionParams(map, VivecraftVRMod.keyRotateAxis, "optional", "vector2", null);
        this.addActionParams(map, VivecraftVRMod.keyRadialMenu, "suggested", "boolean", null);
        this.addActionParams(map, VivecraftVRMod.keySwapMirrorView, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, VivecraftVRMod.keyToggleKeyboard, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, VivecraftVRMod.keyMoveThirdPersonCam, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, VivecraftVRMod.keyToggleHandheldCam, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, VivecraftVRMod.keyQuickHandheldCam, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, VivecraftVRMod.keyTrackpadTouch, "optional", "boolean", VRInputActionSet.TECHNICAL);
        this.addActionParams(map, VivecraftVRMod.keyVRInteract, "suggested", "boolean", VRInputActionSet.CONTEXTUAL);
        this.addActionParams(map, VivecraftVRMod.keyClimbeyGrab, "suggested", "boolean", null);
        this.addActionParams(map, VivecraftVRMod.keyClimbeyJump, "suggested", "boolean", null);
        this.addActionParams(map, GuiHandler.keyLeftClick, "suggested", "boolean", null);
        this.addActionParams(map, GuiHandler.keyScrollAxis, "optional", "vector2", null);
        this.addActionParams(map, GuiHandler.keyRightClick, "suggested", "boolean", null);
        this.addActionParams(map, GuiHandler.keyShift, "suggested", "boolean", null);
        this.addActionParams(map, GuiHandler.keyKeyboardClick, "suggested", "boolean", null);
        this.addActionParams(map, GuiHandler.keyKeyboardShift, "suggested", "boolean", null);
        File file1 = new File("customactionsets.txt");

        if (file1.exists()) {
            System.out.println("Loading custom action set definitions...");
            String s;

            try (BufferedReader bufferedreader = new BufferedReader(new FileReader(file1))) {
                while ((s = bufferedreader.readLine()) != null) {
                    String[] astring = s.split(":", 2);

                    if (astring.length < 2) {
                        System.out.println("Invalid tokens: " + s);
                    } else {
                        KeyMapping keymapping = this.findKeyBinding(astring[0]);

                        if (keymapping == null) {
                            System.out.println("Unknown key binding: " + astring[0]);
                        } else if (VivecraftVRMod.allKeyBindingSet.contains(keymapping)) {
                            System.out.println("NO! Don't touch Vivecraft bindings!");
                        } else {
                            VRInputActionSet vrinputactionset = null;
                            String s1 = astring[1].toLowerCase();

                            switch (s1) {
                                case "ingame":
                                    vrinputactionset = VRInputActionSet.INGAME;
                                    break;

                                case "gui":
                                    vrinputactionset = VRInputActionSet.GUI;
                                    break;

                                case "global":
                                    vrinputactionset = VRInputActionSet.GLOBAL;
                            }

                            if (vrinputactionset == null) {
                                System.out.println("Unknown action set: " + astring[1]);
                            } else {
                                this.addActionParams(map, keymapping, "optional", "boolean", vrinputactionset);
                            }
                        }
                    }
                }
            } catch (IOException ioexception) {
                ioexception.printStackTrace();
            }
        }

        return map;
    }

    protected void changeHotbar(int dir) {
        if (VRState.mc.player != null && (!ClientDataHolderVR.climbTracker.isGrabbingLadder() || !ClientDataHolderVR.climbTracker.isClaws(VRState.mc.player.getMainHandItem()))) {
            if (VRState.mc.screen == null) {
                InputSimulator.scrollMouse(0.0D, dir * 4);
            } else {
                VRState.mc.player.getInventory().swapPaint(dir);
            }
        }
    }

    private void addActionParams(Map<String, ActionParams> map, KeyMapping keyBinding, String requirement, String type, VRInputActionSet actionSetOverride) {
        ActionParams actionparams = new ActionParams(requirement, type, actionSetOverride);
        map.put(keyBinding.getName(), actionparams);
    }

    protected abstract void triggerBindingHapticPulse(KeyMapping var1, int var2);

    protected abstract ControllerType findActiveBindingControllerType(KeyMapping var1);

    public abstract void poll(long var1);

    public abstract Vector2f getPlayAreaSize();

    public abstract boolean init();

    public abstract boolean postinit() throws RenderConfigException;

    public abstract org.vivecraft.common.utils.math.Matrix4f getControllerComponentTransform(int var1, String var2);

    public abstract boolean hasThirdController();

    public abstract List<Long> getOrigins(VRInputAction var1);

    public abstract String getOriginName(long l);

    public abstract VRRenderer createVRRenderer();

    public abstract boolean isActive();
}
