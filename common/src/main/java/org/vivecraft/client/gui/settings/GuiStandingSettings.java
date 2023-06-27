package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client.gui.framework.VROptionLayout.Position;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

public class GuiStandingSettings extends GuiVROptionsBase
{
    public GuiStandingSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.standing";
        super.clearWidgets();
        super.init(VrOptions.WALK_UP_BLOCKS);
        super.init(VrOptions.VEHICLE_ROTATION);
        super.init(VrOptions.WALK_MULTIPLIER);
        super.init(VrOptions.WORLD_ROTATION_INCREMENT);
        super.init(VrOptions.BCB_ON);
        super.init(VrOptions.ALLOW_STANDING_ORIGIN_OFFSET);
        super.init(new VROptionEntry(VrOptions.FORCE_STANDING_FREE_MOVE, Position.POS_CENTER));
        super.init(new VROptionEntry(VrOptions.DUMMY, Position.POS_CENTER));
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
