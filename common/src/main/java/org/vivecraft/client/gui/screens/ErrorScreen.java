package org.vivecraft.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button.Builder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.vivecraft.client.gui.widgets.TextScrollWidget;

import javax.annotation.Nonnull;

import static org.vivecraft.client_vr.VRState.mc;


public class ErrorScreen extends Screen {

    private final Screen lastScreen;
    private final Component error;

    public ErrorScreen(String title, Component error) {
        super(Component.literal(title));
        lastScreen = mc.screen;
        this.error = error;
    }

    @Override
    protected void init() {

        this.addRenderableWidget(new TextScrollWidget(this.width / 2 - 155, 30, 310, this.height - 30 - 36, error));

        this.addRenderableWidget(new Builder(Component.translatable("gui.back"), (p) ->
            mc.setScreen(this.lastScreen)).pos(this.width / 2 + 5, this.height - 32).size(150, 20).build()
        );
        this.addRenderableWidget(new Builder(Component.translatable("chat.copy"), (p) ->
            mc.keyboardHandler.setClipboard(error.getString())).pos(this.width / 2 - 155, this.height - 32)
            .size(150, 20)
            .build()
        );
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int i, int j, float f) {
        this.renderBackground(guiGraphics, i, j, f);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 16777215);

        super.render(guiGraphics, i, j, f);
    }
}
