package org.vivecraft.client.gui.settings;

import org.vivecraft.client.gui.framework.VROptionEntry;
import org.vivecraft.client.gui.framework.VROptionLayout;
import org.vivecraft.client.gui.framework.GuiVROptionsBase;
import org.vivecraft.client_vr.settings.VRSettings;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class GuiVRControls extends GuiVROptionsBase
{
    public GuiVRControls(Screen par1GuiScreen)
    {
        super(par1GuiScreen);
    }

    public void init()
    {
        this.vrTitle = "vivecraft.options.screen.controls";
        super.init(new VROptionEntry(VRSettings.VrOptions.DUMMY, VROptionLayout.Position.POS_CENTER), true);
        super.init(new VROptionEntry(VRSettings.VrOptions.DUMMY, VROptionLayout.Position.POS_CENTER));
        super.init(new VROptionEntry(VRSettings.VrOptions.DUMMY, VROptionLayout.Position.POS_CENTER));
        super.init(new VROptionEntry(VRSettings.VrOptions.DUMMY, VROptionLayout.Position.POS_CENTER));
        super.init(new VROptionEntry(VRSettings.VrOptions.DUMMY, VROptionLayout.Position.POS_CENTER));
        // TODO: generate empty space on this page relative to the height of the message end rather than using rough static dummies
        super.init(GuiVRSkeletalInput.class, VROptionLayout.Position.POS_RIGHT, "vivecraft.options.screen.controls.skeletal_input.button");
        super.init(VRSettings.VrOptions.REVERSE_HANDS);
        super.init(VRSettings.VrOptions.RIGHT_CLICK_DELAY);
        super.init(VRSettings.VrOptions.ALLOW_ADVANCED_BINDINGS);
        super.init(VRSettings.VrOptions.THIRDPERSON_ITEMTRANSFORMS);
        super.addDefaultButtons();
    }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);

        int middle = 240 / 2 - this.minecraft.font.lineHeight;
        int lineHeight = this.minecraft.font.lineHeight + 3;

        drawCenteredString(pMatrixStack, this.minecraft.font, Component.translatable("vivecraft.messages.controls.1"), this.width / 2, middle - lineHeight, 16777215);
        drawCenteredString(pMatrixStack, this.minecraft.font, Component.translatable("vivecraft.messages.controls.2"), this.width / 2, middle, 16777215);
        drawCenteredString(pMatrixStack, this.minecraft.font, Component.translatable("vivecraft.messages.controls.3"), this.width / 2, middle + lineHeight, 16777215);
    }
}
