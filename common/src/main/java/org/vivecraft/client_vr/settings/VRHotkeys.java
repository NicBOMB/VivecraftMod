package org.vivecraft.client_vr.settings;

import org.vivecraft.client.utils.LangHelper;
import org.vivecraft.client_vr.VRData.VRDevicePose;
import org.vivecraft.client_vr.extensions.MinecraftExtension;
import org.vivecraft.client_vr.provider.InputSimulator;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;
import org.vivecraft.common.utils.math.Angle;
import org.vivecraft.common.utils.math.Axis;
import org.vivecraft.common.utils.math.Matrix4f;
import org.vivecraft.common.utils.math.Quaternion;
import org.vivecraft.common.utils.math.Vector3;

import com.google.common.util.concurrent.Runnables;

import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.vivecraft.client.utils.Utils.convertOVRMatrix;
import static org.vivecraft.client.utils.Utils.message;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

import static org.lwjgl.glfw.GLFW.*;

public class VRHotkeys
{
    static long nextRead = 0L;
    static final long COOLOFF_PERIOD_MILLIS = 500L;
    static boolean debug = false;
    private static int startController;
    private static VRDevicePose startControllerPose;
    private static float startCamposX;
    private static float startCamposY;
    private static float startCamposZ;
    private static Quaternion startCamrotQuat;
    private static Triggerer camTriggerer;

    public static boolean handleKeyboardInputs(int key, int scanCode, int action, int modifiers)
    {
        if (nextRead != 0L && System.currentTimeMillis() < nextRead)
        {
            return false;
        }
        else
        {

            boolean flag = false;

            if (action == GLFW_PRESS && key == GLFW_KEY_RIGHT_SHIFT && isKeyDown(GLFW_KEY_RIGHT_CONTROL))
            {
                dh.vrSettings.storeDebugAim = true;
                message(Component.translatable("vivecraft.messages.showaim"));
                flag = true;
            }

            if (action == GLFW_PRESS && key == GLFW_KEY_B && isKeyDown(GLFW_KEY_RIGHT_CONTROL))
            {
                dh.vrSettings.walkUpBlocks = !dh.vrSettings.walkUpBlocks;
                message(Component.translatable("vivecraft.messages.walkupblocks", dh.vrSettings.walkUpBlocks ? LangHelper.getYes() : LangHelper.getNo()));
                flag = true;
            }

            if (action == GLFW_PRESS && key == GLFW_KEY_I && isKeyDown(GLFW_KEY_RIGHT_CONTROL))
            {
                dh.vrSettings.inertiaFactor = dh.vrSettings.inertiaFactor.getNext();
                message(Component.translatable("vivecraft.messages.playerinertia", Component.translatable(dh.vrSettings.inertiaFactor.getLangKey())));

                flag = true;
            }

            if (action == GLFW_PRESS && key == GLFW_KEY_R && isKeyDown(GLFW_KEY_RIGHT_CONTROL))
            {
                if (dh.vrPlayer.isTeleportOverridden())
                {
                    dh.vrPlayer.setTeleportOverride(false);
                    message(Component.translatable("vivecraft.messages.teleportdisabled"));
                }
                else
                {
                    dh.vrPlayer.setTeleportOverride(true);
                    message(Component.translatable("vivecraft.messages.teleportenabled"));
                }

                flag = true;
            }

            if (action == GLFW_PRESS && key == GLFW_KEY_HOME && isKeyDown(GLFW_KEY_RIGHT_CONTROL))
            {
                snapMRCam(0);
                flag = true;
            }

            if (action == GLFW_PRESS && key == GLFW_KEY_F12 && debug)
            {
                mc.setScreen(new WinScreen(false, Runnables.doNothing()));
                flag = true;
            }

            if ((mc.level == null || mc.screen != null) && action == GLFW_PRESS && key == GLFW_KEY_F5)
            {
                dh.vrSettings.setOptionValue(VrOptions.MIRROR_DISPLAY);
                ((MinecraftExtension) mc).notifyMirror(dh.vrSettings.getButtonDisplayString(VrOptions.MIRROR_DISPLAY), false, 3000);
            }

            if (flag)
            {
                dh.vrSettings.saveOptions();
            }

            return flag;
        }
    }

    public static void handleMRKeys()
    {

        boolean flag = false;

        if (isKeyDown(GLFW_KEY_LEFT) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && !isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamPos(new Vector3(-0.01F, 0.0F, 0.0F));
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_RIGHT) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && !isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamPos(new Vector3(0.01F, 0.0F, 0.0F));
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_UP) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && !isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamPos(new Vector3(0.0F, 0.0F, -0.01F));
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_DOWN) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && !isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamPos(new Vector3(0.0F, 0.0F, 0.01F));
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_PAGE_UP) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && !isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamPos(new Vector3(0.0F, 0.01F, 0.0F));
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_PAGE_DOWN) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && !isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamPos(new Vector3(0.0F, -0.01F, 0.0F));
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_UP) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamRot(Axis.PITCH, 0.5F);
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_DOWN) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamRot(Axis.PITCH, -0.5F);
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_LEFT) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamRot(Axis.YAW, 0.5F);
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_RIGHT) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamRot(Axis.YAW, -0.5F);
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_PAGE_UP) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamRot(Axis.ROLL, 0.5F);
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_PAGE_DOWN) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamRot(Axis.ROLL, -0.5F);
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_INSERT) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && !isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            mc.options.fov().set(mc.options.fov().get() + 1);
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_DELETE) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && !isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            mc.options.fov().set(mc.options.fov().get() - 1);
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_INSERT) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            ++dh.vrSettings.mixedRealityFov;
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_DELETE) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            --dh.vrSettings.mixedRealityFov;
            flag = true;
        }

        if (flag)
        {
            dh.vrSettings.saveOptions();

            if (dh.vr.mrMovingCamActive)
            {
                mc.gui.getChat().addMessage(Component.literal(LangHelper.get("vivecraft.messages.coords", dh.vrSettings.mrMovingCamOffsetX, dh.vrSettings.mrMovingCamOffsetY, dh.vrSettings.mrMovingCamOffsetZ)));
                Angle angle = dh.vrSettings.mrMovingCamOffsetRotQuat.toEuler();
                mc.gui.getChat().addMessage(Component.literal(LangHelper.get("vivecraft.messages.angles", angle.getPitch(), angle.getYaw(), angle.getRoll())));
            }
            else
            {
                mc.gui.getChat().addMessage(Component.literal(LangHelper.get("vivecraft.messages.coords", dh.vrSettings.vrFixedCamposX, dh.vrSettings.vrFixedCamposY, dh.vrSettings.vrFixedCamposZ)));
                Angle angle1 = dh.vrSettings.vrFixedCamrotQuat.toEuler();
                mc.gui.getChat().addMessage(Component.literal(LangHelper.get("vivecraft.messages.angles", angle1.getPitch(), angle1.getYaw(), angle1.getRoll())));
            }
        }
    }

    private static void adjustCamPos(Vector3 offset)
    {

        if (dh.vr.mrMovingCamActive)
        {
            offset = dh.vrSettings.mrMovingCamOffsetRotQuat.multiply(offset);
            dh.vrSettings.mrMovingCamOffsetX += offset.getX();
            dh.vrSettings.mrMovingCamOffsetY += offset.getY();
            dh.vrSettings.mrMovingCamOffsetZ += offset.getZ();
        }
        else
        {
            offset = dh.vrSettings.vrFixedCamrotQuat.inverse().multiply(offset);
            dh.vrSettings.vrFixedCamposX += offset.getX();
            dh.vrSettings.vrFixedCamposY += offset.getY();
            dh.vrSettings.vrFixedCamposZ += offset.getZ();
        }
    }

    private static void adjustCamRot(Axis axis, float degrees)
    {

        if (dh.vr.mrMovingCamActive)
        {
            dh.vrSettings.mrMovingCamOffsetRotQuat.set(dh.vrSettings.mrMovingCamOffsetRotQuat.rotate(axis, degrees, true));
        }
        else
        {
            dh.vrSettings.vrFixedCamrotQuat.set(dh.vrSettings.vrFixedCamrotQuat.rotate(axis, degrees, false));
        }
    }

    public static void snapMRCam(int controller)
    {

        Vec3 vec3 = dh.vrPlayer.vrdata_room_pre.getController(controller).getPosition();
        dh.vrSettings.vrFixedCamposX = (float)vec3.x;
        dh.vrSettings.vrFixedCamposY = (float)vec3.y;
        dh.vrSettings.vrFixedCamposZ = (float)vec3.z;
        Quaternion quaternion = new Quaternion(convertOVRMatrix(dh.vrPlayer.vrdata_room_pre.getController(controller).getMatrix()));
        dh.vrSettings.vrFixedCamrotQuat.set(quaternion);
    }

    public static void updateMovingThirdPersonCam()
    {

        if (startControllerPose != null)
        {
            VRDevicePose vrdata$vrdevicepose = dh.vrPlayer.vrdata_room_pre.getController(startController);
            Vec3 vec3 = startControllerPose.getPosition();
            Vec3 vec31 = vrdata$vrdevicepose.getPosition().subtract(vec3);
            Matrix4f matrix4f = Matrix4f.multiply(vrdata$vrdevicepose.getMatrix(), startControllerPose.getMatrix().inverted());
            Vector3 vector3 = new Vector3(startCamposX - (float)vec3.x, startCamposY - (float)vec3.y, startCamposZ - (float)vec3.z);
            Vector3 vector31 = matrix4f.transform(vector3);
            dh.vrSettings.vrFixedCamposX = startCamposX + (float)vec31.x + (vector31.getX() - vector3.getX());
            dh.vrSettings.vrFixedCamposY = startCamposY + (float)vec31.y + (vector31.getY() - vector3.getY());
            dh.vrSettings.vrFixedCamposZ = startCamposZ + (float)vec31.z + (vector31.getZ() - vector3.getZ());
            dh.vrSettings.vrFixedCamrotQuat.set(startCamrotQuat.multiply(new Quaternion(convertOVRMatrix(matrix4f))));
        }
    }

    public static void startMovingThirdPersonCam(int controller, Triggerer triggerer)
    {

        startController = controller;
        startControllerPose = dh.vrPlayer.vrdata_room_pre.getController(controller);
        startCamposX = dh.vrSettings.vrFixedCamposX;
        startCamposY = dh.vrSettings.vrFixedCamposY;
        startCamposZ = dh.vrSettings.vrFixedCamposZ;
        startCamrotQuat = dh.vrSettings.vrFixedCamrotQuat.copy();
        camTriggerer = triggerer;
    }

    public static void stopMovingThirdPersonCam()
    {
        startControllerPose = null;
    }

    public static boolean isMovingThirdPersonCam()
    {
        return startControllerPose != null;
    }

    public static int getMovingThirdPersonCamController()
    {
        return startController;
    }

    public static Triggerer getMovingThirdPersonCamTriggerer()
    {
        return camTriggerer;
    }

    public static void loadExternalCameraConfig()
    {
        File file1 = new File("ExternalCamera.cfg");

        if (file1.exists())
        {
            float f = 0.0F;
            float f1 = 0.0F;
            float f2 = 0.0F;
            float f3 = 0.0F;
            float f4 = 0.0F;
            float f5 = 0.0F;
            float f6 = 40.0F;
            String s;

            try (BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(file1), StandardCharsets.UTF_8)))
            {
                while ((s = bufferedreader.readLine()) != null)
                {
                    String[] astring = s.split("=", 2);
                    String s1 = astring[0];

                    switch (s1) {
                        case "x" -> f = Float.parseFloat(astring[1]);
                        case "y" -> f1 = Float.parseFloat(astring[1]);
                        case "z" -> f2 = Float.parseFloat(astring[1]);
                        case "rx" -> f3 = Float.parseFloat(astring[1]);
                        case "ry" -> f4 = Float.parseFloat(astring[1]);
                        case "rz" -> f5 = Float.parseFloat(astring[1]);
                        case "fov" -> f6 = Float.parseFloat(astring[1]);
                    }
                }
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
                return;
            }

            Quaternion quaternion = new Quaternion(f3, f4, f5, dh.vrSettings.externalCameraAngleOrder);
            dh.vrSettings.mrMovingCamOffsetX = f;
            dh.vrSettings.mrMovingCamOffsetY = f1;
            dh.vrSettings.mrMovingCamOffsetZ = f2;
            dh.vrSettings.mrMovingCamOffsetRotQuat.set(quaternion);
            dh.vrSettings.vrFixedCamposX = f;
            dh.vrSettings.vrFixedCamposY = f1;
            dh.vrSettings.vrFixedCamposZ = f2;
            dh.vrSettings.vrFixedCamrotQuat.set(quaternion);
            dh.vrSettings.mixedRealityFov = f6;
        }
    }

    public static boolean hasExternalCameraConfig()
    {
        return (new File("ExternalCamera.cfg")).exists();
    }

    public static boolean isKeyDown(int key) {
        return glfwGetKey(mc.getWindow().getWindow(), key) == GLFW_PRESS || InputSimulator.isKeyDown(key);
    }

    public enum Triggerer
    {
        BINDING,
        MENUBUTTON,
        INTERACTION
    }
}
