package org.vivecraft.mixin.client_vr.gui.screens.inventory;

import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;

import static org.vivecraft.client_vr.VRState.vrRunning;

@Mixin(AbstractSignEditScreen.class)
public class AbstractSignEditScreenVRMixin {

    @Inject(at = @At("HEAD"), method = "init")
    public void vivecraft$showOverlay(CallbackInfo ci) {
        if (vrRunning) {
            KeyboardHandler.setOverlayShowing(true);
        }
    }

    @Inject(at = @At("HEAD"), method = "removed")
    public void vivecraft$dontShowOverlay(CallbackInfo ci) {
        if (vrRunning) {
            KeyboardHandler.setOverlayShowing(false);
        }
    }
}
