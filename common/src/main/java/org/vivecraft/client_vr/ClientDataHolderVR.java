package org.vivecraft.client_vr;

import net.minecraft.client.resources.model.ModelResourceLocation;
import org.vivecraft.client_vr.gameplay.VRPlayer;
import org.vivecraft.client_vr.gameplay.trackers.*;
import org.vivecraft.client_vr.menuworlds.MenuWorldRenderer;
import org.vivecraft.client_vr.provider.MCVR;
import org.vivecraft.client_vr.provider.VRRenderer;
import org.vivecraft.client_vr.render.RenderPass;
import org.vivecraft.client_vr.settings.VRSettings;

public class ClientDataHolderVR {

    public static boolean kiosk;
    public static boolean ismainhand;
    public static boolean katvr;
    public static boolean infinadeck;
    public static boolean viewonly;
    public static ModelResourceLocation thirdPersonCameraModel = new ModelResourceLocation("vivecraft", "camcorder", "");
    public static ModelResourceLocation thirdPersonCameraDisplayModel = new ModelResourceLocation("vivecraft", "camcorder_display", "");
    public static VRPlayer vrPlayer;
    public static MCVR vr;
    public static VRRenderer vrRenderer;
    public static MenuWorldRenderer menuWorldRenderer;
    public static BackpackTracker backpackTracker = new BackpackTracker();
    public static BowTracker bowTracker = new BowTracker();
    public static SwimTracker swimTracker = new SwimTracker();
    public static EatingTracker autoFood = new EatingTracker();
    public static JumpTracker jumpTracker = new JumpTracker();
    public static SneakTracker sneakTracker = new SneakTracker();
    public static ClimbTracker climbTracker = new ClimbTracker();
    public static RunTracker runTracker = new RunTracker();
    public static RowTracker rowTracker = new RowTracker();
    public static TeleportTracker teleportTracker = new TeleportTracker();
    public static SwingTracker swingTracker = new SwingTracker();
    public static HorseTracker horseTracker = new HorseTracker();
    public static VehicleTracker vehicleTracker = new VehicleTracker();
    public static InteractTracker interactTracker = new InteractTracker();
    public static CrawlTracker crawlTracker = new CrawlTracker();
    public static CameraTracker cameraTracker = new CameraTracker();
    public static VRSettings vrSettings;
    public static boolean integratedServerLaunchInProgress = false;
    public static boolean grabScreenShot = false;
    public static long frameIndex = 0L;
    public static RenderPass currentPass;
    public static int tickCounter;
    public static float watereffect;
    public static float portaleffect;
    public static float pumpkineffect;
    public static boolean isfphand;
    public static boolean isFirstPass;
    static long mirroNotifyStart;
    static String mirrorNotifyText;
    static boolean mirrorNotifyClear;
    static long mirroNotifyLen;

    // showed chat notifications
    public static boolean showedUpdateNotification;

    public static boolean skipStupidGoddamnChunkBoundaryClipping;


    private ClientDataHolderVR() {}

    public static void printChatMessage(String string) {
        // TODO Auto-generated method stub

    }

    public static void print(String string) {
        string = string.replace("\n", "\n[Minecrift] ");
        System.out.println("[Minecrift] " + string);
    }
}
