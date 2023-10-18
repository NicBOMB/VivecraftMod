package org.vivecraft.client_vr.settings;

import com.google.common.util.concurrent.Runnables;
import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client.utils.LangHelper;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.MethodHolder;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.MinecraftExtension;
import org.vivecraft.common.utils.math.*;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class VRHotkeys {
    static long nextRead = 0L;
    static final long COOLOFF_PERIOD_MILLIS = 500L;
    static boolean debug = false;
    private static int startController;
    private static VRData.VRDevicePose startControllerPose;
    private static float startCamposX;
    private static float startCamposY;
    private static float startCamposZ;
    private static Quaternion startCamrotQuat;
    private static Triggerer camTriggerer;

    public static boolean handleKeyboardInputs(int key, int scanCode, int action, int modifiers) {
        if (nextRead != 0L && System.currentTimeMillis() < nextRead) {
            return false;
        } else {
            boolean flag = false;

            if (action == 1 && key == 344 && MethodHolder.isKeyDown(345)) {
                ClientDataHolderVR.vrSettings.storeDebugAim = true;
                VRState.mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.showaim"));
                flag = true;
            }

            if (action == 1 && key == 66 && MethodHolder.isKeyDown(345)) {
                ClientDataHolderVR.vrSettings.walkUpBlocks = !ClientDataHolderVR.vrSettings.walkUpBlocks;
                VRState.mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.walkupblocks", ClientDataHolderVR.vrSettings.walkUpBlocks ? LangHelper.getYes() : LangHelper.getNo()));
                flag = true;
            }

            if (action == 1 && key == 73 && MethodHolder.isKeyDown(345)) {
                ClientDataHolderVR.vrSettings.inertiaFactor = ClientDataHolderVR.vrSettings.inertiaFactor.getNext();
                VRState.mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.playerinertia", Component.translatable(ClientDataHolderVR.vrSettings.inertiaFactor.getLangKey())));

                flag = true;
            }

            if (action == 1 && key == 82 && MethodHolder.isKeyDown(345)) {
                if (ClientDataHolderVR.vrPlayer.isTeleportOverridden()) {
                    ClientDataHolderVR.vrPlayer.setTeleportOverride(false);
                    VRState.mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.teleportdisabled"));
                } else {
                    ClientDataHolderVR.vrPlayer.setTeleportOverride(true);
                    VRState.mc.gui.getChat().addMessage(Component.translatable("vivecraft.messages.teleportenabled"));
                }

                flag = true;
            }

            if (action == 1 && key == 268 && MethodHolder.isKeyDown(345)) {
                snapMRCam(0);
                flag = true;
            }

            if (action == 1 && key == 301 && debug) {
                VRState.mc.setScreen(new WinScreen(false, Runnables.doNothing()));
                flag = true;
            }

            if ((VRState.mc.level == null || VRState.mc.screen != null) && action == 1 && key == 294) {
                ClientDataHolderVR.vrSettings.setOptionValue(VRSettings.VrOptions.MIRROR_DISPLAY);
                ((MinecraftExtension) VRState.mc).vivecraft$notifyMirror(ClientDataHolderVR.vrSettings.getButtonDisplayString(VRSettings.VrOptions.MIRROR_DISPLAY), false, 3000);
            }

            if (flag) {
                ClientDataHolderVR.vrSettings.saveOptions();
            }

            return flag;
        }
    }

    public static void handleMRKeys() {
        boolean flag = false;

        if (MethodHolder.isKeyDown(263) && MethodHolder.isKeyDown(345) && !MethodHolder.isKeyDown(344)) {
            adjustCamPos(new Vector3(-0.01F, 0.0F, 0.0F));
            flag = true;
        }

        if (MethodHolder.isKeyDown(262) && MethodHolder.isKeyDown(345) && !MethodHolder.isKeyDown(344)) {
            adjustCamPos(new Vector3(0.01F, 0.0F, 0.0F));
            flag = true;
        }

        if (MethodHolder.isKeyDown(265) && MethodHolder.isKeyDown(345) && !MethodHolder.isKeyDown(344)) {
            adjustCamPos(new Vector3(0.0F, 0.0F, -0.01F));
            flag = true;
        }

        if (MethodHolder.isKeyDown(264) && MethodHolder.isKeyDown(345) && !MethodHolder.isKeyDown(344)) {
            adjustCamPos(new Vector3(0.0F, 0.0F, 0.01F));
            flag = true;
        }

        if (MethodHolder.isKeyDown(266) && MethodHolder.isKeyDown(345) && !MethodHolder.isKeyDown(344)) {
            adjustCamPos(new Vector3(0.0F, 0.01F, 0.0F));
            flag = true;
        }

        if (MethodHolder.isKeyDown(267) && MethodHolder.isKeyDown(345) && !MethodHolder.isKeyDown(344)) {
            adjustCamPos(new Vector3(0.0F, -0.01F, 0.0F));
            flag = true;
        }

        if (MethodHolder.isKeyDown(265) && MethodHolder.isKeyDown(345) && MethodHolder.isKeyDown(344)) {
            adjustCamRot(Axis.PITCH, 0.5F);
            flag = true;
        }

        if (MethodHolder.isKeyDown(264) && MethodHolder.isKeyDown(345) && MethodHolder.isKeyDown(344)) {
            adjustCamRot(Axis.PITCH, -0.5F);
            flag = true;
        }

        if (MethodHolder.isKeyDown(263) && MethodHolder.isKeyDown(345) && MethodHolder.isKeyDown(344)) {
            adjustCamRot(Axis.YAW, 0.5F);
            flag = true;
        }

        if (MethodHolder.isKeyDown(262) && MethodHolder.isKeyDown(345) && MethodHolder.isKeyDown(344)) {
            adjustCamRot(Axis.YAW, -0.5F);
            flag = true;
        }

        if (MethodHolder.isKeyDown(266) && MethodHolder.isKeyDown(345) && MethodHolder.isKeyDown(344)) {
            adjustCamRot(Axis.ROLL, 0.5F);
            flag = true;
        }

        if (MethodHolder.isKeyDown(267) && MethodHolder.isKeyDown(345) && MethodHolder.isKeyDown(344)) {
            adjustCamRot(Axis.ROLL, -0.5F);
            flag = true;
        }

        if (MethodHolder.isKeyDown(260) && MethodHolder.isKeyDown(345) && !MethodHolder.isKeyDown(344)) {
            VRState.mc.options.fov().set(VRState.mc.options.fov().get() + 1);
            flag = true;
        }

        if (MethodHolder.isKeyDown(261) && MethodHolder.isKeyDown(345) && !MethodHolder.isKeyDown(344)) {
            VRState.mc.options.fov().set(VRState.mc.options.fov().get() - 1);
            flag = true;
        }

        if (MethodHolder.isKeyDown(260) && MethodHolder.isKeyDown(345) && MethodHolder.isKeyDown(344)) {
            ++ClientDataHolderVR.vrSettings.mixedRealityFov;
            flag = true;
        }

        if (MethodHolder.isKeyDown(261) && MethodHolder.isKeyDown(345) && MethodHolder.isKeyDown(344)) {
            --ClientDataHolderVR.vrSettings.mixedRealityFov;
            flag = true;
        }

        if (flag) {
            ClientDataHolderVR.vrSettings.saveOptions();

            if (ClientDataHolderVR.vr.mrMovingCamActive) {
                VRState.mc.gui.getChat().addMessage(Component.literal(LangHelper.get("vivecraft.messages.coords", ClientDataHolderVR.vrSettings.mrMovingCamOffsetX, ClientDataHolderVR.vrSettings.mrMovingCamOffsetY, ClientDataHolderVR.vrSettings.mrMovingCamOffsetZ)));
                Angle angle = ClientDataHolderVR.vrSettings.mrMovingCamOffsetRotQuat.toEuler();
                VRState.mc.gui.getChat().addMessage(Component.literal(LangHelper.get("vivecraft.messages.angles", angle.getPitch(), angle.getYaw(), angle.getRoll())));
            } else {
                VRState.mc.gui.getChat().addMessage(Component.literal(LangHelper.get("vivecraft.messages.coords", ClientDataHolderVR.vrSettings.vrFixedCamposX, ClientDataHolderVR.vrSettings.vrFixedCamposY, ClientDataHolderVR.vrSettings.vrFixedCamposZ)));
                Angle angle1 = ClientDataHolderVR.vrSettings.vrFixedCamrotQuat.toEuler();
                VRState.mc.gui.getChat().addMessage(Component.literal(LangHelper.get("vivecraft.messages.angles", angle1.getPitch(), angle1.getYaw(), angle1.getRoll())));
            }
        }
    }

    private static void adjustCamPos(Vector3 offset) {
        if (ClientDataHolderVR.vr.mrMovingCamActive) {
            offset = ClientDataHolderVR.vrSettings.mrMovingCamOffsetRotQuat.multiply(offset);
            ClientDataHolderVR.vrSettings.mrMovingCamOffsetX += offset.getX();
            ClientDataHolderVR.vrSettings.mrMovingCamOffsetY += offset.getY();
            ClientDataHolderVR.vrSettings.mrMovingCamOffsetZ += offset.getZ();
        } else {
            offset = ClientDataHolderVR.vrSettings.vrFixedCamrotQuat.inverse().multiply(offset);
            ClientDataHolderVR.vrSettings.vrFixedCamposX += offset.getX();
            ClientDataHolderVR.vrSettings.vrFixedCamposY += offset.getY();
            ClientDataHolderVR.vrSettings.vrFixedCamposZ += offset.getZ();
        }
    }

    private static void adjustCamRot(Axis axis, float degrees) {
        if (ClientDataHolderVR.vr.mrMovingCamActive) {
            ClientDataHolderVR.vrSettings.mrMovingCamOffsetRotQuat.set(ClientDataHolderVR.vrSettings.mrMovingCamOffsetRotQuat.rotate(axis, degrees, true));
        } else {
            ClientDataHolderVR.vrSettings.vrFixedCamrotQuat.set(ClientDataHolderVR.vrSettings.vrFixedCamrotQuat.rotate(axis, degrees, false));
        }
    }

    public static void snapMRCam(int controller) {
        Vec3 vec3 = ClientDataHolderVR.vrPlayer.vrdata_room_pre.getController(controller).getPosition();
        ClientDataHolderVR.vrSettings.vrFixedCamposX = (float) vec3.x;
        ClientDataHolderVR.vrSettings.vrFixedCamposY = (float) vec3.y;
        ClientDataHolderVR.vrSettings.vrFixedCamposZ = (float) vec3.z;
        Quaternion quaternion = new Quaternion(Utils.convertOVRMatrix(ClientDataHolderVR.vrPlayer.vrdata_room_pre.getController(controller).getMatrix()));
        ClientDataHolderVR.vrSettings.vrFixedCamrotQuat.set(quaternion);
    }

    public static void updateMovingThirdPersonCam() {
        if (startControllerPose != null) {
            VRData.VRDevicePose vrdata$vrdevicepose = ClientDataHolderVR.vrPlayer.vrdata_room_pre.getController(startController);
            Vec3 vec3 = startControllerPose.getPosition();
            Vec3 vec31 = vrdata$vrdevicepose.getPosition().subtract(vec3);
            Matrix4f matrix4f = Matrix4f.multiply(vrdata$vrdevicepose.getMatrix(), startControllerPose.getMatrix().inverted());
            Vector3 vector3 = new Vector3(startCamposX - (float) vec3.x, startCamposY - (float) vec3.y, startCamposZ - (float) vec3.z);
            Vector3 vector31 = matrix4f.transform(vector3);
            ClientDataHolderVR.vrSettings.vrFixedCamposX = startCamposX + (float) vec31.x + (vector31.getX() - vector3.getX());
            ClientDataHolderVR.vrSettings.vrFixedCamposY = startCamposY + (float) vec31.y + (vector31.getY() - vector3.getY());
            ClientDataHolderVR.vrSettings.vrFixedCamposZ = startCamposZ + (float) vec31.z + (vector31.getZ() - vector3.getZ());
            ClientDataHolderVR.vrSettings.vrFixedCamrotQuat.set(startCamrotQuat.multiply(new Quaternion(Utils.convertOVRMatrix(matrix4f))));
        }
    }

    public static void startMovingThirdPersonCam(int controller, Triggerer triggerer) {
        startController = controller;
        startControllerPose = ClientDataHolderVR.vrPlayer.vrdata_room_pre.getController(controller);
        startCamposX = ClientDataHolderVR.vrSettings.vrFixedCamposX;
        startCamposY = ClientDataHolderVR.vrSettings.vrFixedCamposY;
        startCamposZ = ClientDataHolderVR.vrSettings.vrFixedCamposZ;
        startCamrotQuat = ClientDataHolderVR.vrSettings.vrFixedCamrotQuat.copy();
        camTriggerer = triggerer;
    }

    public static void stopMovingThirdPersonCam() {
        startControllerPose = null;
    }

    public static boolean isMovingThirdPersonCam() {
        return startControllerPose != null;
    }

    public static int getMovingThirdPersonCamController() {
        return startController;
    }

    public static Triggerer getMovingThirdPersonCamTriggerer() {
        return camTriggerer;
    }

    public static void loadExternalCameraConfig() {
        File file1 = new File("ExternalCamera.cfg");

        if (file1.exists()) {
            float f = 0.0F;
            float f1 = 0.0F;
            float f2 = 0.0F;
            float f3 = 0.0F;
            float f4 = 0.0F;
            float f5 = 0.0F;
            float f6 = 40.0F;
            String s;

            try (BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(file1), StandardCharsets.UTF_8))) {
                while ((s = bufferedreader.readLine()) != null) {
                    String[] astring = s.split("=", 2);
                    String s1 = astring[0];

                    switch (s1) {
                        case "x":
                            f = Float.parseFloat(astring[1]);
                            break;

                        case "y":
                            f1 = Float.parseFloat(astring[1]);
                            break;

                        case "z":
                            f2 = Float.parseFloat(astring[1]);
                            break;

                        case "rx":
                            f3 = Float.parseFloat(astring[1]);
                            break;

                        case "ry":
                            f4 = Float.parseFloat(astring[1]);
                            break;

                        case "rz":
                            f5 = Float.parseFloat(astring[1]);
                            break;

                        case "fov":
                            f6 = Float.parseFloat(astring[1]);
                    }
                }
            } catch (Exception exception) {
                exception.printStackTrace();
                return;
            }

            Quaternion quaternion = new Quaternion(f3, f4, f5, ClientDataHolderVR.vrSettings.externalCameraAngleOrder);
            ClientDataHolderVR.vrSettings.mrMovingCamOffsetX = f;
            ClientDataHolderVR.vrSettings.mrMovingCamOffsetY = f1;
            ClientDataHolderVR.vrSettings.mrMovingCamOffsetZ = f2;
            ClientDataHolderVR.vrSettings.mrMovingCamOffsetRotQuat.set(quaternion);
            ClientDataHolderVR.vrSettings.vrFixedCamposX = f;
            ClientDataHolderVR.vrSettings.vrFixedCamposY = f1;
            ClientDataHolderVR.vrSettings.vrFixedCamposZ = f2;
            ClientDataHolderVR.vrSettings.vrFixedCamrotQuat.set(quaternion);
            ClientDataHolderVR.vrSettings.mixedRealityFov = f6;
        }
    }

    public static boolean hasExternalCameraConfig() {
        return (new File("ExternalCamera.cfg")).exists();
    }

    public enum Triggerer {
        BINDING,
        MENUBUTTON,
        INTERACTION
    }
}
