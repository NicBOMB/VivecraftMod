package org.vivecraft.mod_compat_vr.sodium.mixin;

import me.jellysquid.mods.sodium.client.gui.SodiumGameOptionPages;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.vrInitialized;

@Pseudo
@Mixin(SodiumGameOptionPages.class)
public class ForgeSodiumGameOptionPagesVRMixin {

    @Inject(at = @At("HEAD"), method = "lambda$quality$23", remap = false)
    private static void vivecraft$initframe(Options opts, GraphicsStatus value, CallbackInfo ci) {
        if (vrInitialized) {
            dh.vrRenderer.reinitFrameBuffers("gfx setting change");
        }
    }
}
