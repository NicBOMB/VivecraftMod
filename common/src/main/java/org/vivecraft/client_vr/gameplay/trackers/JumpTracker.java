package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.settings.AutoCalibration;

public class JumpTracker extends Tracker {
    public Vec3[] latchStart = new Vec3[]{new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D)};
    public Vec3[] latchStartOrigin = new Vec3[]{new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D)};
    public Vec3[] latchStartPlayer = new Vec3[]{new Vec3(0.0D, 0.0D, 0.0D), new Vec3(0.0D, 0.0D, 0.0D)};
    private boolean c0Latched = false;
    private boolean c1Latched = false;

    public boolean isClimbeyJump() {
        return this.isActive(VRState.mc.player) && this.isClimbeyJumpEquipped();
    }

    public boolean isClimbeyJumpEquipped() {
        return ClientNetworking.serverAllowsClimbey && ((PlayerExtension) VRState.mc.player).vivecraft$isClimbeyJumpEquipped();
    }

    public boolean isActive(LocalPlayer p) {
        if (ClientDataHolderVR.vrSettings.seated) {
            return false;
        } else if (!ClientDataHolderVR.vrPlayer.getFreeMove() && !ClientDataHolderVR.vrSettings.simulateFalling) {
            return false;
        } else if (!ClientDataHolderVR.vrSettings.realisticJumpEnabled) {
            return false;
        } else if (p != null && p.isAlive()) {
            if (VRState.mc.gameMode == null) {
                return false;
            } else if (!p.isInWater() && !p.isInLava() && p.onGround()) {
                return !p.isShiftKeyDown() && !p.isPassenger();
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public boolean isjumping() {
        return this.c1Latched || this.c0Latched;
    }

    public void idleTick(LocalPlayer player) {
        ClientDataHolderVR.vr.getInputAction(VivecraftVRMod.keyClimbeyJump).setEnabled(this.isClimbeyJumpEquipped() && (this.isActive(player) || ClientDataHolderVR.climbTracker.isClimbeyClimbEquipped() && ClientDataHolderVR.climbTracker.isGrabbingLadder()));
    }

    public void reset(LocalPlayer player) {
        this.c1Latched = false;
        this.c0Latched = false;
    }

    public void doProcess(LocalPlayer player) {
        if (this.isClimbeyJumpEquipped()) {
            VRPlayer vrplayer = ClientDataHolderVR.vrPlayer;
            boolean[] aboolean = new boolean[2];

            for (int i = 0; i < 2; ++i) {
                aboolean[i] = VivecraftVRMod.keyClimbeyJump.isDown();
            }

            boolean flag = false;

            if (!aboolean[0] && this.c0Latched) {
                ClientDataHolderVR.vr.triggerHapticPulse(0, 200);
                flag = true;
            }

            Vec3 vec3 = ClientDataHolderVR.vrPlayer.vrdata_room_pre.getController(0).getPosition();
            Vec3 vec31 = ClientDataHolderVR.vrPlayer.vrdata_room_pre.getController(1).getPosition();
            Vec3 vec32 = vec3.add(vec31).scale(0.5D);

            if (aboolean[0] && !this.c0Latched) {
                this.latchStart[0] = vec32;
                this.latchStartOrigin[0] = ClientDataHolderVR.vrPlayer.vrdata_world_pre.origin;
                this.latchStartPlayer[0] = VRState.mc.player.position();
                ClientDataHolderVR.vr.triggerHapticPulse(0, 1000);
            }

            if (!aboolean[1] && this.c1Latched) {
                ClientDataHolderVR.vr.triggerHapticPulse(1, 200);
                flag = true;
            }

            if (aboolean[1] && !this.c1Latched) {
                this.latchStart[1] = vec32;
                this.latchStartOrigin[1] = ClientDataHolderVR.vrPlayer.vrdata_world_pre.origin;
                this.latchStartPlayer[1] = VRState.mc.player.position();
                ClientDataHolderVR.vr.triggerHapticPulse(1, 1000);
            }

            this.c0Latched = aboolean[0];
            this.c1Latched = aboolean[1];
            int j = 0;
            Vec3 vec33 = vec32.subtract(this.latchStart[j]);
            vec33 = vec33.yRot(ClientDataHolderVR.vrPlayer.vrdata_world_pre.rotation_radians);

            if (!flag && this.isjumping()) {
                ClientDataHolderVR.vr.triggerHapticPulse(0, 200);
                ClientDataHolderVR.vr.triggerHapticPulse(1, 200);
            }

            if (flag) {
                ClientDataHolderVR.climbTracker.forceActivate = true;
                Vec3 vec34 = ClientDataHolderVR.vr.controllerHistory[0].netMovement(0.3D).add(ClientDataHolderVR.vr.controllerHistory[1].netMovement(0.3D));
                double d0 = (ClientDataHolderVR.vr.controllerHistory[0].averageSpeed(0.3D) + ClientDataHolderVR.vr.controllerHistory[1].averageSpeed(0.3D)) / 2.0D;
                vec34 = vec34.scale((double) 0.33F * d0);
                float f = 0.66F;

                if (vec34.length() > (double) f) {
                    vec34 = vec34.scale((double) f / vec34.length());
                }

                if (player.hasEffect(MobEffects.JUMP)) {
                    vec34 = vec34.scale((double) player.getEffect(MobEffects.JUMP).getAmplifier() + 1.5D);
                }

                vec34 = vec34.yRot(ClientDataHolderVR.vrPlayer.vrdata_world_pre.rotation_radians);
                Vec3 vec35 = VRState.mc.player.position().subtract(vec33);

                if (vec33.y < 0.0D && vec34.y < 0.0D) {
                    double d2 = -vec34.x;
                    double d1 = player.getDeltaMovement().x + d2 * 1.25D;
                    d2 = -vec34.y;
                    double d3 = -vec34.z;
                    player.setDeltaMovement(d1, d2, player.getDeltaMovement().z + d3 * 1.25D);
                    player.xOld = vec35.x;
                    player.yOld = vec35.y;
                    player.zOld = vec35.z;
                    vec35 = vec35.add(player.getDeltaMovement());
                    player.setPos(vec35.x, vec35.y, vec35.z);
                    ClientDataHolderVR.vrPlayer.snapRoomOriginToPlayerEntity(player, false, true);
                    VRState.mc.player.causeFoodExhaustion(0.3F);
                    VRState.mc.player.setOnGround(false);
                } else {
                    ClientDataHolderVR.vrPlayer.snapRoomOriginToPlayerEntity(player, false, true);
                }
            } else if (this.isjumping()) {
                Vec3 vec36 = this.latchStartOrigin[0].subtract(this.latchStartPlayer[0]).add(VRState.mc.player.position()).subtract(vec33);
                ClientDataHolderVR.vrPlayer.setRoomOrigin(vec36.x, vec36.y, vec36.z, false);
            }
        } else if (ClientDataHolderVR.vr.hmdPivotHistory.netMovement(0.25D).y > 0.1D && ClientDataHolderVR.vr.hmdPivotHistory.latest().y - (double) AutoCalibration.getPlayerHeight() > (double) ClientDataHolderVR.vrSettings.jumpThreshold) {
            player.jumpFromGround();
        }
    }

    public boolean isBoots(ItemStack i) {
        if (i.isEmpty()) {
            return false;
        } else if (!i.hasCustomHoverName()) {
            return false;
        } else if (i.getItem() != Items.LEATHER_BOOTS) {
            return false;
        } else if (!i.hasTag() || !i.getTag().getBoolean("Unbreakable")) {
            return false;
        } else {
            return i.getHoverName().getContents() instanceof TranslatableContents && ((TranslatableContents) i.getHoverName().getContents()).getKey().equals("vivecraft.item.jumpboots") || i.getHoverName().getString().equals("Jump Boots");
        }
    }
}
