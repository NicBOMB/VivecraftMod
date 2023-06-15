package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiOtherHUDSettings extends GuiVROptionsBase
{
    public GuiOtherHUDSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.guiother";
        super.init(VRSettings.VrOptions.CROSSHAIR_SCALE, true);
        super.init(VRSettings.VrOptions.RENDER_CROSSHAIR_MODE);
        super.init(VRSettings.VrOptions.RENDER_BLOCK_OUTLINE_MODE);
        super.init(VRSettings.VrOptions.MENU_CROSSHAIR_SCALE);
        super.init(VRSettings.VrOptions.CROSSHAIR_OCCLUSION);
        super.init(VRSettings.VrOptions.CROSSHAIR_SCALES_WITH_DISTANCE);
        super.init(VRSettings.VrOptions.CHAT_NOTIFICATIONS);

        if (this.dataholder.vrSettings.chatNotifications == VRSettings.ChatNotifications.SOUND || this.dataholder.vrSettings.chatNotifications == VRSettings.ChatNotifications.BOTH)
        {
            super.init(VRSettings.VrOptions.CHAT_NOTIFICATION_SOUND);
        } else {
            super.init(VRSettings.VrOptions.DUMMY);
        }
        super.init(VRSettings.VrOptions.SHOW_UPDATES);
        super.init(VRSettings.VrOptions.SHOW_PLUGIN);

        super.addDefaultButtons();
    }

    protected void actionPerformed(AbstractWidget widget)
    {
        if (widget instanceof GuiVROption guivroption)
        {
            if (guivroption.getId() == VRSettings.VrOptions.CHAT_NOTIFICATIONS.ordinal())
            {
                this.reinit = true;
            }
        }
    }
}
