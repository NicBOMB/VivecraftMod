package org.vivecraft.client_vr;

import org.joml.Vector3d;
import org.joml.Vector3dc;

import net.minecraft.Util;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Deque;

import static java.util.stream.Stream.generate;

public class Vec3History
{
    private final int _capacity = 450;
    private final Deque<entry> _data = new ArrayDeque<>(_capacity);
    {
        _data.addAll(generate(entry::new).limit(_capacity).toList());
    }

    public void add(double x, double y, double z)
    {
        this._data.addFirst(this._data.removeLast().set(x, y, z));
    }

    public void add(Vector3dc in)
    {
        this.add(in.x(), in.y(), in.z());
    }

    public void add(Vec3 in)
    {
        this.add(in.x(), in.y(), in.z());
    }

    public Vector3d latest(Vector3d dest)
    {
        return dest.set((this._data.getFirst()).data);
    }

    public double totalMovement(double seconds)
    {
        long i = Util.getMillis();
        entry vec3history$entry = null;
        double d0 = 0.0D;

        for (entry vec3history$entry1: this._data)
        {
            if ((double)(i - vec3history$entry1.ts) > seconds * 1000.0D)
            {
                break;
            }

            if (vec3history$entry == null)
            {
                vec3history$entry = vec3history$entry1;
            }
            else
            {
                d0 += vec3history$entry.data.distance(vec3history$entry1.data);
            }
        }

        return d0;
    }

    public Vector3d netMovement(double seconds, Vector3d dest)
    {
        long i = Util.getMillis();
        entry vec3history$entry = null;
        entry vec3history$entry1 = null;

        for (entry vec3history$entry2 : this._data)
        {
            if ((double)(i - vec3history$entry2.ts) > seconds * 1000.0D)
            {
                break;
            }

            if (vec3history$entry == null)
            {
                vec3history$entry = vec3history$entry2;
            }
            else
            {
                vec3history$entry1 = vec3history$entry2;
            }
        }

        return vec3history$entry != null && vec3history$entry1 != null ? vec3history$entry.data.sub(vec3history$entry1.data, dest) : dest.set(0);
    }

    public double averageSpeed(double seconds)
    {
        long i = Util.getMillis();
        double d0 = 0.0D;
        entry vec3history$entry = null;
        int j = 0;

        for (entry vec3history$entry1 : this._data)
        {
            if ((double)(i - vec3history$entry1.ts) > seconds * 1000.0D)
            {
                break;
            }

            if (vec3history$entry == null)
            {
                vec3history$entry = vec3history$entry1;
            }
            else
            {
                ++j;
                double d1 = 0.001D * (vec3history$entry.ts - vec3history$entry1.ts);
                double d2 = vec3history$entry.data.distance(vec3history$entry1.data);
                d0 += d2 / d1;
            }
        }

        return j == 0 ? d0 : d0 / j;
    }

    public Vector3d averagePosition(double seconds, Vector3d dest)
    {
        long i = Util.getMillis();
        int j = 0;

        for (entry vec3history$entry: this._data)
        {
            if ((double)(i - vec3history$entry.ts) > seconds * 1000.0D)
            {
                break;
            }

            dest.add(vec3history$entry.data);
            ++j;
        }

        return j == 0 ? dest : dest.mul(1.0D / j);
    }

    private static class entry
    {
        public long ts = Util.getMillis();
        public Vector3d data = new Vector3d();

        public entry set(double x, double y, double z)
        {
            this.ts = Util.getMillis();
            this.data.set(x, y, z);
            return this;
        }
    }
}
