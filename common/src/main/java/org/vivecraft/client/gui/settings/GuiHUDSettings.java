package org.vivecraft.client.gui.settings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiHUDSettings extends GuiVROptionsBase
{
    public GuiHUDSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.gui";
        super.init(new VROptionEntry(VRSettings.VrOptions.HUD_HIDE), true);
        super.init(VRSettings.VrOptions.HUD_LOCK_TO);
        super.init(VRSettings.VrOptions.HUD_SCALE);
        super.init(VRSettings.VrOptions.HUD_DISTANCE);
        super.init(VRSettings.VrOptions.HUD_OCCLUSION);
        super.init(VRSettings.VrOptions.HUD_OPACITY);
        super.init(VRSettings.VrOptions.RENDER_MENU_BACKGROUND);
        super.init(VRSettings.VrOptions.TOUCH_HOTBAR);
        super.init(VRSettings.VrOptions.AUTO_OPEN_KEYBOARD);
        super.init(VRSettings.VrOptions.MENU_ALWAYS_FOLLOW_FACE);
        super.init(new VROptionEntry(VRSettings.VrOptions.PHYSICAL_KEYBOARD, (button, mousePos) -> {
            KeyboardHandler.setOverlayShowing(false);
            return false;
        }));
        super.init(VRSettings.VrOptions.GUI_APPEAR_OVER_BLOCK);
        super.init(VRSettings.VrOptions.PHYSICAL_KEYBOARD_SCALE);
        super.init(new VROptionEntry("vivecraft.options.screen.menuworld.button", (button, mousePos) -> {
            Minecraft.getInstance().setScreen(new GuiMenuWorldSettings(this));
            return true;
        }));
        super.init(VRSettings.VrOptions.PHYSICAL_KEYBOARD_THEME);
        super.init(VRSettings.VrOptions.SHADER_GUI_RENDER);
        super.addDefaultButtons();
    }

    protected void loadDefaults()
    {
        super.loadDefaults();
        this.minecraft.options.hideGui = false;
    }

    protected void actionPerformed(AbstractWidget widget) {
        if (widget instanceof GuiVROption button) {
            if (button.getId() == VRSettings.VrOptions.PHYSICAL_KEYBOARD_THEME.ordinal()) {
                KeyboardHandler.physicalKeyboard.init();
            }
            if (button.getId() == VRSettings.VrOptions.MENU_ALWAYS_FOLLOW_FACE.ordinal()) {
                GuiHandler.onScreenChanged(Minecraft.getInstance().screen, Minecraft.getInstance().screen, false);
            }
        }
    }
}
