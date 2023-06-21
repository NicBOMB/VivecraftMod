package org.vivecraft.client_vr;

import javax.annotation.Nonnull;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client.utils.Utils;
import org.vivecraft.common.utils.math.Matrix4f;
import org.vivecraft.common.utils.math.Vector3;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class VRData
{
    @Nonnull
    public VRDevicePose hmd;
    @Nonnull
    public VRDevicePose eye0;
    @Nonnull
    public VRDevicePose eye1;
    @Nonnull
    public VRDevicePose c0;
    @Nonnull
    public VRDevicePose c1;
    @Nonnull
    public VRDevicePose c2;
    @Nonnull
    public VRDevicePose h0;
    @Nonnull
    public VRDevicePose h1;
    @Nonnull
    public VRDevicePose g0;
    @Nonnull
    public VRDevicePose g1;
    @Nonnull
    public VRDevicePose t0;
    @Nonnull
    public VRDevicePose t1;
    @Nonnull
    public VRDevicePose cam;
    @Nonnull
    public Vec3 origin;
    public float rotation_radians;
    public float worldScale;

    public VRData(@Nonnull Vec3 origin, float walkMul, float worldScale, float rotation)
    {
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
        this.origin = origin;
        this.worldScale = worldScale;
        this.rotation_radians = rotation;
        Vec3 hmd_raw = dataholder.vr.getCenterEyePosition();
        Vec3 scaledPos = new Vec3(hmd_raw.x * (double)walkMul, hmd_raw.y, hmd_raw.z * (double)walkMul);
        this.hmd = new VRDevicePose(this, dataholder.vr.hmdRotation, scaledPos, dataholder.vr.getHmdVector());
        this.eye0 = new VRDevicePose(this, dataholder.vr.getEyeRotation(RenderPass.LEFT), dataholder.vr.getEyePosition(RenderPass.LEFT).subtract(hmd_raw).add(scaledPos), dataholder.vr.getHmdVector());
        this.eye1 = new VRDevicePose(this, dataholder.vr.getEyeRotation(RenderPass.RIGHT), dataholder.vr.getEyePosition(RenderPass.RIGHT).subtract(hmd_raw).add(scaledPos), dataholder.vr.getHmdVector());
        this.c0 = new VRDevicePose(this, dataholder.vr.getAimRotation(0), dataholder.vr.getAimSource(0).subtract(hmd_raw).add(scaledPos), dataholder.vr.getAimVector(0));
        this.c1 = new VRDevicePose(this, dataholder.vr.getAimRotation(1), dataholder.vr.getAimSource(1).subtract(hmd_raw).add(scaledPos), dataholder.vr.getAimVector(1));
//        this.g0 = new VRDevicePose(this, dataholder.vr.getGestureRotation(0), dataholder.vr.getGesturePosition(0), dataholder.vr.getGestureVector(0));
//        this.g1 = new VRDevicePose(this, dataholder.vr.getGestureRotation(1), dataholder.vr.getGesturePosition(1), dataholder.vr.getGestureVector(1));
        this.h0 = new VRDevicePose(this, dataholder.vr.getHandRotation(0), dataholder.vr.getAimSource(0).subtract(hmd_raw).add(scaledPos), dataholder.vr.getHandVector(0));
        this.h1 = new VRDevicePose(this, dataholder.vr.getHandRotation(1), dataholder.vr.getAimSource(1).subtract(hmd_raw).add(scaledPos), dataholder.vr.getHandVector(1));

        if(dataholder.vrSettings.seated) {
        	this.t0 = eye0;
        	this.t1 = eye1;
        } else {
        	Matrix4f s1 = this.getSmoothedRotation(0, 0.2F);
        	Matrix4f s2 = this.getSmoothedRotation(1, 0.2F);
        	this.t0 = new VRDevicePose(this, s1, dataholder.vr.getAimSource(0).subtract(hmd_raw).add(scaledPos), s1.transform(Vector3.forward()).toVector3d());
        	this.t1 = new VRDevicePose(this, s2, dataholder.vr.getAimSource(1).subtract(hmd_raw).add(scaledPos), s2.transform(Vector3.forward()).toVector3d());
        }
        
        Matrix4f camRot = Matrix4f.multiply(Matrix4f.rotationY(-rotation), (new Matrix4f(ClientDataHolderVR.getInstance().cameraTracker.getRotation())).transposed());
        float inverseWorldScale = 1.0F / worldScale;
        this.cam = new VRData.VRDevicePose(this, camRot, ClientDataHolderVR.getInstance().cameraTracker.getPosition().subtract(origin).yRot(-rotation).multiply(inverseWorldScale,inverseWorldScale,inverseWorldScale).subtract(hmd_raw).add(scaledPos), camRot.transform(Vector3.forward()).toVector3d());

        if (dataholder.vr.mrMovingCamActive)
        {
            this.c2 = new VRDevicePose(this, dataholder.vr.getAimRotation(2), dataholder.vr.getAimSource(2).subtract(hmd_raw).add(scaledPos), dataholder.vr.getAimVector(2));
        }
        else
        {
            VRSettings settings = ClientDataHolderVR.getInstance().vrSettings;
            Matrix4f rot = (new Matrix4f(settings.vrFixedCamrotQuat)).transposed();
            Vec3 pos = new Vec3((double)settings.vrFixedCamposX, (double)settings.vrFixedCamposY, (double)settings.vrFixedCamposZ);
            Vec3 dir = rot.transform(Vector3.forward()).toVector3d();
            this.c2 = new VRDevicePose(this, rot, pos.subtract(hmd_raw).add(scaledPos), dir);
        }
    }

    private Matrix4f getSmoothedRotation(int c, float lenSec)
    {
        ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();

        Vec3 pos = dataholder.vr.controllerHistory[c].averagePosition(lenSec);
        Vec3 u = dataholder.vr.controllerForwardHistory[c].averagePosition(lenSec);
        Vec3 f = dataholder.vr.controllerUpHistory[c].averagePosition(lenSec);
        Vec3 r = u.cross(f);
        return new Matrix4f((float)r.x, (float)u.x, (float)f.x, (float)r.y, (float)u.y, (float)f.y, (float)r.z, (float)u.z, (float)f.z);
    }

    public VRDevicePose getController(int c)
    {
        return c == 1 ? this.c1 : (c == 2 ? this.c2 : this.c0);
    }

    public VRDevicePose getHand(int c)
    {
        return c == 0 ? this.h0 : this.h1;
    }

    public VRDevicePose getGesture(int c)
    {
        return c == 0 ? this.g0 : this.g1;
    }

    public float getBodyYaw()
    {
        if (ClientDataHolderVR.getInstance().vrSettings.seated)
        {
            return this.hmd.getYaw();
        }
        else
        {
            Vec3 v = this.c1.getPosition().subtract(this.c0.getPosition()).normalize().yRot((-(float)Math.PI / 2F));
            Vec3 h = this.hmd.getDirection();

            if (v.dot(h) < 0.0D)
            {
                v = v.reverse();
            }

            v = Utils.vecLerp(h, v, 0.7D);
            return (float)Math.toDegrees(Math.atan2(-v.x, v.z));
        }
    }

    public float getFacingYaw()
    {
        if (ClientDataHolderVR.getInstance().vrSettings.seated)
        {
            return this.hmd.getYaw();
        }
        else
        {
            Vec3 v = this.c1.getPosition().subtract(this.c0.getPosition()).normalize().yRot((-(float)Math.PI / 2F));
            return ClientDataHolderVR.getInstance().vrSettings.reverseHands ? (float)Math.toDegrees(Math.atan2(v.x, -v.z)) : (float)Math.toDegrees(Math.atan2(-v.x, v.z));
        }
    }

    public Vec3 getHeadPivot()
    {
        Vec3 eye = this.hmd.getPosition();
        // scale pivot point with world scale, to prevent unwanted player movement
        Vector3 v3 = this.hmd.getMatrix().transform(new Vector3(0.0F, -0.1F * worldScale, 0.1F * worldScale));
        return new Vec3((double)v3.getX() + eye.x, (double)v3.getY() + eye.y, (double)v3.getZ() + eye.z);
    }

    public Vec3 getHeadRear()
    {
        Vec3 eye = this.hmd.getPosition();
        Vector3 v3 = this.hmd.getMatrix().transform(new Vector3(0.0F, -0.2F, 0.2F));
        return new Vec3((double)v3.getX() + eye.x, (double)v3.getY() + eye.y, (double)v3.getZ() + eye.z);
    }

    public VRDevicePose getEye(RenderPass pass)
    {
        return switch (pass) {
            case CENTER -> this.hmd;
            case LEFT -> this.eye0;
            case RIGHT -> this.eye1;
            case THIRD -> this.c2;
            case SCOPER -> this.t0;
            case SCOPEL -> this.t1;
            case CAMERA -> this.cam;
            default -> this.hmd;
        };
    }

    public String toString()
    {
        return "data:\r\n \t\t origin: " + this.origin + "\r\n \t\t rotation: " + String.format("%.2f", this.rotation_radians) + "\r\n \t\t scale: " + String.format("%.2f", this.worldScale) + "\r\n \t\t hmd " + this.hmd + "\r\n \t\t c0 " + this.c0 + "\r\n \t\t c1 " + this.c1 + "\r\n \t\t c2 " + this.c2;
    }

    protected Vec3 vecMult(Vec3 in, float factor)
    {
        return new Vec3(in.x * (double)factor, in.y * (double)factor, in.z * (double)factor);
    }

    public class VRDevicePose
    {
        final VRData data;
        final Vec3 pos;
        final Vec3 dir;
        final Matrix4f matrix;

        public VRDevicePose(VRData data, Matrix4f matrix, Vec3 pos, Vec3 dir)
        {
            this.data = data;
            this.matrix = matrix.transposed().transposed();
            this.pos = new Vec3(pos.x, pos.y, pos.z);
            this.dir = new Vec3(dir.x, dir.y, dir.z);
        }

        public Vec3 getPosition()
        {
            Vec3 out = this.pos.scale(VRData.this.worldScale);
            out = out.yRot(this.data.rotation_radians);
            return out.add(this.data.origin.x, this.data.origin.y, this.data.origin.z);
        }

        public Vec3 getDirection()
        {
            return (new Vec3(this.dir.x, this.dir.y, this.dir.z)).yRot(this.data.rotation_radians);
        }

        public Vec3 getCustomVector(Vec3 axis)
        {
            Vector3 v3 = this.matrix.transform(new Vector3((float)axis.x, (float)axis.y, (float)axis.z));
            return v3.toVector3d().yRot(this.data.rotation_radians);
        }

        public float getYaw()
        {
            Vec3 dir = this.getDirection();
            return (float)Math.toDegrees(Math.atan2(-dir.x, dir.z));
        }

        public float getPitch()
        {
            Vec3 dir = this.getDirection();
            return (float)Math.toDegrees(Math.asin(dir.y / dir.length()));
        }

        public float getRoll()
        {
            return (float)(-Math.toDegrees(Math.atan2(this.matrix.M[1][0], this.matrix.M[1][1])));
        }

        public Matrix4f getMatrix()
        {
            Matrix4f rot = Matrix4f.rotationY(VRData.this.rotation_radians);
            return Matrix4f.multiply(rot, this.matrix);
        }

        public String toString()
        {
            return "Device: pos:" + this.getPosition() + " dir: " + this.getDirection();
        }
    }
}
