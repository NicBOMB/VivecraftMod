package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.VROptionLayout;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiSeatedOptions extends GuiVROptionsBase
{
    public GuiSeatedOptions(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.seated";
        super.init(VRSettings.VrOptions.X_SENSITIVITY, true);
        super.init(VRSettings.VrOptions.Y_SENSITIVITY);
        super.init(VRSettings.VrOptions.KEYHOLE);
        super.init(VRSettings.VrOptions.SEATED_HUD_XHAIR);
        super.init(VRSettings.VrOptions.WALK_UP_BLOCKS);
        super.init(VRSettings.VrOptions.WORLD_ROTATION_INCREMENT);
        super.init(VRSettings.VrOptions.VEHICLE_ROTATION);
        super.init(VRSettings.VrOptions.DUMMY);
        super.init(new VROptionEntry(VRSettings.VrOptions.SEATED_FREE_MOVE, VROptionLayout.Position.POS_CENTER));
        super.init(VRSettings.VrOptions.RIGHT_CLICK_DELAY);
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
