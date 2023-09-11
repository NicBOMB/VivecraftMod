package org.vivecraft.client_vr.settings;

import org.vivecraft.client.utils.LangHelper;
import org.vivecraft.client_vr.VRData.VRDevicePose;
import org.vivecraft.client_vr.extensions.MinecraftExtension;
import org.vivecraft.client_vr.provider.InputSimulator;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import com.google.common.util.concurrent.Runnables;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.client.gui.screens.WinScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.vivecraft.client.utils.Utils.message;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

import static org.joml.Math.*;
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
    private static Quaternionf startCamrotQuat;
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
            adjustCamPos(new Vector3f(-0.01F, 0.0F, 0.0F));
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_RIGHT) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && !isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamPos(new Vector3f(0.01F, 0.0F, 0.0F));
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_UP) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && !isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamPos(new Vector3f(0.0F, 0.0F, -0.01F));
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_DOWN) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && !isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamPos(new Vector3f(0.0F, 0.0F, 0.01F));
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_PAGE_UP) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && !isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamPos(new Vector3f(0.0F, 0.01F, 0.0F));
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_PAGE_DOWN) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && !isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamPos(new Vector3f(0.0F, -0.01F, 0.0F));
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_UP) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamRot(1.0F, 0.0F, 0.0F, 0.5F);
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_DOWN) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamRot(1.0F, 0.0F, 0.0F, -0.5F);
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_LEFT) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamRot(0.0F, 1.0F, 0.0F, 0.5F);
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_RIGHT) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamRot(0.0F, 1.0F, 0.0F, -0.5F);
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_PAGE_UP) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamRot(0.0F, 0.0F, 1.0F, 0.5F);
            flag = true;
        }

        if (isKeyDown(GLFW_KEY_PAGE_DOWN) && isKeyDown(GLFW_KEY_RIGHT_CONTROL) && isKeyDown(GLFW_KEY_RIGHT_SHIFT))
        {
            adjustCamRot(0.0F, 0.0F, 1.0F, -0.5F);
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
                message(Component.literal(LangHelper.get("vivecraft.messages.coords", dh.vrSettings.mrMovingCamOffsetX, dh.vrSettings.mrMovingCamOffsetY, dh.vrSettings.mrMovingCamOffsetZ)));
                message(Component.literal(LangHelper.get(
                    "vivecraft.messages.angles",
                    (float)toDegrees(asin((-2.0F * (dh.vrSettings.mrMovingCamOffsetRotQuat.y * dh.vrSettings.mrMovingCamOffsetRotQuat.z - dh.vrSettings.mrMovingCamOffsetRotQuat.w * dh.vrSettings.mrMovingCamOffsetRotQuat.x)))),
                    (float)toDegrees(atan2((2.0F * (dh.vrSettings.mrMovingCamOffsetRotQuat.x * dh.vrSettings.mrMovingCamOffsetRotQuat.z + dh.vrSettings.mrMovingCamOffsetRotQuat.w * dh.vrSettings.mrMovingCamOffsetRotQuat.y)), (dh.vrSettings.mrMovingCamOffsetRotQuat.w * dh.vrSettings.mrMovingCamOffsetRotQuat.w - dh.vrSettings.mrMovingCamOffsetRotQuat.x * dh.vrSettings.mrMovingCamOffsetRotQuat.x - dh.vrSettings.mrMovingCamOffsetRotQuat.y * dh.vrSettings.mrMovingCamOffsetRotQuat.y + dh.vrSettings.mrMovingCamOffsetRotQuat.z * dh.vrSettings.mrMovingCamOffsetRotQuat.z))),
                    (float)toDegrees(atan2((2.0F * (dh.vrSettings.mrMovingCamOffsetRotQuat.x * dh.vrSettings.mrMovingCamOffsetRotQuat.y + dh.vrSettings.mrMovingCamOffsetRotQuat.w * dh.vrSettings.mrMovingCamOffsetRotQuat.z)), (dh.vrSettings.mrMovingCamOffsetRotQuat.w * dh.vrSettings.mrMovingCamOffsetRotQuat.w - dh.vrSettings.mrMovingCamOffsetRotQuat.x * dh.vrSettings.mrMovingCamOffsetRotQuat.x + dh.vrSettings.mrMovingCamOffsetRotQuat.y * dh.vrSettings.mrMovingCamOffsetRotQuat.y - dh.vrSettings.mrMovingCamOffsetRotQuat.z * dh.vrSettings.mrMovingCamOffsetRotQuat.z)))
                )));
            }
            else
            {
                message(Component.literal(LangHelper.get("vivecraft.messages.coords", dh.vrSettings.vrFixedCamposX, dh.vrSettings.vrFixedCamposY, dh.vrSettings.vrFixedCamposZ)));
                message(Component.literal(LangHelper.get(
                    "vivecraft.messages.angles",
                    (float)toDegrees(asin((-2.0F * (dh.vrSettings.vrFixedCamrotQuat.y * dh.vrSettings.vrFixedCamrotQuat.z - dh.vrSettings.vrFixedCamrotQuat.w * dh.vrSettings.vrFixedCamrotQuat.x)))),
                    (float)toDegrees(atan2((2.0F * (dh.vrSettings.vrFixedCamrotQuat.x * dh.vrSettings.vrFixedCamrotQuat.z + dh.vrSettings.vrFixedCamrotQuat.w * dh.vrSettings.vrFixedCamrotQuat.y)), (dh.vrSettings.vrFixedCamrotQuat.w * dh.vrSettings.vrFixedCamrotQuat.w - dh.vrSettings.vrFixedCamrotQuat.x * dh.vrSettings.vrFixedCamrotQuat.x - dh.vrSettings.vrFixedCamrotQuat.y * dh.vrSettings.vrFixedCamrotQuat.y + dh.vrSettings.vrFixedCamrotQuat.z * dh.vrSettings.vrFixedCamrotQuat.z))),
                    (float)toDegrees(atan2((2.0F * (dh.vrSettings.vrFixedCamrotQuat.x * dh.vrSettings.vrFixedCamrotQuat.y + dh.vrSettings.vrFixedCamrotQuat.w * dh.vrSettings.vrFixedCamrotQuat.z)), (dh.vrSettings.vrFixedCamrotQuat.w * dh.vrSettings.vrFixedCamrotQuat.w - dh.vrSettings.vrFixedCamrotQuat.x * dh.vrSettings.vrFixedCamrotQuat.x + dh.vrSettings.vrFixedCamrotQuat.y * dh.vrSettings.vrFixedCamrotQuat.y - dh.vrSettings.vrFixedCamrotQuat.z * dh.vrSettings.vrFixedCamrotQuat.z)))
                )));
            }
        }
    }

    private static void adjustCamPos(Vector3f offset)
    {

        if (dh.vr.mrMovingCamActive)
        {
            dh.vrSettings.mrMovingCamOffsetRotQuat.transformUnit(offset);
            dh.vrSettings.mrMovingCamOffsetX += offset.x;
            dh.vrSettings.mrMovingCamOffsetY += offset.y;
            dh.vrSettings.mrMovingCamOffsetZ += offset.z;
        }
        else
        {
            dh.vrSettings.vrFixedCamrotQuat.conjugate(new Quaternionf()).transformUnit(offset);
            dh.vrSettings.vrFixedCamposX += offset.x;
            dh.vrSettings.vrFixedCamposY += offset.y;
            dh.vrSettings.vrFixedCamposZ += offset.z;
        }
    }

    private static void adjustCamRot(float axisX, float axisY, float axisZ, float degrees)
    {

        if (dh.vr.mrMovingCamActive)
        {
            dh.vrSettings.mrMovingCamOffsetRotQuat.set(dh.vrSettings.mrMovingCamOffsetRotQuat.mul(new Quaternionf().setAngleAxis(
                    degrees,
                    axisX,
                    axisY,
                    axisZ
                ),
                new Quaternionf()));
        }
        else
        {
            Matrix4f matrix4f = new Matrix4f();
            float f = dh.vrSettings.vrFixedCamrotQuat.w * dh.vrSettings.vrFixedCamrotQuat.w;
            float f1 = dh.vrSettings.vrFixedCamrotQuat.x * dh.vrSettings.vrFixedCamrotQuat.x;
            float f2 = dh.vrSettings.vrFixedCamrotQuat.y * dh.vrSettings.vrFixedCamrotQuat.y;
            float f3 = dh.vrSettings.vrFixedCamrotQuat.z * dh.vrSettings.vrFixedCamrotQuat.z;
            float f4 = 1.0F / (f1 + f2 + f3 + f);
            matrix4f.m00((f1 - f2 - f3 + f) * f4);
            matrix4f.m11((-f1 + f2 - f3 + f) * f4);
            matrix4f.m22((-f1 - f2 + f3 + f) * f4);
            float f5 = dh.vrSettings.vrFixedCamrotQuat.x * dh.vrSettings.vrFixedCamrotQuat.y;
            float f6 = dh.vrSettings.vrFixedCamrotQuat.z * dh.vrSettings.vrFixedCamrotQuat.w;
            matrix4f.m10(2.0F * (f5 + f6) * f4);
            matrix4f.m01(2.0F * (f5 - f6) * f4);
            f5 = dh.vrSettings.vrFixedCamrotQuat.x * dh.vrSettings.vrFixedCamrotQuat.z;
            f6 = dh.vrSettings.vrFixedCamrotQuat.y * dh.vrSettings.vrFixedCamrotQuat.w;
            matrix4f.m20(2.0F * (f5 - f6) * f4);
            matrix4f.m02(2.0F * (f5 + f6) * f4);
            f5 = dh.vrSettings.vrFixedCamrotQuat.y * dh.vrSettings.vrFixedCamrotQuat.z;
            f6 = dh.vrSettings.vrFixedCamrotQuat.x * dh.vrSettings.vrFixedCamrotQuat.w;
            matrix4f.m21(2.0F * (f5 + f6) * f4);
            matrix4f.m12(2.0F * (f5 - f6) * f4);
            matrix4f.rotate(toRadians(degrees), new Vector3f(axisX, axisY, axisZ));
            dh.vrSettings.vrFixedCamrotQuat.setFromNormalized(matrix4f);
        }
    }

    public static void snapMRCam(int controller)
    {

        Vec3 vec3 = dh.vrPlayer.vrdata_room_pre.getController(controller).getPosition();
        dh.vrSettings.vrFixedCamposX = (float)vec3.x;
        dh.vrSettings.vrFixedCamposY = (float)vec3.y;
        dh.vrSettings.vrFixedCamposZ = (float)vec3.z;
        dh.vrSettings.vrFixedCamrotQuat.setFromNormalized(dh.vrPlayer.vrdata_room_pre.getController(controller).getMatrix());
    }

    public static void updateMovingThirdPersonCam()
    {

        if (startControllerPose != null)
        {
            VRDevicePose vrdata$vrdevicepose = dh.vrPlayer.vrdata_room_pre.getController(startController);
            Vec3 vec3 = startControllerPose.getPosition();
            Vec3 vec31 = vrdata$vrdevicepose.getPosition().subtract(vec3);
            Matrix4f matrix4f = vrdata$vrdevicepose.getMatrix().mul0(startControllerPose.getMatrix(), new Matrix4f());
            Vector3f vector3 = new Vector3f(startCamposX - (float)vec3.x, startCamposY - (float)vec3.y, startCamposZ - (float)vec3.z);
            Vector3f vector31 = vector3.mulProject(matrix4f, new Vector3f());
            dh.vrSettings.vrFixedCamposX = startCamposX + (float)vec31.x + (vector31.x - vector3.x);
            dh.vrSettings.vrFixedCamposY = startCamposY + (float)vec31.y + (vector31.y - vector3.y);
            dh.vrSettings.vrFixedCamposZ = startCamposZ + (float)vec31.z + (vector31.z - vector3.z);
            dh.vrSettings.vrFixedCamrotQuat.setFromNormalized(matrix4f);
            startCamrotQuat.mul(dh.vrSettings.vrFixedCamrotQuat, dh.vrSettings.vrFixedCamrotQuat);
        }
    }

    public static void startMovingThirdPersonCam(int controller, Triggerer triggerer)
    {

        startController = controller;
        startControllerPose = dh.vrPlayer.vrdata_room_pre.getController(controller);
        startCamposX = dh.vrSettings.vrFixedCamposX;
        startCamposY = dh.vrSettings.vrFixedCamposY;
        startCamposZ = dh.vrSettings.vrFixedCamposZ;
        startCamrotQuat = new Quaternionf(dh.vrSettings.vrFixedCamrotQuat);
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

            Quaternionf quaternion0 = new Quaternionf().setAngleAxis(f3, 1.0F, 0.0F, 0.0F);
            Quaternionf quaternion1 = new Quaternionf().setAngleAxis(f4, 0.0F, 1.0F, 0.0F);
            Quaternionf quaternion2 = new Quaternionf().setAngleAxis(f5, 0.0F, 0.0F, 1.0F);
            Quaternionf quaternion = (switch (dh.vrSettings.externalCameraAngleOrder)
                {
                    case XYZ -> quaternion0.mul(quaternion1, new Quaternionf()).mul(quaternion2, new Quaternionf());
                    case ZYX -> quaternion2.mul(quaternion1, new Quaternionf()).mul(quaternion0, new Quaternionf());
                    case YXZ -> quaternion1.mul(quaternion0, new Quaternionf()).mul(quaternion2, new Quaternionf());
                    case ZXY -> quaternion2.mul(quaternion0, new Quaternionf()).mul(quaternion1, new Quaternionf());
                    case YZX -> quaternion1.mul(quaternion2, new Quaternionf()).mul(quaternion0, new Quaternionf());
                    case XZY -> quaternion0.mul(quaternion2, new Quaternionf()).mul(quaternion1, new Quaternionf());
                }
            );
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
