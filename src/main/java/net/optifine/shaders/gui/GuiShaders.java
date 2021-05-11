package net.optifine.shaders.gui;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import com.murengezi.minecraft.client.gui.GuiButton;
import com.murengezi.minecraft.client.gui.Screen;
import net.minecraft.client.resources.I18n;
import com.murengezi.minecraft.client.settings.GameSettings;
import net.optifine.config.Config;
import net.optifine.Lang;
import net.optifine.gui.GuiScreenOF;
import net.optifine.gui.TooltipManager;
import net.optifine.gui.TooltipProviderEnumShaderOptions;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.ShadersTex;
import net.optifine.shaders.config.EnumShaderOption;
import org.lwjgl.Sys;

public class GuiShaders extends GuiScreenOF {
   protected Screen parentGui;
   protected String screenTitle = "Shaders";
   private final TooltipManager tooltipManager = new TooltipManager(this, new TooltipProviderEnumShaderOptions());
   private int updateTimer = -1;
   private GuiSlotShaders shaderList;
   private boolean saved = false;
   private static final float[] QUALITY_MULTIPLIERS = new float[]{0.5F, 0.6F, 0.6666667F, 0.75F, 0.8333333F, 0.9F, 1.0F, 1.1666666F, 1.3333334F, 1.5F, 1.6666666F, 1.8F, 2.0F};
   private static final String[] QUALITY_MULTIPLIER_NAMES = new String[]{"0.5x", "0.6x", "0.66x", "0.75x", "0.83x", "0.9x", "1x", "1.16x", "1.33x", "1.5x", "1.66x", "1.8x", "2x"};
   private static final float QUALITY_MULTIPLIER_DEFAULT = 1.0F;
   private static final float[] HAND_DEPTH_VALUES = new float[]{0.0625F, 0.125F, 0.25F};
   private static final String[] HAND_DEPTH_NAMES = new String[]{"0.5x", "1x", "2x"};
   private static final float HAND_DEPTH_DEFAULT = 0.125F;
   public static final int EnumOS_UNKNOWN = 0;
   public static final int EnumOS_WINDOWS = 1;
   public static final int EnumOS_OSX = 2;
   public static final int EnumOS_SOLARIS = 3;
   public static final int EnumOS_LINUX = 4;

   public GuiShaders(Screen par1GuiScreen, GameSettings par2GameSettings) {
      this.parentGui = par1GuiScreen;
   }

   public void initGui() {
      this.screenTitle = I18n.format("of.options.shadersTitle");
      if(Shaders.shadersConfig == null) {
         Shaders.loadConfig();
      }

      int i = 120;
      int j = 20;
      int k = this.width - i - 10;
      int l = 30;
      int i1 = 20;
      int j1 = this.width - i - 20;
      this.shaderList = new GuiSlotShaders(this, j1, this.height, l, this.height - 50, 16);
      this.shaderList.registerScrollButtons(7, 8);
      addButton(new GuiButtonEnumShaderOption(EnumShaderOption.ANTIALIASING, k, 0 * i1 + l, i, j));
      addButton(new GuiButtonEnumShaderOption(EnumShaderOption.NORMAL_MAP, k, 1 * i1 + l, i, j));
      addButton(new GuiButtonEnumShaderOption(EnumShaderOption.SPECULAR_MAP, k, 2 * i1 + l, i, j));
      addButton(new GuiButtonEnumShaderOption(EnumShaderOption.RENDER_RES_MUL, k, 3 * i1 + l, i, j));
      addButton(new GuiButtonEnumShaderOption(EnumShaderOption.SHADOW_RES_MUL, k, 4 * i1 + l, i, j));
      addButton(new GuiButtonEnumShaderOption(EnumShaderOption.HAND_DEPTH_MUL, k, 5 * i1 + l, i, j));
      addButton(new GuiButtonEnumShaderOption(EnumShaderOption.OLD_HAND_LIGHT, k, 6 * i1 + l, i, j));
      addButton(new GuiButtonEnumShaderOption(EnumShaderOption.OLD_LIGHTING, k, 7 * i1 + l, i, j));
      int k1 = Math.min(150, j1 / 2 - 10);
      int l1 = j1 / 4 - k1 / 2;
      int i2 = this.height - 25;
      addButton(new GuiButton(201, l1, i2, k1 - 22 + 1, j, Lang.get("of.options.shaders.shadersFolder")));
      addButton(new GuiButtonDownloadShaders(210, l1 + k1 - 22 - 1, i2));
      addButton(new GuiButton(202, j1 / 4 * 3 - k1 / 2, this.height - 25, k1, j, I18n.format("gui.done")));
      addButton(new GuiButton(203, k, this.height - 25, i, j, Lang.get("of.options.shaders.shaderOptions")));
      this.updateButtons();
   }

   public void updateButtons() {
      boolean flag = Config.isShaders();

      for(GuiButton guibutton : this.getButtonList()) {
         if(guibutton.getId() != 201 && guibutton.getId() != 202 && guibutton.getId() != 210 && guibutton.getId() != EnumShaderOption.ANTIALIASING.ordinal()) {
            guibutton.setEnabled(flag);
         }
      }
   }

   public void handleMouseInput() throws IOException {
      super.handleMouseInput();
      this.shaderList.handleMouseInput();
   }

   protected void actionPerformedLeftClick(GuiButton button) {
      this.actionPerformed(button, false);
   }

   protected void actionPerformedRightClick(GuiButton button) {
      this.actionPerformed(button, true);
   }

   private void actionPerformed(GuiButton button, boolean rightClick) {
      if(button.isEnabled()) {
         if(!(button instanceof GuiButtonEnumShaderOption)) {
            if(!rightClick) {
               switch(button.getId()) {
               case 201:
                  switch(getOSType()) {
                  case 1:
                     String s = String.format("cmd.exe /C start \"Open file\" \"%s\"", Shaders.shaderPacksDir.getAbsolutePath());

                     try {
                        Runtime.getRuntime().exec(s);
                        return;
                     } catch (IOException ioexception) {
                        ioexception.printStackTrace();
                        break;
                     }
                  case 2:
                     try {
                        Runtime.getRuntime().exec(new String[]{"/usr/bin/open", Shaders.shaderPacksDir.getAbsolutePath()});
                        return;
                     } catch (IOException ioexception1) {
                        ioexception1.printStackTrace();
                     }
                  }

                  boolean flag = false;

                  try {
                     Class oclass1 = Class.forName("java.awt.Desktop");
                     Object object1 = oclass1.getMethod("getDesktop").invoke(null);
                     oclass1.getMethod("browse", URI.class).invoke(object1, (new File(getMc().dataDir, "shaderpacks")).toURI());
                  } catch (Throwable throwable1) {
                     throwable1.printStackTrace();
                     flag = true;
                  }

                  if(flag) {
                     Config.dbg("Opening via system class!");
                     Sys.openURL("file://" + Shaders.shaderPacksDir.getAbsolutePath());
                  }
                  break;
               case 202:
                  Shaders.storeConfig();
                  this.saved = true;
                  changeScreen(this.parentGui);
                  break;
               case 203:
                  GuiShaderOptions guishaderoptions = new GuiShaderOptions(this, Config.getGameSettings());
                  Config.getMinecraft().displayGuiScreen(guishaderoptions);
                  break;
               case 210:
                  try {
                     Class<?> oclass = Class.forName("java.awt.Desktop");
                     Object object = oclass.getMethod("getDesktop").invoke(null);
                     oclass.getMethod("browse", URI.class).invoke(object, new URI("http://optifine.net/shaderPacks"));
                  } catch (Throwable throwable) {
                     throwable.printStackTrace();
                  }
               case 204:
               case 205:
               case 206:
               case 207:
               case 208:
               case 209:
               default:
                  this.shaderList.actionPerformed(button);
               }
            }
         } else {
            GuiButtonEnumShaderOption guibuttonenumshaderoption = (GuiButtonEnumShaderOption)button;
            switch(guibuttonenumshaderoption.getEnumShaderOption()) {
            case ANTIALIASING:
               Shaders.nextAntialiasingLevel(!rightClick);
               if(isShiftKeyDown()) {
                  Shaders.configAntialiasingLevel = 0;
               }

               Shaders.uninit();
               break;
            case NORMAL_MAP:
               Shaders.configNormalMap = !Shaders.configNormalMap;
               if(isShiftKeyDown()) {
                  Shaders.configNormalMap = true;
               }

               Shaders.uninit();
               getMc().scheduleResourcesRefresh();
               break;
            case SPECULAR_MAP:
               Shaders.configSpecularMap = !Shaders.configSpecularMap;
               if(isShiftKeyDown()) {
                  Shaders.configSpecularMap = true;
               }

               Shaders.uninit();
               getMc().scheduleResourcesRefresh();
               break;
            case RENDER_RES_MUL:
               Shaders.configRenderResMul = this.getNextValue(Shaders.configRenderResMul, QUALITY_MULTIPLIERS, QUALITY_MULTIPLIER_DEFAULT, !rightClick, isShiftKeyDown());
               Shaders.uninit();
               Shaders.scheduleResize();
               break;
            case SHADOW_RES_MUL:
               Shaders.configShadowResMul = this.getNextValue(Shaders.configShadowResMul, QUALITY_MULTIPLIERS, QUALITY_MULTIPLIER_DEFAULT, !rightClick, isShiftKeyDown());
               Shaders.uninit();
               Shaders.scheduleResizeShadow();
               break;
            case HAND_DEPTH_MUL:
               Shaders.configHandDepthMul = this.getNextValue(Shaders.configHandDepthMul, HAND_DEPTH_VALUES, HAND_DEPTH_DEFAULT, !rightClick, isShiftKeyDown());
               Shaders.uninit();
               break;
            case OLD_HAND_LIGHT:
               Shaders.configOldHandLight.nextValue(!rightClick);
               if(isShiftKeyDown()) {
                  Shaders.configOldHandLight.resetValue();
               }

               Shaders.uninit();
               break;
            case OLD_LIGHTING:
               Shaders.configOldLighting.nextValue(!rightClick);
               if(isShiftKeyDown()) {
                  Shaders.configOldLighting.resetValue();
               }

               Shaders.updateBlockLightLevel();
               Shaders.uninit();
               getMc().scheduleResourcesRefresh();
               break;
            case TWEAK_BLOCK_DAMAGE:
               Shaders.configTweakBlockDamage = !Shaders.configTweakBlockDamage;
               break;
            case CLOUD_SHADOW:
               Shaders.configCloudShadow = !Shaders.configCloudShadow;
               break;
            case TEX_MIN_FIL_B:
               Shaders.configTexMinFilB = (Shaders.configTexMinFilB + 1) % 3;
               Shaders.configTexMinFilN = Shaders.configTexMinFilS = Shaders.configTexMinFilB;
               button.displayString = "Tex Min: " + Shaders.texMinFilDesc[Shaders.configTexMinFilB];
               ShadersTex.updateTextureMinMagFilter();
               break;
            case TEX_MAG_FIL_N:
               Shaders.configTexMagFilN = (Shaders.configTexMagFilN + 1) % 2;
               button.displayString = "Tex_n Mag: " + Shaders.texMagFilDesc[Shaders.configTexMagFilN];
               ShadersTex.updateTextureMinMagFilter();
               break;
            case TEX_MAG_FIL_S:
               Shaders.configTexMagFilS = (Shaders.configTexMagFilS + 1) % 2;
               button.displayString = "Tex_s Mag: " + Shaders.texMagFilDesc[Shaders.configTexMagFilS];
               ShadersTex.updateTextureMinMagFilter();
               break;
            case SHADOW_CLIP_FRUSTRUM:
               Shaders.configShadowClipFrustrum = !Shaders.configShadowClipFrustrum;
               button.displayString = "ShadowClipFrustrum: " + toStringOnOff(Shaders.configShadowClipFrustrum);
               ShadersTex.updateTextureMinMagFilter();
            }

            guibuttonenumshaderoption.updateButtonText();
         }
      }
   }

   @Override
   public void onGuiClosed() {
      super.onGuiClosed();
      if(!this.saved) {
         Shaders.storeConfig();
      }
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      this.drawWorldBackground();
      this.shaderList.drawScreen(mouseX, mouseY, partialTicks);
      if(this.updateTimer <= 0) {
         this.shaderList.updateList();
         this.updateTimer += 20;
      }

      getFr().drawCenteredString(this.screenTitle + " ", this.width / 2, 15, 16777215);
      String s = "OpenGL: " + Shaders.glVersionString + ", " + Shaders.glVendorString + ", " + Shaders.glRendererString;
      int i = getFr().getStringWidth(s);
      if(i < this.width - 5) {
         getFr().drawString(s, this.width / 2, this.height - 40, 8421504);
      } else {
         getFr().drawString(s, 5, this.height - 40, 8421504);
      }

      super.drawScreen(mouseX, mouseY, partialTicks);
      this.tooltipManager.drawTooltips(mouseX, mouseY, this.getButtonList());
   }

   public void updateScreen() {
      super.updateScreen();
      --this.updateTimer;
   }

   public static String toStringOnOff(boolean value) {
      String s = Lang.getOn();
      String s1 = Lang.getOff();
      return value?s:s1;
   }

   public static String toStringAa(int value) {
      return value == 2?"FXAA 2x":(value == 4?"FXAA 4x":Lang.getOff());
   }

   public static String toStringValue(float val, float[] values, String[] names) {
      int i = getValueIndex(val, values);
      return names[i];
   }

   private float getNextValue(float val, float[] values, float valDef, boolean forward, boolean reset) {
      if(reset) {
         return valDef;
      } else {
         int i = getValueIndex(val, values);
         if(forward) {
            ++i;
            if(i >= values.length) {
               i = 0;
            }
         } else {
            --i;
            if(i < 0) {
               i = values.length - 1;
            }
         }

         return values[i];
      }
   }

   public static int getValueIndex(float val, float[] values) {
      for(int i = 0; i < values.length; ++i) {
         float f = values[i];
         if(f >= val) {
            return i;
         }
      }

      return values.length - 1;
   }

   public static String toStringQuality(float val) {
      return toStringValue(val, QUALITY_MULTIPLIERS, QUALITY_MULTIPLIER_NAMES);
   }

   public static String toStringHandDepth(float val) {
      return toStringValue(val, HAND_DEPTH_VALUES, HAND_DEPTH_NAMES);
   }

   public static int getOSType() {
      String s = System.getProperty("os.name").toLowerCase();
      return s.contains("win")?1:(s.contains("mac")?2:(s.contains("solaris")?3:(s.contains("sunos")?3:(s.contains("linux")?4:(s.contains("unix")?4:0)))));
   }

}
