package org.vivecraft.client.gui.settings;

import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client.gui.framework.VROptionLayout.Position;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import net.minecraft.client.gui.screens.Screen;

public class GuiVRSkeletalInput extends GuiVROptionsBase {
    private static final VrOptions[] fingerThresholdsReversed = new VrOptions[] {
        VrOptions.MAIN_THUMB_THRESHOLD,
        VrOptions.OFF_THUMB_THRESHOLD,
        VrOptions.MAIN_INDEX_THRESHOLD,
        VrOptions.OFF_INDEX_THRESHOLD,
        VrOptions.MAIN_MIDDLE_THRESHOLD,
        VrOptions.OFF_MIDDLE_THRESHOLD,
        VrOptions.MAIN_RING_THRESHOLD,
        VrOptions.OFF_RING_THRESHOLD,
        VrOptions.MAIN_LITTLE_THRESHOLD,
        VrOptions.OFF_LITTLE_THRESHOLD
    };

    private static final VrOptions[] fingerThresholds = new VrOptions[] {
        VrOptions.OFF_THUMB_THRESHOLD,
        VrOptions.MAIN_THUMB_THRESHOLD,
        VrOptions.OFF_INDEX_THRESHOLD,
        VrOptions.MAIN_INDEX_THRESHOLD,
        VrOptions.OFF_MIDDLE_THRESHOLD,
        VrOptions.MAIN_MIDDLE_THRESHOLD,
        VrOptions.OFF_RING_THRESHOLD,
        VrOptions.MAIN_RING_THRESHOLD,
        VrOptions.OFF_LITTLE_THRESHOLD,
        VrOptions.MAIN_LITTLE_THRESHOLD
    };

    public GuiVRSkeletalInput(Screen par1GuiScreen)
    {
        super(par1GuiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.controls.skeletal_input";
//        super.init(this.dataholder.vrSettings.reverseHands ? fingerThresholdsReversed : fingerThresholds, true);
        super.init(new VROptionEntry(VrOptions.DUMMY, Position.POS_CENTER));
        super.init(new VROptionEntry(VrOptions.DUMMY, Position.POS_CENTER));
        super.init(new VROptionEntry(VrOptions.DUMMY, Position.POS_CENTER));
        super.init(new VROptionEntry(VrOptions.DUMMY, Position.POS_CENTER));
        super.init(new VROptionEntry(VrOptions.DUMMY, Position.POS_CENTER));
        super.init(VrOptions.FINGER_COUNT);
        super.init(GuiVRFingerDisplays.class, "vivecraft.options.screen.controls.skeletal_input.finger_displays.button");
        super.init(VrOptions.FINGER_VIEW);
        super.init(new VROptionEntry(VrOptions.SKELETAL_INPUT, Position.POS_CENTER));
        super.addDefaultButtons();
    }
}
