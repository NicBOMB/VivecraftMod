package org.vivecraft.client_vr.provider.nullvr;

import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.provider.openvr_lwjgl.VRInputAction;
import org.vivecraft.client_vr.settings.VRHotkeys;

import org.joml.Vector2f;

import net.minecraft.client.KeyMapping;

import java.util.List;

import static org.vivecraft.client.utils.Utils.Matrix4fSetIdentity;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.logger;

import static org.lwjgl.glfw.GLFW.*;

public class NullVR extends MCVR
{
    protected static NullVR ome;

    private boolean vrActive = true;
    private boolean vrActiveChangedLastFrame = false;

    public NullVR()
    {
        super();
        ome = this;
        this.hapticScheduler = new NullVRHapticScheduler();
    }

    public static NullVR get()
    {
        return ome;
    }


    @Override
    public void destroy()
    {
        this.initialized = false;
    }

    @Override
    public String getID()
    {
        return "nullDriver";
    }

    @Override
    public String getName()
    {
        return "nullDriver";
    }

    @Override
    public Vector2f getPlayAreaSize()
    {
        return new Vector2f(2);
    }

    @Override
    public boolean init()
    {
        if (!this.initialized)
        {
            // only supports seated mode
            logger.info("NullDriver. Forcing seated mode.");
            dh.vrSettings.seated = true;

            this.headIsTracking = false;
            Matrix4fSetIdentity(this.hmdPose);
            this.hmdPose.M[1][3] = 1.62F;

            // eye offset, 10cm total distance
            this.hmdPoseLeftEye.M[0][3] = -0.05F;
            this.hmdPoseRightEye.M[0][3] = 0.05F;

            this.initialized = true;
            this.initSuccess = true;
        }

        return true;
    }

    @Override
    public void poll(long frameIndex)
    {
        if (this.initialized)
        {

            mc.getProfiler().push("updatePose");

            if (mc.screen == null) {

                // don't permanently change the sensitivity
                float xSens = dh.vrSettings.xSensitivity;
                float xKey = dh.vrSettings.keyholeX;

                dh.vrSettings.xSensitivity = dh.vrSettings.ySensitivity * 1.636F * ((float)mc.getWindow().getScreenWidth() / (float)mc.getWindow().getScreenHeight());
                dh.vrSettings.keyholeX = 1;

                this.updateAim();

                dh.vrSettings.xSensitivity = xSens;
                dh.vrSettings.keyholeX = xKey;


                // point head in cursor direction
                hmdRotation.M[0][0] = handRotation[0].M[0][0];
                hmdRotation.M[0][1] = handRotation[0].M[0][1];
                hmdRotation.M[0][2] = handRotation[0].M[0][2];
                hmdRotation.M[1][0] = handRotation[0].M[1][0];
                hmdRotation.M[1][1] = handRotation[0].M[1][1];
                hmdRotation.M[1][2] = handRotation[0].M[1][2];
                hmdRotation.M[2][0] = handRotation[0].M[2][0];
                hmdRotation.M[2][1] = handRotation[0].M[2][1];
                hmdRotation.M[2][2] = handRotation[0].M[2][2];
            } else if(GuiHandler.guiRotation_room != null){
                // look at screen, so that it's centered
                hmdRotation.M[0][0] = GuiHandler.guiRotation_room.M[0][0];
                hmdRotation.M[0][1] = GuiHandler.guiRotation_room.M[0][1];
                hmdRotation.M[0][2] = GuiHandler.guiRotation_room.M[0][2];
                hmdRotation.M[1][0] = GuiHandler.guiRotation_room.M[1][0];
                hmdRotation.M[1][1] = GuiHandler.guiRotation_room.M[1][1];
                hmdRotation.M[1][2] = GuiHandler.guiRotation_room.M[1][2];
                hmdRotation.M[2][0] = GuiHandler.guiRotation_room.M[2][0];
                hmdRotation.M[2][1] = GuiHandler.guiRotation_room.M[2][1];
                hmdRotation.M[2][2] = GuiHandler.guiRotation_room.M[2][2];

            }
            mc.getProfiler().popPush("hmdSampling");
            this.hmdSampling();
            
            mc.getProfiler().pop();
        }
    }

    @Override
    public void processInputs()
    {
    }

    @Override
    @Deprecated
    protected void triggerBindingHapticPulse(KeyMapping binding, int duration)
    {
    }

    @Override
    protected ControllerType findActiveBindingControllerType(KeyMapping binding)
    {
        return null;
    }

    @Override
    public org.vivecraft.common.utils.math.Matrix4f getControllerComponentTransform(int controllerIndex, String componentName)
    {
        return new org.vivecraft.common.utils.math.Matrix4f();
    }

    @Override
    public String getOriginName(long handle)
    {
        return "NullDriver";
    }

    @Override
    public ControllerType getOriginControllerType(long inputValueHandle) {
        return null;
    }

    @Override
    public boolean postinit()
    {
        this.populateInputActions();
        return true;
    }

    @Override
    public boolean hasThirdController()
    {
        return false;
    }

    @Override
    public List<Long> getOrigins(VRInputAction action) {
        return null;
    }

    @Override
    public VRRenderer createVRRenderer() {
        return new NullVRStereoRenderer(this);
    }

    @Override
    public boolean isActive() {
        if (VRHotkeys.isKeyDown(GLFW_KEY_RIGHT_CONTROL) && VRHotkeys.isKeyDown(GLFW_KEY_F6)) {
            if (!vrActiveChangedLastFrame) {
                vrActive = !vrActive;
                vrActiveChangedLastFrame = true;
            }
        } else {
            vrActiveChangedLastFrame = false;
        }
        return vrActive;
    }
}
