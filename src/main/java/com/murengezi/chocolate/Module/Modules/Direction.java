package com.murengezi.chocolate.Module.Modules;

import com.darkmagician6.eventapi.EventTarget;
import com.murengezi.chocolate.Event.RenderOverlayEvent;
import com.murengezi.chocolate.Chocolate;
import com.murengezi.chocolate.Module.Adjustable;
import com.murengezi.chocolate.Module.ModuleInfo;
import com.murengezi.minecraft.client.gui.GUI;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

/**
 * @author Tobias Sjöblom
 * Created on 2021-01-11 at 21:03
 */

@ModuleInfo(name = "Direction", description = "Shows the direction you are facing.", version = "1.0.0", enabled = true)
public class Direction extends Adjustable {

    @EventTarget
    public void onRender(RenderOverlayEvent event) {
        GUI.drawRect(getX(), getY(), getX() + getWidth(), getY() + getHeight(), Integer.MIN_VALUE);
        setWidth(40f);
        setHeight(40f);
        /*
         * FONT HEIGHT 9.0f
         */
        int color = Chocolate.getThemeManager().getActiveTheme().getColor().getRGB();
        getFr().drawCenteredString((getPlayer().getHorizontalFacing() == EnumFacing.NORTH ? "" : "§7") + "N§r", getX() + (getWidth() / 2) - 0.5f, getY() + 2, color);
        getFr().drawCenteredString((getPlayer().getHorizontalFacing() == EnumFacing.SOUTH ? "" : "§7") + "S§r", getX() + (getWidth() / 2) - 0.5f, getY() + getHeight() - 10, color);
        getFr().drawCenteredString((getPlayer().getHorizontalFacing() == EnumFacing.WEST ? "" : "§7") + "W§r", getX() + 4, getY() + (getHeight() / 2) - 4.0f, color);
        getFr().drawCenteredString((getPlayer().getHorizontalFacing() == EnumFacing.EAST ? "" : "§7") + "E§r", getX() + getWidth() - 6, getY() + (getHeight() / 2) - 4.0f, color);

        GlStateManager.pushMatrix();
        GlStateManager.colorAllMax();
        GlStateManager.translate(getX() + getWidth() / 2f, getY() + getHeight() / 2f, 0);
        GlStateManager.rotate(getPlayer().rotationYaw - 180, 0, 0, 1);
        ResourceLocation mapIcons = new ResourceLocation("textures/map/map_icons.png");
        getMc().getTextureManager().bindTexture(mapIcons);
        GlStateManager.translate(-3.5f, -3.5f, 0);
        GUI.drawModalRectWithCustomSizedTexture(0, 0, 1, 0, 7, 7, 32, 32);
        GlStateManager.popMatrix();
    }

}
