package com.murengezi.minecraft.client.particle;

import com.murengezi.minecraft.block.BlockLiquid;
import com.murengezi.minecraft.block.material.Material;
import com.murengezi.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class EntityDropParticleFX extends EntityFX {

    private final Material materialType;
    private int bobTimer;

    protected EntityDropParticleFX(World world, double xCoord, double yCoord, double zCoord, Material material) {
        super(world, xCoord, yCoord, zCoord, 0.0D, 0.0D, 0.0D);
        this.motionX = this.motionY = this.motionZ = 0.0D;

        if (material == Material.water) {
            this.particleRed = 0.0F;
            this.particleGreen = 0.0F;
            this.particleBlue = 1.0F;
        } else {
            this.particleRed = 1.0F;
            this.particleGreen = 0.0F;
            this.particleBlue = 0.0F;
        }

        this.setParticleTextureIndex(113);
        this.setSize(0.01F, 0.01F);
        this.particleGravity = 0.06F;
        this.materialType = material;
        this.bobTimer = 40;
        this.particleMaxAge = (int)(64.0D / (Math.random() * 0.8D + 0.2D));
        this.motionX = this.motionY = this.motionZ = 0.0D;
    }

    public int getBrightnessForRender(float partialTicks) {
        return this.materialType == Material.water ? super.getBrightnessForRender(partialTicks) : 257;
    }

    public float getBrightness(float partialTicks) {
        return this.materialType == Material.water ? super.getBrightness(partialTicks) : 1.0F;
    }

    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;

        if (this.materialType == Material.water) {
            this.particleRed = 0.2F;
            this.particleGreen = 0.3F;
            this.particleBlue = 1.0F;
        } else {
            this.particleRed = 1.0F;
            this.particleGreen = 16.0F / (float)(40 - this.bobTimer + 16);
            this.particleBlue = 4.0F / (float)(40 - this.bobTimer + 8);
        }

        this.motionY -= this.particleGravity;

        if (this.bobTimer-- > 0) {
            this.motionX *= 0.02D;
            this.motionY *= 0.02D;
            this.motionZ *= 0.02D;
            this.setParticleTextureIndex(113);
        } else {
            this.setParticleTextureIndex(112);
        }

        this.moveEntity(this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.9800000190734863D;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= 0.9800000190734863D;

        if (this.particleMaxAge-- <= 0) {
            this.setDead();
        }

        if (this.onGround) {
            if (this.materialType == Material.water) {
                this.setDead();
                this.worldObj.spawnParticle(EnumParticleTypes.WATER_SPLASH, this.posX, this.posY, this.posZ, 0.0D, 0.0D, 0.0D);
            } else {
                this.setParticleTextureIndex(114);
            }

            this.motionX *= 0.699999988079071D;
            this.motionZ *= 0.699999988079071D;
        }

        BlockPos blockpos = new BlockPos(this);
        IBlockState iblockstate = this.worldObj.getBlockState(blockpos);
        Material material = iblockstate.getBlock().getMaterial();

        if (material.isLiquid() || material.isSolid()) {
            double d0 = 0.0D;

            if (iblockstate.getBlock() instanceof BlockLiquid) {
                d0 = BlockLiquid.getLiquidHeightPercent(iblockstate.getValue(BlockLiquid.LEVEL).intValue());
            }

            double d1 = (double)(MathHelper.floor_double(this.posY) + 1) - d0;

            if (this.posY < d1) {
                this.setDead();
            }
        }
    }

    public static class LavaFactory implements IParticleFactory {
        public EntityFX getEntityFX(int particleID, World world, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... p_178902_15_) {
            return new EntityDropParticleFX(world, xCoord, yCoord, zCoord, Material.lava);
        }
    }

    public static class WaterFactory implements IParticleFactory {
        public EntityFX getEntityFX(int particleID, World world, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, int... p_178902_15_) {
            return new EntityDropParticleFX(world, xCoord, yCoord, zCoord, Material.water);
        }
    }

}