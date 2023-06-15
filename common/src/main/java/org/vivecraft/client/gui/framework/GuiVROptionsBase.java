package org.vivecraft.client.gui.framework;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.world.phys.Vec2;
import org.vivecraft.client_vr.ClientDataHolderVR;
import org.vivecraft.client_vr.ScreenUtils;
import org.vivecraft.client_vr.settings.VRSettings;

import java.util.ArrayList;
import java.util.List;

public abstract class GuiVROptionsBase extends Screen
{
	protected ClientDataHolderVR dataholder = ClientDataHolderVR.getInstance();
    public static final int DONE_BUTTON = 200;
    public static final int DEFAULTS_BUTTON = 201;
    protected final Screen lastScreen;
    protected final VRSettings settings;
    //private VRTooltipManager tooltipManager = new VRTooltipManager(this, new TooltipProviderVROptions());
    protected boolean reinit;
    protected boolean drawDefaultButtons = true;
    protected ObjectSelectionList visibleList = null;
    private int nextButtonIndex = 0;
    public String vrTitle = "Title";
    private Button btnDone;
    private Button btnDefaults;

    public GuiVROptionsBase(Screen lastScreen)
    {
        super(Component.literal(""));
        this.lastScreen = lastScreen;
        this.settings = ClientDataHolderVR.getInstance().vrSettings;
    }

    protected void addDefaultButtons()
    {
        this.addRenderableWidget(this.btnDone = new Button.Builder(Component.translatable("gui.back"), (p) ->
                    {
                        if (!this.onDoneClicked())
                        {
                            this.dataholder.vrSettings.saveOptions();
                            this.minecraft.setScreen(this.lastScreen);
                        }
                    })
                .pos(this.width / 2 + 5, this.height - 30)
                .size(150, 20)
                .build());
        this.addRenderableWidget(this.btnDefaults = new Button.Builder(Component.translatable("vivecraft.gui.loaddefaults"), (p) ->
                {
                    if (!this.onDoneClicked())
                    {
                        this.dataholder.vrSettings.saveOptions();
                        this.minecraft.setScreen(this.lastScreen);
                    }
                })
                .pos(this.width / 2 - 155, this.height - 30)
                .size(150, 20)
                .build());
    }

    protected boolean onDoneClicked()
    {
        return false;
    }

    protected void loadDefaults()
    {
        for (Renderable renderable : this.renderables) {
            if (!(renderable instanceof GuiVROption))
                continue;

            GuiVROption optionButton = (GuiVROption)renderable;
            this.settings.loadDefault(optionButton.getOption());
        }
    }

    protected void init(VROptionLayout setting, boolean clear)
    {
        if (clear)
        {
            this.clearWidgets();
            this.nextButtonIndex = 0;
        }

        if (this.nextButtonIndex < this.children().size())
        {
            this.nextButtonIndex = this.children().size();
        }

        if (setting.getOption() != null && setting.getOption().getEnumFloat())
        {
            this.addRenderableWidget(new GuiVROptionSlider(setting.getOrdinal(), setting.getX(this.width), setting.getY(this.height), setting.getOption())
            {
                public void onClick(double pMouseX, double p_93635_)
                {
                    if (setting.getCustomHandler() == null || !setting.getCustomHandler().apply(this, new Vec2((float)pMouseX, (float)p_93635_)))
                    {
                        super.onClick(pMouseX, p_93635_);
                    }
                }
            });
        }
        else if (setting.getOption() != null)
        {
            this.addRenderableWidget(new GuiVROptionButton(setting.getOrdinal(), setting.getX(this.width), setting.getY(this.height), setting.getOption(), setting.getButtonText(), (p) ->
            {
                if (setting.getCustomHandler() == null || !setting.getCustomHandler().apply((GuiVROptionButton)p, new Vec2(0.0F, 0.0F)))
                {
                    this.settings.setOptionValue(((GuiVROptionButton)p).getOption());
                    p.setMessage(Component.literal(setting.getButtonText()));
                }
            }));
        }
        else if (setting.getScreen() != null)
        {
            this.addRenderableWidget(new GuiVROptionButton(setting.getOrdinal(), setting.getX(this.width), setting.getY(this.height), setting.getButtonText(), (p) ->
            {
                try {
                    if (setting.getCustomHandler() != null && setting.getCustomHandler().apply((GuiVROptionButton)p, new Vec2(0.0F, 0.0F)))
                    {
                        return;
                    }

                    this.settings.saveOptions();
                    this.minecraft.setScreen(setting.getScreen().getConstructor(Screen.class).newInstance(this));
                }
                catch (ReflectiveOperationException reflectiveoperationexception)
                {
                    reflectiveoperationexception.printStackTrace();
                }
            }));
        }
        else if (setting.getCustomHandler() != null)
        {
            this.addRenderableWidget(new GuiVROptionButton(setting.getOrdinal(), setting.getX(this.width), setting.getY(this.height), setting.getButtonText(), (p) ->
            {
                setting.getCustomHandler().apply((GuiVROptionButton)p, new Vec2(0.0F, 0.0F));
            }));
        }
        else
        {
            this.addRenderableWidget(new GuiVROptionButton(setting.getOrdinal(), setting.getX(this.width), setting.getY(this.height), setting.getButtonText(), (p) ->
            {
            }));
        }
    }

    protected void init(VROptionLayout setting){ this.init(setting, false); }

    protected void init(Class<? extends GuiVROptionsBase> setting, VROptionLayout.Position pos, String title, boolean clear)
    {
        if (clear)
        {
            this.clearWidgets();
            this.nextButtonIndex = 0;
        }

        if (this.nextButtonIndex < this.children().size())
        {
            this.nextButtonIndex = this.children().size();
        }

        if (pos != null && ((pos != VROptionLayout.Position.POS_LEFT && nextButtonIndex % 2 == 0) || (pos == VROptionLayout.Position.POS_LEFT && nextButtonIndex % 2 == 1)))
            ++nextButtonIndex;

        this.init(
                new VROptionLayout(
                        setting,
                        pos != null ? pos : nextButtonIndex % 2 == 0 ? VROptionLayout.Position.POS_LEFT : VROptionLayout.Position.POS_RIGHT,
                        (float) (nextButtonIndex / 2),
                        true,
                        title
                ), clear
        );

        ++nextButtonIndex;
    }

    protected void init(Class<? extends GuiVROptionsBase> setting, VROptionLayout.Position pos, String title){ this.init(setting, pos, title, false); }

    protected void init(VROptionEntry setting, boolean clear)
    {
        if (clear)
        {
            this.clearWidgets();
            this.nextButtonIndex = 0;
        }

        if (this.nextButtonIndex < this.children().size())
        {
            this.nextButtonIndex = this.children().size();
        }

        if (setting.pos != null && ((setting.pos != VROptionLayout.Position.POS_LEFT && nextButtonIndex % 2 == 0) || (setting.pos == VROptionLayout.Position.POS_LEFT && nextButtonIndex % 2 == 1)))
            ++nextButtonIndex;

        VROptionLayout.Position pos = setting.pos != null ? setting.pos : nextButtonIndex % 2 == 0 ? VROptionLayout.Position.POS_LEFT : VROptionLayout.Position.POS_RIGHT;

        if (setting.option != null)
        {
            if (setting.option != VRSettings.VrOptions.DUMMY)
            {
                this.init(new VROptionLayout(setting.option, setting.customHandler, pos, (float)Math.floor((double)((float)nextButtonIndex / 2.0F)), true, setting.title), clear);
            }
        }
        else if (setting.customHandler != null)
        {
            this.init(new VROptionLayout(setting.customHandler, pos, (float)Math.floor((double)((float)nextButtonIndex / 2.0F)), true, setting.title), clear);
        }

        ++nextButtonIndex;
    }

    protected void init(VROptionEntry setting){ this.init(setting, false); }

    protected void init(VRSettings.VrOptions setting, boolean clear)
    {
        if (clear)
        {
            this.clearWidgets();
            this.nextButtonIndex = 0;
        }

        if (this.nextButtonIndex < this.children().size())
        {
            this.nextButtonIndex = this.children().size();
        }

        this.init(new VROptionEntry(setting), false);
    }

    protected void init(VRSettings.VrOptions setting){ this.init(setting, false); }

    protected void init(VROptionLayout[] settings, boolean clear)
    {
        if (clear)
        {
        	this.clearWidgets();
            this.nextButtonIndex = 0;
        }

        if (this.nextButtonIndex < this.children().size())
        {
            this.nextButtonIndex = this.children().size();
        }

        for (final VROptionLayout setting : settings)
        {
            this.init(setting, false);
        }
    }

    protected void init(VROptionLayout[] settings) { this.init(settings, false); }

    protected void init(VROptionEntry[] settings, boolean clear)
    {
        if (clear)
        {
        	this.clearWidgets();
            this.nextButtonIndex = 0;
        }

        if (this.nextButtonIndex < this.children().size())
        {
            this.nextButtonIndex = this.children().size();
        }

        for (final VROptionEntry setting : settings) {
            this.init(setting, false);
        }
    }

    protected void init(VROptionEntry[] settings){ this.init(settings, false); }

    protected void init(VRSettings.VrOptions[] settings, boolean clear)
    {
        if (clear)
        {
            this.clearWidgets();
            this.nextButtonIndex = 0;
        }

        if (this.nextButtonIndex < this.children().size())
        {
            this.nextButtonIndex = this.children().size();
        }

        for (VRSettings.VrOptions setting : settings)
        {
            this.init(setting, false);
        }
    }

    protected void init(VRSettings.VrOptions[] settings){ this.init(settings, false); }

    public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks)
    {
        if (this.reinit)
        {
            this.reinit = false;
            this.init();
        }

        this.renderBackground(pMatrixStack);

        if (this.visibleList != null)
        {
            this.visibleList.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        }

        drawCenteredString(pMatrixStack, this.font, Component.translatable(this.vrTitle), this.width / 2, 15, 16777215);

        if (this.btnDefaults != null)
        {
            this.btnDefaults.visible = this.drawDefaultButtons;
        }

        if (this.btnDone != null)
        {
            this.btnDone.visible = this.drawDefaultButtons;
        }

        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
        renderTooltip(pMatrixStack, pMouseX, pMouseY);
    }

    protected void actionPerformed(AbstractWidget button)
    {
    }

    protected void actionPerformedRightClick(AbstractWidget button)
    {
    }

    public boolean mouseClicked(double pMouseX, double p_94738_, int pMouseY)
    {
        boolean flag = super.mouseClicked(pMouseX, p_94738_, pMouseY);
        AbstractWidget abstractwidget = ScreenUtils.getSelectedButton(this, (int)pMouseX, (int)p_94738_);

        if (abstractwidget != null)
        {
            if (!(abstractwidget instanceof GuiVROptionSlider))
            {
                abstractwidget.playDownSound(this.minecraft.getSoundManager());
            }

            if (pMouseY == 0)
            {
                this.actionPerformed(abstractwidget);
            }
            else if (pMouseY == 1)
            {
                this.actionPerformedRightClick(abstractwidget);
            }
        }
        else if (this.visibleList != null)
        {
            return this.visibleList.mouseClicked(pMouseX, p_94738_, pMouseY);
        }

        return flag;
    }

    public boolean mouseReleased(double pMouseX, double p_94754_, int pMouseY)
    {
        return this.visibleList != null ? this.visibleList.mouseReleased(pMouseX, p_94754_, pMouseY) : super.mouseReleased(pMouseX, p_94754_, pMouseY);
    }

    public boolean mouseDragged(double pMouseX, double p_94741_, int pMouseY, double p_94743_, double pButton)
    {
        return this.visibleList != null ? this.visibleList.mouseDragged(pMouseX, p_94741_, pMouseY, p_94743_, pButton) : super.mouseDragged(pMouseX, p_94741_, pMouseY, p_94743_, pButton);
    }

    public boolean mouseScrolled(double pMouseX, double p_94735_, double pMouseY)
    {
        if (this.visibleList != null)
        {
            this.visibleList.mouseScrolled(pMouseX, p_94735_, pMouseY);
        }

        return super.mouseScrolled(pMouseX, p_94735_, pMouseY);
    }

    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers)
    {
        if (pKeyCode == 256)
        {
            if (!this.onDoneClicked())
            {
                this.dataholder.vrSettings.saveOptions();
                this.minecraft.setScreen(this.lastScreen);
            }

            return true;
        }
        else
        {
            return this.visibleList != null && this.visibleList.keyPressed(pKeyCode, pScanCode, pModifiers) ? true : super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
    }

    public boolean charTyped(char pCodePoint, int pModifiers)
    {
        return this.visibleList != null && this.visibleList.charTyped(pCodePoint, pModifiers) ? true : super.charTyped(pCodePoint, pModifiers);
    }

    private void renderTooltip(PoseStack pMatrixStack, int pMouseX, int pMouseY) {
        AbstractWidget hover = null;
        // find active button
        for (Renderable renderable: renderables) {
            if (renderable instanceof AbstractWidget && ((AbstractWidget) renderable).isMouseOver(pMouseX, pMouseY)) {
                hover = (AbstractWidget) renderable;
            }
        }
        if (hover != null ) {
            if (hover instanceof GuiVROption guiHover) {
                if (guiHover.getOption() != null) {
                    String tooltipString = "vivecraft.options." + guiHover.getOption().name() + ".tooltip";
                    // check if it has a tooltip
                    if (I18n.exists(tooltipString)) {
                        String tooltip = I18n.get(tooltipString, (Object) null);
                        // add format reset at line ends
                        tooltip = tooltip.replace("\n", "§r\n");

                        // make last line the roughly 308 wide
                        List<FormattedText> formattedText = font.getSplitter().splitLines(tooltip, 308, Style.EMPTY);
                        tooltip += " ".repeat((308 - (formattedText.size() == 0 ? 0 : font.width(formattedText.get(formattedText.size() - 1)))) / font.width(" "));

                        // if tooltip is not too low, draw below button, else above
                        if (guiHover.getY() + guiHover.getHeight() + formattedText.size() * (font.lineHeight + 1) + 14 < this.height) {
                            renderTooltip(pMatrixStack, font.split(Component.literal(tooltip), 308), this.width / 2 - 166, guiHover.getY() + guiHover.getHeight() + 14);
                        } else {
                            renderTooltip(pMatrixStack, font.split(Component.literal(tooltip), 308), this.width / 2 - 166, guiHover.getY() - formattedText.size() * (font.lineHeight + 1) + 9);
                        }
                    }
                }
            }
        }
    }
}
