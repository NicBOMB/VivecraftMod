package org.vivecraft.mixin.client_vr.world.level.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;

@Mixin(DoorBlock.class)
public class DoorBlockVRMixin {

    @Inject(at = @At("HEAD"), method = "playSound")
    public void vivecraft$hapticFeedbackOnClose(Entity entity, Level level, BlockPos blockPos, boolean opening, CallbackInfo ci) {
        if (VRState.vrRunning && !opening && VRState.mc.player != null && VRState.mc.player.isAlive() && VRState.mc.player.blockPosition().distSqr(blockPos) < 25.0D) {
            ClientDataHolderVR.vr.triggerHapticPulse(0, 250);
        }
    }
}
