package org.vivecraft.client_vr.settings;

import net.minecraft.network.chat.Component;
import org.vivecraft.client.utils.LangHelper;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;

public class AutoCalibration {
    public static final float defaultHeight = 1.52F;

    public static void calibrateManual() {
        ClientDataHolderVR.vrSettings.manualCalibration = (float) ClientDataHolderVR.vr.hmdPivotHistory.averagePosition(0.5D).y;
        int i = (int) ((float) ((double) Math.round(100.0D * (double) getPlayerHeight() / (double) 1.52F)));
        VRState.mc.gui.getChat().addMessage(Component.literal(LangHelper.get("vivecraft.messages.heightset", i)));
        ClientDataHolderVR.vrSettings.saveOptions();
    }

    public static float getPlayerHeight() {
        float f = 1.52F;

        if (ClientDataHolderVR.vrSettings.seated) {
            return f;
        } else {
            if (ClientDataHolderVR.vrSettings.manualCalibration != -1.0F) {
                f = ClientDataHolderVR.vrSettings.manualCalibration;
            }

            return f;
        }
    }
}
