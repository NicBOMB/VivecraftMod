package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.BlockTags;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client_vr.gameplay.VRMovementStyle;
import org.vivecraft.client_vr.render.helpers.RenderHelper;

import java.util.Random;

import static org.joml.Math.*;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.client_vr.VRState.mc;
import static org.vivecraft.common.utils.Utils.*;

public class TeleportTracker extends Tracker {
    private float teleportEnergy;
    private final Vector3f movementTeleportDestination = new Vector3f();
    private Direction movementTeleportDestinationSideHit;
    public double movementTeleportProgress;
    public double movementTeleportDistance;
    private Vec3[] movementTeleportArc = new Vec3[50];
    public int movementTeleportArcSteps = 0;
    public float lastTeleportArcDisplayOffset = 0.0F;
    public final VRMovementStyle vrMovementStyle = new VRMovementStyle();

    public float getTeleportEnergy() {
        return this.teleportEnergy;
    }

    public boolean isAiming() {
        return this.movementTeleportProgress > 0.0D;
    }

    public Vector3fc getDestination() {
        return this.movementTeleportDestination;
    }

    @Override
    public boolean isActive() {
        if (mc.player == null) {
            return false;
        } else if (mc.gameMode == null) {
            return false;
        } else if (!mc.player.isAlive()) {
            return false;
        } else {
            return !mc.player.isSleeping();
        }
    }

    @Override
    public void reset() {
        this.movementTeleportDestination.zero();
        this.movementTeleportArcSteps = 0;
        this.movementTeleportProgress = 0.0D;
    }

    @Override
    public void doProcess() {
        Random random = new Random();

        if (this.teleportEnergy < 100.0F) {
            ++this.teleportEnergy;
        }

        boolean flag = false;
        Vector3f vec3 = null;
        boolean flag1 = VivecraftVRMod.keyTeleport.isDown() && dh.vrPlayer.isTeleportEnabled();
        boolean flag2 = dh.vrSettings.seated && !dh.vrPlayer.getFreeMove() && (mc.player.input.forwardImpulse != 0.0F || mc.player.input.leftImpulse != 0.0F);

        if ((flag1 || flag2) && !mc.player.isPassenger()) {
            vec3 = this.movementTeleportDestination;

            if (this.vrMovementStyle.teleportOnRelease) {
                if (((PlayerExtension) mc.player).vivecraft$getMovementTeleportTimer() == 0) {
                    String playCustomTeleportSound = this.vrMovementStyle.startTeleportingSound;
                }

                ((PlayerExtension) mc.player).vivecraft$setMovementTeleportTimer(((PlayerExtension) mc.player).vivecraft$getMovementTeleportTimer() + 1);

                if (((PlayerExtension) mc.player).vivecraft$getMovementTeleportTimer() > 0) {
                    this.movementTeleportProgress = (float) ((PlayerExtension) mc.player).vivecraft$getMovementTeleportTimer();

                    if (this.movementTeleportProgress >= 1.0D) {
                        this.movementTeleportProgress = 1.0D;
                    }

                    if (vec3.x != 0.0F || vec3.y != 0.0F || vec3.z != 0.0F) {
                        Vector3f vec38 = dh.vrPlayer.vrdata_world_pre.hmd.getPosition(new Vector3f());
                        Vector3f vec31 = vec3.add(-vec38.x, -vec38.y, -vec38.z, new Vector3f()).normalize();
                        Vec3 vec32 = mc.player.getLookAngle();
                        Vec3 vec33 = vec32.cross(new Vec3(0.0D, 1.0D, 0.0D));
                        Vec3 vec34 = vec33.cross(vec32);

                        if (this.vrMovementStyle.airSparkles) {
                            for (int i = 0; i < 3; ++i) {
                                double d0 = random.nextDouble() + 3.5D;
                                double d1 = random.nextDouble() * 2.5D;
                                double d2 = random.nextDouble() * 4.0D - 2.0D;
                                Vec3 vec36 = new Vec3(vec38.x + vec32.x * d0, vec38.y + vec32.y * d0, vec38.z + vec32.z * d0);
                                vec36 = vec36.add(vec33.x * d2, vec33.y * d2, vec33.z * d2);
                                vec36.add(vec34.x * d1, vec34.y * d1, vec34.z * d1);
                                double d3 = -0.6D;
                            }
                        }
                    }
                }
            } else if (((PlayerExtension) mc.player).vivecraft$getMovementTeleportTimer() >= 0 && (vec3.x != 0.0F || vec3.y != 0.0F || vec3.z != 0.0F)) {
                if (((PlayerExtension) mc.player).vivecraft$getMovementTeleportTimer() == 0) {
                }

                ((PlayerExtension) mc.player).vivecraft$setMovementTeleportTimer(((PlayerExtension) mc.player).vivecraft$getMovementTeleportTimer() + 1);
                Vec3 vec39 = mc.player.position();
                float d6 = vec3.distance((float) vec39.x, (float) vec39.y, (float) vec39.z);
                double d7 = (double) ((PlayerExtension) mc.player).vivecraft$getMovementTeleportTimer() / (d6 + 3.0D);

                if (((PlayerExtension) mc.player).vivecraft$getMovementTeleportTimer() > 0) {
                    this.movementTeleportProgress = d7;

                    if (this.vrMovementStyle.destinationSparkles) {
                    }

                    Vector3f vec310 = vec3.add((float) -mc.player.getX(), (float) -mc.player.getY(), (float) -mc.player.getZ(), new Vector3f()).normalize();
                    Vec3 vec311 = mc.player.getLookAngle();
                    Vec3 vec35 = vec311.cross(new Vec3(0.0D, 1.0D, 0.0D));
                    Vec3 vec312 = vec35.cross(vec311);

                    if (this.vrMovementStyle.airSparkles) {
                        for (int j = 0; j < 3; ++j) {
                            double d8 = random.nextDouble() + 3.5D;
                            double d9 = random.nextDouble() * 2.5D;
                            double d4 = random.nextDouble() * 4.0D - 2.0D;
                            Vec3 vec37 = new Vec3(mc.player.getX() + vec311.x * d8, mc.player.getY() + vec311.y * d8, mc.player.getZ() + vec311.z * d8);
                            vec37 = vec37.add(vec35.x * d4, vec35.y * d4, vec35.z * d4);
                            vec37.add(vec312.x * d9, vec312.y * d9, vec312.z * d9);
                            double d5 = -0.6D;
                        }
                    }
                } else {
                    this.movementTeleportProgress = 0.0D;
                }

                if (d7 >= 1.0D) {
                    flag = true;
                }
            }
        } else {
            if (this.vrMovementStyle.teleportOnRelease && this.movementTeleportProgress >= 1.0D) {
                vec3 = this.movementTeleportDestination;
                flag = true;
            }

            ((PlayerExtension) mc.player).vivecraft$setMovementTeleportTimer(0);
            this.movementTeleportProgress = 0.0D;
        }

        if (flag && vec3 != null && (vec3.x != 0.0D || vec3.y != 0.0D || vec3.z != 0.0D)) {
            this.movementTeleportDistance = vec3.distance((float) mc.player.position().x, (float) mc.player.position().y, (float) mc.player.position().z);

            if (this.movementTeleportDistance > 0.0D && this.vrMovementStyle.endTeleportingSound != null) {
                boolean flag3 = true;
            } else {
                boolean flag4 = false;
            }

            Block block = null;

            if (!dh.vrPlayer.isTeleportSupported()) {
                String s1 = "tp " + vec3.x + " " + vec3.y + " " + vec3.z;
                mc.player.connection.sendCommand(s1);
            } else {
                if (ClientNetworking.serverSupportsDirectTeleport) {
                    ((PlayerExtension) mc.player).vivecraft$setTeleported(true);
                }

                mc.player.moveTo(vec3.x, vec3.y, vec3.z);
            }

            this.doTeleportCallback();
            ((PlayerExtension) mc.player).vivecraft$stepSound(BlockPos.containing(convertToVec3(vec3)), vec3.x(), vec3.y(), vec3.z());
        }
    }

    public void updateTeleportDestinations() {
        mc.getProfiler().push("updateTeleportDestinations");

        if (this.vrMovementStyle.arcAiming) {
            this.movementTeleportDestination.zero();

            if (this.movementTeleportProgress > 0.0D) {
                this.updateTeleportArc();
            }
        }

        mc.getProfiler().pop();
    }

    private void updateTeleportArc() {
        final Vector3fc vec3;
        final Vector3fc vec31;
        Matrix4f matrix4f;

        if (dh.vrSettings.seated) {
            vec3 = RenderHelper.getControllerRenderPos(0, new Vector3f());
            vec31 = dh.vrPlayer.vrdata_world_render.getController(0).getDirection(new Vector3f());
            matrix4f = new Matrix4f(dh.vr.getAimRotation(0)).rotateY(dh.vrPlayer.vrdata_world_render.rotation_radians);
        } else {
            vec3 = dh.vrPlayer.vrdata_world_render.getController(1).getPosition(new Vector3f());
            vec31 = dh.vrPlayer.vrdata_world_render.getController(1).getDirection(new Vector3f());
            matrix4f = new Matrix4f(dh.vr.getAimRotation(1)).rotateY(dh.vrPlayer.vrdata_world_render.rotation_radians);
        }

        Quaternionf quaternion = new Quaternionf().setFromUnnormalized(matrix4f);
        int i = 50;
        this.movementTeleportArc[0] = new Vec3(vec3.x(), vec3.y(), vec3.z());
        this.movementTeleportArcSteps = 1;
        float f = 0.098F;
        Matrix4f matrix4f4 = matrix4f.rotateZ(-atan2((2.0F * (quaternion.x * quaternion.y + quaternion.w * quaternion.z)), (quaternion.w * quaternion.w - quaternion.x * quaternion.x + quaternion.y * quaternion.y - quaternion.z * quaternion.z)));
        Vector3f vector31 = matrix4f4.transformProject(up, new Vector3f());
        Vec3 vec32 = new Vec3(-vector31.x, -vector31.y, -vector31.z);
        vec32 = vec32.scale(f);
        float f1 = 0.5F;
        Vec3 vec33 = new Vec3(vec31.x() * (double) f1, vec31.y() * (double) f1, vec31.z() * (double) f1);
        Vec3 vec34 = new Vec3(vec3.x(), vec3.y(), vec3.z());

        for (int j = this.movementTeleportArcSteps; j < i && !((float) (j * 4) > this.teleportEnergy); ++j) {
            Vec3 vec35 = new Vec3(vec34.x + vec33.x, vec34.y + vec33.y, vec34.z + vec33.z);

            BlockHitResult blockhitresult = mc.level.clip(new ClipContext(
                vec34, vec35, ClipContext.Block.COLLIDER, Fluid.ANY, mc.player
            ));

            if (blockhitresult != null && blockhitresult.getType() != Type.MISS) {
                this.movementTeleportArc[j] = blockhitresult.getLocation();
                this.movementTeleportArcSteps = j + 1;
                Vec3 vec36 = vec34.subtract(vec35).normalize();
                Vec3 vec37 = new Vec3(-vec36.x * 0.02D, -vec36.y * 0.02D, -vec36.z * 0.02D);
                this.checkAndSetTeleportDestination(mc, mc.player, convertToVec3(vec3), blockhitresult, vec37);
                Vec3 vec38 = mc.player.position().subtract(this.movementTeleportDestination.x, this.movementTeleportDestination.y, this.movementTeleportDestination.z);
                double d0 = vec38.y;
                this.movementTeleportDistance = vec38.length();
                double d1 = sqrt(vec38.x * vec38.x + vec38.z * vec38.z);
                boolean flag1 = !mc.player.isShiftKeyDown() || !(d0 > 0.2D);

                if (!mc.player.getAbilities().mayfly && ClientNetworking.isLimitedSurvivalTeleport()) {
                    if (ClientNetworking.getTeleportDownLimit() > 0 && d0 > (double) ClientNetworking.getTeleportDownLimit() + 0.2D) {
                        flag1 = false;
                    } else if (ClientNetworking.getTeleportUpLimit() > 0 && -d0 > (double) ClientNetworking.getTeleportUpLimit() * (double) ((PlayerExtension) mc.player).vivecraft$getMuhJumpFactor() + 0.2D) {
                        flag1 = false;
                    } else if (ClientNetworking.getTeleportHorizLimit() > 0 && d1 > (double) ClientNetworking.getTeleportHorizLimit() * (double) ((PlayerExtension) mc.player).vivecraft$getMuhSpeedFactor() + 0.2D) {
                        flag1 = false;
                    }
                }

                if (!flag1) {
                    this.movementTeleportDestination.zero();
                    this.movementTeleportDistance = 0.0D;
                }

                break;
            }

            vec34 = new Vec3(vec35.x, vec35.y, vec35.z);
            this.movementTeleportArc[j] = new Vec3(vec35.x, vec35.y, vec35.z);
            this.movementTeleportArcSteps = j + 1;
            vec33 = vec33.add(vec32);
        }
    }

    private void doTeleportCallback() {
        dh.swingTracker.disableSwing = 3;

        if (ClientNetworking.isLimitedSurvivalTeleport()) {
            mc.player.causeFoodExhaustion((float) (this.movementTeleportDistance / 16.0D * (double) 1.2F));

            if (mc.gameMode.hasMissTime() && this.vrMovementStyle.arcAiming) {
                this.teleportEnergy = (float) ((double) this.teleportEnergy - this.movementTeleportDistance * 4.0D);
            }
        }

        mc.player.fallDistance = 0.0F;
        ((PlayerExtension) mc.player).vivecraft$setMovementTeleportTimer(-1);
    }

    private boolean checkAndSetTeleportDestination(Minecraft mc, LocalPlayer player, Vec3 start, BlockHitResult collision, Vec3 reverseEpsilon) {
        BlockPos blockpos = collision.getBlockPos();
        BlockState blockstate = player.level().getBlockState(blockpos);

        if (!mc.level.getFluidState(blockpos).isEmpty()) {
            Vec3 vec3 = new Vec3(collision.getLocation().x, blockpos.getY(), collision.getLocation().z);
            Vec3 vec31 = vec3.subtract(player.getX(), player.getBoundingBox().minY, player.getZ());
            AABB aabb = player.getBoundingBox().move(vec31.x, vec31.y, vec31.z);
            boolean flag = mc.level.noCollision(player, aabb);

            if (!flag) {
                Vec3 vec32 = Vec3.atBottomCenterOf(blockpos);
                vec31 = vec32.subtract(player.getX(), player.getBoundingBox().minY, player.getZ());
                aabb = player.getBoundingBox().move(vec31.x, vec31.y, vec31.z);
                flag = mc.level.noCollision(player, aabb);
            }

            float f = 0.0F;

            if (dh.vrSettings.seated) {
                f = 0.5F;
            }

            if (flag) {
                this.movementTeleportDestination.set(aabb.getCenter().x, aabb.minY + f, aabb.getCenter().z);
                this.movementTeleportDestinationSideHit = collision.getDirection();
                return true;
            }
        } else if (collision.getDirection() != Direction.UP) {
            if (blockstate.getBlock() instanceof LadderBlock || blockstate.getBlock() instanceof VineBlock || blockstate.is(BlockTags.VIVECRAFT_CLIMBABLE)) {
                Vec3 vec36 = new Vec3((double) blockpos.getX() + 0.5D, (double) blockpos.getY() + 0.5D, (double) blockpos.getZ() + 0.5D);
                Block block = mc.level.getBlockState(blockpos.below()).getBlock();

                if (block == blockstate.getBlock()) {
                    vec36 = vec36.add(0.0D, -1.0D, 0.0D);
                }

                convertToVector3f(vec36.scale(1.0D), this.movementTeleportDestination);
                this.movementTeleportDestinationSideHit = collision.getDirection();
                return true;
            }

            if (!mc.player.getAbilities().mayfly && ClientNetworking.isLimitedSurvivalTeleport()) {
                return false;
            }
        }

        double d1 = 0.0D;
        BlockPos blockpos1 = collision.getBlockPos().below();

        for (int i = 0; i < 2; ++i) {
            blockstate = player.level().getBlockState(blockpos1);

            if (blockstate.getCollisionShape(mc.level, blockpos1).isEmpty()) {
                blockpos1 = blockpos1.above();
            } else {
                double d2 = blockstate.getCollisionShape(mc.level, blockpos1).max(Axis.Y);
                Vec3 vec33 = new Vec3(collision.getLocation().x, (double) blockpos1.getY() + d2, collision.getLocation().z);
                Vec3 vec34 = vec33.subtract(player.getX(), player.getBoundingBox().minY, player.getZ());
                AABB aabb1 = player.getBoundingBox().move(vec34.x, vec34.y, vec34.z);
                double d0 = 0.0D;

                if (blockstate.getBlock() == Blocks.SOUL_SAND || blockstate.getBlock() == Blocks.HONEY_BLOCK) {
                    d0 = 0.05D;
                }

                boolean flag1 = mc.level.noCollision(player, aabb1) && !mc.level.noCollision(player, aabb1.inflate(0.0D, 0.125D + d0, 0.0D));

                if (!flag1) {
                    Vec3 vec35 = Vec3.upFromBottomCenterOf(blockpos1, d2);
                    vec34 = vec35.subtract(player.getX(), player.getBoundingBox().minY, player.getZ());
                    aabb1 = player.getBoundingBox().move(vec34.x, vec34.y, vec34.z);
                    flag1 = mc.level.noCollision(player, aabb1) && !mc.level.noCollision(player, aabb1.inflate(0.0D, 0.125D + d0, 0.0D));
                }

                if (flag1) {
                    Vec3 vec37 = new Vec3(aabb1.getCenter().x, (double) blockpos1.getY() + d2, aabb1.getCenter().z);
                    convertToVector3f(vec37.scale(1.0D), this.movementTeleportDestination);
                    return true;
                }

                blockpos1 = blockpos1.above();
            }
        }

        return false;
    }

    public Vector3f getInterpolatedArcPosition(float progress, Vector3f dest) {
        if (this.movementTeleportArcSteps != 1 && !(progress <= 0.0F)) {
            if (progress >= 1.0F) {
                return convertToVector3f(this.movementTeleportArc[this.movementTeleportArcSteps - 1], dest);
            } else {
                float f = progress * (float) (this.movementTeleportArcSteps - 1);
                int i = (int) floor(f);
                double d0 = this.movementTeleportArc[i + 1].x - this.movementTeleportArc[i].x;
                double d1 = this.movementTeleportArc[i + 1].y - this.movementTeleportArc[i].y;
                double d2 = this.movementTeleportArc[i + 1].z - this.movementTeleportArc[i].z;
                float f1 = f - (float) i;
                return dest.set(this.movementTeleportArc[i].x + d0 * (double) f1, this.movementTeleportArc[i].y + d1 * (double) f1, this.movementTeleportArc[i].z + d2 * (double) f1);
            }
        } else {
            return convertToVector3f(this.movementTeleportArc[0], dest);
        }
    }
}
