package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client.gui.framework.VROptionLayout;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.settings.VRSettings;

public class GuiMainVRSettings extends GuiVROptionsBase
{
    private boolean isConfirm = false;

    public GuiMainVRSettings(Screen lastScreen)
    {
        super(lastScreen);
    }

    protected void init()
    {
        if (!this.isConfirm)
        {
            this.vrTitle = "vivecraft.options.screen.main";

            super.init(GuiRenderOpticsSettings.class, null,"vivecraft.options.screen.stereorendering.button", true);
            super.init(GuiQuickCommandEditor.class, null, "vivecraft.options.screen.quickcommands.button");
            super.init(GuiHUDSettings.class, null, "vivecraft.options.screen.gui.button");
            super.init(GuiOtherHUDSettings.class, null, "vivecraft.options.screen.guiother.button");

            super.init(new VROptionEntry(
                    VRSettings.VrOptions.PLAY_MODE_SEATED,
                    (button, mousePos) -> {
                        this.reinit = true;

                        if (!this.dataholder.vrSettings.seated)
                        {
                            this.isConfirm = true;
                            return true;
                        }
                        else {
                            return false;
                        }
                    },
                    VROptionLayout.Position.POS_CENTER
            ));

            super.init(new VROptionEntry(VRSettings.VrOptions.DUMMY, VROptionLayout.Position.POS_CENTER));

            if (this.dataholder.vrSettings.seated)
            {
                super.init(GuiSeatedOptions.class, VROptionLayout.Position.POS_LEFT, "vivecraft.options.screen.seated.button");
                super.init(
                    new VROptionEntry(
                        VRSettings.VrOptions.RESET_ORIGIN,
                        (button, mousePos) -> {
                            this.resetOrigin();
                            return true;
                        },
                        VROptionLayout.Position.POS_RIGHT
                    )
                );
                super.init(new VROptionEntry(VRSettings.VrOptions.DUMMY, VROptionLayout.Position.POS_CENTER));
            }
            else
            {
                super.init(GuiStandingSettings.class, null, "vivecraft.options.screen.standing.button");
                super.init(GuiRoomscaleSettings.class, null, "vivecraft.options.screen.roomscale.button");
                super.init(GuiVRControls.class, null, "vivecraft.options.screen.controls.button");
                super.init(GuiRadialConfiguration.class, null, "vivecraft.options.screen.radialmenu.button");

                if (this.dataholder.vrSettings.allowStandingOriginOffset)
                {
                    super.init(new VROptionLayout(
                        VRSettings.VrOptions.RESET_ORIGIN,
                        (button, mousePos) -> {
                            this.resetOrigin();
                            return true;
                        },
                        VROptionLayout.Position.POS_LEFT, 7.0F, true, null)
                    );
                }
            }

            super.init(new VRSettings.VrOptions[]{ VRSettings.VrOptions.WORLD_SCALE, VRSettings.VrOptions.WORLD_ROTATION });
            super.init(new VROptionEntry(VRSettings.VrOptions.LOW_HEALTH_INDICATOR, VROptionLayout.Position.POS_RIGHT));
            super.addDefaultButtons();
        }
        else
        {
            this.vrTitle = "vivecraft.messages.seatedmode";
            super.init(
                new VROptionLayout(
                    (button, mousePos) -> {
                        this.reinit = true;
                        this.isConfirm = false;
                        return false;
                    },
                    VROptionLayout.Position.POS_RIGHT,
                    2.0F,
                    true,
                    "gui.cancel"
                ),
                true
            );
            super.init(
                new VROptionLayout(
                    (button, mousePos) -> {
                        this.dataholder.vrSettings.seated = true;
                        this.settings.saveOptions();
                        this.reinit = true;
                        this.isConfirm = false;
                        return false;
                    },
                    VROptionLayout.Position.POS_LEFT,
                    2.0F,
                    true,
                    "vivecraft.gui.ok"
                )
            );
        }
    }

    protected void loadDefaults()
    {
        super.loadDefaults();
        MCVR.get().seatedRot = 0.0F;
        MCVR.get().clearOffset();
    }

    protected void resetOrigin()
    {
        MCVR.get().resetPosition();
        this.settings.saveOptions();
        this.minecraft.setScreen(null);
    }
}
