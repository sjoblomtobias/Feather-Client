package com.murengezi.minecraft.client.renderer.entity.layers;

import com.murengezi.minecraft.client.renderer.GlStateManager;
import com.murengezi.minecraft.client.renderer.entity.RenderWolf;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.util.ResourceLocation;
import net.optifine.CustomColors;
import net.optifine.config.Config;

public class LayerWolfCollar implements LayerRenderer<EntityWolf> {

	private static final ResourceLocation WOLF_COLLAR = new ResourceLocation("textures/entity/wolf/wolf_collar.png");
	private final RenderWolf wolfRenderer;

	public LayerWolfCollar(RenderWolf wolfRenderer) {
		this.wolfRenderer = wolfRenderer;
	}

	public void doRenderLayer(EntityWolf entityLivingBase, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale) {
		if (entityLivingBase.isTamed() && !entityLivingBase.isInvisible()) {
			this.wolfRenderer.bindTexture(WOLF_COLLAR);
			EnumDyeColor enumdyecolor = EnumDyeColor.byMetadata(entityLivingBase.getCollarColor().getMetadata());
			float[] afloat = EntitySheep.func_175513_a(enumdyecolor);

			if (Config.isCustomColors()) {
				afloat = CustomColors.getWolfCollarColors(enumdyecolor, afloat);
			}

			GlStateManager.color(afloat[0], afloat[1], afloat[2]);
			this.wolfRenderer.getMainModel().render(entityLivingBase, p_177141_2_, p_177141_3_, p_177141_5_, p_177141_6_, p_177141_7_, scale);
		}
	}

	public boolean shouldCombineTextures() {
		return true;
	}

}