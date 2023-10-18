package org.vivecraft.client_vr;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;
import org.vivecraft.client.gui.screens.ErrorScreen;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.menuworlds.MenuWorldRenderer;
import org.vivecraft.client_vr.provider.nullvr.NullVR;
import org.vivecraft.client_vr.provider.openvr_lwjgl.MCOpenVR;
import org.vivecraft.client_vr.render.RenderConfigException;
import org.vivecraft.client_vr.settings.VRSettings;
import org.vivecraft.client_xr.render_pass.RenderPassManager;
import org.vivecraft.mod_compat_vr.optifine.OptifineHelper;

public class VRState {

    @NotNull
    public static Minecraft mc = Minecraft.getInstance();

    public static boolean vrRunning = false;
    public static boolean vrEnabled = false;
    public static boolean vrInitialized = false;

    public static void initializeVR() {
        if (vrInitialized) {
            return;
        }
        try {
            if (OptifineHelper.isOptifineLoaded() && OptifineHelper.isAntialiasing()) {
                throw new RenderConfigException(Component.translatable("vivecraft.messages.incompatiblesettings").getString(), Component.translatable("vivecraft.messages.optifineaa"));
            }

            vrInitialized = true;
            if (ClientDataHolderVR.vrSettings.stereoProviderPluginID == VRSettings.VRProvider.OPENVR) {
                ClientDataHolderVR.vr = new MCOpenVR();
            } else {
                ClientDataHolderVR.vr = new NullVR();
            }
            if (!ClientDataHolderVR.vr.init()) {
                throw new RenderConfigException("VR init Error", Component.translatable("vivecraft.messages.rendersetupfailed", ClientDataHolderVR.vr.initStatus + "\nVR provider: " + ClientDataHolderVR.vr.getName()));
            }

            ClientDataHolderVR.vrRenderer = ClientDataHolderVR.vr.createVRRenderer();
            ClientDataHolderVR.vrRenderer.lastGuiScale = mc.options.guiScale().get();
            try {
                ClientDataHolderVR.vrRenderer.setupRenderConfiguration();
                RenderPassManager.setVanillaRenderPass();
            } catch (RenderConfigException renderConfigException) {
                throw new RenderConfigException("VR Render Error", Component.translatable("vivecraft.messages.rendersetupfailed", renderConfigException.error.getString() + "\nVR provider: " + ClientDataHolderVR.vr.getName()));
            } catch (Exception e) {
                e.printStackTrace();
            }

            ClientDataHolderVR.vrPlayer = new VRPlayer();
            ClientDataHolderVR.vrPlayer.registerTracker(ClientDataHolderVR.backpackTracker);
            ClientDataHolderVR.vrPlayer.registerTracker(ClientDataHolderVR.bowTracker);
            ClientDataHolderVR.vrPlayer.registerTracker(ClientDataHolderVR.climbTracker);
            ClientDataHolderVR.vrPlayer.registerTracker(ClientDataHolderVR.autoFood);
            ClientDataHolderVR.vrPlayer.registerTracker(ClientDataHolderVR.jumpTracker);
            ClientDataHolderVR.vrPlayer.registerTracker(ClientDataHolderVR.rowTracker);
            ClientDataHolderVR.vrPlayer.registerTracker(ClientDataHolderVR.runTracker);
            ClientDataHolderVR.vrPlayer.registerTracker(ClientDataHolderVR.sneakTracker);
            ClientDataHolderVR.vrPlayer.registerTracker(ClientDataHolderVR.swimTracker);
            ClientDataHolderVR.vrPlayer.registerTracker(ClientDataHolderVR.swingTracker);
            ClientDataHolderVR.vrPlayer.registerTracker(ClientDataHolderVR.interactTracker);
            ClientDataHolderVR.vrPlayer.registerTracker(ClientDataHolderVR.teleportTracker);
            ClientDataHolderVR.vrPlayer.registerTracker(ClientDataHolderVR.horseTracker);
            ClientDataHolderVR.vrPlayer.registerTracker(ClientDataHolderVR.vehicleTracker);
            ClientDataHolderVR.vrPlayer.registerTracker(ClientDataHolderVR.crawlTracker);
            ClientDataHolderVR.vrPlayer.registerTracker(ClientDataHolderVR.cameraTracker);

            ClientDataHolderVR.vr.postinit();

            ClientDataHolderVR.menuWorldRenderer = new MenuWorldRenderer();

            ClientDataHolderVR.menuWorldRenderer.init();
        } catch (RenderConfigException renderConfigException) {
            vrEnabled = false;
            destroyVR(true);
            mc.setScreen(new ErrorScreen(renderConfigException.title, renderConfigException.error));
        }
    }

    public static void startVR() {
        GLFW.glfwSwapInterval(0);
    }

    public static void destroyVR(boolean disableVRSetting) {
        if (ClientDataHolderVR.vr != null) {
            ClientDataHolderVR.vr.destroy();
        }
        ClientDataHolderVR.vr = null;
        ClientDataHolderVR.vrPlayer = null;
        if (ClientDataHolderVR.vrRenderer != null) {
            ClientDataHolderVR.vrRenderer.destroy();
        }
        ClientDataHolderVR.vrRenderer = null;
        if (ClientDataHolderVR.menuWorldRenderer != null) {
            ClientDataHolderVR.menuWorldRenderer.completeDestroy();
            ClientDataHolderVR.menuWorldRenderer = null;
        }
        vrEnabled = false;
        vrInitialized = false;
        vrRunning = false;
        if (disableVRSetting) {
            ClientDataHolderVR.vrSettings.vrEnabled = false;
            ClientDataHolderVR.vrSettings.saveOptions();
        }
    }

    public static void pauseVR() {
        //        GLFW.glfwSwapInterval(bl ? 1 : 0);
    }
}
