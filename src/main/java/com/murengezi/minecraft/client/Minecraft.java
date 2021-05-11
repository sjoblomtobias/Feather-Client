package com.murengezi.minecraft.client;

import com.darkmagician6.eventapi.EventManager;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.murengezi.chocolate.Chocolate;
import com.murengezi.chocolate.Event.*;
import com.murengezi.minecraft.block.Block;
import com.murengezi.minecraft.block.material.Material;
import com.murengezi.minecraft.client.audio.MusicTicker;
import com.murengezi.minecraft.client.audio.SoundHandler;
import com.murengezi.minecraft.client.entity.EntityPlayerSP;
import com.murengezi.minecraft.client.gui.Chat.ChatScreen;
import com.murengezi.minecraft.client.gui.*;
import com.murengezi.minecraft.client.gui.InGame.InGameMenuScreen;
import com.murengezi.minecraft.client.gui.InGame.InGameScreen;
import com.murengezi.minecraft.client.gui.InGame.SleepMPScreen;
import com.murengezi.minecraft.client.gui.Multiplayer.ConnectingScreen;
import com.murengezi.minecraft.client.main.GameConfiguration;
import com.murengezi.minecraft.client.multiplayer.PlayerControllerMP;
import com.murengezi.minecraft.client.multiplayer.ServerData;
import com.murengezi.minecraft.client.multiplayer.WorldClient;
import com.murengezi.minecraft.client.network.NetHandlerLoginClient;
import com.murengezi.minecraft.client.network.NetHandlerPlayClient;
import com.murengezi.minecraft.client.particle.EffectRenderer;
import com.murengezi.minecraft.client.renderer.*;
import com.murengezi.minecraft.client.renderer.chunk.RenderChunk;
import com.murengezi.minecraft.client.renderer.entity.RenderItem;
import com.murengezi.minecraft.client.renderer.entity.RenderManager;
import com.murengezi.minecraft.client.renderer.texture.DynamicTexture;
import com.murengezi.minecraft.client.renderer.texture.TextureManager;
import com.murengezi.minecraft.client.renderer.texture.TextureMap;
import com.murengezi.minecraft.client.renderer.vertex.DefaultVertexFormats;
import com.murengezi.minecraft.client.shader.Framebuffer;
import com.murengezi.minecraft.crash.CrashReport;
import com.murengezi.minecraft.crash.CrashReportCategory;
import com.murengezi.minecraft.profiler.IPlayerUsage;
import com.murengezi.minecraft.profiler.PlayerUsageSnooper;
import com.murengezi.minecraft.profiler.Profiler;
import net.minecraft.client.LoadingScreenRenderer;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.achievement.GuiAchievement;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.resources.*;
import net.minecraft.client.resources.data.*;
import net.minecraft.client.resources.model.ModelManager;
import com.murengezi.minecraft.client.settings.GameSettings;
import com.murengezi.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLeashKnot;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Bootstrap;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatFileWriter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Timer;
import net.minecraft.util.Util;
import net.minecraft.util.*;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldProviderEnd;
import net.minecraft.world.WorldProviderHell;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.chunk.storage.AnvilSaveConverter;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.util.glu.GLU;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

public class Minecraft implements IThreadListener, IPlayerUsage {

	private static final Logger logger = LogManager.getLogger();
	private static final ResourceLocation locationMojangPng = new ResourceLocation("textures/gui/title/mojang.png");
	public static final boolean isRunningOnMac = Util.getOSType() == Util.EnumOS.OSX;
	public static byte[] memoryReserve = new byte[10485760];
	private static final List<DisplayMode> macDisplayModes = Lists.newArrayList(new DisplayMode(2560, 1600), new DisplayMode(2880, 1800));
	private final File fileResourcepacks;
	private final PropertyMap profileProperties;
	private ServerData currentServerData;
	private TextureManager renderEngine;
	private static Minecraft theMinecraft;
	public PlayerControllerMP playerController;
	private boolean fullscreen;
	private boolean hasCrashed;
	private CrashReport crashReporter;
	public int displayWidth;
	public int displayHeight;
	private final Timer timer = new Timer(20.0F);
	private final PlayerUsageSnooper usageSnooper = new PlayerUsageSnooper("client", this, MinecraftServer.getCurrentTimeMillis());
	public WorldClient world;
	public RenderGlobal renderGlobal;
	private RenderManager renderManager;
	private RenderItem renderItem;
	private ItemRenderer itemRenderer;
	public EntityPlayerSP player;
	private Entity renderViewEntity;
	public Entity pointedEntity;
	public EffectRenderer effectRenderer;
	private final Session session;
	private boolean isGamePaused;
	public FontRenderer fontRenderer, galacticFontRenderer;
	public Screen currentScreen;
	public LoadingScreenRenderer loadingScreen;
	public EntityRenderer entityRenderer;
	private int leftClickCounter;
	private final int tempDisplayWidth, tempDisplayHeight;
	private IntegratedServer theIntegratedServer;
	public GuiAchievement guiAchievement;
	public InGameScreen inGameScreen;
	public boolean skipRenderWorld;
	public MovingObjectPosition objectMouseOver;
	public GameSettings gameSettings;
	public MouseHelper mouseHelper;
	public final File dataDir;
	private final File fileAssets;
	private final String launchedVersion;
	private final Proxy proxy;
	private ISaveFormat saveLoader;
	private static int debugFPS;
	private int rightClickDelayTimer;
	private String serverName;
	private int serverPort;
	public boolean inGameHasFocus;
	long systemTime = getSystemTime();
	private int joinPlayerCounter;
	public final FrameTimer frameTimer = new FrameTimer();
	long startNanoTime = System.nanoTime();
	private final boolean jvm64bit;
	private NetworkManager myNetworkManager;
	private boolean integratedServerIsRunning;
	public final Profiler mcProfiler = new Profiler();
	private long debugCrashKeyPressTime = -1L;
	private IReloadableResourceManager resourceManager;
	private final IMetadataSerializer metadataSerializer_ = new IMetadataSerializer();
	private final List<IResourcePack> defaultResourcePacks = Lists.newArrayList();
	private final DefaultResourcePack mcDefaultResourcePack;
	private ResourcePackRepository resourcePackRepository;
	private LanguageManager languageManager;
	private Framebuffer framebufferMc;
	private TextureMap textureMapBlocks;
	private SoundHandler soundHandler;
	private MusicTicker musicTicker;
	private ResourceLocation mojangLogo;
	private final MinecraftSessionService sessionService;
	private SkinManager skinManager;
	private final Queue<FutureTask<?>> scheduledTasks = Queues.newArrayDeque();
	private final Thread mcThread = Thread.currentThread();
	private BlockRendererDispatcher blockRenderDispatcher;
	volatile boolean running = true;
	public String debug = "";
	public boolean renderChunkMany = true;
	long debugUpdateTime = getSystemTime();
	int fpsCounter;
	long prevFrameTime = -1L;
	private String debugProfilerName = "root";

	public Minecraft(GameConfiguration gameConfig) {
		theMinecraft = this;
		this.dataDir = gameConfig.folderInfo.dataDir;
		this.fileAssets = gameConfig.folderInfo.assetsDir;
		this.fileResourcepacks = gameConfig.folderInfo.resourcePacksDir;
		this.launchedVersion = gameConfig.gameInfo.version;
		this.profileProperties = gameConfig.userInfo.profileProperties;
		this.mcDefaultResourcePack = new DefaultResourcePack((new ResourceIndex(gameConfig.folderInfo.assetsDir, gameConfig.folderInfo.assetIndex)).getResourceMap());
		this.proxy = gameConfig.userInfo.proxy == null ? Proxy.NO_PROXY : gameConfig.userInfo.proxy;
		this.sessionService = (new YggdrasilAuthenticationService(this.proxy, UUID.randomUUID().toString())).createMinecraftSessionService();
		this.session = gameConfig.userInfo.session;
		logger.info("Setting user: " + this.session.getUsername());
		logger.info("(Session ID is " + this.session.getSessionID() + ")");
		this.displayWidth = gameConfig.displayInfo.width > 0 ? gameConfig.displayInfo.width : 1;
		this.displayHeight = gameConfig.displayInfo.height > 0 ? gameConfig.displayInfo.height : 1;
		this.tempDisplayWidth = gameConfig.displayInfo.width;
		this.tempDisplayHeight = gameConfig.displayInfo.height;
		this.fullscreen = gameConfig.displayInfo.fullscreen;
		this.jvm64bit = isJvm64bit();
		this.theIntegratedServer = new IntegratedServer(this);

		//TODO use to connect to server on start
		if (gameConfig.serverInfo.serverName != null) {
			this.serverName = gameConfig.serverInfo.serverName;
			this.serverPort = gameConfig.serverInfo.serverPort;
		}

		ImageIO.setUseCache(false);
		Bootstrap.register();
	}

	public void run() {
		this.running = true;

		try {
			this.startGame();
		} catch (Throwable throwable) {
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Initializing game");
			crashreport.makeCategory("Initialization");
			this.displayCrashReport(this.addGraphicsAndWorldToCrashReport(crashreport));
			return;
		}

		while (true) {
			try {
				while (this.running) {
					if (!this.hasCrashed || this.crashReporter == null) {
						try {
							this.runGameLoop();
						} catch (OutOfMemoryError var10) {
							this.freeMemory();
							this.displayGuiScreen(new MemoryErrorScreen());
							System.gc();
						}
					} else {
						this.displayCrashReport(this.crashReporter);
					}
				}
			} catch (MinecraftError var12) {
				break;
			} catch (ReportedException reportedexception) {
				this.addGraphicsAndWorldToCrashReport(reportedexception.getCrashReport());
				this.freeMemory();
				logger.fatal("Reported exception thrown!", reportedexception);
				this.displayCrashReport(reportedexception.getCrashReport());
				break;
			} catch (Throwable throwable1) {
				CrashReport crashreport1 = this.addGraphicsAndWorldToCrashReport(new CrashReport("Unexpected error", throwable1));
				this.freeMemory();
				logger.fatal("Unreported exception thrown!", throwable1);
				this.displayCrashReport(crashreport1);
				break;
			} finally {
				this.shutdownMinecraftApplet();
			}

			return;
		}
	}

	private void startGame() throws LWJGLException {
		this.gameSettings = new GameSettings(this.dataDir);
		this.defaultResourcePacks.add(this.mcDefaultResourcePack);
		this.startTimerHackThread();

		if (this.gameSettings.overrideHeight > 0 && this.gameSettings.overrideWidth > 0) {
			this.displayWidth = this.gameSettings.overrideWidth;
			this.displayHeight = this.gameSettings.overrideHeight;
		}

		logger.info("LWJGL Version: " + Sys.getVersion());
		this.setWindowIcon();
		this.setInitialDisplayMode();
		this.createDisplay();
		OpenGlHelper.initializeTextures();
		this.framebufferMc = new Framebuffer(this.displayWidth, this.displayHeight, true);
		this.framebufferMc.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
		this.registerMetadataSerializers();
		this.resourcePackRepository = new ResourcePackRepository(this.fileResourcepacks, new File(this.dataDir, "server-resource-packs"), this.mcDefaultResourcePack, this.metadataSerializer_, this.gameSettings);
		this.resourceManager = new SimpleReloadableResourceManager(this.metadataSerializer_);
		this.languageManager = new LanguageManager(this.metadataSerializer_, this.gameSettings.language);
		this.resourceManager.registerReloadListener(this.languageManager);
		this.refreshResources();
		this.renderEngine = new TextureManager(this.resourceManager);
		this.resourceManager.registerReloadListener(this.renderEngine);
		this.drawSplashScreen(this.renderEngine);
		this.skinManager = new SkinManager(this.renderEngine, new File(this.fileAssets, "skins"), this.sessionService);
		this.saveLoader = new AnvilSaveConverter(new File(this.dataDir, "saves"));
		this.soundHandler = new SoundHandler(this.resourceManager, this.gameSettings);
		this.resourceManager.registerReloadListener(this.soundHandler);
		this.musicTicker = new MusicTicker(this);
		this.fontRenderer = new FontRenderer(this.gameSettings, new ResourceLocation("textures/font/ascii.png"), this.renderEngine, false);

		if (this.gameSettings.language != null) {
			this.fontRenderer.setUnicodeFlag(this.isUnicode());
			this.fontRenderer.setBidiFlag(this.languageManager.isCurrentLanguageBidirectional());
		}

		this.galacticFontRenderer = new FontRenderer(this.gameSettings, new ResourceLocation("textures/font/ascii_sga.png"), this.renderEngine, false);
		this.resourceManager.registerReloadListener(this.fontRenderer);
		this.resourceManager.registerReloadListener(this.galacticFontRenderer);
		this.resourceManager.registerReloadListener(new GrassColorReloadListener());
		this.resourceManager.registerReloadListener(new FoliageColorReloadListener());
		AchievementList.openInventory.setStatStringFormatter(p_74535_1_ -> {
			try {
				return String.format(p_74535_1_, GameSettings.getKeyDisplayString(Minecraft.this.gameSettings.keyBindInventory.getKeyCode()));
			} catch (Exception exception) {
				return "Error: " + exception.getLocalizedMessage();
			}
		});
		this.mouseHelper = new MouseHelper();
		this.checkGLError("Pre startup");
		GlStateManager.enableTexture2D();
		GlStateManager.shadeModel(7425);
		GlStateManager.clearDepth(1.0D);
		GlStateManager.enableDepth();
		GlStateManager.depthFunc(515);
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);
		GlStateManager.cullFace(1029);
		GlStateManager.matrixMode(5889);
		GlStateManager.loadIdentity();
		GlStateManager.matrixMode(5888);
		this.checkGLError("Startup");
		this.textureMapBlocks = new TextureMap("textures");
		this.textureMapBlocks.setMipmapLevels(this.gameSettings.mipmapLevels);
		this.renderEngine.loadTickableTexture(TextureMap.locationBlocksTexture, this.textureMapBlocks);
		this.renderEngine.bindTexture(TextureMap.locationBlocksTexture);
		this.textureMapBlocks.setBlurMipmapDirect(false, this.gameSettings.mipmapLevels > 0);
		ModelManager modelManager = new ModelManager(this.textureMapBlocks);
		this.resourceManager.registerReloadListener(modelManager);
		this.renderItem = new RenderItem(this.renderEngine, modelManager);
		this.renderManager = new RenderManager(this.renderEngine, this.renderItem);
		this.itemRenderer = new ItemRenderer(this);
		this.resourceManager.registerReloadListener(this.renderItem);
		this.entityRenderer = new EntityRenderer(this, this.resourceManager);
		this.resourceManager.registerReloadListener(this.entityRenderer);
		this.blockRenderDispatcher = new BlockRendererDispatcher(modelManager.getBlockModelShapes(), this.gameSettings);
		this.resourceManager.registerReloadListener(this.blockRenderDispatcher);
		this.renderGlobal = new RenderGlobal(this);
		this.resourceManager.registerReloadListener(this.renderGlobal);
		this.guiAchievement = new GuiAchievement(this);
		GlStateManager.viewport(0, 0, this.displayWidth, this.displayHeight);
		this.effectRenderer = new EffectRenderer(this.world, this.renderEngine);
		this.checkGLError("Post startup");
		this.inGameScreen = new InGameScreen();

		//TODO Chocolate Start
		Chocolate.start();

		if (this.serverName != null) {
			this.displayGuiScreen(new ConnectingScreen(new MainMenuScreen(), this.serverName, this.serverPort));
		} else {
			this.displayGuiScreen(new MainMenuScreen());
		}

		this.renderEngine.deleteTexture(this.mojangLogo);
		this.mojangLogo = null;
		this.loadingScreen = new LoadingScreenRenderer(this);

		if (this.gameSettings.fullScreen && !this.fullscreen) {
			this.toggleFullscreen();
		}

		try {
			Display.setVSyncEnabled(this.gameSettings.enableVsync);
		} catch (OpenGLException var2) {
			this.gameSettings.enableVsync = false;
			this.gameSettings.saveOptions();
		}

		this.renderGlobal.makeEntityOutlineShader();
	}

	private void registerMetadataSerializers() {
		this.metadataSerializer_.registerMetadataSectionType(new TextureMetadataSectionSerializer(), TextureMetadataSection.class);
		this.metadataSerializer_.registerMetadataSectionType(new FontMetadataSectionSerializer(), FontMetadataSection.class);
		this.metadataSerializer_.registerMetadataSectionType(new AnimationMetadataSectionSerializer(), AnimationMetadataSection.class);
		this.metadataSerializer_.registerMetadataSectionType(new PackMetadataSectionSerializer(), PackMetadataSection.class);
		this.metadataSerializer_.registerMetadataSectionType(new LanguageMetadataSectionSerializer(), LanguageMetadataSection.class);
	}

	private void createDisplay() throws LWJGLException {
		Display.setResizable(true);
		Display.setTitle("Chocolate (?-?/?)");

		try {
			Display.create((new PixelFormat()).withDepthBits(24));
		} catch (LWJGLException lwjglexception) {
			logger.error("Couldn't set pixel format", lwjglexception);

			try {
				Thread.sleep(1000L);
			} catch (InterruptedException ignored) {
			}

			if (this.fullscreen) {
				this.updateDisplayMode();
			}

			Display.create();
		}
	}

	private void setInitialDisplayMode() throws LWJGLException {
		if (this.fullscreen) {
			Display.setFullscreen(true);
			DisplayMode displaymode = Display.getDisplayMode();
			this.displayWidth = Math.max(1, displaymode.getWidth());
			this.displayHeight = Math.max(1, displaymode.getHeight());
		} else {
			Display.setDisplayMode(new DisplayMode(this.displayWidth, this.displayHeight));
		}
	}

	/**
	 * TODO Custom window icon
	 */
	private void setWindowIcon() {
		Util.EnumOS osType = Util.getOSType();

		if (osType != Util.EnumOS.OSX) {
			InputStream icon16 = null;
			InputStream icon32 = null;

			try {
				icon16 = this.mcDefaultResourcePack.getResourceStream(new ResourceLocation("chocolate/logo_16x16.png"));
				icon32 = this.mcDefaultResourcePack.getResourceStream(new ResourceLocation("chocolate/logo_32x32.png"));

				if (icon16 != null && icon32 != null) {
					Display.setIcon(new ByteBuffer[]{this.readImageToBuffer(icon16), this.readImageToBuffer(icon32)});
				}
			} catch (IOException ioexception) {
				logger.error("Couldn't set icon", ioexception);
			} finally {
				IOUtils.closeQuietly(icon16);
				IOUtils.closeQuietly(icon32);
			}
		}
	}

	private static boolean isJvm64bit() {
		String[] astring = new String[]{"sun.arch.data.model", "com.ibm.vm.bitmode", "os.arch"};

		for (String s : astring) {
			String s1 = System.getProperty(s);

			if (s1 != null && s1.contains("64")) {
				return true;
			}
		}

		return false;
	}

	public Framebuffer getFramebuffer() {
		return this.framebufferMc;
	}

	public String getVersion() {
		return this.launchedVersion;
	}

	private void startTimerHackThread() {
		Thread thread = new Thread("Timer hack thread") {
			public void run() {
				while (Minecraft.this.running) {
					try {
						Thread.sleep(2147483647L);
					} catch (InterruptedException ignored) {
					}
				}
			}
		};
		thread.setDaemon(true);
		thread.start();
	}

	public void crashed(CrashReport crash) {
		this.hasCrashed = true;
		this.crashReporter = crash;
	}

	public void displayCrashReport(CrashReport crashReportIn) {
		File file1 = new File(getMinecraft().dataDir, "crash-reports");
		File file2 = new File(file1, "crash-" + (new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss")).format(new Date()) + "-client.txt");
		Bootstrap.printToSYSOUT(crashReportIn.getCompleteReport());

		if (crashReportIn.getFile() != null) {
			Bootstrap.printToSYSOUT("#@!@# Game crashed! Crash report saved to: #@!@# " + crashReportIn.getFile());
			System.exit(-1);
		} else if (crashReportIn.saveToFile(file2)) {
			Bootstrap.printToSYSOUT("#@!@# Game crashed! Crash report saved to: #@!@# " + file2.getAbsolutePath());
			System.exit(-1);
		} else {
			Bootstrap.printToSYSOUT("#@?@# Game crashed! Crash report could not be saved. #@?@#");
			System.exit(-2);
		}
	}

	public boolean isUnicode() {
		return this.languageManager.isCurrentLocaleUnicode() || this.gameSettings.forceUnicodeFont;
	}

	public void refreshResources() {
		List<IResourcePack> list = Lists.newArrayList(this.defaultResourcePacks);

		for (ResourcePackRepository.Entry resourcepackrepository$entry : this.resourcePackRepository.getRepositoryEntries()) {
			list.add(resourcepackrepository$entry.getResourcePack());
		}

		if (this.resourcePackRepository.getResourcePackInstance() != null) {
			list.add(this.resourcePackRepository.getResourcePackInstance());
		}

		try {
			this.resourceManager.reloadResources(list);
		} catch (RuntimeException runtimeexception) {
			logger.info("Caught error stitching, removing all assigned resourcepacks", runtimeexception);
			list.clear();
			list.addAll(this.defaultResourcePacks);
			this.resourcePackRepository.setRepositories(Collections.emptyList());
			this.resourceManager.reloadResources(list);
			this.gameSettings.resourcePacks.clear();
			this.gameSettings.field_183018_l.clear();
			this.gameSettings.saveOptions();
		}

		this.languageManager.parseLanguageMetadata(list);

		if (this.renderGlobal != null) {
			this.renderGlobal.loadRenderers();
		}
	}

	private ByteBuffer readImageToBuffer(InputStream imageStream) throws IOException {
		BufferedImage bufferedimage = ImageIO.read(imageStream);
		int[] aint = bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), null, 0, bufferedimage.getWidth());
		ByteBuffer bytebuffer = ByteBuffer.allocate(4 * aint.length);

		for (int i : aint) {
			bytebuffer.putInt(i << 8 | i >> 24 & 255);
		}

		bytebuffer.flip();
		return bytebuffer;
	}

	private void updateDisplayMode() throws LWJGLException {
		Set<DisplayMode> set = Sets.newHashSet();
		Collections.addAll(set, Display.getAvailableDisplayModes());
		DisplayMode displaymode = Display.getDesktopDisplayMode();

		if (!set.contains(displaymode) && Util.getOSType() == Util.EnumOS.OSX) {
			label53:

			for (DisplayMode displaymode1 : macDisplayModes) {
				boolean flag = true;

				for (DisplayMode displaymode2 : set) {
					if (displaymode2.getBitsPerPixel() == 32 && displaymode2.getWidth() == displaymode1.getWidth() && displaymode2.getHeight() == displaymode1.getHeight()) {
						flag = false;
						break;
					}
				}

				if (!flag) {
					Iterator iterator = set.iterator();
					DisplayMode displaymode3;

                    do {
                        if (!iterator.hasNext()) {
                            continue label53;
                        }

                        displaymode3 = (DisplayMode) iterator.next();

                    } while (displaymode3.getBitsPerPixel() != 32 || displaymode3.getWidth() != displaymode1.getWidth() / 2 || displaymode3.getHeight() != displaymode1.getHeight() / 2);

					displaymode = displaymode3;
				}
			}
		}

		Display.setDisplayMode(displaymode);
		this.displayWidth = displaymode.getWidth();
		this.displayHeight = displaymode.getHeight();
	}

	private void drawSplashScreen(TextureManager textureManagerInstance) {
		ScaledResolution scaledresolution = new ScaledResolution();
		int i = scaledresolution.getScaleFactor();
		Framebuffer framebuffer = new Framebuffer(scaledresolution.getScaledWidth() * i, scaledresolution.getScaledHeight() * i, true);
		framebuffer.bindFramebuffer(false);
		GlStateManager.matrixMode(5889);
		GlStateManager.loadIdentity();
		GlStateManager.ortho(0.0D, scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight(), 0.0D, 1000.0D, 3000.0D);
		GlStateManager.matrixMode(5888);
		GlStateManager.loadIdentity();
		GlStateManager.translate(0.0F, 0.0F, -2000.0F);
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		GlStateManager.disableDepth();
		GlStateManager.enableTexture2D();
		InputStream inputstream = null;

		try {
			inputstream = this.mcDefaultResourcePack.getInputStream(locationMojangPng);
			this.mojangLogo = textureManagerInstance.getDynamicTextureLocation("logo", new DynamicTexture(ImageIO.read(inputstream)));
			textureManagerInstance.bindTexture(this.mojangLogo);
		} catch (IOException ioexception) {
			logger.error("Unable to load logo: " + locationMojangPng, ioexception);
		} finally {
			IOUtils.closeQuietly(inputstream);
		}

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		worldrenderer.pos(0.0D, this.displayHeight, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
		worldrenderer.pos(this.displayWidth, this.displayHeight, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
		worldrenderer.pos(this.displayWidth, 0.0D, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
		worldrenderer.pos(0.0D, 0.0D, 0.0D).tex(0.0D, 0.0D).color(255, 255, 255, 255).endVertex();
		tessellator.draw();
		GlStateManager.colorAllMax();
		int j = 256;
		int k = 256;
		this.func_181536_a((scaledresolution.getScaledWidth() - j) / 2, (scaledresolution.getScaledHeight() - k) / 2, 0, 0, j, k, 255, 255, 255, 255);
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		framebuffer.unbindFramebuffer();
		framebuffer.framebufferRender(scaledresolution.getScaledWidth() * i, scaledresolution.getScaledHeight() * i);
		GlStateManager.enableAlpha();
		GlStateManager.alphaFunc(516, 0.1F);
		this.updateDisplay();
	}

	public void func_181536_a(int p_181536_1_, int p_181536_2_, int p_181536_3_, int p_181536_4_, int p_181536_5_, int p_181536_6_, int p_181536_7_, int p_181536_8_, int p_181536_9_, int p_181536_10_) {
		float f = 0.00390625F;
		float f1 = 0.00390625F;
		WorldRenderer worldrenderer = Tessellator.getInstance().getWorldRenderer();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
		worldrenderer.pos(p_181536_1_, p_181536_2_ + p_181536_6_, 0.0D).tex((float) p_181536_3_ * f, (float) (p_181536_4_ + p_181536_6_) * f1).color(p_181536_7_, p_181536_8_, p_181536_9_, p_181536_10_).endVertex();
		worldrenderer.pos(p_181536_1_ + p_181536_5_, p_181536_2_ + p_181536_6_, 0.0D).tex((float) (p_181536_3_ + p_181536_5_) * f, (float) (p_181536_4_ + p_181536_6_) * f1).color(p_181536_7_, p_181536_8_, p_181536_9_, p_181536_10_).endVertex();
		worldrenderer.pos(p_181536_1_ + p_181536_5_, p_181536_2_, 0.0D).tex((float) (p_181536_3_ + p_181536_5_) * f, (float) p_181536_4_ * f1).color(p_181536_7_, p_181536_8_, p_181536_9_, p_181536_10_).endVertex();
		worldrenderer.pos(p_181536_1_, p_181536_2_, 0.0D).tex((float) p_181536_3_ * f, (float) p_181536_4_ * f1).color(p_181536_7_, p_181536_8_, p_181536_9_, p_181536_10_).endVertex();
		Tessellator.getInstance().draw();
	}

	public ISaveFormat getSaveLoader() {
		return this.saveLoader;
	}

	public void displayGuiScreen(Screen screen) {
		if (this.currentScreen != null) {
			this.currentScreen.onGuiClosed();
		}

		if (screen == null && this.world == null) {
			screen = new MainMenuScreen();
		} else if (screen == null && this.player.getHealth() <= 0.0F) {
			screen = new GameOverScreen();
		}

		if (screen instanceof MainMenuScreen) {
			this.gameSettings.showDebugInfo = false;
			this.inGameScreen.getChatGUI().clearChatMessages();
		}

		this.currentScreen = screen;

		if (screen != null) {
			this.setIngameNotInFocus();
			ScaledResolution scaledresolution = new ScaledResolution();
			screen.setWorldAndResolution(scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight());
			this.skipRenderWorld = false;
		} else {
			this.soundHandler.resumeSounds();
			this.setIngameFocus();
		}
	}

	private void checkGLError(String message) {
        int i = GL11.glGetError();

        if (i != 0) {
            String s = GLU.gluErrorString(i);
            logger.error("########## GL ERROR ##########");
            logger.error("@ " + message);
            logger.error(i + ": " + s);
        }
    }

	public void shutdownMinecraftApplet() {
		try {
			logger.info("Stopping!");

			try {
				this.loadWorld(null);
			} catch (Throwable ignored) {
			}

			this.soundHandler.unloadSounds();
		} finally {
			Display.destroy();

			if (!this.hasCrashed) {
				System.exit(0);
			}
		}

		System.gc();
	}

	private void runGameLoop() throws IOException {
		long i = System.nanoTime();
		this.mcProfiler.startSection("root");

		if (Display.isCreated() && Display.isCloseRequested()) {
			this.shutdown();
		}

		if (this.isGamePaused && this.world != null) {
			float f = this.timer.renderPartialTicks;
			this.timer.updateTimer();
			this.timer.renderPartialTicks = f;
		} else {
			this.timer.updateTimer();
		}

		this.mcProfiler.startSection("scheduledExecutables");

		synchronized (this.scheduledTasks) {
			while (!this.scheduledTasks.isEmpty()) {
				Util.func_181617_a((FutureTask) this.scheduledTasks.poll(), logger);
			}
		}

		this.mcProfiler.endSection();
        this.mcProfiler.startSection("tick");

		for (int j = 0; j < this.timer.elapsedTicks; ++j) {
			this.runTick();
		}

		this.mcProfiler.endStartSection("preRenderErrors");
        this.checkGLError("Pre render");
		this.mcProfiler.endStartSection("sound");
		this.soundHandler.setListener(this.player, this.timer.renderPartialTicks);
		this.mcProfiler.endSection();
		this.mcProfiler.startSection("render");
		GlStateManager.pushMatrix();
		GlStateManager.clear(16640);
		this.framebufferMc.bindFramebuffer(true);
		this.mcProfiler.startSection("display");
		GlStateManager.enableTexture2D();

		if (this.player != null && this.player.isEntityInsideOpaqueBlock()) {
			this.gameSettings.thirdPersonView = 0;
		}

		this.mcProfiler.endSection();

		if (!this.skipRenderWorld) {
			this.mcProfiler.endStartSection("gameRenderer");
			this.entityRenderer.updateCameraAndRender(this.timer.renderPartialTicks, i);
			this.mcProfiler.endSection();
		}

		this.mcProfiler.endSection();

		if (this.gameSettings.showDebugInfo && this.gameSettings.showDebugProfilerChart && !this.gameSettings.hideGUI) {
			if (!this.mcProfiler.profilingEnabled) {
				this.mcProfiler.clearProfiling();
			}

			this.mcProfiler.profilingEnabled = true;
			this.displayDebugInfo();
		} else {
			this.mcProfiler.profilingEnabled = false;
			this.prevFrameTime = System.nanoTime();
		}

		this.guiAchievement.updateAchievementWindow();
		this.framebufferMc.unbindFramebuffer();
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		this.framebufferMc.framebufferRender(this.displayWidth, this.displayHeight);
		GlStateManager.popMatrix();
		this.mcProfiler.startSection("root");
		this.updateDisplay();
		Thread.yield();
		/**
		 * TODO Remove stream
		 */
		this.mcProfiler.startSection("update");
		this.mcProfiler.endStartSection("submit");
		this.mcProfiler.endSection();
		this.checkGLError("Post render");
		++this.fpsCounter;
		this.isGamePaused = this.isSingleplayer() && this.currentScreen != null && this.currentScreen.doesGuiPauseGame() && !this.theIntegratedServer.getPublic();
		long k = System.nanoTime();
		this.frameTimer.addFrame(k - this.startNanoTime);
		this.startNanoTime = k;

		while (getSystemTime() >= this.debugUpdateTime + 1000L) {
			debugFPS = this.fpsCounter;
			this.debug = String.format("%d fps (%d chunk update%s) T: %s%s%s%s%s", debugFPS, RenderChunk.renderChunksUpdated, RenderChunk.renderChunksUpdated != 1 ? "s" : "", (float) this.gameSettings.limitFramerate == GameSettings.Options.FRAMERATE_LIMIT.getValueMax() ? "inf" : Integer.valueOf(this.gameSettings.limitFramerate), this.gameSettings.enableVsync ? " vsync" : "", this.gameSettings.fancyGraphics ? "" : " fast", this.gameSettings.clouds == 0 ? "" : (this.gameSettings.clouds == 1 ? " fast-clouds" : " fancy-clouds"), OpenGlHelper.useVbo() ? " vbo" : "");
			RenderChunk.renderChunksUpdated = 0;
			this.debugUpdateTime += 1000L;
			this.fpsCounter = 0;
			this.usageSnooper.addMemoryStatsToSnooper();

			if (!this.usageSnooper.isSnooperRunning()) {
				this.usageSnooper.startSnooper();
			}
		}

		if (this.isFramerateLimitBelowMax()) {
			this.mcProfiler.startSection("fpslimit_wait");
			Display.sync(this.getLimitFramerate());
			this.mcProfiler.endSection();
		}

		this.mcProfiler.endSection();
	}

	public void updateDisplay() {
		this.mcProfiler.startSection("display_update");
		Display.update();
		this.mcProfiler.endSection();
		this.checkWindowResize();
	}

	protected void checkWindowResize() {
		if (!this.fullscreen && Display.wasResized()) {
			int i = this.displayWidth;
			int j = this.displayHeight;
			this.displayWidth = Display.getWidth();
			this.displayHeight = Display.getHeight();

			if (this.displayWidth != i || this.displayHeight != j) {
				if (this.displayWidth <= 0) {
					this.displayWidth = 1;
				}

				if (this.displayHeight <= 0) {
					this.displayHeight = 1;
				}

				this.resize(this.displayWidth, this.displayHeight);
			}
		}
	}

	//TODO FPS CAP
	public int getLimitFramerate() {
		return this.world == null && this.currentScreen != null ? 120 : this.gameSettings.limitFramerate;
	}

	public boolean isFramerateLimitBelowMax() {
		return (float) this.getLimitFramerate() < GameSettings.Options.FRAMERATE_LIMIT.getValueMax();
	}

	public void freeMemory() {
		try {
			memoryReserve = new byte[0];
			this.renderGlobal.deleteAllDisplayLists();
		} catch (Throwable ignored) {
		}

		try {
			System.gc();
			this.loadWorld(null);
		} catch (Throwable ignored) {
		}

		System.gc();
	}

	private void updateDebugProfilerName(int keyCount) {
		List<Profiler.Result> list = this.mcProfiler.getProfilingData(this.debugProfilerName);

		if (list != null && !list.isEmpty()) {
			Profiler.Result profiler$result = list.remove(0);

			if (keyCount == 0) {
				if (profiler$result.field_76331_c.length() > 0) {
					int i = this.debugProfilerName.lastIndexOf(".");

					if (i >= 0) {
						this.debugProfilerName = this.debugProfilerName.substring(0, i);
					}
				}
			} else {
				--keyCount;

				if (keyCount < list.size() && !list.get(keyCount).field_76331_c.equals("unspecified")) {
					if (this.debugProfilerName.length() > 0) {
						this.debugProfilerName = this.debugProfilerName + ".";
					}

					this.debugProfilerName = this.debugProfilerName + list.get(keyCount).field_76331_c;
				}
			}
		}
	}

	private void displayDebugInfo() {
		if (this.mcProfiler.profilingEnabled) {
			List<Profiler.Result> list = this.mcProfiler.getProfilingData(this.debugProfilerName);
			Profiler.Result profiler$result = list.remove(0);
			GlStateManager.clear(256);
			GlStateManager.matrixMode(5889);
			GlStateManager.enableColorMaterial();
			GlStateManager.loadIdentity();
			GlStateManager.ortho(0.0D, this.displayWidth, this.displayHeight, 0.0D, 1000.0D, 3000.0D);
			GlStateManager.matrixMode(5888);
			GlStateManager.loadIdentity();
			GlStateManager.translate(0.0F, 0.0F, -2000.0F);
			GL11.glLineWidth(1.0F);
			GlStateManager.disableTexture2D();
			Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer worldrenderer = tessellator.getWorldRenderer();
			int i = 160;
			int j = this.displayWidth - i - 10;
			int k = this.displayHeight - i * 2;
			GlStateManager.enableBlend();
			worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
			worldrenderer.pos((float) j - (float) i * 1.1F, (float) k - (float) i * 0.6F - 16.0F, 0.0D).color(200, 0, 0, 0).endVertex();
			worldrenderer.pos((float) j - (float) i * 1.1F, k + i * 2, 0.0D).color(200, 0, 0, 0).endVertex();
			worldrenderer.pos((float) j + (float) i * 1.1F, k + i * 2, 0.0D).color(200, 0, 0, 0).endVertex();
			worldrenderer.pos((float) j + (float) i * 1.1F, (float) k - (float) i * 0.6F - 16.0F, 0.0D).color(200, 0, 0, 0).endVertex();
			tessellator.draw();
			GlStateManager.disableBlend();
			double d0 = 0.0D;

			for (Profiler.Result profiler$result1 : list) {
				int i1 = MathHelper.floor_double(profiler$result1.field_76332_a / 4.0D) + 1;
				worldrenderer.begin(6, DefaultVertexFormats.POSITION_COLOR);
				int j1 = profiler$result1.func_76329_a();
				int k1 = j1 >> 16 & 255;
				int l1 = j1 >> 8 & 255;
				int i2 = j1 & 255;
				worldrenderer.pos(j, k, 0.0D).color(k1, l1, i2, 255).endVertex();

				for (int j2 = i1; j2 >= 0; --j2) {
					float f = (float) ((d0 + profiler$result1.field_76332_a * (double) j2 / (double) i1) * Math.PI * 2.0D / 100.0D);
					float f1 = MathHelper.sin(f) * (float) i;
					float f2 = MathHelper.cos(f) * (float) i * 0.5F;
					worldrenderer.pos((float) j + f1, (float) k - f2, 0.0D).color(k1, l1, i2, 255).endVertex();
				}

				tessellator.draw();
				worldrenderer.begin(5, DefaultVertexFormats.POSITION_COLOR);

				for (int i3 = i1; i3 >= 0; --i3) {
					float f3 = (float) ((d0 + profiler$result1.field_76332_a * (double) i3 / (double) i1) * Math.PI * 2.0D / 100.0D);
					float f4 = MathHelper.sin(f3) * (float) i;
					float f5 = MathHelper.cos(f3) * (float) i * 0.5F;
					worldrenderer.pos((float) j + f4, (float) k - f5, 0.0D).color(k1 >> 1, l1 >> 1, i2 >> 1, 255).endVertex();
					worldrenderer.pos((float) j + f4, (float) k - f5 + 10.0F, 0.0D).color(k1 >> 1, l1 >> 1, i2 >> 1, 255).endVertex();
				}

				tessellator.draw();
				d0 += profiler$result1.field_76332_a;
			}

			DecimalFormat decimalformat = new DecimalFormat("##0.00");
			GlStateManager.enableTexture2D();
			String s = "";

			if (!profiler$result.field_76331_c.equals("unspecified")) {
				s = s + "[0] ";
			}

			if (profiler$result.field_76331_c.length() == 0) {
				s = s + "ROOT ";
			} else {
				s = s + profiler$result.field_76331_c + " ";
			}

			int l2 = 16777215;
			this.fontRenderer.drawStringWithShadow(s, (float) (j - i), (float) (k - i / 2 - 16), l2);
			this.fontRenderer.drawStringWithShadow(s = decimalformat.format(profiler$result.field_76330_b) + "%", (float) (j + i - this.fontRenderer.getStringWidth(s)), (float) (k - i / 2 - 16), l2);

			for (int k2 = 0; k2 < list.size(); ++k2) {
				Profiler.Result profiler$result2 = list.get(k2);
				String s1 = "";

				if (profiler$result2.field_76331_c.equals("unspecified")) {
					s1 = s1 + "[?] ";
				} else {
					s1 = s1 + "[" + (k2 + 1) + "] ";
				}

				s1 = s1 + profiler$result2.field_76331_c;
				this.fontRenderer.drawStringWithShadow(s1, (float) (j - i), (float) (k + i / 2 + k2 * 8 + 20), profiler$result2.func_76329_a());
				this.fontRenderer.drawStringWithShadow(s1 = decimalformat.format(profiler$result2.field_76332_a) + "%", (float) (j + i - 50 - this.fontRenderer.getStringWidth(s1)), (float) (k + i / 2 + k2 * 8 + 20), profiler$result2.func_76329_a());
				this.fontRenderer.drawStringWithShadow(s1 = decimalformat.format(profiler$result2.field_76330_b) + "%", (float) (j + i - this.fontRenderer.getStringWidth(s1)), (float) (k + i / 2 + k2 * 8 + 20), profiler$result2.func_76329_a());
			}
		}
	}

	public void shutdown() {
		this.running = false;
	}

	public void setIngameFocus() {
		if (Display.isActive()) {
			if (!this.inGameHasFocus) {
				this.inGameHasFocus = true;
				this.mouseHelper.grabMouseCursor();
				this.displayGuiScreen(null);
				this.leftClickCounter = 10000;
			}
		}
	}

	public void setIngameNotInFocus() {
		if (this.inGameHasFocus) {
			KeyBinding.unPressAllKeys();
			this.inGameHasFocus = false;
			this.mouseHelper.ungrabMouseCursor();
		}
	}

	public void displayInGameMenu() {
		if (this.currentScreen == null) {
			this.displayGuiScreen(new InGameMenuScreen());

			if (this.isSingleplayer() && !this.theIntegratedServer.getPublic()) {
				this.soundHandler.pauseSounds();
			}
		}
	}

	private void sendClickBlockToController(boolean leftClick) {
		if (!leftClick) {
			this.leftClickCounter = 0;
		}

		if (this.leftClickCounter <= 0 && !this.player.isUsingItem()) {
			if (leftClick && this.objectMouseOver != null && this.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
				BlockPos blockpos = this.objectMouseOver.getBlockPos();

				if (this.world.getBlockState(blockpos).getBlock().getMaterial() != Material.air && this.playerController.onPlayerDamageBlock(blockpos, this.objectMouseOver.sideHit)) {
					this.effectRenderer.addBlockHitEffects(blockpos, this.objectMouseOver.sideHit);
					this.player.swingItem();
				}
			} else {
				this.playerController.resetBlockRemoving();
			}
		}
	}

	private void clickMouse() {
		if (this.leftClickCounter <= 0) {
			this.player.swingItem();

			if (this.objectMouseOver == null) {
				logger.error("Null returned as 'hitResult', this shouldn't happen!");

				if (this.playerController.isNotCreative()) {
					this.leftClickCounter = 10;
				}
			} else {
				switch (this.objectMouseOver.typeOfHit) {
					case ENTITY:
						this.playerController.attackEntity(this.player, this.objectMouseOver.entityHit);
						break;

					case BLOCK:
						BlockPos blockpos = this.objectMouseOver.getBlockPos();

						if (this.world.getBlockState(blockpos).getBlock().getMaterial() != Material.air) {
							this.playerController.clickBlock(blockpos, this.objectMouseOver.sideHit);
							break;
						}

					case MISS:
					default:
						if (this.playerController.isNotCreative()) {
							this.leftClickCounter = 10;
						}
				}
			}
		}
	}

	private void rightClickMouse() {
		if (!this.playerController.func_181040_m()) {
			this.rightClickDelayTimer = 4;
			boolean flag = true;
			ItemStack itemstack = this.player.inventory.getCurrentItem();

			if (this.objectMouseOver == null) {
				logger.warn("Null returned as 'hitResult', this shouldn't happen!");
			} else {
				switch (this.objectMouseOver.typeOfHit) {
					case ENTITY:
						if (this.playerController.func_178894_a(this.player, this.objectMouseOver.entityHit, this.objectMouseOver)) {
							flag = false;
						} else if (this.playerController.interactWithEntitySendPacket(this.player, this.objectMouseOver.entityHit)) {
							flag = false;
						}

						break;
					case BLOCK:
						BlockPos blockpos = this.objectMouseOver.getBlockPos();

						if (this.world.getBlockState(blockpos).getBlock().getMaterial() != Material.air) {
							int i = itemstack != null ? itemstack.stackSize : 0;

							if (this.playerController.onPlayerRightClick(this.player, this.world, itemstack, blockpos, this.objectMouseOver.sideHit, this.objectMouseOver.hitVec)) {
								flag = false;
								this.player.swingItem();
							}

							if (itemstack == null) {
								return;
							}

							if (itemstack.stackSize == 0) {
								this.player.inventory.mainInventory[this.player.inventory.currentItem] = null;
							} else if (itemstack.stackSize != i || this.playerController.isInCreativeMode()) {
								this.entityRenderer.itemRenderer.resetEquippedProgress();
							}
						}
				}
			}

			if (flag) {
				ItemStack itemstack1 = this.player.inventory.getCurrentItem();

				if (itemstack1 != null && this.playerController.sendUseItem(this.player, this.world, itemstack1)) {
					this.entityRenderer.itemRenderer.resetEquippedProgress();
				}
			}
		}
	}

	public void toggleFullscreen() {
		try {
			this.fullscreen = !this.fullscreen;
			this.gameSettings.fullScreen = this.fullscreen;

			if (this.fullscreen) {
				this.updateDisplayMode();
				this.displayWidth = Display.getDisplayMode().getWidth();
				this.displayHeight = Display.getDisplayMode().getHeight();
			} else {
				Display.setDisplayMode(new DisplayMode(this.tempDisplayWidth, this.tempDisplayHeight));
				this.displayWidth = this.tempDisplayWidth;
				this.displayHeight = this.tempDisplayHeight;
			}

			if (this.displayWidth <= 0) {
				this.displayWidth = 1;
			}

			if (this.displayHeight <= 0) {
				this.displayHeight = 1;
			}

			if (this.currentScreen != null) {
				this.resize(this.displayWidth, this.displayHeight);
			} else {
				this.updateFramebufferSize();
			}

			Display.setFullscreen(this.fullscreen);
			Display.setVSyncEnabled(this.gameSettings.enableVsync);
			this.updateDisplay();
		} catch (Exception exception) {
			logger.error("Couldn't toggle fullscreen", exception);
		}
	}

	private void resize(int width, int height) {
		this.displayWidth = Math.max(1, width);
		this.displayHeight = Math.max(1, height);

		if (this.currentScreen != null) {
			ScaledResolution scaledresolution = new ScaledResolution();
			this.currentScreen.onResize(scaledresolution.getScaledWidth(), scaledresolution.getScaledHeight());
		}

		this.loadingScreen = new LoadingScreenRenderer(this);
		this.updateFramebufferSize();
	}

	private void updateFramebufferSize() {
		this.framebufferMc.createBindFramebuffer(this.displayWidth, this.displayHeight);

		if (this.entityRenderer != null) {
			this.entityRenderer.updateShaderGroupSize(this.displayWidth, this.displayHeight);
		}
	}

	public MusicTicker getMusicTicker() {
		return this.musicTicker;
	}

	public void runTick() throws IOException {
		//TODO TickEvent
		TickEvent tickEvent = new TickEvent();
		EventManager.call(tickEvent);

		if (this.rightClickDelayTimer > 0) {
			--this.rightClickDelayTimer;
		}

		this.mcProfiler.startSection("gui");

		if (!this.isGamePaused) {
			this.inGameScreen.updateTick();
		}

		this.mcProfiler.endSection();
		this.entityRenderer.getMouseOver(1.0F);
		this.mcProfiler.startSection("gameMode");

		if (!this.isGamePaused && this.world != null) {
			this.playerController.updateController();
		}

		this.mcProfiler.endStartSection("textures");

		if (!this.isGamePaused) {
			this.renderEngine.tick();
		}

		if (this.currentScreen == null && this.player != null) {
			if (this.player.getHealth() <= 0.0F) {
				this.displayGuiScreen(null);
			} else if (this.player.isPlayerSleeping() && this.world != null) {
				this.displayGuiScreen(new SleepMPScreen());
			}
		} else if (this.currentScreen != null && this.currentScreen instanceof SleepMPScreen && !this.player.isPlayerSleeping()) {
			this.displayGuiScreen(null);
		}

		if (this.currentScreen != null) {
			this.leftClickCounter = 10000;
		}

		if (this.currentScreen != null) {
			try {
				this.currentScreen.handleInput();
			} catch (Throwable throwable) {
				CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Updating screen events");
				CrashReportCategory category = crashReport.makeCategory("Affected screen");
				category.addCrashSectionCallable("Screen name", () -> Minecraft.this.currentScreen.getClass().getCanonicalName());
				throw new ReportedException(crashReport);
			}

			if (this.currentScreen != null) {
				try {
					this.currentScreen.updateScreen();
				} catch (Throwable throwable) {
					CrashReport crashReport = CrashReport.makeCrashReport(throwable, "Ticking screen");
					CrashReportCategory category = crashReport.makeCategory("Affected screen");
					category.addCrashSectionCallable("Screen name", () -> Minecraft.this.currentScreen.getClass().getCanonicalName());
					throw new ReportedException(crashReport);
				}
			}
		}

		if (this.currentScreen == null || this.currentScreen.allowUserInput) {
			this.mcProfiler.endStartSection("mouse");
			while (Mouse.next()) {
				int i = Mouse.getEventButton();
				KeyBinding.setKeyBindState(i - 100, Mouse.getEventButtonState());

				if (Mouse.getEventButtonState()) {
					if (this.player.isSpectator() && i == 2) {
						this.inGameScreen.getSpectatorGui().func_175261_b();
					} else {
						KeyBinding.onTick(i - 100);
					}
				}

				long i1 = getSystemTime() - this.systemTime;

				if (i1 <= 200L) {
					int j = Mouse.getEventDWheel();

					if (j != 0) {
						if (this.player.isSpectator()) {
							j = j < 0 ? -1 : 1;

							if (this.inGameScreen.getSpectatorGui().func_175262_a()) {
								this.inGameScreen.getSpectatorGui().func_175259_b(-j);
							} else {
								float f = MathHelper.clamp_float(this.player.capabilities.getFlySpeed() + (float) j * 0.005F, 0.0F, 0.2F);
								this.player.capabilities.setFlySpeed(f);
							}
						} else {
							this.player.inventory.changeCurrentItem(j);
						}
					}

					if (this.currentScreen == null) {
						if (!this.inGameHasFocus && Mouse.getEventButtonState()) {
							this.setIngameFocus();
						} else if (this.inGameHasFocus) {

							//TODO Mouse Event
							if (Mouse.getEventButtonState()) {
								EventManager.call(new MousePressEvent(Mouse.getEventButton()));
							} else {
								EventManager.call(new MouseReleaseEvent(Mouse.getEventButton()));
							}
						}
					} else {
						this.currentScreen.handleMouseInput();
					}
				}
			}

			if (this.leftClickCounter > 0) {
				--this.leftClickCounter;
			}

			this.mcProfiler.endStartSection("keyboard");

			while (Keyboard.next()) {
				int key = Keyboard.getEventKey() == 0 ? Keyboard.getEventCharacter() + 256 : Keyboard.getEventKey();
				KeyBinding.setKeyBindState(key, Keyboard.getEventKeyState());

				if (Keyboard.getEventKeyState()) {
					KeyBinding.onTick(key);
				}

				if (this.debugCrashKeyPressTime > 0L) {
					if (getSystemTime() - this.debugCrashKeyPressTime >= 6000L) {
						throw new ReportedException(new CrashReport("Manually triggered debug crash", new Throwable()));
					}

					if (!Keyboard.isKeyDown(Keyboard.KEY_C) || !Keyboard.isKeyDown(Keyboard.KEY_F3)) {
						this.debugCrashKeyPressTime = -1L;
					}
				} else if (Keyboard.isKeyDown(Keyboard.KEY_C) && Keyboard.isKeyDown(Keyboard.KEY_F3)) {
					this.debugCrashKeyPressTime = getSystemTime();
				}

				this.dispatchKeypresses();

				if (this.currentScreen == null) {
					KeyboardEvent keyboardEvent = new KeyboardEvent(key);
					if (Keyboard.getEventKeyState()) {
						EventManager.call(keyboardEvent);
						EventManager.call(new KeyboardPressEvent(key));
					} else {
						EventManager.call(keyboardEvent);
						EventManager.call(new KeyboardReleaseEvent(key));
					}
				}

				if (Keyboard.getEventKeyState()) {
					if (key == Keyboard.KEY_F4 && this.entityRenderer != null) {
						this.entityRenderer.switchUseShader();
					}

					if (this.currentScreen != null) {
						this.currentScreen.handleKeyboardInput();
					} else {
						if (key == Keyboard.KEY_ESCAPE) {
							this.displayInGameMenu();
						}

						if (key == Keyboard.KEY_D && Keyboard.isKeyDown(Keyboard.KEY_F3) && this.inGameScreen != null) {
							this.inGameScreen.getChatGUI().clearChatMessages();
						}

						if (key == Keyboard.KEY_S && Keyboard.isKeyDown(Keyboard.KEY_F3)) {
							this.refreshResources();
						}

						if (key == Keyboard.KEY_T && Keyboard.isKeyDown(Keyboard.KEY_F3)) {
							this.refreshResources();
						}

						if (key == Keyboard.KEY_F && Keyboard.isKeyDown(Keyboard.KEY_F3)) {
							this.gameSettings.setOptionValue(GameSettings.Options.RENDER_DISTANCE, Screen.isShiftKeyDown() ? -1 : 1);
						}

						if (key == Keyboard.KEY_A && Keyboard.isKeyDown(Keyboard.KEY_F3)) {
							this.renderGlobal.loadRenderers();
						}

						if (key == Keyboard.KEY_H && Keyboard.isKeyDown(Keyboard.KEY_F3)) {
							this.gameSettings.advancedItemTooltips = !this.gameSettings.advancedItemTooltips;
							this.gameSettings.saveOptions();
						}

						if (key == Keyboard.KEY_B && Keyboard.isKeyDown(Keyboard.KEY_F3)) {
							this.renderManager.setDebugBoundingBox(!this.renderManager.isDebugBoundingBox());
						}

						if (key == Keyboard.KEY_P && Keyboard.isKeyDown(Keyboard.KEY_F3)) {
							this.gameSettings.pauseOnLostFocus = !this.gameSettings.pauseOnLostFocus;
							this.gameSettings.saveOptions();
						}

						if (key == Keyboard.KEY_F1) {
							this.gameSettings.hideGUI = !this.gameSettings.hideGUI;
						}

						if (key == Keyboard.KEY_F3) {
							this.gameSettings.showDebugInfo = !this.gameSettings.showDebugInfo;
							this.gameSettings.showDebugProfilerChart = Screen.isShiftKeyDown();
							this.gameSettings.field_181657_aC = Screen.isAltKeyDown();
						}

						if (this.gameSettings.keyBindTogglePerspective.isPressed()) {
							++this.gameSettings.thirdPersonView;

							if (this.gameSettings.thirdPersonView > 2) {
								this.gameSettings.thirdPersonView = 0;
							}

							if (this.gameSettings.thirdPersonView == 0) {
								this.entityRenderer.loadEntityShader(this.getRenderViewEntity());
							} else if (this.gameSettings.thirdPersonView == 1) {
								this.entityRenderer.loadEntityShader(null);
							}

							this.renderGlobal.setDisplayListEntitiesDirty();
						}

						if (this.gameSettings.keyBindSmoothCamera.isPressed()) {
							this.gameSettings.smoothCamera = !this.gameSettings.smoothCamera;
						}
					}

					if (this.gameSettings.showDebugInfo && this.gameSettings.showDebugProfilerChart) {
						if (key == Keyboard.KEY_0) {
							this.updateDebugProfilerName(0);
						}

						for (int j1 = 0; j1 < 9; ++j1) {
							if (key == 2 + j1) {
								this.updateDebugProfilerName(j1 + 1);
							}
						}
					}
				}
			}

			for (int l = 0; l < 9; ++l) {
				if (this.gameSettings.keyBindsHotbar[l].isPressed()) {
					if (this.player.isSpectator()) {
						this.inGameScreen.getSpectatorGui().func_175260_a(l);
					} else {
						this.player.inventory.currentItem = l;
					}
				}
			}

			boolean flag = this.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN;

			while (this.gameSettings.keyBindInventory.isPressed()) {
				if (this.playerController.isRidingHorse()) {
					this.player.sendHorseInventory();
				} else {
					this.getNetHandler().addToSendQueue(new C16PacketClientStatus(C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT));
					this.displayGuiScreen(new GuiInventory(this.player));
				}
			}

			while (this.gameSettings.keyBindDrop.isPressed()) {
				if (!this.player.isSpectator()) {
					this.player.dropOneItem(Screen.isCtrlKeyDown());
				}
			}

			while (this.gameSettings.keyBindChat.isPressed() && flag) {
				this.displayGuiScreen(new ChatScreen(""));
			}

			if (this.currentScreen == null && this.gameSettings.keyBindCommand.isPressed() && flag) {
				this.displayGuiScreen(new ChatScreen("/"));
			}

			if (this.player.isUsingItem()) {
				if (!this.gameSettings.keyBindUseItem.isKeyDown()) {
					this.playerController.onStoppedUsingItem(this.player);
				}
			} else {
				while (this.gameSettings.keyBindAttack.isPressed()) {
					this.clickMouse();
				}

				while (this.gameSettings.keyBindUseItem.isPressed()) {
					this.rightClickMouse();
				}

				while (this.gameSettings.keyBindPickBlock.isPressed()) {
					this.middleClickMouse();
				}
			}

			if (this.gameSettings.keyBindUseItem.isKeyDown() && this.rightClickDelayTimer == 0 && !this.player.isUsingItem()) {
				this.rightClickMouse();
			}

			this.sendClickBlockToController(this.currentScreen == null && this.gameSettings.keyBindAttack.isKeyDown() && this.inGameHasFocus);
		}

		if (this.world != null) {
			if (this.player != null) {
				++this.joinPlayerCounter;

				if (this.joinPlayerCounter == 30) {
					this.joinPlayerCounter = 0;
					this.world.joinEntityInSurroundings(this.player);
				}
			}

			this.mcProfiler.endStartSection("gameRenderer");

			if (!this.isGamePaused) {
				this.entityRenderer.updateRenderer();
			}

			this.mcProfiler.endStartSection("levelRenderer");

			if (!this.isGamePaused) {
				this.renderGlobal.updateClouds();
			}

			this.mcProfiler.endStartSection("level");

			if (!this.isGamePaused) {
				if (this.world.getLastLightningBolt() > 0) {
					this.world.setLastLightningBolt(this.world.getLastLightningBolt() - 1);
				}

				this.world.updateEntities();
			}
		} else if (this.entityRenderer.isShaderActive()) {
			this.entityRenderer.func_181022_b();
		}

		if (!this.isGamePaused) {
			this.musicTicker.update();
			this.soundHandler.update();
		}

		if (this.world != null) {
			if (!this.isGamePaused) {
				this.world.setAllowedSpawnTypes(this.world.getDifficulty() != EnumDifficulty.PEACEFUL, true);

				try {
					this.world.tick();
				} catch (Throwable throwable) {
					CrashReport report = CrashReport.makeCrashReport(throwable, "Exception in world tick");

					if (this.world == null) {
						CrashReportCategory category = report.makeCategory("Affected level");
						category.addCrashSection("Problem", "Level is null!");
					} else {
						this.world.addWorldInfoToCrashReport(report);
					}

					throw new ReportedException(report);
				}
			}

			this.mcProfiler.endStartSection("animateTick");

			if (!this.isGamePaused && this.world != null) {
				this.world.doVoidFogParticles(MathHelper.floor_double(this.player.posX), MathHelper.floor_double(this.player.posY), MathHelper.floor_double(this.player.posZ));
			}

			this.mcProfiler.endStartSection("particles");

			if (!this.isGamePaused) {
				this.effectRenderer.updateEffects();
			}
		} else if (this.myNetworkManager != null) {
			this.mcProfiler.endStartSection("pendingConnection");
			this.myNetworkManager.processReceivedPackets();
		}

		this.mcProfiler.endSection();
		this.systemTime = getSystemTime();
	}

	public void launchIntegratedServer(String folderName, String worldName, WorldSettings worldSettings) {
		this.loadWorld(null);
		System.gc();
		ISaveHandler isavehandler = this.saveLoader.getSaveLoader(folderName, false);
		WorldInfo worldinfo = isavehandler.loadWorldInfo();

		if (worldinfo == null && worldSettings != null) {
			worldinfo = new WorldInfo(worldSettings, folderName);
			isavehandler.saveWorldInfo(worldinfo);
		}

		if (worldSettings == null && worldinfo != null) {
			worldSettings = new WorldSettings(worldinfo);
		}

		try {
            assert worldSettings != null;
            this.theIntegratedServer = new IntegratedServer(this, folderName, worldName, worldSettings);
			this.theIntegratedServer.startServerThread();
			this.integratedServerIsRunning = true;
		} catch (Throwable throwable) {
			CrashReport report = CrashReport.makeCrashReport(throwable, "Starting integrated server");
			CrashReportCategory category = report.makeCategory("Starting integrated server");
			category.addCrashSection("Level ID", folderName);
			category.addCrashSection("Level Name", worldName);
			throw new ReportedException(report);
		}

		this.loadingScreen.displaySavingString(I18n.format("menu.loadingLevel"));

		while (!this.theIntegratedServer.serverIsInRunLoop()) {
			String s = this.theIntegratedServer.getUserMessage();

			if (s != null) {
				this.loadingScreen.displayLoadingString(I18n.format(s));
			} else {
				this.loadingScreen.displayLoadingString("");
			}

			try {
				Thread.sleep(200L);
			} catch (InterruptedException ignored) {
			}
		}

		this.displayGuiScreen(null);
		SocketAddress socketaddress = this.theIntegratedServer.getNetworkSystem().addLocalEndpoint();
		NetworkManager networkmanager = NetworkManager.provideLocalClient(socketaddress);
		networkmanager.setNetHandler(new NetHandlerLoginClient(networkmanager, this, null));
		networkmanager.sendPacket(new C00Handshake(47, socketaddress.toString(), 0, EnumConnectionState.LOGIN));
		networkmanager.sendPacket(new C00PacketLoginStart(this.getSession().getProfile()));
		this.myNetworkManager = networkmanager;
	}

	public void loadWorld(WorldClient world) {
		this.loadWorld(world, "");
	}

	public void loadWorld(WorldClient world, String loadingMessage) {
		System.gc();
		if (world == null) {
			NetHandlerPlayClient netHandler = this.getNetHandler();

			if (netHandler != null) {
				netHandler.cleanup();
			}

			if (this.theIntegratedServer != null && this.theIntegratedServer.isAnvilFileSet()) {
				this.theIntegratedServer.initiateShutdown();
				this.theIntegratedServer.setStaticInstance();
			}

			this.theIntegratedServer = null;
			this.guiAchievement.clearAchievements();
			this.entityRenderer.getMapItemRenderer().clearLoadedMaps();
		}

		this.renderViewEntity = null;
		this.myNetworkManager = null;

		if (this.loadingScreen != null) {
			this.loadingScreen.resetProgressAndMessage(loadingMessage);
			this.loadingScreen.displayLoadingString("");
		}

		if (world == null && this.world != null) {
			this.resourcePackRepository.func_148529_f();
			this.inGameScreen.playerListResetHeaderFooter();
			this.setServerData(null);
			this.integratedServerIsRunning = false;
		}

		this.soundHandler.stopSounds();
		this.world = world;

		if (world != null) {
			if (this.renderGlobal != null) {
				this.renderGlobal.setWorldAndLoadRenderers(world);
			}

			if (this.effectRenderer != null) {
				this.effectRenderer.clearEffects(world);
			}

			if (this.player == null) {
				this.player = this.playerController.func_178892_a(world, new StatFileWriter());
				this.playerController.flipPlayer(this.player);
			}

			this.player.preparePlayerToSpawn();
			world.spawnEntityInWorld(this.player);
			this.player.movementInput = new MovementInputFromOptions(this.gameSettings);
			this.playerController.setPlayerCapabilities(this.player);
			this.renderViewEntity = this.player;
		} else {
			this.saveLoader.flushCache();
			this.player = null;
		}

		System.gc();
		this.systemTime = 0L;
	}

	public void setDimensionAndSpawnPlayer(int dimension) {
		this.world.setInitialSpawnLocation();
		this.world.removeAllEntities();
		int i = 0;
		String s = null;

		if (this.player != null) {
			i = this.player.getEntityId();
			this.world.removeEntity(this.player);
			s = this.player.getClientBrand();
		}

		this.renderViewEntity = null;
		EntityPlayerSP entityplayersp = this.player;
		this.player = this.playerController.func_178892_a(this.world, this.player == null ? new StatFileWriter() : this.player.getStatFileWriter());
        assert entityplayersp != null;
        this.player.getDataWatcher().updateWatchedObjectsFromList(entityplayersp.getDataWatcher().getAllWatched());
		this.player.dimension = dimension;
		this.renderViewEntity = this.player;
		this.player.preparePlayerToSpawn();
		this.player.setClientBrand(s);
		this.world.spawnEntityInWorld(this.player);
		this.playerController.flipPlayer(this.player);
		this.player.movementInput = new MovementInputFromOptions(this.gameSettings);
		this.player.setEntityId(i);
		this.playerController.setPlayerCapabilities(this.player);
		this.player.setReducedDebug(entityplayersp.hasReducedDebug());

		if (this.currentScreen instanceof GameOverScreen) {
			this.displayGuiScreen(null);
		}
	}

	public NetHandlerPlayClient getNetHandler() {
		return this.player != null ? this.player.sendQueue : null;
	}

	public static boolean isGuiEnabled() {
		return theMinecraft == null || !theMinecraft.gameSettings.hideGUI;
	}

	public static boolean isAmbientOcclusionEnabled() {
		return theMinecraft != null && theMinecraft.gameSettings.ambientOcclusion != 0;
	}

	private void middleClickMouse() {
		if (this.objectMouseOver != null) {
			boolean flag = this.player.capabilities.isCreativeMode;
			int i = 0;
			boolean flag1 = false;
			TileEntity tileentity = null;
			Item item;

			if (this.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
				BlockPos blockpos = this.objectMouseOver.getBlockPos();
				Block block = this.world.getBlockState(blockpos).getBlock();

				if (block.getMaterial() == Material.air) {
					return;
				}

				item = block.getItem(this.world, blockpos);

				if (item == null) {
					return;
				}

				if (flag && Screen.isCtrlKeyDown()) {
					tileentity = this.world.getTileEntity(blockpos);
				}

				Block block1 = item instanceof ItemBlock && !block.isFlowerPot() ? Block.getBlockFromItem(item) : block;
				i = block1.getDamageValue(this.world, blockpos);
				flag1 = item.getHasSubtypes();
			} else {
				if (this.objectMouseOver.typeOfHit != MovingObjectPosition.MovingObjectType.ENTITY || this.objectMouseOver.entityHit == null || !flag) {
					return;
				}

				if (this.objectMouseOver.entityHit instanceof EntityPainting) {
					item = Items.painting;
				} else if (this.objectMouseOver.entityHit instanceof EntityLeashKnot) {
					item = Items.lead;
				} else if (this.objectMouseOver.entityHit instanceof EntityItemFrame) {
					EntityItemFrame entityitemframe = (EntityItemFrame) this.objectMouseOver.entityHit;
					ItemStack itemstack = entityitemframe.getDisplayedItem();

					if (itemstack == null) {
						item = Items.item_frame;
					} else {
						item = itemstack.getItem();
						i = itemstack.getMetadata();
						flag1 = true;
					}
				} else if (this.objectMouseOver.entityHit instanceof EntityMinecart) {
					EntityMinecart entityminecart = (EntityMinecart) this.objectMouseOver.entityHit;

					switch (entityminecart.getMinecartType()) {
						case FURNACE:
							item = Items.furnace_minecart;
							break;
						case CHEST:
							item = Items.chest_minecart;
							break;
						case TNT:
							item = Items.tnt_minecart;
							break;
						case HOPPER:
							item = Items.hopper_minecart;
							break;
						case COMMAND_BLOCK:
							item = Items.command_block_minecart;
							break;
						default:
							item = Items.minecart;
					}
				} else if (this.objectMouseOver.entityHit instanceof EntityBoat) {
					item = Items.boat;
				} else if (this.objectMouseOver.entityHit instanceof EntityArmorStand) {
					item = Items.armor_stand;
				} else {
					item = Items.spawn_egg;
					i = EntityList.getEntityID(this.objectMouseOver.entityHit);
					flag1 = true;

					if (!EntityList.entityEggs.containsKey(i)) {
						return;
					}
				}
			}

			InventoryPlayer inventoryplayer = this.player.inventory;

			if (tileentity == null) {
				inventoryplayer.setCurrentItem(item, i, flag1, flag);
			} else {
				ItemStack itemStack = this.func_181036_a(item, i, tileentity);
				inventoryplayer.setInventorySlotContents(inventoryplayer.currentItem, itemStack);
			}

			if (flag) {
				int j = this.player.inventoryContainer.inventorySlots.size() - 9 + inventoryplayer.currentItem;
				this.playerController.sendSlotPacket(inventoryplayer.getStackInSlot(inventoryplayer.currentItem), j);
			}
		}
	}

	private ItemStack func_181036_a(Item p_181036_1_, int p_181036_2_, TileEntity p_181036_3_) {
		ItemStack itemstack = new ItemStack(p_181036_1_, 1, p_181036_2_);
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		p_181036_3_.writeToNBT(nbttagcompound);

		if (p_181036_1_ == Items.skull && nbttagcompound.hasKey("Owner")) {
			NBTTagCompound nbttagcompound2 = nbttagcompound.getCompoundTag("Owner");
			NBTTagCompound nbttagcompound3 = new NBTTagCompound();
			nbttagcompound3.setTag("SkullOwner", nbttagcompound2);
			itemstack.setTagCompound(nbttagcompound3);
			return itemstack;
		} else {
			itemstack.setTagInfo("BlockEntityTag", nbttagcompound);
			NBTTagCompound nbttagcompound1 = new NBTTagCompound();
			NBTTagList nbttaglist = new NBTTagList();
			nbttaglist.appendTag(new NBTTagString("(+NBT)"));
			nbttagcompound1.setTag("Lore", nbttaglist);
			itemstack.setTagInfo("display", nbttagcompound1);
			return itemstack;
		}
	}

	public CrashReport addGraphicsAndWorldToCrashReport(CrashReport report) {
		report.getCategory().addCrashSectionCallable("Launched Version", () -> Minecraft.this.launchedVersion);
		report.getCategory().addCrashSectionCallable("LWJGL", Sys::getVersion);
		report.getCategory().addCrashSectionCallable("OpenGL", () -> GL11.glGetString(GL11.GL_RENDERER) + " GL version " + GL11.glGetString(GL11.GL_VERSION) + ", " + GL11.glGetString(GL11.GL_VENDOR));
		report.getCategory().addCrashSectionCallable("GL Caps", OpenGlHelper::getLogText);
		report.getCategory().addCrashSectionCallable("Using VBOs", () -> this.gameSettings.useVbo ? "Yes" : "No");
		report.getCategory().addCrashSectionCallable("Is Modded", () -> {
			String s = com.murengezi.minecraft.client.ClientBrandRetriever.getClientModName();
			return !s.equals("vanilla") ? "Definitely; Client brand changed to '" + s + "'" : (Minecraft.class.getSigners() == null ? "Very likely; Jar signature invalidated" : "Probably not. Jar signature remains and client brand is untouched.");
		});
		report.getCategory().addCrashSectionCallable("Type", () -> "Client (map_client.txt)");
		report.getCategory().addCrashSectionCallable("Resource Packs", () -> {
			StringBuilder stringbuilder = new StringBuilder();

			for (String s : this.gameSettings.resourcePacks) {
				if (stringbuilder.length() > 0) {
					stringbuilder.append(", ");
				}

				stringbuilder.append(s);

				if (this.gameSettings.field_183018_l.contains(s)) {
					stringbuilder.append(" (incompatible)");
				}
			}

			return stringbuilder.toString();
		});
		report.getCategory().addCrashSectionCallable("Current Language", () -> this.languageManager.getCurrentLanguage().toString());
		report.getCategory().addCrashSectionCallable("Profiler Position", () -> this.mcProfiler.profilingEnabled ? this.mcProfiler.getNameOfLastSection() : "N/A (disabled)");
		report.getCategory().addCrashSectionCallable("CPU", OpenGlHelper::func_183029_j);

		if (this.world != null) {
			this.world.addWorldInfoToCrashReport(report);
		}

		return report;
	}

	public static Minecraft getMinecraft() {
		return theMinecraft;
	}

	public ListenableFuture<Object> scheduleResourcesRefresh() {
		return this.addScheduledTask(this::refreshResources);
	}

	public void addServerStatsToSnooper(PlayerUsageSnooper playerSnooper) {
		playerSnooper.addClientStat("fps", debugFPS);
		playerSnooper.addClientStat("vsync_enabled", this.gameSettings.enableVsync);
		playerSnooper.addClientStat("display_frequency", Display.getDisplayMode().getFrequency());
		playerSnooper.addClientStat("display_type", this.fullscreen ? "fullscreen" : "windowed");
		playerSnooper.addClientStat("run_time", (MinecraftServer.getCurrentTimeMillis() - playerSnooper.getMinecraftStartTimeMillis()) / 60L * 1000L);
		playerSnooper.addClientStat("current_action", this.func_181538_aA());
		String s = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN ? "little" : "big";
		playerSnooper.addClientStat("endianness", s);
		playerSnooper.addClientStat("resource_packs", this.resourcePackRepository.getRepositoryEntries().size());
		int i = 0;

		for (ResourcePackRepository.Entry entry : this.resourcePackRepository.getRepositoryEntries()) {
			playerSnooper.addClientStat("resource_pack[" + i++ + "]", entry.getResourcePackName());
		}

		if (this.theIntegratedServer != null && this.theIntegratedServer.getPlayerUsageSnooper() != null) {
			playerSnooper.addClientStat("snooper_partner", this.theIntegratedServer.getPlayerUsageSnooper().getUniqueID());
		}
	}

	private String func_181538_aA() {
		return this.theIntegratedServer != null ? (this.theIntegratedServer.getPublic() ? "hosting_lan" : "singleplayer") : (this.currentServerData != null ? (this.currentServerData.func_181041_d() ? "playing_lan" : "multiplayer") : "out_of_game");
	}

	public void addServerTypeToSnooper(PlayerUsageSnooper playerSnooper) {
		playerSnooper.addStatToSnooper("opengl_version", GL11.glGetString(GL11.GL_VERSION));
		playerSnooper.addStatToSnooper("opengl_vendor", GL11.glGetString(GL11.GL_VENDOR));
		playerSnooper.addStatToSnooper("client_brand", ClientBrandRetriever.getClientModName());
		playerSnooper.addStatToSnooper("launched_version", this.launchedVersion);
		ContextCapabilities capabilities = GLContext.getCapabilities();
		playerSnooper.addStatToSnooper("gl_caps[ARB_arrays_of_arrays]", capabilities.GL_ARB_arrays_of_arrays);
		playerSnooper.addStatToSnooper("gl_caps[ARB_base_instance]", capabilities.GL_ARB_base_instance);
		playerSnooper.addStatToSnooper("gl_caps[ARB_blend_func_extended]", capabilities.GL_ARB_blend_func_extended);
		playerSnooper.addStatToSnooper("gl_caps[ARB_clear_buffer_object]", capabilities.GL_ARB_clear_buffer_object);
		playerSnooper.addStatToSnooper("gl_caps[ARB_color_buffer_float]", capabilities.GL_ARB_color_buffer_float);
		playerSnooper.addStatToSnooper("gl_caps[ARB_compatibility]", capabilities.GL_ARB_compatibility);
		playerSnooper.addStatToSnooper("gl_caps[ARB_compressed_texture_pixel_storage]", capabilities.GL_ARB_compressed_texture_pixel_storage);
		playerSnooper.addStatToSnooper("gl_caps[ARB_compute_shader]", capabilities.GL_ARB_compute_shader);
		playerSnooper.addStatToSnooper("gl_caps[ARB_copy_buffer]", capabilities.GL_ARB_copy_buffer);
		playerSnooper.addStatToSnooper("gl_caps[ARB_copy_image]", capabilities.GL_ARB_copy_image);
		playerSnooper.addStatToSnooper("gl_caps[ARB_depth_buffer_float]", capabilities.GL_ARB_depth_buffer_float);
		playerSnooper.addStatToSnooper("gl_caps[ARB_compute_shader]", capabilities.GL_ARB_compute_shader);
		playerSnooper.addStatToSnooper("gl_caps[ARB_copy_buffer]", capabilities.GL_ARB_copy_buffer);
		playerSnooper.addStatToSnooper("gl_caps[ARB_copy_image]", capabilities.GL_ARB_copy_image);
		playerSnooper.addStatToSnooper("gl_caps[ARB_depth_buffer_float]", capabilities.GL_ARB_depth_buffer_float);
		playerSnooper.addStatToSnooper("gl_caps[ARB_depth_clamp]", capabilities.GL_ARB_depth_clamp);
		playerSnooper.addStatToSnooper("gl_caps[ARB_depth_texture]", capabilities.GL_ARB_depth_texture);
		playerSnooper.addStatToSnooper("gl_caps[ARB_draw_buffers]", capabilities.GL_ARB_draw_buffers);
		playerSnooper.addStatToSnooper("gl_caps[ARB_draw_buffers_blend]", capabilities.GL_ARB_draw_buffers_blend);
		playerSnooper.addStatToSnooper("gl_caps[ARB_draw_elements_base_vertex]", capabilities.GL_ARB_draw_elements_base_vertex);
		playerSnooper.addStatToSnooper("gl_caps[ARB_draw_indirect]", capabilities.GL_ARB_draw_indirect);
		playerSnooper.addStatToSnooper("gl_caps[ARB_draw_instanced]", capabilities.GL_ARB_draw_instanced);
		playerSnooper.addStatToSnooper("gl_caps[ARB_explicit_attrib_location]", capabilities.GL_ARB_explicit_attrib_location);
		playerSnooper.addStatToSnooper("gl_caps[ARB_explicit_uniform_location]", capabilities.GL_ARB_explicit_uniform_location);
		playerSnooper.addStatToSnooper("gl_caps[ARB_fragment_layer_viewport]", capabilities.GL_ARB_fragment_layer_viewport);
		playerSnooper.addStatToSnooper("gl_caps[ARB_fragment_program]", capabilities.GL_ARB_fragment_program);
		playerSnooper.addStatToSnooper("gl_caps[ARB_fragment_shader]", capabilities.GL_ARB_fragment_shader);
		playerSnooper.addStatToSnooper("gl_caps[ARB_fragment_program_shadow]", capabilities.GL_ARB_fragment_program_shadow);
		playerSnooper.addStatToSnooper("gl_caps[ARB_framebuffer_object]", capabilities.GL_ARB_framebuffer_object);
		playerSnooper.addStatToSnooper("gl_caps[ARB_framebuffer_sRGB]", capabilities.GL_ARB_framebuffer_sRGB);
		playerSnooper.addStatToSnooper("gl_caps[ARB_geometry_shader4]", capabilities.GL_ARB_geometry_shader4);
		playerSnooper.addStatToSnooper("gl_caps[ARB_gpu_shader5]", capabilities.GL_ARB_gpu_shader5);
		playerSnooper.addStatToSnooper("gl_caps[ARB_half_float_pixel]", capabilities.GL_ARB_half_float_pixel);
		playerSnooper.addStatToSnooper("gl_caps[ARB_half_float_vertex]", capabilities.GL_ARB_half_float_vertex);
		playerSnooper.addStatToSnooper("gl_caps[ARB_instanced_arrays]", capabilities.GL_ARB_instanced_arrays);
		playerSnooper.addStatToSnooper("gl_caps[ARB_map_buffer_alignment]", capabilities.GL_ARB_map_buffer_alignment);
		playerSnooper.addStatToSnooper("gl_caps[ARB_map_buffer_range]", capabilities.GL_ARB_map_buffer_range);
		playerSnooper.addStatToSnooper("gl_caps[ARB_multisample]", capabilities.GL_ARB_multisample);
		playerSnooper.addStatToSnooper("gl_caps[ARB_multitexture]", capabilities.GL_ARB_multitexture);
		playerSnooper.addStatToSnooper("gl_caps[ARB_occlusion_query2]", capabilities.GL_ARB_occlusion_query2);
		playerSnooper.addStatToSnooper("gl_caps[ARB_pixel_buffer_object]", capabilities.GL_ARB_pixel_buffer_object);
		playerSnooper.addStatToSnooper("gl_caps[ARB_seamless_cube_map]", capabilities.GL_ARB_seamless_cube_map);
		playerSnooper.addStatToSnooper("gl_caps[ARB_shader_objects]", capabilities.GL_ARB_shader_objects);
		playerSnooper.addStatToSnooper("gl_caps[ARB_shader_stencil_export]", capabilities.GL_ARB_shader_stencil_export);
		playerSnooper.addStatToSnooper("gl_caps[ARB_shader_texture_lod]", capabilities.GL_ARB_shader_texture_lod);
		playerSnooper.addStatToSnooper("gl_caps[ARB_shadow]", capabilities.GL_ARB_shadow);
		playerSnooper.addStatToSnooper("gl_caps[ARB_shadow_ambient]", capabilities.GL_ARB_shadow_ambient);
		playerSnooper.addStatToSnooper("gl_caps[ARB_stencil_texturing]", capabilities.GL_ARB_stencil_texturing);
		playerSnooper.addStatToSnooper("gl_caps[ARB_sync]", capabilities.GL_ARB_sync);
		playerSnooper.addStatToSnooper("gl_caps[ARB_tessellation_shader]", capabilities.GL_ARB_tessellation_shader);
		playerSnooper.addStatToSnooper("gl_caps[ARB_texture_border_clamp]", capabilities.GL_ARB_texture_border_clamp);
		playerSnooper.addStatToSnooper("gl_caps[ARB_texture_buffer_object]", capabilities.GL_ARB_texture_buffer_object);
		playerSnooper.addStatToSnooper("gl_caps[ARB_texture_cube_map]", capabilities.GL_ARB_texture_cube_map);
		playerSnooper.addStatToSnooper("gl_caps[ARB_texture_cube_map_array]", capabilities.GL_ARB_texture_cube_map_array);
		playerSnooper.addStatToSnooper("gl_caps[ARB_texture_non_power_of_two]", capabilities.GL_ARB_texture_non_power_of_two);
		playerSnooper.addStatToSnooper("gl_caps[ARB_uniform_buffer_object]", capabilities.GL_ARB_uniform_buffer_object);
		playerSnooper.addStatToSnooper("gl_caps[ARB_vertex_blend]", capabilities.GL_ARB_vertex_blend);
		playerSnooper.addStatToSnooper("gl_caps[ARB_vertex_buffer_object]", capabilities.GL_ARB_vertex_buffer_object);
		playerSnooper.addStatToSnooper("gl_caps[ARB_vertex_program]", capabilities.GL_ARB_vertex_program);
		playerSnooper.addStatToSnooper("gl_caps[ARB_vertex_shader]", capabilities.GL_ARB_vertex_shader);
		playerSnooper.addStatToSnooper("gl_caps[EXT_bindable_uniform]", capabilities.GL_EXT_bindable_uniform);
		playerSnooper.addStatToSnooper("gl_caps[EXT_blend_equation_separate]", capabilities.GL_EXT_blend_equation_separate);
		playerSnooper.addStatToSnooper("gl_caps[EXT_blend_func_separate]", capabilities.GL_EXT_blend_func_separate);
		playerSnooper.addStatToSnooper("gl_caps[EXT_blend_minmax]", capabilities.GL_EXT_blend_minmax);
		playerSnooper.addStatToSnooper("gl_caps[EXT_blend_subtract]", capabilities.GL_EXT_blend_subtract);
		playerSnooper.addStatToSnooper("gl_caps[EXT_draw_instanced]", capabilities.GL_EXT_draw_instanced);
		playerSnooper.addStatToSnooper("gl_caps[EXT_framebuffer_multisample]", capabilities.GL_EXT_framebuffer_multisample);
		playerSnooper.addStatToSnooper("gl_caps[EXT_framebuffer_object]", capabilities.GL_EXT_framebuffer_object);
		playerSnooper.addStatToSnooper("gl_caps[EXT_framebuffer_sRGB]", capabilities.GL_EXT_framebuffer_sRGB);
		playerSnooper.addStatToSnooper("gl_caps[EXT_geometry_shader4]", capabilities.GL_EXT_geometry_shader4);
		playerSnooper.addStatToSnooper("gl_caps[EXT_gpu_program_parameters]", capabilities.GL_EXT_gpu_program_parameters);
		playerSnooper.addStatToSnooper("gl_caps[EXT_gpu_shader4]", capabilities.GL_EXT_gpu_shader4);
		playerSnooper.addStatToSnooper("gl_caps[EXT_multi_draw_arrays]", capabilities.GL_EXT_multi_draw_arrays);
		playerSnooper.addStatToSnooper("gl_caps[EXT_packed_depth_stencil]", capabilities.GL_EXT_packed_depth_stencil);
		playerSnooper.addStatToSnooper("gl_caps[EXT_paletted_texture]", capabilities.GL_EXT_paletted_texture);
		playerSnooper.addStatToSnooper("gl_caps[EXT_rescale_normal]", capabilities.GL_EXT_rescale_normal);
		playerSnooper.addStatToSnooper("gl_caps[EXT_separate_shader_objects]", capabilities.GL_EXT_separate_shader_objects);
		playerSnooper.addStatToSnooper("gl_caps[EXT_shader_image_load_store]", capabilities.GL_EXT_shader_image_load_store);
		playerSnooper.addStatToSnooper("gl_caps[EXT_shadow_funcs]", capabilities.GL_EXT_shadow_funcs);
		playerSnooper.addStatToSnooper("gl_caps[EXT_shared_texture_palette]", capabilities.GL_EXT_shared_texture_palette);
		playerSnooper.addStatToSnooper("gl_caps[EXT_stencil_clear_tag]", capabilities.GL_EXT_stencil_clear_tag);
		playerSnooper.addStatToSnooper("gl_caps[EXT_stencil_two_side]", capabilities.GL_EXT_stencil_two_side);
		playerSnooper.addStatToSnooper("gl_caps[EXT_stencil_wrap]", capabilities.GL_EXT_stencil_wrap);
		playerSnooper.addStatToSnooper("gl_caps[EXT_texture_3d]", capabilities.GL_EXT_texture_3d);
		playerSnooper.addStatToSnooper("gl_caps[EXT_texture_array]", capabilities.GL_EXT_texture_array);
		playerSnooper.addStatToSnooper("gl_caps[EXT_texture_buffer_object]", capabilities.GL_EXT_texture_buffer_object);
		playerSnooper.addStatToSnooper("gl_caps[EXT_texture_integer]", capabilities.GL_EXT_texture_integer);
		playerSnooper.addStatToSnooper("gl_caps[EXT_texture_lod_bias]", capabilities.GL_EXT_texture_lod_bias);
		playerSnooper.addStatToSnooper("gl_caps[EXT_texture_sRGB]", capabilities.GL_EXT_texture_sRGB);
		playerSnooper.addStatToSnooper("gl_caps[EXT_vertex_shader]", capabilities.GL_EXT_vertex_shader);
		playerSnooper.addStatToSnooper("gl_caps[EXT_vertex_weighting]", capabilities.GL_EXT_vertex_weighting);
		playerSnooper.addStatToSnooper("gl_caps[gl_max_vertex_uniforms]", GL11.glGetInteger(GL20.GL_MAX_VERTEX_UNIFORM_COMPONENTS));
		GL11.glGetError();
		playerSnooper.addStatToSnooper("gl_caps[gl_max_fragment_uniforms]", GL11.glGetInteger(GL20.GL_MAX_FRAGMENT_UNIFORM_COMPONENTS));
		GL11.glGetError();
		playerSnooper.addStatToSnooper("gl_caps[gl_max_vertex_attribs]", GL11.glGetInteger(GL20.GL_MAX_VERTEX_ATTRIBS));
		GL11.glGetError();
		playerSnooper.addStatToSnooper("gl_caps[gl_max_vertex_texture_image_units]", GL11.glGetInteger(GL20.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS));
		GL11.glGetError();
		playerSnooper.addStatToSnooper("gl_caps[gl_max_texture_image_units]", GL11.glGetInteger(GL20.GL_MAX_TEXTURE_IMAGE_UNITS));
		GL11.glGetError();
		playerSnooper.addStatToSnooper("gl_caps[gl_max_texture_image_units]", GL11.glGetInteger(35071));
		GL11.glGetError();
		playerSnooper.addStatToSnooper("gl_max_texture_size", getGLMaximumTextureSize());
	}

	public static int getGLMaximumTextureSize() {
		for (int i = 16384; i > 0; i >>= 1) {
			GL11.glTexImage2D(GL11.GL_PROXY_TEXTURE_2D, 0, GL11.GL_RGBA, i, i, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
			int j = GL11.glGetTexLevelParameteri(GL11.GL_PROXY_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);

			if (j != 0) {
				return i;
			}
		}

		return -1;
	}

	public boolean isSnooperEnabled() {
		return this.gameSettings.snooperEnabled;
	}

	public void setServerData(ServerData serverData) {
		this.currentServerData = serverData;
	}

	public ServerData getCurrentServerData() {
		return this.currentServerData;
	}

	public boolean isIntegratedServerRunning() {
		return this.integratedServerIsRunning;
	}

	public boolean isSingleplayer() {
		return this.integratedServerIsRunning && this.theIntegratedServer != null;
	}

	public IntegratedServer getIntegratedServer() {
		return this.theIntegratedServer;
	}

	public static void stopIntegratedServer() {
		if (theMinecraft != null) {
			IntegratedServer integratedserver = theMinecraft.getIntegratedServer();

			if (integratedserver != null) {
				integratedserver.stopServer();
			}
		}
	}

	public PlayerUsageSnooper getPlayerUsageSnooper() {
		return this.usageSnooper;
	}

	public static long getSystemTime() {
		return Sys.getTime() * 1000L / Sys.getTimerResolution();
	}

	public boolean isFullScreen() {
		return this.fullscreen;
	}

	public Session getSession() {
		return this.session;
	}

	public PropertyMap func_181037_M() {
		if (this.profileProperties.isEmpty()) {
			GameProfile gameprofile = this.getSessionService().fillProfileProperties(this.session.getProfile(), false);
			this.profileProperties.putAll(gameprofile.getProperties());
		}

		return this.profileProperties;
	}

	public Proxy getProxy() {
		return this.proxy;
	}

	public TextureManager getTextureManager() {
		return this.renderEngine;
	}

	public IResourceManager getResourceManager() {
		return this.resourceManager;
	}

	public ResourcePackRepository getResourcePackRepository() {
		return this.resourcePackRepository;
	}

	public LanguageManager getLanguageManager() {
		return this.languageManager;
	}

	public TextureMap getTextureMapBlocks() {
		return this.textureMapBlocks;
	}

	public boolean isJava64bit() {
		return this.jvm64bit;
	}

	public boolean isGamePaused() {
		return this.isGamePaused;
	}

	public SoundHandler getSoundHandler() {
		return this.soundHandler;
	}

	public MusicTicker.MusicType getAmbientMusicType() {
		return this.player != null ? (this.player.worldObj.provider instanceof WorldProviderHell ? MusicTicker.MusicType.NETHER : (this.player.worldObj.provider instanceof WorldProviderEnd ? (BossStatus.bossName != null && BossStatus.statusBarTime > 0 ? MusicTicker.MusicType.END_BOSS : MusicTicker.MusicType.END) : (this.player.capabilities.isCreativeMode && this.player.capabilities.allowFlying ? MusicTicker.MusicType.CREATIVE : MusicTicker.MusicType.GAME))) : MusicTicker.MusicType.MENU;
	}

	public void dispatchKeypresses() {
	}

	public MinecraftSessionService getSessionService() {
		return this.sessionService;
	}

	public SkinManager getSkinManager() {
		return this.skinManager;
	}

	public Entity getRenderViewEntity() {
		return this.renderViewEntity;
	}

	public void setRenderViewEntity(Entity viewingEntity) {
		this.renderViewEntity = viewingEntity;
		this.entityRenderer.loadEntityShader(viewingEntity);
	}

	public <V> ListenableFuture<V> addScheduledTask(Callable<V> callableToSchedule) {
		Validate.notNull(callableToSchedule);

		if (!this.isCallingFromMinecraftThread()) {
			ListenableFutureTask<V> futureTask = ListenableFutureTask.create(callableToSchedule);

			synchronized (this.scheduledTasks) {
				this.scheduledTasks.add(futureTask);
				return futureTask;
			}
		} else {
			try {
				return Futures.immediateFuture(callableToSchedule.call());
			} catch (Exception exception) {
				return Futures.immediateFailedCheckedFuture(exception);
			}
		}
	}

	public ListenableFuture<Object> addScheduledTask(Runnable runnableToSchedule) {
		Validate.notNull(runnableToSchedule);
		return this.addScheduledTask(Executors.callable(runnableToSchedule));
	}

	public boolean isCallingFromMinecraftThread() {
		return Thread.currentThread() == this.mcThread;
	}

	public BlockRendererDispatcher getBlockRendererDispatcher() {
		return this.blockRenderDispatcher;
	}

	public RenderManager getRenderManager() {
		return this.renderManager;
	}

	public RenderItem getRenderItem() {
		return this.renderItem;
	}

	public ItemRenderer getItemRenderer() {
		return this.itemRenderer;
	}

	public static int getDebugFPS() {
		return debugFPS;
	}

	public FrameTimer func_181539_aj() {
		return this.frameTimer;
	}

	public static Map<String, String> getSessionInfo() {
		Map<String, String> map = Maps.newHashMap();
		map.put("X-Minecraft-Username", getMinecraft().getSession().getUsername());
		map.put("X-Minecraft-UUID", getMinecraft().getSession().getPlayerID());
		map.put("X-Minecraft-Version", "1.8.10");
		return map;
	}

	public boolean isRunning() {
		return running;
	}
}
