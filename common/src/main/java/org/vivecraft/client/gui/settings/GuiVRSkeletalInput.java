package org.vivecraft.client.gui.settings;

import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.VROptionEntry;
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
            VRSettings.VrOptions.MAIN_LITTLE_THRESHOLD,
            VRSettings.VrOptions.OFF_LITTLE_THRESHOLD
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
            VRSettings.VrOptions.OFF_LITTLE_THRESHOLD,
            VRSettings.VrOptions.MAIN_LITTLE_THRESHOLD
    };

    public GuiVRSkeletalInput(Screen par1GuiScreen)
    {
        super(par1GuiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.controls.skeletal_input";
        super.init(this.dataholder.vrSettings.reverseHands ? fingerThresholdsReversed : fingerThresholds, true);
        super.init(VRSettings.VrOptions.FINGER_COUNT);
        super.init(GuiVRFingerMapping.class, null, "vivecraft.options.screen.controls.skeletal_input.finger_displays.button");
        super.init(new VROptionEntry(VRSettings.VrOptions.SKELETAL_INPUT, VROptionLayout.Position.POS_CENTER));
        super.addDefaultButtons();
    }
}
