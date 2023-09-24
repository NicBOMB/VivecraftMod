package org.vivecraft.mixin.client_vr.multiplayer;

import org.vivecraft.client.VRPlayersClient;
import org.vivecraft.client.network.ClientNetworking;
import org.vivecraft.client_vr.gameplay.screenhandlers.GuiHandler;
import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.settings.VRSettings.ChatNotifications;
import org.vivecraft.common.VRServerPerms;
import org.vivecraft.common.network.CommonNetworkHelper;
import org.vivecraft.common.network.CommonNetworkHelper.PacketDiscriminators;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.telemetry.WorldSessionTelemetryManager;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import static org.vivecraft.client_vr.VRState.*;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(net.minecraft.client.multiplayer.ClientPacketListener.class)
public class ClientPacketListenerVRMixin {

    @Inject(at = @At("TAIL"), method = "<init>")
    public void init(net.minecraft.client.Minecraft minecraft, Screen screen, Connection connection, ServerData serverData, GameProfile gameProfile, WorldSessionTelemetryManager worldSessionTelemetryManager, CallbackInfo ci) {
        if (ClientNetworking.needsReset) {
            dh.vrSettings.overrides.resetAll();
            ClientNetworking.resetServerSettings();
            ClientNetworking.displayedChatMessage = false;
            ClientNetworking.displayedChatWarning = false;
            ClientNetworking.needsReset = false;
        }
    }

    @Inject(at = @At("TAIL"), method = "handleLogin(Lnet/minecraft/network/protocol/game/ClientboundLoginPacket;)V")
    public void login(ClientboundLoginPacket p_105030_, CallbackInfo callback) {
        VRPlayersClient.clear();
        ClientNetworking.sendVersionInfo();

        if (vrInitialized) {
            // set the timer, even if vr is currently not running
            dh.vrPlayer.chatWarningTimer = 200;
            dh.vrPlayer.teleportWarning = true;
            dh.vrPlayer.vrSwitchWarning = false;
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/telemetry/WorldSessionTelemetryManager;onPlayerInfoReceived(Lnet/minecraft/world/level/GameType;Z)V"), method = "handleLogin(Lnet/minecraft/network/protocol/game/ClientboundLoginPacket;)V")
    public void noTelemetry(WorldSessionTelemetryManager instance, GameType gameType, boolean bl) {
        // TODO, should we still cancel that in NONVR?
        return;
    }

    @Inject(at = @At("TAIL"), method = "onDisconnect")
    public void disconnect(Component component, CallbackInfo ci) {
        VRServerPerms.setTeleportSupported(false);
        if (vrInitialized) {
            dh.vrPlayer.setTeleportOverride(false);
        }
        dh.vrSettings.overrides.resetAll();
    }

    @Inject(at = @At("TAIL"), method = "close")
    public void cleanup(CallbackInfo ci) {
        ClientNetworking.needsReset = true;
    }
    @Unique String lastMsg;
    @Inject(at = @At("TAIL"), method = "sendChat")
    public void chatMsg(String string, CallbackInfo ci) {
        this.lastMsg = string;
    }

    @Inject(at = @At("TAIL"), method = "sendCommand")
    public void commandMsg(String string, CallbackInfo ci) {
        this.lastMsg = string;
    }

    @Inject(at = @At("TAIL"), method = "handlePlayerChat")
    public void chat(ClientboundPlayerChatPacket clientboundPlayerChatPacket, CallbackInfo ci) {
        if (vrRunning && (mc.player == null || this.lastMsg == null || clientboundPlayerChatPacket.sender() == mc.player.getUUID())) {
            this.triggerHapticSound();
        }
        this.lastMsg = null;
    }

    @Inject(at = @At("TAIL"), method = "handleSystemChat")
    public void chatSystem(ClientboundSystemChatPacket clientboundSystemChatPacket, CallbackInfo ci) {
        if (vrRunning && (mc.player == null || this.lastMsg == null || clientboundSystemChatPacket.content().getString().contains(this.lastMsg))) {
            this.triggerHapticSound();
        }
        this.lastMsg = null;
    }

    @Unique
    private void triggerHapticSound(){
        if (dh.vrSettings.chatNotifications != ChatNotifications.NONE) {
            if ((dh.vrSettings.chatNotifications == ChatNotifications.HAPTIC || dh.vrSettings.chatNotifications == ChatNotifications.BOTH) && !dh.vrSettings.seated) {
                dh.vr.triggerHapticPulse(ControllerType.LEFT, 0.2F, 1000.0F, 1.0F);}

            if (dh.vrSettings.chatNotifications == ChatNotifications.SOUND || dh.vrSettings.chatNotifications == ChatNotifications.BOTH) {
                Vec3 vec3 = dh.vrPlayer.vrdata_world_pre.getController(1).getPosition();
                mc.level.playLocalSound(vec3.x(), vec3.y(), vec3.z(), BuiltInRegistries.SOUND_EVENT.get(new ResourceLocation(dh.vrSettings.chatNotificationSound)), SoundSource.NEUTRAL, 0.3F, 0.1F, false);
            }
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;adjustPlayer(Lnet/minecraft/world/entity/player/Player;)V", shift = Shift.BEFORE), method = "handleRespawn")
    public void readdInput2(ClientboundRespawnPacket clientboundRespawnPacket, CallbackInfo ci) {
        ClientNetworking.resetServerSettings();
        ClientNetworking.sendVersionInfo();
        if (vrInitialized) {
            // set the timer, even if vr is currently not running
            dh.vrPlayer.chatWarningTimer = 200;
            dh.vrPlayer.teleportWarning = true;
            dh.vrPlayer.vrSwitchWarning = false;
        }
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", ordinal = 0, shift = Shift.AFTER), method = "handleRespawn(Lnet/minecraft/network/protocol/game/ClientboundRespawnPacket;)V")
    public void respawn(ClientboundRespawnPacket packet, CallbackInfo callback) {
        dh.vrSettings.overrides.resetAll();
    }

    @Inject(at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/network/protocol/game/ClientboundCustomPayloadPacket;getData()Lnet/minecraft/network/FriendlyByteBuf;"), method = "handleCustomPayload(Lnet/minecraft/network/protocol/game/ClientboundCustomPayloadPacket;)V", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
	public void handlepacket(ClientboundCustomPayloadPacket packet, CallbackInfo info, ResourceLocation channelID, FriendlyByteBuf buffer) {
        if (channelID.equals(CommonNetworkHelper.CHANNEL)) {
            var packetID = PacketDiscriminators.values()[buffer.readByte()];
            ClientNetworking.handlePacket(packetID, buffer);
            buffer.release();
            info.cancel();
        }
	}

    @Inject(at = @At("HEAD"), method = "handleOpenScreen")
    public void markScreenActive(ClientboundOpenScreenPacket clientboundOpenScreenPacket, CallbackInfo ci) {
        GuiHandler.guiAppearOverBlockActive = true;
    }
}
