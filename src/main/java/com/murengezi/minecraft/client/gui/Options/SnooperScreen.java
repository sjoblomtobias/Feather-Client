package com.murengezi.minecraft.client.gui.Options;

import com.murengezi.minecraft.client.gui.GuiButton;
import com.murengezi.minecraft.client.gui.Screen;
import com.murengezi.minecraft.client.gui.GUI;
import com.murengezi.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * @author Tobias Sjöblom
 * Created on 2021-01-21 at 11:39
 */
public class SnooperScreen extends Screen {

    private final Screen previousScreen;

    /** Reference to the GameSettings object. */
    private final java.util.List<String> clientSnooper = new ArrayList<>();
    private final java.util.List<String> serverSnooper = new ArrayList<>();
    private List<String> description;
    private SnooperSlot snooperSlot;

    private static final int SNOOPER = 0, DONE = 1;

    public SnooperScreen(Screen previousScreen) {
        this.previousScreen = previousScreen;
    }

    @Override
    public void initGui() {
        String format = I18n.format("options.snooper.desc");
        description = new ArrayList<>(getFr().listFormattedStringToWidth(format, this.width - 30));
        this.clientSnooper.clear();
        this.serverSnooper.clear();
        addButton(new GuiButton(SNOOPER, this.width / 2 - 152, this.height - 30, 150, 20, getGs().getKeyBinding(GameSettings.Options.SNOOPER_ENABLED)));
        addButton(new GuiButton(DONE, this.width / 2 + 2, this.height - 30, 150, 20, I18n.format("gui.done")));
        boolean multiplayer = getMc().getIntegratedServer() != null && getMc().getIntegratedServer().getPlayerUsageSnooper() != null;

        (new TreeMap<>(getMc().getPlayerUsageSnooper().getCurrentStats())).forEach((key, value) -> {
            this.clientSnooper.add((multiplayer ? "C " : "") + key);
            this.serverSnooper.add(getFr().trimStringToWidth(value, this.width - 220));
        });

        if (multiplayer) {
            (new TreeMap<>(getMc().getIntegratedServer().getPlayerUsageSnooper().getCurrentStats())).forEach((key, value) -> {
                this.clientSnooper.add("S " + key);
                this.serverSnooper.add(getFr().trimStringToWidth(value, this.width - 220));
            });
        }

        this.snooperSlot = new SnooperSlot();
        super.initGui();
    }

    @Override
    public void handleMouseInput() throws IOException {
        this.snooperSlot.handleMouseInput();
        super.handleMouseInput();
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.isEnabled()) {
            switch (button.getId()) {
                case SNOOPER:
                    getGs().setOptionValue(GameSettings.Options.SNOOPER_ENABLED, 1);
                    getButton(SNOOPER).displayString = getGs().getKeyBinding(GameSettings.Options.SNOOPER_ENABLED);
                    break;
                case DONE:
                    saveSettings();
                    changeScreen(previousScreen);
                    break;
            }
        }
        super.actionPerformed(button);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawWorldBackground(mouseX, mouseY, 60);

        GlStateManager.pushMatrix();
        scissorBox(snooperSlot.getLeft(), snooperSlot.getTop(), snooperSlot.getRight(), snooperSlot.getBottom(), new ScaledResolution());
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        snooperSlot.drawScreen(mouseX, mouseY, partialTicks);
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.popMatrix();

        GUI.drawRect(0, 0, snooperSlot.getWidth(), snooperSlot.getTop(), Integer.MIN_VALUE);
        GUI.drawRect(0, snooperSlot.getBottom(), snooperSlot.getWidth(), this.height, Integer.MIN_VALUE);

        this.drawCenteredString(getFr(), I18n.format("options.snooper.title"), this.width / 2, 8, 16777215);

        description.forEach(line -> this.drawCenteredString(getFr(), line, this.width / 2, 22 + (description.indexOf(line) * getFr().FONT_HEIGHT), 8421504));

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    class SnooperSlot extends GuiSlot {

        public SnooperSlot() {
            super(SnooperScreen.this.width, SnooperScreen.this.height, 80, SnooperScreen.this.height - 40, SnooperScreen.getFr().FONT_HEIGHT + 1);
        }

        @Override
        protected int getSize() {
            return SnooperScreen.this.clientSnooper.size();
        }

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            return false;
        }

        @Override
        protected void drawBackground() {}

        @Override
        protected void drawSlot(int entryID, int x, int y, int p_180791_4_, int mouseXIn, int mouseYIn) {
            SnooperScreen.getFr().drawString(SnooperScreen.this.clientSnooper.get(entryID), 10, y, 16777215);
            SnooperScreen.getFr().drawString(SnooperScreen.this.serverSnooper.get(entryID), 230, y, 16777215);
        }

        @Override
        protected int getScrollBarX() {
            return this.width - 10;
        }
    }
}
