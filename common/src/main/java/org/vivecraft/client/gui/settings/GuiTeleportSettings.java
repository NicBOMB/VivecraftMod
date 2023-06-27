package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

public class GuiTeleportSettings extends GuiVROptionsBase
{
    public GuiTeleportSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.teleport";
        super.clearWidgets();
        super.init(VrOptions.SIMULATE_FALLING);
        super.init(VrOptions.LIMIT_TELEPORT);

        if (this.settings.vrLimitedSurvivalTeleport)
        {
            super.init(VrOptions.TELEPORT_UP_LIMIT);
            super.init(VrOptions.TELEPORT_DOWN_LIMIT);
            super.init(VrOptions.TELEPORT_HORIZ_LIMIT);
        }

        super.addDefaultButtons();
    }

    protected void actionPerformed(AbstractWidget widget)
    {
        if (widget instanceof GuiVROption guivroption)
        {
            if (guivroption.getOption() == VrOptions.LIMIT_TELEPORT)
            {
                this.reinit = true;
            }
        }
    }
}
