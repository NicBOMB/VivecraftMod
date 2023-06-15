package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiTeleportSettings extends GuiVROptionsBase
{
    public GuiTeleportSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.teleport";
        super.init(VRSettings.VrOptions.SIMULATE_FALLING, true);
        super.init(VRSettings.VrOptions.LIMIT_TELEPORT);

        if (this.settings.vrLimitedSurvivalTeleport)
        {
            super.init(VRSettings.VrOptions.TELEPORT_UP_LIMIT);
            super.init(VRSettings.VrOptions.TELEPORT_DOWN_LIMIT);
            super.init(VRSettings.VrOptions.TELEPORT_HORIZ_LIMIT);
        }

        super.addDefaultButtons();
    }

    protected void actionPerformed(AbstractWidget widget)
    {
        if (widget instanceof GuiVROption guivroption)
        {
            if (guivroption.getId() == VRSettings.VrOptions.LIMIT_TELEPORT.ordinal())
            {
                this.reinit = true;
            }
        }
    }
}
