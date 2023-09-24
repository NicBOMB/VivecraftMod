package org.vivecraft.mixin.client_vr;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;

import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.stream.Stream;

import static org.vivecraft.client.VivecraftVRMod.userKeyBindingSet;
import static org.vivecraft.client.VivecraftVRMod.vanillaBindingSet;

import static org.objectweb.asm.Opcodes.PUTFIELD;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Options.class)
public abstract class OptionsVRMixin
{

    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "FIELD",
            opcode = PUTFIELD,
            target = "Lnet/minecraft/client/Options;keyMappings:[Lnet/minecraft/client/KeyMapping;"
        )
    )
    void processOptionsMixin(Options instance, KeyMapping[] keyMappings, Operation<KeyMapping[]> original)
    {
        if (keyMappings != null)
        {
            if (keyMappings.length > 0)
            {
                keyMappings = Stream.concat(Arrays.stream(keyMappings), userKeyBindingSet.stream()).toArray(KeyMapping[]::new);
                vanillaBindingSet.addAll(Arrays.asList(keyMappings));
                KeyMapping.CATEGORY_SORT_ORDER.put("vivecraft.key.category.gui", 8);
                KeyMapping.CATEGORY_SORT_ORDER.put("vivecraft.key.category.climbey", 9);
                KeyMapping.CATEGORY_SORT_ORDER.put("vivecraft.key.category.keyboard", 10);
                original.call(instance, keyMappings);
            }
            else
            {
                throw new MissingResourceException(
                    "keyMappings is empty!",
                    keyMappings.getClass().getName(),
                    Arrays.toString(keyMappings)
                );
            }
        }
        else
        {
            throw new NullPointerException("keyMappings is null!");
        }
    }

}
