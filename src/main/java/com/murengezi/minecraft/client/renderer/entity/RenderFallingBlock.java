package com.murengezi.minecraft.client.renderer.entity;

import com.murengezi.minecraft.block.Block;
import com.murengezi.minecraft.block.state.IBlockState;
import com.murengezi.minecraft.client.Minecraft;
import com.murengezi.minecraft.client.renderer.BlockRendererDispatcher;
import com.murengezi.minecraft.client.renderer.GlStateManager;
import com.murengezi.minecraft.client.renderer.Tessellator;
import com.murengezi.minecraft.client.renderer.WorldRenderer;
import com.murengezi.minecraft.client.renderer.texture.TextureMap;
import com.murengezi.minecraft.client.renderer.vertex.DefaultVertexFormats;
import com.murengezi.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class RenderFallingBlock extends Render<EntityFallingBlock> {

	public RenderFallingBlock(RenderManager renderManager) {
		super(renderManager);
		this.shadowSize = 0.5F;
	}

	public void doRender(EntityFallingBlock entity, double x, double y, double z, float entityYaw, float partialTicks) {
		if (entity.getBlock() != null) {
			this.bindTexture(TextureMap.locationBlocksTexture);
			IBlockState iblockstate = entity.getBlock();
			Block block = iblockstate.getBlock();
			BlockPos blockpos = new BlockPos(entity);
			World world = entity.getWorldObj();

			if (iblockstate != world.getBlockState(blockpos) && block.getRenderType() != -1) {
				if (block.getRenderType() == 3) {
					GlStateManager.pushMatrix();
					GlStateManager.translate((float) x, (float) y, (float) z);
					GlStateManager.disableLighting();
					Tessellator tessellator = Tessellator.getInstance();
					WorldRenderer worldrenderer = tessellator.getWorldRenderer();
					worldrenderer.begin(7, DefaultVertexFormats.BLOCK);
					int i = blockpos.getX();
					int j = blockpos.getY();
					int k = blockpos.getZ();
					worldrenderer.setTranslation((float) (-i) - 0.5F, -j, (float) (-k) - 0.5F);
					BlockRendererDispatcher blockrendererdispatcher = Minecraft.getMinecraft().getBlockRendererDispatcher();
					IBakedModel ibakedmodel = blockrendererdispatcher.getModelFromBlockState(iblockstate, world, null);
					blockrendererdispatcher.getBlockModelRenderer().renderModel(world, ibakedmodel, iblockstate, blockpos, worldrenderer, false);
					worldrenderer.setTranslation(0.0D, 0.0D, 0.0D);
					tessellator.draw();
					GlStateManager.enableLightning();
					GlStateManager.popMatrix();
					super.doRender(entity, x, y, z, entityYaw, partialTicks);
				}
			}
		}
	}

	protected ResourceLocation getEntityTexture(EntityFallingBlock entity) {
		return TextureMap.locationBlocksTexture;
	}

}