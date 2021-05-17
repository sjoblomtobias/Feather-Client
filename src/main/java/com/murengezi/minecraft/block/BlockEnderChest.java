package com.murengezi.minecraft.block;

import java.util.Random;
import com.murengezi.minecraft.block.material.Material;
import com.murengezi.minecraft.block.properties.PropertyDirection;
import com.murengezi.minecraft.block.state.BlockState;
import com.murengezi.minecraft.block.state.IBlockState;
import com.murengezi.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import com.murengezi.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnderChest;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class BlockEnderChest extends BlockContainer {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);

    protected BlockEnderChest() {
        super(Material.rock);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        this.setCreativeTab(CreativeTabs.tabDecorations);
        this.setBlockBounds(0.0625F, 0.0F, 0.0625F, 0.9375F, 0.875F, 0.9375F);
    }

    public boolean isOpaqueCube() {
        return false;
    }

    public boolean isFullCube() {
        return false;
    }

    public int getRenderType() {
        return 2;
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(Blocks.obsidian);
    }

    public int quantityDropped(Random random) {
        return 8;
    }

    protected boolean canSilkHarvest() {
        return true;
    }

    public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        world.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()), 2);
    }

    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
        InventoryEnderChest inventoryenderchest = player.getInventoryEnderChest();
        TileEntity tileentity = world.getTileEntity(pos);

        if (inventoryenderchest != null && tileentity instanceof TileEntityEnderChest) {
            if (world.getBlockState(pos.up()).getBlock().isNormalCube()) {
                return true;
            } else if (world.isRemote) {
                return true;
            } else {
                inventoryenderchest.setChestTileEntity((TileEntityEnderChest)tileentity);
                player.displayGUIChest(inventoryenderchest);
                player.triggerAchievement(StatList.field_181738_V);
                return true;
            }
        } else {
            return true;
        }
    }

    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityEnderChest();
    }

    public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random rand) {
        for (int i = 0; i < 3; ++i) {
            int j = rand.nextInt(2) * 2 - 1;
            int k = rand.nextInt(2) * 2 - 1;
            double d0 = (double)pos.getX() + 0.5D + 0.25D * (double)j;
            double d1 = (float)pos.getY() + rand.nextFloat();
            double d2 = (double)pos.getZ() + 0.5D + 0.25D * (double)k;
            double d3 = rand.nextFloat() * (float)j;
            double d4 = ((double)rand.nextFloat() - 0.5D) * 0.125D;
            double d5 = rand.nextFloat() * (float)k;
            world.spawnParticle(EnumParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
        }
    }

    public IBlockState getStateFromMeta(int meta) {
        EnumFacing enumfacing = EnumFacing.getFront(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing);
    }

    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    protected BlockState createBlockState() {
        return new BlockState(this, FACING);
    }

}
