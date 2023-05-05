package org.vivecraft.mixin_mod_compat_vr.rei;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;

@Pseudo
@Mixin(targets = {"me.shedaniel.rei.impl.client.gui.widget.basewidgets.TextFieldWidget"})
public class reiTextFieldWidgetMixin {

    @Inject(method = "setFocused(Z)V", at = @At("HEAD"))
    private void openKeyboard(boolean focused, CallbackInfo ci) {
        if (!ClientDataHolderVR.getInstance().vrSettings.seated) {
            KeyboardHandler.setOverlayShowing(focused);
        }
    }
}