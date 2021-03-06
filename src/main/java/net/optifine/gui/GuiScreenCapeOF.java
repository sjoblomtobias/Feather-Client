package net.optifine.gui;

import com.mojang.authlib.exceptions.InvalidCredentialsException;
import java.math.BigInteger;
import java.net.URI;
import java.util.Random;

import com.murengezi.minecraft.client.gui.GuiButton;
import com.murengezi.minecraft.client.gui.Screen;
import net.minecraft.client.resources.I18n;
import net.optifine.config.Config;
import net.optifine.Lang;

public class GuiScreenCapeOF extends GuiScreenOF {

   private final Screen parentScreen;
   private String title;
   private String message;
   private long messageHideTimeMs;
   private String linkUrl;
   private GuiButtonOF buttonCopyLink;

   public GuiScreenCapeOF(Screen parentScreenIn) {
      this.parentScreen = parentScreenIn;
   }

   public void initGui() {
      int i = 0;
      this.title = I18n.format("of.options.capeOF.title");
      i = i + 2;
      addButton(new GuiButtonOF(210, this.width / 2 - 155, this.height / 6 + 24 * (i >> 1), 150, 20, I18n.format("of.options.capeOF.openEditor")));
      addButton(new GuiButtonOF(220, this.width / 2 - 155 + 160, this.height / 6 + 24 * (i >> 1), 150, 20, I18n.format("of.options.capeOF.reloadCape")));
      i = i + 6;
      this.buttonCopyLink = new GuiButtonOF(230, this.width / 2 - 100, this.height / 6 + 24 * (i >> 1), 200, 20, I18n.format("of.options.capeOF.copyEditorLink"));
      this.buttonCopyLink.setVisible(this.linkUrl != null);
      addButton(this.buttonCopyLink);
      i = i + 4;
      addButton(new GuiButtonOF(200, this.width / 2 - 100, this.height / 6 + 24 * (i >> 1), I18n.format("gui.done")));
   }

   protected void actionPerformed(GuiButton button) {
      if(button.isEnabled()) {
         if(button.getId() == 200) {
            changeScreen(this.parentScreen);
         }

         if(button.getId() == 210) {
            try {
               String s = getMc().getSession().getProfile().getName();
               String s1 = getMc().getSession().getProfile().getId().toString().replace("-", "");
               String s2 = getMc().getSession().getToken();
               Random random = new Random();
               Random random1 = new Random(System.identityHashCode(new Object()));
               BigInteger biginteger = new BigInteger(128, random);
               BigInteger biginteger1 = new BigInteger(128, random1);
               BigInteger biginteger2 = biginteger.xor(biginteger1);
               String s3 = biginteger2.toString(16);
               getMc().getSessionService().joinServer(getMc().getSession().getProfile(), s2, s3);
               String s4 = "https://optifine.net/capeChange?u=" + s1 + "&n=" + s + "&s=" + s3;
               boolean flag = Config.openWebLink(new URI(s4));
               if(flag) {
                  this.showMessage(Lang.get("of.message.capeOF.openEditor"), 10000L);
               } else {
                  this.showMessage(Lang.get("of.message.capeOF.openEditorError"), 10000L);
                  this.setLinkUrl(s4);
               }
            } catch (InvalidCredentialsException invalidcredentialsexception) {
               Config.showGuiMessage(I18n.format("of.message.capeOF.error1"), I18n.format("of.message.capeOF.error2", invalidcredentialsexception.getMessage()));
               Config.warn("Mojang authentication failed");
               Config.warn(invalidcredentialsexception.getClass().getName() + ": " + invalidcredentialsexception.getMessage());
            } catch (Exception exception) {
               Config.warn("Error opening OptiFine cape link");
               Config.warn(exception.getClass().getName() + ": " + exception.getMessage());
            }
         }

         /*
          * TODO Fix cape reload
          */

         if(button.getId() == 220) {
            this.showMessage(Lang.get("of.message.capeOF.reloadCape"), 15000L);
            if(getPlayer() != null) {
               long i = 15000L;
               long j = System.currentTimeMillis() + i;
               getPlayer().setReloadCapeTimeMs(j);
            }
         }

         if(button.getId() == 230 && this.linkUrl != null) {
            setClipboardString(this.linkUrl);
         }
      }
   }

   private void showMessage(String msg, long timeMs) {
      this.message = msg;
      this.messageHideTimeMs = System.currentTimeMillis() + timeMs;
      this.setLinkUrl(null);
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawWorldBackground();
      getFr().drawCenteredString(this.title, this.width / 2, 20, 16777215);
      if(this.message != null) {
         getFr().drawCenteredString(this.message, this.width / 2, this.height / 6 + 60, 16777215);
         if(System.currentTimeMillis() > this.messageHideTimeMs) {
            this.message = null;
            this.setLinkUrl(null);
         }
      }

      super.drawScreen(mouseX, mouseY, partialTicks);
   }

   public void setLinkUrl(String linkUrl) {
      this.linkUrl = linkUrl;
      this.buttonCopyLink.setVisible(linkUrl != null);
   }
}
