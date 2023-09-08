package org.vivecraft.mixin.client_vr;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;

import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.stream.Stream;

import static org.vivecraft.client.VivecraftVRMod.userKeyBindingSet;
import static org.vivecraft.client.VivecraftVRMod.vanillaBindingSet;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Options.class)
public abstract class OptionsVRMixin {
    @Shadow
    public KeyMapping[] keyMappings;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;load()V"))
    void processOptionsMixin(Options instance) {
        if (this.keyMappings != null)
        {
            if (this.keyMappings.length > 0)
            {
                this.keyMappings = Stream.concat(Arrays.stream(this.keyMappings), userKeyBindingSet.stream()).toArray(KeyMapping[]::new);
            }
            else
            {
                throw new MissingResourceException("keyMappings is empty!", this.keyMappings.getClass().getName(), Arrays.toString(this.keyMappings));
            }
        }
        else
        {
            throw new NullPointerException("keyMappings is null!");
        }

        vanillaBindingSet.addAll(Arrays.asList(this.keyMappings));
        KeyMapping.CATEGORY_SORT_ORDER.put("vivecraft.key.category.gui", 8);
        KeyMapping.CATEGORY_SORT_ORDER.put("vivecraft.key.category.climbey", 9);
        KeyMapping.CATEGORY_SORT_ORDER.put("vivecraft.key.category.keyboard", 10);

        instance.load();
    }

}
