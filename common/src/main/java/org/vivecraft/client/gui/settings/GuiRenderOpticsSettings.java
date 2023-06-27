package org.vivecraft.client.gui.settings;

import org.vivecraft.mod_compat_vr.ShadersHelper;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client.gui.framework.GuiVROption;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRHotkeys;
import org.vivecraft.client_vr.settings.VRSettings.MirrorMode;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;

public class GuiRenderOpticsSettings extends GuiVROptionsBase
{
    static VrOptions[] monoDisplayOptions = new VrOptions[] {
            VrOptions.MONO_FOV,
            VrOptions.DUMMY,
            VrOptions.FSAA
    };
    static VrOptions[] openVRDisplayOptions = new VrOptions[] {
            VrOptions.RENDER_SCALEFACTOR,
            VrOptions.MIRROR_DISPLAY,
            VrOptions.FSAA,
            VrOptions.STENCIL_ON,
            VrOptions.HANDHELD_CAMERA_RENDER_SCALE,
            VrOptions.HANDHELD_CAMERA_FOV,
            VrOptions.RELOAD_EXTERNAL_CAMERA,
            VrOptions.MIRROR_EYE
    };
    static VrOptions[] MROptions = new VrOptions[] {
            VrOptions.MIXED_REALITY_UNITY_LIKE,
            VrOptions.MIXED_REALITY_RENDER_HANDS,
            VrOptions.MIXED_REALITY_KEY_COLOR,
            VrOptions.MIXED_REALITY_FOV,
            VrOptions.MIXED_REALITY_UNDISTORTED,
            VrOptions.MONO_FOV,
            VrOptions.MIXED_REALITY_ALPHA_MASK,
            VrOptions.MIXED_REALITY_RENDER_CAMERA_MODEL
    };
    static VrOptions[] UDOptions = new VrOptions[] {
            VrOptions.MONO_FOV
    };
    static VrOptions[] TUDOptions = new VrOptions[] {
            VrOptions.MIXED_REALITY_FOV,
            VrOptions.MIXED_REALITY_RENDER_CAMERA_MODEL
    };
    private float prevRenderScaleFactor = this.settings.renderScaleFactor;
    private float prevHandCameraResScale = this.settings.handCameraResScale;

    public GuiRenderOpticsSettings(Screen par1Screen)
    {
        super(par1Screen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.stereorendering";
        VrOptions[] avrsettings$vroptions = new VrOptions[openVRDisplayOptions.length];
        System.arraycopy(openVRDisplayOptions, 0, avrsettings$vroptions, 0, openVRDisplayOptions.length);

        for (int i = 0; i < avrsettings$vroptions.length; ++i)
        {
            VrOptions vrsettings$vroptions = avrsettings$vroptions[i];

            if (vrsettings$vroptions == VrOptions.RELOAD_EXTERNAL_CAMERA && (!VRHotkeys.hasExternalCameraConfig() || this.dataholder.vrSettings.displayMirrorMode != MirrorMode.MIXED_REALITY && this.dataholder.vrSettings.displayMirrorMode != MirrorMode.THIRD_PERSON))
            {
                avrsettings$vroptions[i] = VrOptions.DUMMY;
            }

            if (vrsettings$vroptions == VrOptions.MIRROR_EYE && this.dataholder.vrSettings.displayMirrorMode != MirrorMode.CROPPED && this.dataholder.vrSettings.displayMirrorMode != MirrorMode.SINGLE)
            {
                avrsettings$vroptions[i] = VrOptions.DUMMY;
            }
        }

        super.clearWidgets();
        super.init(avrsettings$vroptions);

        if (this.dataholder.vrSettings.displayMirrorMode == MirrorMode.MIXED_REALITY)
        {
            avrsettings$vroptions = new VrOptions[MROptions.length];
            System.arraycopy(MROptions, 0, avrsettings$vroptions, 0, MROptions.length);

            for (int j = 0; j < avrsettings$vroptions.length; ++j)
            {
                VrOptions vrsettings$vroptions1 = avrsettings$vroptions[j];

                if (vrsettings$vroptions1 == VrOptions.MONO_FOV && (!this.dataholder.vrSettings.mixedRealityUndistorted || !this.dataholder.vrSettings.mixedRealityUnityLike))
                {
                    avrsettings$vroptions[j] = VrOptions.DUMMY;
                }

                if (vrsettings$vroptions1 == VrOptions.MIXED_REALITY_ALPHA_MASK && !this.dataholder.vrSettings.mixedRealityUnityLike)
                {
                    avrsettings$vroptions[j] = VrOptions.DUMMY;
                }

                if (vrsettings$vroptions1 == VrOptions.MIXED_REALITY_UNDISTORTED && !this.dataholder.vrSettings.mixedRealityUnityLike)
                {
                    avrsettings$vroptions[j] = VrOptions.DUMMY;
                }

                if (vrsettings$vroptions1 == VrOptions.MIXED_REALITY_KEY_COLOR && this.dataholder.vrSettings.mixedRealityAlphaMask && this.dataholder.vrSettings.mixedRealityUnityLike)
                {
                    avrsettings$vroptions[j] = VrOptions.DUMMY;
                }
            }

            super.init(avrsettings$vroptions);
        }
        else if (this.dataholder.vrSettings.displayMirrorMode == MirrorMode.FIRST_PERSON)
        {
            super.init(UDOptions);
        }
        else if (this.dataholder.vrSettings.displayMirrorMode == MirrorMode.THIRD_PERSON)
        {
            super.init(TUDOptions);
        }

        super.addDefaultButtons();
        this.renderables.stream().filter((w) ->
        {
            return w instanceof GuiVROption;
        }).forEach((w) ->
        {
            GuiVROption guivroption = (GuiVROption)w;

//            if (guivroption.getOption() == VrOptions.HANDHELD_CAMERA_RENDER_SCALE && Config.isShaders())  //Optifine
//            {
//                guivroption.active = false;
//            }
        });
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
    }

    protected void loadDefaults()
    {
        super.loadDefaults();
        this.minecraft.options.fov().set(70);
        this.dataholder.vrRenderer.reinitFrameBuffers("Defaults Loaded");
    }

    protected void actionPerformed(AbstractWidget widget)
    {
        if (widget instanceof GuiVROption guivroption)
        {
            switch(guivroption.getOption()){
                case MIRROR_DISPLAY, FSAA, STENCIL_ON -> {
                    if (VRState.vrRunning)
                    {
                        if (guivroption.getOption() != VrOptions.MIRROR_DISPLAY || !ShadersHelper.isShaderActive()) {
                            this.dataholder.vrRenderer.reinitFrameBuffers("Render Setting Changed");
                        }
                    }
                }
                case RELOAD_EXTERNAL_CAMERA -> VRHotkeys.loadExternalCameraConfig();
            }
        }
    }

    public boolean mouseReleased(double pMouseX, double p_94754_, int pMouseY)
    {
        if (this.settings.renderScaleFactor != this.prevRenderScaleFactor || this.settings.handCameraResScale != this.prevHandCameraResScale)
        {
            this.prevRenderScaleFactor = this.settings.renderScaleFactor;
            this.prevHandCameraResScale = this.settings.handCameraResScale;
            if (VRState.vrRunning) {
                this.dataholder.vrRenderer.reinitFrameBuffers("Render Setting Changed");
            }
        }

        return super.mouseReleased(pMouseX, p_94754_, pMouseY);
    }
}
