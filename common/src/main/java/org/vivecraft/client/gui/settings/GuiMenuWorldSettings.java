package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiMenuWorldSettings extends GuiVROptionsBase {
    private final VROptionEntry[] miscSettings = new VROptionEntry[]{
        new VROptionEntry(VRSettings.VrOptions.MENU_WORLD_SELECTION),
        new VROptionEntry("vivecraft.gui.menuworld.refresh", (button, mousePos) -> {
            if (ClientDataHolderVR.menuWorldRenderer != null && ClientDataHolderVR.menuWorldRenderer.getLevel() != null) {
                try {
                    ClientDataHolderVR.menuWorldRenderer.destroy();
                    ClientDataHolderVR.menuWorldRenderer.prepare();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }

            return true;
        }),
        new VROptionEntry(VRSettings.VrOptions.DUMMY), new VROptionEntry("vivecraft.gui.menuworld.loadnew", (button, mousePos) -> {
        if (ClientDataHolderVR.menuWorldRenderer != null) {
            try {
                if (ClientDataHolderVR.menuWorldRenderer.isReady()) {
                    ClientDataHolderVR.menuWorldRenderer.destroy();
                }

                ClientDataHolderVR.menuWorldRenderer.init();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        return true;
    })
    };

    public GuiMenuWorldSettings(Screen guiScreen) {
        super(guiScreen);
    }

    public void init() {
        this.vrTitle = "vivecraft.options.screen.menuworld";
        super.init(this.miscSettings, true);
        super.addDefaultButtons();
    }
}
