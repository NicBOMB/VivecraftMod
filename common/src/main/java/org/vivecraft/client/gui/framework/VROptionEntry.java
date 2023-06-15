package org.vivecraft.client.gui.framework;

import java.util.function.BiFunction;
import net.minecraft.world.phys.Vec2;
import org.vivecraft.client_vr.settings.VRSettings;

public class VROptionEntry
{
    public final VRSettings.VrOptions option;
    public final String title;
    public final BiFunction<GuiVROption, Vec2, Boolean> customHandler;
    public final VROptionLayout.Position pos;

    public VROptionEntry(String label, BiFunction<GuiVROption, Vec2, Boolean> customHandler, VROptionLayout.Position pos)
    {
        this.option = null;
        this.title = label;
        this.customHandler = customHandler;
        this.pos = pos;
    }

    public VROptionEntry(String label, BiFunction<GuiVROption, Vec2, Boolean> customHandler)
    {
        this.option = null;
        this.title = label;
        this.customHandler = customHandler;
        this.pos = null;
    }

    public VROptionEntry(VRSettings.VrOptions option, BiFunction<GuiVROption, Vec2, Boolean> customHandler, VROptionLayout.Position pos)
    {
        this.option = option;
        this.title = null;
        this.customHandler = customHandler;
        this.pos = pos;
    }

    public VROptionEntry(VRSettings.VrOptions option, BiFunction<GuiVROption, Vec2, Boolean> customHandler)
    {
        this.option = option;
        this.title = null;
        this.customHandler = customHandler;
        this.pos = null;
    }

    public VROptionEntry(VRSettings.VrOptions option, VROptionLayout.Position pos)
    {
        this.option = option;
        this.title = null;
        this.customHandler = null;
        this.pos = pos;
    }

    public VROptionEntry(VRSettings.VrOptions option)
    {
        this.option = option;
        this.title = null;
        this.customHandler = null;
        this.pos = null;
    }
}
