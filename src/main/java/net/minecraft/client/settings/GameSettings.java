package net.minecraft.client.settings;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.murengezi.chocolate.Util.MinecraftUtils;
import com.murengezi.minecraft.client.audio.SoundCategory;
import net.minecraft.client.gui.GuiNewChat;
import com.murengezi.minecraft.client.gui.Screen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.network.play.client.C15PacketClientSettings;
import net.optifine.config.Config;
import net.minecraft.util.MathHelper;
import net.minecraft.world.EnumDifficulty;
import net.optifine.ClearWater;
import net.optifine.CustomColors;
import net.optifine.CustomGuis;
import net.optifine.CustomSky;
import net.optifine.DynamicLights;
import net.optifine.Lang;
import net.optifine.NaturalTextures;
import net.optifine.RandomEntities;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.Shaders;
import net.optifine.util.KeyUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

public class GameSettings extends MinecraftUtils {
    
    private static final Logger logger = LogManager.getLogger();
    private static final Gson gson = new Gson();
    private static final ParameterizedType typeListString = new ParameterizedType() {
        public Type[] getActualTypeArguments() {
            return new Type[] {String.class};
        }
        
        public Type getRawType() {
            return List.class;
        }
        
        public Type getOwnerType() {
            return null;
        }
    };

    /** GUI scale values */
    private static final String[] GUISCALES = new String[] {"options.guiScale.auto", "options.guiScale.small", "options.guiScale.normal", "options.guiScale.large"};
    private static final String[] PARTICLES = new String[] {"options.particles.all", "options.particles.decreased", "options.particles.minimal"};
    private static final String[] AMBIENT_OCCLUSIONS = new String[] {"options.ao.off", "options.ao.min", "options.ao.max"};
    private static final String[] field_181149_aW = new String[] {"options.off", "options.graphics.fast", "options.graphics.fancy"};
    public float mouseSensitivity = 0.5F;
    public boolean invertMouse;
    public int renderDistanceChunks = -1;
    public boolean viewBobbing = true;
    public boolean anaglyph;
    public boolean fboEnable = true;
    public int limitFramerate = 120;

    /** Clouds flag */
    public int clouds = 2;
    public boolean fancyGraphics = true;

    /** Smooth Lighting */
    public int ambientOcclusion = 2;
    public List<String> resourcePacks = Lists.newArrayList();
    public List<String> field_183018_l = Lists.newArrayList();
    public EntityPlayer.EnumChatVisibility chatVisibility = EntityPlayer.EnumChatVisibility.FULL;
    public boolean chatColours = true;
    public boolean chatLinks = true;
    public boolean chatLinksPrompt = true;
    public float chatOpacity = 1.0F;
    public boolean snooperEnabled = true;
    public boolean fullScreen;
    public boolean enableVsync = true;
    public boolean useVbo = false;
    public boolean allowBlockAlternatives = true;
    public boolean reducedDebugInfo = false;
    public boolean hideServerAddress;

    /**
     * Whether to show advanced information on item tooltips, toggled by F3+H
     */
    public boolean advancedItemTooltips;

    /** Whether to pause when the game loses focus, toggled by F3+P */
    public boolean pauseOnLostFocus = true;
    private final Set<EnumPlayerModelParts> setModelParts = Sets.newHashSet(EnumPlayerModelParts.values());
    public int overrideWidth;
    public int overrideHeight;
    public boolean heldItemTooltips = true;
    public float chatScale = 1.0F;
    public float chatWidth = 1.0F;
    public float chatHeightUnfocused = 0.44366196F;
    public float chatHeightFocused = 1.0F;
    public boolean showInventoryAchievementHint = true;
    public int mipmapLevels = 4;
    private final Map<SoundCategory, Float> mapSoundLevels = Maps.newEnumMap(SoundCategory.class);
    public boolean field_181150_U = true;
    public boolean field_181151_V = true;
    public boolean field_183509_X = true;

    /**
     * Perspective mod
     */
    public KeyBinding keyPerspective = new KeyBinding("Toggle Perspective", Keyboard.KEY_LMENU, "Perspective Mod");


    public KeyBinding keyBindForward = new KeyBinding("key.forward", 17, "key.categories.movement");
    public KeyBinding keyBindLeft = new KeyBinding("key.left", 30, "key.categories.movement");
    public KeyBinding keyBindBack = new KeyBinding("key.back", 31, "key.categories.movement");
    public KeyBinding keyBindRight = new KeyBinding("key.right", 32, "key.categories.movement");
    public KeyBinding keyBindJump = new KeyBinding("key.jump", 57, "key.categories.movement");
    public KeyBinding keyBindSneak = new KeyBinding("key.sneak", 42, "key.categories.movement");
    public KeyBinding keyBindSprint = new KeyBinding("key.sprint", 29, "key.categories.movement");
    public KeyBinding keyBindInventory = new KeyBinding("key.inventory", 18, "key.categories.inventory");
    public KeyBinding keyBindUseItem = new KeyBinding("key.use", -99, "key.categories.gameplay");
    public KeyBinding keyBindDrop = new KeyBinding("key.drop", 16, "key.categories.gameplay");
    public KeyBinding keyBindAttack = new KeyBinding("key.attack", -100, "key.categories.gameplay");
    public KeyBinding keyBindPickBlock = new KeyBinding("key.pickItem", -98, "key.categories.gameplay");
    public KeyBinding keyBindChat = new KeyBinding("key.chat", 20, "key.categories.multiplayer");
    public KeyBinding keyBindPlayerList = new KeyBinding("key.playerlist", 15, "key.categories.multiplayer");
    public KeyBinding keyBindCommand = new KeyBinding("key.command", 53, "key.categories.multiplayer");
    public KeyBinding keyBindScreenshot = new KeyBinding("key.screenshot", 60, "key.categories.misc");
    public KeyBinding keyBindTogglePerspective = new KeyBinding("key.togglePerspective", 63, "key.categories.misc");
    public KeyBinding keyBindSmoothCamera = new KeyBinding("key.smoothCamera", 0, "key.categories.misc");
    public KeyBinding keyBindFullscreen = new KeyBinding("key.fullscreen", 87, "key.categories.misc");
    public KeyBinding keyBindSpectatorOutlines = new KeyBinding("key.spectatorOutlines", 0, "key.categories.misc");
    public KeyBinding[] keyBindsHotbar = new KeyBinding[] {new KeyBinding("key.hotbar.1", 2, "key.categories.inventory"), new KeyBinding("key.hotbar.2", 3, "key.categories.inventory"), new KeyBinding("key.hotbar.3", 4, "key.categories.inventory"), new KeyBinding("key.hotbar.4", 5, "key.categories.inventory"), new KeyBinding("key.hotbar.5", 6, "key.categories.inventory"), new KeyBinding("key.hotbar.6", 7, "key.categories.inventory"), new KeyBinding("key.hotbar.7", 8, "key.categories.inventory"), new KeyBinding("key.hotbar.8", 9, "key.categories.inventory"), new KeyBinding("key.hotbar.9", 10, "key.categories.inventory")};
    public KeyBinding[] keyBindings;
    private File optionsFile;
    public EnumDifficulty difficulty;
    public boolean hideGUI;
    public int thirdPersonView;

    /** true if debug info should be displayed instead of version */
    public boolean showDebugInfo;
    public boolean showDebugProfilerChart;
    public boolean field_181657_aC;

    /** The lastServer string. */
    public String lastServer;

    /** Smooth Camera Toggle */
    public boolean smoothCamera;
    public boolean debugCamEnable;
    public float fovSetting;
    public float gammaSetting;
    public float saturation;

    /** GUI scale */
    public int guiScale;

    /** Determines amount of particles. 0 = All, 1 = Decreased, 2 = Minimal */
    public int particleSetting;

    /** Game settings language */
    public String language;
    public boolean forceUnicodeFont;
    public int ofFogType = 1;
    public float ofFogStart = 0.8F;
    public int ofMipmapType = 0;
    public boolean ofOcclusionFancy = false;
    public boolean ofSmoothFps = false;
    public boolean ofSmoothWorld = Config.isSingleProcessor();
    public boolean ofLazyChunkLoading = Config.isSingleProcessor();
    public boolean ofRenderRegions = false;
    public boolean ofSmartAnimations = false;
    public float ofAoLevel = 1.0F;
    public int ofAaLevel = 0;
    public int ofAfLevel = 1;
    public int ofClouds = 0;
    public float ofCloudsHeight = 0.0F;
    public int ofTrees = 0;
    public int ofRain = 0;
    public int ofDroppedItems = 0;
    public int ofBetterGrass = 3;
    public int ofAutoSaveTicks = 4000;
    public boolean ofLagometer = false;
    public boolean ofProfiler = false;
    public boolean ofShowFps = false;
    public boolean ofWeather = true;
    public boolean ofSky = true;
    public boolean ofStars = true;
    public boolean ofSunMoon = true;
    public int ofVignette = 0;
    public int ofChunkUpdates = 1;
    public boolean ofChunkUpdatesDynamic = false;
    public int ofTime = 0;
    public boolean ofClearWater = false;
    public boolean ofBetterSnow = false;
    public String ofFullscreenMode = "Default";
    public boolean ofSwampColors = true;
    public boolean ofRandomEntities = true;
    public boolean ofSmoothBiomes = true;
    public boolean ofCustomFonts = true;
    public boolean ofCustomColors = true;
    public boolean ofCustomSky = true;
    public boolean ofShowCapes = true;
    public int ofConnectedTextures = 2;
    public boolean ofCustomItems = true;
    public boolean ofNaturalTextures = false;
    public boolean ofEmissiveTextures = true;
    public boolean ofFastMath = false;
    public boolean ofFastRender = false;
    public int ofTranslucentBlocks = 0;
    public boolean ofDynamicFov = true;
    public boolean ofAlternateBlocks = true;
    public int ofDynamicLights = 3;
    public boolean ofCustomEntityModels = true;
    public boolean ofCustomGuis = true;
    public boolean ofShowGlErrors = true;
    public int ofScreenshotSize = 1;
    public int ofAnimatedWater = 0;
    public int ofAnimatedLava = 0;
    public boolean ofAnimatedFire = true;
    public boolean ofAnimatedPortal = true;
    public boolean ofAnimatedRedstone = true;
    public boolean ofAnimatedExplosion = true;
    public boolean ofAnimatedFlame = true;
    public boolean ofAnimatedSmoke = true;
    public boolean ofVoidParticles = true;
    public boolean ofWaterParticles = true;
    public boolean ofRainSplash = true;
    public boolean ofPortalParticles = true;
    public boolean ofPotionParticles = true;
    public boolean ofFireworkParticles = true;
    public boolean ofDrippingWaterLava = true;
    public boolean ofAnimatedTerrain = true;
    public boolean ofAnimatedTextures = true;
    public static final int DEFAULT = 0;
    public static final int FAST = 1;
    public static final int FANCY = 2;
    public static final int OFF = 3;
    public static final int SMART = 4;
    public static final int ANIM_ON = 0;
    public static final String DEFAULT_STR = "Default";
    private static final int[] OF_TREES_VALUES = new int[] {0, 1, 4, 2};
    private static final int[] OF_DYNAMIC_LIGHTS = new int[] {3, 1, 2};
    private static final String[] KEYS_DYNAMIC_LIGHTS = new String[] {"options.off", "options.graphics.fast", "options.graphics.fancy"};
    public KeyBinding ofKeyBindZoom;
    private File optionsFileOF;

    public GameSettings(File file) {
        this.keyBindings = ArrayUtils.addAll(new KeyBinding[] {this.keyBindAttack, this.keyBindUseItem, this.keyBindForward, this.keyBindLeft, this.keyBindBack, this.keyBindRight, this.keyBindJump, this.keyBindSneak, this.keyBindSprint, this.keyBindDrop, this.keyBindInventory, this.keyBindChat, this.keyBindPlayerList, this.keyBindPickBlock, this.keyBindCommand, this.keyBindScreenshot, this.keyBindTogglePerspective, this.keyBindSmoothCamera, this.keyBindFullscreen, this.keyBindSpectatorOutlines, this.keyPerspective}, this.keyBindsHotbar);
        this.difficulty = EnumDifficulty.NORMAL;
        this.lastServer = "";
        this.fovSetting = 70.0F;
        this.language = "en_US";
        this.forceUnicodeFont = false;
        this.optionsFile = new File(file, "options.txt");

        if (getMc().isJava64bit() && Runtime.getRuntime().maxMemory() >= 1000000000L) {
            GameSettings.Options.RENDER_DISTANCE.setValueMax(32.0F);
            long i = 1000000L;

            if (Runtime.getRuntime().maxMemory() >= 1500L * i) {
                GameSettings.Options.RENDER_DISTANCE.setValueMax(48.0F);
            }

            if (Runtime.getRuntime().maxMemory() >= 2500L * i) {
                GameSettings.Options.RENDER_DISTANCE.setValueMax(64.0F);
            }
        } else {
            GameSettings.Options.RENDER_DISTANCE.setValueMax(16.0F);
        }

        this.renderDistanceChunks = getMc().isJava64bit() ? 12 : 8;
        this.optionsFileOF = new File(file, "optionsof.txt");
        this.limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
        this.ofKeyBindZoom = new KeyBinding("of.key.zoom", 46, "key.categories.misc");
        this.keyBindings = ArrayUtils.add(this.keyBindings, this.ofKeyBindZoom);
        KeyUtils.fixKeyConflicts(this.keyBindings, new KeyBinding[] {this.ofKeyBindZoom});
        this.renderDistanceChunks = 8;
        this.loadOptions();
        Config.initGameSettings(this);
    }

    public GameSettings() {
        this.keyBindings = ArrayUtils.addAll(new KeyBinding[] {this.keyBindAttack, this.keyBindUseItem, this.keyBindForward, this.keyBindLeft, this.keyBindBack, this.keyBindRight, this.keyBindJump, this.keyBindSneak, this.keyBindSprint, this.keyBindDrop, this.keyBindInventory, this.keyBindChat, this.keyBindPlayerList, this.keyBindPickBlock, this.keyBindCommand, this.keyBindScreenshot, this.keyBindTogglePerspective, this.keyBindSmoothCamera, this.keyBindFullscreen, this.keyBindSpectatorOutlines}, this.keyBindsHotbar);
        this.difficulty = EnumDifficulty.NORMAL;
        this.lastServer = "";
        this.fovSetting = 70.0F;
        this.language = "en_US";
        this.forceUnicodeFont = false;
    }

    /**
     * Represents a key or mouse button as a string. Args: key
     */
    public static String getKeyDisplayString(int keyCode) {
        return keyCode < 0 ? I18n.format("key.mouseButton", keyCode + 101): (keyCode < 256 ? Keyboard.getKeyName(keyCode) : String.format("%c", (char) (keyCode - 256)).toUpperCase());
    }

    /**
     * Returns whether the specified key binding is currently being pressed.
     */
    public static boolean isKeyDown(KeyBinding keyBinding) {
        return keyBinding.getKeyCode() != 0 && (keyBinding.getKeyCode() < 0 ? Mouse.isButtonDown(keyBinding.getKeyCode() + 100) : Keyboard.isKeyDown(keyBinding.getKeyCode()));
    }

    /**
     * Sets a key binding and then saves all settings.
     */
    public void setOptionKeyBinding(KeyBinding keyBinding, int keyCode) {
        keyBinding.setKeyCode(keyCode);
        this.saveOptions();
    }

    /**
     * If the specified option is controlled by a slider (float value), this will set the float value.
     */
    public void setOptionFloatValue(GameSettings.Options option, float value) {
        this.setOptionFloatValueOF(option, value);

        if (option == GameSettings.Options.SENSITIVITY) {
            this.mouseSensitivity = value;
        }

        if (option == GameSettings.Options.FOV) {
            this.fovSetting = value;
        }

        if (option == GameSettings.Options.GAMMA) {
            this.gammaSetting = value;
        }

        if (option == GameSettings.Options.FRAMERATE_LIMIT) {
            this.limitFramerate = (int)value;
            this.enableVsync = false;

            if (this.limitFramerate <= 0) {
                this.limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
                this.enableVsync = true;
            }

            this.updateVSync();
        }

        if (option == GameSettings.Options.CHAT_OPACITY) {
            this.chatOpacity = value;
            getMc().inGameScreen.getChatGUI().refreshChat();
        }

        if (option == GameSettings.Options.CHAT_HEIGHT_FOCUSED) {
            this.chatHeightFocused = value;
            getMc().inGameScreen.getChatGUI().refreshChat();
        }

        if (option == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED) {
            this.chatHeightUnfocused = value;
            getMc().inGameScreen.getChatGUI().refreshChat();
        }

        if (option == GameSettings.Options.CHAT_WIDTH) {
            this.chatWidth = value;
            getMc().inGameScreen.getChatGUI().refreshChat();
        }

        if (option == GameSettings.Options.CHAT_SCALE) {
            this.chatScale = value;
            getMc().inGameScreen.getChatGUI().refreshChat();
        }

        if (option == GameSettings.Options.MIPMAP_LEVELS) {
            int i = this.mipmapLevels;
            this.mipmapLevels = (int)value;

            if ((float)i != value) {
                getMc().getTextureMapBlocks().setMipmapLevels(this.mipmapLevels);
                getMc().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
                getMc().getTextureMapBlocks().setBlurMipmapDirect(false, this.mipmapLevels > 0);
                getMc().scheduleResourcesRefresh();
            }
        }

        if (option == GameSettings.Options.BLOCK_ALTERNATIVES) {
            this.allowBlockAlternatives = !this.allowBlockAlternatives;
            getMc().renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.RENDER_DISTANCE) {
            this.renderDistanceChunks = (int)value;
            getMc().renderGlobal.setDisplayListEntitiesDirty();
        }
    }

    /**
     * For non-float options. Toggles the option on/off, or cycles through the list i.e. render distances.
     */
    public void setOptionValue(GameSettings.Options option, int value) {
        this.setOptionValueOF(option);

        if (option == GameSettings.Options.INVERT_MOUSE) {
            this.invertMouse = !this.invertMouse;
        }

        if (option == GameSettings.Options.GUI_SCALE) {
            this.guiScale += value;

            if (Screen.isShiftKeyDown()) {
                this.guiScale = 0;
            }

            DisplayMode displaymode = Config.getLargestDisplayMode();
            int i = displaymode.getWidth() / 320;
            int j = displaymode.getHeight() / 240;
            int k = Math.min(i, j);

            if (this.guiScale < 0) {
                this.guiScale = k - 1;
            }

            if (getMc().isUnicode() && this.guiScale % 2 != 0) {
                this.guiScale += value;
            }

            if (this.guiScale < 0 || this.guiScale >= k) {
                this.guiScale = 0;
            }
        }

        if (option == GameSettings.Options.PARTICLES) {
            this.particleSetting = (this.particleSetting + value) % 3;
        }

        if (option == GameSettings.Options.VIEW_BOBBING) {
            this.viewBobbing = !this.viewBobbing;
        }

        if (option == GameSettings.Options.RENDER_CLOUDS) {
            this.clouds = (this.clouds + value) % 3;
        }

        if (option == GameSettings.Options.FORCE_UNICODE_FONT) {
            this.forceUnicodeFont = !this.forceUnicodeFont;
            getMc().fontRenderer.setUnicodeFlag(getMc().getLanguageManager().isCurrentLocaleUnicode() || this.forceUnicodeFont);
        }

        if (option == GameSettings.Options.FBO_ENABLE) {
            this.fboEnable = !this.fboEnable;
        }

        if (option == GameSettings.Options.ANAGLYPH) {
            if (!this.anaglyph && Config.isShaders()) {
                Config.showGuiMessage(Lang.get("of.message.an.shaders1"), Lang.get("of.message.an.shaders2"));
                return;
            }

            this.anaglyph = !this.anaglyph;
            getMc().refreshResources();
        }

        if (option == GameSettings.Options.GRAPHICS) {
            this.fancyGraphics = !this.fancyGraphics;
            this.updateRenderClouds();
            getMc().renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.AMBIENT_OCCLUSION) {
            this.ambientOcclusion = (this.ambientOcclusion + value) % 3;
            getMc().renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.CHAT_VISIBILITY) {
            this.chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility((this.chatVisibility.getChatVisibility() + value) % 3);
        }

        if (option == GameSettings.Options.CHAT_COLOR) {
            this.chatColours = !this.chatColours;
        }

        if (option == GameSettings.Options.CHAT_LINKS) {
            this.chatLinks = !this.chatLinks;
        }

        if (option == GameSettings.Options.CHAT_LINKS_PROMPT) {
            this.chatLinksPrompt = !this.chatLinksPrompt;
        }

        if (option == GameSettings.Options.SNOOPER_ENABLED) {
            this.snooperEnabled = !this.snooperEnabled;
        }

        if (option == GameSettings.Options.USE_FULLSCREEN) {
            this.fullScreen = !this.fullScreen;

            if (getMc().isFullScreen() != this.fullScreen) {
                getMc().toggleFullscreen();
            }
        }

        if (option == GameSettings.Options.ENABLE_VSYNC) {
            this.enableVsync = !this.enableVsync;
            Display.setVSyncEnabled(this.enableVsync);
        }

        if (option == GameSettings.Options.USE_VBO) {
            this.useVbo = !this.useVbo;
            getMc().renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.BLOCK_ALTERNATIVES) {
            this.allowBlockAlternatives = !this.allowBlockAlternatives;
            getMc().renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.REDUCED_DEBUG_INFO) {
            this.reducedDebugInfo = !this.reducedDebugInfo;
        }

        if (option == GameSettings.Options.ENTITY_SHADOWS) {
            this.field_181151_V = !this.field_181151_V;
        }

        this.saveOptions();
    }

    public float getOptionFloatValue(GameSettings.Options option) {
        float f = this.getOptionFloatValueOF(option);
        return f != Float.MAX_VALUE ? f : (option == GameSettings.Options.FOV ? this.fovSetting : (option == GameSettings.Options.GAMMA ? this.gammaSetting : (option == GameSettings.Options.SATURATION ? this.saturation : (option == GameSettings.Options.SENSITIVITY ? this.mouseSensitivity : (option == GameSettings.Options.CHAT_OPACITY ? this.chatOpacity : (option == GameSettings.Options.CHAT_HEIGHT_FOCUSED ? this.chatHeightFocused : (option == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED ? this.chatHeightUnfocused : (option == GameSettings.Options.CHAT_SCALE ? this.chatScale : (option == GameSettings.Options.CHAT_WIDTH ? this.chatWidth : (option == GameSettings.Options.FRAMERATE_LIMIT ? (float)this.limitFramerate : (option == GameSettings.Options.MIPMAP_LEVELS ? (float)this.mipmapLevels : (option == GameSettings.Options.RENDER_DISTANCE ? (float)this.renderDistanceChunks : 0.0F))))))))))));
    }

    public boolean getOptionOrdinalValue(GameSettings.Options option) {
        switch (option) {
            case INVERT_MOUSE:
                return this.invertMouse;
            case VIEW_BOBBING:
                return this.viewBobbing;
            case ANAGLYPH:
                return this.anaglyph;
            case FBO_ENABLE:
                return this.fboEnable;
            case CHAT_COLOR:
                return this.chatColours;
            case CHAT_LINKS:
                return this.chatLinks;
            case CHAT_LINKS_PROMPT:
                return this.chatLinksPrompt;
            case SNOOPER_ENABLED:
                return this.snooperEnabled;
            case USE_FULLSCREEN:
                return this.fullScreen;
            case ENABLE_VSYNC:
                return this.enableVsync;
            case USE_VBO:
                return this.useVbo;
            case FORCE_UNICODE_FONT:
                return this.forceUnicodeFont;
            case BLOCK_ALTERNATIVES:
                return this.allowBlockAlternatives;
            case REDUCED_DEBUG_INFO:
                return this.reducedDebugInfo;
            case ENTITY_SHADOWS:
                return this.field_181151_V;
            default:
                return false;
        }
    }

    /**
     * Returns the translation of the given index in the given String array. If the index is smaller than 0 or greater
     * than/equal to the length of the String array, it is changed to 0.
     */
    private static String getTranslation(String[] p_74299_0_, int p_74299_1_) {
        if (p_74299_1_ < 0 || p_74299_1_ >= p_74299_0_.length) {
            p_74299_1_ = 0;
        }

        return I18n.format(p_74299_0_[p_74299_1_]);
    }

    /**
     * Gets a key binding.
     */
    public String getKeyBinding(GameSettings.Options options) {
        String s = this.getKeyBindingOF(options);

        if (s != null) {
            return s;
        } else {
            String s1 = I18n.format(options.getEnumString()) + ": ";

            if (options.getEnumFloat()) {
                float f1 = this.getOptionFloatValue(options);
                float f = options.normalizeValue(f1);
                return options == GameSettings.Options.MIPMAP_LEVELS && (double)f1 >= 4.0D ? s1 + Lang.get("of.general.max") : (options == GameSettings.Options.SENSITIVITY ? (f == 0.0F ? s1 + I18n.format("options.sensitivity.min") : (f == 1.0F ? s1 + I18n.format("options.sensitivity.max") : s1 + (int)(f * 200.0F) + "%")) : (options == GameSettings.Options.FOV ? (f1 == 70.0F ? s1 + I18n.format("options.fov.min") : (f1 == 110.0F ? s1 + I18n.format("options.fov.max") : s1 + (int)f1)) : (options == GameSettings.Options.FRAMERATE_LIMIT ? (f1 == options.valueMax ? s1 + I18n.format("options.framerateLimit.max") : s1 + (int)f1 + " fps") : (options == GameSettings.Options.RENDER_CLOUDS ? (f1 == options.valueMin ? s1 + I18n.format("options.cloudHeight.min") : s1 + ((int)f1 + 128)) : (options == GameSettings.Options.GAMMA ? (f == 0.0F ? s1 + I18n.format("options.gamma.min") : (f == 1.0F ? s1 + I18n.format("options.gamma.max") : s1 + "+" + (int)(f * 100.0F) + "%")) : (options == GameSettings.Options.SATURATION ? s1 + (int)(f * 400.0F) + "%" : (options == GameSettings.Options.CHAT_OPACITY ? s1 + (int)(f * 90.0F + 10.0F) + "%" : (options == GameSettings.Options.CHAT_HEIGHT_UNFOCUSED ? s1 + GuiNewChat.calculateChatboxHeight(f) + "px" : (options == GameSettings.Options.CHAT_HEIGHT_FOCUSED ? s1 + GuiNewChat.calculateChatboxHeight(f) + "px" : (options == GameSettings.Options.CHAT_WIDTH ? s1 + GuiNewChat.calculateChatboxWidth(f) + "px" : (options == GameSettings.Options.RENDER_DISTANCE ? s1 + (int)f1 + " chunks" : (options == GameSettings.Options.MIPMAP_LEVELS ? (f1 == 0.0F ? s1 + I18n.format("options.off") : s1 + (int)f1) : (f == 0.0F ? s1 + I18n.format("options.off") : s1 + (int)(f * 100.0F) + "%")))))))))))));
            } else if (options.getEnumBoolean()) {
                boolean flag = this.getOptionOrdinalValue(options);
                return flag ? s1 + I18n.format("options.on") : s1 + I18n.format("options.off");
            } else if (options == GameSettings.Options.GUI_SCALE) {
                return this.guiScale >= GUISCALES.length ? s1 + this.guiScale + "x" : s1 + getTranslation(GUISCALES, this.guiScale);
            } else if (options == GameSettings.Options.CHAT_VISIBILITY) {
                return s1 + I18n.format(this.chatVisibility.getResourceKey());
            } else if (options == GameSettings.Options.PARTICLES) {
                return s1 + getTranslation(PARTICLES, this.particleSetting);
            } else if (options == GameSettings.Options.AMBIENT_OCCLUSION) {
                return s1 + getTranslation(AMBIENT_OCCLUSIONS, this.ambientOcclusion);
            } else if (options == GameSettings.Options.RENDER_CLOUDS) {
                return s1 + getTranslation(field_181149_aW, this.clouds);
            } else if (options == GameSettings.Options.GRAPHICS) {
                if (this.fancyGraphics) {
                    return s1 + I18n.format("options.graphics.fancy");
                } else {
                    return s1 + I18n.format("options.graphics.fast");
                }
            } else {
                return s1;
            }
        }
    }

    /**
     * Loads the options from the options file. It appears that this has replaced the previous 'loadOptions'
     */
    public void loadOptions() {
        FileInputStream fileinputstream = null;
        label2: {
            try {
                if (this.optionsFile.exists()) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(fileinputstream = new FileInputStream(this.optionsFile)));
                    String s;
                    this.mapSoundLevels.clear();

                    while ((s = reader.readLine()) != null) {
                        try {
                            String[] astring = s.split(":");

                            if (astring[0].equals("mouseSensitivity")) {
                                this.mouseSensitivity = this.parseFloat(astring[1]);
                            }

                            if (astring[0].equals("fov")) {
                                this.fovSetting = this.parseFloat(astring[1]) * 40.0F + 70.0F;
                            }

                            if (astring[0].equals("gamma")) {
                                this.gammaSetting = this.parseFloat(astring[1]);
                            }

                            if (astring[0].equals("saturation")) {
                                this.saturation = this.parseFloat(astring[1]);
                            }

                            if (astring[0].equals("invertYMouse")) {
                                this.invertMouse = astring[1].equals("true");
                            }

                            if (astring[0].equals("renderDistance")) {
                                this.renderDistanceChunks = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("guiScale")) {
                                this.guiScale = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("particles")) {
                                this.particleSetting = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("bobView")) {
                                this.viewBobbing = astring[1].equals("true");
                            }

                            if (astring[0].equals("anaglyph3d")) {
                                this.anaglyph = astring[1].equals("true");
                            }

                            if (astring[0].equals("maxFps")) {
                                this.limitFramerate = Integer.parseInt(astring[1]);

                                if (this.enableVsync) {
                                    this.limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
                                }

                                if (this.limitFramerate <= 0) {
                                    this.limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
                                }
                            }

                            if (astring[0].equals("fboEnable")) {
                                this.fboEnable = astring[1].equals("true");
                            }

                            if (astring[0].equals("difficulty")) {
                                this.difficulty = EnumDifficulty.getDifficultyEnum(Integer.parseInt(astring[1]));
                            }

                            if (astring[0].equals("fancyGraphics")) {
                                this.fancyGraphics = astring[1].equals("true");
                                this.updateRenderClouds();
                            }

                            if (astring[0].equals("ao")) {
                                if (astring[1].equals("true")) {
                                    this.ambientOcclusion = 2;
                                } else if (astring[1].equals("false")) {
                                    this.ambientOcclusion = 0;
                                } else {
                                    this.ambientOcclusion = Integer.parseInt(astring[1]);
                                }
                            }

                            if (astring[0].equals("renderClouds")) {
                                switch (astring[1]) {
                                    case "true":
                                        this.clouds = 2;
                                        break;
                                    case "false":
                                        this.clouds = 0;
                                        break;
                                    case "fast":
                                        this.clouds = 1;
                                        break;
                                }
                            }

                            if (astring[0].equals("resourcePacks")) {
                                this.resourcePacks = gson.fromJson(s.substring(s.indexOf(58) + 1), typeListString);

                                if (this.resourcePacks == null) {
                                    this.resourcePacks = Lists.newArrayList();
                                }
                            }

                            if (astring[0].equals("incompatibleResourcePacks")) {
                                this.field_183018_l = gson.fromJson(s.substring(s.indexOf(58) + 1), typeListString);

                                if (this.field_183018_l == null) {
                                    this.field_183018_l = Lists.newArrayList();
                                }
                            }

                            if (astring[0].equals("lastServer") && astring.length >= 2) {
                                this.lastServer = s.substring(s.indexOf(58) + 1);
                            }

                            if (astring[0].equals("lang") && astring.length >= 2) {
                                this.language = astring[1];
                            }

                            if (astring[0].equals("chatVisibility")) {
                                this.chatVisibility = EntityPlayer.EnumChatVisibility.getEnumChatVisibility(Integer.parseInt(astring[1]));
                            }

                            if (astring[0].equals("chatColors")) {
                                this.chatColours = astring[1].equals("true");
                            }

                            if (astring[0].equals("chatLinks")) {
                                this.chatLinks = astring[1].equals("true");
                            }

                            if (astring[0].equals("chatLinksPrompt")) {
                                this.chatLinksPrompt = astring[1].equals("true");
                            }

                            if (astring[0].equals("chatOpacity")) {
                                this.chatOpacity = this.parseFloat(astring[1]);
                            }

                            if (astring[0].equals("snooperEnabled")) {
                                this.snooperEnabled = astring[1].equals("true");
                            }

                            if (astring[0].equals("fullscreen")) {
                                this.fullScreen = astring[1].equals("true");
                            }

                            if (astring[0].equals("enableVsync")) {
                                this.enableVsync = astring[1].equals("true");

                                if (this.enableVsync) {
                                    this.limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
                                }

                                this.updateVSync();
                            }

                            if (astring[0].equals("useVbo")) {
                                this.useVbo = astring[1].equals("true");
                            }

                            if (astring[0].equals("hideServerAddress")) {
                                this.hideServerAddress = astring[1].equals("true");
                            }

                            if (astring[0].equals("advancedItemTooltips")) {
                                this.advancedItemTooltips = astring[1].equals("true");
                            }

                            if (astring[0].equals("pauseOnLostFocus")) {
                                this.pauseOnLostFocus = astring[1].equals("true");
                            }

                            if (astring[0].equals("overrideHeight")) {
                                this.overrideHeight = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("overrideWidth")) {
                                this.overrideWidth = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("heldItemTooltips")) {
                                this.heldItemTooltips = astring[1].equals("true");
                            }

                            if (astring[0].equals("chatHeightFocused")) {
                                this.chatHeightFocused = this.parseFloat(astring[1]);
                            }

                            if (astring[0].equals("chatHeightUnfocused")) {
                                this.chatHeightUnfocused = this.parseFloat(astring[1]);
                            }

                            if (astring[0].equals("chatScale")) {
                                this.chatScale = this.parseFloat(astring[1]);
                            }

                            if (astring[0].equals("chatWidth")) {
                                this.chatWidth = this.parseFloat(astring[1]);
                            }

                            if (astring[0].equals("showInventoryAchievementHint")) {
                                this.showInventoryAchievementHint = astring[1].equals("true");
                            }

                            if (astring[0].equals("mipmapLevels")) {
                                this.mipmapLevels = Integer.parseInt(astring[1]);
                            }

                            if (astring[0].equals("forceUnicodeFont")) {
                                this.forceUnicodeFont = astring[1].equals("true");
                            }

                            if (astring[0].equals("allowBlockAlternatives")) {
                                this.allowBlockAlternatives = astring[1].equals("true");
                            }

                            if (astring[0].equals("reducedDebugInfo")) {
                                this.reducedDebugInfo = astring[1].equals("true");
                            }

                            if (astring[0].equals("useNativeTransport")) {
                                this.field_181150_U = astring[1].equals("true");
                            }

                            if (astring[0].equals("entityShadows")) {
                                this.field_181151_V = astring[1].equals("true");
                            }

                            if (astring[0].equals("realmsNotifications")) {
                                this.field_183509_X = astring[1].equals("true");
                            }

                            for (KeyBinding keybinding : this.keyBindings) {
                                if (astring[0].equals("key_" + keybinding.getKeyDescription())) {
                                    keybinding.setKeyCode(Integer.parseInt(astring[1]));
                                }
                            }

                            for (SoundCategory category : SoundCategory.values()) {
                                if (astring[0].equals("soundCategory_" + category.getCategoryName())) {
                                    this.mapSoundLevels.put(category, this.parseFloat(astring[1]));
                                }
                            }

                            for (EnumPlayerModelParts modelPart : EnumPlayerModelParts.values()) {
                                if (astring[0].equals("modelPart_" + modelPart.getPartName())) {
                                    this.setModelPartEnabled(modelPart, astring[1].equals("true"));
                                }
                            }
                        } catch (Exception exception) {
                            logger.warn("Skipping bad option: " + s);
                            exception.printStackTrace();
                        }
                    }

                    KeyBinding.resetKeyBindingArrayAndHash();
                    reader.close();
                    break label2;
                }
            } catch (Exception exception) {
                logger.error("Failed to load options", exception);
                break label2;
            } finally {
                IOUtils.closeQuietly(fileinputstream);
            }

            return;
        }
        this.loadOfOptions();
    }

    /**
     * Parses a string into a float.
     */
    private float parseFloat(String input) {
        return input.equals("true") ? 1.0F : (input.equals("false") ? 0.0F : Float.parseFloat(input));
    }

    /**
     * Saves the options to the options file.
     */
    public void saveOptions() {
        if (Reflector.FMLClientHandler.exists()) {
            Object object = Reflector.call(Reflector.FMLClientHandler_instance);

            if (object != null && Reflector.callBoolean(object, Reflector.FMLClientHandler_isLoading)) {
                return;
            }
        }

        try {
            PrintWriter writer = new PrintWriter(new FileWriter(this.optionsFile));
            writer.println("invertYMouse:" + this.invertMouse);
            writer.println("mouseSensitivity:" + this.mouseSensitivity);
            writer.println("fov:" + (this.fovSetting - 70.0F) / 40.0F);
            writer.println("gamma:" + this.gammaSetting);
            writer.println("saturation:" + this.saturation);
            writer.println("renderDistance:" + this.renderDistanceChunks);
            writer.println("guiScale:" + this.guiScale);
            writer.println("particles:" + this.particleSetting);
            writer.println("bobView:" + this.viewBobbing);
            writer.println("anaglyph3d:" + this.anaglyph);
            writer.println("maxFps:" + this.limitFramerate);
            writer.println("fboEnable:" + this.fboEnable);
            writer.println("difficulty:" + this.difficulty.getDifficultyId());
            writer.println("fancyGraphics:" + this.fancyGraphics);
            writer.println("ao:" + this.ambientOcclusion);

            switch (this.clouds) {
                case 0:
                    writer.println("renderClouds:false");
                    break;
                case 1:
                    writer.println("renderClouds:fast");
                    break;
                case 2:
                    writer.println("renderClouds:true");
            }

            writer.println("resourcePacks:" + gson.toJson(this.resourcePacks));
            writer.println("incompatibleResourcePacks:" + gson.toJson(this.field_183018_l));
            writer.println("lastServer:" + this.lastServer);
            writer.println("lang:" + this.language);
            writer.println("chatVisibility:" + this.chatVisibility.getChatVisibility());
            writer.println("chatColors:" + this.chatColours);
            writer.println("chatLinks:" + this.chatLinks);
            writer.println("chatLinksPrompt:" + this.chatLinksPrompt);
            writer.println("chatOpacity:" + this.chatOpacity);
            writer.println("snooperEnabled:" + this.snooperEnabled);
            writer.println("fullscreen:" + this.fullScreen);
            writer.println("enableVsync:" + this.enableVsync);
            writer.println("useVbo:" + this.useVbo);
            writer.println("hideServerAddress:" + this.hideServerAddress);
            writer.println("advancedItemTooltips:" + this.advancedItemTooltips);
            writer.println("pauseOnLostFocus:" + this.pauseOnLostFocus);
            writer.println("overrideWidth:" + this.overrideWidth);
            writer.println("overrideHeight:" + this.overrideHeight);
            writer.println("heldItemTooltips:" + this.heldItemTooltips);
            writer.println("chatHeightFocused:" + this.chatHeightFocused);
            writer.println("chatHeightUnfocused:" + this.chatHeightUnfocused);
            writer.println("chatScale:" + this.chatScale);
            writer.println("chatWidth:" + this.chatWidth);
            writer.println("showInventoryAchievementHint:" + this.showInventoryAchievementHint);
            writer.println("mipmapLevels:" + this.mipmapLevels);
            writer.println("forceUnicodeFont:" + this.forceUnicodeFont);
            writer.println("allowBlockAlternatives:" + this.allowBlockAlternatives);
            writer.println("reducedDebugInfo:" + this.reducedDebugInfo);
            writer.println("useNativeTransport:" + this.field_181150_U);
            writer.println("entityShadows:" + this.field_181151_V);
            writer.println("realmsNotifications:" + this.field_183509_X);

            for (KeyBinding keybinding : this.keyBindings) {
                writer.println("key_" + keybinding.getKeyDescription() + ":" + keybinding.getKeyCode());
            }

            for (SoundCategory soundcategory : SoundCategory.values()) {
                writer.println("soundCategory_" + soundcategory.getCategoryName() + ":" + this.getSoundLevel(soundcategory));
            }

            for (EnumPlayerModelParts enumplayermodelparts : EnumPlayerModelParts.values()) {
                writer.println("modelPart_" + enumplayermodelparts.getPartName() + ":" + this.setModelParts.contains(enumplayermodelparts));
            }

            writer.close();
        } catch (Exception exception) {
            logger.error("Failed to save options", exception);
        }

        this.saveOfOptions();
        this.sendSettingsToServer();
    }

    public float getSoundLevel(SoundCategory soundCategory) {
        return this.mapSoundLevels.getOrDefault(soundCategory, 1.0F);
    }

    public void setSoundLevel(SoundCategory soundCategory, float soundLevel) {
        getMc().getSoundHandler().setSoundLevel(soundCategory, soundLevel);
        this.mapSoundLevels.put(soundCategory, soundLevel);
    }

    /**
     * Send a client info packet with settings information to the server
     */
    public void sendSettingsToServer() {
        if (getMc().player != null) {
            int i = 0;

            for (EnumPlayerModelParts enumplayermodelparts : this.setModelParts) {
                i |= enumplayermodelparts.getPartMask();
            }

            getMc().player.sendQueue.addToSendQueue(new C15PacketClientSettings(this.language, this.renderDistanceChunks, this.chatVisibility, this.chatColours, i));
        }
    }

    public Set<EnumPlayerModelParts> getModelParts() {
        return ImmutableSet.copyOf(this.setModelParts);
    }

    public void setModelPartEnabled(EnumPlayerModelParts modelParts, boolean enable) {
        if (enable) {
            this.setModelParts.add(modelParts);
        } else {
            this.setModelParts.remove(modelParts);
        }

        this.sendSettingsToServer();
    }

    public void switchModelPartEnabled(EnumPlayerModelParts playerModelParts) {
        if (!this.getModelParts().contains(playerModelParts)) {
            this.setModelParts.add(playerModelParts);
        } else {
            this.setModelParts.remove(playerModelParts);
        }

        this.sendSettingsToServer();
    }

    public int func_181147_e() {
        return this.renderDistanceChunks >= 4 ? this.clouds : 0;
    }

    public boolean func_181148_f() {
        return this.field_181150_U;
    }

    private void setOptionFloatValueOF(GameSettings.Options option, float value) {
        if (option == GameSettings.Options.CLOUD_HEIGHT) {
            this.ofCloudsHeight = value;
            getMc().renderGlobal.resetClouds();
        }

        if (option == GameSettings.Options.AO_LEVEL) {
            this.ofAoLevel = value;
            getMc().renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.AA_LEVEL) {
            int i = (int)value;

            if (i > 0 && Config.isShaders()) {
                Config.showGuiMessage(Lang.get("of.message.aa.shaders1"), Lang.get("of.message.aa.shaders2"));
                return;
            }

            int[] aint = new int[] {0, 2, 4, 6, 8, 12, 16};
            this.ofAaLevel = 0;

            for (int k : aint) {
                if (i >= k) {
                    this.ofAaLevel = k;
                }
            }

            this.ofAaLevel = Config.limit(this.ofAaLevel, 0, 16);
        }

        if (option == GameSettings.Options.AF_LEVEL) {
            int k = (int)value;

            if (k > 1 && Config.isShaders()) {
                Config.showGuiMessage(Lang.get("of.message.af.shaders1"), Lang.get("of.message.af.shaders2"));
                return;
            }

            for (this.ofAfLevel = 1; this.ofAfLevel * 2 <= k; this.ofAfLevel *= 2)
            {
            }

            this.ofAfLevel = Config.limit(this.ofAfLevel, 1, 16);
            getMc().refreshResources();
        }

        if (option == GameSettings.Options.MIPMAP_TYPE) {
            int l = (int)value;
            this.ofMipmapType = Config.limit(l, 0, 3);
            getMc().refreshResources();
        }

        if (option == GameSettings.Options.FULLSCREEN_MODE) {
            int i1 = (int)value - 1;
            String[] astring = Config.getDisplayModeNames();

            if (i1 < 0 || i1 >= astring.length) {
                this.ofFullscreenMode = "Default";
                return;
            }

            this.ofFullscreenMode = astring[i1];
        }
    }

    private float getOptionFloatValueOF(GameSettings.Options option) {
        if (option == GameSettings.Options.CLOUD_HEIGHT) {
            return this.ofCloudsHeight;
        } else if (option == GameSettings.Options.AO_LEVEL) {
            return this.ofAoLevel;
        } else if (option == GameSettings.Options.AA_LEVEL) {
            return (float)this.ofAaLevel;
        } else if (option == GameSettings.Options.AF_LEVEL) {
            return (float)this.ofAfLevel;
        } else if (option == GameSettings.Options.MIPMAP_TYPE) {
            return (float)this.ofMipmapType;
        } else if (option == GameSettings.Options.FRAMERATE_LIMIT) {
            return (float)this.limitFramerate == GameSettings.Options.FRAMERATE_LIMIT.getValueMax() && this.enableVsync ? 0.0F : (float)this.limitFramerate;
        } else if (option == GameSettings.Options.FULLSCREEN_MODE) {
            if (this.ofFullscreenMode.equals("Default")) {
                return 0.0F;
            } else {
                List list = Arrays.asList(Config.getDisplayModeNames());
                int i = list.indexOf(this.ofFullscreenMode);
                return i < 0 ? 0.0F : (float)(i + 1);
            }
        } else {
            return Float.MAX_VALUE;
        }
    }

    private void setOptionValueOF(GameSettings.Options option) {
        if (option == GameSettings.Options.FOG_FANCY) {
            switch (this.ofFogType) {
                case 1:
                    this.ofFogType = 2;

                    if (!Config.isFancyFogAvailable())
                    {
                        this.ofFogType = 3;
                    }
                    break;
                case 2:
                    this.ofFogType = 3;
                    break;
                case 3:
                default:
                    this.ofFogType = 1;
            }
        }

        if (option == GameSettings.Options.FOG_START) {
            this.ofFogStart += 0.2F;

            if (this.ofFogStart > 0.81F) {
                this.ofFogStart = 0.2F;
            }
        }

        if (option == GameSettings.Options.SMOOTH_FPS) {
            this.ofSmoothFps = !this.ofSmoothFps;
        }

        if (option == GameSettings.Options.SMOOTH_WORLD) {
            this.ofSmoothWorld = !this.ofSmoothWorld;
            Config.updateThreadPriorities();
        }

        if (option == GameSettings.Options.CLOUDS) {
            ++this.ofClouds;

            if (this.ofClouds > 3) {
                this.ofClouds = 0;
            }

            this.updateRenderClouds();
            getMc().renderGlobal.resetClouds();
        }

        if (option == GameSettings.Options.TREES) {
            this.ofTrees = nextValue(this.ofTrees, OF_TREES_VALUES);
            getMc().renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.DROPPED_ITEMS) {
            ++this.ofDroppedItems;

            if (this.ofDroppedItems > 2) {
                this.ofDroppedItems = 0;
            }
        }

        if (option == GameSettings.Options.RAIN) {
            ++this.ofRain;

            if (this.ofRain > 3) {
                this.ofRain = 0;
            }
        }

        if (option == GameSettings.Options.ANIMATED_WATER) {
            ++this.ofAnimatedWater;

            if (this.ofAnimatedWater == 1) {
                ++this.ofAnimatedWater;
            }

            if (this.ofAnimatedWater > 2) {
                this.ofAnimatedWater = 0;
            }
        }

        if (option == GameSettings.Options.ANIMATED_LAVA) {
            ++this.ofAnimatedLava;

            if (this.ofAnimatedLava == 1) {
                ++this.ofAnimatedLava;
            }

            if (this.ofAnimatedLava > 2) {
                this.ofAnimatedLava = 0;
            }
        }

        if (option == GameSettings.Options.ANIMATED_FIRE) {
            this.ofAnimatedFire = !this.ofAnimatedFire;
        }

        if (option == GameSettings.Options.ANIMATED_PORTAL) {
            this.ofAnimatedPortal = !this.ofAnimatedPortal;
        }

        if (option == GameSettings.Options.ANIMATED_REDSTONE) {
            this.ofAnimatedRedstone = !this.ofAnimatedRedstone;
        }

        if (option == GameSettings.Options.ANIMATED_EXPLOSION) {
            this.ofAnimatedExplosion = !this.ofAnimatedExplosion;
        }

        if (option == GameSettings.Options.ANIMATED_FLAME) {
            this.ofAnimatedFlame = !this.ofAnimatedFlame;
        }

        if (option == GameSettings.Options.ANIMATED_SMOKE) {
            this.ofAnimatedSmoke = !this.ofAnimatedSmoke;
        }

        if (option == GameSettings.Options.VOID_PARTICLES) {
            this.ofVoidParticles = !this.ofVoidParticles;
        }

        if (option == GameSettings.Options.WATER_PARTICLES) {
            this.ofWaterParticles = !this.ofWaterParticles;
        }

        if (option == GameSettings.Options.PORTAL_PARTICLES) {
            this.ofPortalParticles = !this.ofPortalParticles;
        }

        if (option == GameSettings.Options.POTION_PARTICLES) {
            this.ofPotionParticles = !this.ofPotionParticles;
        }

        if (option == GameSettings.Options.FIREWORK_PARTICLES) {
            this.ofFireworkParticles = !this.ofFireworkParticles;
        }

        if (option == GameSettings.Options.DRIPPING_WATER_LAVA) {
            this.ofDrippingWaterLava = !this.ofDrippingWaterLava;
        }

        if (option == GameSettings.Options.ANIMATED_TERRAIN) {
            this.ofAnimatedTerrain = !this.ofAnimatedTerrain;
        }

        if (option == GameSettings.Options.ANIMATED_TEXTURES) {
            this.ofAnimatedTextures = !this.ofAnimatedTextures;
        }

        if (option == GameSettings.Options.RAIN_SPLASH) {
            this.ofRainSplash = !this.ofRainSplash;
        }

        if (option == GameSettings.Options.LAGOMETER) {
            this.ofLagometer = !this.ofLagometer;
        }

        if (option == GameSettings.Options.SHOW_FPS) {
            this.ofShowFps = !this.ofShowFps;
        }

        if (option == GameSettings.Options.AUTOSAVE_TICKS) {
            int i = 900;
            this.ofAutoSaveTicks = Math.max(this.ofAutoSaveTicks / i * i, i);
            this.ofAutoSaveTicks *= 2;

            if (this.ofAutoSaveTicks > 32 * i) {
                this.ofAutoSaveTicks = i;
            }
        }

        if (option == GameSettings.Options.BETTER_GRASS) {
            ++this.ofBetterGrass;

            if (this.ofBetterGrass > 3)
            {
                this.ofBetterGrass = 1;
            }

            getMc().renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.CONNECTED_TEXTURES) {
            ++this.ofConnectedTextures;

            if (this.ofConnectedTextures > 3) {
                this.ofConnectedTextures = 1;
            }

            if (this.ofConnectedTextures == 2) {
                getMc().renderGlobal.loadRenderers();
            } else {
                getMc().refreshResources();
            }
        }

        if (option == GameSettings.Options.WEATHER) {
            this.ofWeather = !this.ofWeather;
        }

        if (option == GameSettings.Options.SKY) {
            this.ofSky = !this.ofSky;
        }

        if (option == GameSettings.Options.STARS) {
            this.ofStars = !this.ofStars;
        }

        if (option == GameSettings.Options.SUN_MOON) {
            this.ofSunMoon = !this.ofSunMoon;
        }

        if (option == GameSettings.Options.VIGNETTE) {
            ++this.ofVignette;

            if (this.ofVignette > 2) {
                this.ofVignette = 0;
            }
        }

        if (option == GameSettings.Options.CHUNK_UPDATES) {
            ++this.ofChunkUpdates;

            if (this.ofChunkUpdates > 5) {
                this.ofChunkUpdates = 1;
            }
        }

        if (option == GameSettings.Options.CHUNK_UPDATES_DYNAMIC) {
            this.ofChunkUpdatesDynamic = !this.ofChunkUpdatesDynamic;
        }

        if (option == GameSettings.Options.TIME) {
            ++this.ofTime;

            if (this.ofTime > 2) {
                this.ofTime = 0;
            }
        }

        if (option == GameSettings.Options.CLEAR_WATER) {
            this.ofClearWater = !this.ofClearWater;
            this.updateWaterOpacity();
        }

        if (option == GameSettings.Options.PROFILER) {
            this.ofProfiler = !this.ofProfiler;
        }

        if (option == GameSettings.Options.BETTER_SNOW) {
            this.ofBetterSnow = !this.ofBetterSnow;
            getMc().renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.SWAMP_COLORS) {
            this.ofSwampColors = !this.ofSwampColors;
            CustomColors.updateUseDefaultGrassFoliageColors();
            getMc().renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.RANDOM_ENTITIES) {
            this.ofRandomEntities = !this.ofRandomEntities;
            RandomEntities.update();
        }

        if (option == GameSettings.Options.SMOOTH_BIOMES) {
            this.ofSmoothBiomes = !this.ofSmoothBiomes;
            CustomColors.updateUseDefaultGrassFoliageColors();
            getMc().renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.CUSTOM_FONTS) {
            this.ofCustomFonts = !this.ofCustomFonts;
            getMc().fontRenderer.onResourceManagerReload(Config.getResourceManager());
            getMc().galacticFontRenderer.onResourceManagerReload(Config.getResourceManager());
        }

        if (option == GameSettings.Options.CUSTOM_COLORS) {
            this.ofCustomColors = !this.ofCustomColors;
            CustomColors.update();
            getMc().renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.CUSTOM_ITEMS) {
            this.ofCustomItems = !this.ofCustomItems;
            getMc().refreshResources();
        }

        if (option == GameSettings.Options.CUSTOM_SKY) {
            this.ofCustomSky = !this.ofCustomSky;
            CustomSky.update();
        }

        if (option == GameSettings.Options.SHOW_CAPES) {
            this.ofShowCapes = !this.ofShowCapes;
        }

        if (option == GameSettings.Options.NATURAL_TEXTURES) {
            this.ofNaturalTextures = !this.ofNaturalTextures;
            NaturalTextures.update();
            getMc().renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.EMISSIVE_TEXTURES) {
            this.ofEmissiveTextures = !this.ofEmissiveTextures;
            getMc().refreshResources();
        }

        if (option == GameSettings.Options.FAST_MATH) {
            this.ofFastMath = !this.ofFastMath;
            MathHelper.fastMath = this.ofFastMath;
        }

        if (option == GameSettings.Options.FAST_RENDER) {
            if (!this.ofFastRender && Config.isShaders()) {
                Config.showGuiMessage(Lang.get("of.message.fr.shaders1"), Lang.get("of.message.fr.shaders2"));
                return;
            }

            this.ofFastRender = !this.ofFastRender;

            if (this.ofFastRender) {
                getMc().entityRenderer.func_181022_b();
            }

            Config.updateFramebufferSize();
        }

        if (option == GameSettings.Options.TRANSLUCENT_BLOCKS) {
            if (this.ofTranslucentBlocks == 0) {
                this.ofTranslucentBlocks = 1;
            } else if (this.ofTranslucentBlocks == 1) {
                this.ofTranslucentBlocks = 2;
            } else if (this.ofTranslucentBlocks == 2) {
                this.ofTranslucentBlocks = 0;
            } else {
                this.ofTranslucentBlocks = 0;
            }

            getMc().renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.LAZY_CHUNK_LOADING) {
            this.ofLazyChunkLoading = !this.ofLazyChunkLoading;
        }

        if (option == GameSettings.Options.RENDER_REGIONS) {
            this.ofRenderRegions = !this.ofRenderRegions;
            getMc().renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.SMART_ANIMATIONS) {
            this.ofSmartAnimations = !this.ofSmartAnimations;
            getMc().renderGlobal.loadRenderers();
        }

        if (option == GameSettings.Options.DYNAMIC_FOV) {
            this.ofDynamicFov = !this.ofDynamicFov;
        }

        if (option == GameSettings.Options.ALTERNATE_BLOCKS) {
            this.ofAlternateBlocks = !this.ofAlternateBlocks;
            getMc().refreshResources();
        }

        if (option == GameSettings.Options.DYNAMIC_LIGHTS) {
            this.ofDynamicLights = nextValue(this.ofDynamicLights, OF_DYNAMIC_LIGHTS);
            DynamicLights.removeLights(getMc().renderGlobal);
        }

        if (option == GameSettings.Options.SCREENSHOT_SIZE) {
            ++this.ofScreenshotSize;

            if (this.ofScreenshotSize > 4) {
                this.ofScreenshotSize = 1;
            }

            if (!OpenGlHelper.isFramebufferEnabled()) {
                this.ofScreenshotSize = 1;
            }
        }

        if (option == GameSettings.Options.CUSTOM_ENTITY_MODELS) {
            this.ofCustomEntityModels = !this.ofCustomEntityModels;
            getMc().refreshResources();
        }

        if (option == GameSettings.Options.CUSTOM_GUIS) {
            this.ofCustomGuis = !this.ofCustomGuis;
            CustomGuis.update();
        }

        if (option == GameSettings.Options.SHOW_GL_ERRORS) {
            this.ofShowGlErrors = !this.ofShowGlErrors;
        }

        if (option == GameSettings.Options.HELD_ITEM_TOOLTIPS) {
            this.heldItemTooltips = !this.heldItemTooltips;
        }

        if (option == GameSettings.Options.ADVANCED_TOOLTIPS) {
            this.advancedItemTooltips = !this.advancedItemTooltips;
        }
    }

    private String getKeyBindingOF(GameSettings.Options option) {
        String s = I18n.format(option.getEnumString()) + ": ";

        if (s == null) {
            s = option.getEnumString();
        }

        if (option == GameSettings.Options.RENDER_DISTANCE) {
            int i1 = (int)this.getOptionFloatValue(option);
            String s1 = I18n.format("options.renderDistance.tiny");
            int i = 2;

            if (i1 >= 4) {
                s1 = I18n.format("options.renderDistance.short");
                i = 4;
            }

            if (i1 >= 8) {
                s1 = I18n.format("options.renderDistance.normal");
                i = 8;
            }

            if (i1 >= 16) {
                s1 = I18n.format("options.renderDistance.far");
                i = 16;
            }

            if (i1 >= 32) {
                s1 = Lang.get("of.options.renderDistance.extreme");
                i = 32;
            }

            if (i1 >= 48) {
                s1 = Lang.get("of.options.renderDistance.insane");
                i = 48;
            }

            if (i1 >= 64) {
                s1 = Lang.get("of.options.renderDistance.ludicrous");
                i = 64;
            }

            int j = this.renderDistanceChunks - i;
            String s2 = s1;

            if (j > 0)
            {
                s2 = s1 + "+";
            }

            return s + i1 + " " + s2 + "";
        } else if (option == GameSettings.Options.FOG_FANCY) {
            switch (this.ofFogType) {
                case 1:
                    return s + Lang.getFast();
                case 2:
                    return s + Lang.getFancy();
                case 3:
                default:
                    return s + Lang.getOff();
            }
        } else if (option == GameSettings.Options.FOG_START) {
            return s + this.ofFogStart;
        } else if (option == GameSettings.Options.MIPMAP_TYPE) {
            switch (this.ofMipmapType) {
                case 0:
                    return s + Lang.get("of.options.mipmap.nearest");
                case 1:
                    return s + Lang.get("of.options.mipmap.linear");
                case 2:
                    return s + Lang.get("of.options.mipmap.bilinear");
                case 3:
                    return s + Lang.get("of.options.mipmap.trilinear");
                default:
                    return s + "of.options.mipmap.nearest";
            }
        } else if (option == GameSettings.Options.SMOOTH_FPS) {
            return this.ofSmoothFps ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.SMOOTH_WORLD) {
            return this.ofSmoothWorld ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.CLOUDS) {
            switch (this.ofClouds) {
                case 1:
                    return s + Lang.getFast();
                case 2:
                    return s + Lang.getFancy();
                case 3:
                    return s + Lang.getOff();
                default:
                    return s + Lang.getDefault();
            }
        } else if (option == GameSettings.Options.TREES) {
            switch (this.ofTrees) {
                case 1:
                    return s + Lang.getFast();
                case 2:
                    return s + Lang.getFancy();
                case 3:
                default:
                    return s + Lang.getDefault();
                case 4:
                    return s + Lang.get("of.general.smart");
            }
        } else if (option == GameSettings.Options.DROPPED_ITEMS) {
            switch (this.ofDroppedItems) {
                case 1:
                    return s + Lang.getFast();
                case 2:
                    return s + Lang.getFancy();
                default:
                    return s + Lang.getDefault();
            }
        } else if (option == GameSettings.Options.RAIN) {
            switch (this.ofRain) {
                case 1:
                    return s + Lang.getFast();
                case 2:
                    return s + Lang.getFancy();
                case 3:
                    return s + Lang.getOff();
                default:
                    return s + Lang.getDefault();
            }
        } else if (option == GameSettings.Options.ANIMATED_WATER) {
            switch (this.ofAnimatedWater) {
                case 1:
                    return s + Lang.get("of.options.animation.dynamic");
                case 2:
                    return s + Lang.getOff();
                default:
                    return s + Lang.getOn();
            }
        } else if (option == GameSettings.Options.ANIMATED_LAVA) {
            switch (this.ofAnimatedLava) {
                case 1:
                    return s + Lang.get("of.options.animation.dynamic");
                case 2:
                    return s + Lang.getOff();
                default:
                    return s + Lang.getOn();
            }
        } else if (option == GameSettings.Options.ANIMATED_FIRE) {
            return this.ofAnimatedFire ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.ANIMATED_PORTAL) {
            return this.ofAnimatedPortal ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.ANIMATED_REDSTONE) {
            return this.ofAnimatedRedstone ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.ANIMATED_EXPLOSION) {
            return this.ofAnimatedExplosion ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.ANIMATED_FLAME) {
            return this.ofAnimatedFlame ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.ANIMATED_SMOKE) {
            return this.ofAnimatedSmoke ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.VOID_PARTICLES) {
            return this.ofVoidParticles ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.WATER_PARTICLES) {
            return this.ofWaterParticles ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.PORTAL_PARTICLES) {
            return this.ofPortalParticles ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.POTION_PARTICLES) {
            return this.ofPotionParticles ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.FIREWORK_PARTICLES) {
            return this.ofFireworkParticles ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.DRIPPING_WATER_LAVA) {
            return this.ofDrippingWaterLava ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.ANIMATED_TERRAIN) {
            return this.ofAnimatedTerrain ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.ANIMATED_TEXTURES) {
            return this.ofAnimatedTextures ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.RAIN_SPLASH) {
            return this.ofRainSplash ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.LAGOMETER) {
            return this.ofLagometer ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.SHOW_FPS) {
            return this.ofShowFps ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.AUTOSAVE_TICKS) {
            int l = 900;
            return this.ofAutoSaveTicks <= l ? s + Lang.get("of.options.save.45s") : (this.ofAutoSaveTicks <= 2 * l ? s + Lang.get("of.options.save.90s") : (this.ofAutoSaveTicks <= 4 * l ? s + Lang.get("of.options.save.3min") : (this.ofAutoSaveTicks <= 8 * l ? s + Lang.get("of.options.save.6min") : (this.ofAutoSaveTicks <= 16 * l ? s + Lang.get("of.options.save.12min") : s + Lang.get("of.options.save.24min")))));
        } else if (option == GameSettings.Options.BETTER_GRASS) {
            switch (this.ofBetterGrass) {
                case 1:
                    return s + Lang.getFast();
                case 2:
                    return s + Lang.getFancy();
                default:
                    return s + Lang.getOff();
            }
        } else if (option == GameSettings.Options.CONNECTED_TEXTURES) {
            switch (this.ofConnectedTextures) {
                case 1:
                    return s + Lang.getFast();
                case 2:
                    return s + Lang.getFancy();
                default:
                    return s + Lang.getOff();
            }
        } else if (option == GameSettings.Options.WEATHER) {
            return this.ofWeather ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.SKY) {
            return this.ofSky ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.STARS) {
            return this.ofStars ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.SUN_MOON) {
            return this.ofSunMoon ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.VIGNETTE) {
            switch (this.ofVignette) {
                case 1:
                    return s + Lang.getFast();
                case 2:
                    return s + Lang.getFancy();
                default:
                    return s + Lang.getDefault();
            }
        } else if (option == GameSettings.Options.CHUNK_UPDATES) {
            return s + this.ofChunkUpdates;
        } else if (option == GameSettings.Options.CHUNK_UPDATES_DYNAMIC) {
            return this.ofChunkUpdatesDynamic ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.TIME) {
            return this.ofTime == 1 ? s + Lang.get("of.options.time.dayOnly") : (this.ofTime == 2 ? s + Lang.get("of.options.time.nightOnly") : s + Lang.getDefault());
        } else if (option == GameSettings.Options.CLEAR_WATER) {
            return this.ofClearWater ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.AA_LEVEL) {
            String s3 = "";

            if (this.ofAaLevel != Config.getAntialiasingLevel()) {
                s3 = " (" + Lang.get("of.general.restart") + ")";
            }

            return this.ofAaLevel == 0 ? s + Lang.getOff() + s3 : s + this.ofAaLevel + s3;
        } else if (option == GameSettings.Options.AF_LEVEL) {
            return this.ofAfLevel == 1 ? s + Lang.getOff() : s + this.ofAfLevel;
        } else if (option == GameSettings.Options.PROFILER) {
            return this.ofProfiler ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.BETTER_SNOW) {
            return this.ofBetterSnow ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.SWAMP_COLORS) {
            return this.ofSwampColors ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.RANDOM_ENTITIES) {
            return this.ofRandomEntities ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.SMOOTH_BIOMES) {
            return this.ofSmoothBiomes ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.CUSTOM_FONTS) {
            return this.ofCustomFonts ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.CUSTOM_COLORS) {
            return this.ofCustomColors ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.CUSTOM_SKY) {
            return this.ofCustomSky ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.SHOW_CAPES) {
            return this.ofShowCapes ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.CUSTOM_ITEMS) {
            return this.ofCustomItems ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.NATURAL_TEXTURES) {
            return this.ofNaturalTextures ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.EMISSIVE_TEXTURES) {
            return this.ofEmissiveTextures ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.FAST_MATH) {
            return this.ofFastMath ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.FAST_RENDER) {
            return this.ofFastRender ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.TRANSLUCENT_BLOCKS) {
            return this.ofTranslucentBlocks == 1 ? s + Lang.getFast() : (this.ofTranslucentBlocks == 2 ? s + Lang.getFancy() : s + Lang.getDefault());
        } else if (option == GameSettings.Options.LAZY_CHUNK_LOADING) {
            return this.ofLazyChunkLoading ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.RENDER_REGIONS) {
            return this.ofRenderRegions ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.SMART_ANIMATIONS) {
            return this.ofSmartAnimations ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.DYNAMIC_FOV) {
            return this.ofDynamicFov ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.ALTERNATE_BLOCKS) {
            return this.ofAlternateBlocks ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.DYNAMIC_LIGHTS) {
            int k = indexOf(this.ofDynamicLights, OF_DYNAMIC_LIGHTS);
            return s + getTranslation(KEYS_DYNAMIC_LIGHTS, k);
        } else if (option == GameSettings.Options.SCREENSHOT_SIZE) {
            return this.ofScreenshotSize <= 1 ? s + Lang.getDefault() : s + this.ofScreenshotSize + "x";
        } else if (option == GameSettings.Options.CUSTOM_ENTITY_MODELS) {
            return this.ofCustomEntityModels ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.CUSTOM_GUIS) {
            return this.ofCustomGuis ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.SHOW_GL_ERRORS) {
            return this.ofShowGlErrors ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.FULLSCREEN_MODE) {
            return this.ofFullscreenMode.equals("Default") ? s + Lang.getDefault() : s + this.ofFullscreenMode;
        } else if (option == GameSettings.Options.HELD_ITEM_TOOLTIPS) {
            return this.heldItemTooltips ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.ADVANCED_TOOLTIPS) {
            return this.advancedItemTooltips ? s + Lang.getOn() : s + Lang.getOff();
        } else if (option == GameSettings.Options.FRAMERATE_LIMIT) {
            float f = this.getOptionFloatValue(option);
            return f == 0.0F ? s + Lang.get("of.options.framerateLimit.vsync") : (f == option.valueMax ? s + I18n.format("options.framerateLimit.max") : s + (int)f + " fps");
        } else {
            return null;
        }
    }

    public void loadOfOptions() {
        try {
            File file1 = this.optionsFileOF;

            if (!file1.exists()) {
                file1 = this.optionsFile;
            }

            if (!file1.exists()) {
                return;
            }

            BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(new FileInputStream(file1), StandardCharsets.UTF_8));
            String s;

            while ((s = bufferedreader.readLine()) != null) {
                try {
                    String[] astring = s.split(":");

                    if (astring[0].equals("ofRenderDistanceChunks") && astring.length >= 2) {
                        this.renderDistanceChunks = Integer.parseInt(astring[1]);
                        this.renderDistanceChunks = Config.limit(this.renderDistanceChunks, 2, 1024);
                    }

                    if (astring[0].equals("ofFogType") && astring.length >= 2) {
                        this.ofFogType = Integer.parseInt(astring[1]);
                        this.ofFogType = Config.limit(this.ofFogType, 1, 3);
                    }

                    if (astring[0].equals("ofFogStart") && astring.length >= 2) {
                        this.ofFogStart = Float.parseFloat(astring[1]);

                        if (this.ofFogStart < 0.2F)
                        {
                            this.ofFogStart = 0.2F;
                        }

                        if (this.ofFogStart > 0.81F)
                        {
                            this.ofFogStart = 0.8F;
                        }
                    }

                    if (astring[0].equals("ofMipmapType") && astring.length >= 2) {
                        this.ofMipmapType = Integer.parseInt(astring[1]);
                        this.ofMipmapType = Config.limit(this.ofMipmapType, 0, 3);
                    }

                    if (astring[0].equals("ofOcclusionFancy") && astring.length >= 2) {
                        this.ofOcclusionFancy = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofSmoothFps") && astring.length >= 2) {
                        this.ofSmoothFps = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofSmoothWorld") && astring.length >= 2) {
                        this.ofSmoothWorld = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofAoLevel") && astring.length >= 2) {
                        this.ofAoLevel = Float.parseFloat(astring[1]);
                        this.ofAoLevel = Config.limit(this.ofAoLevel, 0.0F, 1.0F);
                    }

                    if (astring[0].equals("ofClouds") && astring.length >= 2) {
                        this.ofClouds = Integer.parseInt(astring[1]);
                        this.ofClouds = Config.limit(this.ofClouds, 0, 3);
                        this.updateRenderClouds();
                    }

                    if (astring[0].equals("ofCloudsHeight") && astring.length >= 2) {
                        this.ofCloudsHeight = Float.parseFloat(astring[1]);
                        this.ofCloudsHeight = Config.limit(this.ofCloudsHeight, 0.0F, 1.0F);
                    }

                    if (astring[0].equals("ofTrees") && astring.length >= 2) {
                        this.ofTrees = Integer.parseInt(astring[1]);
                        this.ofTrees = limit(this.ofTrees, OF_TREES_VALUES);
                    }

                    if (astring[0].equals("ofDroppedItems") && astring.length >= 2) {
                        this.ofDroppedItems = Integer.parseInt(astring[1]);
                        this.ofDroppedItems = Config.limit(this.ofDroppedItems, 0, 2);
                    }

                    if (astring[0].equals("ofRain") && astring.length >= 2) {
                        this.ofRain = Integer.parseInt(astring[1]);
                        this.ofRain = Config.limit(this.ofRain, 0, 3);
                    }

                    if (astring[0].equals("ofAnimatedWater") && astring.length >= 2) {
                        this.ofAnimatedWater = Integer.parseInt(astring[1]);
                        this.ofAnimatedWater = Config.limit(this.ofAnimatedWater, 0, 2);
                    }

                    if (astring[0].equals("ofAnimatedLava") && astring.length >= 2) {
                        this.ofAnimatedLava = Integer.parseInt(astring[1]);
                        this.ofAnimatedLava = Config.limit(this.ofAnimatedLava, 0, 2);
                    }

                    if (astring[0].equals("ofAnimatedFire") && astring.length >= 2) {
                        this.ofAnimatedFire = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedPortal") && astring.length >= 2) {
                        this.ofAnimatedPortal = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedRedstone") && astring.length >= 2) {
                        this.ofAnimatedRedstone = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedExplosion") && astring.length >= 2) {
                        this.ofAnimatedExplosion = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedFlame") && astring.length >= 2) {
                        this.ofAnimatedFlame = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedSmoke") && astring.length >= 2) {
                        this.ofAnimatedSmoke = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofVoidParticles") && astring.length >= 2) {
                        this.ofVoidParticles = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofWaterParticles") && astring.length >= 2) {
                        this.ofWaterParticles = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofPortalParticles") && astring.length >= 2) {
                        this.ofPortalParticles = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofPotionParticles") && astring.length >= 2) {
                        this.ofPotionParticles = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofFireworkParticles") && astring.length >= 2) {
                        this.ofFireworkParticles = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofDrippingWaterLava") && astring.length >= 2) {
                        this.ofDrippingWaterLava = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedTerrain") && astring.length >= 2) {
                        this.ofAnimatedTerrain = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofAnimatedTextures") && astring.length >= 2) {
                        this.ofAnimatedTextures = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofRainSplash") && astring.length >= 2) {
                        this.ofRainSplash = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofLagometer") && astring.length >= 2) {
                        this.ofLagometer = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofShowFps") && astring.length >= 2) {
                        this.ofShowFps = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofAutoSaveTicks") && astring.length >= 2) {
                        this.ofAutoSaveTicks = Integer.parseInt(astring[1]);
                        this.ofAutoSaveTicks = Config.limit(this.ofAutoSaveTicks, 40, 40000);
                    }

                    if (astring[0].equals("ofBetterGrass") && astring.length >= 2) {
                        this.ofBetterGrass = Integer.parseInt(astring[1]);
                        this.ofBetterGrass = Config.limit(this.ofBetterGrass, 1, 3);
                    }

                    if (astring[0].equals("ofConnectedTextures") && astring.length >= 2) {
                        this.ofConnectedTextures = Integer.parseInt(astring[1]);
                        this.ofConnectedTextures = Config.limit(this.ofConnectedTextures, 1, 3);
                    }

                    if (astring[0].equals("ofWeather") && astring.length >= 2) {
                        this.ofWeather = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofSky") && astring.length >= 2) {
                        this.ofSky = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofStars") && astring.length >= 2) {
                        this.ofStars = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofSunMoon") && astring.length >= 2) {
                        this.ofSunMoon = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofVignette") && astring.length >= 2) {
                        this.ofVignette = Integer.parseInt(astring[1]);
                        this.ofVignette = Config.limit(this.ofVignette, 0, 2);
                    }

                    if (astring[0].equals("ofChunkUpdates") && astring.length >= 2) {
                        this.ofChunkUpdates = Integer.parseInt(astring[1]);
                        this.ofChunkUpdates = Config.limit(this.ofChunkUpdates, 1, 5);
                    }

                    if (astring[0].equals("ofChunkUpdatesDynamic") && astring.length >= 2) {
                        this.ofChunkUpdatesDynamic = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofTime") && astring.length >= 2) {
                        this.ofTime = Integer.parseInt(astring[1]);
                        this.ofTime = Config.limit(this.ofTime, 0, 2);
                    }

                    if (astring[0].equals("ofClearWater") && astring.length >= 2) {
                        this.ofClearWater = Boolean.parseBoolean(astring[1]);
                        this.updateWaterOpacity();
                    }

                    if (astring[0].equals("ofAaLevel") && astring.length >= 2) {
                        this.ofAaLevel = Integer.parseInt(astring[1]);
                        this.ofAaLevel = Config.limit(this.ofAaLevel, 0, 16);
                    }

                    if (astring[0].equals("ofAfLevel") && astring.length >= 2) {
                        this.ofAfLevel = Integer.parseInt(astring[1]);
                        this.ofAfLevel = Config.limit(this.ofAfLevel, 1, 16);
                    }

                    if (astring[0].equals("ofProfiler") && astring.length >= 2) {
                        this.ofProfiler = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofBetterSnow") && astring.length >= 2) {
                        this.ofBetterSnow = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofSwampColors") && astring.length >= 2) {
                        this.ofSwampColors = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofRandomEntities") && astring.length >= 2) {
                        this.ofRandomEntities = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofSmoothBiomes") && astring.length >= 2) {
                        this.ofSmoothBiomes = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofCustomFonts") && astring.length >= 2) {
                        this.ofCustomFonts = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofCustomColors") && astring.length >= 2) {
                        this.ofCustomColors = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofCustomItems") && astring.length >= 2) {
                        this.ofCustomItems = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofCustomSky") && astring.length >= 2) {
                        this.ofCustomSky = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofShowCapes") && astring.length >= 2) {
                        this.ofShowCapes = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofNaturalTextures") && astring.length >= 2) {
                        this.ofNaturalTextures = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofEmissiveTextures") && astring.length >= 2) {
                        this.ofEmissiveTextures = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofLazyChunkLoading") && astring.length >= 2) {
                        this.ofLazyChunkLoading = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofRenderRegions") && astring.length >= 2) {
                        this.ofRenderRegions = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofSmartAnimations") && astring.length >= 2) {
                        this.ofSmartAnimations = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofDynamicFov") && astring.length >= 2) {
                        this.ofDynamicFov = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofAlternateBlocks") && astring.length >= 2) {
                        this.ofAlternateBlocks = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofDynamicLights") && astring.length >= 2) {
                        this.ofDynamicLights = Integer.parseInt(astring[1]);
                        this.ofDynamicLights = limit(this.ofDynamicLights, OF_DYNAMIC_LIGHTS);
                    }

                    if (astring[0].equals("ofScreenshotSize") && astring.length >= 2) {
                        this.ofScreenshotSize = Integer.parseInt(astring[1]);
                        this.ofScreenshotSize = Config.limit(this.ofScreenshotSize, 1, 4);
                    }

                    if (astring[0].equals("ofCustomEntityModels") && astring.length >= 2) {
                        this.ofCustomEntityModels = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofCustomGuis") && astring.length >= 2) {
                        this.ofCustomGuis = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofShowGlErrors") && astring.length >= 2) {
                        this.ofShowGlErrors = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofFullscreenMode") && astring.length >= 2) {
                        this.ofFullscreenMode = astring[1];
                    }

                    if (astring[0].equals("ofFastMath") && astring.length >= 2) {
                        this.ofFastMath = Boolean.parseBoolean(astring[1]);
                        MathHelper.fastMath = this.ofFastMath;
                    }

                    if (astring[0].equals("ofFastRender") && astring.length >= 2) {
                        this.ofFastRender = Boolean.parseBoolean(astring[1]);
                    }

                    if (astring[0].equals("ofTranslucentBlocks") && astring.length >= 2) {
                        this.ofTranslucentBlocks = Integer.parseInt(astring[1]);
                        this.ofTranslucentBlocks = Config.limit(this.ofTranslucentBlocks, 0, 2);
                    }

                    if (astring[0].equals("key_" + this.ofKeyBindZoom.getKeyDescription())) {
                        this.ofKeyBindZoom.setKeyCode(Integer.parseInt(astring[1]));
                    }
                } catch (Exception exception) {
                    Config.dbg("Skipping bad option: " + s);
                    exception.printStackTrace();
                }
            }

            KeyUtils.fixKeyConflicts(this.keyBindings, new KeyBinding[] {this.ofKeyBindZoom});
            KeyBinding.resetKeyBindingArrayAndHash();
            bufferedreader.close();
        } catch (Exception exception1) {
            Config.warn("Failed to load options");
            exception1.printStackTrace();
        }
    }

    public void saveOfOptions() {
        try {
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(this.optionsFileOF), StandardCharsets.UTF_8));
            writer.println("ofFogType:" + this.ofFogType);
            writer.println("ofFogStart:" + this.ofFogStart);
            writer.println("ofMipmapType:" + this.ofMipmapType);
            writer.println("ofOcclusionFancy:" + this.ofOcclusionFancy);
            writer.println("ofSmoothFps:" + this.ofSmoothFps);
            writer.println("ofSmoothWorld:" + this.ofSmoothWorld);
            writer.println("ofAoLevel:" + this.ofAoLevel);
            writer.println("ofClouds:" + this.ofClouds);
            writer.println("ofCloudsHeight:" + this.ofCloudsHeight);
            writer.println("ofTrees:" + this.ofTrees);
            writer.println("ofDroppedItems:" + this.ofDroppedItems);
            writer.println("ofRain:" + this.ofRain);
            writer.println("ofAnimatedWater:" + this.ofAnimatedWater);
            writer.println("ofAnimatedLava:" + this.ofAnimatedLava);
            writer.println("ofAnimatedFire:" + this.ofAnimatedFire);
            writer.println("ofAnimatedPortal:" + this.ofAnimatedPortal);
            writer.println("ofAnimatedRedstone:" + this.ofAnimatedRedstone);
            writer.println("ofAnimatedExplosion:" + this.ofAnimatedExplosion);
            writer.println("ofAnimatedFlame:" + this.ofAnimatedFlame);
            writer.println("ofAnimatedSmoke:" + this.ofAnimatedSmoke);
            writer.println("ofVoidParticles:" + this.ofVoidParticles);
            writer.println("ofWaterParticles:" + this.ofWaterParticles);
            writer.println("ofPortalParticles:" + this.ofPortalParticles);
            writer.println("ofPotionParticles:" + this.ofPotionParticles);
            writer.println("ofFireworkParticles:" + this.ofFireworkParticles);
            writer.println("ofDrippingWaterLava:" + this.ofDrippingWaterLava);
            writer.println("ofAnimatedTerrain:" + this.ofAnimatedTerrain);
            writer.println("ofAnimatedTextures:" + this.ofAnimatedTextures);
            writer.println("ofRainSplash:" + this.ofRainSplash);
            writer.println("ofLagometer:" + this.ofLagometer);
            writer.println("ofShowFps:" + this.ofShowFps);
            writer.println("ofAutoSaveTicks:" + this.ofAutoSaveTicks);
            writer.println("ofBetterGrass:" + this.ofBetterGrass);
            writer.println("ofConnectedTextures:" + this.ofConnectedTextures);
            writer.println("ofWeather:" + this.ofWeather);
            writer.println("ofSky:" + this.ofSky);
            writer.println("ofStars:" + this.ofStars);
            writer.println("ofSunMoon:" + this.ofSunMoon);
            writer.println("ofVignette:" + this.ofVignette);
            writer.println("ofChunkUpdates:" + this.ofChunkUpdates);
            writer.println("ofChunkUpdatesDynamic:" + this.ofChunkUpdatesDynamic);
            writer.println("ofTime:" + this.ofTime);
            writer.println("ofClearWater:" + this.ofClearWater);
            writer.println("ofAaLevel:" + this.ofAaLevel);
            writer.println("ofAfLevel:" + this.ofAfLevel);
            writer.println("ofProfiler:" + this.ofProfiler);
            writer.println("ofBetterSnow:" + this.ofBetterSnow);
            writer.println("ofSwampColors:" + this.ofSwampColors);
            writer.println("ofRandomEntities:" + this.ofRandomEntities);
            writer.println("ofSmoothBiomes:" + this.ofSmoothBiomes);
            writer.println("ofCustomFonts:" + this.ofCustomFonts);
            writer.println("ofCustomColors:" + this.ofCustomColors);
            writer.println("ofCustomItems:" + this.ofCustomItems);
            writer.println("ofCustomSky:" + this.ofCustomSky);
            writer.println("ofShowCapes:" + this.ofShowCapes);
            writer.println("ofNaturalTextures:" + this.ofNaturalTextures);
            writer.println("ofEmissiveTextures:" + this.ofEmissiveTextures);
            writer.println("ofLazyChunkLoading:" + this.ofLazyChunkLoading);
            writer.println("ofRenderRegions:" + this.ofRenderRegions);
            writer.println("ofSmartAnimations:" + this.ofSmartAnimations);
            writer.println("ofDynamicFov:" + this.ofDynamicFov);
            writer.println("ofAlternateBlocks:" + this.ofAlternateBlocks);
            writer.println("ofDynamicLights:" + this.ofDynamicLights);
            writer.println("ofScreenshotSize:" + this.ofScreenshotSize);
            writer.println("ofCustomEntityModels:" + this.ofCustomEntityModels);
            writer.println("ofCustomGuis:" + this.ofCustomGuis);
            writer.println("ofShowGlErrors:" + this.ofShowGlErrors);
            writer.println("ofFullscreenMode:" + this.ofFullscreenMode);
            writer.println("ofFastMath:" + this.ofFastMath);
            writer.println("ofFastRender:" + this.ofFastRender);
            writer.println("ofTranslucentBlocks:" + this.ofTranslucentBlocks);
            writer.println("key_" + this.ofKeyBindZoom.getKeyDescription() + ":" + this.ofKeyBindZoom.getKeyCode());
            writer.close();
        } catch (Exception exception) {
            Config.warn("Failed to save options");
            exception.printStackTrace();
        }
    }

    private void updateRenderClouds() {
        switch (this.ofClouds) {
            case 1:
                this.clouds = 1;
                break;
            case 2:
                this.clouds = 2;
                break;
            case 3:
                this.clouds = 0;
                break;
            default:
                if (this.fancyGraphics) {
                    this.clouds = 2;
                } else {
                    this.clouds = 1;
                }
        }
    }

    public void resetSettings() {
        this.renderDistanceChunks = 8;
        this.viewBobbing = true;
        this.anaglyph = false;
        this.limitFramerate = (int)GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
        this.enableVsync = false;
        this.updateVSync();
        this.mipmapLevels = 4;
        this.fancyGraphics = true;
        this.ambientOcclusion = 2;
        this.clouds = 2;
        this.fovSetting = 70.0F;
        this.gammaSetting = 0.0F;
        this.guiScale = 0;
        this.particleSetting = 0;
        this.heldItemTooltips = true;
        this.useVbo = false;
        this.forceUnicodeFont = false;
        this.ofFogType = 1;
        this.ofFogStart = 0.8F;
        this.ofMipmapType = 0;
        this.ofOcclusionFancy = false;
        this.ofSmartAnimations = false;
        this.ofSmoothFps = false;
        Config.updateAvailableProcessors();
        this.ofSmoothWorld = Config.isSingleProcessor();
        this.ofLazyChunkLoading = false;
        this.ofRenderRegions = false;
        this.ofFastMath = false;
        this.ofFastRender = false;
        this.ofTranslucentBlocks = 0;
        this.ofDynamicFov = true;
        this.ofAlternateBlocks = true;
        this.ofDynamicLights = 3;
        this.ofScreenshotSize = 1;
        this.ofCustomEntityModels = true;
        this.ofCustomGuis = true;
        this.ofShowGlErrors = true;
        this.ofAoLevel = 1.0F;
        this.ofAaLevel = 0;
        this.ofAfLevel = 1;
        this.ofClouds = 0;
        this.ofCloudsHeight = 0.0F;
        this.ofTrees = 0;
        this.ofRain = 0;
        this.ofBetterGrass = 3;
        this.ofAutoSaveTicks = 4000;
        this.ofLagometer = false;
        this.ofShowFps = false;
        this.ofProfiler = false;
        this.ofWeather = true;
        this.ofSky = true;
        this.ofStars = true;
        this.ofSunMoon = true;
        this.ofVignette = 0;
        this.ofChunkUpdates = 1;
        this.ofChunkUpdatesDynamic = false;
        this.ofTime = 0;
        this.ofClearWater = false;
        this.ofBetterSnow = false;
        this.ofFullscreenMode = "Default";
        this.ofSwampColors = true;
        this.ofRandomEntities = true;
        this.ofSmoothBiomes = true;
        this.ofCustomFonts = true;
        this.ofCustomColors = true;
        this.ofCustomItems = true;
        this.ofCustomSky = true;
        this.ofShowCapes = true;
        this.ofConnectedTextures = 2;
        this.ofNaturalTextures = false;
        this.ofEmissiveTextures = true;
        this.ofAnimatedWater = 0;
        this.ofAnimatedLava = 0;
        this.ofAnimatedFire = true;
        this.ofAnimatedPortal = true;
        this.ofAnimatedRedstone = true;
        this.ofAnimatedExplosion = true;
        this.ofAnimatedFlame = true;
        this.ofAnimatedSmoke = true;
        this.ofVoidParticles = true;
        this.ofWaterParticles = true;
        this.ofRainSplash = true;
        this.ofPortalParticles = true;
        this.ofPotionParticles = true;
        this.ofFireworkParticles = true;
        this.ofDrippingWaterLava = true;
        this.ofAnimatedTerrain = true;
        this.ofAnimatedTextures = true;
        Shaders.setShaderPack("OFF");
        Shaders.configAntialiasingLevel = 0;
        Shaders.uninit();
        Shaders.storeConfig();
        this.updateWaterOpacity();
        getMc().refreshResources();
        this.saveOptions();
    }

    public void updateVSync() {
        Display.setVSyncEnabled(this.enableVsync);
    }

    private void updateWaterOpacity() {
        if (Config.isIntegratedServerRunning()) {
            Config.waterOpacityChanged = true;
        }

        ClearWater.updateWaterOpacity(this, getMc().world);
    }

    public void setAllAnimations(boolean p_setAllAnimations_1_) {
        int i = p_setAllAnimations_1_ ? 0 : 2;
        this.ofAnimatedWater = i;
        this.ofAnimatedLava = i;
        this.ofAnimatedFire = p_setAllAnimations_1_;
        this.ofAnimatedPortal = p_setAllAnimations_1_;
        this.ofAnimatedRedstone = p_setAllAnimations_1_;
        this.ofAnimatedExplosion = p_setAllAnimations_1_;
        this.ofAnimatedFlame = p_setAllAnimations_1_;
        this.ofAnimatedSmoke = p_setAllAnimations_1_;
        this.ofVoidParticles = p_setAllAnimations_1_;
        this.ofWaterParticles = p_setAllAnimations_1_;
        this.ofRainSplash = p_setAllAnimations_1_;
        this.ofPortalParticles = p_setAllAnimations_1_;
        this.ofPotionParticles = p_setAllAnimations_1_;
        this.ofFireworkParticles = p_setAllAnimations_1_;
        this.particleSetting = p_setAllAnimations_1_ ? 0 : 2;
        this.ofDrippingWaterLava = p_setAllAnimations_1_;
        this.ofAnimatedTerrain = p_setAllAnimations_1_;
        this.ofAnimatedTextures = p_setAllAnimations_1_;
    }

    private static int nextValue(int p_nextValue_0_, int[] p_nextValue_1_) {
        int i = indexOf(p_nextValue_0_, p_nextValue_1_);

        if (i < 0) {
            return p_nextValue_1_[0];
        } else {
            ++i;

            if (i >= p_nextValue_1_.length) {
                i = 0;
            }

            return p_nextValue_1_[i];
        }
    }

    private static int limit(int p_limit_0_, int[] p_limit_1_) {
        int i = indexOf(p_limit_0_, p_limit_1_);
        return i < 0 ? p_limit_1_[0] : p_limit_0_;
    }

    private static int indexOf(int p_indexOf_0_, int[] p_indexOf_1_) {
        for (int i = 0; i < p_indexOf_1_.length; ++i) {
            if (p_indexOf_1_[i] == p_indexOf_0_) {
                return i;
            }
        }

        return -1;
    }

    public enum Options {
        INVERT_MOUSE("options.invertMouse", false, true),
        SENSITIVITY("options.sensitivity", true, false),
        FOV("options.fov", true, false, 30.0F, 110.0F, 1.0F),
        GAMMA("options.gamma", true, false),
        SATURATION("options.saturation", true, false),
        RENDER_DISTANCE("options.renderDistance", true, false, 2.0F, 16.0F, 1.0F),
        VIEW_BOBBING("options.viewBobbing", false, true),
        ANAGLYPH("options.anaglyph", false, true),
        FRAMERATE_LIMIT("options.framerateLimit", true, false, 0.0F, 260.0F, 5.0F),
        FBO_ENABLE("options.fboEnable", false, true),
        RENDER_CLOUDS("options.renderClouds", false, false),
        GRAPHICS("options.graphics", false, false),
        AMBIENT_OCCLUSION("options.ao", false, false),
        GUI_SCALE("options.guiScale", false, false),
        PARTICLES("options.particles", false, false),
        CHAT_VISIBILITY("options.chat.visibility", false, false),
        CHAT_COLOR("options.chat.color", false, true),
        CHAT_LINKS("options.chat.links", false, true),
        CHAT_OPACITY("options.chat.opacity", true, false),
        CHAT_LINKS_PROMPT("options.chat.links.prompt", false, true),
        SNOOPER_ENABLED("options.snooper", false, true),
        USE_FULLSCREEN("options.fullscreen", false, true),
        ENABLE_VSYNC("options.vsync", false, true),
        USE_VBO("options.vbo", false, true),
        CHAT_SCALE("options.chat.scale", true, false),
        CHAT_WIDTH("options.chat.width", true, false),
        CHAT_HEIGHT_FOCUSED("options.chat.height.focused", true, false),
        CHAT_HEIGHT_UNFOCUSED("options.chat.height.unfocused", true, false),
        MIPMAP_LEVELS("options.mipmapLevels", true, false, 0.0F, 4.0F, 1.0F),
        FORCE_UNICODE_FONT("options.forceUnicodeFont", false, true),
        BLOCK_ALTERNATIVES("options.blockAlternatives", false, true),
        REDUCED_DEBUG_INFO("options.reducedDebugInfo", false, true),
        ENTITY_SHADOWS("options.entityShadows", false, true),
        FOG_FANCY("of.options.FOG_FANCY", false, false),
        FOG_START("of.options.FOG_START", false, false),
        MIPMAP_TYPE("of.options.MIPMAP_TYPE", true, false, 0.0F, 3.0F, 1.0F),
        SMOOTH_FPS("of.options.SMOOTH_FPS", false, false),
        CLOUDS("of.options.CLOUDS", false, false),
        CLOUD_HEIGHT("of.options.CLOUD_HEIGHT", true, false),
        TREES("of.options.TREES", false, false),
        RAIN("of.options.RAIN", false, false),
        ANIMATED_WATER("of.options.ANIMATED_WATER", false, false),
        ANIMATED_LAVA("of.options.ANIMATED_LAVA", false, false),
        ANIMATED_FIRE("of.options.ANIMATED_FIRE", false, false),
        ANIMATED_PORTAL("of.options.ANIMATED_PORTAL", false, false),
        AO_LEVEL("of.options.AO_LEVEL", true, false),
        LAGOMETER("of.options.LAGOMETER", false, false),
        SHOW_FPS("of.options.SHOW_FPS", false, false),
        AUTOSAVE_TICKS("of.options.AUTOSAVE_TICKS", false, false),
        BETTER_GRASS("of.options.BETTER_GRASS", false, false),
        ANIMATED_REDSTONE("of.options.ANIMATED_REDSTONE", false, false),
        ANIMATED_EXPLOSION("of.options.ANIMATED_EXPLOSION", false, false),
        ANIMATED_FLAME("of.options.ANIMATED_FLAME", false, false),
        ANIMATED_SMOKE("of.options.ANIMATED_SMOKE", false, false),
        WEATHER("of.options.WEATHER", false, false),
        SKY("of.options.SKY", false, false),
        STARS("of.options.STARS", false, false),
        SUN_MOON("of.options.SUN_MOON", false, false),
        VIGNETTE("of.options.VIGNETTE", false, false),
        CHUNK_UPDATES("of.options.CHUNK_UPDATES", false, false),
        CHUNK_UPDATES_DYNAMIC("of.options.CHUNK_UPDATES_DYNAMIC", false, false),
        TIME("of.options.TIME", false, false),
        CLEAR_WATER("of.options.CLEAR_WATER", false, false),
        SMOOTH_WORLD("of.options.SMOOTH_WORLD", false, false),
        VOID_PARTICLES("of.options.VOID_PARTICLES", false, false),
        WATER_PARTICLES("of.options.WATER_PARTICLES", false, false),
        RAIN_SPLASH("of.options.RAIN_SPLASH", false, false),
        PORTAL_PARTICLES("of.options.PORTAL_PARTICLES", false, false),
        POTION_PARTICLES("of.options.POTION_PARTICLES", false, false),
        FIREWORK_PARTICLES("of.options.FIREWORK_PARTICLES", false, false),
        PROFILER("of.options.PROFILER", false, false),
        DRIPPING_WATER_LAVA("of.options.DRIPPING_WATER_LAVA", false, false),
        BETTER_SNOW("of.options.BETTER_SNOW", false, false),
        FULLSCREEN_MODE("of.options.FULLSCREEN_MODE", true, false, 0.0F, (float)Config.getDisplayModes().length, 1.0F),
        ANIMATED_TERRAIN("of.options.ANIMATED_TERRAIN", false, false),
        SWAMP_COLORS("of.options.SWAMP_COLORS", false, false),
        RANDOM_ENTITIES("of.options.RANDOM_ENTITIES", false, false),
        SMOOTH_BIOMES("of.options.SMOOTH_BIOMES", false, false),
        CUSTOM_FONTS("of.options.CUSTOM_FONTS", false, false),
        CUSTOM_COLORS("of.options.CUSTOM_COLORS", false, false),
        SHOW_CAPES("of.options.SHOW_CAPES", false, false),
        CONNECTED_TEXTURES("of.options.CONNECTED_TEXTURES", false, false),
        CUSTOM_ITEMS("of.options.CUSTOM_ITEMS", false, false),
        AA_LEVEL("of.options.AA_LEVEL", true, false, 0.0F, 16.0F, 1.0F),
        AF_LEVEL("of.options.AF_LEVEL", true, false, 1.0F, 16.0F, 1.0F),
        ANIMATED_TEXTURES("of.options.ANIMATED_TEXTURES", false, false),
        NATURAL_TEXTURES("of.options.NATURAL_TEXTURES", false, false),
        EMISSIVE_TEXTURES("of.options.EMISSIVE_TEXTURES", false, false),
        HELD_ITEM_TOOLTIPS("of.options.HELD_ITEM_TOOLTIPS", false, false),
        DROPPED_ITEMS("of.options.DROPPED_ITEMS", false, false),
        LAZY_CHUNK_LOADING("of.options.LAZY_CHUNK_LOADING", false, false),
        CUSTOM_SKY("of.options.CUSTOM_SKY", false, false),
        FAST_MATH("of.options.FAST_MATH", false, false),
        FAST_RENDER("of.options.FAST_RENDER", false, false),
        TRANSLUCENT_BLOCKS("of.options.TRANSLUCENT_BLOCKS", false, false),
        DYNAMIC_FOV("of.options.DYNAMIC_FOV", false, false),
        DYNAMIC_LIGHTS("of.options.DYNAMIC_LIGHTS", false, false),
        ALTERNATE_BLOCKS("of.options.ALTERNATE_BLOCKS", false, false),
        CUSTOM_ENTITY_MODELS("of.options.CUSTOM_ENTITY_MODELS", false, false),
        ADVANCED_TOOLTIPS("of.options.ADVANCED_TOOLTIPS", false, false),
        SCREENSHOT_SIZE("of.options.SCREENSHOT_SIZE", false, false),
        CUSTOM_GUIS("of.options.CUSTOM_GUIS", false, false),
        RENDER_REGIONS("of.options.RENDER_REGIONS", false, false),
        SHOW_GL_ERRORS("of.options.SHOW_GL_ERRORS", false, false),
        SMART_ANIMATIONS("of.options.SMART_ANIMATIONS", false, false);

        private final boolean enumFloat;
        private final boolean enumBoolean;
        private final String enumString;
        private final float valueStep;
        private final float valueMin;
        private float valueMax;

        public static GameSettings.Options getEnumOptions(int index) {
            for (GameSettings.Options options : values()) {
                if (options.returnEnumOrdinal() == index) {
                    return options;
                }
            }

            return null;
        }

        Options(String enumString, boolean enumFloat, boolean enumBoolean) {
            this(enumString, enumFloat, enumBoolean, 0.0F, 1.0F, 0.0F);
        }

        Options(String enumString, boolean enumFloat, boolean enumBoolean, float valueMin, float valueMax, float valueStep) {
            this.enumString = enumString;
            this.enumFloat = enumFloat;
            this.enumBoolean = enumBoolean;
            this.valueMin = valueMin;
            this.valueMax = valueMax;
            this.valueStep = valueStep;
        }

        public boolean getEnumFloat() {
            return this.enumFloat;
        }

        public boolean getEnumBoolean() {
            return this.enumBoolean;
        }

        public int returnEnumOrdinal() {
            return this.ordinal();
        }

        public String getEnumString() {
            return this.enumString;
        }

        public float getValueMax() {
            return this.valueMax;
        }

        public void setValueMax(float valueMax) {
            this.valueMax = valueMax;
        }

        public float normalizeValue(float p_148266_1_) {
            return MathHelper.clamp_float((this.snapToStepClamp(p_148266_1_) - this.valueMin) / (this.valueMax - this.valueMin), 0.0F, 1.0F);
        }

        public float denormalizeValue(float p_148262_1_) {
            return this.snapToStepClamp(this.valueMin + (this.valueMax - this.valueMin) * MathHelper.clamp_float(p_148262_1_, 0.0F, 1.0F));
        }

        public float snapToStepClamp(float p_148268_1_) {
            p_148268_1_ = this.snapToStep(p_148268_1_);
            return MathHelper.clamp_float(p_148268_1_, this.valueMin, this.valueMax);
        }

        protected float snapToStep(float p_148264_1_) {
            if (this.valueStep > 0.0F) {
                p_148264_1_ = this.valueStep * (float)Math.round(p_148264_1_ / this.valueStep);
            }

            return p_148264_1_;
        }
    }
}
