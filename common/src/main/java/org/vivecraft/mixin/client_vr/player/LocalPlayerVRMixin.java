package org.vivecraft.mixin.client_vr.player;

import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.extensions.ItemInHandRendererExtension;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.render.VRFirstPersonArmSwing;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_vr.utils.external.jinfinadeck;
import org.vivecraft.client_vr.utils.external.jkatvr;
import org.vivecraft.common.network.CommonNetworkHelper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static org.vivecraft.client_vr.VRState.*;

import static org.joml.Math.*;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerVRMixin extends AbstractClientPlayer implements PlayerExtension {

    @Unique
    private Vec3 moveMulIn = Vec3.ZERO;
    @Unique
    private boolean initFromServer;
    @Unique
    private int movementTeleportTimer;
    @Unique
    private boolean teleported;
    @Unique
    private double additionX;
    @Unique
    private double additionZ;
    @Final
    @Shadow
    protected Minecraft minecraft;
    @Shadow
    private boolean startedUsingItem;
    @Shadow
    @Final
    public ClientPacketListener connection;
    @Shadow
    private InteractionHand usingItemHand;
    @Shadow
    public Input input;

    public LocalPlayerVRMixin(ClientLevel clientLevel, GameProfile gameProfile) {
        super(clientLevel, gameProfile);
    }

    @Shadow
    protected abstract void updateAutoJump(float f, float g);

    @Shadow
    public abstract void swing(InteractionHand interactionHand);

    @Inject(at = @At("TAIL"), method = "startRiding")
    public void startRidingTracker(Entity entity, boolean bl, CallbackInfoReturnable<Boolean> cir) {
        if (vrInitialized) {
            dh.vehicleTracker.onStartRiding(entity, (LocalPlayer) (Object) this);
        }
    }

    @Inject(at = @At("TAIL"), method = "removeVehicle")
    public void stopRidingTracker(CallbackInfo ci) {
        if (vrInitialized) {
            dh.vehicleTracker.onStopRiding((LocalPlayer) (Object) this);
        }
    }

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift = At.Shift.BEFORE), method = "tick")
	public void overrideLookPre(CallbackInfo ci) {
        if (vrRunning) {
            dh.vrPlayer.doPermanentLookOverride((LocalPlayer) (Object) this, dh.vrPlayer.vrdata_world_pre);
        }
	}

	@Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;tick()V", shift = At.Shift.AFTER), method = "tick")
	public void overridePose(CallbackInfo ci) {
        if (vrRunning) {
            ClientNetworking.overridePose((LocalPlayer) (Object) this);
            dh.vrPlayer.doPermanentLookOverride((LocalPlayer) (Object) this, dh.vrPlayer.vrdata_world_pre);
        }
	}

    @ModifyVariable(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isPassenger()Z"), ordinal = 2, method = "sendPosition")
    private boolean directTeleport(boolean updateRotation) {
        if (this.teleported) {
            updateRotation = true;
            ByteBuf bytebuf = Unpooled.buffer();
            bytebuf.writeFloat((float) this.getX());
            bytebuf.writeFloat((float) this.getY());
            bytebuf.writeFloat((float) this.getZ());
            byte[] abyte = new byte[bytebuf.readableBytes()];
            bytebuf.readBytes(abyte);
            ServerboundCustomPayloadPacket serverboundcustompayloadpacket = ClientNetworking.getVivecraftClientPacket(CommonNetworkHelper.PacketDiscriminators.TELEPORT, abyte);
            this.connection.send(serverboundcustompayloadpacket);
        }
        return updateRotation;
    }

    // needed, or the server will spam 'moved too quickly'/'moved wrongly'
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;send(Lnet/minecraft/network/protocol/Packet;)V"), method = "sendPosition", slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isPassenger()Z")))
    public void noMovePacketsOnTeleport(ClientPacketListener instance, Packet<?> packet) {
        if (!this.teleported) {
            instance.send(packet);
        }
    }

    @Inject(at = @At(value = "FIELD", target = "Lnet/minecraft/client/player/LocalPlayer;lastOnGround:Z", shift = At.Shift.AFTER, ordinal = 1), method = "sendPosition")
    public void walkUp(CallbackInfo ci) {
        // clear teleport here, after all the packets would be sent
        this.teleported = false;
        if (vrRunning && dh.vrSettings.walkUpBlocks) {
            this.minecraft.options.autoJump().set(false);
        }
    }

    @Override
    public void swingArm(InteractionHand interactionhand, VRFirstPersonArmSwing interact) {
        ((ItemInHandRendererExtension) this.minecraft.getEntityRenderDispatcher().getItemInHandRenderer()).setSwingType(interact);
        this.swing(interactionhand);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/AbstractClientPlayer;aiStep()V"), method = "aiStep")
    public void ai(CallbackInfo ci) {
        if (vrRunning) {
            dh.vrPlayer.tick((LocalPlayer) (Object) this, this.minecraft, this.random);
        }
    }

    @Inject(at = @At("HEAD"), method = "move(Lnet/minecraft/world/entity/MoverType;Lnet/minecraft/world/phys/Vec3;)V", cancellable = true)
    public void overwriteMove(MoverType pType, Vec3 pPos, CallbackInfo info) {
        if (!vrRunning) {
            return;
        }

        this.moveMulIn = this.stuckSpeedMultiplier;

        if (pPos.length() != 0.0D && !this.isPassenger()) {
            boolean flag = VRPlayer.get().getFreeMove();
            boolean flag1 = flag || dh.vrSettings.simulateFalling && !this.onClimbable()
                    && !this.isShiftKeyDown();

            if (dh.climbTracker.isActive()
                    && (flag || dh.climbTracker.isGrabbingLadder())) {
                flag1 = true;
            }

            Vec3 vec3 = VRPlayer.get().roomOrigin;

            if ((dh.climbTracker.isGrabbingLadder() || flag
                    || dh.swimTracker.isActive())
                    && (this.zza != 0.0F || this.isFallFlying() || abs(this.getDeltaMovement().x) > 0.01D
                    || abs(this.getDeltaMovement().z) > 0.01D)) {
                double d0 = vec3.x - this.getX();
                double d1 = vec3.z - this.getZ();
                double d2 = this.getX();
                double d3 = this.getZ();
                super.move(pType, pPos);

                if (dh.vrSettings.walkUpBlocks) {
                    this.setMaxUpStep(this.getBlockJumpFactor() == 1.0F ? 1.0F : 0.6F);
                } else {
                    this.setMaxUpStep(0.6F);
                    this.updateAutoJump((float) (this.getX() - d2), (float) (this.getZ() - d3));
                }

                double d4 = this.getY() + this.getRoomYOffsetFromPose();
                VRPlayer.get().setRoomOrigin(this.getX() + d0, d4, this.getZ() + d1, false);
            } else if (flag1) {
                super.move(pType, new Vec3(0.0D, pPos.y, 0.0D));
                VRPlayer.get().setRoomOrigin(VRPlayer.get().roomOrigin.x, this.getY() + this.getRoomYOffsetFromPose(),
                        VRPlayer.get().roomOrigin.z, false);
            } else {
                this.setOnGround(true);
            }
        } else {
            super.move(pType, pPos);
        }
        info.cancel();
    }

    @Override
    public double getRoomYOffsetFromPose() {
        double d0 = 0.0D;

        if (this.getPose() == Pose.FALL_FLYING || this.getPose() == Pose.SPIN_ATTACK
                || this.getPose() == Pose.SWIMMING && !dh.crawlTracker.crawlsteresis) {
            d0 = -1.2D;
        }

        return d0;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;sin(F)F"), method = "updateAutoJump")
    private float modifyAutoJumpSin(float original) {
        return vrRunning ? dh.vrPlayer.vrdata_world_pre.getBodyYaw() * ((float) PI / 180) : original;
    }

    @ModifyArg(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;cos(F)F"), method = "updateAutoJump")
    private float modifyAutoJumpCos(float original) {
        return vrRunning ? dh.vrPlayer.vrdata_world_pre.getBodyYaw() * ((float) PI / 180) : original;
    }

    @Override
    public ItemStack eat(Level level, ItemStack itemStack) {
        if (vrRunning && itemStack.isEdible() && ((LocalPlayer) (Object) this) == mc.player && itemStack.getHoverName().getString().equals("EAT ME")) {
            dh.vrPlayer.wfMode = 0.5D;
            dh.vrPlayer.wfCount = 400;
        }
        return super.eat(level, itemStack);
    }

    @Override
    public void moveTo(double x, double y, double z, float yRot, float xRot) {
        super.moveTo(x, y, z, yRot, xRot);
        if (!vrRunning) {
            return;
        }
        if (this.initFromServer) {
            dh.vrPlayer.snapRoomOriginToPlayerEntity((LocalPlayer) (Object) this, false, false);
        }
    }

    @Override
    public void absMoveTo(double x, double y, double z, float yRot, float xRot) {
        super.absMoveTo(x, y, z, yRot, xRot);
        if (!vrRunning) {
            return;
        }
        dh.vrPlayer.snapRoomOriginToPlayerEntity((LocalPlayer) (Object) this, false, false);
    }

    @Override
    public void setPos(double x, double y, double z) {
        this.initFromServer = true;
        if (!vrRunning) {
            super.setPos(x, y, z);
            return;
        }
        double d0 = this.getX();
        double d1 = this.getY();
        double d2 = this.getZ();
        super.setPos(x, y, z);
        double d3 = this.getX();
        double d4 = this.getY();
        double d5 = this.getZ();
        Entity entity = this.getVehicle();

        if (this.isPassenger()) {
            Vec3 vec3 = dh.vehicleTracker.Premount_Pos_Room;
            vec3 = vec3.yRot(dh.vrPlayer.vrdata_world_pre.rotation_radians);
            x = x - vec3.x;
            y = dh.vehicleTracker.getVehicleFloor(entity, y);
            z = z - vec3.z;
            dh.vrPlayer.setRoomOrigin(x, y, z, x + y + z == 0.0D);
        } else {
            Vec3 vec31 = dh.vrPlayer.roomOrigin;
            VRPlayer.get().setRoomOrigin(vec31.x + (d3 - d0), vec31.y + (d4 - d1), vec31.z + (d5 - d2),
                    x + y + z == 0.0D);
        }
    }

    public void doDrag() {
        float friction = 0.91F;

        if (this.onGround()) {
            friction = this.level().getBlockState(this.getBlockPosBelowThatAffectsMyMovement()).getBlock().getFriction() * 0.91F;
        }
        double xFactor = friction;
        double zFactor = friction;
        // account for stock drag code we can't change in LivingEntity#travel
        this.setDeltaMovement(this.getDeltaMovement().x / xFactor, this.getDeltaMovement().y, this.getDeltaMovement().z / zFactor);

        double addFactor = dh.vrSettings.inertiaFactor.getFactor();

        double boundedAdditionX = this.getBoundedAddition(this.additionX);
        double targetLimitX = (friction * boundedAdditionX) / (1.0f - friction);
        double multiFactorX = targetLimitX / (friction * (targetLimitX + (boundedAdditionX * addFactor)));
        xFactor *= multiFactorX;

        double boundedAdditionZ = this.getBoundedAddition(this.additionZ);
        double targetLimitZ = (friction * boundedAdditionZ) / (1.0f - friction);
        double multiFactorZ = targetLimitZ / (friction * (targetLimitZ + (boundedAdditionZ * addFactor)));
        zFactor *= multiFactorZ;

        this.setDeltaMovement(this.getDeltaMovement().x * xFactor, this.getDeltaMovement().y, this.getDeltaMovement().z * zFactor);
    }

    public double getBoundedAddition(double orig) {
        return orig >= -1.0E-6D && orig <= 1.0E-6D ? 1.0E-6D : orig;
    }

    @Override
    public void moveRelative(float pAmount, Vec3 pRelative) {
        if (!vrRunning) {
            super.moveRelative(pAmount, pRelative);
            return;
        }

        double d0 = pRelative.y;
        double d1 = pRelative.x;
        double d2 = pRelative.z;
        VRPlayer vrplayer = dh.vrPlayer;

        if (vrplayer.getFreeMove()) {
            double d3 = d1 * d1 + d2 * d2;
            double d4 = 0.0D;
            double d5 = 0.0D;
            double d6 = 0.0D;
            double d7 = 1.0D;

            if (d3 >= (double) 1.0E-4F || dh.katvr) {
                d3 = (double) sqrt((float) d3);

                if (d3 < 1.0D && !dh.katvr) {
                    d3 = 1.0D;
                }

                d3 = (double) pAmount / d3;
                d1 = d1 * d3;
                d2 = d2 * d3;
                Vec3 vec3 = new Vec3(d1, 0.0D, d2);
                VRPlayer vrplayer1 = dh.vrPlayer;
                boolean isFlyingOrSwimming = !this.isPassenger() && (this.getAbilities().flying || this.isSwimming());

                if (dh.katvr) {
                    jkatvr.query();
                    d3 = (double) (jkatvr.getSpeed() * jkatvr.walkDirection() * dh.vrSettings.movementSpeedMultiplier);
                    vec3 = new Vec3(0.0D, 0.0D, d3);

                    if (isFlyingOrSwimming) {
                        vec3 = vec3.xRot(vrplayer1.vrdata_world_pre.hmd.getPitch() * ((float) PI / 180F));
                    }

                    vec3 = vec3.yRot(-jkatvr.getYaw() * ((float) PI / 180F) + dh.vrPlayer.vrdata_world_pre.rotation_radians);
                } else if (dh.infinadeck) {
                    jinfinadeck.query();
                    d3 = (double) (jinfinadeck.getSpeed() * jinfinadeck.walkDirection() * dh.vrSettings.movementSpeedMultiplier);
                    vec3 = new Vec3(0.0D, 0.0D, d3);

                    if (isFlyingOrSwimming) {
                        vec3 = vec3.xRot(vrplayer1.vrdata_world_pre.hmd.getPitch() * ((float) PI / 180F));
                    }

                    vec3 = vec3.yRot(-jinfinadeck.getYaw() * ((float) PI / 180F) + dh.vrPlayer.vrdata_world_pre.rotation_radians);
                } else if (dh.vrSettings.seated) {
                    int j = 0;
                    if (dh.vrSettings.seatedUseHMD) {
                        j = 1;
                    }

                    if (isFlyingOrSwimming) {
                        vec3 = vec3.xRot(vrplayer1.vrdata_world_pre.getController(j).getPitch() * ((float) PI / 180F));
                    }

                    vec3 = vec3.yRot(-vrplayer1.vrdata_world_pre.getController(j).getYaw() * ((float) PI / 180F));
                } else {

                    VRSettings.FreeMove freeMoveType = !this.isPassenger() && this.getAbilities().flying && dh.vrSettings.vrFreeMoveFlyMode != VRSettings.FreeMove.AUTO ? dh.vrSettings.vrFreeMoveFlyMode : dh.vrSettings.vrFreeMoveMode;

                    if (isFlyingOrSwimming) {
                        switch (freeMoveType) {
                            case CONTROLLER:
                                vec3 = vec3.xRot(vrplayer1.vrdata_world_pre.getController(1).getPitch() * ((float) PI / 180F));
                                break;
                            case HMD:
                            case RUN_IN_PLACE:
                            case ROOM:
                                vec3 = vec3.xRot(vrplayer1.vrdata_world_pre.hmd.getPitch() * ((float) PI / 180F));
                        }
                    }
                    if (dh.jumpTracker.isjumping()) {
                        vec3 = vec3.yRot(-vrplayer1.vrdata_world_pre.hmd.getYaw() * ((float) PI / 180F));
                    } else {
                        switch (freeMoveType) {
                            case CONTROLLER:
                                vec3 = vec3.yRot(-vrplayer1.vrdata_world_pre.getController(1).getYaw() * ((float) PI / 180F));
                                break;

                            case HMD:
                                vec3 = vec3.yRot(-vrplayer1.vrdata_world_pre.hmd.getYaw() * ((float) PI / 180F));
                                break;

                            case RUN_IN_PLACE:
                                vec3 = vec3.yRot((float) (-dh.runTracker.getYaw() * (double) ((float) PI / 180F)));
                                vec3 = vec3.scale(dh.runTracker.getSpeed());

                            case ROOM:
                                vec3 = vec3.yRot((180.0F + dh.vrSettings.worldRotation) * ((float) PI / 180F));
                        }
                    }
                }

                d4 = vec3.x;
                d6 = vec3.y;
                d5 = vec3.z;

                if (!this.getAbilities().flying && !this.wasTouchingWater) {
                    d7 = dh.vrSettings.inertiaFactor.getFactor();
                }

                float f = 1.0F;

                if (this.getAbilities().flying) {
                    f = 5.0F;
                }

                this.setDeltaMovement(this.getDeltaMovement().x + d4 * d7, this.getDeltaMovement().y + d6 * (double) f, this.getDeltaMovement().z + d5 * d7);
                this.additionX = d4;
                this.additionZ = d5;
            }

            if (!this.getAbilities().flying && !this.wasTouchingWater) {
                this.doDrag();
            }
        }
    }

    @Override
    public void die(DamageSource pCause) {
        super.die(pCause);
        if (!vrRunning) {
            return;
        }
        dh.vr.triggerHapticPulse(0, 2000);
        dh.vr.triggerHapticPulse(1, 2000);
    }

    @Override
    public boolean getInitFromServer() {
        return this.initFromServer;
    }

    @Override
    public void setMovementTeleportTimer(int value) {
        this.movementTeleportTimer = value;
    }

    @Override
    public int getMovementTeleportTimer() {
        return this.movementTeleportTimer;
    }

    @Override
    public float getMuhSpeedFactor() {
        return this.moveMulIn.lengthSqr() > 0.0D ? (float) ((double) this.getBlockSpeedFactor() * (this.moveMulIn.x + this.moveMulIn.z) / 2.0D) : this.getBlockSpeedFactor();
    }

    @Override
    public float getMuhJumpFactor() {
        return this.moveMulIn.lengthSqr() > 0.0D ? (float) ((double) this.getBlockJumpFactor() * this.moveMulIn.y) : this.getBlockJumpFactor();
    }

    @Override
    public void stepSound(BlockPos blockforNoise, double soundPosX, double soundPosY, double soundPosZ) {
        BlockState bs = this.level().getBlockState(blockforNoise);
        Block block = bs.getBlock();
        SoundType soundtype = block.getSoundType(bs);
        BlockState bsup = this.level().getBlockState(blockforNoise.above());

        if (bsup.getBlock() == Blocks.SNOW) {
            soundtype = Blocks.SNOW.getSoundType(bsup);
        }

        float volume = soundtype.getVolume();
        float pitch = soundtype.getPitch();
        SoundEvent soundIn = soundtype.getStepSound();

        // TODO: liquid is deprecated
        if (!this.isSilent() && !block.defaultBlockState().liquid()) {
            this.level().playSound(null, soundPosX, soundPosY, soundPosZ, soundIn, this.getSoundSource(), volume, pitch);
        }
    }

    @Override
    public boolean isClimbeyJumpEquipped() {
        return this.getItemBySlot(EquipmentSlot.FEET) != null && dh.jumpTracker.isBoots(this.getItemBySlot(EquipmentSlot.FEET));
    }

    @Override
    public boolean isClimbeyClimbEquipped() {
        if (this.getMainHandItem() != null && dh.climbTracker.isClaws(this.getMainHandItem())) {
            return true;
        } else {
            return this.getOffhandItem() != null && dh.climbTracker.isClaws(this.getOffhandItem());
        }
    }

    @Override
    public void releaseUsingItem() {
        ClientNetworking.sendActiveHand((byte) this.getUsedItemHand().ordinal());
        super.releaseUsingItem();
    }
    @Override
    public void setItemInUseClient(ItemStack item, InteractionHand hand) {
        this.useItem = item;
        this.startedUsingItem = item != ItemStack.EMPTY;
        this.usingItemHand = hand;
    }

    @Override
    public void setTeleported(boolean teleported) {
        this.teleported = teleported;
    }

    @Override
    public void setItemInUseCountClient(int count) {
        this.useItemRemaining = count;
    }
}
