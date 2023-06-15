package org.vivecraft.client.gui.settings;

import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.VROptionLayout;
import org.vivecraft.client_vr.settings.VRSettings;

import net.minecraft.client.gui.screens.Screen;

public class GuiVRFingerMapping extends GuiVROptionsBase {
    private static final VRSettings.VrOptions[] fingerDisplays = new VRSettings.VrOptions[] {
            VRSettings.VrOptions.MAIN_THUMB_DISPLAY,
            VRSettings.VrOptions.OFF_THUMB_DISPLAY,
            VRSettings.VrOptions.MAIN_INDEX_DISPLAY,
            VRSettings.VrOptions.OFF_INDEX_DISPLAY,
            VRSettings.VrOptions.MAIN_MIDDLE_DISPLAY,
            VRSettings.VrOptions.OFF_MIDDLE_DISPLAY,
            VRSettings.VrOptions.MAIN_RING_DISPLAY,
            VRSettings.VrOptions.OFF_RING_DISPLAY,
            VRSettings.VrOptions.MAIN_PINKY_DISPLAY,
            VRSettings.VrOptions.OFF_PINKY_DISPLAY
    };

    private static final VRSettings.VrOptions[] fingerDisplaysReversed = new VRSettings.VrOptions[] {
            VRSettings.VrOptions.OFF_THUMB_DISPLAY,
            VRSettings.VrOptions.MAIN_THUMB_DISPLAY,
            VRSettings.VrOptions.OFF_INDEX_DISPLAY,
            VRSettings.VrOptions.MAIN_INDEX_DISPLAY,
            VRSettings.VrOptions.OFF_MIDDLE_DISPLAY,
            VRSettings.VrOptions.MAIN_MIDDLE_DISPLAY,
            VRSettings.VrOptions.OFF_RING_DISPLAY,
            VRSettings.VrOptions.MAIN_RING_DISPLAY,
            VRSettings.VrOptions.OFF_PINKY_DISPLAY,
            VRSettings.VrOptions.MAIN_PINKY_DISPLAY
    };

    public GuiVRFingerMapping(Screen par1GuiScreen)
    {
        super(par1GuiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.controls.skeletal_input.finger_displays";
        super.init(this.dataholder.vrSettings.reverseHands ? fingerDisplaysReversed : fingerDisplays, true);
        super.addDefaultButtons();
    }
}
