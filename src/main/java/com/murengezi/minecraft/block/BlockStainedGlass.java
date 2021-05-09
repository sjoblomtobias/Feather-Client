package com.murengezi.minecraft.block;

import java.util.List;
import java.util.Random;
import com.murengezi.minecraft.block.material.MapColor;
import com.murengezi.minecraft.block.material.Material;
import com.murengezi.minecraft.block.properties.PropertyEnum;
import com.murengezi.minecraft.block.state.BlockState;
import com.murengezi.minecraft.block.state.IBlockState;
import com.murengezi.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.world.World;

public class BlockStainedGlass extends BlockBreakable {

    public static final PropertyEnum<EnumDyeColor> COLOR = PropertyEnum.create("color", EnumDyeColor.class);

    public BlockStainedGlass(Material material) {
        super(material, false);
        this.setDefaultState(this.blockState.getBaseState().withProperty(COLOR, EnumDyeColor.WHITE));
        this.setCreativeTab(CreativeTabs.tabBlock);
    }

    public int damageDropped(IBlockState state) {
        return state.getValue(COLOR).getMetadata();
    }

    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
        for (EnumDyeColor enumdyecolor : EnumDyeColor.values()) {
            list.add(new ItemStack(item, 1, enumdyecolor.getMetadata()));
        }
    }

    public MapColor getMapColor(IBlockState state) {
        return state.getValue(COLOR).getMapColor();
    }

    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.TRANSLUCENT;
    }

    public int quantityDropped(Random random) {
        return 0;
    }

    protected boolean canSilkHarvest() {
        return true;
    }

    public boolean isFullCube() {
        return false;
    }

    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(COLOR, EnumDyeColor.byMetadata(meta));
    }

    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            BlockBeacon.updateColorAsync(world, pos);
        }
    }

    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (!world.isRemote) {
            BlockBeacon.updateColorAsync(world, pos);
        }
    }

    public int getMetaFromState(IBlockState state) {
        return state.getValue(COLOR).getMetadata();
    }

    protected BlockState createBlockState() {
        return new BlockState(this, COLOR);
    }

}