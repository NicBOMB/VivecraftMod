package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.VROptionLayout.Position;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

public class GuiSeatedOptions extends GuiVROptionsBase
{
    public GuiSeatedOptions(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.seated";
        super.clearWidgets();
        super.init(VrOptions.X_SENSITIVITY);
        super.init(VrOptions.Y_SENSITIVITY);
        super.init(VrOptions.KEYHOLE);
        super.init(VrOptions.SEATED_HUD_XHAIR);
        super.init(VrOptions.WALK_UP_BLOCKS);
        super.init(VrOptions.WORLD_ROTATION_INCREMENT);
        super.init(VrOptions.VEHICLE_ROTATION);
        super.init(VrOptions.DUMMY);
        super.init(new VROptionEntry(VrOptions.SEATED_FREE_MOVE, Position.POS_CENTER));
        super.init(VrOptions.RIGHT_CLICK_DELAY);
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
