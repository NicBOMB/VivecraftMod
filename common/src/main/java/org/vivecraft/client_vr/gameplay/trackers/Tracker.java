package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.player.LocalPlayer;

public abstract class Tracker {

    public abstract boolean isActive(LocalPlayer var1);

    public abstract void doProcess(LocalPlayer var1);

    public void reset(LocalPlayer player) {
    }

    public void idleTick(LocalPlayer player) {
    }

    public EntryPoint getEntryPoint() {
        return EntryPoint.LIVING_UPDATE;
    }

    public enum EntryPoint {
        LIVING_UPDATE,
        SPECIAL_ITEMS
    }
}
