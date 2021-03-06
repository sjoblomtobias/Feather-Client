package com.murengezi.chocolate.Util;

import com.murengezi.minecraft.potion.Potion;
import net.minecraft.client.Minecraft;
import com.murengezi.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.settings.GameSettings;

/**
 * @author Tobias Sjöblom
 * Created on 2021-01-10 at 00:06
 */
public class MinecraftUtils {

    public static Minecraft getMc() {
        return Minecraft.getMinecraft();
    }

    public static EntityPlayerSP getPlayer() {
        return getMc().player;
    }

    public static WorldClient getWorld() {
        return getMc().world;
    }

    public static FontRenderer getFr() {
        return getMc().fontRenderer;
    }

    public static GameSettings getGs() {
        return getMc().gameSettings;
    }

    public static LanguageManager getLm() {
        return getMc().getLanguageManager();
    }

    public static boolean canPlayerSprint() {
        return !getPlayer().isSprinting()
                && !getPlayer().isSneaking()
                && !getPlayer().isCollidedHorizontally
                && !getPlayer().isBlocking()
                && !getPlayer().isDead
                && !getPlayer().isRiding()
                && !getPlayer().isUsingItem()
                && !getPlayer().isPotionActive(Potion.blindness)
                && getPlayer().getFoodStats().getFoodLevel() > 6
                && getPlayer().moveForward > 0.0f;
    }

}
