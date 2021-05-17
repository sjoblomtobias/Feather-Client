package com.murengezi.minecraft.client.renderer;

import com.murengezi.minecraft.block.Block;
import com.murengezi.minecraft.block.state.IBlockState;
import com.murengezi.minecraft.client.entity.AbstractClientPlayer;
import com.murengezi.minecraft.client.entity.EntityPlayerSP;
import com.murengezi.minecraft.client.Minecraft;
import com.murengezi.minecraft.client.renderer.block.model.ItemCameraTransforms;
import com.murengezi.minecraft.client.renderer.entity.Render;
import com.murengezi.minecraft.client.renderer.entity.RenderItem;
import com.murengezi.minecraft.client.renderer.entity.RenderManager;
import com.murengezi.minecraft.client.renderer.entity.RenderPlayer;
import com.murengezi.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.murengezi.minecraft.client.renderer.texture.TextureMap;
import com.murengezi.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import com.murengezi.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.storage.MapData;
import net.optifine.DynamicLights;
import net.optifine.config.Config;
import net.optifine.reflect.Reflector;
import net.optifine.shaders.Shaders;
import org.lwjgl.opengl.GL11;

public class ItemRenderer {
	private static final ResourceLocation RES_MAP_BACKGROUND = new ResourceLocation("textures/map/map_background.png");

	private final Minecraft mc;
	private ItemStack itemToRender;
	private float equippedProgress, prevEquippedProgress;
	private final RenderManager renderManager;
	private final RenderItem itemRenderer;
	private int equippedItemSlot = -1;

	public ItemRenderer(Minecraft mc) {
		this.mc = mc;
		this.renderManager = mc.getRenderManager();
		this.itemRenderer = mc.getRenderItem();
	}

	public void renderItem(EntityLivingBase entityIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform) {
		if (heldStack != null) {
			Item item = heldStack.getItem();
			Block block = Block.getBlockFromItem(item);
			GlStateManager.pushMatrix();

			if (this.itemRenderer.shouldRenderItemIn3D(heldStack)) {
				GlStateManager.scale(2.0F, 2.0F, 2.0F);

				if (this.isBlockTranslucent(block) && (!Config.isShaders() || !Shaders.renderItemKeepDepthMask)) {
					GlStateManager.depthMask(false);
				}
			}

			this.itemRenderer.renderItemModelForEntity(heldStack, entityIn, transform);

			if (this.isBlockTranslucent(block)) {
				GlStateManager.depthMask(true);
			}

			GlStateManager.popMatrix();
		}
	}

	private boolean isBlockTranslucent(Block block) {
		return block != null && block.getBlockLayer() == EnumWorldBlockLayer.TRANSLUCENT;
	}

	private void func_178101_a(float angle, float p_178101_2_) {
		GlStateManager.pushMatrix();
		GlStateManager.rotate(angle, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(p_178101_2_, 0.0F, 1.0F, 0.0F);
		RenderHelper.enableStandardItemLighting();
		GlStateManager.popMatrix();
	}

	private void func_178109_a(AbstractClientPlayer clientPlayer) {
		int i = this.mc.world.getCombinedLight(new BlockPos(clientPlayer.posX, clientPlayer.posY + (double) clientPlayer.getEyeHeight(), clientPlayer.posZ), 0);

		if (Config.isDynamicLights()) {
			i = DynamicLights.getCombinedLight(this.mc.getRenderViewEntity(), i);
		}

		float f = (float) (i & 65535);
		float f1 = (float) (i >> 16);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, f, f1);
	}

	private void func_178110_a(EntityPlayerSP entityplayerspIn, float partialTicks) {
		float f = entityplayerspIn.prevRenderArmPitch + (entityplayerspIn.renderArmPitch - entityplayerspIn.prevRenderArmPitch) * partialTicks;
		float f1 = entityplayerspIn.prevRenderArmYaw + (entityplayerspIn.renderArmYaw - entityplayerspIn.prevRenderArmYaw) * partialTicks;
		GlStateManager.rotate((entityplayerspIn.rotationPitch - f) * 0.1F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate((entityplayerspIn.rotationYaw - f1) * 0.1F, 0.0F, 1.0F, 0.0F);
	}

	private float func_178100_c(float p_178100_1_) {
		float f = 1.0F - p_178100_1_ / 45.0F + 0.1F;
		f = MathHelper.clamp_float(f, 0.0F, 1.0F);
		f = -MathHelper.cos(f * (float) Math.PI) * 0.5F + 0.5F;
		return f;
	}

	private void renderRightArm(RenderPlayer renderPlayerIn) {
		GlStateManager.pushMatrix();
		GlStateManager.rotate(54.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(64.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(-62.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.translate(0.25F, -0.85F, 0.75F);
		renderPlayerIn.renderRightArm(this.mc.player);
		GlStateManager.popMatrix();
	}

	private void renderLeftArm(RenderPlayer renderPlayerIn) {
		GlStateManager.pushMatrix();
		GlStateManager.rotate(92.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(45.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(41.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.translate(-0.3F, -1.1F, 0.45F);
		renderPlayerIn.renderLeftArm(this.mc.player);
		GlStateManager.popMatrix();
	}

	private void renderPlayerArms(AbstractClientPlayer clientPlayer) {
		this.mc.getTextureManager().bindTexture(clientPlayer.getLocationSkin());
		Render<AbstractClientPlayer> render = this.renderManager.getEntityRenderObject(this.mc.player);
		RenderPlayer renderplayer = (RenderPlayer) render;

		if (!clientPlayer.isInvisible()) {
			GlStateManager.disableCull();
			this.renderRightArm(renderplayer);
			this.renderLeftArm(renderplayer);
			GlStateManager.enableCull();
		}
	}

	private void renderItemMap(AbstractClientPlayer clientPlayer, float p_178097_2_, float p_178097_3_, float p_178097_4_) {
		float f = -0.4F * MathHelper.sin(MathHelper.sqrt_float(p_178097_4_) * (float) Math.PI);
		float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(p_178097_4_) * (float) Math.PI * 2.0F);
		float f2 = -0.2F * MathHelper.sin(p_178097_4_ * (float) Math.PI);
		GlStateManager.translate(f, f1, f2);
		float f3 = this.func_178100_c(p_178097_2_);
		GlStateManager.translate(0.0F, 0.04F, -0.72F);
		GlStateManager.translate(0.0F, p_178097_3_ * -1.2F, 0.0F);
		GlStateManager.translate(0.0F, f3 * -0.5F, 0.0F);
		GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(f3 * -85.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.rotate(0.0F, 1.0F, 0.0F, 0.0F);
		this.renderPlayerArms(clientPlayer);
		float f4 = MathHelper.sin(p_178097_4_ * p_178097_4_ * (float) Math.PI);
		float f5 = MathHelper.sin(MathHelper.sqrt_float(p_178097_4_) * (float) Math.PI);
		GlStateManager.rotate(f4 * -20.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(f5 * -20.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.rotate(f5 * -80.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.scale(0.38F, 0.38F, 0.38F);
		GlStateManager.rotate(90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.rotate(0.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.translate(-1.0F, -1.0F, 0.0F);
		GlStateManager.scale(0.015625F, 0.015625F, 0.015625F);
		this.mc.getTextureManager().bindTexture(RES_MAP_BACKGROUND);
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		GL11.glNormal3f(0.0F, 0.0F, -1.0F);
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(-7.0D, 135.0D, 0.0D).tex(0.0D, 1.0D).endVertex();
		worldrenderer.pos(135.0D, 135.0D, 0.0D).tex(1.0D, 1.0D).endVertex();
		worldrenderer.pos(135.0D, -7.0D, 0.0D).tex(1.0D, 0.0D).endVertex();
		worldrenderer.pos(-7.0D, -7.0D, 0.0D).tex(0.0D, 0.0D).endVertex();
		tessellator.draw();
		MapData mapdata = Items.filled_map.getMapData(this.itemToRender, this.mc.world);

		if (mapdata != null) {
			this.mc.entityRenderer.getMapItemRenderer().renderMap(mapdata, false);
		}
	}

	private void func_178095_a(AbstractClientPlayer clientPlayer, float p_178095_2_, float p_178095_3_) {
		float f = -0.3F * MathHelper.sin(MathHelper.sqrt_float(p_178095_3_) * (float) Math.PI);
		float f1 = 0.4F * MathHelper.sin(MathHelper.sqrt_float(p_178095_3_) * (float) Math.PI * 2.0F);
		float f2 = -0.4F * MathHelper.sin(p_178095_3_ * (float) Math.PI);
		GlStateManager.translate(f, f1, f2);
		GlStateManager.translate(0.64000005F, -0.6F, -0.71999997F);
		GlStateManager.translate(0.0F, p_178095_2_ * -0.6F, 0.0F);
		GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
		float f3 = MathHelper.sin(p_178095_3_ * p_178095_3_ * (float) Math.PI);
		float f4 = MathHelper.sin(MathHelper.sqrt_float(p_178095_3_) * (float) Math.PI);
		GlStateManager.rotate(f4 * 70.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(f3 * -20.0F, 0.0F, 0.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(clientPlayer.getLocationSkin());
		GlStateManager.translate(-1.0F, 3.6F, 3.5F);
		GlStateManager.rotate(120.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.rotate(200.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(-135.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.scale(1.0F, 1.0F, 1.0F);
		GlStateManager.translate(5.6F, 0.0F, 0.0F);
		Render<AbstractClientPlayer> render = this.renderManager.getEntityRenderObject(this.mc.player);
		GlStateManager.disableCull();
		RenderPlayer renderplayer = (RenderPlayer) render;
		renderplayer.renderRightArm(this.mc.player);
		GlStateManager.enableCull();
	}

	private void func_178105_d(float p_178105_1_) {
		float f = -0.4F * MathHelper.sin(MathHelper.sqrt_float(p_178105_1_) * (float) Math.PI);
		float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(p_178105_1_) * (float) Math.PI * 2.0F);
		float f2 = -0.2F * MathHelper.sin(p_178105_1_ * (float) Math.PI);
		GlStateManager.translate(f, f1, f2);
	}

	private void func_178104_a(AbstractClientPlayer clientPlayer, float p_178104_2_) {
		float f = (float) clientPlayer.getItemInUseCount() - p_178104_2_ + 1.0F;
		float f1 = f / (float) this.itemToRender.getMaxItemUseDuration();
		float f2 = MathHelper.abs(MathHelper.cos(f / 4.0F * (float) Math.PI) * 0.1F);

		if (f1 >= 0.8F) {
			f2 = 0.0F;
		}

		GlStateManager.translate(0.0F, f2, 0.0F);
		float f3 = 1.0F - (float) Math.pow(f1, 27.0D);
		GlStateManager.translate(f3 * 0.6F, f3 * -0.5F, f3 * 0.0F);
		GlStateManager.rotate(f3 * 90.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(f3 * 10.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(f3 * 30.0F, 0.0F, 0.0F, 1.0F);
	}

	/**
	 * Performs transformations prior to the rendering of a held item in first person.
	 *
	 * @param equipProgress The progress of the animation to equip (raise from out of frame) while switching held items.
	 * @param swingProgress The progress of the arm swing animation.
	 */
	private void transformFirstPersonItem(float equipProgress, float swingProgress, boolean rod) {
		if (rod) {
			GlStateManager.translate(0.4F, -0.42F, -0.71999997F);
		} else {
			GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
		}
		GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
		GlStateManager.rotate(rod ? 50.0F : 45.0F, 0.0F, 1.0F, 0.0F);
		float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
		float f1 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
		GlStateManager.rotate(f * -20.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(f1 * -20.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.rotate(f1 * -80.0F, 1.0F, 0.0F, 0.0F);
		if (rod) {
			GlStateManager.scale(0.3F, 0.3F, 0.3F);
		} else {
			GlStateManager.scale(0.4F, 0.4F, 0.4F);
		}
	}

	private void func_178098_a(float p_178098_1_, AbstractClientPlayer clientPlayer) {
		GlStateManager.rotate(-18.0F, 0.0F, 0.0F, 1.0F);
		GlStateManager.rotate(-12.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-8.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.translate(-0.9F, 0.2F, 0.0F);
		float f = (float) this.itemToRender.getMaxItemUseDuration() - ((float) clientPlayer.getItemInUseCount() - p_178098_1_ + 1.0F);
		float f1 = f / 20.0F;
		f1 = (f1 * f1 + f1 * 2.0F) / 3.0F;

		if (f1 > 1.0F) {
			f1 = 1.0F;
		}

		if (f1 > 0.1F) {
			float f2 = MathHelper.sin((f - 0.1F) * 1.3F);
			float f3 = f1 - 0.1F;
			float f4 = f2 * f3;
			GlStateManager.translate(f4 * 0.0F, f4 * 0.01F, f4 * 0.0F);
		}

		GlStateManager.translate(f1 * 0.0F, f1 * 0.0F, f1 * 0.1F);
		GlStateManager.scale(1.0F, 1.0F, 1.0F + f1 * 0.2F);
	}

	private void func_178103_d() {
		GlStateManager.translate(-0.5F, 0.2F, 0.0F);
		GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
		GlStateManager.rotate(-80.0F, 1.0F, 0.0F, 0.0F);
		GlStateManager.rotate(60.0F, 0.0F, 1.0F, 0.0F);
	}

	/**
	 * Renders the active item in the player's hand when in first person mode. Args: partialTickTime
	 *
	 * @param partialTicks The amount of time passed during the current tick, ranging from 0 to 1.
	 */
	public void renderItemInFirstPerson(float partialTicks) {
		if (!Config.isShaders() || !Shaders.isSkipRenderHand()) {
			float f = 1.0F - (this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTicks);
			EntityPlayerSP player = this.mc.player;
			float swingProgress = player.getSwingProgress(partialTicks);
			float f2 = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
			float f3 = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;
			this.func_178101_a(f2, f3);
			this.func_178109_a(player);
			this.func_178110_a(player, partialTicks);
			GlStateManager.enableRescaleNormal();
			GlStateManager.pushMatrix();

			if (this.itemToRender != null) {
				boolean rod = (this.itemToRender.getItem() instanceof ItemFishingRod);
				if (this.itemToRender.getItem() instanceof ItemMap) {
					this.renderItemMap(player, f2, f, swingProgress);
				} else if (player.getItemInUseCount() > 0) {
					EnumAction enumaction = this.itemToRender.getItemUseAction();


					switch (enumaction) {
						case NONE:
							this.transformFirstPersonItem(f, swingProgress, rod);
							break;

						case EAT:
						case DRINK:
							this.func_178104_a(player, partialTicks);
							this.transformFirstPersonItem(f, swingProgress, rod);
							break;

						case BLOCK:
							this.transformFirstPersonItem(f, swingProgress, rod);
							this.func_178103_d();
							break;

						case BOW:
							this.transformFirstPersonItem(f, swingProgress, rod);
							this.func_178098_a(partialTicks, player);
					}
				} else {
					this.func_178105_d(swingProgress);
					this.transformFirstPersonItem(f, swingProgress, rod);
				}

				this.renderItem(player, this.itemToRender, ItemCameraTransforms.TransformType.FIRST_PERSON);
			} else if (!player.isInvisible()) {
				this.func_178095_a(player, f, swingProgress);
			}

			GlStateManager.popMatrix();
			GlStateManager.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
		}
	}

	/**
	 * Renders all the overlays that are in first person mode. Args: partialTickTime
	 */
	public void renderOverlays(float partialTicks) {
		GlStateManager.disableAlpha();

		if (this.mc.player.isEntityInsideOpaqueBlock()) {
			IBlockState iblockstate = this.mc.world.getBlockState(new BlockPos(this.mc.player));
			BlockPos blockpos = new BlockPos(this.mc.player);
			EntityPlayer entityplayer = this.mc.player;

			for (int i = 0; i < 8; ++i) {
				double d0 = entityplayer.posX + (double) (((float) ((i) % 2) - 0.5F) * entityplayer.width * 0.8F);
				double d1 = entityplayer.posY + (double) (((float) ((i >> 1) % 2) - 0.5F) * 0.1F);
				double d2 = entityplayer.posZ + (double) (((float) ((i >> 2) % 2) - 0.5F) * entityplayer.width * 0.8F);
				BlockPos blockpos1 = new BlockPos(d0, d1 + (double) entityplayer.getEyeHeight(), d2);
				IBlockState iblockstate1 = this.mc.world.getBlockState(blockpos1);

				if (iblockstate1.getBlock().isVisuallyOpaque()) {
					iblockstate = iblockstate1;
					blockpos = blockpos1;
				}
			}

			if (iblockstate.getBlock().getRenderType() != -1) {
				Object object = Reflector.getFieldValue(Reflector.RenderBlockOverlayEvent_OverlayType_BLOCK);

				if (!Reflector.callBoolean(Reflector.ForgeEventFactory_renderBlockOverlay, this.mc.player, partialTicks, object, iblockstate, blockpos)) {
					this.func_178108_a(this.mc.getBlockRendererDispatcher().getBlockModelShapes().getTexture(iblockstate));
				}
			}
		}

		if (!this.mc.player.isSpectator() && this.mc.player.isBurning() && !Reflector.callBoolean(Reflector.ForgeEventFactory_renderFireOverlay, this.mc.player, partialTicks)) {
			this.renderFireInFirstPerson();
		}

		GlStateManager.enableAlpha();
	}

	private void func_178108_a(TextureAtlasSprite p_178108_2_) {
		this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		GlStateManager.color(0.1F, 0.1F, 0.1F, 0.5F);
		GlStateManager.pushMatrix();
		float f6 = p_178108_2_.getMinU();
		float f7 = p_178108_2_.getMaxU();
		float f8 = p_178108_2_.getMinV();
		float f9 = p_178108_2_.getMaxV();
		worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
		worldrenderer.pos(-1.0D, -1.0D, -0.5D).tex(f7, f9).endVertex();
		worldrenderer.pos(1.0D, -1.0D, -0.5D).tex(f6, f9).endVertex();
		worldrenderer.pos(1.0D, 1.0D, -0.5D).tex(f6, f8).endVertex();
		worldrenderer.pos(-1.0D, 1.0D, -0.5D).tex(f7, f8).endVertex();
		tessellator.draw();
		GlStateManager.popMatrix();
		GlStateManager.colorAllMax();
	}

	private void renderFireInFirstPerson() {
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldrenderer = tessellator.getWorldRenderer();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 0.9F);
		GlStateManager.depthFunc(519);
		GlStateManager.depthMask(false);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
		float f = 1.0F;

		for (int i = 0; i < 2; ++i) {
			GlStateManager.pushMatrix();
			TextureAtlasSprite textureatlassprite = this.mc.getTextureMapBlocks().getAtlasSprite("minecraft:blocks/fire_layer_1");
			this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
			float f1 = textureatlassprite.getMinU();
			float f2 = textureatlassprite.getMaxU();
			float f3 = textureatlassprite.getMinV();
			float f4 = textureatlassprite.getMaxV();
			float f5 = (0.0F - f) / 2.0F;
			float f6 = f5 + f;
			float f7 = 0.0F - f / 2.0F;
			float f8 = f7 + f;
			float f9 = -0.5F;
			GlStateManager.translate((float) (-(i * 2 - 1)) * 0.24F, -0.3F, 0.0F);
			GlStateManager.rotate((float) (i * 2 - 1) * 10.0F, 0.0F, 1.0F, 0.0F);
			worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
			worldrenderer.setSprite(textureatlassprite);
			worldrenderer.pos(f5, f7, f9).tex(f2, f4).endVertex();
			worldrenderer.pos(f6, f7, f9).tex(f1, f4).endVertex();
			worldrenderer.pos(f6, f8, f9).tex(f1, f3).endVertex();
			worldrenderer.pos(f5, f8, f9).tex(f2, f3).endVertex();
			tessellator.draw();
			GlStateManager.popMatrix();
		}

		GlStateManager.colorAllMax();
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
		GlStateManager.depthFunc(515);
	}

	public void updateEquippedItem() {
		this.prevEquippedProgress = this.equippedProgress;
		EntityPlayer entityplayer = this.mc.player;
		ItemStack itemstack = entityplayer.inventory.getCurrentItem();
		boolean flag = false;

		if (this.itemToRender != null && itemstack != null) {
			if (!this.itemToRender.getIsItemStackEqual(itemstack)) {
				if (Reflector.ForgeItem_shouldCauseReequipAnimation.exists()) {
					boolean flag1 = Reflector.callBoolean(this.itemToRender.getItem(), Reflector.ForgeItem_shouldCauseReequipAnimation, this.itemToRender, itemstack, this.equippedItemSlot != entityplayer.inventory.currentItem);

					if (!flag1) {
						this.itemToRender = itemstack;
						this.equippedItemSlot = entityplayer.inventory.currentItem;
						return;
					}
				}

				flag = true;
			}
		} else flag = this.itemToRender != null || itemstack != null;

		float f2 = 0.4F;
		float f = flag ? 0.0F : 1.0F;
		float f1 = MathHelper.clamp_float(f - this.equippedProgress, -f2, f2);
		this.equippedProgress += f1;

		if (this.equippedProgress < 0.1F) {
			this.itemToRender = itemstack;
			this.equippedItemSlot = entityplayer.inventory.currentItem;

			if (Config.isShaders()) {
				Shaders.setItemToRenderMain(itemstack);
			}
		}
	}

	public void resetEquippedProgress() {
		this.equippedProgress = 0.0F;
	}

	public void attemptSwing() {
		if (this.mc.player.getItemInUseCount() > 0) {
			boolean mouseDown = (this.mc.gameSettings.keyBindAttack.isKeyDown() && this.mc.gameSettings.keyBindUseItem.isKeyDown());
			if (mouseDown && this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
				this.mc.player.swingItemOnlyClient();
			}
		}
	}

}