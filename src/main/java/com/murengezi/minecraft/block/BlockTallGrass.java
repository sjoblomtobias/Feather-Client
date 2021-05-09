package com.murengezi.minecraft.block;

import java.util.List;
import java.util.Random;
import com.murengezi.minecraft.block.material.Material;
import com.murengezi.minecraft.block.properties.PropertyEnum;
import com.murengezi.minecraft.block.state.BlockState;
import com.murengezi.minecraft.block.state.IBlockState;
import com.murengezi.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.ColorizerGrass;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockTallGrass extends BlockBush implements IGrowable {

    public static final PropertyEnum<BlockTallGrass.EnumType> TYPE = PropertyEnum.create("type", BlockTallGrass.EnumType.class);

    protected BlockTallGrass() {
        super(Material.vine);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TYPE, BlockTallGrass.EnumType.DEAD_BUSH));
        float f = 0.4F;
        this.setBlockBounds(0.5F - f, 0.0F, 0.5F - f, 0.5F + f, 0.8F, 0.5F + f);
    }

    public int getBlockColor() {
        return ColorizerGrass.getGrassColor(0.5D, 1.0D);
    }

    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        return this.canPlaceBlockOn(world.getBlockState(pos.down()).getBlock());
    }

    public boolean isReplaceable(World world, BlockPos pos) {
        return true;
    }

    public int getRenderColor(IBlockState state) {
        if (state.getBlock() != this) {
            return super.getRenderColor(state);
        } else {
            BlockTallGrass.EnumType type = state.getValue(TYPE);
            return type == BlockTallGrass.EnumType.DEAD_BUSH ? 16777215 : ColorizerGrass.getGrassColor(0.5D, 1.0D);
        }
    }

    public int colorMultiplier(IBlockAccess world, BlockPos pos, int renderPass) {
        return world.getBiomeGenForCoords(pos).getGrassColorAtPos(pos);
    }

    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return rand.nextInt(8) == 0 ? Items.wheat_seeds : null;
    }

    public int quantityDroppedWithBonus(int fortune, Random random) {
        return 1 + random.nextInt(fortune * 2 + 1);
    }

    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te) {
        if (!world.isRemote && player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() == Items.shears) {
            player.triggerAchievement(StatList.mineBlockStatArray[Block.getIdFromBlock(this)]);
            spawnAsEntity(world, pos, new ItemStack(Blocks.tallgrass, 1, state.getValue(TYPE).getMeta()));
        } else {
            super.harvestBlock(world, player, pos, state, te);
        }
    }

    public int getDamageValue(World world, BlockPos pos) {
        IBlockState iblockstate = world.getBlockState(pos);
        return iblockstate.getBlock().getMetaFromState(iblockstate);
    }

    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
        for (int i = 1; i < 3; ++i) {
            list.add(new ItemStack(item, 1, i));
        }
    }

    public boolean canGrow(World world, BlockPos pos, IBlockState state, boolean isClient) {
        return state.getValue(TYPE) != BlockTallGrass.EnumType.DEAD_BUSH;
    }

    public boolean canUseBonemeal(World world, Random rand, BlockPos pos, IBlockState state) {
        return true;
    }

    public void grow(World world, Random rand, BlockPos pos, IBlockState state) {
        BlockDoublePlant.EnumPlantType plantType = BlockDoublePlant.EnumPlantType.GRASS;

        if (state.getValue(TYPE) == BlockTallGrass.EnumType.FERN) {
            plantType = BlockDoublePlant.EnumPlantType.FERN;
        }

        if (Blocks.double_plant.canPlaceBlockAt(world, pos)) {
            Blocks.double_plant.placeAt(world, pos, plantType, 2);
        }
    }

    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(TYPE, BlockTallGrass.EnumType.byMetadata(meta));
    }

    public int getMetaFromState(IBlockState state) {
        return state.getValue(TYPE).getMeta();
    }

    protected BlockState createBlockState() {
        return new BlockState(this, TYPE);
    }

    public Block.EnumOffsetType getOffsetType() {
        return Block.EnumOffsetType.XYZ;
    }

    public enum EnumType implements IStringSerializable {
        DEAD_BUSH(0, "dead_bush"), GRASS(1, "tall_grass"), FERN(2, "fern");

        private static final BlockTallGrass.EnumType[] META_LOOKUP = new BlockTallGrass.EnumType[values().length];
        private final int meta;
        private final String name;

        EnumType(int meta, String name) {
            this.meta = meta;
            this.name = name;
        }

        public int getMeta() {
            return this.meta;
        }

        public String toString() {
            return this.name;
        }

        public static BlockTallGrass.EnumType byMetadata(int meta) {
            if (meta < 0 || meta >= META_LOOKUP.length) {
                meta = 0;
            }

            return META_LOOKUP[meta];
        }

        public String getName() {
            return this.name;
        }

        static {
            for (BlockTallGrass.EnumType type : values()) {
                META_LOOKUP[type.getMeta()] = type;
            }
        }
    }
}