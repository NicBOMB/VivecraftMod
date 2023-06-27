package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client.gui.framework.VROptionLayout.Position;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

public class GuiTouchHotbarSettings extends GuiVROptionsBase
{
    public GuiTouchHotbarSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.touch_hotbar";
        super.clearWidgets();
        super.init(new VROptionEntry(VrOptions.TOUCH_HOTBAR, Position.POS_CENTER));
        super.init(new VROptionEntry(VrOptions.DUMMY, Position.POS_CENTER));
        if (settings.vrTouchHotbar) {
            super.init(VrOptions.TOUCH_HOTBAR_COLOR_R);
            super.init(VrOptions.TOUCH_HOTBAR_COLOR_G);
            super.init(VrOptions.TOUCH_HOTBAR_COLOR_B);
            super.init(VrOptions.TOUCH_HOTBAR_COLOR_A);
            super.init(new VROptionEntry(VrOptions.DUMMY, Position.POS_CENTER));
            super.init(VrOptions.TOUCH_OFFBAR_COLOR_R);
            super.init(VrOptions.TOUCH_OFFBAR_COLOR_G);
            super.init(VrOptions.TOUCH_OFFBAR_COLOR_B);
            super.init(VrOptions.TOUCH_OFFBAR_COLOR_A);
        }
        super.addDefaultButtons();
    }

    protected void actionPerformed(AbstractWidget widget)
    {
        if (widget instanceof GuiVROption guivroption)
        {
            if(guivroption.getOption() == VrOptions.TOUCH_HOTBAR){
                this.reinit = true;
            }
        }
    }
}
