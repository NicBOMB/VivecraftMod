package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

public class GuiHUDSettings extends GuiVROptionsBase
{
    public GuiHUDSettings(Screen guiScreen)
    {
        super(guiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.gui";
        super.clearWidgets();
        super.init(VrOptions.HUD_HIDE);
        super.init(VrOptions.HUD_LOCK_TO);
        super.init(VrOptions.HUD_SCALE);
        super.init(VrOptions.HUD_DISTANCE);
        super.init(VrOptions.HUD_OCCLUSION);
        super.init(VrOptions.HUD_OPACITY);
        super.init(VrOptions.GUI_APPEAR_OVER_BLOCK);
        super.init(VrOptions.RENDER_MENU_BACKGROUND);
        super.init(VrOptions.MENU_ALWAYS_FOLLOW_FACE);
        super.init(GuiMenuWorldSettings.class, "vivecraft.options.screen.menuworld.button");
        super.init(VrOptions.SHADER_GUI_RENDER);
        super.init(GuiTouchHotbarSettings.class, "vivecraft.options.screen.touch_hotbar.button");
        super.addDefaultButtons();
    }

    protected void loadDefaults()
    {
        super.loadDefaults();
        this.minecraft.options.hideGui = false;
    }
}
