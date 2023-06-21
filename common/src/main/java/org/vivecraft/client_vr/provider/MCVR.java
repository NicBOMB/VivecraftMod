package org.vivecraft.client_vr.provider;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import org.joml.Vector2f;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.extensions.GuiExtension;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.Vec3History;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;
import org.vivecraft.client_vr.provider.openvr_lwjgl.control.VRInputActionSet;
import org.vivecraft.client_vr.provider.openvr_lwjgl.control.VivecraftMovementInput;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.common.utils.lwjgl.Matrix4f;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.common.utils.lwjgl.Vector3f;
import org.vivecraft.common.utils.math.Quaternion;
import org.vivecraft.common.utils.math.Vector3;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.phys.Vec3;

import javax.annotation.CheckForNull;

public abstract class MCVR
{
    protected Minecraft mc;
    protected ClientDataHolderVR dh;
    protected static MCVR me;
    protected static VivecraftVRMod mod;
    protected org.vivecraft.common.utils.math.Matrix4f hmdPose = new org.vivecraft.common.utils.math.Matrix4f();
    public org.vivecraft.common.utils.math.Matrix4f hmdRotation = new org.vivecraft.common.utils.math.Matrix4f();
    public HardwareType detectedHardware = HardwareType.VIVE;
    protected org.vivecraft.common.utils.math.Matrix4f hmdPoseLeftEye = new org.vivecraft.common.utils.math.Matrix4f();
    protected org.vivecraft.common.utils.math.Matrix4f hmdPoseRightEye = new org.vivecraft.common.utils.math.Matrix4f();
    public Vec3History hmdHistory = new Vec3History();
    public Vec3History hmdPivotHistory = new Vec3History();
    protected boolean headIsTracking;
    protected org.vivecraft.common.utils.math.Matrix4f[] controllerPose = new org.vivecraft.common.utils.math.Matrix4f[3];
    protected org.vivecraft.common.utils.math.Matrix4f[] controllerRotation = new org.vivecraft.common.utils.math.Matrix4f[3];
    protected boolean[] controllerTracking = new boolean[3];
    protected int[] controllerSkeletalInputTrackingLevel = {0, 0};
    protected ArrayList<Float>[] gestureFingerSplay = new ArrayList[]{new ArrayList<Float>(), new ArrayList<Float>()};
    protected ArrayList<Float>[] gestureFingerCurl = new ArrayList[]{new ArrayList<Float>(), new ArrayList<Float>()};
    protected org.vivecraft.common.utils.math.Matrix4f[] gesturePose = new org.vivecraft.common.utils.math.Matrix4f[2];
    protected org.vivecraft.common.utils.math.Matrix4f[] gestureRotation = new org.vivecraft.common.utils.math.Matrix4f[2];
    protected Vec3[] gestureVelocity = new Vec3[2];
    protected org.vivecraft.common.utils.math.Matrix4f[] handRotation = new org.vivecraft.common.utils.math.Matrix4f[3];
    public Vec3History[] controllerHistory = new Vec3History[] {new Vec3History(), new Vec3History()};
    public Vec3History[] controllerForwardHistory = new Vec3History[] {new Vec3History(), new Vec3History()};
    public Vec3History[] controllerUpHistory = new Vec3History[] {new Vec3History(), new Vec3History()};
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

    public MCVR(Minecraft mc, ClientDataHolderVR dh, VivecraftVRMod vrMod)
    {
        this.mc = mc;
        this.dh = dh;
        mod = vrMod;
        me = this;

        this.gesturePose[0] = new org.vivecraft.common.utils.math.Matrix4f();
        this.gesturePose[1] = new org.vivecraft.common.utils.math.Matrix4f();

        for (int i = 0; i < 3; ++i)
        {
            this.aimSource[i] = new Vec3(0.0D, 0.0D, 0.0D);
            this.controllerPose[i] = new org.vivecraft.common.utils.math.Matrix4f();
            this.controllerRotation[i] = new org.vivecraft.common.utils.math.Matrix4f();
            this.handRotation[i] = new org.vivecraft.common.utils.math.Matrix4f();
        }
    }

    @CheckForNull
    public static MCVR get()
    {
        return me;
    }

    public abstract String getName();

    public abstract String getID();

    public abstract void processInputs();

    public abstract void destroy();

    public double getGunAngle()
    {
        return this.gunAngle;
    }

    public org.vivecraft.common.utils.math.Matrix4f getAimRotation(int controller)
    {
        return this.controllerRotation[controller];
    }

    public Vec3 getAimSource(int controller)
    {
        Vec3 out = new Vec3(this.aimSource[controller].x, this.aimSource[controller].y, this.aimSource[controller].z);

        if (!this.dh.vrSettings.seated && this.dh.vrSettings.allowStandingOriginOffset)
        {
        	if(this.dh.vr.isHMDTracking())
        		out = out.add(this.dh.vrSettings.originOffset.getX(), this.dh.vrSettings.originOffset.getY(), this.dh.vrSettings.originOffset.getZ());
        }

        return out;
    }

    public Vec3 getAimVector(int controller)
    {
        Vector3 v = this.controllerRotation[controller].transform(this.forward);
        return v.toVector3d();
    }

    public void triggerHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude)
    {
        this.triggerHapticPulse(controller, durationSeconds, frequency, amplitude, 0.0F);
    }

    public void triggerHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude, float delaySeconds)
    {
        if (!this.dh.vrSettings.seated)
        {
            if (this.dh.vrSettings.reverseHands)
            {
                if (controller == ControllerType.RIGHT)
                {
                    controller = ControllerType.LEFT;
                }
                else
                {
                    controller = ControllerType.RIGHT;
                }
            }

            this.hapticScheduler.queueHapticPulse(controller, durationSeconds, frequency, amplitude, delaySeconds);
        }
    }

    @Deprecated
    public void triggerHapticPulse(ControllerType controller, int strength)
    {
        if (strength >= 1)
        {
            this.triggerHapticPulse(controller, (float)strength / 1000000.0F, 160.0F, 1.0F);
        }
    }

    @Deprecated
    public void triggerHapticPulse(int controller, int strength)
    {
        if (controller >= 0 && controller < ControllerType.values().length)
        {
            this.triggerHapticPulse(ControllerType.values()[controller], strength);
        }
    }

    public org.vivecraft.common.utils.math.Matrix4f getHandRotation(int controller)
    {
        return this.handRotation[controller];
    }

    public Vec3 getHandVector(int controller)
    {
        Vector3 forward = new Vector3(0.0F, 0.0F, -1.0F);
        org.vivecraft.common.utils.math.Matrix4f aimRotation = this.handRotation[controller];
        Vector3 controllerDirection = aimRotation.transform(forward);
        return controllerDirection.toVector3d();
    }

    public Vec3 getCenterEyePosition()
    {
        Vector3 pos = Utils.convertMatrix4ftoTranslationVector(this.hmdPose);

        if (this.dh.vrSettings.seated || this.dh.vrSettings.allowStandingOriginOffset)
        {
        	if(this.dh.vr.isHMDTracking())
        		pos = pos.add(this.dh.vrSettings.originOffset);
        }

        return pos.toVector3d();
    }

    public Vec3 getEyePosition(RenderPass eye)
    {
        org.vivecraft.common.utils.math.Matrix4f hmdToEye = switch(eye){
            case LEFT -> this.hmdPoseLeftEye;
            case RIGHT -> this.hmdPoseRightEye;
            default -> null;
        };

        if (hmdToEye == null)
        {
            org.vivecraft.common.utils.math.Matrix4f pose = this.hmdPose;
            Vector3 pos = Utils.convertMatrix4ftoTranslationVector(pose);

            if (this.dh.vrSettings.seated || this.dh.vrSettings.allowStandingOriginOffset)
            {
            	if(this.dh.vr.isHMDTracking())
            		pos = pos.add(this.dh.vrSettings.originOffset);
            }

            return pos.toVector3d();
        }
        else
        {
            org.vivecraft.common.utils.math.Matrix4f pose = org.vivecraft.common.utils.math.Matrix4f.multiply(this.hmdPose, hmdToEye);
            Vector3 pos = Utils.convertMatrix4ftoTranslationVector(pose);

            if (this.dh.vrSettings.seated || this.dh.vrSettings.allowStandingOriginOffset)
            {
            	if(this.dh.vr.isHMDTracking())
            		pos = pos.add(this.dh.vrSettings.originOffset);
            }

            return pos.toVector3d();
        }
    }

    public HardwareType getHardwareType()
    {
        return this.dh.vrSettings.forceHardwareDetection > 0 ? HardwareType.values()[this.dh.vrSettings.forceHardwareDetection - 1] : this.detectedHardware;
    }

    public Vec3 getHmdVector()
    {
        Vector3 v = this.hmdRotation.transform(this.forward);
        return v.toVector3d();
    }

    public org.vivecraft.common.utils.math.Matrix4f getEyeRotation(RenderPass eye)
    {
        org.vivecraft.common.utils.math.Matrix4f hmdToEye = switch(eye){
            case LEFT -> this.hmdPoseLeftEye;
            case RIGHT -> this.hmdPoseRightEye;
            default -> null;
        };

        if (hmdToEye != null)
        {
            org.vivecraft.common.utils.math.Matrix4f eyeRot = new org.vivecraft.common.utils.math.Matrix4f();
            eyeRot.M[0][0] = hmdToEye.M[0][0];
            eyeRot.M[0][1] = hmdToEye.M[0][1];
            eyeRot.M[0][2] = hmdToEye.M[0][2];
            eyeRot.M[0][3] = 0.0F;
            eyeRot.M[1][0] = hmdToEye.M[1][0];
            eyeRot.M[1][1] = hmdToEye.M[1][1];
            eyeRot.M[1][2] = hmdToEye.M[1][2];
            eyeRot.M[1][3] = 0.0F;
            eyeRot.M[2][0] = hmdToEye.M[2][0];
            eyeRot.M[2][1] = hmdToEye.M[2][1];
            eyeRot.M[2][2] = hmdToEye.M[2][2];
            eyeRot.M[2][3] = 0.0F;
            eyeRot.M[3][0] = 0.0F;
            eyeRot.M[3][1] = 0.0F;
            eyeRot.M[3][2] = 0.0F;
            eyeRot.M[3][3] = 1.0F;
            return org.vivecraft.common.utils.math.Matrix4f.multiply(this.hmdRotation, eyeRot);
        }
        else
        {
            return this.hmdRotation;
        }
    }

    public VRInputAction getInputAction(String keyBindingDesc)
    {
        return this.inputActionsByKeyBinding.get(keyBindingDesc);
    }

    public VRInputAction getInputActionByName(String name)
    {
        return this.inputActions.get(name);
    }

    public Collection<VRInputAction> getInputActions()
    {
        return Collections.unmodifiableCollection(this.inputActions.values());
    }

    public VRInputAction getInputAction(KeyMapping keyBinding)
    {
        return this.getInputAction(keyBinding.getName());
    }

    public Collection<VRInputAction> getInputActionsInSet(VRInputActionSet set)
    {
        return Collections.unmodifiableCollection(this.inputActions.values().stream().filter((action) ->
        {
            return action.actionSet == set;
        }).collect(Collectors.toList()));
    }

    public boolean isControllerTracking(ControllerType controller)
    {
        return this.isControllerTracking(controller.ordinal());
    }

    public boolean isControllerTracking(int controller)
    {
        return this.controllerTracking[controller];
    }

    public int getControllerSkeletalTrackingLevel(int controller) {
        return this.controllerSkeletalInputTrackingLevel[controller];
    }

    public ArrayList<Float>[] getGestureFingerSplay(){
        return this.gestureFingerSplay;
    }

    public ArrayList<Float>[] getGestureFingerCurl(){
        return this.gestureFingerCurl;
    }

    public void resetPosition()
    {
        Vec3 pos = this.getCenterEyePosition().scale(-1.0D).add(this.dh.vrSettings.originOffset.getX(), this.dh.vrSettings.originOffset.getY(), this.dh.vrSettings.originOffset.getZ());
        this.dh.vrSettings.originOffset = new Vector3((float)pos.x, (float)pos.y + 1.62F, (float)pos.z);
    }

    public void clearOffset()
    {
        this.dh.vrSettings.originOffset = new Vector3(0.0F, 0.0F, 0.0F);
    }

    public boolean isHMDTracking()
    {
        return this.headIsTracking;
    }

    protected void processHotbar()
    {
        this.dh.interactTracker.hotbar = -1;
        if(mc.player == null) return;
        if(mc.player.getInventory() == null) return;

        if(dh.climbTracker.isGrabbingLadder() &&
        		dh.climbTracker.isClaws(mc.player.getMainHandItem())) return;
        if(!dh.interactTracker.isActive(mc.player)) return;

        Vec3 main = this.getAimSource(0);
        Vec3 off = this.getAimSource(1);
        Vec3 barStartos = null, barEndos = null;
        int hand = this.dh.vrSettings.reverseHands ? 0 : 1;

        if (this.dh.vrSettings.vrHudLockMode == VRSettings.HUDLock.WRIST) {
            barStartos = this.getAimRotation(1).transform(new Vector3((float)hand * 0.02F, 0.05F, 0.26F)).toVector3d();
            barEndos = this.getAimRotation(1).transform(new Vector3((float)hand * 0.02F, 0.05F, 0.01F)).toVector3d();
        } else if (this.dh.vrSettings.vrHudLockMode == VRSettings.HUDLock.HAND) {
            barStartos = this.getAimRotation(1).transform(new Vector3((float)hand * -0.18F, 0.08F, -0.01F)).toVector3d();
            barEndos = this.getAimRotation(1).transform(new Vector3((float)hand * 0.19F, 0.04F, -0.08F)).toVector3d();
        } else return; //how did u get here


        Vec3 barStart = off.add(barStartos.x, barStartos.y, barStartos.z);
        Vec3 barEnd = off.add(barEndos.x, barEndos.y, barEndos.z);

        Vec3 u = barStart.subtract(barEnd);
        Vec3 pq = barStart.subtract(main);
        float dist = (float) (pq.cross(u).length() / u.length());

        if(dist > 0.06) return;

        float fact = (float) (pq.dot(u) / (u.x*u.x + u.y*u.y + u.z*u.z));

        if(fact < -1) return;

        Vec3 w2 = u.scale(fact).subtract(pq);

        Vec3 point = main.subtract(w2);
        float linelen = (float) u.length();
        float ilen = (float) barStart.subtract(point).length();
        if(fact < 0) ilen *= -1;
        float pos = ilen / linelen * 9;

        if(dh.vrSettings.reverseHands) pos = 9 - pos;

        int box = (int) Math.floor(pos);

        if(box > 8) return;
        if(box < 0) {
            if(pos <= -0.5 && pos >= -1.5) //TODO fix reversed hands situation.
                box = 9;
            else
                return;
        }
        //all that maths for this.
        dh.interactTracker.hotbar = box;
        if(box != dh.interactTracker.hotbar){
            triggerHapticPulse(0, 750);
        }
    }

    protected KeyMapping findKeyBinding(String name)
    {
        return Arrays.stream(this.mc.options.keyMappings).filter((kb) ->
        {
            return name.equals(kb.getName());
        }).findFirst().orElse((KeyMapping)null);
    }

    protected void hmdSampling()
    {
        if (this.hmdPosSamples.size() == this.hmdAvgLength)
        {
            this.hmdPosSamples.removeFirst();
        }

        if (this.hmdYawSamples.size() == this.hmdAvgLength)
        {
            this.hmdYawSamples.removeFirst();
        }

        float yaw = this.dh.vrPlayer.vrdata_room_pre.hmd.getYaw();

        if (yaw < 0.0F)
        {
            yaw += 360.0F;
        }

        this.hmdYawTotal += Utils.angleDiff(yaw, this.hmdYawLast);
        this.hmdYawLast = yaw;

        if (Math.abs(Utils.angleNormalize(this.hmdYawTotal) - this.hmdYawLast) > 1.0F || this.hmdYawTotal > 100000.0F)
        {
            this.hmdYawTotal = this.hmdYawLast;
            System.out.println("HMD yaw desync/overflow corrected");
        }

        this.hmdPosSamples.add(this.dh.vrPlayer.vrdata_room_pre.hmd.getPosition());
        float yawAvg = 0.0F;

        if (this.hmdYawSamples.size() > 0)
        {
            for (float f2 : this.hmdYawSamples)
            {
                yawAvg += f2;
            }

            yawAvg /= (float)this.hmdYawSamples.size();
        }

        if (Math.abs(this.hmdYawTotal - yawAvg) > 20.0F)
        {
            this.trigger = true;
        }

        if (Math.abs(this.hmdYawTotal - yawAvg) < 1.0F)
        {
            this.trigger = false;
        }

        if (this.trigger || this.hmdYawSamples.isEmpty())
        {
            this.hmdYawSamples.add(this.hmdYawTotal);
        }
    }

    protected void updateAim()
    {
        RenderPassManager.setGUIRenderPass();


        if (this.mc != null)
        {
            { // hmd
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
                Vec3 eye = this.getCenterEyePosition();
                this.hmdHistory.add(eye);
                Vector3 v3 = this.hmdRotation.transform(new Vector3(0.0F, -0.1F, 0.1F));
                this.hmdPivotHistory.add(new Vec3((double) v3.getX() + eye.x, (double) v3.getY() + eye.y, (double) v3.getZ() + eye.z));
            }

            if (this.dh.vrSettings.seated)
            {
                this.controllerPose[0] = this.hmdPose.inverted().inverted();
                this.controllerPose[1] = this.hmdPose.inverted().inverted();
            }

            org.vivecraft.common.utils.math.Matrix4f[] controllerPoseTip = new org.vivecraft.common.utils.math.Matrix4f[] {
                new org.vivecraft.common.utils.math.Matrix4f(),
                new org.vivecraft.common.utils.math.Matrix4f()
            };
            org.vivecraft.common.utils.math.Matrix4f[] controllerPoseHand = new org.vivecraft.common.utils.math.Matrix4f[] {
                new org.vivecraft.common.utils.math.Matrix4f(),
                new org.vivecraft.common.utils.math.Matrix4f()
            };

            { // right controller
                if (this.dh.vrSettings.seated) {
                    controllerPoseHand[0] = this.controllerPose[0];
                } else {
                    controllerPoseHand[0] = org.vivecraft.common.utils.math.Matrix4f.multiply(this.controllerPose[0], this.getControllerComponentTransform(0, "handgrip"));
                }

                this.handRotation[0].M[0][0] = controllerPoseHand[0].M[0][0];
                this.handRotation[0].M[0][1] = controllerPoseHand[0].M[0][1];
                this.handRotation[0].M[0][2] = controllerPoseHand[0].M[0][2];
                this.handRotation[0].M[0][3] = 0.0F;
                this.handRotation[0].M[1][0] = controllerPoseHand[0].M[1][0];
                this.handRotation[0].M[1][1] = controllerPoseHand[0].M[1][1];
                this.handRotation[0].M[1][2] = controllerPoseHand[0].M[1][2];
                this.handRotation[0].M[1][3] = 0.0F;
                this.handRotation[0].M[2][0] = controllerPoseHand[0].M[2][0];
                this.handRotation[0].M[2][1] = controllerPoseHand[0].M[2][1];
                this.handRotation[0].M[2][2] = controllerPoseHand[0].M[2][2];
                this.handRotation[0].M[2][3] = 0.0F;
                this.handRotation[0].M[3][0] = 0.0F;
                this.handRotation[0].M[3][1] = 0.0F;
                this.handRotation[0].M[3][2] = 0.0F;
                this.handRotation[0].M[3][3] = 1.0F;

                if (this.dh.vrSettings.seated) {
                    controllerPoseTip[0] = this.controllerPose[0];
                } else {
                    controllerPoseTip[0] = org.vivecraft.common.utils.math.Matrix4f.multiply(this.controllerPose[0], this.getControllerComponentTransform(0, "tip"));
                }

                Vector3 controllerPos = Utils.convertMatrix4ftoTranslationVector(controllerPoseTip[0]);
                this.aimSource[0] = controllerPos.toVector3d();
                this.controllerHistory[0].add(this.getAimSource(0));
                this.controllerRotation[0].M[0][0] = controllerPoseTip[0].M[0][0];
                this.controllerRotation[0].M[0][1] = controllerPoseTip[0].M[0][1];
                this.controllerRotation[0].M[0][2] = controllerPoseTip[0].M[0][2];
                this.controllerRotation[0].M[0][3] = 0.0F;
                this.controllerRotation[0].M[1][0] = controllerPoseTip[0].M[1][0];
                this.controllerRotation[0].M[1][1] = controllerPoseTip[0].M[1][1];
                this.controllerRotation[0].M[1][2] = controllerPoseTip[0].M[1][2];
                this.controllerRotation[0].M[1][3] = 0.0F;
                this.controllerRotation[0].M[2][0] = controllerPoseTip[0].M[2][0];
                this.controllerRotation[0].M[2][1] = controllerPoseTip[0].M[2][1];
                this.controllerRotation[0].M[2][2] = controllerPoseTip[0].M[2][2];
                this.controllerRotation[0].M[2][3] = 0.0F;
                this.controllerRotation[0].M[3][0] = 0.0F;
                this.controllerRotation[0].M[3][1] = 0.0F;
                this.controllerRotation[0].M[3][2] = 0.0F;
                this.controllerRotation[0].M[3][3] = 1.0F;
                Vec3 hdir = this.getHmdVector();

                if (this.dh.vrSettings.seated && this.mc.screen == null) {
                    Matrix4f temp = new Matrix4f();
                    float hRange = 110.0F;
                    float vRange = 180.0F;
                    double h = this.mc.mouseHandler.xpos() / (double) this.mc.getWindow().getScreenWidth() * (double) hRange - (double) (hRange / 2.0F);
                    int hei = this.mc.getWindow().getScreenHeight();

                    if (hei % 2 != 0) {
                        --hei;
                    }

                    double v = -this.mc.mouseHandler.ypos() / (double) hei * (double) vRange + (double) (vRange / 2.0F);
                    double nPitch = -v;

                    if (this.mc.isWindowActive()) {
                        float rotStart = this.dh.vrSettings.keyholeX;
                        float rotSpeed = 20.0F * this.dh.vrSettings.xSensitivity;
                        int leftedge = (int) ((double) (-rotStart + hRange / 2.0F) * (double) this.mc.getWindow().getScreenWidth() / (double) hRange) + 1;
                        int rightedge = (int) ((double) (rotStart + hRange / 2.0F) * (double) this.mc.getWindow().getScreenWidth() / (double) hRange) - 1;
                        float rotmul = ((float) Math.abs(h) - rotStart) / (hRange / 2.0F - rotStart);
                        double xpos = this.mc.mouseHandler.xpos();

                        if (h < (double) (-rotStart)) {
                            this.seatedRot += rotSpeed * rotmul;
                            this.seatedRot %= 360.0F;
                            this.hmdForwardYaw = (float) Math.toDegrees(Math.atan2(hdir.x, hdir.z));
                            xpos = leftedge;
                            h = (-rotStart);
                        } else if (h > (double) rotStart) {
                            this.seatedRot -= rotSpeed * rotmul;
                            this.seatedRot %= 360.0F;
                            this.hmdForwardYaw = (float) Math.toDegrees(Math.atan2(hdir.x, hdir.z));
                            xpos = rightedge;
                            h = rotStart;
                        }

                        double ySpeed = 0.5D * (double) this.dh.vrSettings.ySensitivity;
                        nPitch = (double) this.aimPitch + v * ySpeed;
                        nPitch = Mth.clamp(nPitch, -89.9D, 89.9D);
                        InputSimulator.setMousePos(xpos, (double) hei / 2);
                        GLFW.glfwSetCursorPos(this.mc.getWindow().getWindow(), xpos, (double) (hei / 2));
                        temp.rotate((float) Math.toRadians(-nPitch), new Vector3f(1.0F, 0.0F, 0.0F));
                        temp.rotate((float) Math.toRadians(-180.0D + h - (double) this.hmdForwardYaw), new Vector3f(0.0F, 1.0F, 0.0F));
                    }

                    this.controllerRotation[0].M[0][0] = temp.m00;
                    this.controllerRotation[0].M[0][1] = temp.m01;
                    this.controllerRotation[0].M[0][2] = temp.m02;
                    this.controllerRotation[0].M[1][0] = temp.m10;
                    this.controllerRotation[0].M[1][1] = temp.m11;
                    this.controllerRotation[0].M[1][2] = temp.m12;
                    this.controllerRotation[0].M[2][0] = temp.m20;
                    this.controllerRotation[0].M[2][1] = temp.m21;
                    this.controllerRotation[0].M[2][2] = temp.m22;

                    this.handRotation[0].M[0][0] = temp.m00;
                    this.handRotation[0].M[0][1] = temp.m01;
                    this.handRotation[0].M[0][2] = temp.m02;
                    this.handRotation[0].M[1][0] = temp.m10;
                    this.handRotation[0].M[1][1] = temp.m11;
                    this.handRotation[0].M[1][2] = temp.m12;
                    this.handRotation[0].M[2][0] = temp.m20;
                    this.handRotation[0].M[2][1] = temp.m21;
                    this.handRotation[0].M[2][2] = temp.m22;
                }

                Vec3 dir = this.getAimVector(0);
                this.aimPitch = (float) Math.toDegrees(Math.asin(dir.y / dir.length()));
                this.controllerForwardHistory[0].add(dir);
                Vec3 updir = this.controllerRotation[0].transform(this.up).toVector3d();
                this.controllerUpHistory[0].add(updir);
            }

            { // left controller
                if (this.dh.vrSettings.seated) {
                    controllerPoseHand[1] = this.controllerPose[1];
                } else {
                    controllerPoseHand[1] = org.vivecraft.common.utils.math.Matrix4f.multiply(this.controllerPose[1], this.getControllerComponentTransform(1, "handgrip"));
                }

                this.handRotation[1].M[0][0] = controllerPoseHand[1].M[0][0];
                this.handRotation[1].M[0][1] = controllerPoseHand[1].M[0][1];
                this.handRotation[1].M[0][2] = controllerPoseHand[1].M[0][2];
                this.handRotation[1].M[0][3] = 0.0F;
                this.handRotation[1].M[1][0] = controllerPoseHand[1].M[1][0];
                this.handRotation[1].M[1][1] = controllerPoseHand[1].M[1][1];
                this.handRotation[1].M[1][2] = controllerPoseHand[1].M[1][2];
                this.handRotation[1].M[1][3] = 0.0F;
                this.handRotation[1].M[2][0] = controllerPoseHand[1].M[2][0];
                this.handRotation[1].M[2][1] = controllerPoseHand[1].M[2][1];
                this.handRotation[1].M[2][2] = controllerPoseHand[1].M[2][2];
                this.handRotation[1].M[2][3] = 0.0F;
                this.handRotation[1].M[3][0] = 0.0F;
                this.handRotation[1].M[3][1] = 0.0F;
                this.handRotation[1].M[3][2] = 0.0F;
                this.handRotation[1].M[3][3] = 1.0F;

                if (this.dh.vrSettings.seated) {
                    controllerPoseTip[1] = this.controllerPose[1];
                } else {
                    controllerPoseTip[1] = org.vivecraft.common.utils.math.Matrix4f.multiply(this.controllerPose[1], this.getControllerComponentTransform(1, "tip"));
                }

                Vector3 leftControllerPos = Utils.convertMatrix4ftoTranslationVector(controllerPoseTip[1]);
                this.aimSource[1] = leftControllerPos.toVector3d();
                this.controllerHistory[1].add(this.getAimSource(1));
                this.controllerRotation[1].M[0][0] = controllerPoseTip[1].M[0][0];
                this.controllerRotation[1].M[0][1] = controllerPoseTip[1].M[0][1];
                this.controllerRotation[1].M[0][2] = controllerPoseTip[1].M[0][2];
                this.controllerRotation[1].M[0][3] = 0.0F;
                this.controllerRotation[1].M[1][0] = controllerPoseTip[1].M[1][0];
                this.controllerRotation[1].M[1][1] = controllerPoseTip[1].M[1][1];
                this.controllerRotation[1].M[1][2] = controllerPoseTip[1].M[1][2];
                this.controllerRotation[1].M[1][3] = 0.0F;
                this.controllerRotation[1].M[2][0] = controllerPoseTip[1].M[2][0];
                this.controllerRotation[1].M[2][1] = controllerPoseTip[1].M[2][1];
                this.controllerRotation[1].M[2][2] = controllerPoseTip[1].M[2][2];
                this.controllerRotation[1].M[2][3] = 0.0F;
                this.controllerRotation[1].M[3][0] = 0.0F;
                this.controllerRotation[1].M[3][1] = 0.0F;
                this.controllerRotation[1].M[3][2] = 0.0F;
                this.controllerRotation[1].M[3][3] = 1.0F;
                Vec3 dir = this.getAimVector(1);
                this.controllerForwardHistory[1].add(dir);
                Vec3 updir = this.controllerRotation[1].transform(this.up).toVector3d();
                this.controllerUpHistory[1].add(updir);

                if (this.dh.vrSettings.seated) {
                    this.aimSource[1] = this.getCenterEyePosition();
                    this.aimSource[0] = this.getCenterEyePosition();
                }
            }

            boolean debugThirdController = false;
            { // third controller
                if (debugThirdController)
                {
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
            }

            if ((!this.hasThirdController() || this.dh.vrSettings.displayMirrorMode != VRSettings.MirrorMode.MIXED_REALITY && this.dh.vrSettings.displayMirrorMode != VRSettings.MirrorMode.THIRD_PERSON) && !debugThirdController)
            {
                this.mrMovingCamActive = false;
                this.aimSource[2] = new Vec3(this.dh.vrSettings.vrFixedCamposX, this.dh.vrSettings.vrFixedCamposY, this.dh.vrSettings.vrFixedCamposZ);
            }
            else
            {
                this.mrMovingCamActive = true;
                Vector3 thirdControllerPos = Utils.convertMatrix4ftoTranslationVector(this.controllerPose[2]);
                this.aimSource[2] = thirdControllerPos.toVector3d();
            }
        }
    }

    public void processBindings()
    {
        if (!this.inputActions.isEmpty())
        {
            boolean sleeping = this.mc.level != null && this.mc.player != null && this.mc.player.isSleeping();
            boolean gui = this.mc.screen != null;
            boolean toggleMovementPressed = mod.keyToggleMovement.consumeClick();

            if (!this.mc.options.keyPickItem.isDown() && !toggleMovementPressed)
            {
                this.moveModeSwitchCount = 0;
            }
            else if (++this.moveModeSwitchCount == 80 || toggleMovementPressed)
            {
                if (this.dh.vrSettings.seated)
                {
                    this.dh.vrSettings.seatedFreeMove = !this.dh.vrSettings.seatedFreeMove;
                    this.mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.movementmodeswitch", this.dh.vrSettings.seatedFreeMove ? Component.translatable("vivecraft.options.freemove") : Component.translatable("vivecraft.options.teleport")));
                }
                else if (this.dh.vrPlayer.isTeleportSupported())
                {
                    this.dh.vrSettings.forceStandingFreeMove = !this.dh.vrSettings.forceStandingFreeMove;
                    this.mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.movementmodeswitch", this.dh.vrSettings.seatedFreeMove ? Component.translatable("vivecraft.options.freemove") : Component.translatable("vivecraft.options.teleport")));
                }
                else if (this.dh.vrPlayer.isTeleportOverridden())
                {
                    this.dh.vrPlayer.setTeleportOverride(false);
                    this.mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.teleportdisabled"));
                }
                else
                {
                    this.dh.vrPlayer.setTeleportOverride(true);
                    this.mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.teleportenabled"));
                }
            }

            Vec3 main = this.getAimVector(0);
            Vec3 off = this.getAimVector(1);
            float myaw = (float)Math.toDegrees(Math.atan2(-main.x, main.z));
            float oyaw = (float)Math.toDegrees(Math.atan2(-off.x, off.z));

            if (!gui)
            {
                if (mod.keyWalkabout.isDown())
                {
                    float yaw = myaw;

                    //oh this is ugly. TODO: cache which hand when binding button.
                    ControllerType controller = this.findActiveBindingControllerType(mod.keyWalkabout);
                    if (controller != null && controller == ControllerType.LEFT)
                    {
                        yaw = oyaw;
                    }

                    if (!this.isWalkingAbout)
                    {
                        this.isWalkingAbout = true;
                        this.walkaboutYawStart = this.dh.vrSettings.worldRotation - yaw;
                    }
                    else
                    {
                        this.dh.vrSettings.worldRotation = this.walkaboutYawStart + yaw;
                        this.dh.vrSettings.worldRotation %= 360.0F;
                    }
                }
                else
                {
                    this.isWalkingAbout = false;
                }

                if (mod.keyRotateFree.isDown())
                {
                    float yaw = myaw;

                    //oh this is ugly. TODO: cache which hand when binding button.
                    ControllerType controller = this.findActiveBindingControllerType(mod.keyRotateFree);
                    if (controller != null && controller == ControllerType.LEFT)
                    {
                        yaw = oyaw;
                    }

                    if (!this.isFreeRotate)
                    {
                        this.isFreeRotate = true;
                        this.walkaboutYawStart = this.dh.vrSettings.worldRotation + yaw;
                    }
                    else
                    {
                        this.dh.vrSettings.worldRotation = this.walkaboutYawStart - yaw;
                        // mc.vrPlayer.checkandUpdateRotateScale(true,0);
                    }
                }
                else
                {
                    this.isFreeRotate = false;
                }
            }

            if (mod.keyHotbarNext.consumeClick())
            {
                this.changeHotbar(-1);
                this.triggerBindingHapticPulse(mod.keyHotbarNext, 250);
            }

            if (mod.keyHotbarPrev.consumeClick())
            {
                this.changeHotbar(1);
                this.triggerBindingHapticPulse(mod.keyHotbarPrev, 250);
            }

            if (mod.keyQuickTorch.consumeClick() && this.mc.player != null)
            {
                for (int slot = 0; slot < 9; ++slot)
                {
                    ItemStack itemstack = this.mc.player.getInventory().getItem(slot);

                    if (itemstack.getItem() instanceof BlockItem && ((BlockItem)itemstack.getItem()).getBlock() instanceof TorchBlock && this.mc.screen == null)
                    {
                        this.quickTorchPreviousSlot = this.mc.player.getInventory().selected;
                        this.mc.player.getInventory().selected = slot;
                        this.mc.startUseItem();
                        // switch back immediately
                        this.mc.player.getInventory().selected = this.quickTorchPreviousSlot;
                        this.quickTorchPreviousSlot = -1;
                        break;
                    }
                }
            }

            // if you start teleporting, close any UI
            if (gui && !sleeping && this.mc.options.keyUp.isDown() && !(this.mc.screen instanceof WinScreen) && this.mc.player != null)
            {
                this.mc.player.closeContainer();
            }

            //GuiContainer.java only listens directly to the keyboard to close.
            if (this.mc.screen instanceof AbstractContainerScreen && this.mc.options.keyInventory.consumeClick() && this.mc.player != null)
            {
                this.mc.player.closeContainer();
            }

            // allow toggling chat window with chat keybind
            if (this.mc.screen instanceof ChatScreen && this.mc.options.keyChat.consumeClick())
            {
                this.mc.setScreen((Screen)null);
            }

            if (this.dh.vrSettings.worldRotationIncrement == 0.0F)
            {
                float ax = this.getInputAction(mod.keyRotateAxis).getAxis2DUseTracked().getX();

                if (ax == 0.0F)
                {
                    ax = this.getInputAction(mod.keyFreeMoveRotate).getAxis2DUseTracked().getX();
                }

                if (ax != 0.0F)
                {
                    float analogRotSpeed = 10.0F * ax;
                    this.dh.vrSettings.worldRotation -= analogRotSpeed;
                    this.dh.vrSettings.worldRotation %= 360.0F;
                }
            }
            else if (mod.keyRotateAxis.consumeClick() || mod.keyFreeMoveRotate.consumeClick())
            {
                float ax = this.getInputAction(mod.keyRotateAxis).getAxis2D(false).getX();

                if (ax == 0.0F)
                {
                    ax = this.getInputAction(mod.keyFreeMoveRotate).getAxis2D(false).getX();
                }

                if (Math.abs(ax) > 0.5F)
                {
                    this.dh.vrSettings.worldRotation -= this.dh.vrSettings.worldRotationIncrement * Math.signum(ax);
                    this.dh.vrSettings.worldRotation %= 360.0F;
                }
            }

            if (this.dh.vrSettings.worldRotationIncrement == 0.0F)
            {
                float ax = VivecraftMovementInput.getMovementAxisValue(mod.keyRotateLeft);

                if (ax > 0.0F)
                {
                    float analogRotSpeed = 5.0F;

                    if (ax > 0.0F)
                    {
                        analogRotSpeed = 10.0F * ax;
                    }

                    this.dh.vrSettings.worldRotation += analogRotSpeed;
                    this.dh.vrSettings.worldRotation %= 360.0F;
                }
            }
            else if (mod.keyRotateLeft.consumeClick())
            {
                this.dh.vrSettings.worldRotation += this.dh.vrSettings.worldRotationIncrement;
                this.dh.vrSettings.worldRotation %= 360.0F;
            }

            if (this.dh.vrSettings.worldRotationIncrement == 0.0F)
            {
                float ax = VivecraftMovementInput.getMovementAxisValue(mod.keyRotateRight);

                if (ax > 0.0F)
                {
                    float analogRotSpeed = 5.0F;

                    if (ax > 0.0F)
                    {
                        analogRotSpeed = 10.0F * ax;
                    }

                    this.dh.vrSettings.worldRotation -= analogRotSpeed;
                    this.dh.vrSettings.worldRotation %= 360.0F;
                }
            }
            else if (mod.keyRotateRight.consumeClick())
            {
                this.dh.vrSettings.worldRotation -= this.dh.vrSettings.worldRotationIncrement;
                this.dh.vrSettings.worldRotation %= 360.0F;
            }

            this.seatedRot = this.dh.vrSettings.worldRotation;

            if (mod.keyRadialMenu.consumeClick() && !gui)
            {
                ControllerType controller = this.findActiveBindingControllerType(mod.keyRadialMenu);

                if (controller != null)
                {
                    RadialHandler.setOverlayShowing(!RadialHandler.isShowing(), controller);
                }
            }

            if (mod.keySwapMirrorView.consumeClick())
            {
                if (this.dh.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON)
                {
                    this.dh.vrSettings.displayMirrorMode = VRSettings.MirrorMode.FIRST_PERSON;
                }
                else if (this.dh.vrSettings.displayMirrorMode == VRSettings.MirrorMode.FIRST_PERSON)
                {
                    this.dh.vrSettings.displayMirrorMode = VRSettings.MirrorMode.THIRD_PERSON;
                }

                this.dh.vrRenderer.reinitFrameBuffers("Mirror Setting Changed");
            }

            if (mod.keyToggleKeyboard.consumeClick())
            {
                KeyboardHandler.setOverlayShowing(!KeyboardHandler.Showing);
            }

            if (mod.keyMoveThirdPersonCam.consumeClick() && !ClientDataHolderVR.kiosk && !this.dh.vrSettings.seated && (this.dh.vrSettings.displayMirrorMode == VRSettings.MirrorMode.MIXED_REALITY || this.dh.vrSettings.displayMirrorMode == VRSettings.MirrorMode.THIRD_PERSON))
            {
                ControllerType controller = this.findActiveBindingControllerType(mod.keyMoveThirdPersonCam);

                if (controller != null)
                {
                    VRHotkeys.startMovingThirdPersonCam(controller.ordinal(), VRHotkeys.Triggerer.BINDING);
                }
            }

            if (!mod.keyMoveThirdPersonCam.isDown() && VRHotkeys.isMovingThirdPersonCam() && VRHotkeys.getMovingThirdPersonCamTriggerer() == VRHotkeys.Triggerer.BINDING)
            {
                VRHotkeys.stopMovingThirdPersonCam();
                this.dh.vrSettings.saveOptions();
            }

            if (VRHotkeys.isMovingThirdPersonCam() && VRHotkeys.getMovingThirdPersonCamTriggerer() == VRHotkeys.Triggerer.MENUBUTTON && mod.keyMenuButton.consumeClick())
            {
                VRHotkeys.stopMovingThirdPersonCam();
                this.dh.vrSettings.saveOptions();
            }

            if (KeyboardHandler.Showing && this.mc.screen == null && mod.keyMenuButton.consumeClick())
            {
                KeyboardHandler.setOverlayShowing(false);
            }

            if (RadialHandler.isShowing() && mod.keyMenuButton.consumeClick())
            {
                RadialHandler.setOverlayShowing(false, (ControllerType)null);
            }

            if (mod.keyMenuButton.consumeClick())
            {
                if (!gui)
                {
                    if (!ClientDataHolderVR.kiosk)
                    {
                        this.mc.pauseGame(false);
                    }
                }
                else
                {
                    InputSimulator.pressKey(256);
                    InputSimulator.releaseKey(256);
                }

                KeyboardHandler.setOverlayShowing(false);
            }

            if (mod.keyExportWorld.consumeClick() && this.mc.level != null && this.mc.player != null)
            {
//                try
//                {
//                    final BlockPos blockpos = this.mc.player.blockPosition();
//                    int k = 320;
//                    File file1 = new File("menuworlds/custom_114");
//                    file1.mkdirs();
//                    int i = 0;
//
//                    while (true)
//                    {
//                        final File file2 = new File(file1, "world" + i + ".mmw");
//
//                        if (!file2.exists())
//                        {
//                            System.out.println("Exporting world... area size: 320");
//                            System.out.println("Saving to " + file2.getAbsolutePath());
//
//                            if (this.mc.isLocalServer())
//                            {
//                                final Level level = this.mc.getSingleplayerServer().getLevel(this.mc.player.level.dimension());
//                                CompletableFuture<Void> completablefuture = this.mc.getSingleplayerServer().submit(new Runnable()
//                                {
//                                    public void run()
//                                    {
//                                        try
//                                        {
//                                            MenuWorldExporter.saveAreaToFile(level, blockpos.getX() - 160, blockpos.getZ() - 160, 320, 320, blockpos.getY(), file2);
//                                        }
//                                        catch (IOException ioexception)
//                                        {
//                                            ioexception.printStackTrace();
//                                        }
//                                    }
//                                });
//
//                                while (!completablefuture.isDone())
//                                {
//                                    Thread.sleep(10L);
//                                }
//                            }
//                            else
//                            {
//                                MenuWorldExporter.saveAreaToFile(this.mc.level, blockpos.getX() - 160, blockpos.getZ() - 160, 320, 320, blockpos.getY(), file2);
//                                this.mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.menuworldexportclientwarning"));
//                            }
//
//                            this.mc.gui.getChat().addMessage(Component.literal(LangHelper.get("vivecraft.messages.menuworldexportcomplete.1", 320)));
//                            this.mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.menuworldexportcomplete.2", file2.getAbsolutePath()));
//                            break;
//                        }
//
//                        ++i;
//                    }
//                }
//                catch (Exception exception)
//                {
//                    exception.printStackTrace();
//                }
            }

            if (mod.keyTogglePlayerList.consumeClick())
            {
                ((GuiExtension) this.mc.gui).setShowPlayerList(!((GuiExtension) this.mc.gui).getShowPlayerList());
            }

            if (mod.keyToggleHandheldCam.consumeClick() && this.mc.player != null)
            {
                this.dh.cameraTracker.toggleVisibility();

                if (this.dh.cameraTracker.isVisible())
                {
                    ControllerType hand = this.findActiveBindingControllerType(mod.keyToggleHandheldCam);

                    if (hand == null)
                    {
                        hand = ControllerType.RIGHT;
                    }

                    VRData.VRDevicePose handPose = this.dh.vrPlayer.vrdata_world_pre.getController(hand.ordinal());
                    this.dh.cameraTracker.setPosition(handPose.getPosition());
                    this.dh.cameraTracker.setRotation(new Quaternion(handPose.getMatrix().transposed()));
                }
            }

            if (mod.keyQuickHandheldCam.consumeClick() && this.mc.player != null)
            {
                if (!this.dh.cameraTracker.isVisible())
                {
                    this.dh.cameraTracker.toggleVisibility();
                }

                ControllerType hand = this.findActiveBindingControllerType(mod.keyQuickHandheldCam);

                if (hand == null)
                {
                    hand = ControllerType.RIGHT;
                }

                VRData.VRDevicePose handPose = this.dh.vrPlayer.vrdata_world_pre.getController(hand.ordinal());
                this.dh.cameraTracker.setPosition(handPose.getPosition());
                this.dh.cameraTracker.setRotation(new Quaternion(handPose.getMatrix().transposed()));
                this.dh.cameraTracker.startMoving(hand.ordinal(), true);
            }

            if (!mod.keyQuickHandheldCam.isDown() && this.dh.cameraTracker.isMoving() && this.dh.cameraTracker.isQuickMode() && this.mc.player != null)
            {
                this.dh.cameraTracker.stopMoving();
                this.dh.grabScreenShot = true;
            }

            //SkeletonHandler.processBindings();
            GuiHandler.processBindingsGui();
            RadialHandler.processBindings();
            KeyboardHandler.processBindings();
            this.dh.interactTracker.processBindings();
        }
    }

    public void populateInputActions()
    {
        Map<String, ActionParams> actionParams = this.getSpecialActionParams();

        for (KeyMapping keymapping : this.mc.options.keyMappings)
        {
            ActionParams params = actionParams.getOrDefault(keymapping.getName(), new ActionParams("optional", "boolean", (VRInputActionSet)null));
            VRInputAction action = new VRInputAction(keymapping, params.requirement, params.type, params.actionSetOverride);
            this.inputActions.put(action.name, action);
        }

        for (VRInputAction action : this.inputActions.values())
        {
            this.inputActionsByKeyBinding.put(action.keyBinding.getName(), action);
        }

        this.getInputAction(mod.keyVRInteract).setPriority(5).setEnabled(false);
        this.getInputAction(mod.keyClimbeyGrab).setPriority(10).setEnabled(false);
        this.getInputAction(mod.keyClimbeyJump).setEnabled(false);
        this.getInputAction(GuiHandler.keyKeyboardClick).setPriority(50);
        this.getInputAction(GuiHandler.keyKeyboardShift).setPriority(50);
    }

    public Map<String, ActionParams> getSpecialActionParams()
    {
        Map<String, ActionParams> map = new HashMap<>();
        this.addActionParams(map, this.mc.options.keyUp, "optional", "vector1", (VRInputActionSet)null);
        this.addActionParams(map, this.mc.options.keyDown, "optional", "vector1", (VRInputActionSet)null);
        this.addActionParams(map, this.mc.options.keyLeft, "optional", "vector1", (VRInputActionSet)null);
        this.addActionParams(map, this.mc.options.keyRight, "optional", "vector1", (VRInputActionSet)null);
        this.addActionParams(map, this.mc.options.keyInventory, "suggested", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, this.mc.options.keyAttack, "suggested", "boolean", (VRInputActionSet)null);
        this.addActionParams(map, this.mc.options.keyUse, "suggested", "boolean", (VRInputActionSet)null);
        this.addActionParams(map, this.mc.options.keyChat, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, mod.keyHotbarScroll, "optional", "vector2", (VRInputActionSet)null);
        this.addActionParams(map, mod.keyHotbarSwipeX, "optional", "vector2", (VRInputActionSet)null);
        this.addActionParams(map, mod.keyHotbarSwipeY, "optional", "vector2", (VRInputActionSet)null);
        this.addActionParams(map, mod.keyMenuButton, "suggested", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, mod.keyTeleportFallback, "suggested", "vector1", (VRInputActionSet)null);
        this.addActionParams(map, mod.keyFreeMoveRotate, "optional", "vector2", (VRInputActionSet)null);
        this.addActionParams(map, mod.keyFreeMoveStrafe, "optional", "vector2", (VRInputActionSet)null);
        this.addActionParams(map, mod.keyRotateLeft, "optional", "vector1", (VRInputActionSet)null);
        this.addActionParams(map, mod.keyRotateRight, "optional", "vector1", (VRInputActionSet)null);
        this.addActionParams(map, mod.keyRotateAxis, "optional", "vector2", (VRInputActionSet)null);
        this.addActionParams(map, mod.keyRadialMenu, "suggested", "boolean", (VRInputActionSet)null);
        this.addActionParams(map, mod.keySwapMirrorView, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, mod.keyToggleKeyboard, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, mod.keyMoveThirdPersonCam, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, mod.keyToggleHandheldCam, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, mod.keyQuickHandheldCam, "optional", "boolean", VRInputActionSet.GLOBAL);
        this.addActionParams(map, mod.keyTrackpadTouch, "optional", "boolean", VRInputActionSet.TECHNICAL);
        this.addActionParams(map, mod.keyVRInteract, "suggested", "boolean", VRInputActionSet.CONTEXTUAL);
        this.addActionParams(map, mod.keyClimbeyGrab, "suggested", "boolean", (VRInputActionSet)null);
        this.addActionParams(map, mod.keyClimbeyJump, "suggested", "boolean", (VRInputActionSet)null);
        this.addActionParams(map, GuiHandler.keyLeftClick, "suggested", "boolean", (VRInputActionSet)null);
        this.addActionParams(map, GuiHandler.keyScrollAxis, "optional", "vector2", (VRInputActionSet)null);
        this.addActionParams(map, GuiHandler.keyRightClick, "suggested", "boolean", (VRInputActionSet)null);
        this.addActionParams(map, GuiHandler.keyShift, "suggested", "boolean", (VRInputActionSet)null);
        this.addActionParams(map, GuiHandler.keyKeyboardClick, "suggested", "boolean", (VRInputActionSet)null);
        this.addActionParams(map, GuiHandler.keyKeyboardShift, "suggested", "boolean", (VRInputActionSet)null);
        File file = new File("customactionsets.txt");

        if (file.exists())
        {
            System.out.println("Loading custom action set definitions...");
            String line;

            try (BufferedReader br = new BufferedReader(new FileReader(file)))
            {
                while ((line = br.readLine()) != null)
                {
                    String[] tokens = line.split(":", 2);

                    if (tokens.length < 2)
                    {
                        System.out.println("Invalid tokens: " + line);
                    }
                    else
                    {
                        KeyMapping keymapping = this.findKeyBinding(tokens[0]);

                        if (keymapping == null)
                        {
                            System.out.println("Unknown key binding: " + tokens[0]);
                        }
                        else if (mod.getKeyBindings().contains(keymapping))
                        {
                            System.out.println("NO! Don't touch Vivecraft bindings!");
                        }
                        else
                        {
                            VRInputActionSet actionSet = switch (tokens[1].toLowerCase()) {
                                case "ingame" -> VRInputActionSet.INGAME;
                                case "gui" -> VRInputActionSet.GUI;
                                case "global" -> VRInputActionSet.GLOBAL;
                                default -> null;
                            };

                            if (actionSet == null)
                            {
                                System.out.println("Unknown action set: " + tokens[1]);
                            }
                            else
                            {
                                this.addActionParams(map, keymapping, "optional", "boolean", actionSet);
                            }
                        }
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        return map;
    }

    protected void changeHotbar(int dir)
    {
        if (this.mc.player != null && (!this.dh.climbTracker.isGrabbingLadder() || !this.dh.climbTracker.isClaws(this.mc.player.getMainHandItem())))
        {
            if (this.mc.screen == null)
            {
                InputSimulator.scrollMouse(0.0D, dir * 4);
            }
            else
            {
                this.mc.player.getInventory().swapPaint(dir);
            }
        }
    }

    private void addActionParams(Map<String, ActionParams> map, KeyMapping keymapping, String requirement, String type, VRInputActionSet actionSetOverride)
    {
        ActionParams actionparams = new ActionParams(requirement, type, actionSetOverride);
        map.put(keymapping.getName(), actionparams);
    }

    protected abstract void triggerBindingHapticPulse(KeyMapping binding, int duration);

    protected abstract ControllerType findActiveBindingControllerType(KeyMapping binding);

    public abstract void poll(long frameIndex);

    public abstract Vector2f getPlayAreaSize();

    public abstract boolean init();

    public abstract boolean postinit();

    public abstract org.vivecraft.common.utils.math.Matrix4f getControllerComponentTransform(int controllerIndex, String componentName);

    public abstract boolean hasThirdController();

    public abstract List<Long> getOrigins(VRInputAction action);

    public abstract String getOriginName(long l);

    public abstract VRRenderer createVRRenderer();

    public abstract boolean isActive();
}
