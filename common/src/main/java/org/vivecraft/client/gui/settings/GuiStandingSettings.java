package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client.gui.framework.VROptionLayout;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiStandingSettings extends GuiVROptionsBase
{
    public GuiStandingSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.standing";
        super.init(VRSettings.VrOptions.WALK_UP_BLOCKS, true);
        super.init(VRSettings.VrOptions.VEHICLE_ROTATION);
        super.init(VRSettings.VrOptions.WALK_MULTIPLIER);
        super.init(VRSettings.VrOptions.WORLD_ROTATION_INCREMENT);
        super.init(VRSettings.VrOptions.BCB_ON);
        super.init(VRSettings.VrOptions.ALLOW_STANDING_ORIGIN_OFFSET);
        super.init(new VROptionEntry(VRSettings.VrOptions.FORCE_STANDING_FREE_MOVE, VROptionLayout.Position.POS_CENTER));
        super.init(new VROptionEntry(VRSettings.VrOptions.DUMMY, VROptionLayout.Position.POS_CENTER));
        super.init(new VROptionEntry("vivecraft.options.screen.teleport.button", (button, mousePos) -> {
            this.minecraft.setScreen(new GuiTeleportSettings(this));
            return true;
        }));
        super.init(new VROptionEntry("vivecraft.options.screen.freemove.button", (button, mousePos) -> {
            this.minecraft.setScreen(new GuiFreeMoveSettings(this));
            return true;
        }));
        super.addDefaultButtons();
    }
}
