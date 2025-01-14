package org.vivecraft.mixin.client_vr.gui.screens;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Button.Builder;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.GridLayout.RowHelper;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.vivecraft.client.gui.settings.GuiMainVRSettings;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;

@Mixin(net.minecraft.client.gui.screens.OptionsScreen.class)
public class OptionsScreenVRMixin extends Screen {
    protected OptionsScreenVRMixin(Component component) {
        super(component);
    }

    // replace FOV slider
    /*
    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/OptionInstance;createButton(Lnet/minecraft/client/Options;III)Lnet/minecraft/client/gui/components/AbstractWidget;"))
    private AbstractWidget vivecraft$addVivecraftSettings(OptionInstance option, Options options, int i, int j, int k) {
        if (option == options.fov()) {
            return new Button.Builder( Component.translatable("vivecraft.options.screen.main.button"),  (p) ->
                {
                    mc.options.save();
                    mc.setScreen(new GuiMainVRSettings(this));
                })
                .size( k,  20)
                .pos(i,  j)
                .build();
        } else {
            return option.createButton(options, i, j, k);
        }
    }
    */

    // place below FOV slider
    @ModifyArg(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;I)Lnet/minecraft/client/gui/layouts/LayoutElement;"))
    private int vivecraft$makeSpacer1wide(int layoutElement) {
        return dh.vrSettings.vrSettingsButtonEnabled ? 1 : 2;
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;I)Lnet/minecraft/client/gui/layouts/LayoutElement;"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void vivecraft$addVivecraftSettingsLeft(CallbackInfo ci, GridLayout gridLayout, RowHelper rowHelper) {
        if (dh.vrSettings.vrSettingsButtonEnabled && dh.vrSettings.vrSettingsButtonPositionLeft) {
            this.vivecraft$addVivecraftButton(rowHelper);
        }
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;I)Lnet/minecraft/client/gui/layouts/LayoutElement;", shift = Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void vivecraft$addVivecraftSettingsRight(CallbackInfo ci, GridLayout gridLayout, RowHelper rowHelper) {
        if (dh.vrSettings.vrSettingsButtonEnabled && !dh.vrSettings.vrSettingsButtonPositionLeft) {
            this.vivecraft$addVivecraftButton(rowHelper);
        }
    }

    @Unique
    private void vivecraft$addVivecraftButton(RowHelper rowHelper) {
        rowHelper.addChild(new Builder(Component.translatable("vivecraft.options.screen.main.button"), (p) ->
        {
            mc.options.save();
            mc.setScreen(new GuiMainVRSettings(this));
        })
            .build());
    }

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout;arrangeElements()V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void vivecraft$noBigButtonsPlease(CallbackInfo ci, GridLayout gridLayout, RowHelper rowHelper) {
        gridLayout.visitChildren(child -> {
            if (child.getWidth() > 150 && child instanceof Button button) {
                button.setWidth(150);
            }
        });
    }
}
