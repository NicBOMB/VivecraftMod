package org.vivecraft.client.gui.settings;

import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.VROptionLayout;
import org.vivecraft.client_vr.settings.VRSettings;

import net.minecraft.client.gui.screens.Screen;

public class GuiVRSkeletalInput extends GuiVROptionsBase {
    private static final VRSettings.VrOptions[] fingerThresholds = new VRSettings.VrOptions[] {
            VRSettings.VrOptions.MAIN_THUMB_THRESHOLD,
            VRSettings.VrOptions.OFF_THUMB_THRESHOLD,
            VRSettings.VrOptions.MAIN_INDEX_THRESHOLD,
            VRSettings.VrOptions.OFF_INDEX_THRESHOLD,
            VRSettings.VrOptions.MAIN_MIDDLE_THRESHOLD,
            VRSettings.VrOptions.OFF_MIDDLE_THRESHOLD,
            VRSettings.VrOptions.MAIN_RING_THRESHOLD,
            VRSettings.VrOptions.OFF_RING_THRESHOLD,
            VRSettings.VrOptions.MAIN_PINKY_THRESHOLD,
            VRSettings.VrOptions.OFF_PINKY_THRESHOLD
    };

    private static final VRSettings.VrOptions[] fingerThresholdsReversed = new VRSettings.VrOptions[] {
            VRSettings.VrOptions.OFF_THUMB_THRESHOLD,
            VRSettings.VrOptions.MAIN_THUMB_THRESHOLD,
            VRSettings.VrOptions.OFF_INDEX_THRESHOLD,
            VRSettings.VrOptions.MAIN_INDEX_THRESHOLD,
            VRSettings.VrOptions.OFF_MIDDLE_THRESHOLD,
            VRSettings.VrOptions.MAIN_MIDDLE_THRESHOLD,
            VRSettings.VrOptions.OFF_RING_THRESHOLD,
            VRSettings.VrOptions.MAIN_RING_THRESHOLD,
            VRSettings.VrOptions.OFF_PINKY_THRESHOLD,
            VRSettings.VrOptions.MAIN_PINKY_THRESHOLD
    };

    private static final VROptionLayout[] skeletal_input = new VROptionLayout[] {
            new VROptionLayout(VRSettings.VrOptions.SKELETAL_INPUT, VROptionLayout.Position.POS_CENTER, 6.0F, true, null)
    };

    private static final VRSettings.VrOptions[] finger_count = new VRSettings.VrOptions[] {
            VRSettings.VrOptions.FINGER_COUNT
    };

    public GuiVRSkeletalInput(Screen par1GuiScreen)
    {
        super(par1GuiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.controls.skeletal_input";
        super.init(this.dataholder.vrSettings.reverseHands ? fingerThresholdsReversed : fingerThresholds, true);
        super.init(finger_count, false);
        super.init(GuiVRFingerMapping.class, null, "vivecraft.options.screen.controls.skeletal_input.finger_displays.button", false);
        super.init(skeletal_input, false);
        super.addDefaultButtons();
    }
}
