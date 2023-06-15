package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiRoomscaleSettings extends GuiVROptionsBase
{
    public GuiRoomscaleSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.roomscale";
        super.init(VRSettings.VrOptions.WEAPON_COLLISION, true);
        super.init(VRSettings.VrOptions.REALISTIC_JUMP);
        super.init(VRSettings.VrOptions.REALISTIC_SNEAK);
        super.init(VRSettings.VrOptions.REALISTIC_CLIMB);
        super.init(VRSettings.VrOptions.REALISTIC_ROW);
        super.init(VRSettings.VrOptions.REALISTIC_SWIM);
        super.init(VRSettings.VrOptions.BOW_MODE);
        super.init(VRSettings.VrOptions.BACKPACK_SWITCH);
        super.init(VRSettings.VrOptions.ALLOW_CRAWLING);
        super.addDefaultButtons();
    }
}
