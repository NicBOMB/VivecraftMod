package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiFreeMoveSettings extends GuiVROptionsBase
{
    public GuiFreeMoveSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.freemove";

        if (this.dataholder.vrSettings.seated)
        {
            super.init(VRSettings.VrOptions.SEATED_HMD, true);
            super.init(VRSettings.VrOptions.FOV_REDUCTION);
            super.init(VRSettings.VrOptions.INERTIA_FACTOR);
        }
        else
        {
            super.init(VRSettings.VrOptions.FREEMOVE_MODE, true);
            super.init(VRSettings.VrOptions.FREEMOVE_FLY_MODE);
            super.init(VRSettings.VrOptions.FOV_REDUCTION);
            super.init(VRSettings.VrOptions.INERTIA_FACTOR);
            super.init(VRSettings.VrOptions.MOVEMENT_MULTIPLIER);
            super.init( VRSettings.VrOptions.AUTO_SPRINT);
            super.init(VRSettings.VrOptions.AUTO_SPRINT_THRESHOLD);
            super.init( VRSettings.VrOptions.ANALOG_MOVEMENT);
        }

        if (this.dataholder.vrSettings.useFOVReduction)
        {
            super.init(VRSettings.VrOptions.FOV_REDUCTION_MIN);
            super.init(VRSettings.VrOptions.FOV_REDUCTION_OFFSET);
        }

        super.addDefaultButtons();
    }

    protected void actionPerformed(AbstractWidget widget)
    {
        if (widget instanceof GuiVROption guivroption)
        {
            if (guivroption.getId() == VRSettings.VrOptions.FOV_REDUCTION.ordinal())
            {
                this.reinit = true;
            }
        }
    }
}
