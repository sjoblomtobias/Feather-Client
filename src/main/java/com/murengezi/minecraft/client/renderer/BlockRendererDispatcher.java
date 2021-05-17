package com.murengezi.minecraft.client.renderer;

import com.murengezi.minecraft.block.Block;
import com.murengezi.minecraft.block.state.IBlockState;
import com.murengezi.minecraft.crash.CrashReport;
import com.murengezi.minecraft.crash.CrashReportCategory;
import com.murengezi.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.murengezi.minecraft.client.resources.IResourceManager;
import com.murengezi.minecraft.client.resources.IResourceManagerReloadListener;
import com.murengezi.minecraft.client.resources.model.IBakedModel;
import com.murengezi.minecraft.client.resources.model.SimpleBakedModel;
import com.murengezi.minecraft.client.resources.model.WeightedBakedModel;
import com.murengezi.minecraft.client.settings.GameSettings;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ReportedException;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;

public class BlockRendererDispatcher implements IResourceManagerReloadListener {

	private final BlockModelShapes blockModelShapes;
	private final GameSettings gameSettings;
	private final BlockModelRenderer blockModelRenderer = new BlockModelRenderer();
	private final ChestRenderer chestRenderer = new ChestRenderer();
	private final BlockFluidRenderer fluidRenderer = new BlockFluidRenderer();

	public BlockRendererDispatcher(BlockModelShapes blockModelShapesIn, GameSettings gameSettingsIn) {
		this.blockModelShapes = blockModelShapesIn;
		this.gameSettings = gameSettingsIn;
	}

	public BlockModelShapes getBlockModelShapes() {
		return this.blockModelShapes;
	}

	public void renderBlockDamage(IBlockState state, BlockPos pos, TextureAtlasSprite texture, IBlockAccess blockAccess) {
		Block block = state.getBlock();
		int i = block.getRenderType();

		if (i == 3) {
			state = block.getActualState(state, blockAccess, pos);
			IBakedModel ibakedmodel = this.blockModelShapes.getModelForState(state);
			IBakedModel ibakedmodel1 = (new SimpleBakedModel.Builder(ibakedmodel, texture)).makeBakedModel();
			this.blockModelRenderer.renderModel(blockAccess, ibakedmodel1, state, pos, Tessellator.getInstance().getWorldRenderer());
		}
	}

	public boolean renderBlock(IBlockState state, BlockPos pos, IBlockAccess blockAccess, WorldRenderer worldRendererIn) {
		try {
			int i = state.getBlock().getRenderType();

			if (i == -1) {
				return false;
			} else {
				switch (i) {
					case 1:
						return this.fluidRenderer.renderFluid(blockAccess, state, pos, worldRendererIn);
					case 3:
						IBakedModel ibakedmodel = this.getModelFromBlockState(state, blockAccess, pos);
						return this.blockModelRenderer.renderModel(blockAccess, ibakedmodel, state, pos, worldRendererIn);
					default:
						return false;
				}
			}
		} catch (Throwable throwable) {
			CrashReport crashreport = CrashReport.makeCrashReport(throwable, "Tesselating block in world");
			CrashReportCategory crashreportcategory = crashreport.makeCategory("Block being tesselated");
			CrashReportCategory.addBlockInfo(crashreportcategory, pos, state.getBlock(), state.getBlock().getMetaFromState(state));
			throw new ReportedException(crashreport);
		}
	}

	public BlockModelRenderer getBlockModelRenderer() {
		return this.blockModelRenderer;
	}

	private IBakedModel getBakedModel(IBlockState state) {
		return this.blockModelShapes.getModelForState(state);
	}

	public IBakedModel getModelFromBlockState(IBlockState state, IBlockAccess world, BlockPos pos) {
		Block block = state.getBlock();

		if (world.getWorldType() != WorldType.DEBUG_WORLD) {
			try {
				state = block.getActualState(state, world, pos);
			} catch (Exception ignored) {
			}
		}

		IBakedModel ibakedmodel = this.blockModelShapes.getModelForState(state);

		if (pos != null && this.gameSettings.allowBlockAlternatives && ibakedmodel instanceof WeightedBakedModel) {
			ibakedmodel = ((WeightedBakedModel) ibakedmodel).getAlternativeModel(MathHelper.getPositionRandom(pos));
		}

		return ibakedmodel;
	}

	public void renderBlockBrightness(IBlockState state, float brightness) {
		int i = state.getBlock().getRenderType();

		if (i != -1) {
			switch (i) {
				case 1:
				default:
					break;
				case 2:
					this.chestRenderer.renderChestBrightness(state.getBlock(), brightness);
					break;
				case 3:
					IBakedModel ibakedmodel = this.getBakedModel(state);
					this.blockModelRenderer.renderModelBrightness(ibakedmodel, state, brightness, true);
			}
		}
	}

	public boolean isRenderTypeChest(Block block) {
		if (block == null) {
			return false;
		} else {
			int i = block.getRenderType();
			return i == 2;
		}
	}

	public void onResourceManagerReload(IResourceManager resourceManager) {
		this.fluidRenderer.initAtlasSprites();
	}

}