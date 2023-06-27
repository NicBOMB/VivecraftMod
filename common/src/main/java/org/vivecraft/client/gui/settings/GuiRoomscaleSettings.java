package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

public class GuiRoomscaleSettings extends GuiVROptionsBase
{
    public GuiRoomscaleSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.roomscale";
        super.clearWidgets();
        super.init(VrOptions.WEAPON_COLLISION);
        super.init(VrOptions.REALISTIC_JUMP);
        super.init(VrOptions.REALISTIC_SNEAK);
        super.init(VrOptions.REALISTIC_CLIMB);
        super.init(VrOptions.REALISTIC_ROW);
        super.init(VrOptions.REALISTIC_SWIM);
        super.init(VrOptions.BOW_MODE);
        super.init(VrOptions.BACKPACK_SWITCH);
        super.init(VrOptions.ALLOW_CRAWLING);
        super.addDefaultButtons();
    }
}
