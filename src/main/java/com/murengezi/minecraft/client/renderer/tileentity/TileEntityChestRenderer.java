package com.murengezi.minecraft.client.renderer.tileentity;

import com.murengezi.minecraft.block.Block;
import com.murengezi.minecraft.block.BlockChest;
import com.murengezi.minecraft.client.model.ModelChest;
import com.murengezi.minecraft.client.model.ModelLargeChest;
import com.murengezi.minecraft.client.renderer.GlStateManager;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.ResourceLocation;

import java.util.Calendar;

public class TileEntityChestRenderer extends TileEntitySpecialRenderer<TileEntityChest> {

	private static final ResourceLocation textureTrappedDouble = new ResourceLocation("textures/entity/chest/trapped_double.png"), textureChristmasDouble = new ResourceLocation("textures/entity/chest/christmas_double.png"), textureNormalDouble = new ResourceLocation("textures/entity/chest/normal_double.png"), textureTrapped = new ResourceLocation("textures/entity/chest/trapped.png"), textureChristmas = new ResourceLocation("textures/entity/chest/christmas.png"), textureNormal = new ResourceLocation("textures/entity/chest/normal.png");
	private final ModelChest simpleChest = new ModelChest(), largeChest = new ModelLargeChest();
	private boolean isChristmas;

	public TileEntityChestRenderer() {
		Calendar calendar = Calendar.getInstance();

		if (calendar.get(Calendar.MONTH) + 1 == 12 && calendar.get(Calendar.DATE) >= 24 && calendar.get(Calendar.DATE) <= 26) {
			this.isChristmas = true;
		}
	}

	public void renderTileEntityAt(TileEntityChest te, double x, double y, double z, float partialTicks, int destroyStage) {
		GlStateManager.enableDepth();
		GlStateManager.depthFunc(515);
		GlStateManager.depthMask(true);
		int i;

		if (!te.hasWorldObj()) {
			i = 0;
		} else {
			Block block = te.getBlockType();
			i = te.getBlockMetadata();

			if (block instanceof BlockChest && i == 0) {
				((BlockChest) block).checkForSurroundingChests(te.getWorld(), te.getPos(), te.getWorld().getBlockState(te.getPos()));
				i = te.getBlockMetadata();
			}

			te.checkForAdjacentChests();
		}

		if (te.adjacentChestZNeg == null && te.adjacentChestXNeg == null) {
			ModelChest modelchest;

			if (te.adjacentChestXPos == null && te.adjacentChestZPos == null) {
				modelchest = this.simpleChest;

				if (destroyStage >= 0) {
					this.bindTexture(DESTROY_STAGES[destroyStage]);
					GlStateManager.matrixMode(5890);
					GlStateManager.pushMatrix();
					GlStateManager.scale(4.0F, 4.0F, 1.0F);
					GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
					GlStateManager.matrixMode(5888);
				} else if (this.isChristmas) {
					this.bindTexture(textureChristmas);
				} else if (te.getChestType() == 1) {
					this.bindTexture(textureTrapped);
				} else {
					this.bindTexture(textureNormal);
				}
			} else {
				modelchest = this.largeChest;

				if (destroyStage >= 0) {
					this.bindTexture(DESTROY_STAGES[destroyStage]);
					GlStateManager.matrixMode(5890);
					GlStateManager.pushMatrix();
					GlStateManager.scale(8.0F, 4.0F, 1.0F);
					GlStateManager.translate(0.0625F, 0.0625F, 0.0625F);
					GlStateManager.matrixMode(5888);
				} else if (this.isChristmas) {
					this.bindTexture(textureChristmasDouble);
				} else if (te.getChestType() == 1) {
					this.bindTexture(textureTrappedDouble);
				} else {
					this.bindTexture(textureNormalDouble);
				}
			}

			GlStateManager.pushMatrix();
			GlStateManager.enableRescaleNormal();

			if (destroyStage < 0) {
				GlStateManager.colorAllMax();
			}

			GlStateManager.translate((float) x, (float) y + 1.0F, (float) z + 1.0F);
			GlStateManager.scale(1.0F, -1.0F, -1.0F);
			GlStateManager.translate(0.5F, 0.5F, 0.5F);
			int j = 0;

			if (i == 2) {
				j = 180;
			}

			if (i == 3) {
				j = 0;
			}

			if (i == 4) {
				j = 90;
			}

			if (i == 5) {
				j = -90;
			}

			if (i == 2 && te.adjacentChestXPos != null) {
				GlStateManager.translate(1.0F, 0.0F, 0.0F);
			}

			if (i == 5 && te.adjacentChestZPos != null) {
				GlStateManager.translate(0.0F, 0.0F, -1.0F);
			}

			GlStateManager.rotate((float) j, 0.0F, 1.0F, 0.0F);
			GlStateManager.translate(-0.5F, -0.5F, -0.5F);
			float f = te.prevLidAngle + (te.lidAngle - te.prevLidAngle) * partialTicks;

			if (te.adjacentChestZNeg != null) {
				float f1 = te.adjacentChestZNeg.prevLidAngle + (te.adjacentChestZNeg.lidAngle - te.adjacentChestZNeg.prevLidAngle) * partialTicks;

				if (f1 > f) {
					f = f1;
				}
			}

			if (te.adjacentChestXNeg != null) {
				float f2 = te.adjacentChestXNeg.prevLidAngle + (te.adjacentChestXNeg.lidAngle - te.adjacentChestXNeg.prevLidAngle) * partialTicks;

				if (f2 > f) {
					f = f2;
				}
			}

			f = 1.0F - f;
			f = 1.0F - f * f * f;
			modelchest.chestLid.rotateAngleX = -(f * (float) Math.PI / 2.0F);
			modelchest.renderAll();
			GlStateManager.disableRescaleNormal();
			GlStateManager.popMatrix();
			GlStateManager.colorAllMax();

			if (destroyStage >= 0) {
				GlStateManager.matrixMode(5890);
				GlStateManager.popMatrix();
				GlStateManager.matrixMode(5888);
			}
		}
	}

}