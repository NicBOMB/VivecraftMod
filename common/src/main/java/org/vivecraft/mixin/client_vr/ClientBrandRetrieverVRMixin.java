package org.vivecraft.mixin.client_vr;

import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.vivecraft.client_vr.VRState.vrEnabled;

@Mixin(ClientBrandRetriever.class)
public class ClientBrandRetrieverVRMixin {
    @Inject(at = @At("RETURN"), method = "getClientModName", remap = false, cancellable = true)
    private static void vivecraft$vivecraftClientBrand(CallbackInfoReturnable<String> cir) {
        if (vrEnabled) {
            cir.setReturnValue("vivecraft");
        }
    }
}
