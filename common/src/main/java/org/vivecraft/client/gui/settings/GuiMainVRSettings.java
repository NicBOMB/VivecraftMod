package org.vivecraft.client.gui.settings;

import net.minecraft.client.gui.screens.Screen;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client.gui.framework.VROptionLayout;
import org.vivecraft.client.gui.framework.VROptionLayout.Position;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

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

            super.clearWidgets();
            super.init(GuiRenderOpticsSettings.class, "vivecraft.options.screen.stereorendering.button");
            super.init(GuiQuickCommandEditor.class, "vivecraft.options.screen.quickcommands.button");
            super.init(GuiHUDSettings.class, "vivecraft.options.screen.gui.button");
            super.init(GuiOtherHUDSettings.class, "vivecraft.options.screen.guiother.button");

            super.init(new VROptionEntry(
                    VrOptions.PLAY_MODE_SEATED,
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
                    Position.POS_CENTER
            ));

            super.init(new VROptionEntry(VrOptions.DUMMY, Position.POS_CENTER));

            if (this.dataholder.vrSettings.seated)
            {
                super.init(GuiSeatedOptions.class, "vivecraft.options.screen.seated.button", Position.POS_LEFT);
                super.init(
                    new VROptionEntry(
                        VrOptions.RESET_ORIGIN,
                        (button, mousePos) -> {
                            this.resetOrigin();
                            return true;
                        },
                        Position.POS_RIGHT
                    )
                );
                super.init(new VROptionEntry(VrOptions.DUMMY, Position.POS_CENTER));
            }
            else
            {
                super.init(GuiStandingSettings.class, "vivecraft.options.screen.standing.button");
                super.init(GuiRoomscaleSettings.class, "vivecraft.options.screen.roomscale.button");
                super.init(GuiVRControls.class, "vivecraft.options.screen.controls.button");
                super.init(GuiRadialConfiguration.class, "vivecraft.options.screen.radialmenu.button");

                if (this.dataholder.vrSettings.allowStandingOriginOffset)
                {
                    super.init(new VROptionLayout(
                        VrOptions.RESET_ORIGIN,
                        (button, mousePos) -> {
                            this.resetOrigin();
                            return true;
                        },
                        Position.POS_LEFT, 7.0F, true, null)
                    );
                }
            }

            super.init(new VrOptions[]{ VrOptions.WORLD_SCALE, VrOptions.WORLD_ROTATION });
            super.init(new VROptionEntry(VrOptions.LOW_HEALTH_INDICATOR, Position.POS_RIGHT));
            super.addDefaultButtons();
        }
        else
        {
            this.vrTitle = "vivecraft.messages.seatedmode";
            super.clearWidgets();
            super.init(
                new VROptionLayout(
                    (button, mousePos) -> {
                        this.reinit = true;
                        this.isConfirm = false;
                        return false;
                    },
                    Position.POS_RIGHT,
                    2.0F,
                    true,
                    "gui.cancel"
                )
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
                    Position.POS_LEFT,
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
        if (MCVR.get() != null) {
            MCVR.get().resetPosition();
        }
        this.settings.saveOptions();
        this.minecraft.setScreen(null);
    }
}
