package com.murengezi.minecraft.client.renderer.entity;

import com.murengezi.chocolate.Util.MinecraftUtils;
import com.murengezi.minecraft.block.Block;
import com.murengezi.minecraft.client.gui.GUI;
import com.murengezi.minecraft.client.model.ModelBiped;
import com.murengezi.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import com.murengezi.minecraft.client.renderer.GlStateManager;
import com.murengezi.minecraft.client.renderer.Tessellator;
import com.murengezi.minecraft.client.renderer.WorldRenderer;
import com.murengezi.minecraft.client.renderer.culling.ICamera;
import com.murengezi.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.murengezi.minecraft.client.renderer.texture.TextureMap;
import com.murengezi.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.optifine.config.Config;
import net.optifine.entity.model.IEntityRenderer;
import net.optifine.shaders.Shaders;
import org.lwjgl.opengl.GL11;

public abstract class Render<T extends Entity> implements IEntityRenderer {

	private static final ResourceLocation shadowTextures = new ResourceLocation("textures/misc/shadow.png");
	protected final RenderManager renderManager;
	public float shadowSize;

	protected float shadowOpaque = 1.0F;
	private Class entityClass = null;
	private ResourceLocation locationTextureCustom = null;

	protected Render(RenderManager renderManager) {
		this.renderManager = renderManager;
	}

	public boolean shouldRender(T livingEntity, ICamera camera, double camX, double camY, double camZ) {
		AxisAlignedBB axisalignedbb = livingEntity.getEntityBoundingBox();

		if (axisalignedbb.func_181656_b() || axisalignedbb.getAverageEdgeLength() == 0.0D) {
			axisalignedbb = new AxisAlignedBB(livingEntity.posX - 2.0D, livingEntity.posY - 2.0D, livingEntity.posZ - 2.0D, livingEntity.posX + 2.0D, livingEntity.posY + 2.0D, livingEntity.posZ + 2.0D);
		}

		return livingEntity.isInRangeToRender3d(camX, camY, camZ) && (livingEntity.ignoreFrustumCheck || camera.isBoundingBoxInFrustum(axisalignedbb));
	}

	public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
		this.renderName(entity, x, y, z);
	}

	protected void renderName(T entity, double x, double y, double z) {
		if (this.canRenderName(entity)) {
			this.renderLivingLabel(entity, entity.getDisplayName().getFormattedText(), x, y, z);
		}
	}

	protected boolean canRenderName(T entity) {
		return entity.getAlwaysRenderNameTagForRender() && entity.hasCustomName();
	}

	protected void renderOffsetLivingLabel(T entityIn, double x, double y, double z, String str, float p_177069_9_, double p_177069_10_) {
		this.renderLivingLabel(entityIn, str, x, y, z);
	}

	protected abstract ResourceLocation getEntityTexture(T entity);

	protected boolean bindEntityTexture(T entity) {
		ResourceLocation resourcelocation = this.getEntityTexture(entity);

		if (this.locationTextureCustom != null) {
			resourcelocation = this.locationTextureCustom;
		}

		if (resourcelocation == null) {
			return false;
		} else {
			this.bindTexture(resourcelocation);
			return true;
		}
	}

	public void bindTexture(ResourceLocation location) {
		this.renderManager.renderEngine.bindTexture(location);
	}

	private void renderEntityOnFire(Entity entity, double x, double y, double z, float partialTicks) {
		GlStateManager.disableLighting();
		TextureMap texturemap = Minecraft.getMinecraft().getTextureMapBlocks();
		TextureAtlasSprite textureatlassprite = texturemap.getAtlasSprite("minecraft:blocks/fire_layer_0");
		TextureAtlasSprite textureatlassprite1 = texturemap.getAtlasSprite("minecraft:blocks/fire_layer_1");
		GlStateManager.pushMatrix();
		GlStateManager.translate((float) x, (float) y, (float) z);
		float f = entity.width * 1.4F;
		GlStateManager.scale(f, f, f);
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		float f1 = 0.5F;
		float f2 = 0.0F;
		float f3 = entity.height / f;
		float f4 = (float) (entity.posY - entity.getEntityBoundingBox().minY);
		GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
		GlStateManager.translate(0.0F, 0.0F, -0.3F + (float) ((int) f3) * 0.02F);
		GlStateManager.colorAllMax();
		float f5 = 0.0F;
		int i = 0;
		boolean flag = Config.isMultiTexture();

		if (flag) {
			worldrenderer.setBlockLayer(EnumWorldBlockLayer.SOLID);
		}

		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);

		while (f3 > 0.0F) {
			TextureAtlasSprite textureatlassprite2 = i % 2 == 0 ? textureatlassprite : textureatlassprite1;
			worldrenderer.setSprite(textureatlassprite2);
			this.bindTexture(TextureMap.locationBlocksTexture);
			float f6 = textureatlassprite2.getMinU();
			float f7 = textureatlassprite2.getMinV();
			float f8 = textureatlassprite2.getMaxU();
			float f9 = textureatlassprite2.getMaxV();

			if (i / 2 % 2 == 0) {
				float f10 = f8;
				f8 = f6;
				f6 = f10;
			}

			worldrenderer.pos(f1 - f2, 0.0F - f4, f5).tex(f8, f9).endVertex();
			worldrenderer.pos(-f1 - f2, 0.0F - f4, f5).tex(f6, f9).endVertex();
			worldrenderer.pos(-f1 - f2, 1.4F - f4, f5).tex(f6, f7).endVertex();
			worldrenderer.pos(f1 - f2, 1.4F - f4, f5).tex(f8, f7).endVertex();
			f3 -= 0.45F;
			f4 -= 0.45F;
			f1 *= 0.9F;
			f5 += 0.03F;
			++i;
		}

		tessellator.draw();

		if (flag) {
			worldrenderer.setBlockLayer(null);
			GlStateManager.bindCurrentTexture();
		}

		GlStateManager.popMatrix();
		GlStateManager.enableLightning();
	}

	private void renderShadow(Entity entityIn, double x, double y, double z, float shadowAlpha, float partialTicks) {
		if (!Config.isShaders() || !Shaders.shouldSkipDefaultShadow) {
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(770, 771);
			this.renderManager.renderEngine.bindTexture(shadowTextures);
			World world = this.getWorldFromRenderManager();
			GlStateManager.depthMask(false);
			float f = this.shadowSize;

			if (entityIn instanceof EntityLiving) {
				EntityLiving entityliving = (EntityLiving) entityIn;
				f *= entityliving.getRenderSizeModifier();

				if (entityliving.isChild()) {
					f *= 0.5F;
				}
			}

			double d5 = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
			double d0 = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
			double d1 = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
			int i = MathHelper.floor_double(d5 - (double) f);
			int j = MathHelper.floor_double(d5 + (double) f);
			int k = MathHelper.floor_double(d0 - (double) f);
			int l = MathHelper.floor_double(d0);
			int i1 = MathHelper.floor_double(d1 - (double) f);
			int j1 = MathHelper.floor_double(d1 + (double) f);
			double d2 = x - d5;
			double d3 = y - d0;
			double d4 = z - d1;
			Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer worldrenderer = tessellator.getWorldRenderer();
			worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

			for (BlockPos blockpos : BlockPos.getAllInBoxMutable(new BlockPos(i, k, i1), new BlockPos(j, l, j1))) {
				Block block = world.getBlockState(blockpos.down()).getBlock();

				if (block.getRenderType() != -1 && world.getLightFromNeighbors(blockpos) > 3) {
					this.func_180549_a(block, x, y, z, blockpos, shadowAlpha, f, d2, d3, d4);
				}
			}

			tessellator.draw();
			GlStateManager.colorAllMax();
			GlStateManager.disableBlend();
			GlStateManager.depthMask(true);
		}
	}

	private World getWorldFromRenderManager() {
		return this.renderManager.worldObj;
	}

	private void func_180549_a(Block block, double p_180549_2_, double p_180549_4_, double p_180549_6_, BlockPos pos, float p_180549_9_, float p_180549_10_, double p_180549_11_, double p_180549_13_, double p_180549_15_) {
		if (block.isFullCube()) {
			Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer worldrenderer = tessellator.getWorldRenderer();
			double d0 = ((double) p_180549_9_ - (p_180549_4_ - ((double) pos.getY() + p_180549_13_)) / 2.0D) * 0.5D * (double) this.getWorldFromRenderManager().getLightBrightness(pos);

			if (d0 >= 0.0D) {
				if (d0 > 1.0D) {
					d0 = 1.0D;
				}

				double d1 = (double) pos.getX() + block.getBlockBoundsMinX() + p_180549_11_;
				double d2 = (double) pos.getX() + block.getBlockBoundsMaxX() + p_180549_11_;
				double d3 = (double) pos.getY() + block.getBlockBoundsMinY() + p_180549_13_ + 0.015625D;
				double d4 = (double) pos.getZ() + block.getBlockBoundsMinZ() + p_180549_15_;
				double d5 = (double) pos.getZ() + block.getBlockBoundsMaxZ() + p_180549_15_;
				float f = (float) ((p_180549_2_ - d1) / 2.0D / (double) p_180549_10_ + 0.5D);
				float f1 = (float) ((p_180549_2_ - d2) / 2.0D / (double) p_180549_10_ + 0.5D);
				float f2 = (float) ((p_180549_6_ - d4) / 2.0D / (double) p_180549_10_ + 0.5D);
				float f3 = (float) ((p_180549_6_ - d5) / 2.0D / (double) p_180549_10_ + 0.5D);
				worldrenderer.pos(d1, d3, d4).tex(f, f2).color(1.0F, 1.0F, 1.0F, (float) d0).endVertex();
				worldrenderer.pos(d1, d3, d5).tex(f, f3).color(1.0F, 1.0F, 1.0F, (float) d0).endVertex();
				worldrenderer.pos(d2, d3, d5).tex(f1, f3).color(1.0F, 1.0F, 1.0F, (float) d0).endVertex();
				worldrenderer.pos(d2, d3, d4).tex(f1, f2).color(1.0F, 1.0F, 1.0F, (float) d0).endVertex();
			}
		}
	}

	public static void renderOffsetAABB(AxisAlignedBB boundingBox, double x, double y, double z) {
		GlStateManager.disableTexture2D();
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		GlStateManager.colorAllMax();
		worldrenderer.setTranslation(x, y, z);
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_NORMAL);
		worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
		worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
		worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
		worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
		worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
		worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
		worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
		worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
		worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
		worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
		worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
		worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
		worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
		worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
		worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
		worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
		worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
		worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
		worldrenderer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
		worldrenderer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
		worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
		worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
		worldrenderer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
		worldrenderer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
		tessellator.draw();
		worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
		GlStateManager.enableTexture2D();
	}

	public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {
		if (this.renderManager.options != null) {
			if (this.renderManager.options.field_181151_V && this.shadowSize > 0.0F && !entityIn.isInvisible() && this.renderManager.isRenderShadow()) {
				double d0 = this.renderManager.getDistanceToCamera(entityIn.posX, entityIn.posY, entityIn.posZ);
				float f = (float) ((1.0D - d0 / 256.0D) * (double) this.shadowOpaque);

				if (f > 0.0F) {
					this.renderShadow(entityIn, x, y, z, f, partialTicks);
				}
			}

			if (entityIn.canRenderOnFire() && (!(entityIn instanceof EntityPlayer) || !((EntityPlayer) entityIn).isSpectator())) {
				this.renderEntityOnFire(entityIn, x, y, z, partialTicks);
			}
		}
	}

	public FontRenderer getFontRendererFromRenderManager() {
		return this.renderManager.getFontRenderer();
	}

	protected void renderLivingLabel(T entityIn, String str, double x, double y, double z) {
		double d0 = entityIn.getDistanceSqToEntity(this.renderManager.livingPlayer);

		if (d0 <= (double) (64 * 64)) {
			FontRenderer fontrenderer = this.getFontRendererFromRenderManager();
			float f = 1.6F;
			float f1 = 0.016666668F * f;
			GlStateManager.pushMatrix();
			GlStateManager.translate((float) x + 0.0F, (float) y + entityIn.height + 0.5F, (float) z);
			GL11.glNormal3f(0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(-this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(Minecraft.getMinecraft().gameSettings.thirdPersonView != 2 ? this.renderManager.playerViewX : -this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
			GlStateManager.scale(-f1, -f1, f1);
			GlStateManager.disableLighting();
			GlStateManager.depthMask(false);
			GlStateManager.disableDepth();
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
			Tessellator tessellator = Tessellator.getInstance();
			WorldRenderer worldrenderer = tessellator.getWorldRenderer();
			int i = 0;

			if (str.equals("deadmau5")) {
				i = -10;
			}

			int j = fontrenderer.getStringWidth(str) / 2;
			GlStateManager.disableTexture2D();
			worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
			worldrenderer.pos(-j - 1 - 8, -1 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
			worldrenderer.pos(-j - 1 - 8, 8 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
			worldrenderer.pos(j + 1, 8 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
			worldrenderer.pos(j + 1, -1 + i, 0.0D).color(0.0F, 0.0F, 0.0F, 0.25F).endVertex();
			tessellator.draw();
			GlStateManager.enableTexture2D();
			GlStateManager.colorAllMax();
			fontrenderer.drawString(str, -j, i, 553648127);
			GlStateManager.enableDepth();
			GlStateManager.depthMask(true);

			if (entityIn == MinecraftUtils.getPlayer()) {
				bindTexture(new ResourceLocation("chocolate/logo_16x16.png"));
				GlStateManager.colorAllMax();
				GUI.drawModalRectWithCustomSizedTexture(-j - 8, i, 0, 0, 8, 8, 8, 8);
			}

			fontrenderer.drawString(str, -j, i, -1);
			GlStateManager.enableLightning();
			GlStateManager.disableBlend();
			GlStateManager.colorAllMax();
			GlStateManager.popMatrix();
		}
	}

	public RenderManager getRenderManager() {
		return this.renderManager;
	}

    public Class getEntityClass() {
		return this.entityClass;
	}

	public void setEntityClass(Class p_setEntityClass_1_) {
		this.entityClass = p_setEntityClass_1_;
	}

	public void setLocationTextureCustom(ResourceLocation p_setLocationTextureCustom_1_) {
		this.locationTextureCustom = p_setLocationTextureCustom_1_;
	}

	public static void setModelBipedMain(RenderBiped p_setModelBipedMain_0_, ModelBiped p_setModelBipedMain_1_) {
		p_setModelBipedMain_0_.modelBipedMain = p_setModelBipedMain_1_;
	}

}