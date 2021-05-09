package com.murengezi.minecraft.block;

import java.util.Random;
import com.murengezi.minecraft.block.material.Material;
import com.murengezi.minecraft.block.properties.PropertyDirection;
import com.murengezi.minecraft.block.state.BlockState;
import com.murengezi.minecraft.block.state.IBlockState;
import com.murengezi.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class BlockTorch extends Block {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", p_apply_1_ -> p_apply_1_ != EnumFacing.DOWN);

    protected BlockTorch() {
        super(Material.circuits);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.UP));
        this.setTickRandomly(true);
        this.setCreativeTab(CreativeTabs.tabDecorations);
    }

    public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state) {
        return null;
    }

    public boolean isOpaqueCube() {
        return false;
    }

    public boolean isFullCube() {
        return false;
    }

    private boolean canPlaceOn(World world, BlockPos pos) {
        if (World.doesBlockHaveSolidTopSurface(world, pos)) {
            return true;
        } else {
            Block block = world.getBlockState(pos).getBlock();
            return block instanceof BlockFence || block == Blocks.glass || block == Blocks.cobblestone_wall || block == Blocks.stained_glass;
        }
    }

    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        for (EnumFacing enumfacing : FACING.getAllowedValues()) {
            if (this.canPlaceAt(world, pos, enumfacing)) {
                return true;
            }
        }

        return false;
    }

    private boolean canPlaceAt(World world, BlockPos pos, EnumFacing facing) {
        BlockPos blockpos = pos.offset(facing.getOpposite());
        boolean flag = facing.getAxis().isHorizontal();
        return flag && world.isBlockNormalCube(blockpos, true) || facing.equals(EnumFacing.UP) && this.canPlaceOn(world, blockpos);
    }

    public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        if (this.canPlaceAt(world, pos, facing)) {
            return this.getDefaultState().withProperty(FACING, facing);
        } else {
            for (EnumFacing enumfacing : EnumFacing.Plane.HORIZONTAL) {
                if (world.isBlockNormalCube(pos.offset(enumfacing.getOpposite()), true)) {
                    return this.getDefaultState().withProperty(FACING, enumfacing);
                }
            }

            return this.getDefaultState();
        }
    }

    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        this.checkForDrop(world, pos, state);
    }

    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        this.onNeighborChangeInternal(world, pos, state);
    }

    protected boolean onNeighborChangeInternal(World world, BlockPos pos, IBlockState state) {
        if (!this.checkForDrop(world, pos, state)) {
            return true;
        } else {
            EnumFacing facing = state.getValue(FACING);
            EnumFacing.Axis axis = facing.getAxis();
            EnumFacing facingOpposite = facing.getOpposite();
            boolean flag = false;

            if (axis.isHorizontal() && !world.isBlockNormalCube(pos.offset(facingOpposite), true)) {
                flag = true;
            } else if (axis.isVertical() && !this.canPlaceOn(world, pos.offset(facingOpposite))) {
                flag = true;
            }

            if (flag) {
                this.dropBlockAsItem(world, pos, state, 0);
                world.setBlockToAir(pos);
                return true;
            } else {
                return false;
            }
        }
    }

    protected boolean checkForDrop(World world, BlockPos pos, IBlockState state) {
        if (state.getBlock() == this && this.canPlaceAt(world, pos, state.getValue(FACING))) {
            return true;
        } else {
            if (world.getBlockState(pos).getBlock() == this) {
                this.dropBlockAsItem(world, pos, state, 0);
                world.setBlockToAir(pos);
            }

            return false;
        }
    }

    public MovingObjectPosition collisionRayTrace(World world, BlockPos pos, Vec3 start, Vec3 end) {
        EnumFacing enumfacing = world.getBlockState(pos).getValue(FACING);
        float f = 0.15F;

        if (enumfacing == EnumFacing.EAST) {
            this.setBlockBounds(0.0F, 0.2F, 0.5F - f, f * 2.0F, 0.8F, 0.5F + f);
        } else if (enumfacing == EnumFacing.WEST) {
            this.setBlockBounds(1.0F - f * 2.0F, 0.2F, 0.5F - f, 1.0F, 0.8F, 0.5F + f);
        } else if (enumfacing == EnumFacing.SOUTH) {
            this.setBlockBounds(0.5F - f, 0.2F, 0.0F, 0.5F + f, 0.8F, f * 2.0F);
        } else if (enumfacing == EnumFacing.NORTH) {
            this.setBlockBounds(0.5F - f, 0.2F, 1.0F - f * 2.0F, 0.5F + f, 0.8F, 1.0F);
        } else {
            f = 0.1F;
            this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.6F, 0.5F + f);
        }

        return super.collisionRayTrace(world, pos, start, end);
    }

    public void randomDisplayTick(World world, BlockPos pos, IBlockState state, Random rand) {
        EnumFacing enumfacing = state.getValue(FACING);
        double d0 = (double)pos.getX() + 0.5D;
        double d1 = (double)pos.getY() + 0.7D;
        double d2 = (double)pos.getZ() + 0.5D;
        double d3 = 0.22D;
        double d4 = 0.27D;

        if (enumfacing.getAxis().isHorizontal()) {
            EnumFacing enumfacing1 = enumfacing.getOpposite();
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0 + d4 * (double)enumfacing1.getFrontOffsetX(), d1 + d3, d2 + d4 * (double)enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
            world.spawnParticle(EnumParticleTypes.FLAME, d0 + d4 * (double)enumfacing1.getFrontOffsetX(), d1 + d3, d2 + d4 * (double)enumfacing1.getFrontOffsetZ(), 0.0D, 0.0D, 0.0D);
        } else {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, d0, d1, d2, 0.0D, 0.0D, 0.0D);
            world.spawnParticle(EnumParticleTypes.FLAME, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }

    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT;
    }

    public IBlockState getStateFromMeta(int meta) {
        IBlockState iblockstate = this.getDefaultState();

        switch (meta) {
            case 1:
                iblockstate = iblockstate.withProperty(FACING, EnumFacing.EAST);
                break;
            case 2:
                iblockstate = iblockstate.withProperty(FACING, EnumFacing.WEST);
                break;
            case 3:
                iblockstate = iblockstate.withProperty(FACING, EnumFacing.SOUTH);
                break;
            case 4:
                iblockstate = iblockstate.withProperty(FACING, EnumFacing.NORTH);
                break;
            case 5:
            default:
                iblockstate = iblockstate.withProperty(FACING, EnumFacing.UP);
        }

        return iblockstate;
    }

    public int getMetaFromState(IBlockState state) {
        int i = 0;

        switch (state.getValue(FACING)) {
            case EAST:
                i = i | 1;
                break;
            case WEST:
                i = i | 2;
                break;
            case SOUTH:
                i = i | 3;
                break;
            case NORTH:
                i = i | 4;
                break;
            case DOWN:
            case UP:
            default:
                i = i | 5;
        }

        return i;
    }

    protected BlockState createBlockState() {
        return new BlockState(this, FACING);
    }

}