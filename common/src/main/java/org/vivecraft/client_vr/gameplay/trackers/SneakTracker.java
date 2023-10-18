package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.player.LocalPlayer;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.client_vr.settings.AutoCalibration;

public class SneakTracker extends Tracker {
    public boolean sneakOverride = false;
    public int sneakCounter = 0;

    public boolean isActive(LocalPlayer p) {
        if (ClientDataHolderVR.vrSettings.seated) {
            return false;
        } else if (!ClientDataHolderVR.vrPlayer.getFreeMove() && !ClientDataHolderVR.vrSettings.simulateFalling) {
            return false;
        } else if (!ClientDataHolderVR.vrSettings.realisticSneakEnabled) {
            return false;
        } else if (VRState.mc.gameMode == null) {
            return false;
        } else if (p != null && p.isAlive() && p.onGround()) {
            return !p.isPassenger();
        } else {
            return false;
        }
    }

    public void reset(LocalPlayer player) {
        this.sneakOverride = false;
    }

    public void doProcess(LocalPlayer player) {
        if (!VRState.mc.isPaused() && ClientDataHolderVR.sneakTracker.sneakCounter > 0) {
            --ClientDataHolderVR.sneakTracker.sneakCounter;
        }

        this.sneakOverride = (double) AutoCalibration.getPlayerHeight() - ClientDataHolderVR.vr.hmdPivotHistory.latest().y > (double) ClientDataHolderVR.vrSettings.sneakThreshold;
    }
}
