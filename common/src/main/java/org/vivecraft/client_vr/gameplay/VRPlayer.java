package org.vivecraft.client_vr.gameplay;

import net.minecraft.client.particle.TerrainParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.Xplat;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ItemTags;
import org.vivecraft.client_vr.VRData;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.KeyboardHandler;
import org.vivecraft.client_vr.gameplay.screenhandlers.RadialHandler;
import org.vivecraft.client_vr.gameplay.trackers.Tracker;
import org.vivecraft.client_vr.gameplay.trackers.Tracker.EntryPoint;
import org.vivecraft.client_vr.gameplay.trackers.VehicleTracker;
import org.vivecraft.client_vr.settings.VRSettings.FreeMove;
import org.vivecraft.client_vr.settings.VRSettings.VrOptions;
import org.vivecraft.common.VRServerPerms;
import org.vivecraft.mod_compat_vr.pehkui.PehkuiHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Random;

import static org.joml.Math.*;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.*;

public class VRPlayer {
    public VRData vrdata_room_pre = new VRData();
    public VRData vrdata_world_pre = new VRData();
    public VRData vrdata_room_post = new VRData();
    public VRData vrdata_world_post = new VRData();
    @Nullable
    public VRData vrdata_world_render;
    ArrayList<Tracker> trackers = new ArrayList<>();
    public float worldScale = dh.vrSettings.overrides.getSetting(VrOptions.WORLD_SCALE).getFloat();
    private float rawWorldScale = dh.vrSettings.overrides.getSetting(VrOptions.WORLD_SCALE).getFloat();
    private boolean teleportOverride = false;
    public boolean teleportWarning = false;
    public boolean vrSwitchWarning = false;
    public int chatWarningTimer = -1;
    public Vector3f roomOrigin = new Vector3f();
    private boolean isFreeMoveCurrent = true;
    public double wfMode = 0.0D;
    public int wfCount = 0;
    public int roomScaleMovementDelay = 0;
    boolean initdone = false;
    public boolean onTick;

    public void registerTracker(Tracker tracker) {
        this.trackers.add(tracker);
    }

    @Nonnull
    public VRData getVRDataWorld() {
        return this.vrdata_world_render != null ? this.vrdata_world_render : this.vrdata_world_pre;
    }

    public static Vector3f room_to_world_pos(Vector3fc pos, VRData data, Vector3f dest) {
        return pos.mul(data.worldScale, dest).rotateY(data.rotation_radians).add(data.origin);
    }

    public static Vector3f world_to_room_pos(Vector3fc pos, VRData data, Vector3f dest) {
        return pos.sub(data.origin, dest).div(data.worldScale).rotateY(-data.rotation_radians);
    }

    public void postPoll() {
        this.vrdata_room_pre = new VRData();
        GuiHandler.processGui();
        KeyboardHandler.processGui();
        RadialHandler.processGui();
    }

    public void preTick() {
        this.onTick = true;
        this.vrdata_world_pre = new VRData(this.roomOrigin, this.worldScale, toRadians(dh.vrSettings.worldRotation));
        float f = dh.vrSettings.overrides.getSetting(VrOptions.WORLD_SCALE).getFloat();

        if (((GameRendererExtension) mc.gameRenderer).vivecraft$isInMenuRoom()) {
            this.worldScale = 1.0F;
        } else {
            if (this.wfCount > 0 && !mc.isPaused()) {
                if (this.wfCount < 40) {
                    this.rawWorldScale = (float) (this.rawWorldScale - this.wfMode);

                    if (this.wfMode > 0.0D) {
                        if (this.rawWorldScale < f) {
                            this.rawWorldScale = f;
                        }
                    } else if (this.wfMode < 0.0D && this.rawWorldScale > f) {
                        this.rawWorldScale = f;
                    }
                } else {
                    this.rawWorldScale = (float) (this.rawWorldScale + this.wfMode);

                    if (this.wfMode > 0.0D) {
                        if (this.rawWorldScale > 20.0F) {
                            this.rawWorldScale = 20.0F;
                        }
                    } else if (this.wfMode < 0.0D && this.rawWorldScale < 0.1F) {
                        this.rawWorldScale = 0.1F;
                    }
                }

                --this.wfCount;
            } else {
                this.rawWorldScale = f;
            }

            this.worldScale = rawWorldScale;

            if (Xplat.isModLoaded("pehkui")) {
                // scale world with player size
                this.worldScale *= PehkuiHelper.getPlayerScale(mc.player, mc.getFrameTime());
                // limit scale
                if (this.worldScale > 100.0F) {
                    this.worldScale = 100.0F;
                } else if (this.worldScale < 0.025F) //minClip + player position indicator offset
                {
                    this.worldScale = 0.025F;
                }
            }
        }

        if (dh.vrSettings.seated && !(((GameRendererExtension) mc.gameRenderer).vivecraft$isInMenuRoom())) {
            dh.vrSettings.worldRotation = dh.vr.seatedRot;
        }
    }

    public void postTick() {
        VRData vrdata = new VRData(this.vrdata_world_pre.origin, this.vrdata_world_pre.worldScale, this.vrdata_world_pre.rotation_radians);
        VRData vrdata1 = new VRData(this.vrdata_world_pre.origin, this.worldScale, this.vrdata_world_pre.rotation_radians);
        this.roomOrigin.sub(vrdata1.hmd.getPosition(new Vector3f()).sub(vrdata.hmd.getPosition(new Vector3f())));
        VRData vrdata2 = new VRData(this.roomOrigin, this.worldScale, this.vrdata_world_pre.rotation_radians);
        float rad = this.vrdata_world_pre.rotation_radians - toRadians(dh.vrSettings.worldRotation);
        if (rad != 0.0F) {
            Vector3f o = vrdata2.getHeadPivot(new Vector3f());
            this.setRoomOrigin(
                cos(rad) * (this.roomOrigin.x - o.x) - sin(rad) * (this.roomOrigin.z - o.z) + o.x,
                this.roomOrigin.y,
                sin(rad) * (this.roomOrigin.x - o.x) + cos(rad) * (this.roomOrigin.z - o.z) + o.z,
                false
            );
        }
        this.vrdata_room_post = new VRData();
        this.vrdata_world_post = new VRData(this.roomOrigin, this.worldScale, toRadians(dh.vrSettings.worldRotation));
        this.doPermanentLookOverride(mc.player, this.vrdata_world_post);
        ClientNetworking.sendVRPlayerPositions(this);
        this.onTick = false;
    }

    public void preRender(float par1) {
        float f = this.vrdata_world_post.worldScale * par1 + this.vrdata_world_pre.worldScale * (1.0F - par1);
        float f1 = this.vrdata_world_post.rotation_radians;
        float f2 = this.vrdata_world_pre.rotation_radians;
        float f3 = abs(f1 - f2);

        if (f3 > (float) PI) {
            if (f1 > f2) {
                f2 += (float) PI * 2F;
            } else {
                f1 += (float) PI * 2F;
            }
        }

        float f4 = f1 * par1 + f2 * (1.0F - par1);
        this.vrdata_world_render = new VRData(
            this.vrdata_world_post.origin.sub(this.vrdata_world_pre.origin, new Vector3f())
                .mulAdd(par1, this.vrdata_world_pre.origin),
            f,
            f4
        );

        for (Tracker tracker : this.trackers) {
            if (tracker.getEntryPoint() == EntryPoint.SPECIAL_ITEMS) {
                tracker.idleTick();

                if (tracker.isActive()) {
                    tracker.doProcess();
                } else {
                    tracker.reset();
                }
            }
        }
    }

    public void postRender(float par1) {
    }

    public void setRoomOrigin(double x, double y, double z, boolean reset) {
        if (reset && this.vrdata_world_pre != null) {
            this.vrdata_world_pre.origin.set(x, y, z);
        }

        this.roomOrigin.set(x, y, z);
    }

    public void snapRoomOriginToPlayerEntity(boolean reset, boolean instant) {
        if (!"Server thread".equals(Thread.currentThread().getName())) {
            if (mc.player != null && mc.player.position() != Vec3.ZERO) {
                if (dh.sneakTracker.sneakCounter <= 0) {
                    VRData vrdata = (
                        instant ?
                        new VRData(this.roomOrigin, this.worldScale, toRadians(dh.vrSettings.worldRotation)) :
                        this.vrdata_world_pre
                    );
                    Vector3f vec3 = vrdata.getHeadPivot(new Vector3f()).sub(vrdata.origin.x(), vrdata.origin.y(), vrdata.origin.z());
                    this.setRoomOrigin(
                        mc.player.getX() - vec3.x,
                        mc.player.getY() + ((PlayerExtension) mc.player).vivecraft$getRoomYOffsetFromPose(),
                        mc.player.getZ() - vec3.z,
                        reset
                    );
                }
            }
        }
    }

    public void tick(LocalPlayer player) {
        if (((PlayerExtension) player).vivecraft$getInitFromServer()) {
            if (!this.initdone) {
                logger.info(
                    """
                        {}
                            Hmd Projection Left:
                        {}
                            Hmd Projection Right:
                        {}
                        """,
                    this,
                    dh.vrRenderer.eyeproj[0],
                    dh.vrRenderer.eyeproj[1]
                );
                this.initdone = true;
            }

            this.doPlayerMoveInRoom(player);

            for (Tracker tracker : this.trackers) {
                if (tracker.getEntryPoint() == EntryPoint.LIVING_UPDATE) {
                    tracker.idleTick();

                    if (tracker.isActive()) {
                        tracker.doProcess();
                    } else {
                        tracker.reset();
                    }
                }
            }

            if (player.isPassenger()) {
                Entity entity = mc.player.getVehicle();

                if (entity instanceof AbstractHorse abstracthorse) {

                    if (abstracthorse.isControlledByLocalInstance() && abstracthorse.isSaddled() && !dh.horseTracker.isActive()) {
                        abstracthorse.yBodyRot = this.vrdata_world_pre.getBodyYaw();
                        dh.vehicleTracker.rotationCooldown = 10;
                    }
                } else if (entity instanceof Mob mob) {

                    if (mob.isControlledByLocalInstance()) {
                        mob.yBodyRot = this.vrdata_world_pre.getBodyYaw();
                        dh.vehicleTracker.rotationCooldown = 10;
                    }
                }
            }
        }
    }

    public void doPlayerMoveInRoom(LocalPlayer player) {
        if (this.roomScaleMovementDelay > 0) {
            --this.roomScaleMovementDelay;
        } else {

            if (player != null && !player.isShiftKeyDown() && !player.isSleeping() && !dh.jumpTracker.isjumping() &&
                !dh.climbTracker.isGrabbingLadder() && player.isAlive()
            ) {
                VRData vrdata = new VRData(
                    this.roomOrigin,
                    this.worldScale,
                    this.vrdata_world_pre.rotation_radians
                );

                if (dh.vehicleTracker.canRoomscaleDismount()) {
                    Vec3 vec35 = mc.player.getVehicle().position();
                    Vector3f vec36 = vrdata.getHeadPivot(new Vector3f());
                    double d6 = sqrt(
                        (vec36.x - vec35.x) * (vec36.x - vec35.x) + (vec36.z - vec35.z) * (vec36.z - vec35.z));

                    if (d6 > 1.0D) {
                        dh.sneakTracker.sneakCounter = 5;
                    }
                } else {
                    float f = player.getBbWidth() / 2.0F;
                    float f1 = player.getBbHeight();
                    Vector3f vec3 = vrdata.getHeadPivot(new Vector3f());
                    double d0 = vec3.x;
                    double d1 = player.getY();
                    double d2 = vec3.z;
                    AABB aabb = new AABB(
                        d0 - (double) f,
                        d1,
                        d2 - (double) f,
                        d0 + (double) f,
                        d1 + (double) f1,
                        d2 + (double) f
                    );
                    Vec3 vec31 = null;
                    float f2 = 0.0625F;
                    boolean flag = mc.level.noCollision(player, aabb);

                    if (flag) {
                        player.setPosRaw(d0, !dh.vrSettings.simulateFalling ? d1 : player.getY(), d2);
                        player.setBoundingBox(new AABB(
                            aabb.minX,
                            aabb.minY,
                            aabb.minZ,
                            aabb.maxX,
                            aabb.minY + (double) f1,
                            aabb.maxZ
                        ));
                        player.fallDistance = 0.0F;
                        this.getEstimatedTorsoPosition(d0, d1, d2);
                    } else if (
                        (dh.vrSettings.walkUpBlocks && ((PlayerExtension) player).vivecraft$getMuhJumpFactor() == 1.0F ||
                            dh.climbTracker.isGrabbingLadder() && dh.vrSettings.realisticClimbEnabled
                        ) && player.fallDistance == 0.0F) {
                        if (vec31 == null) {
                            vec31 = this.getEstimatedTorsoPosition(d0, d1, d2);
                        }

                        float f3 = player.getDimensions(player.getPose()).width * 0.45F;
                        double d3 = f - f3;
                        AABB aabb1 = new AABB(
                            vec31.x - d3,
                            aabb.minY,
                            vec31.z - d3,
                            vec31.x + d3,
                            aabb.maxY,
                            vec31.z + d3
                        );
                        boolean flag1 = !mc.level.noCollision(player, aabb1);

                        if (flag1) {
                            double d4 = vec31.x - d0;
                            double d5 = vec31.z - d2;
                            aabb = aabb.move(d4, 0.0D, d5);
                            int i = 0;

                            if (player.onClimbable() && dh.vrSettings.realisticClimbEnabled) {
                                i = 6;
                            }

                            for (int j = 0; j <= 10 + i; ++j) {
                                aabb = aabb.move(0.0D, 0.1D, 0.0D);
                                flag = mc.level.noCollision(player, aabb);

                                if (flag) {
                                    d0 = d0 + d4;
                                    d2 = d2 + d5;
                                    d1 = d1 + (double) (0.1F * j);
                                    player.setPosRaw(d0, d1, d2);
                                    player.setBoundingBox(new AABB(
                                        aabb.minX,
                                        aabb.minY,
                                        aabb.minZ,
                                        aabb.maxX,
                                        aabb.maxY,
                                        aabb.maxZ
                                    ));
                                    Vec3 vec32 = new Vec3(this.roomOrigin.x + d4, this.roomOrigin.y + 0.1F * j, this.roomOrigin.z + d5);
                                    this.setRoomOrigin(vec32.x, vec32.y, vec32.z, false);
                                    Vec3 vec33 = player.getLookAngle();
                                    Vec3 vec34 = (new Vec3(vec33.x, 0.0D, vec33.z)).normalize();
                                    player.fallDistance = 0.0F;
                                    Vec3 pos = player.position();
                                    ((PlayerExtension) mc.player).vivecraft$stepSound(
                                        BlockPos.containing(pos),
                                        pos.x(),
                                        pos.y(),
                                        pos.z()
                                    );
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public Vec3 getEstimatedTorsoPosition(double x, double y, double z) {
        Entity entity = mc.player;
        Vec3 vec3 = entity.getLookAngle();
        Vec3 vec31 = (new Vec3(vec3.x, 0.0D, vec3.z)).normalize();
        float f = (float) vec3.y * 0.25F;
        return new Vec3(x + vec31.x * (double) f, y + vec31.y * (double) f, z + vec31.z * (double) f);
    }

    public void blockDust(double x, double y, double z, int count, BlockPos bp, BlockState bs, float scale, float velscale) {
        new Random();

        for (int i = 0; i < count; ++i) {
            TerrainParticle terrainparticle = new TerrainParticle(mc.level, x, y, z, 0.0D, 0.0D, 0.0D, bs);
            terrainparticle.setPower(velscale);
            //TODO: check
            // minecraft.particleEngine.add(terrainparticle.init(bp).scale(scale));
            mc.particleEngine.add(terrainparticle.scale(scale));
        }
    }

    public void updateFreeMove() {
        if (dh.teleportTracker.isAiming()) {
            this.isFreeMoveCurrent = false;
        }

        if (mc.player.input.forwardImpulse != 0.0F || mc.player.input.leftImpulse != 0.0F) {
            this.isFreeMoveCurrent = true;
        }

        this.updateTeleportKeys();
    }

    public boolean getFreeMove() {
        if (dh.vrSettings.seated) {
            return dh.vrSettings.seatedFreeMove || !this.isTeleportEnabled();
        } else {
            return this.isFreeMoveCurrent || dh.vrSettings.forceStandingFreeMove;
        }
    }

    public String toString() {
        return "VRPlayer: \r\n \t origin: " + this.roomOrigin + "\r\n \t rotation: " + String.format("%.3f", dh.vrSettings.worldRotation) + "\r\n \t scale: " + String.format("%.3f", this.worldScale) + "\r\n \t room_pre " + this.vrdata_room_pre + "\r\n \t world_pre " + this.vrdata_world_pre + "\r\n \t world_post " + this.vrdata_world_post + "\r\n \t world_render " + this.vrdata_world_render;
    }

    public Vector3f getRightClickLookOverride(Player entity, int c) {
        Vector3f vec3 = new Vector3f();

        if (((GameRendererExtension) mc.gameRenderer).vivecraft$getCrossVec(vec3) != null) {
            convertToVector3f(entity.getEyePosition(1.0F).subtract(vec3.x(), vec3.y(), vec3.z()).normalize().reverse(), vec3);
        } else {
            convertToVector3f(entity.getLookAngle(), vec3);
        }

        ItemStack itemstack;
        label54:
        {
            itemstack = c == 0 ? entity.getMainHandItem() : entity.getOffhandItem();

            if (!(itemstack.getItem() instanceof SnowballItem) && !(itemstack.getItem() instanceof EggItem) && !(itemstack.getItem() instanceof SpawnEggItem) && !(itemstack.getItem() instanceof PotionItem) && !(itemstack.getItem() instanceof BowItem) && !itemstack.is(ItemTags.VIVECRAFT_THROW_ITEMS)) {
                if (!(itemstack.getItem() instanceof CrossbowItem crossbowitem)) {
                    break label54;
                }

                if (!CrossbowItem.isCharged(itemstack)) {
                    break label54;
                }
            }

            VRData vrdata = dh.vrPlayer.vrdata_world_pre;
            vrdata.getController(c).getDirection(vec3);
            Vector3fc vec31 = dh.bowTracker.getAimVector();

            if (dh.bowTracker.isNotched() && vec31 != null && vec31.lengthSquared() > 0.0F) {
                vec31.negate(vec3);
            }

            return vec3;
        }

        if (itemstack.getItem() == Items.BUCKET && dh.interactTracker.bukkit[c]) {
            vec3 = convertToVector3f(entity.getEyePosition(1.0F), new Vector3f()).sub(dh.vrPlayer.vrdata_world_pre.getController(c).getPosition(new Vector3f())).normalize().negate();
        }

        return vec3;
    }

    public void doPermanentLookOverride(LocalPlayer entity, VRData data) {
        if (entity == null) {
            return;
        }
        Vector3f vector3f = new Vector3f(); // TODO: 😈

        if (entity.isPassenger()) {
            //Server-side movement
            Vector3f vec3 = VehicleTracker.getSteeringDirection(entity, new Vector3f());

            if (vec3 != null) {
                entity.setXRot((float) toDegrees(asin(-vec3.y / vec3.length())));
                entity.setYRot((float) toDegrees(atan2(-vec3.x, vec3.z)));
                entity.setYHeadRot(entity.getYRot());
            }
        } else if (entity.isBlocking()) {
            //block direction
            if (entity.getUsedItemHand() == InteractionHand.MAIN_HAND) {
                entity.setYRot(data.getController(0).getYaw());
                entity.setYHeadRot(entity.getYRot());
                entity.setXRot(-data.getController(0).getPitch());
            } else {
                entity.setYRot(data.getController(1).getYaw());
                entity.setYHeadRot(entity.getYRot());
                entity.setXRot(-data.getController(1).getPitch());
            }
        } else if (entity.isSprinting() && (entity.input.jumping || mc.options.keyJump.isDown()) || entity.isFallFlying() || entity.isSwimming() && entity.zza > 0.0F) {
            //Server-side movement
            FreeMove freeMoveType = entity.isFallFlying() && dh.vrSettings.vrFreeMoveFlyMode != FreeMove.AUTO ? dh.vrSettings.vrFreeMoveFlyMode : dh.vrSettings.vrFreeMoveMode;

            if (freeMoveType == FreeMove.CONTROLLER) {
                entity.setYRot(data.getController(1).getYaw());
                entity.setYHeadRot(entity.getYRot());
                entity.setXRot(-data.getController(1).getPitch());
            } else {
                entity.setYRot(data.hmd.getYaw());
                entity.setYHeadRot(entity.getYRot());
                entity.setXRot(-data.hmd.getPitch());
            }
        } else if (((GameRendererExtension) mc.gameRenderer).vivecraft$getCrossVec(vector3f) != null) {
            //Look AT the crosshair by default, most compatible with mods.
            Vector3f playerToCrosshair = convertToVector3f(entity.getEyePosition(1), new Vector3f()).sub(vector3f); //backwards
            double what = playerToCrosshair.y / playerToCrosshair.length();
            if (what > 1) {
                what = 1;
            }
            if (what < -1) {
                what = -1;
            }
            float pitch = (float) toDegrees(asin(what));
            float yaw = (float) toDegrees(atan2(playerToCrosshair.x, -playerToCrosshair.z));
            entity.setXRot(pitch);
            entity.setYRot(yaw);
            entity.setYHeadRot(yaw);
        } else {
            //use HMD only if no crosshair hit.
            entity.setYRot(data.hmd.getYaw());
            entity.setYHeadRot(entity.getYRot());
            entity.setXRot(-data.hmd.getPitch());
        }
    }

    public Vector3f AimedPointAtDistance(VRData source, int controller, float distance, Vector3f dest) {
        return source.getController(controller).getPosition(dest)
            .add(source.getController(controller).getDirection(new Vector3f()).mul(distance));
    }

    public HitResult rayTraceBlocksVR(VRData source, int controller, float blockReachDistance, boolean fluid) {
        return mc.level.clip(new ClipContext(
            convertToVec3(source.getController(controller).getPosition(new Vector3f())),
            convertToVec3(this.AimedPointAtDistance(source, controller, blockReachDistance, new Vector3f())),
            Block.OUTLINE,
            fluid ? Fluid.ANY : Fluid.NONE,
            mc.player
        ));
    }

    public boolean isTeleportSupported() {
        return !VRServerPerms.noTeleportClient;
    }

    public boolean isTeleportOverridden() {
        return this.teleportOverride;
    }

    public boolean isTeleportEnabled() {
        boolean flag = !VRServerPerms.noTeleportClient || this.teleportOverride;

        if (dh.vrSettings.seated) {
            return flag;
        } else {
            return flag && !dh.vrSettings.forceStandingFreeMove;
        }
    }

    public void setTeleportOverride(boolean override) {
        this.teleportOverride = override;
        this.updateTeleportKeys();
    }

    public void updateTeleportKeys() {
        dh.vr.getInputAction(VivecraftVRMod.keyTeleport).setEnabled(this.isTeleportEnabled());
        dh.vr.getInputAction(VivecraftVRMod.keyTeleportFallback).setEnabled(!this.isTeleportEnabled());
    }
}
