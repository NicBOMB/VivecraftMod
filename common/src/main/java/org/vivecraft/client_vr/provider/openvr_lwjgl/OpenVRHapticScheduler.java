package org.vivecraft.client_vr.provider.openvr_lwjgl;

import org.vivecraft.client_vr.provider.ControllerType;
import org.vivecraft.client_vr.provider.HapticScheduler;

import java.util.concurrent.TimeUnit;

import static org.lwjgl.openvr.VRInput.VRInput_TriggerHapticVibrationAction;
import static org.vivecraft.client_vr.VRState.dh;
import static org.vivecraft.common.utils.Utils.logger;

public class OpenVRHapticScheduler extends HapticScheduler {
    private void triggerHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude) {
        int i = VRInput_TriggerHapticVibrationAction(((MCOpenVR) dh.vr).getHapticHandle(controller), 0.0F, durationSeconds, frequency, amplitude, 0L);

        if (i != 0) {
            logger.error("Error triggering haptic: {}", ((MCOpenVR) dh.vr).getInputErrorName(i));
        }
    }

    @Override
    public void queueHapticPulse(ControllerType controller, float durationSeconds, float frequency, float amplitude, float delaySeconds) {
        this.executor.schedule(
            () -> this.triggerHapticPulse(controller, durationSeconds, frequency, amplitude),
            (long) (delaySeconds * 1000000.0F),
            TimeUnit.MICROSECONDS
        );
    }
}
