package com.murengezi.minecraft.client.particle;

import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityCrit2FX extends EntityFX {
    
    float field_174839_a;

    protected EntityCrit2FX(World world, double xCoord, double yCoord, double zCoord, double p_i46284_8_, double p_i46284_10_, double p_i46284_12_) {
        this(world, xCoord, yCoord, zCoord, p_i46284_8_, p_i46284_10_, p_i46284_12_, 1.0F);
    }

    protected EntityCrit2FX(World world, double xCoord, double yCoord, double zCoord, double p_i46285_8_, double p_i46285_10_, double p_i46285_12_, float p_i46285_14_) {
        super(world, xCoord, yCoord, zCoord, 0.0D, 0.0D, 0.0D);
        this.motionX *= 0.10000000149011612D;
        this.motionY *= 0.10000000149011612D;
        this.motionZ *= 0.10000000149011612D;
        this.motionX += p_i46285_8_ * 0.4D;
        this.motionY += p_i46285_10_ * 0.4D;
        this.motionZ += p_i46285_12_ * 0.4D;
        this.particleRed = this.particleGreen = this.particleBlue = (float)(Math.random() * 0.30000001192092896D + 0.6000000238418579D);
        this.particleScale *= 0.75F;
        this.particleScale *= p_i46285_14_;
        this.field_174839_a = this.particleScale;
        this.particleMaxAge = (int)(6.0D / (Math.random() * 0.8D + 0.6D));
        this.particleMaxAge = (int)((float)this.particleMaxAge * p_i46285_14_);
        this.noClip = false;
        this.setParticleTextureIndex(65);
        this.onUpdate();
    }

    public void renderParticle(WorldRenderer worldRenderer, Entity entityIn, float partialTicks, float p_180434_4_, float p_180434_5_, float p_180434_6_, float p_180434_7_, float p_180434_8_) {
        float f = ((float)this.particleAge + partialTicks) / (float)this.particleMaxAge * 32.0F;
        f = MathHelper.clamp_float(f, 0.0F, 1.0F);
        this.particleScale = this.field_174839_a * f;
        super.renderParticle(worldRenderer, entityIn, partialTicks, p_180434_4_, p_180434_5_, p_180434_6_, p_180434_7_, p_180434_8_);
    }

    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.particleAge++ >= this.particleMaxAge) {
            this.setDead();
        }

        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.particleGreen = (float)((double)this.particleGreen * 0.96D);
        this.particleBlue = (float)((double)this.particleBlue * 0.9D);
        this.motionX *= 0.699999988079071D;
        this.motionY *= 0.699999988079071D;
        this.motionZ *= 0.699999988079071D;
        this.motionY -= 0.019999999552965164D;

        if (this.onGround) {
            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }
    }

    public static class Factory implements IParticleFactory {
        public EntityFX getEntityFX(int particleID, World world, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... p_178902_15_) {
            return new EntityCrit2FX(world, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed);
        }
    }

    public static class MagicFactory implements IParticleFactory {
        public EntityFX getEntityFX(int particleID, World world, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... p_178902_15_) {
            EntityFX entityfx = new EntityCrit2FX(world, xCoord, yCoord, zCoord, xSpeed, ySpeed, zSpeed);
            entityfx.setRBGColorF(entityfx.getRedColorF() * 0.3F, entityfx.getGreenColorF() * 0.8F, entityfx.getBlueColorF());
            entityfx.nextTextureIndexX();
            return entityfx;
        }
    }
    
}