package com.murengezi.minecraft.client.resources.model;

import com.murengezi.minecraft.client.renderer.block.model.BakedQuad;
import com.murengezi.minecraft.client.renderer.block.model.ItemCameraTransforms;
import com.murengezi.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.EnumFacing;

import java.util.List;

public interface IBakedModel {

	List<BakedQuad> getFaceQuads(EnumFacing p_177551_1_);

	List<BakedQuad> getGeneralQuads();

	boolean isAmbientOcclusion();

	boolean isGui3d();

	boolean isBuiltInRenderer();

	TextureAtlasSprite getTexture();

	ItemCameraTransforms getItemCameraTransforms();

}