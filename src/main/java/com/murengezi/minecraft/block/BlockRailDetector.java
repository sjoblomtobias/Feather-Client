package com.murengezi.minecraft.block;

import com.google.common.base.Predicate;
import java.util.List;
import java.util.Random;
import com.murengezi.minecraft.block.properties.IProperty;
import com.murengezi.minecraft.block.properties.PropertyBool;
import com.murengezi.minecraft.block.properties.PropertyEnum;
import com.murengezi.minecraft.block.state.BlockState;
import com.murengezi.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityMinecartCommandBlock;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockRailDetector extends BlockRailBase {

    public static final PropertyEnum<BlockRailBase.EnumRailDirection> SHAPE = PropertyEnum.create("shape", BlockRailBase.EnumRailDirection.class, p_apply_1_ -> p_apply_1_ != EnumRailDirection.NORTH_EAST && p_apply_1_ != EnumRailDirection.NORTH_WEST && p_apply_1_ != EnumRailDirection.SOUTH_EAST && p_apply_1_ != EnumRailDirection.SOUTH_WEST);
    public static final PropertyBool POWERED = PropertyBool.create("powered");

    public BlockRailDetector() {
        super(true);
        this.setDefaultState(this.blockState.getBaseState().withProperty(POWERED, false).withProperty(SHAPE, BlockRailBase.EnumRailDirection.NORTH_SOUTH));
        this.setTickRandomly(true);
    }

    public int tickRate(World world) {
        return 20;
    }

    public boolean canProvidePower() {
        return true;
    }

    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entityIn) {
        if (!world.isRemote) {
            if (!state.getValue(POWERED)) {
                this.updatePoweredState(world, pos, state);
            }
        }
    }

    public void randomTick(World world, BlockPos pos, IBlockState state, Random random) {}

    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote && state.getValue(POWERED)) {
            this.updatePoweredState(world, pos, state);
        }
    }

    public int isProvidingWeakPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    public int isProvidingStrongPower(IBlockAccess world, BlockPos pos, IBlockState state, EnumFacing side) {
        return !state.getValue(POWERED) ? 0 : (side == EnumFacing.UP ? 15 : 0);
    }

    private void updatePoweredState(World world, BlockPos pos, IBlockState state) {
        boolean flag = state.getValue(POWERED);
        boolean flag1 = false;
        List<EntityMinecart> list = this.findMinecarts(world, pos, EntityMinecart.class);

        if (!list.isEmpty()) {
            flag1 = true;
        }

        if (flag1 && !flag) {
            world.setBlockState(pos, state.withProperty(POWERED, true), 3);
            world.notifyNeighborsOfStateChange(pos, this);
            world.notifyNeighborsOfStateChange(pos.down(), this);
            world.markBlockRangeForRenderUpdate(pos, pos);
        }

        if (!flag1 && flag) {
            world.setBlockState(pos, state.withProperty(POWERED, false), 3);
            world.notifyNeighborsOfStateChange(pos, this);
            world.notifyNeighborsOfStateChange(pos.down(), this);
            world.markBlockRangeForRenderUpdate(pos, pos);
        }

        if (flag1) {
            world.scheduleUpdate(pos, this, this.tickRate(world));
        }

        world.updateComparatorOutputLevel(pos, this);
    }

    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        this.updatePoweredState(world, pos, state);
    }

    public IProperty<BlockRailBase.EnumRailDirection> getShapeProperty() {
        return SHAPE;
    }

    public boolean hasComparatorInputOverride() {
        return true;
    }

    public int getComparatorInputOverride(World world, BlockPos pos) {
        if (world.getBlockState(pos).getValue(POWERED)) {
            List<EntityMinecartCommandBlock> list = this.findMinecarts(world, pos, EntityMinecartCommandBlock.class);

            if (!list.isEmpty()) {
                return list.get(0).getCommandBlockLogic().getSuccessCount();
            }

            List<EntityMinecart> list1 = this.findMinecarts(world, pos, EntityMinecart.class, EntitySelectors.selectInventories);

            if (!list1.isEmpty()) {
                return Container.calcRedstoneFromInventory((IInventory)list1.get(0));
            }
        }

        return 0;
    }

    protected <T extends EntityMinecart> List<T> findMinecarts(World world, BlockPos pos, Class<T> clazz, Predicate<Entity>... filter) {
        AxisAlignedBB axisalignedbb = this.getDectectionBox(pos);
        return filter.length != 1 ? world.getEntitiesWithinAABB(clazz, axisalignedbb) : world.getEntitiesWithinAABB(clazz, axisalignedbb, filter[0]);
    }

    private AxisAlignedBB getDectectionBox(BlockPos pos) {
        return new AxisAlignedBB((float)pos.getX() + 0.2F, pos.getY(), (float)pos.getZ() + 0.2F, (float)(pos.getX() + 1) - 0.2F, (float)(pos.getY() + 1) - 0.2F, (float)(pos.getZ() + 1) - 0.2F);
    }

    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(SHAPE, BlockRailBase.EnumRailDirection.byMetadata(meta & 7)).withProperty(POWERED, (meta & 8) > 0);
    }

    public int getMetaFromState(IBlockState state) {
        int i = 0;
        i = i | state.getValue(SHAPE).getMetadata();

        if (state.getValue(POWERED)) {
            i |= 8;
        }

        return i;
    }

    protected BlockState createBlockState() {
        return new BlockState(this, SHAPE, POWERED);
    }

}