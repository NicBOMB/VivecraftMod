package org.vivecraft.client_vr.gameplay.trackers;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.world.entity.Pose;
import org.vivecraft.client.Xplat;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.VRState;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.mod_compat_vr.pehkui.PehkuiHelper;

public class CrawlTracker extends Tracker {
    private boolean wasCrawling;
    public boolean crawling;
    public boolean crawlsteresis;

    public boolean isActive(LocalPlayer player) {
        if (ClientDataHolderVR.vrSettings.seated) {
            return false;
        } else if (!ClientDataHolderVR.vrSettings.allowCrawling) {
            return false;
        } else if (!ClientNetworking.serverAllowsCrawling) {
            return false;
        } else if (!player.isAlive()) {
            return false;
        } else if (player.isSpectator()) {
            return false;
        } else if (player.isSleeping()) {
            return false;
        } else {
            return !player.isPassenger();
        }
    }

    public void reset(LocalPlayer player) {
        this.crawling = false;
        this.crawlsteresis = false;
        this.updateState(player);
    }

    public void doProcess(LocalPlayer player) {
        double scaleMultiplier = 1.0;
        if (Xplat.isModLoaded("pehkui")) {
            scaleMultiplier /= PehkuiHelper.getPlayerScale(player, VRState.mc.getFrameTime());
        }
        this.crawling = ClientDataHolderVR.vr.hmdPivotHistory.averagePosition(0.2F).y * (double) ClientDataHolderVR.vrPlayer.worldScale * scaleMultiplier + (double) 0.1F < (double) ClientDataHolderVR.vrSettings.crawlThreshold;
        this.updateState(player);
    }

    private void updateState(LocalPlayer player) {
        if (this.crawling != this.wasCrawling) {
            if (this.crawling) {
                player.setPose(Pose.SWIMMING);
                this.crawlsteresis = true;
            }

            if (ClientNetworking.serverAllowsCrawling) {
                ServerboundCustomPayloadPacket serverboundcustompayloadpacket = ClientNetworking.getVivecraftClientPacket(CommonNetworkHelper.PacketDiscriminators.CRAWL, new byte[]{(byte) (this.crawling ? 1 : 0)});

                if (VRState.mc.getConnection() != null) {
                    VRState.mc.getConnection().send(serverboundcustompayloadpacket);
                }
            }

            this.wasCrawling = this.crawling;
        }

        if (!this.crawling && player.getPose() != Pose.SWIMMING) {
            this.crawlsteresis = false;
        }
    }
}
