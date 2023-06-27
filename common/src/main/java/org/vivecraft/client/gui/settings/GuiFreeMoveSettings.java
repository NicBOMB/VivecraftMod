package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

public class GuiFreeMoveSettings extends GuiVROptionsBase
{
    public GuiFreeMoveSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.freemove";
        super.clearWidgets();

        if (this.dataholder.vrSettings.seated)
        {
            super.init(VrOptions.SEATED_HMD);
            super.init(VrOptions.FOV_REDUCTION);
            super.init(VrOptions.INERTIA_FACTOR);
        }
        else
        {
            super.init(VrOptions.FREEMOVE_MODE);
            super.init(VrOptions.FREEMOVE_FLY_MODE);
            super.init(VrOptions.FOV_REDUCTION);
            super.init(VrOptions.INERTIA_FACTOR);
            super.init(VrOptions.MOVEMENT_MULTIPLIER);
            super.init(VrOptions.AUTO_SPRINT);
            super.init(VrOptions.AUTO_SPRINT_THRESHOLD);
            super.init(VrOptions.ANALOG_MOVEMENT);
        }

        if (this.dataholder.vrSettings.useFOVReduction)
        {
            super.init(VrOptions.FOV_REDUCTION_MIN);
            super.init(VrOptions.FOV_REDUCTION_OFFSET);
        }

        super.addDefaultButtons();
    }

    protected void actionPerformed(AbstractWidget widget)
    {
        if (widget instanceof GuiVROption guivroption)
        {
            if (guivroption.getOption() == VrOptions.FOV_REDUCTION)
            {
                this.reinit = true;
            }
        }
    }
}
