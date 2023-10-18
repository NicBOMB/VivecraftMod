package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.ClientDataHolderVR;

public class GuiQuickCommandEditor extends GuiVROptionsBase {
    private GuiQuickCommandsList guiList;

    public GuiQuickCommandEditor(Screen par1Screen) {
        super(par1Screen);
    }

    public void init() {
        this.vrTitle = "vivecraft.options.screen.quickcommands";
        this.guiList = new GuiQuickCommandsList(this);
        super.init();
        super.addDefaultButtons();
        this.visibleList = this.guiList;
    }

    protected void loadDefaults() {
        super.loadDefaults();
        ClientDataHolderVR.vrSettings.vrQuickCommands = ClientDataHolderVR.vrSettings.getQuickCommandsDefaults();
    }

    protected boolean onDoneClicked() {
        for (int i = 0; i < 12; ++i) {
            String s = (this.guiList.children().get(i)).txt.getValue();
            ClientDataHolderVR.vrSettings.vrQuickCommands[i] = s;
        }

        return super.onDoneClicked();
    }
}
