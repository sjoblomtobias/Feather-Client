package com.murengezi.minecraft.block;

import java.util.Random;
import com.murengezi.minecraft.block.material.Material;
import com.murengezi.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import com.murengezi.minecraft.init.Blocks;
import com.murengezi.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class BlockRedstoneOre extends Block {

    private final boolean isOn;

    public BlockRedstoneOre(boolean isOn) {
        super(Material.rock);

        if (isOn) {
            this.setTickRandomly(true);
        }

        this.isOn = isOn;
    }

    public int tickRate(World world) {
        return 30;
    }

    public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
        this.activate(world, pos);
        super.onBlockClicked(world, pos, player);
    }

    public void onEntityCollidedWithBlock(World world, BlockPos pos, Entity entity) {
        this.activate(world, pos);
        super.onEntityCollidedWithBlock(world, pos, entity);
    }

    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
        this.activate(world, pos);
        return super.onBlockActivated(world, pos, state, player, side, hitX, hitY, hitZ);
    }

    private void activate(World world, BlockPos pos) {
        this.spawnParticles(world, pos);

        if (this == Blocks.redstone_ore) {
            world.setBlockState(pos, Blocks.lit_redstone_ore.getDefaultState());
        }
    }

    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (this == Blocks.lit_redstone_ore) {
            world.setBlockState(pos, Blocks.redstone_ore.getDefaultState());
        }
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.redstone;
    }

    public int quantityDroppedWithBonus(int fortune, Random random) {
        return this.quantityDropped(random) + random.nextInt(fortune + 1);
    }

    public int quantityDropped(Random random) {
        return 4 + random.nextInt(2);
    }

    public void dropBlockAsItemWithChance(World world, BlockPos pos, IBlockState state, float chance, int fortune) {
        super.dropBlockAsItemWithChance(world, pos, state, chance, fortune);

        if (this.getItemDropped(state, world.rand, fortune) != Item.getItemFromBlock(this)) {
            int i = 1 + world.rand.nextInt(5);
            this.dropXpOnBlockBreak(world, pos, i);
        }
    }

    public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (this.isOn) {
            this.spawnParticles(world, pos);
        }
    }

    private void spawnParticles(World world, BlockPos pos) {
        Random random = world.rand;
        double d0 = 0.0625D;

        for (int i = 0; i < 6; ++i) {
            double d1 = (float)pos.getX() + random.nextFloat();
            double d2 = (float)pos.getY() + random.nextFloat();
            double d3 = (float)pos.getZ() + random.nextFloat();

            if (i == 0 && !world.getBlockState(pos.up()).getBlock().isOpaqueCube()) {
                d2 = (double)pos.getY() + d0 + 1.0D;
            }

            if (i == 1 && !world.getBlockState(pos.down()).getBlock().isOpaqueCube()) {
                d2 = (double)pos.getY() - d0;
            }

            if (i == 2 && !world.getBlockState(pos.south()).getBlock().isOpaqueCube()) {
                d3 = (double)pos.getZ() + d0 + 1.0D;
            }

            if (i == 3 && !world.getBlockState(pos.north()).getBlock().isOpaqueCube()) {
                d3 = (double)pos.getZ() - d0;
            }

            if (i == 4 && !world.getBlockState(pos.east()).getBlock().isOpaqueCube()) {
                d1 = (double)pos.getX() + d0 + 1.0D;
            }

            if (i == 5 && !world.getBlockState(pos.west()).getBlock().isOpaqueCube()) {
                d1 = (double)pos.getX() - d0;
            }

            if (d1 < (double)pos.getX() || d1 > (double)(pos.getX() + 1) || d2 < 0.0D || d2 > (double)(pos.getY() + 1) || d3 < (double)pos.getZ() || d3 > (double)(pos.getZ() + 1)) {
                world.spawnParticle(EnumParticleTypes.REDSTONE, d1, d2, d3, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    protected ItemStack createStackedBlock(IBlockState state) {
        return new ItemStack(Blocks.redstone_ore);
    }

}
