package com.murengezi.minecraft.client.renderer.texture;

import com.murengezi.minecraft.client.resources.IResource;
import com.murengezi.minecraft.client.resources.IResourceManager;
import com.murengezi.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.util.ResourceLocation;
import net.optifine.EmissiveTextures;
import net.optifine.config.Config;
import net.optifine.shaders.ShadersTex;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class SimpleTexture extends AbstractTexture {

	private static final Logger logger = LogManager.getLogger();
	protected final ResourceLocation textureLocation;
	public ResourceLocation locationEmissive;
	public boolean isEmissive;

	public SimpleTexture(ResourceLocation textureResourceLocation) {
		this.textureLocation = textureResourceLocation;
	}

	public void loadTexture(IResourceManager resourceManager) throws IOException {
		this.deleteGlTexture();
		InputStream inputstream = null;

		try {
			IResource iresource = resourceManager.getResource(this.textureLocation);
			inputstream = iresource.getInputStream();
			BufferedImage bufferedimage = TextureUtil.readBufferedImage(inputstream);
			boolean flag = false;
			boolean flag1 = false;

			if (iresource.hasMetadata()) {
				try {
					TextureMetadataSection texturemetadatasection = iresource.getMetadata("texture");

					if (texturemetadatasection != null) {
						flag = texturemetadatasection.getTextureBlur();
						flag1 = texturemetadatasection.getTextureClamp();
					}
				} catch (RuntimeException runtimeexception) {
					logger.warn("Failed reading metadata of: " + this.textureLocation, runtimeexception);
				}
			}

			if (Config.isShaders()) {
				ShadersTex.loadSimpleTexture(this.getGlTextureId(), bufferedimage, flag, flag1, resourceManager, this.textureLocation, this.getMultiTexID());
			} else {
				TextureUtil.uploadTextureImageAllocate(this.getGlTextureId(), bufferedimage, flag, flag1);
			}

			if (EmissiveTextures.isActive()) {
				EmissiveTextures.loadTexture(this.textureLocation, this);
			}
		} finally {
			if (inputstream != null) {
				inputstream.close();
			}
		}
	}

}