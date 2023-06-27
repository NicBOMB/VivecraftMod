package org.vivecraft.client.gui.settings;

import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import net.minecraft.client.gui.screens.Screen;

public class GuiVRFingerDisplays extends GuiVROptionsBase {
    private static final VrOptions[] fingerDisplaysReversed = new VrOptions[] {
        VrOptions.MAIN_THUMB_DISPLAY,
        VrOptions.OFF_THUMB_DISPLAY,
        VrOptions.MAIN_INDEX_DISPLAY,
        VrOptions.OFF_INDEX_DISPLAY,
        VrOptions.MAIN_MIDDLE_DISPLAY,
        VrOptions.OFF_MIDDLE_DISPLAY,
        VrOptions.MAIN_RING_DISPLAY,
        VrOptions.OFF_RING_DISPLAY,
        VrOptions.MAIN_LITTLE_DISPLAY,
        VrOptions.OFF_LITTLE_DISPLAY
    };

    private static final VrOptions[] fingerDisplays = new VrOptions[] {
        VrOptions.OFF_THUMB_DISPLAY,
        VrOptions.MAIN_THUMB_DISPLAY,
        VrOptions.OFF_INDEX_DISPLAY,
        VrOptions.MAIN_INDEX_DISPLAY,
        VrOptions.OFF_MIDDLE_DISPLAY,
        VrOptions.MAIN_MIDDLE_DISPLAY,
        VrOptions.OFF_RING_DISPLAY,
        VrOptions.MAIN_RING_DISPLAY,
        VrOptions.OFF_LITTLE_DISPLAY,
        VrOptions.MAIN_LITTLE_DISPLAY
    };

    public GuiVRFingerDisplays(Screen par1GuiScreen)
    {
        super(par1GuiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.controls.skeletal_input.finger_displays";
        super.clearWidgets();
        super.init(this.dataholder.vrSettings.reverseHands ? fingerDisplaysReversed : fingerDisplays);
        super.addDefaultButtons();
    }
}
