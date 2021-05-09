package com.murengezi.minecraft.block;

import java.util.List;
import com.murengezi.minecraft.block.material.MapColor;
import com.murengezi.minecraft.block.material.Material;
import com.murengezi.minecraft.block.properties.PropertyBool;
import com.murengezi.minecraft.block.properties.PropertyDirection;
import com.murengezi.minecraft.block.state.BlockState;
import com.murengezi.minecraft.block.state.IBlockState;
import com.murengezi.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityHopper;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockHopper extends BlockContainer {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", p_apply_1_ -> p_apply_1_ != EnumFacing.UP);
    public static final PropertyBool ENABLED = PropertyBool.create("enabled");

    public BlockHopper() {
        super(Material.iron, MapColor.stoneColor);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.DOWN).withProperty(ENABLED, true));
        this.setCreativeTab(CreativeTabs.tabRedstone);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    public void setBlockBoundsBasedOnState(IBlockAccess world, BlockPos pos) {
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    public void addCollisionBoxesToList(World world, BlockPos pos, IBlockState state, AxisAlignedBB mask, List<AxisAlignedBB> list, Entity collidingEntity) {
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.625F, 1.0F);
        super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
        float f = 0.125F;
        this.setBlockBounds(0.0F, 0.0F, 0.0F, f, 1.0F, 1.0F);
        super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, f);
        super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
        this.setBlockBounds(1.0F - f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
        super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
        this.setBlockBounds(0.0F, 0.0F, 1.0F - f, 1.0F, 1.0F, 1.0F);
        super.addCollisionBoxesToList(world, pos, state, mask, list, collidingEntity);
        this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    }

    public IBlockState onBlockPlaced(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing enumfacing = facing.getOpposite();

        if (enumfacing == EnumFacing.UP) {
            enumfacing = EnumFacing.DOWN;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing).withProperty(ENABLED, true);
    }

    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityHopper();
    }

    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, placer, stack);

        if (stack.hasDisplayName()) {
            TileEntity tileentity = world.getTileEntity(pos);

            if (tileentity instanceof TileEntityHopper) {
                ((TileEntityHopper)tileentity).setCustomName(stack.getDisplayName());
            }
        }
    }

    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        this.updateState(world, pos, state);
    }

    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            TileEntity tileentity = world.getTileEntity(pos);

            if (tileentity instanceof TileEntityHopper) {
                player.displayGUIChest((TileEntityHopper) tileentity);
                player.triggerAchievement(StatList.field_181732_P);
            }

        }
        return true;
    }

    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block neighborBlock) {
        this.updateState(world, pos, state);
    }

    private void updateState(World world, BlockPos pos, IBlockState state) {
        boolean flag = !world.isBlockPowered(pos);

        if (flag != state.getValue(ENABLED)) {
            world.setBlockState(pos, state.withProperty(ENABLED, flag), 4);
        }
    }

    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tileentity = world.getTileEntity(pos);

        if (tileentity instanceof TileEntityHopper) {
            InventoryHelper.dropInventoryItems(world, pos, (TileEntityHopper)tileentity);
            world.updateComparatorOutputLevel(pos, this);
        }

        super.breakBlock(world, pos, state);
    }

    public int getRenderType() {
        return 3;
    }

    public boolean isFullCube() {
        return false;
    }

    public boolean isOpaqueCube() {
        return false;
    }

    public boolean shouldSideBeRendered(IBlockAccess world, BlockPos pos, EnumFacing side)
    {
        return true;
    }

    public static EnumFacing getFacing(int meta) {
        return EnumFacing.getFront(meta & 7);
    }

    public static boolean isEnabled(int meta) {
        return (meta & 8) != 8;
    }

    public boolean hasComparatorInputOverride() {
        return true;
    }

    public int getComparatorInputOverride(World world, BlockPos pos) {
        return Container.calcRedstone(world.getTileEntity(pos));
    }

    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT_MIPPED;
    }

    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, getFacing(meta)).withProperty(ENABLED, isEnabled(meta));
    }

    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i = i | state.getValue(FACING).getIndex();

        if (!state.getValue(ENABLED)) {
            i |= 8;
        }

        return i;
    }

    protected BlockState createBlockState() {
        return new BlockState(this, FACING, ENABLED);
    }

}