package com.murengezi.minecraft.client.particle;

import com.murengezi.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class EntityLavaFX extends EntityFX {

    private final float lavaParticleScale;

    protected EntityLavaFX(World world, double xCoord, double yCoord, double zCoord) {
        super(world, xCoord, yCoord, zCoord, 0.0D, 0.0D, 0.0D);
        this.motionX *= 0.800000011920929D;
        this.motionY *= 0.800000011920929D;
        this.motionZ *= 0.800000011920929D;
        this.motionY = this.rand.nextFloat() * 0.4F + 0.05F;
        this.particleRed = this.particleGreen = this.particleBlue = 1.0F;
        this.particleScale *= this.rand.nextFloat() * 2.0F + 0.2F;
        this.lavaParticleScale = this.particleScale;
        this.particleMaxAge = (int)(16.0D / (Math.random() * 0.8D + 0.2D));
        this.noClip = false;
        this.setParticleTextureIndex(49);
    }

    public int getBrightnessForRender(float partialTicks) {
        int i = super.getBrightnessForRender(partialTicks);
        int j = 240;
        int k = i >> 16 & 255;
        return j | k << 16;
    }

    public float getBrightness(float partialTicks) {
        return 1.0F;
    }

    public void renderParticle(WorldRenderer worldRenderer, Entity entityIn, float partialTicks, float p_180434_4_, float p_180434_5_, float p_180434_6_, float p_180434_7_, float p_180434_8_) {
        float f = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge;
        this.particleScale = this.lavaParticleScale * (1.0F - f * f);
        super.renderParticle(worldRenderer, entityIn, partialTicks, p_180434_4_, p_180434_5_, p_180434_6_, p_180434_7_, p_180434_8_);
    }

    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setDead();
        }

        float f = (float)this.particleAge / (float)this.particleMaxAge;

        if (this.rand.nextFloat() > f) {
            this.worldObj.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY, this.posZ, this.motionX, this.motionY, this.motionZ);
        }

        this.motionY -= 0.03D;
        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9990000128746033D;
        this.motionY *= 0.9990000128746033D;
        this.motionZ *= 0.9990000128746033D;

        if (this.onGround) {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }
    }

    public static class Factory implements IParticleFactory {
        public EntityFX getEntityFX(int particleID, World world, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... p_178902_15_) {
            return new EntityLavaFX(world, xCoord, yCoord, zCoord);
        }
    }

}
