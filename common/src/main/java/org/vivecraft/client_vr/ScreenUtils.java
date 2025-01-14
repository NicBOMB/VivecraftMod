package org.vivecraft.client_vr;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

import static org.vivecraft.client_vr.VRState.dh;

public class ScreenUtils {

    //Vivecraft / OF
    public static List<AbstractWidget> getButtonList(Screen screen) {
        List<AbstractWidget> list = new ArrayList<>();

        for (GuiEventListener guieventlistener : screen.children()) {
            if (guieventlistener instanceof AbstractWidget) {
                list.add((AbstractWidget) guieventlistener);
            }
        }

        return list;
    }

    public static AbstractWidget getSelectedButton(Screen screen, int x, int y) {
        for (AbstractWidget butt : getButtonList(screen)) {
            if (butt.visible && butt.isHoveredOrFocused()) {
                return butt;
            }
        }

        return null;
    }

    public static int getBGFrom() {
        if (dh.vrSettings == null || dh.vrSettings.menuBackground) {
            return -1072689136;
        }
        return 0;
    }

    public static int getBGTo() {
        if (dh.vrSettings == null || dh.vrSettings.menuBackground) {
            return -804253680;
        }
        return 0;
    }

    public static AbstractWidget getSelectedButton(int x, int y, List<AbstractWidget> listButtons) {
        for (AbstractWidget abstractwidget : listButtons) {
            if (abstractwidget.visible) {
                int j = abstractwidget.getWidth();
                int k = abstractwidget.getHeight();

                if (x >= abstractwidget.getX() && y >= abstractwidget.getY() && x < abstractwidget.getX() + j && y < abstractwidget.getY() + k) {
                    return abstractwidget;
                }
            }
        }

        return null;
    }
    //
}
