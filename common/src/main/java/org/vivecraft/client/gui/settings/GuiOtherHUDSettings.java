package org.vivecraft.client.gui.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client.gui.framework.VROptionLayout.Position;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.settings.VRSettings.ChatNotifications;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

public class GuiOtherHUDSettings extends GuiVROptionsBase
{
    public GuiOtherHUDSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.guiother";
        super.clearWidgets();
        super.init(VrOptions.CROSSHAIR_SCALE);
        super.init(VrOptions.RENDER_CROSSHAIR_MODE);
        super.init(VrOptions.RENDER_BLOCK_OUTLINE_MODE);
        super.init(VrOptions.MENU_CROSSHAIR_SCALE);
        super.init(VrOptions.CROSSHAIR_OCCLUSION);
        super.init(VrOptions.CROSSHAIR_SCALES_WITH_DISTANCE);

        super.init(new VROptionEntry(VrOptions.DUMMY, Position.POS_CENTER));

        super.init(VrOptions.AUTO_OPEN_KEYBOARD);
        super.init(new VROptionEntry(VrOptions.PHYSICAL_KEYBOARD, (button, mousePos) -> {
            KeyboardHandler.setOverlayShowing(false);
            return false;
        }));
        super.init(VrOptions.PHYSICAL_KEYBOARD_SCALE);
        super.init(VrOptions.PHYSICAL_KEYBOARD_THEME);
        super.init(VrOptions.CHAT_NOTIFICATIONS);

        if (this.dataholder.vrSettings.chatNotifications == ChatNotifications.SOUND || this.dataholder.vrSettings.chatNotifications == ChatNotifications.BOTH)
        {
            super.init(VrOptions.CHAT_NOTIFICATION_SOUND);
        } else {
            super.init(VrOptions.DUMMY);
        }
        super.init(VrOptions.SHOW_UPDATES);
        super.init(VrOptions.SHOW_PLUGIN);

        super.addDefaultButtons();
    }

    protected void actionPerformed(AbstractWidget widget)
    {
        if (widget instanceof GuiVROption guivroption)
        {
            switch(guivroption.getOption()){
                case CHAT_NOTIFICATIONS -> this.reinit = true;
                case PHYSICAL_KEYBOARD_THEME -> KeyboardHandler.physicalKeyboard.init();
                case MENU_ALWAYS_FOLLOW_FACE -> GuiHandler.onScreenChanged(
                    Minecraft.getInstance().screen,
                    Minecraft.getInstance().screen,
                    false
                );
            }
        }
    }
}
