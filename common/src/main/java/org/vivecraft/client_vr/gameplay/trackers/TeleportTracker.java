package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.vivecraft.client.VivecraftVRMod;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.BlockTags;
import org.vivecraft.client_vr.extensions.GameRendererExtension;
import org.vivecraft.client_vr.extensions.PlayerExtension;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.provider.openvr_lwjgl.OpenVRUtil;
import org.vivecraft.client_vr.gameplay.VRMovementStyle;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.common.utils.math.Angle;
import org.vivecraft.common.utils.math.Matrix4f;
import org.vivecraft.common.utils.math.Quaternion;
import org.vivecraft.common.utils.math.Vector3;

import java.util.Random;

public class TeleportTracker extends Tracker
{
    private float teleportEnergy;
    private Vec3 movementTeleportDestination = new Vec3(0.0D, 0.0D, 0.0D);
    private Direction movementTeleportDestinationSideHit;
    public double movementTeleportProgress;
    public double movementTeleportDistance;
    private Vec3[] movementTeleportArc = new Vec3[50];
    public int movementTeleportArcSteps = 0;
    public double lastTeleportArcDisplayOffset = 0.0D;
    public VRMovementStyle vrMovementStyle;

    public TeleportTracker(Minecraft mc, ClientDataHolderVR dh)
    {
        super(mc, dh);
        this.vrMovementStyle = new VRMovementStyle(dh);
    }

    public float getTeleportEnergy()
    {
        return this.teleportEnergy;
    }

    public boolean isAiming()
    {
        return this.movementTeleportProgress > 0.0D;
    }

    public Vec3 getDestination()
    {
        return this.movementTeleportDestination;
    }

    public boolean isActive(LocalPlayer p)
    {
        if (p == null)
        {
            return false;
        }
        else if (this.mc.gameMode == null)
        {
            return false;
        }
        else if (!p.isAlive())
        {
            return false;
        }
        else
        {
            return !p.isSleeping();
        }
    }

    public void reset(LocalPlayer player)
    {
        this.movementTeleportDestination = new Vec3(0.0D, 0.0D, 0.0D);
        this.movementTeleportArcSteps = 0;
        this.movementTeleportProgress = 0.0D;
    }

    public void doProcess(LocalPlayer player)
    {
        Random random = new Random();
        PlayerExtension playerExt = (PlayerExtension) player;

        if (this.teleportEnergy < 100.0F)
        {
            ++this.teleportEnergy;
        }

        boolean doTeleport = false;
        Vec3 dest = null;
        boolean bindingTeleport = VivecraftVRMod.INSTANCE.keyTeleport.isDown() && this.dh.vrPlayer.isTeleportEnabled();
        boolean seatedTeleport = this.dh.vrSettings.seated && !this.dh.vrPlayer.getFreeMove() && (player.input.forwardImpulse != 0.0F || player.input.leftImpulse != 0.0F);

        if ((bindingTeleport || seatedTeleport) && !player.isPassenger())
        {
            dest = this.movementTeleportDestination;

            if (this.vrMovementStyle.teleportOnRelease)
            {
                if (playerExt.getMovementTeleportTimer() == 0)
                {
                    String playCustomTeleportSound = this.vrMovementStyle.startTeleportingSound;
                    // if (playCustomTeleportSound != null)
                    // {
                    //     player.playSound(SoundEvents(playCustomTeleportSound), vrMovementStyle.startTeleportingSoundVolume,
                    //     1.0F / (rand.nextFloat() * 0.4F + 1.2F) + 1.0f * 0.5F);
                    // }
                }

                playerExt.setMovementTeleportTimer(playerExt.getMovementTeleportTimer() +1);

                if (playerExt.getMovementTeleportTimer() > 0)
                {
                    this.movementTeleportProgress = playerExt.getMovementTeleportTimer();

                    if (this.movementTeleportProgress >= 1.0D)
                    {
                        this.movementTeleportProgress = 1.0D;
                    }

                    if (dest.x != 0.0D || dest.y != 0.0D || dest.z != 0.0D)
                    {
                        Vec3 eyeCenterPos = this.dh.vrPlayer.vrdata_world_pre.hmd.getPosition();

                        // cloud of sparks moving past you
                        Vec3 motionDir = dest.add(-eyeCenterPos.x, -eyeCenterPos.y, -eyeCenterPos.z).normalize();
                        Vec3 forward = player.getLookAngle();

                        Vec3 right = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
                        Vec3 up = right.cross(forward);

                        if (this.vrMovementStyle.airSparkles)
                        {
                            for (int iParticle = 0; iParticle < 3; ++iParticle)
                            {
                                double forwardDist = random.nextDouble() * 1.0D + 3.5D;
                                double upDist = random.nextDouble() * 2.5D;
                                double rightDist = random.nextDouble() * 4.0D - 2.0D;
                                Vec3 sparkPos = new Vec3(eyeCenterPos.x + forward.x * forwardDist, eyeCenterPos.y + forward.y * forwardDist, eyeCenterPos.z + forward.z * forwardDist);
                                sparkPos = sparkPos.add(right.x * rightDist, right.y * rightDist, right.z * rightDist);
                                sparkPos.add(up.x * upDist, up.y * upDist, up.z * upDist);
                                double speed = -0.6D;
                                // EntityFX particle = new ParticleVRTeleportFX(
                                //     player.world,
                                //     sparkPos.x, sparkPos.y, sparkPos.z,
                                //     motionDir.x * speed, motionDir.y * speed, motionDir.z * speed,
                                //     1.0f
                                // );
                                // mc.effectRenderer.addEffect(particle);
                            }
                        }
                    }
                }
            }
            else if (playerExt.getMovementTeleportTimer() >= 0 && (dest.x != 0.0D || dest.y != 0.0D || dest.z != 0.0D))
            {
                if (playerExt.getMovementTeleportTimer() == 0)
                {
//                        String sound = vrMovementStyle.startTeleportingSound;
//                        if (sound != null)
//                        {
//                            player.playSound(SoundEvents.getRegisteredSoundEvent(sound), vrMovementStyle.startTeleportingSoundVolume,
//                                    1.0F / (rand.nextFloat() * 0.4F + 1.2F) + 1.0f * 0.5F);
//                        }
                }

                playerExt.setMovementTeleportTimer(playerExt.getMovementTeleportTimer() + 1);
                Vec3 playerPos = player.position();
                double dist = dest.distanceTo(playerPos);
                double progress = (playerExt.getMovementTeleportTimer() * 1.0D) / (dist + 3.0D);

                if (playerExt.getMovementTeleportTimer() > 0)
                {
                    this.movementTeleportProgress = progress;

                    // spark at dest point
                    if (this.vrMovementStyle.destinationSparkles)
                    {
                        //  player.world.spawnParticle("instantSpell", dest.x, dest.y, dest.z, 0, 1.0, 0);
                    }

                    // cloud of sparks moving past you
                    Vec3 motionDir = dest.add(-player.getX(), -player.getY(), -player.getZ()).normalize();
                    Vec3 forward = player.getLookAngle();
                    Vec3 right = forward.cross(new Vec3(0.0D, 1.0D, 0.0D));
                    Vec3 up = right.cross(forward);

                    if (this.vrMovementStyle.airSparkles)
                    {
                        for (int iParticle = 0; iParticle < 3; ++iParticle)
                        {
                            double forwardDist = random.nextDouble() * 1.0D + 3.5D;
                            double upDist = random.nextDouble() * 2.5D;
                            double rightDist = random.nextDouble() * 4.0D - 2.0D;
                            Vec3 sparkPos = new Vec3(player.getX() + forward.x * forwardDist, player.getY() + forward.y * forwardDist, player.getZ() + forward.z * forwardDist);
                            sparkPos = sparkPos.add(right.x * rightDist, right.y * rightDist, right.z * rightDist);
                            sparkPos.add(up.x * upDist, up.y * upDist, up.z * upDist);
                            double speed = -0.6D;
//                                EntityFX particle = new ParticleVRTeleportFX(
//                                        player.world,
//                                        sparkPos.x, sparkPos.y, sparkPos.z,
//                                        motionDir.x * speed, motionDir.y * speed, motionDir.z * speed,
//                                        1.0f);
//                                mc.effectRenderer.addEffect(particle);
                        }
                    }
                }
                else
                {
                    this.movementTeleportProgress = 0.0D;
                }

                if (progress >= 1.0D)
                {
                    doTeleport = true;
                }
            }
        }
        else
        {
            if (this.vrMovementStyle.teleportOnRelease && this.movementTeleportProgress >= 1.0D)
            {
                dest = this.movementTeleportDestination;
                doTeleport = true;
            }

            playerExt.setMovementTeleportTimer(0);
            this.movementTeleportProgress = 0.0D;
        }

        if (doTeleport && dest != null && (dest.x != 0.0D || dest.y != 0.0D || dest.z != 0.0D))
        {
            this.movementTeleportDistance = dest.distanceTo(player.position());

            boolean playCustomTeleportSound = this.movementTeleportDistance > 0.0D && this.vrMovementStyle.endTeleportingSound != null;
            Block block = null;

            if (!this.dh.vrPlayer.isTeleportSupported())
            {
                String s1 = "tp " + dest.x + " " + dest.y + " " + dest.z;
                this.mc.player.connection.sendCommand(s1);
            }
            else
            {
                if (ClientNetworking.serverSupportsDirectTeleport)
                {
                	playerExt.setTeleported(true);
                }

                player.moveTo(dest.x, dest.y, dest.z);
            }

            this.doTeleportCallback();
            ((PlayerExtension) this.mc.player).stepSound(BlockPos.containing(dest), dest);
        }
    }

    public void updateTeleportDestinations(GameRenderer renderer, Minecraft mc, LocalPlayer player)
    {
        mc.getProfiler().push("updateTeleportDestinations");

        if (this.vrMovementStyle.arcAiming)
        {
            this.movementTeleportDestination = new Vec3(0.0D, 0.0D, 0.0D);

            if (this.movementTeleportProgress > 0.0D)
            {
                this.updateTeleportArc(mc, player);
            }
        }

        mc.getProfiler().pop();
    }

    private void updateTeleportArc(Minecraft mc, LocalPlayer player)
    {
        Vec3 start = dh.vrPlayer.vrdata_world_render.getController(1).getPosition();
        Vec3 tiltedAim = dh.vrPlayer.vrdata_world_render.getController(1).getDirection();
        Matrix4f handRotation = dh.vr.getAimRotation(1);

        if (dh.vrSettings.seated)
        {
            start = ((GameRendererExtension) mc.gameRenderer).getControllerRenderPos(0);
            tiltedAim = dh.vrPlayer.vrdata_world_render.getController(0).getDirection();
            handRotation = dh.vr.getAimRotation(0);
        }

        Matrix4f rot = Matrix4f.rotationY(dh.vrPlayer.vrdata_world_render.rotation_radians);
        handRotation = Matrix4f.multiply(rot, handRotation);

        // extract hand roll
        Quaternion handQuat = OpenVRUtil.convertMatrix4ftoRotationQuat(handRotation);
        Angle angle = handQuat.toEuler();
        //TODO: use vrdata for this

        int maxSteps = 50;
        this.movementTeleportArc[0] = new Vec3(start.x, start.y, start.z);
        this.movementTeleportArcSteps = 1;

        // calculate gravity vector for arc
        float f = 0.098F;
        Matrix4f rollCounter = Utils.rotationZMatrix((float)Math.toRadians((double)(-angle.getRoll())));
        Matrix4f gravityTilt = Utils.rotationXMatrix(-2.5132742F);
        Matrix4f gravityRotation = Matrix4f.multiply(handRotation, rollCounter);

        Vector3 forward = new Vector3(0.0F, 1.0F, 0.0F);
        Vector3 gravityDirection = gravityRotation.transform(forward);
        Vec3 gravity = gravityDirection.negate().toVector3d();

        gravity = gravity.scale(f);


        //   gravity.rotateAroundY(this.worldRotationRadians);

        // calculate initial move step
        float speed = 0.5F;
        Vec3 velocity = new Vec3(tiltedAim.x * (double)speed, tiltedAim.y * (double)speed, tiltedAim.z * (double)speed);
        Vec3 pos = new Vec3(start.x, start.y, start.z);

        for (int i = this.movementTeleportArcSteps; i < maxSteps && !((float)(i * 4) > this.teleportEnergy); ++i)
        {
            Vec3 newPos = new Vec3(pos.x + velocity.x, pos.y + velocity.y, pos.z + velocity.z);
            boolean water = dh.vrSettings.seated ? ((GameRendererExtension) mc.gameRenderer).isInWater() : !mc.level.getFluidState(BlockPos.containing(start)).isEmpty();

            BlockHitResult blockhitresult = mc.level.clip(new ClipContext(pos, newPos, ClipContext.Block.COLLIDER, water ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, mc.player));

            if (blockhitresult != null && blockhitresult.getType() != HitResult.Type.MISS)
            {
                this.movementTeleportArc[i] = blockhitresult.getLocation();
                this.movementTeleportArcSteps = i + 1;

                Vec3 traceDir = pos.subtract(newPos).normalize();
                Vec3 reverseEpsilon = new Vec3(-traceDir.x * 0.02D, -traceDir.y * 0.02D, -traceDir.z * 0.02D);

                this.checkAndSetTeleportDestination(mc, player, start, blockhitresult, reverseEpsilon);

                Vec3 diff = mc.player.position().subtract(this.movementTeleportDestination);

                double yDiff = diff.y;
                this.movementTeleportDistance = diff.length();
                double xzdiff = Math.sqrt(diff.x * diff.x + diff.z * diff.z);

                boolean ok = true;

                if (mc.player.isShiftKeyDown() && yDiff > 0.2D)
                {
                    ok = false;
                }

                if (!mc.player.getAbilities().mayfly && ClientNetworking.isLimitedSurvivalTeleport())
                {
                    if (ClientNetworking.getTeleportDownLimit() > 0 && yDiff > (double) ClientNetworking.getTeleportDownLimit() + 0.2D)
                    {
                        ok = false;
                    }
                    else if (ClientNetworking.getTeleportUpLimit() > 0 && -yDiff > (double) ClientNetworking.getTeleportUpLimit() * (double)((PlayerExtension) player).getMuhJumpFactor() + 0.2D)
                    {
                        ok = false;
                    }
                    else if (ClientNetworking.getTeleportHorizLimit() > 0 && xzdiff > (double) ClientNetworking.getTeleportHorizLimit() * (double)((PlayerExtension) player).getMuhSpeedFactor() + 0.2D)
                    {
                        ok = false;
                    }
                }

                if (!ok)
                {
                    this.movementTeleportDestination = new Vec3(0.0D, 0.0D, 0.0D);
                    this.movementTeleportDistance = 0.0D;
                }

                break;
            }

            pos = new Vec3(newPos.x, newPos.y, newPos.z);

            this.movementTeleportArc[i] = new Vec3(newPos.x, newPos.y, newPos.z);
            this.movementTeleportArcSteps = i + 1;

            velocity = velocity.add(gravity);
        }
    }

    private void doTeleportCallback()
    {
        Minecraft minecraft = Minecraft.getInstance();
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        dataholder.swingTracker.disableSwing = 3;

        if (ClientNetworking.isLimitedSurvivalTeleport())
        {
            minecraft.player.causeFoodExhaustion((float)((this.movementTeleportDistance / 16.0D) * 1.2F));

            if (minecraft.gameMode.hasMissTime() && this.vrMovementStyle.arcAiming)
            {
                this.teleportEnergy = (float)(((double)this.teleportEnergy) - this.movementTeleportDistance * 4.0D);
            }
        }

        minecraft.player.fallDistance = 0.0F;
        ((PlayerExtension) minecraft.player).setMovementTeleportTimer(-1);
    }

    private boolean checkAndSetTeleportDestination(Minecraft mc, LocalPlayer player, Vec3 start, BlockHitResult collision, Vec3 reverseEpsilon)
    {
        BlockPos bp = collision.getBlockPos();
        BlockState testClimb = player.level.getBlockState(bp);

        if (!mc.level.getFluidState(bp).isEmpty())
        {
            Vec3 hitVec = new Vec3(collision.getLocation().x, (double)bp.getY(), collision.getLocation().z);
            Vec3 offset = hitVec.subtract(player.getX(), player.getBoundingBox().minY, player.getZ());
            AABB aabb = player.getBoundingBox().move(offset.x, offset.y, offset.z);
            boolean emptySpotReq = mc.level.noCollision(player, aabb);

            if (!emptySpotReq)
            {
                Vec3 center = Vec3.atBottomCenterOf(bp);
                offset = center.subtract(player.getX(), player.getBoundingBox().minY, player.getZ());
                aabb = player.getBoundingBox().move(offset.x, offset.y, offset.z);
                emptySpotReq = mc.level.noCollision(player, aabb);
            }

            float ex = dh.vrSettings.seated ? 0.5F : 0.0F;

            if (emptySpotReq)
            {
                this.movementTeleportDestination = new Vec3(aabb.getCenter().x, aabb.minY + (double)ex, aabb.getCenter().z);
                this.movementTeleportDestinationSideHit = collision.getDirection();
                return true;
            }
        }
        else if (collision.getDirection() != Direction.UP)
        { //sides
            //jrbudda require arc hitting top of block.	unless ladder or vine or creative or limits off.
            if (testClimb.getBlock() instanceof LadderBlock || testClimb.getBlock() instanceof VineBlock || testClimb.is(BlockTags.VIVECRAFT_CLIMBABLE))
            {
                Vec3 dest = new Vec3((double)bp.getX() + 0.5D, (double)bp.getY() + 0.5D, (double)bp.getZ() + 0.5D);
                Block playerblock = mc.level.getBlockState(bp.below()).getBlock();

                if (playerblock == testClimb.getBlock())
                {
                    dest = dest.add(0.0D, -1.0D, 0.0D);
                }

                this.movementTeleportDestination = dest.scale(1.0D);
                this.movementTeleportDestinationSideHit = collision.getDirection();
                return true;
            }

            if (!mc.player.getAbilities().mayfly && ClientNetworking.isLimitedSurvivalTeleport())
            {
                return false;
            }
        }

        double y = 0.0D;
        BlockPos hitBlock = collision.getBlockPos().below();

        for (int i = 0; i < 2; ++i)
        {
            testClimb = player.level.getBlockState(hitBlock);

            if (testClimb.getCollisionShape(mc.level, hitBlock).isEmpty())
            {
                hitBlock = hitBlock.above();
            }
            else
            {
                double height = testClimb.getCollisionShape(mc.level, hitBlock).max(Direction.Axis.Y);
                Vec3 hitVec = new Vec3(collision.getLocation().x, (double)hitBlock.getY() + height, collision.getLocation().z);
                Vec3 offset = hitVec.subtract(player.getX(), player.getBoundingBox().minY, player.getZ());
                AABB bb = player.getBoundingBox().move(offset.x, offset.y, offset.z);
                double ex = testClimb.getBlock() == Blocks.SOUL_SAND || testClimb.getBlock() == Blocks.HONEY_BLOCK ? 0.05D : 0.0D;

                boolean emptySpotReq = mc.level.noCollision(player, bb) && !mc.level.noCollision(player, bb.inflate(0.0D, 0.125D + ex, 0.0D));

                if (!emptySpotReq)
                {
                    Vec3 center = Vec3.upFromBottomCenterOf(hitBlock, height);
                    offset = center.subtract(player.getX(), player.getBoundingBox().minY, player.getZ());
                    bb = player.getBoundingBox().move(offset.x, offset.y, offset.z);
                    emptySpotReq = mc.level.noCollision(player, bb) && !mc.level.noCollision(player, bb.inflate(0.0D, 0.125D + ex, 0.0D));
                }

                if (emptySpotReq)
                {
                    Vec3 dest = new Vec3(bb.getCenter().x, (double)hitBlock.getY() + height, bb.getCenter().z);
                    this.movementTeleportDestination = dest.scale(1.0D);
                    return true;
                }

                hitBlock = hitBlock.above();
            }
        }

        return false;
    }

    public Vec3 getInterpolatedArcPosition(float progress)
    {
        if (this.movementTeleportArcSteps != 1 && !(progress <= 0.0F))
        {
            if (progress >= 1.0F)
            {
                return new Vec3(this.movementTeleportArc[this.movementTeleportArcSteps - 1].x, this.movementTeleportArc[this.movementTeleportArcSteps - 1].y, this.movementTeleportArc[this.movementTeleportArcSteps - 1].z);
            }
            else
            {
                float stepFloat = progress * (float)(this.movementTeleportArcSteps - 1);
                int step = (int)Math.floor(stepFloat);

                double deltaX = this.movementTeleportArc[step + 1].x - this.movementTeleportArc[step].x;
                double deltaY = this.movementTeleportArc[step + 1].y - this.movementTeleportArc[step].y;
                double deltaZ = this.movementTeleportArc[step + 1].z - this.movementTeleportArc[step].z;

                float stepProgress = stepFloat - step;

                return new Vec3(
                        this.movementTeleportArc[step].x + deltaX * (double)stepProgress,
                        this.movementTeleportArc[step].y + deltaY * (double)stepProgress,
                        this.movementTeleportArc[step].z + deltaZ * (double)stepProgress
                );
            }
        }
        else
        {
            return new Vec3(
                    this.movementTeleportArc[0].x,
                    this.movementTeleportArc[0].y,
                    this.movementTeleportArc[0].z
            );
        }
    }
}
