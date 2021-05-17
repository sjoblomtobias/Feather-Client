package com.murengezi.minecraft.client.renderer.entity;

import com.murengezi.minecraft.client.Minecraft;
import com.murengezi.minecraft.client.renderer.GlStateManager;
import com.murengezi.minecraft.client.renderer.Tessellator;
import com.murengezi.minecraft.client.renderer.WorldRenderer;
import com.murengezi.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.murengezi.minecraft.client.renderer.texture.TextureMap;
import com.murengezi.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.projectile.EntityFireball;
import com.murengezi.minecraft.init.Items;
import net.minecraft.util.ResourceLocation;

public class RenderFireball extends Render<EntityFireball> {

    private final float scale;

    public RenderFireball(RenderManager renderManager, float scale) {
        super(renderManager);
        this.scale = scale;
    }

    public void doRender(EntityFireball entity, double x, double y, double z, float entityYaw, float partialTicks) {
        GlStateManager.pushMatrix();
        this.bindEntityTexture(entity);
        GlStateManager.translate((float)x, (float)y, (float)z);
        GlStateManager.enableRescaleNormal();
        GlStateManager.scale(this.scale, this.scale, this.scale);
        TextureAtlasSprite textureatlassprite = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getParticleIcon(Items.fire_charge);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        float f = textureatlassprite.getMinU();
        float f1 = textureatlassprite.getMaxU();
        float f2 = textureatlassprite.getMinV();
        float f3 = textureatlassprite.getMaxV();
        GlStateManager.rotate(180.0F - this.renderManager.playerViewY, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(-this.renderManager.playerViewX, 1.0F, 0.0F, 0.0F);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX_NORMAL);
        worldrenderer.pos(-0.5D, -0.25D, 0.0D).tex(f, f3).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldrenderer.pos(0.5D, -0.25D, 0.0D).tex(f1, f3).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldrenderer.pos(0.5D, 0.75D, 0.0D).tex(f1, f2).normal(0.0F, 1.0F, 0.0F).endVertex();
        worldrenderer.pos(-0.5D, 0.75D, 0.0D).tex(f, f2).normal(0.0F, 1.0F, 0.0F).endVertex();
        tessellator.draw();
        GlStateManager.disableRescaleNormal();
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    protected ResourceLocation getEntityTexture(EntityFireball entity) {
        return TextureMap.locationBlocksTexture;
    }

}
