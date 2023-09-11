package org.vivecraft.common.utils;

import org.vivecraft.common.utils.color.Color;

import org.joml.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult.Type;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import static org.joml.Math.*;

@ParametersAreNonnullByDefault public class Utils {

    public static final Vector3dc forward = new Vector3d(0.0F, 0.0F, -1.0F);
    public static final Vector3dc backward = new Vector3d(0.0F, 0.0F, 1.0F);
    public static final Vector3dc down = new Vector3d(0.0F, -1.0F, 0.0F);
    public static final Vector3dc up = new Vector3d(0.0F, 1.0F, 0.0F);
    public static final Vector3dc left = new Vector3d(-1.0F, 0.0F, 0.0F);
    public static final Vector3dc right = new Vector3d(1.0F, 0.0F, 0.0F);
    public static Vector3d forward()
    {
        return new Vector3d(forward);
    }
    public static Vector3d backward()
    {
        return new Vector3d(backward);
    }
    public static Vector3d up()
    {
        return new Vector3d(up);
    }
    public static Vector3d down()
    {
        return new Vector3d(down);
    }
    public static Vector3d right()
    {
        return new Vector3d(right);
    }
    public static Vector3d left()
    {
        return new Vector3d(left);
    }

    /**
     * Convert a {@link net.minecraft.world.phys.Vec3} to a {@link Color}.
     * <br>
     * This function is helpful for handling Optifine methods.
     * @apiNote Avoid this function whenever reasonably possible.
     * @see Color
     * @param vector the vector to convert.
     * @param dest the {@link Color} to save to.
     * @return the {@code dest} {@link Color} containing the x, y, and z components of {@code vector} cast to floats.
     */
    public static Color convertToColor(@Nullable net.minecraft.world.phys.Vec3 vector, Color dest)
    {
        return vector != null ? dest.set((float) vector.x, (float) vector.y, (float) vector.z) : null;
    }

    /**
     * Convert a {@link Color} to a {@link net.minecraft.world.phys.Vec3}.
     * This function is helpful for handling Optfine methods.
     * @apiNote Use this function whenever reasonably possible.
     * <br>
     * {@link net.minecraft.world.phys.Vec3} is superseded by {@link Vector3d} in any color context.
     * @see Color
     * @param color the color to convert.
     * @return a new {@link net.minecraft.world.phys.Vec3} whose x, y, and z components are equal to the {@link Color#r}, {@link Color#g}, and {@link Color#b} components of {@code color}.
     */
    public static net.minecraft.world.phys.Vec3 convertToVec3(Color color)
    {
        return new net.minecraft.world.phys.Vec3(color.getR(), color.getG(), color.getB());
    }

   /**
     * Convert a {@link net.minecraft.world.phys.Vec3} to a {@link Vector3d}.
     * @apiNote Use this function whenever reasonably possible.
     * <br>
     * {@link net.minecraft.world.phys.Vec3} is superseded by {@link Vector3d} in any math context.
     * @see Utils#convertToVec3(Vector3dc)
     * @param vector the vector to copy.
     * @return a new Vector3d containing the same x, y, and z components of vector.
     */
    public static Vector3d convertToVector3d(@Nullable net.minecraft.world.phys.Vec3 vector, @Nonnull Vector3d dest)
    {
        return vector != null ? dest.set(vector.x, vector.y, vector.z) : dest;
    }

    /**
     * Convert a {@link net.minecraft.world.phys.Vec3} to a {@link Vector3f}.
     * @apiNote Use this function whenever reasonably possible.
     * <br>
     * @see Utils#convertToVec3(Vector3fc)
     * @param vector the vector to copy.
     * @return a new Vector3f containing the same x, y, and z components of vector.
     */
    public static Vector3f convertToVector3f(net.minecraft.world.phys.Vec3 vector)
    {
        return new Vector3f((float)vector.x, (float)vector.y, (float)vector.z);
    }

    /**
     * Convert a JOML {@link Vector3d} to a {@link net.minecraft.world.phys.Vec3}.
     * @apiNote Avoid this function whenever reasonably possible.
     * <br>
     * If there is an x, y, z signature alternative,
     * <br>
     * instead save {@code vector} and use its {@link Vector3d#x}, {@link Vector3d#y}, and {@link Vector3d#z} directly.
     * @see Utils#convertToVector3d(net.minecraft.world.phys.Vec3, Vector3d)
     * @param vector the vector to copy.
     * @return a new Vec3 containing the same x, y, and z components of vector.
     */
    public static net.minecraft.world.phys.Vec3 convertToVec3(Vector3dc vector)
    {
        return new net.minecraft.world.phys.Vec3(vector.x(), vector.y(), vector.z());
    }

    /**
     * Convert a JOML {@link Vector3f} to a {@link net.minecraft.world.phys.Vec3}.
     * @apiNote Avoid this function whenever reasonably possible.
     * <br>
     * If there is an x, y, z signature alternative,
     * <br>
     * instead save {@code vector} and use its {@link Vector3f#x}, {@link Vector3f#y}, and {@link Vector3f#z} directly.
     * @see Utils#convertToVector3d(net.minecraft.world.phys.Vec3, Vector3d)
     * @param vector the vector to copy.
     * @return a new Vec3 containing the same x, y, and z components of vector.
     */
    public static net.minecraft.world.phys.Vec3 convertToVec3(Vector3fc vector)
    {
        return new net.minecraft.world.phys.Vec3(vector.x(), vector.y(), vector.z());
    }

    /**
     * Convert a row-major 3x4 matrix, like {@link org.lwjgl.openvr.HmdMatrix34}, to a column-major 4x4 matrix.
     * This function is required for org.lwjgl.openvr compatibility,
     * as JOML will only read-in values using column-major layout, whereas
     * {@link org.lwjgl.openvr} <a href="https://github.com/ValveSoftware/openvr/wiki/Matrix-Usage-Example">uses row-major layout.</a>
     * @see Utils#convertRM44ToCM44(DoubleBuffer, Matrix4d)
     */
    public static Matrix4f convertRM34ToCM44(FloatBuffer hmdMatrix34, Matrix4f dest)
    {
        return dest.set(
            hmdMatrix34.get(0), hmdMatrix34.get(4), hmdMatrix34.get(8), 0.0F,
            hmdMatrix34.get(1), hmdMatrix34.get(5), hmdMatrix34.get(9), 0.0F,
            hmdMatrix34.get(2), hmdMatrix34.get(6), hmdMatrix34.get(10), 0.0F,
            hmdMatrix34.get(3), hmdMatrix34.get(7), hmdMatrix34.get(11), 1.0F
        );
    }

    /**
     * Convert a row-major 3x4 matrix, like {@link org.lwjgl.openvr.HmdMatrix34}, to a column-major 4x4 matrix.
     * This function is required for org.lwjgl.openvr compatibility,
     * as JOML will only read-in values using column-major layout, whereas
     * {@link org.lwjgl.openvr} <a href="https://github.com/ValveSoftware/openvr/wiki/Matrix-Usage-Example">uses row-major layout.</a>
     * @see Utils#convertRM34ToCM44(FloatBuffer, Matrix4d)
     */
    public static Matrix4d convertRM34ToCM44(FloatBuffer hmdMatrix34, Matrix4d dest)
    {
        return dest.set(
            hmdMatrix34.get(0), hmdMatrix34.get(4), hmdMatrix34.get(8), 0.0F,
            hmdMatrix34.get(1), hmdMatrix34.get(5), hmdMatrix34.get(9), 0.0F,
            hmdMatrix34.get(2), hmdMatrix34.get(6), hmdMatrix34.get(10), 0.0F,
            hmdMatrix34.get(3), hmdMatrix34.get(7), hmdMatrix34.get(11), 1.0F
        );
    }

    /**
     * Convert a row-major 3x4 matrix to a column-major 4x4 matrix.
     * @see Utils#convertRM44ToCM44(FloatBuffer, Matrix4f)
     */
    public static Matrix4d convertRM34ToCM44(DoubleBuffer doubleBuffer, Matrix4d dest)
    {
        return dest.set(
            doubleBuffer.get(0), doubleBuffer.get(4), doubleBuffer.get(8), 0.0F,
            doubleBuffer.get(1), doubleBuffer.get(5), doubleBuffer.get(9), 0.0F,
            doubleBuffer.get(2), doubleBuffer.get(6), doubleBuffer.get(10), 0.0F,
            doubleBuffer.get(3), doubleBuffer.get(7), doubleBuffer.get(11), 1.0F
        );
    }

    /**
     * Convert a row-major 4x4 matrix, like {@link org.lwjgl.openvr.HmdMatrix44}, to a column-major 4x4 matrix.
     * This function is required for org.lwjgl.openvr compatibility,
     * as JOML will only read-in values using column-major layout, whereas
     * {@link org.lwjgl.openvr} <a href="https://github.com/ValveSoftware/openvr/wiki/Matrix-Usage-Example">uses row-major layout.</a>
     * @see Utils#convertRM34ToCM44(FloatBuffer, Matrix4d)
     */
    public static Matrix4f convertRM44ToCM44(FloatBuffer hmdMatrix44, Matrix4f dest)
    {
        return dest.set(
            hmdMatrix44.get(0), hmdMatrix44.get(4), hmdMatrix44.get(8), hmdMatrix44.get(12),
            hmdMatrix44.get(1), hmdMatrix44.get(5),  hmdMatrix44.get(9), hmdMatrix44.get(13),
            hmdMatrix44.get(2), hmdMatrix44.get(6), hmdMatrix44.get(10), hmdMatrix44.get(14),
            hmdMatrix44.get(3), hmdMatrix44.get(7), hmdMatrix44.get(11), hmdMatrix44.get(15)
        );
    }

    /**
     * Convert a row-major 4x4 matrix to a column-major 4x4 matrix.
     * @see Utils#convertRM34ToCM44(DoubleBuffer, Matrix4d)
     */
    public static Matrix4d convertRM44ToCM44(DoubleBuffer doubleBuffer, Matrix4d dest)
    {
        return dest.set(
            doubleBuffer.get(0), doubleBuffer.get(4), doubleBuffer.get(8), doubleBuffer.get(12),
            doubleBuffer.get(1), doubleBuffer.get(5),  doubleBuffer.get(9), doubleBuffer.get(13),
            doubleBuffer.get(2), doubleBuffer.get(6), doubleBuffer.get(10), doubleBuffer.get(14),
            doubleBuffer.get(3), doubleBuffer.get(7), doubleBuffer.get(11), doubleBuffer.get(15)
        );
    }

    public static AABB getEntityHeadHitbox(Entity entity, double inflate) {
        if ((entity instanceof Player player && !player.isSwimming()) || // swimming players hitbox is just a box around their butt
            entity instanceof Zombie ||
            entity instanceof AbstractPiglin ||
            entity instanceof AbstractSkeleton ||
            entity instanceof Witch ||
            entity instanceof AbstractIllager ||
            entity instanceof Blaze ||
            entity instanceof Creeper ||
            entity instanceof EnderMan ||
            entity instanceof AbstractVillager ||
            entity instanceof SnowGolem ||
            entity instanceof Vex ||
            entity instanceof Strider
        )
        {
            Vector3d headpos = convertToVector3d(entity.getEyePosition(), new Vector3d());
            double headsize = entity.getBbWidth()*0.5;
            if (((LivingEntity) entity).isBaby()) {
                // babies have big heads
                headsize *= 1.20;
            }
            Vector3d minHead = headpos.sub(headsize, headsize-inflate, headsize, new Vector3d());
            Vector3d maxHead = headpos.add(headsize, headsize+inflate, headsize, new Vector3d());

            return new AABB(
                minHead.x, minHead.y, minHead.z,
                maxHead.x, maxHead.y, maxHead.z
            ).inflate(inflate);

        }
        // ender dragon head hitbox is unsuppported since the code doesn't work for it
        else if (!(entity instanceof EnderDragon) && entity instanceof LivingEntity livingEntity)
        {
            float yRot = toRadians(-livingEntity.yBodyRot);
            // offset head in entity rotation
            Vector3d headpos = (convertToVector3d(entity.getEyePosition(), new Vector3d())
                .add(sin(yRot), 0.0D, cos(yRot))
                .mul(livingEntity.getBbWidth() * 0.5D)
            );
            double headsize = livingEntity.getBbWidth() * 0.25D;
            if (livingEntity.isBaby())
            {
                // babies have big heads
                headsize *= 1.5D;
            }
            Vector3d minHead = headpos.sub(headsize, headsize, headsize, new Vector3d());
            Vector3d maxHead = headpos.add(headsize, headsize, headsize, new Vector3d());

            return (
                new AABB(
                    minHead.x, minHead.y, minHead.z,
                    maxHead.x, maxHead.y, maxHead.z
                )
                .inflate(inflate*0.25D)
                .expandTowards(convertToVec3(headpos.sub(convertToVector3d(entity.position(), new Vector3d())).mul(inflate)))
            );
        }
        return null;
    }

    public static boolean canEntityBeSeen(Entity entity, Vector3dc playerEyePos) {
        return entity.level().clip(
            new ClipContext(convertToVec3(playerEyePos), entity.getEyePosition(), Block.COLLIDER, Fluid.NONE, entity)
        ).getType() == Type.MISS;
    }

    /**
     * Vivecraft's logger for printing to console.
     */
    public static final Logger logger = LoggerFactory.getLogger("Vivecraft");

    public static void printStackIfContainsClass(String className)
    {
        StackTraceElement[] astacktraceelement = Thread.currentThread().getStackTrace();
        boolean flag = false;

        for (StackTraceElement stacktraceelement : astacktraceelement)
        {
            if (stacktraceelement.getClassName().equals(className))
            {
                flag = true;
                break;
            }
        }

        if (flag)
        {
            Thread.dumpStack();
        }
    }

    public static long microTime()
    {
        return System.nanoTime() / 1000L;
    }

    public static long milliTime()
    {
        return System.nanoTime() / 1000000L;
    }
}
