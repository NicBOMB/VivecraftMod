package org.vivecraft.mixin.client_vr.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static org.vivecraft.client_vr.VRState.*;

@Mixin(net.minecraft.world.level.block.DoorBlock.class)
public class DoorBlockVRMixin {

    @Inject(at = @At("HEAD"), method = "playSound")
    public void vivecraft$hapticFeedbackOnClose(Entity entity, Level level, BlockPos blockPos, boolean opening, CallbackInfo ci) {
        if (vrRunning && !opening && mc.player != null && mc.player.isAlive() && mc.player.blockPosition().distSqr(blockPos) < 25.0D) {
            dh.vr.triggerHapticPulse(0, 250);
        }
    }
}
